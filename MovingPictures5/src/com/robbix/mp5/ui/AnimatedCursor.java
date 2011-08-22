package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.robbix.mp5.Utils;

public class AnimatedCursor
{
	public static AnimatedCursor load(File xmlFile) throws IOException
	{
		File parent;
		
		if (xmlFile.isDirectory())
		{
			parent = xmlFile;
			xmlFile = new File(parent, parent.getName() + ".xml");
		}
		else
		{
			parent = xmlFile.getParentFile();
		}
		
		Document doc = Utils.loadXML(xmlFile, false);
		Node rootNode = Utils.getNode(doc, "AnimatedCursor");
		
		String name = Utils.getAttribute(rootNode, "name");
		Color bgColor = Color.decode(Utils.getAttribute(rootNode, "bgcolor"));
		
		int totalDuration = Utils.getIntAttribute(rootNode, "duration", -1);
		int totalHotspotX = Utils.getIntAttribute(rootNode, "hotspotX", -1);
		int totalHotspotY = Utils.getIntAttribute(rootNode, "hotspotY", -1);

		List<Node> frameNodes = Utils.getNodes(rootNode, "Frame");
		
		if (frameNodes.isEmpty())
			throw new IOException("Expected 1 or more <Frame> tags");
		
		AnimatedCursor aCur = new AnimatedCursor(name, frameNodes.size());
		int frameIndex = 0;
		
		for (Node frameNode : frameNodes)
		{
			String path = Utils.getAttribute(frameNode, "image");
			String durationString = Utils.getAttribute(frameNode, "duration", null);
			String hotspotXString = Utils.getAttribute(frameNode, "hotspotX", null);
			String hotspotYString = Utils.getAttribute(frameNode, "hotspotY", null);
			
			try
			{
				int duration;
				
				if (durationString != null)
					duration = Integer.parseInt(durationString);
				else if (totalDuration > 0)
					duration = totalDuration / frameNodes.size();
				else
					throw new IOException(
						"Frame duration not defined for frame " + frameIndex
					);
				
				int hotspotX;
				
				if (hotspotXString != null)
					hotspotX = Integer.parseInt(hotspotXString);
				else if (totalHotspotX >= 0)
					hotspotX = totalHotspotX;
				else
					throw new IOException(
						"Hotspot X not defined for frame " + frameIndex
					);
				
				int hotspotY;
				
				if (hotspotYString != null)
					hotspotY = Integer.parseInt(hotspotYString);
				else if (totalHotspotY >= 0)
					hotspotY = totalHotspotY;
				else
					throw new IOException(
						"Hotspot Y not defined for frame " + frameIndex
					);
				
				BufferedImage image = (BufferedImage) ImageIO.read(
					new File(parent, path)
				);
				
				image = Utils.getAlphaImage(image);
				
				image = Utils.replaceColors(
					image,
					Collections.singleton(bgColor),
					Utils.CLEAR
				);
				
				aCur.cursors[frameIndex] =
					Toolkit.getDefaultToolkit().createCustomCursor(
						image,
						new Point(hotspotX, hotspotY),
						name + frameIndex
					);
				
				// Convert from jiffies (1/60 sec) to milliseconds
				aCur.delays[frameIndex] = (int)((duration / 60.0) * 1000.0);
			}
			catch (NumberFormatException nfExc)
			{
				throw new IOException(
					"Misformatted attribute value in <Frame> " + frameIndex
				);
			}
			
			frameIndex++;
		}
		
		return aCur;
	}
	
	private Cursor[] cursors;
	private int[] delays;
	private String name;
	
	protected AnimatedCursor(String name, int frameCount)
	{
		cursors = new Cursor[frameCount];
		delays = new int[frameCount];
	}
	
	public String getName()
	{
		return name;
	}
	
	public Cursor getCursor(int index)
	{
		return cursors[index];
	}
	
	/**
	 * Get delay for nth frame, measured in milliseconds.
	 */
	public int getDelay(int index)
	{
		return delays[index];
	}
	
	public int getFrameCount()
	{
		return cursors.length;
	}
}
