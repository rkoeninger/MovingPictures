package com.robbix.mp5.basics;

import static java.lang.Math.PI;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * For this class, it is safe to use the equality operator ( == ), as there
 * are a fixed number of instances of this class, all unique. Similarly, 
 * the equals(), clone() and hashCode() methods have not been overridden.
 * 
 * Absolute angle/steps values are measured starting from East = 0 with
 * positive value representing counter-clockwise rotation.
 * All "angles" are measured in revs.
 * Each "step" is a sixteenth-turn (or sixteenth-rev).
 * 
 * @author bort
 */
public class Direction
{
	/*
	 * References to all directions. There are no other
	 * instances of this class other than these.
	 */
	public static final Direction E, ENE, NE, NNE;
	public static final Direction N, NNW, NW, WNW;
	public static final Direction W, WSW, SW, SSW;
	public static final Direction S, SSE, SE, ESE;
	
	/**
	 * Returns an Iterator that will list off all 16 Directions starting
	 * with East and ending with East-South-East.
	 */
	public static IterableIterator<Direction> getIterator()
	{
		return new DirectionIterator(Direction.E, 1);
	}
	
	/**
	 * Returns an Iterator that will list off Directions starting at the
	 * given start Direction and each successive Direction {@code step}
	 * steps apart.
	 * 
	 * The argument {@code step} may be negative, to iterate clockwise instead
	 * of counter-clockwise, but it must be a divisor of 16. So acceptable
	 * step values are {1, 2, 4, 8, 16} and their negatives.
	 */
	public static IterableIterator<Direction> getIterator(
		Direction start,
		int step)
	{
		if (start == null)
			throw new IllegalArgumentException("Starting direction is null");
		if (16 % step != 0)
			throw new IllegalArgumentException("Step increment must be even");
		
		return new DirectionIterator(start, step);
	}

	/**
	 * Returns an Iterator that will list off Directions alternate to the
	 * given start Direction. Seven directions will be listed in total.
	 * Only starts with and only returns movable Directions
	 * (not sixteenth-turns).
	 * 
	 * For equally-distant alternative Directions, the counter-clockwise one
	 * will be returned first.
	 * 
	 * e.g. Alternatives for North will be listed:
	 * <pre>
	 *     North-West -- Counter-clockwise first 
	 *     North-East
	 *     West
	 *     East
	 *     South-West
	 *     South-East
	 *     South      -- Reverse last
	 * </pre>
	 */
	public static IterableIterator<Direction> getAlternatives(Direction start)
	{
		if (start == null)
			throw new IllegalArgumentException("Starting direction is null");
		if (start.isThirdOrder())
			throw new IllegalArgumentException("Sixteenth turns not allowed");
		
		return new AlternativeIterator(start);
	}
	
	/**
	 * Gets the Direction which is the given number of steps from East.
	 */
	public static Direction getDirection(int steps)
	{
		return angleMap[((steps % 16) + 16) % 16];
	}
	
	/**
	 * Gets the Direction whose angle is closest to the given angle from East.
	 * 
	 * Angle is measured in revs.
	 */
	public static Direction getDirection(double angle)
	{
		return angleMap[revsTo16Steps(angle)];
	}
	
	/**
	 * Gets the Direction that most closely points from
	 * (x, y) to (x + dx, y + dy).
	 */
	public static Direction getDirection(int dx, int dy)
	{
		// y-axis is inverted, so dy is inverted
		return angleMap[revsTo16Steps(Math.atan2(-dy, dx) / (2 * PI))];
	}

	/**
	 * Gets the Direction that most closely points from a to b.
	 */
	public static Direction getDirection(Position a, Position b)
	{
		// y-axis is inverted, so dy is inverted
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		
		if (dx == 0 && dy == 0)
			return null;
		
		return angleMap[revsTo16Steps(Math.atan2(-dy, dx) / (2 * PI))];
	}
	
	/**
	 * Gets the Direction that most closely points from a to b.
	 * 
	 * This method only returns eighth-turn (second-order) Directions as it
	 * is used for navigating the grid and Units can only move in
	 * eigth-turn Directions.
	 */
	public static Direction getMoveDirection(Position a, Position b)
	{
		// y-axis is inverted, so dy is inverted
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		return angleMap[revsTo8Steps(Math.atan2(-dy, dx) / (2 * PI))];
	}
	
