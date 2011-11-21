package com.robbix.utils;

import java.awt.Color;

public class ColorScheme
{
	public static ColorScheme withEdgeOnly(Color edge)
	{
		return new ColorScheme(edge, Utils.CLEAR);
	}
	
	public static ColorScheme withFillOnly(Color fill)
	{
		return new ColorScheme(Utils.CLEAR, fill);
	}
	
	public static ColorScheme withTranslucentBody(Color edge)
	{
		return new ColorScheme(edge, Utils.getTranslucency(edge, 0.5f));
	}
	
	public final Color edge;
	public final Color fill;
	
	public Color getEdge(){ return edge; }
	public Color getFill(){ return fill; }
	
	public ColorScheme(Color edge, Color fill)
	{
		this.edge = edge;
		this.fill = fill;
	}
	
	public ColorScheme getEdgeOnly()
	{
		return withEdgeOnly(edge);
	}
	
	public ColorScheme getFillOnly()
	{
		return withFillOnly(fill);
	}
}
