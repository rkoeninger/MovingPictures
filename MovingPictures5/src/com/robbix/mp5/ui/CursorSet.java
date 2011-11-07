package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import com.robbix.mp5.utils.AnimatedCursor;
import com.robbix.mp5.utils.Utils;
import com.robbix.mp5.utils.XNode;

public class CursorSet
{
	public static CursorSet load(File rootDir) throws IOException
	{
		CursorSet cursorSet = new CursorSet();
		
		for (File dir : rootDir.listFiles())
		{
			File xmlFile = new File(dir, dir.getName() + ".xml");
			
			if (xmlFile.exists())
				cursorSet.loadCursor(xmlFile);
		}
		
		return cursorSet;
	}
	
	public void loadCursor(File xmlFile) throws IOException
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
		
		XNode rootNode = XNode.load(xmlFile);
		
		String name = rootNode.getAttribute("name");
		Color bgColor = Color.decode(rootNode.getAttribute("bgcolor"));
		
		int totalDuration = rootNode.getIntAttribute("duration", -1);
		int totalHotspotX = rootNode.getIntAttribute("hotspotX", -1);
		int totalHotspotY = rootNode.getIntAttribute("hotspotY", -1);
		
		List<XNode> frameNodes = rootNode.getNodes("Frame");
		int frameCount = frameNodes.size();
		
		if (frameNodes.isEmpty())
			throw new IOException("Expected 1 or more <Frame> tags");
		
		int frameIndex = 0;
		int[] frameDelays = new int[frameCount];
		Cursor[] frames = new Cursor[frameCount];
		
		int averageDuration = totalDuration / frameNodes.size();
		
		for (XNode frameNode : frameNodes)
		{
			try
			{
				String path = frameNode.getAttribute("image");
				int duration = frameNode.getIntAttribute("duration", averageDuration);
				int hotspotX = frameNode.getIntAttribute("hotspotX", totalHotspotX);
				int hotspotY = frameNode.getIntAttribute("hotspotY", totalHotspotY);
				
				BufferedImage image = (BufferedImage) ImageIO.read(new File(parent, path));
				image = Utils.getAlphaImage(image);
				image = Utils.replaceColors(
					image,
					Collections.singleton(bgColor),
					Utils.CLEAR
				);
				
				frames[frameIndex] = Toolkit.getDefaultToolkit().createCustomCursor(
					image,
					new Point(hotspotX, hotspotY),
					name + frameIndex
				);
				
				// Convert from jiffies (1/60 sec) to milliseconds
				frameDelays[frameIndex] = (int)((duration / 60.0) * 1000.0);
			}
			catch (NumberFormatException nfExc)
			{
				throw new IOException("Misformatted attribute value in <Frame> " + frameIndex);
			}
			
			frameIndex++;
		}
		
		this.cursors.put(name, new AnimatedCursor(name, frames, frameDelays));
	}
	
	private HashMap<String, AnimatedCursor> cursors;
	
	private CursorSet()
	{
		cursors = new HashMap<String, AnimatedCursor>();
	}
	
	public Set<String> getCursorNames()
	{
		return cursors.keySet();
	}
	
	public AnimatedCursor getCursor(String name)
	{
		return cursors.get(name);
	}
}