	/**
	 * Gets the Direction by the given short name. Returns null if
	 * the there is no Direction by that short name.
	 * 
	 * Valid short names:
	 * e, ene, ne, nne, n, nnw, nw, wnw, w, wsw, sw, ssw, s, sse, se, ese.
	 */
	public static Direction getDirection(String shortName)
	{
		return shortNameMap.get(shortName);
	}

	/**
	 * Returns the cosine of this Direction's angle.
	 * 
	 * This method is applicable to all Directions.
	 */
	public double cos()
	{
		return Math.cos(angle * (2 * PI));
	}

	/**
	 * Returns the sine of this Direction's angle.
	 *
	 * Value is negated before being returned so caller doesn't
	 * have to consider this. (y-axis on screen is inverted).
	 * 
	 * This method is applicable to all Directions.
	 */
	public double sin()
	{
		// Math.sin treats y-axis differently than screen co-ordinates
		return -Math.sin(angle * (2 * PI));
	}

	/**
	 * Returns the tangent of this Direction's angle.
	 *
	 * Value is negated before being returned so caller doesn't
	 * have to consider this.
	 * 
	 * This method is applicable to all Directions.
	 */
	public double tan()
	{
		// Math.tan treats y-axis differently than screen co-ordinates
		return -Math.tan(angle * (2 * PI));
	}
	
	/**
	 * Gets the x-coordinate offset this Direction applies to a Postition.
	 * 
	 * This method is not applicable to sixteenth-turns (i.e. NNE, WSW, etc).
	 * 
	 * @throws IllegalStateException If this Direction is a sixteenth-turn.
	 */
	public int getDX()
	{
		if (isThirdOrder())
			throw new IllegalStateException(SIXTEENTH_DX_DY);
		
		return dx;
	}

	/**
	 * Gets the y-coordinate offset this Direction applies to a Postition.
	 * 
	 * This method is not applicable to sixteenth-turns (i.e. NNE, WSW, etc).
	 * 
	 * @throws IllegalStateException If this Direction is a sixteenth-turn.
	 */
	public int getDY()
	{
		if (isThirdOrder())
			throw new IllegalStateException(SIXTEENTH_DX_DY);
		
		return dy;
	}
	
	/**
	 * Gets hash code for this Direction.
	 * 
	 * Hash codes are assigned sequentially. This should allow Directions
	 * to work very efficently with HashMaps with a size of 16.
	 */
	public int hashCode()
	{
		return steps | 0x10;
	}
	
	/**
	 * Gets the full, human-readable name of this Direction.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Gets the short, code-name of this Direction.
	 */
	public String getShortName()
	{
		return shortName;
	}
	
	/**
	 * Returns the short name of this Direciton.
	 */
	public String toString()
	{
		return shortName;
	}
	
	/**
	 * Gets a Position object shifted <dx, dy> from the given Position.
	 * 
	 * @throws IllegalStateException If this Direction is a sixteenth-turn.
	 */
	public Position apply(Position pos)
	{
		if (isThirdOrder())
			throw new IllegalStateException(SIXTEENTH_DX_DY);
		
		return pos.shift(dx, dy);
	}
	
	/**
	 * Returns a the Direction that is this Direction rotated by
	 * the given angle.
	 * 
	 * <b>Be sure to cast to double if rotation by angle is intended.</b>
	 * 
	 * Angle is measured in radians with positive values representing
	 * counter-clockwise and negative values representing clockwise.
	 * 
	 * Eighth-turns (i.e. North to North-West) are 1/8 revs.
	 * Sixteenth-turns (i.e. South-East to East-South-East) are 1/16 revs.
	 * 
	 * @see rotate(int)
	 */
	public Direction rotate(double angle)
	{
		int steps = revsTo16Steps(angle);
		return angleMap[(((this.steps + steps) % 16) + 16) % 16];
	}
	
