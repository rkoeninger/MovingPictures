package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;

public enum DisplayLayer
{
	SURFACE,
	UNDER_UNIT,
	UNIT(Comparators.Z_ORDER),
	OVER_UNIT,
	AIR,
	OVERLAY;
	
	public final Comparator<DisplayObject> comparator;
	
	private DisplayLayer()
	{
		this.comparator = null;
	}
	
	private DisplayLayer(Comparator<DisplayObject> comparator)
	{
		this.comparator = comparator;
	}
}

class Comparators
{
	static final Comparator<DisplayObject> Z_ORDER = new Comparator<DisplayObject>()
	{
		private static final int A_ON_TOP = 1;
		private static final int B_ON_TOP = -1;
		
		public int compare(DisplayObject dObj1, DisplayObject dObj2)
		{
			Rectangle2D a = dObj1.getBounds();
			Rectangle2D b = dObj2.getBounds();
			
			if (a.getY() > b.getY())
			{
				if (a.getMaxY() > b.getMaxY())
				{
					return A_ON_TOP;
				}
				if (a.getMaxY() < b.getMaxY())
				{
					return a.getMaxY() >= b.getMaxY() - 1 ? A_ON_TOP : B_ON_TOP;
				}
			}
			if (b.getY() > a.getY())
			{
				if (b.getMaxY() > a.getMaxY())
				{
					return B_ON_TOP;
				}
				if (b.getMaxY() < a.getMaxY())
				{
					return b.getMaxY() >= a.getMaxY() - 1 ? B_ON_TOP : A_ON_TOP;
				}
			}
			if (a.getX() > b.getX())
			{
				if (a.getMaxX() > b.getMaxX())
				{
					return A_ON_TOP;
				}
				if (a.getMaxX() < b.getMaxX())
				{
					return a.getMaxX() >= b.getMaxX() - 1 ? A_ON_TOP : B_ON_TOP;
				}
			}
			if (b.getX() > a.getX())
			{
				if (b.getMaxX() > a.getMaxX())
				{
					return B_ON_TOP;
				}
				if (b.getMaxX() < a.getMaxX())
				{
					return b.getMaxX() >= a.getMaxX() - 1 ? B_ON_TOP : A_ON_TOP;
				}
			}
			
			return 0;
		}
	};
}
