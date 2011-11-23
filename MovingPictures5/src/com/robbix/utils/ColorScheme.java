package com.robbix.utils;

import java.awt.Color;

public class ColorScheme
{
	public static ColorScheme withEdgeOnly(Color edge)
	{
		return new ColorScheme(edge, RColor.CLEAR);
	}
	
	public static ColorScheme withFillOnly(Color fill)
	{
		return new ColorScheme(RColor.CLEAR, fill);
	}
	
	public static ColorScheme withTranslucentBody(Color edge)
	{
		return new ColorScheme(edge, new RColor(edge).toTranslucent(127));
	}
	
	public final RColor edge;
	public final RColor fill;
	
	public RColor getEdge(){ return edge; }
	public RColor getFill(){ return fill; }
	
	public ColorScheme(Color edge, Color fill)
	{
		this.edge = new RColor(edge);
		this.fill = new RColor(fill);
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
