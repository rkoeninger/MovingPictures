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
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);
			
			int activityOffsetX = activityNode.getIntAttribute("offsetX", 0);
			int activityOffsetY = activityNode.getIntAttribute("offsetY", 0);
			int fileNumber      = activityNode.getIntAttribute("fileNumber");
			
			if (activityName.equals("move"))
			{
				int majorTurnFrameCount = activityNode.getIntAttribute("majorTurnFrameCount");
				int minorTurnFrameCount = activityNode.getIntAttribute("minorTurnFrameCount");
				
				String cargo = activityNode.getAttribute("cargo", null);
				
				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile, "Cargo type not marked for Truck");
				
				List<XNode> directionNodes = getOffsetFrames(activityNode);
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					int dirOffsetX      = directionNode.getIntAttribute("offsetX", 0);
					int dirOffsetY      = directionNode.getIntAttribute("offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					int frameCount = direction.isThirdOrder()
						? minorTurnFrameCount
						: majorTurnFrameCount;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						cargo,
						direction.getShortName()
					);
					
					metadata.put(parentSpritePath, frameCount);
					
					for (int i = 0; i < frameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(
								img,
								playerColorHue,
								dirOffsetX,
								dirOffsetY
							)
						);
					}
				}
			}
			else if (activityName.equals("dump"))
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				String cargo = activityNode.getAttribute("cargo");

				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);

				List<XNode> directionNodes = getOffsetFrames(activityNode);
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					int dirOffsetX      = directionNode.getIntAttribute("offsetX", 0);
					int dirOffsetY      = directionNode.getIntAttribute("offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						cargo,
						direction.getShortName()
					);
					
					metadata.put(parentSpritePath, perTurnFrameCount);
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(
								img,
								playerColorHue,
								dirOffsetX,
								dirOffsetY
							)
						);
					}
				}
			}
			else if (activityName.equals("dockUp")
				  || activityName.equals("dockDown")
				  || activityName.equals("mineLoad"))
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				String cargo;
				
				try
				{
					cargo = activityNode.getAttribute("cargo");
				}
				catch (IllegalArgumentException iae)
				{
					cargo = null;
				}

				if (unitType.contains("Truck") && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activityName,
					cargo
				);
				
				List<XNode> offsetFrames = getOffsetFrames(activityNode);
				
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
						offsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(
							img,
							playerColorHue,
							activityOffsetX + offsetX,
							activityOffsetY + offsetY
						)
					);
				}
			}
			else if (activityName.equals("survey"))
			{
			}
			else if (activityName.equals("bulldoze"))
			{
				int perTurnFrameCount = activityNode.getIntAttribute("perTurnFrameCount");
				List<XNode> directionNodes = getOffsetFrames(activityNode);
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					int dirOffsetX      = directionNode.getIntAttribute("offsetX", 0);
					int dirOffsetY      = directionNode.getIntAttribute("offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						direction.getShortName()
					);
					
					metadata.put(parentSpritePath, perTurnFrameCount);
					
					List<XNode> offsetFrames = directionNode.getNodes("OffsetFrame");
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						int frameOffsetX = dirOffsetX;
						int frameOffsetY = dirOffsetY;
						
						if (offsetFrames.size() > 0)
						{
							frameOffsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
							frameOffsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
						}
						
						sprites.put(
							Utils.getPath(parentSpritePath, i),
							new Sprite(
								img,
								playerColorHue,
								frameOffsetX,
								frameOffsetY
							)
						);
					}
				}
			}
			else if (activityName.equals("construct"))
			{
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activityName
				);
				
				List<XNode> offsetFrames = getOffsetFrames(activityNode);
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
						offsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(
							img,
							playerColorHue,
							activityOffsetX + offsetX,
							activityOffsetY + offsetY
						)
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
			String path = activityNode.getAttribute("path", ".");
			
			File activityDir = new File(dir, path);

			int activityOffsetX = activityNode.getIntAttribute("offsetX", 0);
			int activityOffsetY = activityNode.getIntAttribute("offsetY", 0);
			
			if (activityName.equals("still"))
			{
				List<XNode> healthNodes = activityNode.getNodes("HealthState");
				
				for (XNode healthNode : healthNodes)
				{
					String health = healthNode.getAttribute("health");
					int fileNumber = healthNode.getIntAttribute("fileNumber");
					int healthOffsetX = healthNode.getIntAttribute("offsetX", 0);
					int healthOffsetY = healthNode.getIntAttribute("offsetY", 0);
					
					healthOffsetX += activityOffsetX;
					healthOffsetY += activityOffsetY;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						health
					);
					
					Image img = loadFrame(activityDir, fileNumber);
					
					metadata.put(parentSpritePath, 1);
					
					sprites.put(
						parentSpritePath,
						new Sprite(
							img,
							playerColorHue,
							healthOffsetX,
							healthOffsetY
						)
					);
				}
			}
			else if (activityName.equals("build"))
			{
				List<XNode> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
				
				String parentSpritePath = Utils.getPath(unitType, activityName);
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
						offsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(
							img,
							playerColorHue,
							activityOffsetX + offsetX,
							activityOffsetY + offsetY
						)
					);
				}
			}
			else if (activityName.equals("collapse"))
			{
				List<XNode> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");
	
				String parentSpritePath = Utils.getPath(
					unitType,
					activityName
				);
				
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
						offsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(
							img,
							playerColorHue,
							activityOffsetX + offsetX,
							activityOffsetY + offsetY
						)
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
		
		if (! activityName.equals("turret"))
			throw new IOException("Only \"turret\" activity valid for turrets");
		
		String path = activityNode.getAttribute("path", ".");

		File activityDir = new File(dir, path);

		int activityOffsetX = activityNode.getIntAttribute("offsetX", 0);
		int activityOffsetY = activityNode.getIntAttribute("offsetY", 0);
		int fileNumber      = activityNode.getIntAttribute("fileNumber");
		
		for (XNode directionNode : activityNode.getNodes("Direction"))
		{
			Direction direction = directionNode.getDirectionAttribute("name");
			int dirOffsetX      = directionNode.getIntAttribute("offsetX", 0);
			int dirOffsetY      = directionNode.getIntAttribute("offsetY", 0);
			
			dirOffsetX += activityOffsetX;
			dirOffsetY += activityOffsetY;
			
			String parentSpritePath = Utils.getPath(
				unitType,
				activityName,
				direction.getShortName()
			);
			
			metadata.put(parentSpritePath, 1);
			
			int h = 0;
			
			for (XNode hotspotNode : directionNode.getNodes("Hotspot"))
			{
				metadata.put(
					Utils.getPath(parentSpritePath, h, "x"),
					hotspotNode.getIntAttribute("x"));
				metadata.put(
					Utils.getPath(parentSpritePath, h, "y"),
					hotspotNode.getIntAttribute("y"));
				
				h++;
			}
			
			Image img = loadFrame(activityDir, fileNumber++);
			
			sprites.put(
				Utils.getPath(parentSpritePath, 0),
				new Sprite(
					img,
					playerColorHue,
					dirOffsetX,
					dirOffsetY
				)
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
			String path = activityNode.getAttribute("path", ".");
			File activityDir = new File(dir, path);
			int activityOffsetX = activityNode.getIntAttribute("offsetX", 0);
			int activityOffsetY = activityNode.getIntAttribute("offsetY", 0);
			
			if (activityName.equals("turret"))
			{
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				List<XNode> directionNodes = activityNode.getNodes("Direction");
				
				for (XNode directionNode : directionNodes)
				{
					Direction direction = directionNode.getDirectionAttribute("name");
					int dirOffsetX = directionNode.getIntAttribute("offsetX", 0);
					int dirOffsetY = directionNode.getIntAttribute("offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						direction.getShortName()
					);
					
					metadata.put(parentSpritePath, 1);
					
					int h = 0;
					
					for (XNode hotspotNode : directionNode.getNodes("Hotspot"))
					{
						metadata.put(
							Utils.getPath(parentSpritePath, h, "x"),
							hotspotNode.getIntAttribute("x"));
						metadata.put(
							Utils.getPath(parentSpritePath, h, "y"),
							hotspotNode.getIntAttribute("y"));
						
						h++;
					}
					
					Image img = loadFrame(activityDir, fileNumber++);
					
					sprites.put(
						Utils.getPath(parentSpritePath, 0),
						new Sprite(
							img,
							playerColorHue,
							dirOffsetX,
							dirOffsetY
						)
					);
				}
			}
			else if (activityName.equals("build"))
			{
				List<XNode> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = activityNode.getIntAttribute("fileNumber");
				int frameCount = activityNode.getIntAttribute("frameCount");

				String parentSpritePath = Utils.getPath(
					unitType,
					activityName
				);
				
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += offsetFrames.get(i).getIntAttribute("offsetX", 0);
						offsetY += offsetFrames.get(i).getIntAttribute("offsetY", 0);
					}
					
					sprites.put(
						Utils.getPath(parentSpritePath, i),
						new Sprite(
							img,
							playerColorHue,
							activityOffsetX + offsetX,
							activityOffsetY + offsetY
						)
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
			int eventOffsetX = eventNode.getIntAttribute("x", 0);
			int eventOffsetY = eventNode.getIntAttribute("y", 0);
			
			File eventDir = new File(dir, path);
	
			String parentSpritePath = Utils.getPath(eventType, eventName);
			
			metadata.put(parentSpritePath, frameCount);
			
			List<XNode> offsetFrames = eventNode.getNodes("OffsetFrame");
			
			for (int i = 0; i < frameCount; ++i)
			{
				Image img = loadFrame(eventDir, fileNumber++, trans);
				
				int offsetX = eventOffsetX;
				int offsetY = eventOffsetY;
				
				if (offsetFrames.size() > 0)
				{
					offsetX += offsetFrames.get(i).getIntAttribute("x", 0);
					offsetY += offsetFrames.get(i).getIntAttribute("y", 0);
				}
				
				sprites.put(
					Utils.getPath(parentSpritePath, i),
					new Sprite(img, -1, offsetX, offsetY)
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
	
	public List<XNode> getOffsetFrames(XNode activityNode)
	throws FileFormatException
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
	
	private static Map<String, List<XNode>> getOffsetFrameMap(XNode rootNode)
	throws FileFormatException
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
}