	/**
	 * Returns a the Direction that is this Direction rotated by
	 * the given number of sixteenth-turn steps.
	 * 
	 * <b>Be sure to cast to int if rotation by steps is intended.</b>
	 * 
	 * Each step represents a sixteenth-turn, so a rotation from
	 * North to North-West would require 2 positive steps, not 1.
	 * 
	 * Positive values represent counter-clockwise rotation and
	 * negative values represent clockwise rotation.
	 * 
	 * @see rotate(double)
	 */
	public Direction rotate(int steps)
	{
		return angleMap[(((this.steps + steps) % 16) + 16) % 16];
	}
	
	/**
	 * Gets the Direction that is a 180-degree turn from this Direction.
	 */
	public Direction reverse()
	{
		return rotate(8);
	}
	
	/**
	 * Gets the Direction flipped over the y-axis.
	 */
	public Direction flipHorizontal()
	{
		return getDirection(-dx, dy);
	}

	/**
	 * Gets the Direction flipped over the x-axis.
	 */
	public Direction flipVertical()
	{
		return getDirection(dx, -dy);
	}
	
	/**
	 * Returns true if this Direction is a sixteenth-turn
	 * (aka a third-order turn).
	 */
	public boolean isThirdOrder()
	{
		return (Math.abs(dx) == 2 || Math.abs(dy) == 2);
	}
	
	/**
	 * Returns true if this Direction is an eighth-turn
	 * (aka a second-order turn).
	 */
	public boolean isSecondOrder()
	{
		return (Math.abs(dx) == 1 || Math.abs(dy) == 1);
	}
	
	/**
	 * Returns true if this Direction is an fourth-turn
	 * (aka a first-order turn).
	 */
	public boolean isFirstOrder()
	{
		return (Math.abs(dx) == 1 ^ Math.abs(dy) == 1);
	}
	
	/**
	 * Returns true if this Direction is diagonal -
	 * i.e. it is one of: NE, NW, SW, SE.
	 */
	public boolean isDiagonal()
	{
		return (Math.abs(dx) == 1 && Math.abs(dy) == 1);
	}
	
	/**
	 * Returns true if this Direction is vertical -
	 * i.e. it is either N or S.
	 */
	public boolean isVertical()
	{
		return dx == 0;
	}
	
	/**
	 * Returns true if this Direction is horizontal -
	 * i.e. it is either E or W.
	 */
	public boolean isHorizontal()
	{
		return dy == 0;
	}
	
	/**
	 * Gets the angle (in radians) this Direction points in.
	 */
	public double getAngle()
	{
		return angle;
	}
	
	/**
	 * Gets the step index of this Direction - i.e. the number of
	 * sixteenth-turn steps from East this Direction is.
	 */
	public int getStepIndex()
	{
		return steps;
	}
	
	/**
	 * Gets the displacement in sixteenth-turn steps.
	 */
	public int getDisplacement(Direction that)
	{
		return revsTo16Steps(getDisplacementAngle(that));
	}

	/**
	 * Gets the counter-clockwise (positive) displacement angle.
	 * 
	 * If the returned value is greate than pi, then it will be quicker to
	 * turn in the clockwise (negative) direction.
	 */
	public double getDisplacementAngle(Direction that)
	{
		return that.angle - this.angle;
	}
	
	/**
	 * Human-readable representation of this Direction's name.
	 */
	private final String name;
	
	/**
	 * Short machine-readable code-name representation of this Direction.
	 */
	private final String shortName;
	
	/**
	 * The x-coordinate offset this Direction applies to a Position.
	 */
	private final int dx;
	
	/**
	 * The y-coordinate offset this Direction applies to a Position.
	 */
	private final int dy;
	
	/**
	 * The angle in the range [0, 1) this Direction points in.
	 */
	private final double angle;
	
	/**
	 * The number of sixteenth-turn steps this Direction is rotated from E.
	 */
	private final int steps;
	
	/**
	 * Private constructor. External code should not be creating
	 * instances of this class.
	 */
	private Direction(int dx, int dy, String name, String shortName)
	{
		this.dx = dx;
		this.dy = dy;
		this.name = name;
		this.shortName = shortName;
		
		// y-axis is inverted, so dy is inverted
		steps = revsTo16Steps(Math.atan2(-dy, dx) / (2 * PI));
		angle = steps / 16.0;
		angleMap[steps] = this;
		shortNameMap.put(shortName, this);
	}
	
