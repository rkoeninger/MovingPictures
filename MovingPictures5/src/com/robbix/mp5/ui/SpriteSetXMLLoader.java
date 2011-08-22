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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.FileFormatException;

/**
 * WARNING! NOT THREAD SAFE!
 */
class SpriteSetXMLLoader
{
	private Map<String, Sprite> sprites;
	private Map<String, Integer> metadata;
	
	private Map<String, List<Node>> offsetFrameMap;
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
		
		Document doc = Utils.loadXML(xmlFile, false);
		Node rootNode = Utils.getNode(doc, "SpriteSet");
		
		offsetFrameMap = getOffsetFrameMap(rootNode);
		
		String type = Utils.getAttribute(rootNode, "type");
		
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
	public void loadVehicle(File dir, Node rootNode) throws IOException
	{
		String unitType = Utils.getAttribute(rootNode, "unitType");
		
		Color color = Color.decode(Utils.getAttribute(rootNode, "color"));
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		int playerColorHue = (int) (hsb[0] * 360);
		
		boolean truck = unitType.contains("Truck");
		
		List<Node> activityNodes = Utils.getNodes(rootNode, "Activity");
		
		for (Node activityNode : activityNodes)
		{
			String activityName = Utils.getAttribute(activityNode, "name");
			String path = Utils.getAttribute(activityNode, "path", ".");

			File activityDir = new File(dir, path);
			
			int activityOffsetX = Utils.getIntAttribute(activityNode, "offsetX", 0);
			int activityOffsetY = Utils.getIntAttribute(activityNode, "offsetY", 0);
			int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
			
			if (activityName.equals("move"))
			{
				int majorTurnFrameCount = Utils.getIntAttribute(activityNode, "majorTurnFrameCount");
				int minorTurnFrameCount = Utils.getIntAttribute(activityNode, "minorTurnFrameCount");
				
				String cargo = Utils.getAttribute(activityNode, "cargo", null);
				
				if (truck && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);
				
				List<Node> directionNodes = getOffsetFrames(activityNode);
				
				for (Node directionNode : directionNodes)
				{
					Direction direction = Direction.getDirection(
						Utils.getAttribute(directionNode, "name"));
					
					int dirOffsetX = Utils.getIntAttribute(directionNode, "offsetX", 0);
					int dirOffsetY = Utils.getIntAttribute(directionNode, "offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					int frameCount = direction.isThirdOrder()
						? minorTurnFrameCount
						: majorTurnFrameCount;
					
					String parentSpritePath =
						Utils.getPath(
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
				int perTurnFrameCount =
					Utils.getIntAttribute(activityNode, "perTurnFrameCount");
				
				String cargo = Utils.getAttribute(activityNode, "cargo");

				if (truck && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);

				List<Node> directionNodes = getOffsetFrames(activityNode);
				
				for (Node directionNode : directionNodes)
				{
					Direction direction = Direction.getDirection(
						Utils.getAttribute(directionNode, "name"));

					int dirOffsetX = Utils.getIntAttribute(directionNode, "offsetX", 0);
					int dirOffsetY = Utils.getIntAttribute(directionNode, "offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					String parentSpritePath =
						Utils.getPath(
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
				int frameCount = Utils.getIntAttribute(activityNode, "frameCount");
				
				String cargo;
				
				try
				{
					cargo = Utils.getAttribute(
						activityNode,
						"cargo"
					);
				}
				catch (IllegalArgumentException iae)
				{
					cargo = null;
				}

				if (truck && cargo == null)
					throw new FileFormatException(xmlFile,
						"Cargo type not marked for Truck"
					);
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activityName,
					cargo
				);
				
				List<Node> offsetFrames = getOffsetFrames(activityNode);
				
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetX", 0);
						offsetY += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetY", 0);
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
				int perTurnFrameCount =
					Utils.getIntAttribute(activityNode, "perTurnFrameCount");
				
				List<Node> directionNodes = getOffsetFrames(activityNode);
				
				for (Node directionNode : directionNodes)
				{
					Direction direction = Direction.getDirection(
						Utils.getAttribute(directionNode, "name"));
					
					int dirOffsetX = Utils.getIntAttribute(directionNode, "offsetX", 0);
					int dirOffsetY = Utils.getIntAttribute(directionNode, "offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					String parentSpritePath =
						Utils.getPath(
							unitType,
							activityName,
							direction.getShortName()
						);
					
					metadata.put(parentSpritePath, perTurnFrameCount);
					
					List<Node> offsetFrames = Utils.getNodes(directionNode, "OffsetFrame");
					
					for (int i = 0; i < perTurnFrameCount; ++i)
					{
						Image img = loadFrame(activityDir, fileNumber++);
						
						int frameOffsetX = dirOffsetX;
						int frameOffsetY = dirOffsetY;
						
						if (offsetFrames.size() > 0)
						{
							frameOffsetX += Utils.getIntAttribute(offsetFrames.get(i), "offsetX", 0);
							frameOffsetY += Utils.getIntAttribute(offsetFrames.get(i), "offsetY", 0);
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
				int frameCount = Utils.getIntAttribute(activityNode, "frameCount");
				
				String parentSpritePath = Utils.getPath(
					unitType,
					activityName
				);
				
				List<Node> offsetFrames = getOffsetFrames(activityNode);
				
				metadata.put(parentSpritePath, frameCount);
				
				for (int i = 0; i < frameCount; ++i)
				{
					Image img = loadFrame(activityDir, fileNumber++);
					
					int offsetX = 0;
					int offsetY = 0;
					
					if (offsetFrames.size() > 0)
					{
						offsetX += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetX", 0);
						offsetY += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetY", 0);
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
	public void loadStructure(File dir, Node rootNode) throws IOException
	{
		String unitType = Utils.getAttribute(rootNode, "unitType");
		
		Color color = Color.decode(Utils.getAttribute(rootNode, "color"));
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		int playerColorHue = (int) (hsb[0] * 360);
		
		List<Node> activityNodes = Utils.getNodes(rootNode, "Activity");
		
		for (Node activityNode : activityNodes)
		{
			String activityName = Utils.getAttribute(activityNode, "name");
			String path = Utils.getAttribute(activityNode, "path", ".");
			
			File activityDir = new File(dir, path);

			int activityOffsetX = Utils.getIntAttribute(activityNode, "offsetX", 0);
			int activityOffsetY = Utils.getIntAttribute(activityNode, "offsetY", 0);
			
			if (activityName.equals("still"))
			{
				List<Node> healthNodes = Utils.getNodes(
					activityNode,
					"HealthState"
				);
				
				for (Node healthNode : healthNodes)
				{
					String health = Utils.getAttribute(healthNode, "health");
					int fileNumber = Utils.getIntAttribute(healthNode, "fileNumber");
					int healthOffsetX = Utils.getIntAttribute(healthNode, "offsetX", 0);
					int healthOffsetY = Utils.getIntAttribute(healthNode, "offsetY", 0);
					
					healthOffsetX += activityOffsetX;
					healthOffsetY += activityOffsetY;
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						health
					);
					
					Image img = loadFrame(activityDir, fileNumber);
					
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
				List<Node> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
				int frameCount = Utils.getIntAttribute(activityNode, "frameCount");

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
						offsetX += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetX", 0);
						offsetY += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetY", 0);
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
				List<Node> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
				int frameCount = Utils.getIntAttribute(activityNode, "frameCount");
	
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
						offsetX += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetX", 0);
						offsetY += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetY", 0);
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
	public void loadTurret(File dir, Node rootNode) throws IOException
	{
		String unitType = Utils.getAttribute(rootNode, "unitType");

		Color color = Color.decode(Utils.getAttribute(rootNode, "color"));
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		int playerColorHue = (int) (hsb[0] * 360);
		
		Node activityNode = Utils.getNode(rootNode, "Activity");
		
		if (activityNode == null)
			throw new FileFormatException(xmlFile, "No activity Node");
		
		String activityName = Utils.getAttribute(activityNode, "name");
		
		if (! activityName.equals("turret"))
			throw new IOException(
				"Only \"turret\" activity valid for turrets"
			);
		
		String path = Utils.getAttribute(activityNode, "path", ".");

		File activityDir = new File(dir, path);

		int activityOffsetX = Utils.getIntAttribute(activityNode, "offsetX", 0);
		int activityOffsetY = Utils.getIntAttribute(activityNode, "offsetY", 0);
		int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
		
		List<Node> directionNodes = Utils.getNodes(
			activityNode,
			"Direction"
		);
		
		for (Node directionNode : directionNodes)
		{
			Direction direction = Direction.getDirection(
				Utils.getAttribute(directionNode, "name")
			);
			
			int dirOffsetX = Utils.getIntAttribute(directionNode, "offsetX", 0);
			int dirOffsetY = Utils.getIntAttribute(directionNode, "offsetY", 0);
			
			dirOffsetX += activityOffsetX;
			dirOffsetY += activityOffsetY;
			
			List<Node> hotspotNodes = Utils.getNodes(
				directionNode,
				"Hotspot"
			);
			
			String parentSpritePath = Utils.getPath(
				unitType,
				activityName,
				direction.getShortName()
			);
			
			int h = 0;
			
			for (Node hotspotNode : hotspotNodes)
			{
				int hotspotX = Utils.getIntAttribute(hotspotNode, "x");
				int hotspotY = Utils.getIntAttribute(hotspotNode, "y");
				
				metadata.put(
					Utils.getPath(parentSpritePath, h, "x"),
					hotspotX);
				metadata.put(
					Utils.getPath(parentSpritePath, h, "y"),
					hotspotY);
				
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
	public void loadGuardPost(File dir, Node rootNode) throws IOException
	{
		String unitType = Utils.getAttribute(rootNode, "unitType");

		Color color = Color.decode(Utils.getAttribute(rootNode, "color"));
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		int playerColorHue = (int) (hsb[0] * 360);
		
		List<Node> activityNodes = Utils.getNodes(rootNode, "Activity");
		
		for (Node activityNode : activityNodes)
		{
			String activityName = Utils.getAttribute(activityNode, "name");
			String path = Utils.getAttribute(activityNode, "path", ".");
			File activityDir = new File(dir, path);
			int activityOffsetX = Utils.getIntAttribute(activityNode, "offsetX", 0);
			int activityOffsetY = Utils.getIntAttribute(activityNode, "offsetY", 0);
			
			if (activityName.equals("turret"))
			{
				int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
				
				List<Node> directionNodes = Utils.getNodes(
					activityNode,
					"Direction"
				);
				
				for (Node directionNode : directionNodes)
				{
					Direction direction = Direction.getDirection(
						Utils.getAttribute(directionNode, "name")
					);
					
					int dirOffsetX = Utils.getIntAttribute(directionNode, "offsetX", 0);
					int dirOffsetY = Utils.getIntAttribute(directionNode, "offsetY", 0);
					
					dirOffsetX += activityOffsetX;
					dirOffsetY += activityOffsetY;
					
					List<Node> hotspotNodes = Utils.getNodes(
						directionNode,
						"Hotspot"
					);
					
					String parentSpritePath = Utils.getPath(
						unitType,
						activityName,
						direction.getShortName()
					);
					
					int h = 0;
					
					for (Node hotspotNode : hotspotNodes)
					{
						int hotspotX = Utils.getIntAttribute(hotspotNode, "x");
						int hotspotY = Utils.getIntAttribute(hotspotNode, "y");
						
						metadata.put(
							Utils.getPath(parentSpritePath, h, "x"),
							hotspotX);
						metadata.put(
							Utils.getPath(parentSpritePath, h, "y"),
							hotspotY);
						
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
				List<Node> offsetFrames = getOffsetFrames(activityNode);
				
				int fileNumber = Utils.getIntAttribute(activityNode, "fileNumber");
				int frameCount = Utils.getIntAttribute(activityNode, "frameCount");

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
						offsetX += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetX", 0);
						offsetY += Utils.getIntAttribute(
							offsetFrames.get(i), "offsetY", 0);
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
	public void loadAmbient(File dir, Node rootNode) throws IOException
	{
		String eventType = Utils.getAttribute(rootNode, "eventType");
		List<Node> eventNodes = Utils.getNodes(rootNode, "Event");
		double trans = Utils.getFloatAttribute(rootNode, "translucency", 1.0);
		
		for (Node eventNode : eventNodes)
		{
			String eventName = Utils.getAttribute(eventNode, "name");
			String path = Utils.getAttribute(eventNode, "path");
			
			int fileNumber = Utils.getIntAttribute(eventNode, "fileNumber");
			int frameCount = Utils.getIntAttribute(eventNode, "frameCount");
			int eventOffsetX = Utils.getIntAttribute(eventNode, "x", 0);
			int eventOffsetY = Utils.getIntAttribute(eventNode, "y", 0);
			
			File eventDir = new File(dir, path);
	
			String parentSpritePath = Utils.getPath(eventType, eventName);
			
			metadata.put(parentSpritePath, frameCount);
			
			List<Node> offsetFrames = Utils.getNodes(
				eventNode, "OffsetFrame");
			
			for (int i = 0; i < frameCount; ++i)
			{
				Image img = loadFrame(eventDir, fileNumber++, trans);
				
				int offsetX = eventOffsetX;
				int offsetY = eventOffsetY;
				
				if (offsetFrames.size() > 0)
				{
					offsetX += Utils.getIntAttribute(
						offsetFrames.get(i), "x", 0);
					offsetY += Utils.getIntAttribute(
						offsetFrames.get(i), "y", 0);
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
	
	public List<Node> getOffsetFrames(Node activityNode)
	throws FileFormatException
	{
		String offsetGroupName =
			Utils.getAttribute(activityNode, "useOffsets", null);
		
		if (offsetGroupName == null)
		{
			List<Node> offsetFrames;
			
			offsetFrames = Utils.getNodes(activityNode, "OffsetFrame");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			offsetFrames = Utils.getNodes(activityNode, "Direction");
			
			if (!offsetFrames.isEmpty())
				return offsetFrames;
			
			return new ArrayList<Node>(0);
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
	
	private static Map<String, List<Node>> getOffsetFrameMap(Node rootNode)
	throws FileFormatException
	{
		Map<String, List<Node>> offsetFrameMap =
			new HashMap<String, List<Node>>();
		
		for (Node offsetFrameGroup : Utils.getNodes(rootNode, "OffsetFrames"))
		{
			offsetFrameMap.put(
				Utils.getAttribute(offsetFrameGroup, "name"),
				Utils.getNodes(offsetFrameGroup, "OffsetFrame")
			);
		}

		for (Node offsetFrameGroup : Utils.getNodes(rootNode, "DirectionOffsetFrames"))
		{
			offsetFrameMap.put(
				Utils.getAttribute(offsetFrameGroup, "name"),
				Utils.getNodes(offsetFrameGroup, "Direction")
			);
		}
		
		return offsetFrameMap;
	}
}
