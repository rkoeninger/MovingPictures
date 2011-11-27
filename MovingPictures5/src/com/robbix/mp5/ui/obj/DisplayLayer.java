package com.robbix.mp5.ui.obj;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;

public enum DisplayLayer
{
	SURFACE(null),
	UNDER_UNIT(null),
	UNIT(Comparators.Z_ORDER),
	OVER_UNIT(null),
	AIR(null),
	OVERLAY(null);
	
	public final Comparator<DisplayObject> comparator;
	
	private DisplayLayer(Comparator<DisplayObject> comparator)
	{
		this.comparator = comparator;
	}
}

class Comparators
{
	static final Comparator<DisplayObject> Z_ORDER = new Comparator<DisplayObject>()
	{
		private static final int A_MORE_SOUTH = 1;
		private static final int B_MORE_SOUTH = -1;
		
		public int compare(DisplayObject dObj1, DisplayObject dObj2)
		{
			Rectangle2D a = dObj1.getBounds();
			Rectangle2D b = dObj2.getBounds();
			
			if (a.getHeight() > b.getHeight())
			{
				if (a.getY() > b.getY()) return A_MORE_SOUTH;
				if (a.getY() < b.getY()) return B_MORE_SOUTH;
			}
			else if (a.getHeight() < b.getHeight())
			{
				if (a.getY() > b.getY()) return B_MORE_SOUTH;
				if (a.getY() < b.getY()) return A_MORE_SOUTH;
			}
			
			if (a.getY() > b.getY()) return A_MORE_SOUTH;
			if (a.getY() < b.getY()) return B_MORE_SOUTH;

			if (a.getX() > b.getX()) return A_MORE_SOUTH;
			if (a.getX() < b.getX()) return B_MORE_SOUTH;
			
			return 0;
		}
	};
}