	/**
	 * Converts an angle in the range (-Inf, Inf) to an absolute step value
	 * in the range [0, 16).
	 * 
	 * Result is accurate to sixteenth-steps.
	 */
	private static int revsTo16Steps(double angle)
	{
		return ((((int) Math.round(angle * 16)) % 16) + 16) % 16;
	}
	
	/**
	 * Converts an angle in the range (-Inf, Inf) to an absolute step value
	 * in the range [0, 8).
	 * 
	 * Result is accurate to eighth-steps.
	 */
	private static int revsTo8Steps(double angle)
	{
		return ((((int) Math.round(angle * 8)) % 8) + 8) % 8 * 2;
	}
	
	/**
	 * shortNameMap is used to look up a direction by its short name.
	 */
	private static Map<String, Direction> shortNameMap;
	
	/**
	 * angleMap is used to look up a direction by the number
	 * of steps it is from East.
	 */
	private static Direction[] angleMap;
	
	/**
	 * Create lookup tables, create all Direction instances.
	 */
	static
	{
		shortNameMap = new HashMap<String, Direction>();
		angleMap = new Direction[16];
		
		N   = new Direction( 0, -1, "North",      "n");
		S   = new Direction( 0,  1, "South",      "s");
		E   = new Direction( 1,  0, "East",       "e");
		W   = new Direction(-1,  0, "West",       "w");
		NE  = new Direction( 1, -1, "North-East", "ne");
		NW  = new Direction(-1, -1, "North-West", "nw");
		SE  = new Direction( 1,  1, "South-East", "se");
		SW  = new Direction(-1,  1, "South-West", "sw");
		
		/*
		 * The dx, dy for sixteenth-turns are just placeholders.
		 * They can be used to find the direction's angle,
		 * but are not meaningful as offsets.
		 * 
		 * Units should never travel in these directions.
		 */
		NNW = new Direction(-2, -5, "North-North-West", "nnw");
		NNE = new Direction( 2, -5, "North-North-East", "nne");
		SSW = new Direction(-2,  5, "South-South-West", "ssw");
		SSE = new Direction( 2,  5, "South-South-East", "sse");
		WNW = new Direction(-5, -2, "West-North-West",  "wnw");
		ENE = new Direction( 5, -2, "East-North-East",  "ene");
		WSW = new Direction(-5,  2, "West-South-West",  "wsw");
		ESE = new Direction( 5,  2, "East-South-East",  "ese");
	}
	
	/**
	 * Iterator that lists off Directions from given starting direction
	 * and rotation increment.
	 */
	private static class DirectionIterator extends IterableIterator<Direction>
	{
		private final Direction start;
		private Direction next;
		private int step;
		
		private boolean stillOnFirst = true;
		
		public DirectionIterator(Direction start, int step)
		{
			this.start = start;
			this.next = start;
			this.step = step;
		}
		
		public boolean hasNext()
		{
			return (next != start || stillOnFirst);
		}
		
		public Direction next()
		{
			if (next == start && !stillOnFirst)
				throw new NoSuchElementException();
			
			stillOnFirst = false;
			
			try     { return next;              }
			finally { next = next.rotate(step); }
		}
	}
	
	/**
	 * Iterator that lists off Directions alternative to the starting direction
	 * listing the closest ones first and the reverse last.
	 */
	private static class AlternativeIterator extends IterableIterator<Direction>
	{
		private final Direction start;
		private int alternative = 0;
		private int alternativeMax = 6;
		
		public AlternativeIterator(Direction start)
		{
			this.start = start;
		}
		
		public boolean hasNext()
		{
			return alternative <= alternativeMax;
		}
		
		public Direction next()
		{
			if (alternative > alternativeMax)
				throw new NoSuchElementException();
			
			if (alternative % 2 == 0)
			{
				// Counter-clockwise
				return start.rotate(((alternative++ / 2) + 1) * 2);
			}
			else
			{
				// Clockwise
				return start.rotate(-((alternative++ / 2) + 1) * 2);
			}
		}
	}
	
	private static final String SIXTEENTH_DX_DY =
		"Offsets <dx, dy> are undefined for sixteenth-turn directions";
}
