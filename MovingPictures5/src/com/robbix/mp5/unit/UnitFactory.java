package com.robbix.mp5.unit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.robbix.mp5.ResourceType;
import com.robbix.mp5.TestMP5;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.SelfDestructAttackTask;
import com.robbix.mp5.ai.task.Task;
import com.robbix.mp5.ai.task.TurretTask;
import com.robbix.mp5.basics.FileFormatException;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.player.Player;

public class UnitFactory
{
	public static UnitFactory load(File rootDir) throws IOException
	{
		UnitFactory factory = new UnitFactory();
		factory.rootDir = rootDir;
		
		File[] files = rootDir.listFiles();
		
		factory.types = new HashMap<String, UnitType>(files.length);
		factory.typeList = new ArrayList<String>(files.length);
		factory.names = new ArrayList<String>(files.length);
		
		for (File file : files)
			if (file.getName().endsWith(".xml"))
				factory.loadXml(file);
		
		return factory;
	}
	
	private Map<String, UnitType> types;
	private List<String> typeList;
	private List<String> names;
	
	private Player defaultOwner;
	
	private File rootDir;
	
	private UnitFactory()
	{
	}
	
	public List<UnitType> getVehicleTypes()
	{
		List<UnitType> vehicleTypes = new ArrayList<UnitType>();
		
		for (UnitType type : types.values())
		{
			if ((type.isVehicleType() || type.isTankType()) && !type.isChassisType())
			{
				vehicleTypes.add(type);
			}
		}
		
		return vehicleTypes;
	}
	
	public List<UnitType> getStructureTypes()
	{
		List<UnitType> structTypes = new ArrayList<UnitType>();
		
		for (UnitType type : types.values())
		{
			if (type.isStructureType() || type.isGuardPostType())
			{
				structTypes.add(type);
			}
		}
		
		return structTypes;
	}
	
	public UnitType getType(String name)
	{
		return types.get(name);
	}
	
	public List<String> getUnitTypes()
	{
		return typeList;
	}
	
	public List<String> getUnitNames()
	{
		return names;
	}
	
	public void setDefaultOwner(Player owner)
	{
		this.defaultOwner = owner;
	}
	
	public Unit newUnit(UnitType uType, Player owner)
	{
		Unit unit = null;
		
		if (uType.isTankType())
		{
			String chassisTypeName = uType.getChassisTypeName();
			String turretTypeName  = uType.getTurretTypeName();
			
			UnitType chassisType = types.get(chassisTypeName);
			UnitType turretType  = types.get(turretTypeName);
			
			if (chassisType == null)
				throw new IllegalArgumentException(chassisTypeName + " does not exist");
			
			if (turretType == null)
				throw new IllegalArgumentException(turretTypeName + " does not exist");
			
			unit = Unit.newTank(chassisType, turretType);
			Filter<Unit> filter = new TestMP5.NotMyTeamFilter(owner);
			Task turretTask = null;
			
			turretTask =
				unit.isStarflare() || unit.isSupernova()
				? new SelfDestructAttackTask(filter)
				: new TurretTask(filter);
			
			unit.getTurret().setDefaultTask(turretTask);
		}
		else if (uType.isStructureType())
		{
			unit = Unit.newStructure(uType);
		}
		else if (uType.isGuardPostType())
		{
			unit = Unit.newGuardPost(uType);
			unit.setDefaultTask(
				new TurretTask(new TestMP5.NotMyTeamFilter(owner)));
		}
		else
		{
			unit = new Unit(uType);
		}
		
		unit.setOwner(owner);
		
		return unit;
	}
	
	public Unit newUnit(String type, Player owner)
	{
		UnitType uType = types.get(type);
		
		if (uType == null)
			throw new IllegalArgumentException(type + "ty does not exist");
		
		return newUnit(uType, owner);
	}
	
	public Unit newUnit(String type)
	{
		return newUnit(type, defaultOwner);
	}
	
	public void loadXml(String filename) throws IOException
	{
		load(new File(rootDir, filename + ".xml"));
	}
	
