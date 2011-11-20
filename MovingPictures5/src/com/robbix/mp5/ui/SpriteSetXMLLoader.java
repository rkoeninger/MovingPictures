package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.HealthBracket;
import com.robbix.mp5.utils.AutoArrayList;
import com.robbix.mp5.utils.Direction;
import com.robbix.mp5.utils.FileFormatException;
import com.robbix.mp5.utils.Offset;
import com.robbix.mp5.utils.RImage;
import com.robbix.mp5.utils.RNode;

import static com.robbix.mp5.unit.Activity.*;

/**
 * WARNING! NOT THREAD SAFE!
 * 
 * OR REUSABLE! DO NOT REUSE!
 * 
 * One instance per info.xml file/sprite set!
 */
class SpriteSetXMLLoader
{
	private Map<String, List<RNode>> offsetFrameMap;
	private File xmlFile;
	
	private List<Sprite> tempList = new AutoArrayList<Sprite>();
	
	public SpriteSetXMLLoader(File xmlFile)
	{
		if (xmlFile.isDirectory())
			xmlFile = new File(xmlFile, "info.xml");
		
		if (!xmlFile.exists() || !xmlFile.isFile() || !xmlFile.getName().endsWith(".xml"))
			throw new IllegalArgumentException(xmlFile + " not valid");
		
		this.xmlFile = xmlFile;
	}
	
	public SpriteSet load() throws IOException
	{
		RNode rootNode = RNode.load(xmlFile);
		
		offsetFrameMap = getOffsetFrameMap(rootNode);
		String type = rootNode.getAttribute("type");
		File rootDir = xmlFile.getParentFile();
		
		if      (type.equals("vehicle"))   return loadVehicle  (rootDir, rootNode);
		else if (type.equals("turret"))    return loadTurret   (rootDir, rootNode);
		else if (type.equals("guardPost")) return loadGuardPost(rootDir, rootNode);
		else if (type.equals("structure")) return loadStructure(rootDir, rootNode);
		else if (type.equals("ambient"))   return loadAmbient  (rootDir, rootNode);
		else
		{
			throw new FileFormatException(xmlFile, "Not a valid SpriteSet type");
		}
	}
	
