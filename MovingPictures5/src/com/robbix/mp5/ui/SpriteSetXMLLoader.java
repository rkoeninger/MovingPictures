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

import com.robbix.mp5.Utils;
import com.robbix.mp5.XNode;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.FileFormatException;
import com.robbix.mp5.basics.Offset;
import com.robbix.mp5.unit.Activity;
import static com.robbix.mp5.unit.Activity.*;

/**
 * WARNING! NOT THREAD SAFE!
 */
class SpriteSetXMLLoader
{
	private Map<String, Sprite> sprites;
	private Map<String, Integer> metadata;
	
	private Map<String, List<XNode>> offsetFrameMap;
	private File xmlFile;
	
	public SpriteSetXMLLoader(
		Map<String, Sprite> sprites,
		Map<String, Integer> metadata)
	{
		this.sprites = sprites;
		this.metadata = metadata;
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
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			int fileNumber = activityNode.getIntAttribute("fileNumber");
			
			if (activity == MOVE)
			{
				int majorTurnFrameCount = activityNode.getIntAttribute("majorTurnFrameCount");
				int minorTurnFrameCount = activityNode.getIntAttribute("minorTurnFrameCount");
				
				String cargo = activityNode.getAttribute("cargo", null);
				
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
					
					for (int i = 0; i < frameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(img, playerColorHue, dirOffset)
						);
					}
				}
			}
			else if (activity == DUMP)
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				String cargo = activityNode.getAttribute("cargo");

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
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(img, playerColorHue, dirOffset)
						);
					}
				}
			}
			else if (activity == DOCKUP
				  || activity == DOCKDOWN
				  || activity == MINELOAD)
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				String cargo;
				
				try
				{
					cargo = activityNode.getAttribute("cargo");
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
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(img, playerColorHue, frameOffset)
					);
				}
			}
			else if (activity == SURVEY)
			{
			}
			else if (activity == BULLDOZE)
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
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						Offset frameOffset = dirOffset;
						
						if (!offsetNodes.isEmpty())
						{
							frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
						}
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(img, playerColorHue, frameOffset)
						);
					}
				}
			}
			else if (activity == CONSTRUCT)
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activity
				);
				
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(img, playerColorHue, frameOffset)
					);
				}
			}
		}
	}
	
	/**
	 * Loads structure frames.
	 */
	public void loadStructure(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			if (activity == STILL)
			{
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
					sprites.put(
						parentSpritePath,
						new Sprite(img, playerColorHue, healthOffset)
					);
				}
			}
			else if (activity == BUILD)
			{
				List<XNode> offsetNodes = getOffsetNodes(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(unitType, activity);
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(img, playerColorHue, frameOffset
						)
					);
				}
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
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(img, playerColorHue, frameOffset)
					);
				}
			}
		}
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public void loadTurret(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");

		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
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
			
			sprites.put(
				Utils.getPath(parentSpritePath, 0),
				new Sprite(img, playerColorHue, dirOffset)
			);
		}
	}
	
	/**
	 * Loads sprites for a turret and hotspot info.
	 */
	public void loadGuardPost(File dir, XNode rootNode) throws IOException
	{
		String unitType = rootNode.getAttribute("unitType");
		Color color = rootNode.getColorAttribute("color");
		int playerColorHue = Utils.getHueInt(color);
		
		for (XNode activityNode : rootNode.getNodes("Activity"))
		{
			String activityName = activityNode.getAttribute("name");
			Activity activity = getActivity(activityName);
			String path = activityNode.getAttribute("path", ".");
			File activityDir = new File(dir, path);
			Offset activityOffset = activityNode.getOffsetAttributes();
			
			if (activity == TURRET)
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				List<XNode> directionNodes = activityNode.getNodes("Direction");
				
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
					
					sprites.put(
						Utils.getPath(parentSpritePath, 0),
						new Sprite(img, playerColorHue, dirOffset)
					);
				}
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
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					Offset frameOffset = activityOffset;
					
					if (!offsetNodes.isEmpty())
					{
						frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(img, playerColorHue, frameOffset)
					);
				}
			}
		}
	}
	
	/**
	 * Loads an Ambient sequence.
	 */
	public void loadAmbient(File dir, XNode rootNode) throws IOException
	{
		String eventType = rootNode.getAttribute("eventType");
		double trans = rootNode.getFloatAttribute("translucency", 1.0);
		
		for (XNode eventNode : rootNode.getNodes("Event"))
		{
			String eventName = eventNode.getAttribute("name");
			String path = eventNode.getAttribute("path");
			
			int fileNumber = eventNode.getIntAttribute("fileNumber");
			int frameCount = eventNode.getIntAttribute("frameCount");
			Offset eventOffset = eventNode.getOffsetAttributes();
			
			File eventDir = new File(dir, path);
			String parentSpritePath = Utils.getPath(eventType, eventName);
			metadata.put(parentSpritePath, frameCount);
			
			List<XNode> offsetNodes = eventNode.getNodes("OffsetFrame");
			
			for (int i = 0; i < frameCount; ++i)
			{
				Image img = loadFrame(eventDir, fileNumber++, trans);
				Offset frameOffset = eventOffset;
				
				if (!offsetNodes.isEmpty())
				{
					frameOffset = frameOffset.add(offsetNodes.get(i).getOffsetAttributes());
				}
				
				sprites.put(
					Utils.getPath(parentSpritePath, i),
					new Sprite(img, -1, frameOffset)
				);
			}
		}
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
		name = name.replace(' ', '_');
		name = name.toUpperCase();
		return Enum.valueOf(Activity.class, name);
	}
}
