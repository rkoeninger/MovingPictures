package com.robbix.mp5.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;

public class AStar// implements Pathfinder
{
	/**
	 * The heuristic used by this algoritm to project potential paths.
	 */
	private Heuristic heuristic;
	
	/**
	 * Constructs a new AStar algoritm using default Euclidean Distance
	 * heuristic.
	 */
	public AStar()
	{
		this(new EuclideanDistance());
	}

	/**
	 * Constructs a new AStar algoritm using the given Heuristic.
	 */
	public AStar(Heuristic heuristic)
	{
		if (heuristic == null)
			throw new NullPointerException();
		
		this.heuristic = heuristic;
	}
	
	public List<Position> getPath(CostMap course, Position start, Position end)
	{
		return getPath(course, start, end, 0);
	}
	
	/**
	 * Gets a (mostly) optimal path from start to end through the given
	 * CostMap. The CostMap can simply be the terrain texture from
	 * TerrainLayer.getCostMap() or it can be a combination of
	 * terrain, enemy positions, friendly postions, etc.
	 * 
	 * Returns null if a path cannot be found.
	 */
	public List<Position> getPath(
		CostMap course,
		Position start,
		Position end,
		double distance)
	{
		/*
		 * Go ahead and return null now if end is an unreachable spot.
		 */
		if (course.isInfinite(start) || course.isInfinite(end))
			return null;
		
		/*
		 * Get complete search tree. If it's null, then no path could be
		 * found, so return null.
		 */
		List<PosPair> nodes = getNodes(course, start, end, distance);
		
		if (nodes == null)
			return null;
		
		/*
		 * Retrace the path from the search tree. Start with the end Position
		 * and find the Position that leads to that and add it to the end of
		 * the path. Repeat until the Position at the end of the path is
		 * the start point. Path is end->start so reverse before returning.
		 */
		int manhattanDistance = Math.abs(start.x - end.x)
							  + Math.abs(start.y - end.y); 
		List<Position> path = new ArrayList<Position>(manhattanDistance);
		path.add(end);
		
		while (!path.get(path.size() - 1).equals(start))
		{
			PosPair pair = getNodePointingTo(nodes, path.get(path.size() - 1));
			path.add(pair.from);
		}
		
		Collections.reverse(path);
		
		if (distance == 1)
			path = path.subList(0, path.size() - 1);
		
		return path;
	}
	
	/**
	 * Gets a list of nodes that make up the search tree.
	 * The last node will be the one that goes from the
	 * second-to-last postion to the destination.
	 * 
	 * Returns null if all alternative paths were exhausted and one could not
	 * be found.
	 */
	private List<PosPair> getNodes(
		CostMap course,
		Position start,
		Position end,
		double distance)
	{
		Map<Position, Cost> openSet = new HashMap<Position, Cost>(64);
		Set<Position> closedSet = new HashSet<Position>(128);
		List<PosPair> nodes = new LinkedList<PosPair>();
		
		double startF = heuristic.project(course, start, end);
		openSet.put(start, new Cost(0, startF));
		
		/*
		 * Loop while there are still potential positions to branch into
		 * and explore. If this loop ends before the destination is found,
		 * then there isn't any way to reach the destination and the method
		 * returns null.
		 */
		while (!openSet.isEmpty())
		{
			Position current = null;
			double lowestFCost = Double.POSITIVE_INFINITY;
			
			/*
			 * Find the position in the open set with the lowest f-value,
			 * we'll branch from there.
			 */
			for (Map.Entry<Position, Cost> entry : openSet.entrySet())
				if (entry.getValue().f < lowestFCost)
				{
					lowestFCost = entry.getValue().f;
					current = entry.getKey();
				}
			
			/*
			 * If we found the end, we're done.
			 */
			if (end.equals(current))
				return Arrays.asList(nodes.toArray(new PosPair[0]));
			
			/*
			 * Add current position to the closed set so it doesn't
			 * get visited again.
			 */
			Cost currentCost = openSet.remove(current);
			closedSet.add(current);
			
			/*
			 * Check neighbor in each of 8 directions to see if it is
			 * on the map, passable, and hasn't been added to the closed set.
			 * 
			 * Neighbor will also be ignored if there is already a position
			 * in the open set of lesser or equal g (measured) value.
			 */
			for (Direction dir : Direction.getIterator(Direction.E, 2))
			{
				Position neighbor = dir.apply(current);
				
				/*
				 * Consider neighbor only if it is on the map,
				 * it is not in the closed set of already considered nodes
				 * and is passable.
				 */
				if ((!course.getBounds().contains(neighbor))
				|| closedSet.contains(neighbor)
				|| Double.isInfinite(course.get(neighbor)))
					continue;
				
				double moveFactor = dir.isDiagonal() ? 1.414 : 1;
				Cost neighborCost = new Cost(
					currentCost.g + course.get(neighbor) * moveFactor,
					heuristic.project(course, neighbor, end)
				);
				
				/*
				 * Add neighbor to openSet and current->neighbor to node list
				 * if the node hasn't been visited yet or it has and the
				 * g value this time around is less than it was last time.
				 */
				if (!openSet.containsKey(neighbor)
				|| neighborCost.g < openSet.get(neighbor).g)
				{
					/*
					 * Remove the old from->to PosPair from the nodes list as
					 * this way is cheaper and we don't want the old way
					 * being traced with the path is constructed.
					 */
					Iterator<PosPair> nodeIterator = nodes.iterator();
					
					while (nodeIterator.hasNext())
					{
						PosPair oldPair = nodeIterator.next();
						
						if (oldPair.to.equals(neighbor))
							nodeIterator.remove();
					}

					nodes.add(new PosPair(current, neighbor));
					openSet.put(neighbor, neighborCost);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Finds which node in the list of Position pairs leads to the given
	 * Position.
	 * 
	 * Returns null if such a node is not present.
	 */
	private static PosPair getNodePointingTo(List<PosPair> pairs, Position pos)
	{
		for (PosPair pair : pairs)
			if (pair.to.equals(pos))
				return pair;
		
		return null;
	}
	
	/**
	 * Simple value-pair that represents g (measured cost) and f
	 * (measured + projected cost).
	 * 
	 * Constructor takes g and h as aruguments, producing f = g + h.
	 * Doesn't store h as it is never called for later.
	 */
	private static class Cost
	{
		/*
		 * Only necessary to remember measured cost up to that
		 * point and measured cost plus heuristic predicted cost.
		 */
		public double f, g;
		
		public Cost(double g, double h)
		{
			this.f = g + h;
			this.g = g;
		}
	}
	
	/**
	 * Represents a branch in the search tree.
	 */
	private static class PosPair
	{
		public final Position from, to;
		
		public PosPair(Position from, Position to)
		{
			this.from = from;
			this.to = to;
		}
	}
	
	/**
	 * Finds the square of the straight-line distance.
	 */
//	private static double sqDistance(Position a, Position b)
//	{
//		return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
//	}
}
