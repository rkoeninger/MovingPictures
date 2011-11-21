package com.robbix.mp5.unit;

import static com.robbix.mp5.unit.Command.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.robbix.utils.Direction;

public class UnitType
{
	public static UnitType newVehicleType(
		String name,
		String displayName,
		String ack,
		String civ,
		Cost cost,
		int maxHP,
		Armor armor,
		double sightRange,
		double speed,
		int rotationSpeed,
		int rotationDegree,
		Set<Command> commands)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.ack = ack;
		type.civ = civ;
		type.cost = cost;
		type.maxHP = maxHP;
		type.armor = armor;
		type.sightRange = sightRange;
		type.speed = speed;
		type.rotationSpeed = rotationSpeed;
		type.rotationDegree = rotationDegree;
		type.fp = Footprint.VEHICLE;
		type.commands = EnumSet.copyOf(VEHICLE_COMMANDS);
		type.commands.addAll(commands);
		type.initSpriteArgs();
		
		return type;
	}
	
	public static UnitType newStructureType(
		String name,
		String displayName,
		String ack,
		String civ,
		Cost cost,
		int maxHP,
		int buildTime,
		Armor armor,
		double sightRange,
		Footprint fp,
		boolean connectionSource,
		boolean needsConnection,
		Set<Command> commands)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.ack = ack;
		type.civ = civ;
		type.cost = cost;
		type.maxHP = maxHP;
		type.armor = armor;
		type.sightRange = sightRange;
		type.fp = fp;
		type.buildTime = 150;
		type.connectionSource = connectionSource;
		type.needsConnection = needsConnection;
		type.commands = EnumSet.copyOf(STRUCT_COMMANDS);
		type.commands.addAll(commands);
		type.initSpriteArgs();
		
		return type;
	}
	
	public static UnitType newGuardPostType(
		String name,
		String displayName,
		String ack,
		String civ,
		Cost cost,
		int maxHP,
		int buildTime,
		Armor armor,
		double sightRange,
		double damage,
		double attackRange,
		int weaponChargeCost)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.ack = ack;
		type.civ = civ;
		type.cost = cost;
		type.maxHP = maxHP;
		type.armor = armor;
		type.sightRange = sightRange;
		type.damage = damage;
		type.attackRange = attackRange;
		type.weaponChargeCost = weaponChargeCost;
		type.fp = Footprint.STRUCT_1_BY_1;
		type.buildTime = 50;
		type.commands = EnumSet.copyOf(GUARD_POST_COMMANDS);
		type.initSpriteArgs();
		
		return type;
	}
	
	public static UnitType newTankType(
		String name,
		String displayName,
		String civ,
		String chassisTypeName,
		String turretTypeName,
		Cost cost)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.civ = civ;
		type.chassisTypeName = chassisTypeName;
		type.turretTypeName = turretTypeName;
		type.cost = cost;
		
		return type;
	}
	
	public static UnitType newChassisType(
		String name,
		String displayName,
		String ack,
		String civ,
		int maxHP,
		Armor armor,
		double sightRange,
		double speed,
		int rotationSpeed,
		int rotationDegree)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.ack = ack;
		type.civ = civ;
		type.maxHP = maxHP;
		type.armor = armor;
		type.sightRange = sightRange;
		type.speed = speed;
		type.rotationSpeed = rotationSpeed;
		type.rotationDegree = rotationDegree;
		type.fp = Footprint.VEHICLE;
		type.commands = EnumSet.copyOf(CHASSIS_COMMANDS);
		type.initSpriteArgs();
		
		return type;
	}
	
	public static UnitType newTurretType(
		String name,
		String displayName,
		String civ,
		double damage,
		double attackRange,
		int weaponChargeCost)
	{
		UnitType type = new UnitType();
		
		type.name = name;
		type.displayName = displayName;
		type.civ = civ;
		type.damage = damage;
		type.attackRange = attackRange;
		type.weaponChargeCost = weaponChargeCost;
		type.initSpriteArgs();
		
		return type;
	}
	
	private static Set<Command> VEHICLE_COMMANDS = EnumSet.of(
		MOVE,
		SELF_DESTRUCT,
		SCATTER,
		KILL,
		STAND_GROUND,
		TRANSFER,
		KILL
	);
	
	private static Set<Command> STRUCT_COMMANDS = EnumSet.of(
		IDLE,
		KILL
	);
	
	private static Set<Command> GUARD_POST_COMMANDS = EnumSet.of(
		ATTACK,
		KILL,
		IDLE
	);
	
	private static Set<Command> CHASSIS_COMMANDS = EnumSet.of(
		MOVE,
		KILL,
		SELF_DESTRUCT,
		GUARD,
		STAND_GROUND
	);
	
	private static AtomicInteger nextSerial = new AtomicInteger();
	
	private int serial;
	
	private String name;
	private String displayName;
	private String civ;
	private Cost cost;
	private Armor armor;
	private int buildTime;
	private int maxHP;
	private double sightRange;
	private double speed;
	private int rotationSpeed;
	private int rotationDegree;
	private Footprint fp;
	private double damage;
	private double attackRange;
	private int weaponChargeCost;
	private String chassisTypeName;
	private String turretTypeName;
	private String ack;
	private boolean connectionSource;
	private boolean needsConnection;
	private Object[] spriteArgs;
	private Set<Command> commands;
	
	private UnitType()
	{
		serial = nextSerial.getAndIncrement();
	}
	
	private void initSpriteArgs()
	{
		if (name.contains("Truck"))
		{
			spriteArgs = new Object[3];
		}
		else if (fp == Footprint.VEHICLE)
		{
			spriteArgs = new Object[2];
		}
		else
		{
			spriteArgs = new Object[1];
		}
		
		if (name.contains("Truck"))
		{
			spriteArgs[0] = Cargo.Type.EMPTY;
			spriteArgs[1] = Activity.MOVE;
			spriteArgs[2] = Direction.E;
		}
		else if (fp == Footprint.VEHICLE)
		{
			spriteArgs[0] = Activity.MOVE;
			spriteArgs[1] = Direction.E;
		}
		else if (isGuardPostType() || isTurretType())
		{
			spriteArgs[0] = Activity.TURRET;
		}
		else
		{
			spriteArgs[0] = Activity.STILL;
		}
	}
	
	public int getSerial()
	{
		return serial;
	}
	
	public Set<Command> getCommands()
	{
		return commands;
	}
	
	public String getAcknowledgement()
	{
		return ack;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public String getChassisTypeName()
	{
		return chassisTypeName;
	}

	public String getTurretTypeName()
	{
		return turretTypeName;
	}
	
	public Footprint getFootprint()
	{
		return fp;
	}
	
	public int getBuildTime()
	{
		return buildTime;
	}
	
	public String getCiv()
	{
		return civ;
	}
	
	public int getMaxHP()
	{
		return maxHP;
	}
	
	public int getWeaponChargeCost()
	{
		return weaponChargeCost;
	}
	
	public double getSightRange()
	{
		return sightRange;
	}
	
	public double getDamage()
	{
		return damage;
	}
	
	public double getAttackRange()
	{
		return attackRange;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	
	public int getRotationSpeed()
	{
		return rotationSpeed;
	}
	
	public boolean hasSixteenthTurn()
	{
		return rotationDegree == 16;
	}
	
	public Armor getArmor()
	{
		return armor;
	}
	
	public Cost getCost()
	{
		return cost;
	}
	
	public boolean isConnectionSource()
	{
		return connectionSource;
	}
	
	public boolean needsConnection()
	{
		return needsConnection;
	}
	
	public Set<Cargo.Type> getSupportedCargoTypes()
	{
		return Collections.emptySet();
	}
	
	public int getCargoCapacity(Cargo.Type type)
	{
		return 100;
	}
	
	public Set<String> getSupportedCommands()
	{
		return Collections.emptySet();
	}
	
	public String toString()
	{
		return civ + " " + displayName;
	}
	
	public boolean isTankType()
	{
		return turretTypeName != null;
	}
	
	public boolean isTurretType()
	{
		return name.contains("Turret");
	}
	
	public boolean isChassisType()
	{
		return name.contains("Chassis");
	}
	
	public boolean isVehicleType()
	{
		return fp == Footprint.VEHICLE;
	}
	
	public boolean isStructureType()
	{
		return fp != null && fp != Footprint.VEHICLE && !isGuardPostType();
	}
	
	public boolean isGuardPostType()
	{
		return name.contains("GuardPost");
	}
	
	public boolean is(String phrase)
	{
		return name.contains(phrase);
	}
	
	public Object[] getDefaultSpriteArgs()
	{
		return spriteArgs;
	}
}
