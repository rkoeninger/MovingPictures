package com.robbix.mp5.utils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


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
	/**
	 * Loads a CostMap from a bitmap image. The CostMap will be the
	 * same size as the bitmap is in pixels. The value at each position
	 * in the CostMap will be the average of the three color components
	 * of each pixel in the bitmap.
	 * 
	 * @throws IOException If there is any problem reading from file.
	 */
	public static CostMap loadBitmap(File file) throws IOException
	{
		BufferedImage bitmap = (BufferedImage) ImageIO.read(file);
		WritableRaster raster = bitmap.getRaster();
		CostMap costMap = new CostMap(bitmap.getWidth(), bitmap.getHeight());
		float[] pixel = new float[4];
		double div = 3 * 255;
		
		for (int y = 0; y < costMap.getHeight(); ++y)
		for (int x = 0; x < costMap.getWidth();  ++x)
		{
			raster.getPixel(x, y, pixel);
			double gray = (pixel[0] + pixel[1] + pixel[2]) / div;
			costMap.setScaleFactor(x, y, gray);
		}
		
		return costMap;
	}
	
	public void saveBitmap(File file) throws IOException
	{
		BufferedImage bitmap = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = bitmap.getRaster();
		float[] pixel = new float[4];
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
		{
			pixel[0] = pixel[1] = pixel[2] = (float) getScaleFactor(x, y);
			raster.setPixel(x, y, pixel);
		}
		
		ImageIO.write(bitmap, "bmp", file);
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
	
	private static final String NOT_SAME_DIMS =
		"Maps must be of the same dimensions";
	
	private static final String NEGATIVE_WEIGHT =
		"Add weights must be non-negative";
	
	private static final String NEGATIVE_COST =
		"Cost values must be in the range: [0, Inf]";
	
	private static final String SCALE_FACTOR_RANGE =
		"Scale factors must be in the range: [0, 1]";
}