	/**
	 * Loads vehicle activities/frames.
	 */
	public SpriteSet loadVehicle(File dir, RNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = getHueInt(color);
		
		SpriteSet spriteSet = unitType.contains("Truck")
			? SpriteSet.forTrucks(unitType)
			: SpriteSet.forVehicles(unitType);
		
		for (RNode activityNode : rootNode.getNodes("Activity"))
		{
			Activity activity = activityNode.getEnumAttribute(Activity.class, "name");
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			int fileNumber = activityNode.getIntAttribute("fileNumber");
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			if (activity == MOVE)
			{
				int majorTurnFrameCount = activityNode.getIntAttribute("majorTurnFrameCount");
				int minorTurnFrameCount = activityNode.getIntAttribute("minorTurnFrameCount");
				
				Cargo.Type cargo = activityNode.getEnumAttribute(Cargo.Type.class, "cargo", null);
				
				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile, "Cargo type not marked for Truck");
								
				for (RNode directionNode : getOffsetNodes(activityNode))
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					int frameCount = direction.isThirdOrder()
						? minorTurnFrameCount
						: majorTurnFrameCount;
					
					tempList.clear();
					
					for (int i = 0; i < frameCount; ++i)
					{
						RImage img = loadFrame(activityDir, fileNumber++);
						Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, true, delay);
					
					if (unitType.contains("Truck"))
						spriteSet.set(group, cargo, activity, direction);
					else
						spriteSet.set(group, activity, direction);
				}
			}
			else if (activity == DUMP)
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				Cargo.Type cargo = activityNode.getEnumAttribute(Cargo.Type.class, "cargo");

				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);
				
				for (RNode directionNode : getOffsetNodes(activityNode))
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					tempList.clear();
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						RImage img = loadFrame(activityDir, fileNumber++);
						Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, false, delay);
					spriteSet.set(group, cargo, activity, direction);
				}
			}
			else if (activity == DOCKUP || activity == DOCKDOWN || activity == MINELOAD)
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				Cargo.Type cargo;
				
				try
				{
					cargo = activityNode.getEnumAttribute(Cargo.Type.class, "cargo");
				}
				catch (FileFormatException iae)
				{
					cargo = null;
				}
				
				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile, "Cargo type not marked for Truck");
				
				List<RNode> offsetNodes = getOffsetNodes(activityNode);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					RImage img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				if (unitType.contains("Truck"))
					spriteSet.set(group, cargo, activity, Direction.W);
				else
					spriteSet.set(group, activity, Direction.W);
			}
			else if (activity == SURVEY)
			{
			}
			else if (activity == BULLDOZE) // also earthworker construct
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				List<RNode> directionNodes = getOffsetNodes(activityNode);
				
				for (RNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					List<RNode> offsetNodes = directionNode.getNodes("OffsetFrame");
					tempList.clear();
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						RImage img = loadFrame(activityDir, fileNumber++);
						Offset frameOffset = dirOffset;
						
						if (!offsetNodes.isEmpty())
						{
							frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
						}
						
						Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, true, delay);
					spriteSet.set(group, activity, direction);
				}
			}
			else if (activity == CONSTRUCT) // convec construct
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				List<RNode> offsetNodes = getOffsetNodes(activityNode);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					RImage img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, true, delay);
				spriteSet.set(group, activity, Direction.SW);
			}
		}
		
		return spriteSet;
	}
	
	/**
	 * Loads structure frames.
	 */
	public SpriteSet loadStructure(File dir, RNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forStructures(unitType);
		
		for (RNode activityNode : rootNode.getNodes("Activity"))
		{
			Activity activity = activityNode.getEnumAttribute(Activity.class, "name");
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			if (activity == STILL)
			{
				setListSize(tempList, HealthBracket.values().length);
				
				for (RNode healthNode : activityNode.getNodes("HealthState"))
				{
					HealthBracket hb = healthNode.getEnumAttribute(HealthBracket.class, "health");
					int fileNumber = healthNode.getIntAttribute("fileNumber");
					Offset healthOffset = healthNode.getOffsetAttributes();
					healthOffset = healthOffset.add(activityOffset);
					RImage img = loadFrame(activityDir, fileNumber);
					Sprite sprite = new Sprite(img, playerColorHue, healthOffset);
					tempList.set(hb.ordinal(), sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, HealthBracket.class);
				spriteSet.set(group, activity);
			}
			else if (activity == BUILD)
			{
				List<RNode> offsetNodes = getOffsetNodes(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					RImage img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
			}
			else if (activity == COLLAPSE)
			{
				List<RNode> offsetNodes = getOffsetNodes(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					RImage img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
			}
		}
		
		return spriteSet;
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public SpriteSet loadTurret(File dir, RNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forTurrets(unitType);
		
		RNode activityNode = rootNode.getNode("Activity");
		
		if (activityNode == null)
			throw new FileFormatException(xmlFile, "No activity Node");

		Activity activity = activityNode.getEnumAttribute(Activity.class, "name");
		
		if (activity != TURRET)
			throw new IOException("Only \"turret\" activity valid for turrets");
		
		String path = activityNode.getAttribute("path", ".");

		File activityDir = new File(dir, path);
		
		Offset activityOffset = activityNode.getOffsetAttributes();
		int fileNumber = activityNode.getIntAttribute("fileNumber");
		setListSize(tempList, 16);
		
		for (RNode directionNode : activityNode.getNodes("Direction"))
		{
			Direction direction = directionNode.getDirectionAttribute("name");
			Offset dirOffset = directionNode.getOffsetAttributes();
			dirOffset = dirOffset.add(activityOffset);
			List<RNode> hotspotNodes = directionNode.getNodes("Hotspot");
			Point2D hotspot = new Point2D.Double();
			if (!hotspotNodes.isEmpty())
			{
				RNode hotspotNode = hotspotNodes.get(0);
				Offset hotspotOffset = hotspotNode.getOffsetAttributes();
				hotspot = new Point2D.Double(
					hotspotOffset.dx / 32.0,
					hotspotOffset.dy / 32.0
				);
			}
			RImage img = loadFrame(activityDir, fileNumber++);
			Sprite sprite = new TurretSprite(img, playerColorHue, dirOffset, hotspot);
			tempList.set(direction.ordinal(), sprite);
		}
		
		SpriteGroup group = new SpriteGroup(tempList, Direction.class);
		spriteSet.set(group, activity);
		return spriteSet;
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public SpriteSet loadGuardPost(File dir, RNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forGuardPosts(unitType);
		
		for (RNode activityNode : rootNode.getNodes("Activity"))
		{
			Activity activity = activityNode.getEnumAttribute(Activity.class, "name");
			String path = activityNode.getAttribute("path", ".");
			File activityDir = new File(dir, path);
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			if (activity == TURRET)
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				List<RNode> directionNodes = activityNode.getNodes("Direction");
				
				setListSize(tempList, 16);
				
				for (RNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					List<RNode> hotspotNodes = directionNode.getNodes("Hotspot");
					Point2D hotspot = new Point2D.Double();
					if (!hotspotNodes.isEmpty())
					{
						RNode hotspotNode = hotspotNodes.get(0);
						Offset hotspotOffset = hotspotNode.getOffsetAttributes();
						hotspot = new Point2D.Double(
							hotspotOffset.dx / 32.0,
							hotspotOffset.dy / 32.0
						);
					}
					RImage img = loadFrame(activityDir, fileNumber++);
					Sprite sprite = new TurretSprite(img, playerColorHue, dirOffset, hotspot);
					tempList.set(direction.ordinal(), sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, Direction.class);
				spriteSet.set(group, activity);
			}
			else if (activity == BUILD)
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				List<RNode> offsetNodes = getOffsetNodes(activityNode);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					RImage img = loadFrame(activityDir, fileNumber++);
					
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
			}
		}
		
		return spriteSet;
	}
	
	/**
	 * Loads an Ambient sequence.
	 */
	public SpriteSet loadAmbient(File dir, RNode rootNode) throws IOException
	{
		String eventType = rootNode.getAttribute("eventType");
		double trans = rootNode.getFloatAttribute("translucency", 1.0);
		
		SpriteSet spriteSet = SpriteSet.forAmbient(eventType);
		
		for (RNode eventNode : rootNode.getNodes("Event"))
		{
			String eventName = eventNode.getAttribute("name");
			String path = eventNode.getAttribute("path");
			
			int fileNumber = eventNode.getIntAttribute("fileNumber");
			int frameCount = eventNode.getIntAttribute("frameCount");
			Offset eventOffset = eventNode.getOffsetAttributes();
			
			int delay = eventNode.getIntAttribute("delay", 1);
			
			File eventDir = new File(dir, path);
			tempList.clear();
			List<RNode> offsetNodes = eventNode.getNodes("OffsetFrame");
			
			for (int i = 0; i < frameCount; ++i)
			{
				RImage img = loadFrame(eventDir, fileNumber++, trans);
				Offset frameOffset = eventOffset;
				
				if (!offsetNodes.isEmpty())
				{
					frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
				}
				
				Sprite sprite = new Sprite(img, -1, frameOffset);
				tempList.add(sprite);
			}
			
			SpriteGroup group = new SpriteGroup(tempList, false, delay);
			spriteSet.set(group, eventName);
		}
		
		return spriteSet;
	}
	
	private static final Color[] bgColors = {
		new Color(255, 127, 0),
		new Color(127, 63, 0)
	};
	
	public static RImage loadFrame(File dir, int fileNumber) throws IOException
	{
		RImage img = RImage.readEnsureAlpha(new File(dir, "frm-" + (fileNumber++) + ".bmp"));
		img.extract(bgColors);
		return img;
	}
	
	public static RImage loadFrame(File dir, int fileNumber, double trans) throws IOException
	{
		RImage img = loadFrame(dir, fileNumber);
		img.fade(trans);
		return img;
	}
	
	public List<RNode> getOffsetNodes(RNode activityNode) throws FileFormatException
	{
		String offsetGroupName = activityNode.getAttribute("useOffsets", null);
		
		if (offsetGroupName == null)
		{
			List<RNode> offsetFrames;
			
			offsetFrames = activityNode.getNodes("OffsetFrame");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			offsetFrames = activityNode.getNodes("Direction");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			return new ArrayList<RNode>(0);
		}
		else
		{
			if (! offsetFrameMap.containsKey(offsetGroupName))
			{
				throw new FileFormatException(xmlFile,
					"OffsetFrame group "
					+ offsetGroupName
					+ " does not exist"
				);
			}
			
			return offsetFrameMap.get(offsetGroupName);
		}
		
	}
	
	private static Map<String, List<RNode>> getOffsetFrameMap(RNode rootNode)
	throws FileFormatException
	{
		Map<String, List<RNode>> offsetFrameMap = new HashMap<String, List<RNode>>();
		
		for (RNode offsetFrameGroup : rootNode.getNodes("OffsetFrames"))
		{
			offsetFrameMap.put(
				offsetFrameGroup.getAttribute("name"),
				offsetFrameGroup.getNodes("OffsetFrame")
			);
		}

		for (RNode offsetFrameGroup : rootNode.getNodes("DirectionOffsetFrames"))
		{
			offsetFrameMap.put(
				offsetFrameGroup.getAttribute("name"),
				offsetFrameGroup.getNodes("Direction")
			);
		}
		
		return offsetFrameMap;
	}
	
	private static void setListSize(List<?> list, int size)
	{
		list.clear();
		
		for (int i = 0; i < size; ++i)
			list.add(null);
	}
	
	private static int getHueInt(Color color)
	{
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		return (int) (hsb[0] * 360);
	}
}
