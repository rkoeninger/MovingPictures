package com.robbix.mp5.unit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.ai.task.SelfDestructAttackTask;
import com.robbix.mp5.ai.task.Task;
import com.robbix.mp5.ai.task.TurretTask;
import com.robbix.mp5.basics.FileFormatException;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.XNode;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.player.Player;

public class UnitFactory
{
	public static UnitFactory load(File rootDir) throws IOException
	{
		UnitFactory factory = new UnitFactory();
		factory.rootDir = rootDir;
		
		File[] files = rootDir.listFiles();
		
		if (files == null)
			throw new IOException("No directory listing");
		
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
	
	public UnitType getTurretType(UnitType tankType)
	{
		String turretTypeName = tankType.getTurretTypeName();
		
		if (turretTypeName == null)
			throw new IllegalArgumentException(tankType + " doesn't have turret");
		
		return getType(turretTypeName);
	}
	
	public UnitType getChassisType(UnitType tankType)
	{
		String chassisTypeName = tankType.getChassisTypeName();
		
		if (chassisTypeName == null)
			throw new IllegalArgumentException(tankType + " doesn't have chassis");
		
		return getType(chassisTypeName);
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
			Filter<Unit> filter = new UnitFactory.NotMyTeamFilter(owner);
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
				new TurretTask(new UnitFactory.NotMyTeamFilter(owner)));
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
	
	public Unit newUnit(String type, int playerID)
	{
		return newUnit(type, Mediator.game.getPlayer(playerID));
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
		XNode rootNode = new XNode(xmlFile, false).getNode("UnitType");
		String displayName = rootNode.getValue("DisplayName");
		String civ         = rootNode.getAttribute("civ");
		String unitType    = rootNode.getAttribute("unitType");
		String type        = rootNode.getAttribute("type");
		
		String ack = null;
		
		try
		{
			ack = rootNode.getValue("Acknowledgement");
		}
		catch (Exception e)
		{
			// Let ack stay null
		}
		
		if (type.equals("tank"))
		{
			String chassisTypeName = rootNode.getValue("Chassis");
			String turretTypeName = rootNode.getValue("Turret");
			Cost cost = getCost(rootNode.getNode("Cost"), xmlFile);
			
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
			XNode statsNode    = rootNode.getNode("Stats");
			double damage      = statsNode.getFloatAttribute("damage");
			int reloadDelay    = statsNode.getIntAttribute("reloadDelay");
			double attackRange = statsNode.getFloatAttribute("attackRange");
			
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
			XNode statsNode    = rootNode.getNode("Stats");
			int maxHP          = statsNode.getIntAttribute("hp");
			double speed       = statsNode.getFloatAttribute("speed");
			double sightRange  = statsNode.getFloatAttribute("sightRange");
			int rotationSpeed  = statsNode.getIntAttribute("rotationSpeed");
			int rotationDegree = statsNode.getIntAttribute("rotationDegree");
			String armorString = statsNode.getAttribute("armor");
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
			XNode statsNode    = rootNode.getNode("Stats");
			int maxHP          = statsNode.getIntAttribute("hp");
			double speed       = statsNode.getFloatAttribute("speed");
			double sightRange  = statsNode.getFloatAttribute("sightRange");
			int rotationSpeed  = statsNode.getIntAttribute("rotationSpeed");
			int rotationDegree = statsNode.getIntAttribute("rotationDegree");
			String armorString = statsNode.getAttribute("armor");
			Armor armor        = getArmor(armorString, xmlFile);
			Cost cost          = getCost(rootNode.getNode("Cost"), xmlFile);
			
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
			XNode statsNode         = rootNode.getNode("Stats");
			int maxHP               = statsNode.getIntAttribute("hp");
			int buildTime           = statsNode.getIntAttribute("buildTime");
			double sightRange       = statsNode.getFloatAttribute("sightRange");
			String armorString      = statsNode.getAttribute("armor");
			Armor armor             = getArmor(armorString, xmlFile);
			String footprintString  = rootNode.getValue("Footprint");
			Footprint footprint     = getFootprint(footprintString, xmlFile);
			Cost cost               = getCost(rootNode.getNode("Cost"), xmlFile);
			boolean source          = statsNode.getBooleanAttribute("connectionSource", false);
			boolean needsConnection = statsNode.getBooleanAttribute("needsConnection", false);
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newStructureType(
				unitType,
				displayName,
				ack,
				civ,
				cost,
				maxHP,
				buildTime,
				armor,
				sightRange,
				footprint,
				source,
				needsConnection
			));
		}
		else if (type.equals("guardPost"))
		{
			XNode statsNode    = rootNode.getNode("Stats");
			int maxHP          = statsNode.getIntAttribute("hp");
			int buildTime      = statsNode.getIntAttribute("buildTime");
			double sightRange  = statsNode.getFloatAttribute("sightRange");
			String armorString = statsNode.getAttribute("armor");
			Armor armor        = getArmor(armorString, xmlFile);
			double damage      = statsNode.getFloatAttribute("damage");
			int reloadDelay    = statsNode.getIntAttribute("reloadDelay");
			Cost cost          = getCost(rootNode.getNode("Cost"), xmlFile);
			double attackRange = statsNode.getFloatAttribute("attackRange");
			
			names.add(civ + " " + displayName);
			typeList.add(unitType);
			types.put(unitType, UnitType.newGuardPostType(
				unitType,
				displayName,
				ack,
				civ,
				cost,
				maxHP,
				buildTime,
				armor,
				sightRange,
				damage,
				attackRange,
				reloadDelay
			));
		}
	}
	
	private static Cost getCost(XNode costNode, File xmlFile)
	throws FileFormatException
	{
		List<XNode> costResources = costNode.getNodes("Resource");
		
		Map<ResourceType, Integer> resourceMap =
			new HashMap<ResourceType, Integer>();
		
		for (int n = 0; n < costResources.size(); ++n)
		{
			XNode resNode = costResources.get(n);
			resourceMap.put(
				getResourceType(resNode.getAttribute("type"), xmlFile),
				resNode.getIntAttribute("amount"));
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
	
	public static class NotMyTeamFilter extends Filter<Unit>
	{
		private Player myOwner;
		
		public NotMyTeamFilter(Player myOwner)
		{
			this.myOwner = myOwner;
		}
		
		public boolean accept(Unit unit)
		{
			if (unit == null)
				return false;
			
			return !myOwner.equals(unit.getOwner());
		}
	}
}
