package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.XNode;
import com.robbix.mp5.basics.AutoArrayList;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.FileFormatException;
import com.robbix.mp5.basics.Offset;
import com.robbix.mp5.unit.Activity;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.HealthBracket;

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
	private Map<String, Sprite> sprites;
	private Map<String, Integer> metadata;
	
	private Map<String, SpriteGroup> groups;
	private boolean eagerGroups = true;
	
	private List<SpriteSet> unitSets;
	private Map<String, SpriteSet> ambientSets;
	
	private Map<String, List<XNode>> offsetFrameMap;
	private File xmlFile;
	
	private List<Sprite> tempList = new AutoArrayList<Sprite>();
	
	public SpriteSetXMLLoader(
		Map<String, Sprite> sprites,
		Map<String, Integer> metadata,
		Map<String, SpriteGroup> groups,
		List<SpriteSet> unitSets,
		Map<String, SpriteSet> ambientSets)
	{
		this.sprites = sprites;
		this.metadata = metadata;
		this.groups = groups;
		this.unitSets = unitSets;
		this.ambientSets = ambientSets;
	}
	
	public void load(File xmlFile) throws IOException
	{
		if (xmlFile.isDirectory())
			xmlFile = new File(xmlFile, "info.xml");
		
		this.xmlFile = xmlFile;
		
		XNode rootNode = new XNode(xmlFile, false).getNode("SpriteSet");
		
		offsetFrameMap = getOffsetFrameMap(rootNode);
		String type = rootNode.getAttribute("type");
		File rootDir = xmlFile.getParentFile();
		
		if      (type.equals("vehicle"))   loadVehicle  (rootDir, rootNode);
		else if (type.equals("turret"))    loadTurret   (rootDir, rootNode);
		else if (type.equals("guardPost")) loadGuardPost(rootDir, rootNode);
		else if (type.equals("structure")) loadStructure(rootDir, rootNode);
		else if (type.equals("ambient"))   loadAmbient  (rootDir, rootNode);
		else
		{
			throw new FileFormatException(xmlFile, "Not a valid SpriteSet type");
		}
	}
	
	/**
	 * Loads vehicle activities/frames.
	 */
	public void loadVehicle(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		SpriteSet spriteSet = unitType.contains("Truck")
			? SpriteSet.forTrucks(unitType)
			: SpriteSet.forVehicles(unitType);
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			int fileNumber = activityNode.getIntAttribute("fileNumber");
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			if (activity == MOVE)
			{
				int majorTurnFrameCount = activityNode.getIntAttribute("majorTurnFrameCount");
				int minorTurnFrameCount = activityNode.getIntAttribute("minorTurnFrameCount");
				
				Cargo.Type cargo = getCargoType(activityNode.getAttribute("cargo", null));
				
				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile, "Cargo type not marked for Truck");
								
				for (XNode directionNode : getOffsetNodes(activityNode))
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					int frameCount = direction.isThirdOrder()
						? minorTurnFrameCount
						: majorTurnFrameCount;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activity,
						cargo,
						direction
					);
					
					metadata.put(parentSpritePath, frameCount);
					tempList.clear();
					
					for (int i = 0; i < frameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							sprite
						);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, true, delay);
					
					if (unitType.contains("Truck"))
						spriteSet.set(group, activity, direction, cargo);
					else
						spriteSet.set(group, activity, direction);
					
					if (eagerGroups) groups.put(parentSpritePath, group);
				}
			}
			else if (activity == DUMP)
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				Cargo.Type cargo = getCargoType(activityNode.getAttribute("cargo"));

				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);
				
				for (XNode directionNode : getOffsetNodes(activityNode))
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activity,
						cargo,
						direction
					);
					
					metadata.put(parentSpritePath, perTurnFrameCount);
					tempList.clear();
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							sprite
						);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, false, delay);
					spriteSet.set(group, activity, direction, cargo);
					if (eagerGroups) groups.put(parentSpritePath, group);
				}
			}
			else if (activity == DOCKUP || activity == DOCKDOWN || activity == MINELOAD)
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				Cargo.Type cargo;
				
				try
				{
					cargo = getCargoType(activityNode.getAttribute("cargo"));
				}
				catch (FileFormatException iae)
				{
					cargo = null;
				}
				
				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile, "Cargo type not marked for Truck");
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activity,
					cargo
				);
				
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				metadata.put(parentSpritePath, frameCount);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						sprite
					);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				if (unitType.contains("Truck"))
					spriteSet.set(group, activity, Direction.W, cargo);
				else
					spriteSet.set(group, activity, Direction.W);
				if (eagerGroups) groups.put(parentSpritePath, group);
			}
			else if (activity == SURVEY)
			{
			}
			else if (activity == BULLDOZE) // also earthworker construct
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				List<XNode> directionNodes = getOffsetNodes(activityNode);
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activity,
						direction
					);
					
					metadata.put(parentSpritePath, perTurnFrameCount);
					List<XNode> offsetNodes = directionNode.getNodes("OffsetFrame");
					tempList.clear();
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						Offset frameOffset = dirOffset;
						
						if (!offsetNodes.isEmpty())
						{
							frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
						}
						
						Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							sprite
						);
						tempList.add(sprite);
					}
					
					SpriteGroup group = new SpriteGroup(tempList, true, delay);
					spriteSet.set(group, activity, direction);
					if (eagerGroups) groups.put(parentSpritePath, group);
				}
			}
			else if (activity == CONSTRUCT) // convec construct
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activity
				);
				
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				metadata.put(parentSpritePath, frameCount);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						sprite
					);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, true, delay);
				spriteSet.set(group, activity, Direction.SW);
				if (eagerGroups) groups.put(parentSpritePath, group);
			}
		}
		
		unitSets.set(Mediator.factory.getType(unitType).getSerial(), spriteSet);
	}
	
	/**
	 * Loads structure frames.
	 */
	public void loadStructure(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forStructures(unitType);
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			if (activity == STILL)
			{
				setListSize(tempList, HealthBracket.values().length);
				
				for (XNode healthNode : activityNode.getNodes("HealthState"))
				{
					String health = healthNode.getAttribute("health");
					int fileNumber = healthNode.getIntAttribute("fileNumber");
					Offset healthOffset = healthNode.getOffsetAttributes();
					healthOffset = healthOffset.add(activityOffset);
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activity,
						health
					);
					
					Image img = loadFrame(activityDir, fileNumber);
					metadata.put(parentSpritePath, 1);
					Sprite sprite = new Sprite(img, playerColorHue, healthOffset);
					sprites.put(
						parentSpritePath,
						sprite
					);
					tempList.set(getHealth(health).ordinal(), sprite);
				}
				
				SpriteGroup group = new EnumSpriteGroup<HealthBracket>(HealthBracket.class, tempList);
				spriteSet.set(group, activity);
			}
			else if (activity == BUILD)
			{
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(unitType, activity);
				metadata.put(parentSpritePath, frameCount);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						sprite
					);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
				if (eagerGroups) groups.put(parentSpritePath, group);
			}
			else if (activity == COLLAPSE)
			{
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
	
				String parentSpritePath = Utils.getPath(
					unitType,
					activity
				);
				
				metadata.put(parentSpritePath, frameCount);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						sprite
					);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
				if (eagerGroups) groups.put(parentSpritePath, group);
			}
		}
		
		unitSets.set(Mediator.factory.getType(unitType).getSerial(), spriteSet);
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public void loadTurret(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forTurrets(unitType);
		
		XNode activityNode = rootNode.getNode("Activity");
		
		if (activityNode == null)
			throw new FileFormatException(xmlFile, "No activity Node");
		
		String activityName = activityNode.getAttribute("name");
		Activity activity = getActivity(activityName);
		
		if (activity != TURRET)
			throw new IOException("Only \"turret\" activity valid for turrets");
		
		String path = activityNode.getAttribute("path", ".");

		File activityDir = new File(dir, path);
		
		Offset activityOffset = activityNode.getOffsetAttributes();
		int fileNumber = activityNode.getIntAttribute("fileNumber");
		setListSize(tempList, 16);
		
		for (XNode directionNode : activityNode.getNodes("Direction"))
		{
			Direction direction = directionNode.getDirectionAttribute("name");
			Offset dirOffset = directionNode.getOffsetAttributes();
			dirOffset = dirOffset.add(activityOffset);
			
			String parentSpritePath = Utils.getPath(
				unitType,
				activity,
				direction
			);
			
			metadata.put(parentSpritePath, 1);
			
			int h = 0;
			
			for (XNode hotspotNode : directionNode.getNodes("Hotspot"))
			{
				Offset hotspotOffset = hotspotNode.getOffsetAttributes();
				
				metadata.put(
					Utils.getPath(parentSpritePath, h, "x"),
					hotspotOffset.dx);
				metadata.put(
					Utils.getPath(parentSpritePath, h, "y"),
					hotspotOffset.dy);
				
				h++;
			}
			
			Image img = loadFrame(activityDir, fileNumber++);
			Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
			sprites.put(
				Utils.getPath(parentSpritePath, 0),
				sprite
			);
			tempList.set(direction.ordinal(), sprite);
		}
		
		SpriteGroup group = new EnumSpriteGroup<Direction>(Direction.class, tempList);
		spriteSet.set(group, activity);
		unitSets.set(Mediator.factory.getType(unitType).getSerial(), spriteSet);
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public void loadGuardPost(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		SpriteSet spriteSet = SpriteSet.forGuardPosts(unitType);
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			File activityDir = new File(dir, path);
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			int delay = activityNode.getIntAttribute("delay", 1);
			
			if (activity == TURRET)
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				List<XNode> directionNodes = activityNode.getNodes("Direction");
				
				setListSize(tempList, 16);
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					Offset dirOffset = directionNode.getOffsetAttributes();
					dirOffset = dirOffset.add(activityOffset);
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activity,
						direction
					);
					
					metadata.put(parentSpritePath, 1);
					
					int h = 0;
					
					for (XNode hotspotNode : directionNode.getNodes("Hotspot"))
					{
						Offset hotspotOffset = hotspotNode.getOffsetAttributes();
						
						metadata.put(
							Utils.getPath(parentSpritePath, h, "x"),
							hotspotOffset.dx);
						metadata.put(
							Utils.getPath(parentSpritePath, h, "y"),
							hotspotOffset.dy);
						
						h++;
					}
					
					Image img = loadFrame(activityDir, fileNumber++);
					Sprite sprite = new Sprite(img, playerColorHue, dirOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, 0),
						sprite
					);
					tempList.set(direction.ordinal(), sprite);
				}
				
				SpriteGroup group = new EnumSpriteGroup<Direction>(Direction.class, tempList);
				spriteSet.set(group, activity);
			}
			else if (activity == BUILD)
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");

				String parentSpritePath = Utils.getPath(
					unitType,
					activity
				);
				
				metadata.put(parentSpritePath, frameCount);
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				tempList.clear();
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					Sprite sprite = new Sprite(img, playerColorHue, frameOffset);
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						sprite
					);
					tempList.add(sprite);
				}
				
				SpriteGroup group = new SpriteGroup(tempList, false, delay);
				spriteSet.set(group, activity);
				if (eagerGroups) groups.put(parentSpritePath, group);
			}
		}
		
		unitSets.set(Mediator.factory.getType(unitType).getSerial(), spriteSet);
	}
	
	/**
	 * Loads an Ambient sequence.
	 */
	public void loadAmbient(File dir, XNode rootNode) throws IOException
	{
		String eventType = rootNode.getAttribute("eventType");
		double trans = rootNode.getFloatAttribute("translucency", 1.0);
		
		SpriteSet spriteSet = SpriteSet.forAmbient(eventType);
		
		for (XNode eventNode : rootNode.getNodes("Event"))
		{
			String eventName = eventNode.getAttribute("name");
			String path = eventNode.getAttribute("path");
			
			int fileNumber = eventNode.getIntAttribute("fileNumber");
			int frameCount = eventNode.getIntAttribute("frameCount");
			Offset eventOffset = eventNode.getOffsetAttributes();
			
			int delay = eventNode.getIntAttribute("delay", 1);
			
			File eventDir = new File(dir, path);
			String parentSpritePath = Utils.getPath(eventType, eventName);
			metadata.put(parentSpritePath, frameCount);
			tempList.clear();
			List<XNode> offsetNodes = eventNode.getNodes("OffsetFrame");
			
			for (int i = 0; i < frameCount; ++i)
			{
				Image img = loadFrame(eventDir, fileNumber++, trans);
				Offset frameOffset = eventOffset;
				
				if (!offsetNodes.isEmpty())
				{
					frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
				}
				
				Sprite sprite = new Sprite(img, -1, frameOffset);
				sprites.put(
					Utils.getPath(parentSpritePath, i),
					sprite
				);
				tempList.add(sprite);
			}
			
			SpriteGroup group = new SpriteGroup(tempList, false, delay);
			spriteSet.set(group, eventName);
			if (eagerGroups) groups.put(parentSpritePath, group);
		}
		
		ambientSets.put(eventType, spriteSet);
	}
	
	public static Image loadFrame(File dir, int fileNumber) throws IOException
	{
		Image img = ImageIO.read(new File(dir,
			"frm-" + (fileNumber++) + ".bmp"
		));
		
		img = Utils.replaceColors(
			Utils.getAlphaImage((BufferedImage) img),
			Utils.ORANGE_AND_DARK_ORANGE,
			Utils.CLEAR
		);
		
		return img;
	}
	
	public static Image loadFrame(File dir, int fileNumber, double trans) throws IOException
	{
		BufferedImage img = (BufferedImage)loadFrame(dir, fileNumber);
		
		img = Utils.getTranslucency(img, (float)trans);
		
		return img;
	}
	
	public List<XNode> getOffsetNodes(XNode activityNode) throws FileFormatException
	{
		String offsetGroupName = activityNode.getAttribute("useOffsets", null);
		
		if (offsetGroupName == null)
		{
			List<XNode> offsetFrames;
			
			offsetFrames = activityNode.getNodes("OffsetFrame");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			offsetFrames = activityNode.getNodes("Direction");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			return new ArrayList<XNode>(0);
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
	
	private static Map<String, List<XNode>> getOffsetFrameMap(XNode rootNode) throws FileFormatException
	{
		Map<String, List<XNode>> offsetFrameMap =
			new HashMap<String, List<XNode>>();
		
		for (XNode offsetFrameGroup : rootNode.getNodes("OffsetFrames"))
		{
			offsetFrameMap.put(
				offsetFrameGroup.getAttribute("name"),
				offsetFrameGroup.getNodes("OffsetFrame")
			);
		}

		for (XNode offsetFrameGroup : rootNode.getNodes("DirectionOffsetFrames"))
		{
			offsetFrameMap.put(
				offsetFrameGroup.getAttribute("name"),
				offsetFrameGroup.getNodes("Direction")
			);
		}
		
		return offsetFrameMap;
	}
	
	private static Activity getActivity(String name)
	{
		if (name == null) return null;
		name = name.replace(' ', '_');
		name = name.toUpperCase();
		return Activity.valueOf(name);
	}
	
	private static HealthBracket getHealth(String name)
	{
		if (name == null) return null;
		name = name.replace(' ', '_');
		name = name.toUpperCase();
		return HealthBracket.valueOf(name);
	}
	
	private static Cargo.Type getCargoType(String name)
	{
		if (name == null) return null;
		name = name.replace(' ', '_');
		name = name.toUpperCase();
		return Cargo.Type.valueOf(name);
	}
	
	private static void setListSize(List<?> list, int size)
	{
		list.clear();
		for (int i = 0; i < size; ++i)
			list.add(null);
	}
}
