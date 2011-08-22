package com.robbix.mp5.basics;


/**
 * Cost values should be in the range [0 ... Infinity].
 * Scale factors are in the range [0, 1]. They equal grayscale values.
 * 
 * ScaleFactor = 1 / (1 + Cost)
 * Cost = (1 / ScaleFactor) - 1
 * 
 * ***Maybe add methods need to be changed.
 * Simply adding values might not reflect the proper combination of costs.
 * 
 * ***And linear scale values might not make sense either.
 * Scaling might need to be done exponentially.
 * 
 * @author bort
 */
public class CostMap extends Grid<Double>
{
	public static CostMap add(CostMap mapA, double weightA,
							  CostMap mapB, double weightB)
	{
		if (mapA.w != mapB.w || mapA.h != mapB.h)
			throw new IllegalArgumentException(NOT_SAME_DIMS);
		if (weightA < 0.0 || weightB < 0.0)
			throw new IllegalArgumentException(NEGATIVE_WEIGHT);
		
		CostMap sum = new CostMap(mapA.w, mapA.h);
		
		for (int y = 0; y < sum.h; ++y)
		for (int x = 0; x < sum.w; ++x)
			sum.set(x, y, mapA.get(x, y) * weightA
						+ mapB.get(x, y) * weightB);
		
		return sum;
	}

	/**
	 * Creates a "free" costmap where all cells contain a cost of zero.
	 */
	public CostMap(int w, int h)
	{
		super(w, h, 0.0);
	}
	
	public CostMap(int w, int h, double initValue)
	{
		super(w, h, initValue);
		
		if (initValue < 0.0)
			throw new IllegalArgumentException(NEGATIVE_COST);
	}
	
	public boolean isFree(Position pos)
	{
		return super.get(pos) == 0;
	}
	
	public boolean isInfinite(Position pos)
	{
		return Double.isInfinite(super.get(pos));
	}
	
	public double getScaleFactor(int x, int y)
	{
		return 1 / (1 + super.get(x, y));
	}
	
	public double getScaleFactor(Position pos)
	{
		return 1 / (1 + super.get(pos));
	}
	
	public void setScaleFactor(int x, int y, double factor)
	{
		if (factor < 0 || factor > 1)
			throw new IllegalArgumentException(SCALE_FACTOR_RANGE);
		
		set(x, y, (1 / factor) - 1);
	}
	
	public void setScaleFactor(Position pos, double factor)
	{
		if (factor < 0 || factor > 1)
			throw new IllegalArgumentException(SCALE_FACTOR_RANGE);
		
		set(pos, (1 / factor) - 1);
	}
	
	public void set(int x, int y, double value)
	{
		if (value < 0.0)
			throw new IllegalArgumentException(NEGATIVE_COST);
		
		super.set(x, y, value);
	}
	
	public void set(Position pos, double value)
	{
		if (value < 0.0)
			throw new IllegalArgumentException(NEGATIVE_COST);
		
		super.set(pos, value);
	}
	
	public void setInfinite(Position pos)
	{
		setInfinite(pos.x, pos.y);
	}
	
	public void setInfinite(int x, int y)
	{
		super.set(x, y, Double.POSITIVE_INFINITY);
	}
	
	public void setZero(Position pos)
	{
		setZero(pos.x, pos.y);
	}
	
	public void setZero(int x, int y)
	{
		super.set(x, y, new Double(0));
	}
	
	public void scale(double weight)
	{
		if  (weight < 0.0)
			throw new IllegalArgumentException(NEGATIVE_WEIGHT);
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			set(x, y, get(x, y) * weight);
	}
	
	public void invert()
	{
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			set(x, y, 1.0 / get(x, y));
	}

	public void add(CostMap that)
	{
		if (w != that.w || h != that.h)
			throw new IllegalArgumentException(NOT_SAME_DIMS);

		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			set(x, y, get(x, y) + that.get(x, y));
	}
	
	public void add(CostMap that, double weight)
	{
		if (w != that.w || h != that.h)
			throw new IllegalArgumentException(NOT_SAME_DIMS);
		if (weight < 0.0)
			throw new IllegalArgumentException(NEGATIVE_WEIGHT);

		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			set(x, y, get(x, y) + that.get(x, y) * weight);
	}

	private static final String NOT_SAME_DIMS =
		"Maps must be of the same dimensions";
	
	private static final String NEGATIVE_WEIGHT =
		"Add weights must be non-negative";
	
	private static final String NEGATIVE_COST =
		"Cost values must be in the range: [0, Inf]";
	
	private static final String SCALE_FACTOR_RANGE =
		"Scale factors must be in the range: [0, 1]";
}
