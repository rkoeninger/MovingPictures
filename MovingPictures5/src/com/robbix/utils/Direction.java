package com.robbix.utils;

import static java.lang.Math.PI;

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
public enum Direction
{
	/*
	 * <dx, dy> are undefined for 16th-turns (e.g. ENE, SSW).
	 * angle is measured in counter-clockwise revs from East.
	 */
	//  dx  dy  angle   longName
	E  ( 1,  0, 0,      "East"),
	ENE( 0,  0, 0.0625, "East-North-East"),
	NE ( 1, -1, 0.125,  "North-East"),
	NNE( 0,  0, 0.1875, "North-North-East"),
	N  ( 0, -1, 0.25,   "North"),
	NNW( 0,  0, 0.3125, "North-North-West"),
	NW (-1, -1, 0.375,  "North-West"),
	WNW( 0,  0, 0.4375, "West-North-West"),
	W  (-1,  0, 0.5,    "West"),
	WSW( 0,  0, 0.5625, "West-South-West"),
	SW (-1,  1, 0.625,  "South-West"),
	SSW( 0,  0, 0.6875, "South-South-West"),
	S  ( 0,  1, 0.75,   "South"),
	SSE( 0,  0, 0.8125, "South-South-East"),
	SE ( 1,  1, 0.875,  "South-East"),
	ESE( 0,  0, 0.9375, "East-South-East");
	
	public static Direction getDefault()
	{
		return E;
	}
	
	/**
	 * Returns an Iterator that will list off all 16 Directions starting
	 * with East and ending with East-South-East.
	 */
	public static RIterator<Direction> getIterator()
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
	public static RIterator<Direction> getIterator(Direction start, int step)
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
	public static RIterator<Direction> getAlternatives(Direction start)
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
		return values()[(steps % 16 + 16) % 16];
	}
	
	/**
	 * Gets the Direction whose angle is closest to the given angle from East.
	 * 
	 * Angle is measured in revs.
	 */
	public static Direction getDirection(double angle)
	{
		return values()[revsTo16Steps(angle)];
	}
	
	/**
	 * Gets the Direction that most closely points from
	 * (x, y) to (x + dx, y + dy).
	 */
	public static Direction getDirection(int dx, int dy)
	{
		// y-axis is inverted, so dy is inverted
		return values()[revsTo16Steps(Math.atan2(-dy, dx) / (2 * PI))];
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
		
		return values()[revsTo16Steps(Math.atan2(-dy, dx) / (2 * PI))];
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
		return values()[revsTo8Steps(Math.atan2(-dy, dx) / (2 * PI))];
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
		return Enum.valueOf(Direction.class, shortName.toUpperCase());
	}

	/**
	 * Returns the cosine of this Direction's angle.
	 * 
	 * This method is applicable to all Directions.
	 */
	public double cos()
	{
		return Math.cos(angle * 2 * PI);
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
		return -Math.sin(angle * 2 * PI);
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
		return -Math.tan(angle * 2 * PI);
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
	 * Gets the full, human-readable name of this Direction.
	 */
	public String getName()
	{
		return longName;
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
		return values()[((ordinal() + steps) % 16 + 16) % 16];
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
		return values()[((ordinal() + steps) % 16 + 16) % 16];
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
		return dx == 0 && dy == 0;
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
		return dx == 0 && dy != 0;
	}
	
	/**
	 * Returns true if this Direction is horizontal -
	 * i.e. it is either E or W.
	 */
	public boolean isHorizontal()
	{
		return dx != 0 && dy == 0;
	}
	
	/**
	 * Gets the angle (in revs) this Direction points in.
	 */
	public double getAngle()
	{
		return angle;
	}
	
	/**
	 * Gets the angle (in radians) this Direction points in.
	 */
	public double getRadianAngle()
	{
		return angle * PI * 2;
	}
	
	/**
	 * Gets the angle (in radians) this Direction points in.
	 */
	public double getDegreeAngle()
	{
		return angle * 360.0;
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
	private final String longName;
	
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
	 * Measured in revolutions.
	 */
	private final double angle;
	
	/**
	 * <dx, dy> is undefined for 16th-turns.
	 */
	private Direction(int dx, int dy, double angle, String name)
	{
		this.dx = dx;
		this.dy = dy;
		this.angle = angle;
		this.longName = name;
	}
	
	/**
	 * Converts an angle in the range (-Inf, Inf) to an absolute step value
	 * in the range [0, 16).
	 * 
	 * Result is accurate to sixteenth-steps.
	 */
	private static int revsTo16Steps(double angle)
	{
		return (((int) Math.round(angle * 16)) % 16 + 16) % 16;
	}
	
	/**
	 * Converts an angle in the range (-Inf, Inf) to an absolute step value
	 * in the range [0, 8).
	 * 
	 * Result is accurate to eighth-steps.
	 */
	private static int revsTo8Steps(double angle)
	{
		return (((int) Math.round(angle * 8)) % 8 + 8) % 8 * 2;
	}
	
	/**
	 * Iterator that lists off Directions from given starting direction
	 * and rotation increment.
	 */
	private static class DirectionIterator extends RIterator<Direction>
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
			checkHasNext();
			
			stillOnFirst = false;
			
			try     { return next;              }
			finally { next = next.rotate(step); }
		}
	}
	
	/**
	 * Iterator that lists off Directions alternative to the starting direction
	 * listing the closest ones first and the reverse last.
	 */
	private static class AlternativeIterator extends RIterator<Direction>
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
			checkHasNext();
			
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