	public void loadXml(File xmlFile) throws IOException
	{
		Document doc = Utils.loadXML(xmlFile, false);
		Node rootNode = Utils.getNode(doc, "UnitType");
		String displayName = Utils.getValue(rootNode, "DisplayName");
		String civ = Utils.getAttribute(rootNode, "civ");
		String unitType = Utils.getAttribute(rootNode, "unitType");
		String type = Utils.getAttribute(rootNode, "type");
		
		String ack = null;
		
		try
		{
			ack = Utils.getValue(rootNode, "Acknowledgement");
		}
		catch (Exception e)
		{
			// Let ack stay null
		}
		
		if (type.equals("tank"))
		{
			String chassisTypeName = Utils.getValue(rootNode, "Chassis");
			String turretTypeName = Utils.getValue(rootNode, "Turret");
			Cost cost = getCost(Utils.getNode(rootNode, "Cost"), xmlFile);
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newTankType(
				unitType,
				displayName,
				civ,
				chassisTypeName,
				turretTypeName,
				cost
			));
		}
		else if (type.equals("turret"))
		{
			Node statsNode = Utils.getNode(rootNode, "Stats");
			double damage = Utils.getFloatAttribute(statsNode, "damage");
			int reloadDelay = Utils.getIntAttribute(statsNode, "reloadDelay");
			double attackRange = Utils.getFloatAttribute(statsNode, "attackRange");
			
			types.put(unitType, UnitType.newTurretType(
				unitType,
				displayName,
				civ,
				damage,
				attackRange,
				reloadDelay
			));
		}
		else if (type.equals("chassis"))
		{
			Node statsNode = Utils.getNode(rootNode, "Stats");
			int maxHP = Utils.getIntAttribute(statsNode, "hp");
			double speed = Utils.getFloatAttribute(statsNode, "speed");
			double sightRange = Utils.getFloatAttribute(statsNode, "sightRange");
			int rotationSpeed = Utils.getIntAttribute(statsNode, "rotationSpeed");
			int rotationDegree = Utils.getIntAttribute(statsNode, "rotationDegree");
			String armorString = Utils.getAttribute(statsNode, "armor");
			Armor armor = getArmor(armorString, xmlFile);
			
			types.put(unitType, UnitType.newChassisType(
				unitType,
				displayName,
				ack,
				civ,
				maxHP,
				armor,
				sightRange,
				speed,
				rotationSpeed,
				rotationDegree
			));
		}
		else if (type.equals("vehicle"))
		{
			Node statsNode = Utils.getNode(rootNode, "Stats");
			int maxHP = Utils.getIntAttribute(statsNode, "hp");
			double speed = Utils.getFloatAttribute(statsNode, "speed");
			double sightRange = Utils.getFloatAttribute(statsNode, "sightRange");
			int rotationSpeed = Utils.getIntAttribute(statsNode, "rotationSpeed");
			int rotationDegree = Utils.getIntAttribute(statsNode, "rotationDegree");
			String armorString = Utils.getAttribute(statsNode, "armor");
			Armor armor = getArmor(armorString, xmlFile);
			Cost cost = getCost(Utils.getNode(rootNode, "Cost"), xmlFile);
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newVehicleType(
				unitType,
				displayName,
				ack,
				civ,
				cost,
				maxHP,
				armor,
				sightRange,
				speed,
				rotationSpeed,
				rotationDegree
			));
		}
		else if (type.equals("structure"))
		{
			Node statsNode = Utils.getNode(rootNode, "Stats");
			int maxHP = Utils.getIntAttribute(statsNode, "hp");
			double sightRange = Utils.getFloatAttribute(statsNode, "sightRange");
			String armorString = Utils.getAttribute(statsNode, "armor");
			Armor armor = getArmor(armorString, xmlFile);
			String footprintString = Utils.getValue(rootNode, "Footprint");
			Footprint footprint = getFootprint(footprintString, xmlFile);
			Cost cost = getCost(Utils.getNode(rootNode, "Cost"), xmlFile);
			boolean source = Utils.getBooleanAttribute(statsNode, "connectionSource", false);
			boolean needsConnection = Utils.getBooleanAttribute(statsNode, "needsConnection", false);
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newStructureType(
				unitType,
				displayName,
				ack,
				civ,
				cost,
				maxHP,
				armor,
				sightRange,
				footprint,
				source,
				needsConnection
			));
		}
		else if (type.equals("guardPost"))
		{
			Node statsNode = Utils.getNode(rootNode, "Stats");
			int maxHP = Utils.getIntAttribute(statsNode, "hp");
			double sightRange = Utils.getFloatAttribute(statsNode, "sightRange");
			String armorString = Utils.getAttribute(statsNode, "armor");
			Armor armor = getArmor(armorString, xmlFile);
			double damage = Utils.getFloatAttribute(statsNode, "damage");
			int reloadDelay = Utils.getIntAttribute(statsNode, "reloadDelay");
			Cost cost = getCost(Utils.getNode(rootNode, "Cost"), xmlFile);
			double attackRange = Utils.getFloatAttribute(statsNode, "attackRange");
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newGuardPostType(
				unitType,
				displayName,
				ack,
				civ,
				cost,
				maxHP,
				armor,
				sightRange,
				damage,
				attackRange,
				reloadDelay
			));
		}
	}
	
	private static Cost getCost(Node costNode, File xmlFile)
	throws FileFormatException
	{
		List<Node> costResources = Utils.getNodes(costNode, "Resource");
		
		Map<ResourceType, Integer> resourceMap =
			new HashMap<ResourceType, Integer>();
		
		for (int n = 0; n < costResources.size(); ++n)
		{
			Node resNode = costResources.get(n);
			resourceMap.put(
				getResourceType(Utils.getAttribute(resNode, "type"), xmlFile),
				Utils.getIntAttribute(resNode, "amount"));
		}
		
		return new Cost(resourceMap);
	}
	
	private static Armor getArmor(String name, File xmlFile)
	throws FileFormatException
	{
		try
		{
			name = name.replace(' ', '_');
			name = name.toUpperCase();
			Armor armor = Enum.valueOf(Armor.class, name);
			
			if (armor == null)
				throw new IllegalArgumentException(name + " in " + xmlFile);
			
			return armor;
		}
		catch (IllegalArgumentException iae)
		{
			throw new FileFormatException(xmlFile, "Invalid armor");
		}
	}

	private static ResourceType getResourceType(String name, File xmlFile)
	throws FileFormatException
	{
		try
		{
			name = name.replace(' ', '_');
			name = name.toUpperCase();
			ResourceType type = Enum.valueOf(ResourceType.class, name);
			
			if (type == null)
				throw new IllegalArgumentException(name + " in " + xmlFile);
			
			return type;
		}
		catch (IllegalArgumentException iae)
		{
			throw new FileFormatException(xmlFile, "Invalid resource type");
		}
	}
	
	private static Footprint getFootprint(String name, File xmlFile)
	throws FileFormatException
	{
		try
		{
			name = name.replace(' ', '_');
			name = name.toUpperCase();
			Field fpField = Footprint.class.getDeclaredField(name);
			return (Footprint) fpField.get(null);
		}
		catch (NoSuchFieldException nsfe)
		{
			throw new FileFormatException(xmlFile, "Invalid footprint");
		}
		catch (IllegalAccessException e)
		{
			throw new Error("Field should be accessible: " + name);
		}
	}
}
