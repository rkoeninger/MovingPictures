package com.robbix.mp5.unit;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.robbix.mp5.ai.task.Task;
import com.robbix.mp5.basics.Direction;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.EnumSpriteGroup;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;

import static com.robbix.mp5.unit.Activity.*;

/**
 * Hashcode and equals methods remain defaults, since there should
 * not be two Unit objects describing the same unit.
 */
public class Unit
{
	private static AtomicInteger nextSerial = new AtomicInteger();
	
	public static Unit newStructure(UnitType type)
	{
		Unit struct = new Unit(type);
		struct.setActivity(STILL);
		return struct;
	}
	
	public static Unit newTank(UnitType chassisType, UnitType turretType)
	{
		Unit chassis = new Unit(chassisType);
		Unit turret = new Unit(turretType);
		chassis.turret = turret;
		turret.chassis = chassis;
		turret.setActivity(TURRET);
		return chassis;
	}
	
	public static Unit newGuardPost(UnitType type)
	{
		Unit guardPost = new Unit(type);
		guardPost.setActivity(TURRET);
		return guardPost;
	}
	
	private Position pos;
	private Position previous = null;
	private int offX, offY;
	private Direction dir;
	
	private Player owner;
	
	private LinkedList<Task> taskList;
	private Task defaultTask;
	private Activity activity;
	
	private UnitType type;
	
	private String kit;
	private Cargo cargo;
	private int hp;
	private int charge = 1024;
	
	private Unit turret;
	private Unit chassis;
	
	private int animationFrame = 0;
		
	private LayeredMap container;
	private Set<Position> reservations;
	private int priority;
	
	private int serial;
	
	private boolean idle = false;
	
	private Object[] spriteArgs;
	
	public Unit(UnitType type)
	{
		this.serial = nextSerial.getAndIncrement();
		
		this.type = type;
		this.activity = MOVE;
		this.hp = type.getMaxHP();
		taskList = new LinkedList<Task>();
		reservations = new HashSet<Position>();
		dir = Direction.E;
		cargo = Cargo.EMPTY;
		
		if (isTruck())
		{
			spriteArgs = new Object[3];
		}
		else if (type.getFootprint() == Footprint.VEHICLE)
		{
			spriteArgs = new Object[2];
		}
		else
		{
			spriteArgs = new Object[1];
		}
	}
	
	public int getSerial()
	{
		return serial;
	}
	
	public int hashCode()
	{
		return serial;
	}
	
	public Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Player owner)
	{
		this.owner = owner;
		
		if (chassis != null)
			chassis.owner = owner;
		
		if (turret != null)
			turret.owner = owner;
	}
	
	public void setHP(int hp)
	{
		if (hp < 0 || hp > type.getMaxHP())
			throw new IllegalArgumentException("HP out of range");
		
		this.hp = hp;
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public HealthBracket getHealthBracket()
	{
		float ratio = hp / (float) type.getMaxHP();
		
		if (ratio >= 0.5)
			return HealthBracket.GREEN;
		
		if (ratio >= 0.25)
			return HealthBracket.YELLOW;
		
		return HealthBracket.RED;
	}
	
	public boolean isDead()
	{
		return hp <= 0;
	}
	
	public boolean hasChassis()
	{
		return chassis != null;
	}
	
	public Unit getChassis()
	{
		return type.isGuardPostType() ? this : chassis;
	}
	
	public boolean hasTurret()
	{
		return turret != null;
	}
	
	public Unit getTurret()
	{
		return type.isGuardPostType() ? this : turret;
	}
	
	/**
	 * Gets x offset in terms of map.getSpotSize().
	 */
	public int getXOffset()
	{
		return chassis == null ? offX : chassis.offX;
	}
	
	/**
	 * Gets y offset in terms of map.getSpotSize().
	 */
	public int getYOffset()
	{
		return chassis == null ? offY : chassis.offY;
	}
	
	public void setXOffset(int offX)
	{
		this.offX = offX;
	}

	public void setYOffset(int offY)
	{
		this.offY = offY;
	}
	
	public void shiftXOffset(int dx)
	{
		if (chassis == null)
			offX += dx;
		else
			chassis.shiftXOffset(dx);
	}
	
	public void shiftYOffset(int dy)
	{
		if (chassis == null)
			offY += dy;
		else
			chassis.shiftYOffset(dy);
	}
	
	/**
	 * Called when weapon is fired.
	 */
	public void discharge()
	{
		charge = 0;
	}
	
	/**
	 * Recharges Unit's weapon energy by the given amount.
	 */
	public void recharge(int amount)
	{
		charge += amount;
	}
	
	/**
	 * Gets Unit's weapon energy level.
	 */
	public int getCharge()
	{
		return charge;
	}
	
	/**
	 * Gets the current speed of this Unit, considering terrain/damage.
	 */
	public double getSpeed()
	{
		if (chassis != null)
			return chassis.getSpeed();
		
		double cargoFactor = 1;
			
		if (isTruck() && (getCargo() != Cargo.EMPTY))
			cargoFactor = 0.5;
		
		double healthFactor = hp / (double) type.getMaxHP();
		healthFactor *= 0.75;
		healthFactor += 0.25;
		
		double terrainFactor = getMap()
							  .getTerrainCostMap()
				              .getScaleFactor(pos);
		
		return type.getSpeed() * cargoFactor * terrainFactor * healthFactor;
	}

	/**
	 * Gets the base speed of this Unit, on smooth terrain and full health.
	 */
	public double getBaseSpeed()
	{
		return type.getSpeed();
	}
	
	public int getPriority()
	{
		return priority;
	}
	
	public String getStructureKit()
	{
		return kit;
	}
	
	public void setStructureKit(String kit)
	{
		this.kit = kit;
	}
	
	public void setCargo(Cargo cargo)
	{
		if (!isTruck() && !type.getName().contains("ConVec"))
			throw new IllegalStateException("Not a truck");
		
		this.cargo = cargo == null ? Cargo.EMPTY : cargo;
	}
	
	public Cargo getCargo()
	{
		return cargo;
	}
	
	public boolean isCargoEmpty()
	{
		return cargo.getType() == Cargo.Type.EMPTY;
	}
	
	public void setActivity(Activity activity)
	{
		this.activity = activity;
	}
	
	public Activity getActivity()
	{
		if (activity != MOVE)
			previous = null;
		
		return activity;
	}
	
	public UnitType getType()
	{
		return type;
	}
	
	public boolean isConnected()
	{
		return pos == null ? false : !needsConnection() || container.isAlive(pos);
	}
	
	public boolean needsConnection()
	{
		return type.needsConnection();
	}
	
	public boolean isConnectionSource()
	{
		return type.isConnectionSource() && !isDisabled() && !isDead();
	}
	
	public String getStatusString()
	{
		return String.format("#%1$d %2$s%7$s%3$s %4$d/%5$d %6$s",
			serial,
			type.getName(),
			activity,
			hp,
			type.getMaxHP(),
			(cargo != null ? cargo.toString() : ""),
			(isConnected() ? " " : " [disconnected] ")
		);
	}
	
	public Object[] getSpriteArgs()
	{
		spriteArgs[0] = activity;
		
		if (type.getFootprint() == Footprint.VEHICLE)
		{
			spriteArgs[1] = dir;
			
			if (isTruck())
			{
				spriteArgs[2] = cargo.getType();
			}
		}
		
		return spriteArgs;
	}
	
	public SpriteGroup getAnimationSequence()
	{
		return type.getSpriteSet().get(getSpriteArgs());
	}
	
	public Sprite getSprite(SpriteLibrary lib)
	{
		if (type.getSpriteSet() == null)
		{
			try
			{
				lib.loadModule(type.getName());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if (type.getSpriteSet() == null)
			{
				throw new Error("Could not load spriteset: " + type.getName());
			}
		}
		
		SpriteGroup group = getAnimationSequence();
		
		if (group instanceof EnumSpriteGroup)
		{
			EnumSpriteGroup<?> enumGroup = (EnumSpriteGroup<?>) group;
			Class<?> enumClass = enumGroup.getEnumType();
			
			if (enumClass.equals(Direction.class))
			{
				return enumGroup.getFrame(dir.ordinal());
			}
			else if (enumClass.equals(HealthBracket.class))
			{
				return enumGroup.getFrame(getHealthBracket().ordinal());
			}
			else
			{
				throw new Error("Unsupported enum type" + enumClass);
			}
		}
		else
		{
			return group.getFrame(animationFrame);
		}
	}
	
	public int getAnimationFrame()
	{
		return animationFrame;
	}
	
	public void resetAnimationFrame()
	{
		animationFrame = 0;
	}
	
	public void incrementAnimationFrame()
	{
		animationFrame++;
	}
	
	public void setAnimationFrame(int frame)
	{
		animationFrame = frame;
	}
	
	public void incrementAnimationFrame(int amount)
	{
		animationFrame += amount;
	}
	
	public boolean isArachnid()
	{
		return type.getName().contains("Spider")
			|| type.getName().contains("Scorpion");
	}
	
	public boolean isStructure()
	{
		return type.isStructureType();
	}
	
	public boolean isGuardPost()
	{
		return type.isGuardPostType();
	}
	
	public boolean isTruck()
	{
		return type.getName().contains("Truck");
	}
	
	public boolean isMine()
	{
		return type.getName().endsWith("Mine");
	}
	
	public boolean isMiner()
	{
		return type.getName().contains("Miner");
	}
	
	public boolean isTurret()
	{
		return type.getName().contains("Turret");
	}
	
	public boolean isStarflare()
	{
		return turret != null
		? turret.getType().getName().contains("Starflare")
		: type.getName().contains("Starflare");
	}
	
	public boolean isSupernova()
	{
		return turret != null
			? turret.getType().getName().contains("Supernova")
			: type.getName().contains("Supernova");
	}
	
	public LayeredMap getContainer()
	{
		return chassis != null ? chassis.container : container;
	}
	
	public LayeredMap getMap()
	{
		return chassis != null ? chassis.container : container;
	}
	
	public boolean isFloating()
	{
		return container == null || pos == null;
	}
	
	public void rotate(int steps)
	{
		this.dir = dir.rotate(steps);
		resetAnimationFrame();
	}
	
	public void setDirection(Direction dir)
	{
		this.dir = dir;
		resetAnimationFrame();
	}
	
	public Direction getDirection()
	{
		return dir;
	}
	
	/**
	 * Gets the Footprint for this Unit.
	 * 
	 * Non-structures always have Footprints with equal inner and outer
	 * Regions, with dimensions 1 by 1.
	 */
	public Footprint getFootprint()
	{
		return chassis != null
			? chassis.type.getFootprint()
			: type.getFootprint();
	}

	/**
	 * Gets this Unit's current Position.
	 * 
	 * Is never null as long as this Unit is contained by a UnitLayer.
	 * 
	 * Position is always null if this Unit is not contained by a UnitLayer.
	 */
	public Position getPosition()
	{
		return chassis != null ? chassis.getPosition() : pos;
	}
	
	public Position getPreviousPosition()
	{
		return chassis != null ? chassis.getPreviousPosition() : previous;
	}
	
	/**
	 * Returns true if this Unit is currently occupying the given Position.
	 */
	public boolean isAt(Position pos)
	{
		if (isStructure())
		{
			return getContainer().getUnit(pos) == this;
		}
		else
		{
			return getPosition().equals(pos);
		}
	}
	
	/**
	 * Get absolute pixel position (x) on DisplayPanel.
	 */
	public int getAbsX()
	{
		int spotSize = getMap().getSpotSize();
		int tileSize = getMap().getDisplayPanel().getTileSize();
		
		double absX = getX() + getXOffset() / (double) spotSize;
		
		return (int) (absX * tileSize);
	}
	
	/**
	 * Get absolute pixel position (y) on DisplayPanel.
	 */
	public int getAbsY()
	{
		int spotSize = getMap().getSpotSize();
		int tileSize = getMap().getDisplayPanel().getTileSize();
		
		double absY = getY() + getYOffset() / (double) spotSize;
		
		return (int) (absY * tileSize);
	}
	
	/**
	 * Gets the x-coordinate of this Unit's current Position.
	 * 
	 * Position will not be set if Unit is not attached to a UnitLayer.
	 * 
	 * @throws IllegalStateException If Position is not set.
	 */
	public int getX()
	{
		if (chassis != null)
			return chassis.getX();
		
		if (pos == null)
			throw new IllegalStateException(POS_NOT_SET);
		
		return pos.x;
	}

	/**
	 * Gets the y-coordinate of this Unit's current Position.
	 * 
	 * Position will not be set if Unit is not attached to a UnitLayer.
	 * 
	 * @throws IllegalStateException If Position is not set.
	 */
	public int getY()
	{
		if (chassis != null)
			return chassis.getY();
		
		if (pos == null)
			throw new IllegalStateException(POS_NOT_SET);
		
		return pos.y;
	}
	
	/**
	 * Returns the width of this Unit's footprint at its widest.
	 */
	public int getWidth()
	{
		return getType().getFootprint().getInnerRegion().w;
	}
	
	/**
	 * Returns the height of this Unit's footprint at its tallest.
	 */
	public int getHeight()
	{
		return getType().getFootprint().getInnerRegion().h;
	}
	
	public Region getOccupiedBounds()
	{
		return type.getFootprint().getInnerRegion().move(pos);
	}
	
	public boolean isIdle()
	{
		return idle;
	}
	
	public void idle()
	{
		if (isStructure() && !type.isGuardPostType())
		{
			idle = true;
			cancelAssignments();
		}
	}
	
	public void activate()
	{
		idle = false;
	}
	
	public boolean isDisabled()
	{
		return isStructure() &&
			(getHealthBracket() == HealthBracket.RED
			|| (needsConnection() && !isConnected()));
	}
	
	/**
	 * Sets the default Task to be run when the tasklist is empty.
	 * 
	 * @throws IllegalArgumentException
	 *         If task is not appropriate for this type of unit
	 */
	public void setDefaultTask(Task defaultTask)
	{
		if (! defaultTask.isAcceptable(this))
			throw new IllegalArgumentException(TASK_NOT_ACCEPTABLE);
		
		this.defaultTask = defaultTask;
	}
	
	/**
	 * Gets the default Task, null if one is not set.
	 */
	public Task getDefaultTask()
	{
		return defaultTask;
	}
	
	/**
	 * Returns true if this Unit's task list is empty.
	 */
	public boolean isBusy()
	{
		return !taskList.isEmpty();
	}
	
	/**
	 * Removes all interruptible Tasks from the bottom of the list.
	 */
	public void cancelAssignments()
	{
		while (!taskList.isEmpty() && taskList.getLast().isInterruptible())
			taskList.removeLast();
	}
	
	/**
	 * Inserts task in task list before the first interruptible task.
	 */
	public void interrupt(Task task)
	{
		ListIterator<Task> taskIterator = taskList.listIterator();
		
		while (taskIterator.hasNext())
			if (taskIterator.next().isInterruptible())
			{
				taskIterator.previous();
				taskIterator.add(task);
				return;
			}
		
		taskList.addLast(task);
	}
	
	/**
	 * Adds task to the bottom of the Task list.
	 * 
	 * @throws IllegalArgumentException
	 *         If task is not appropriate for this type of unit
	 */
	public void assignLater(Task task)
	{
		if (! task.isAcceptable(this))
			throw new IllegalArgumentException(TASK_NOT_ACCEPTABLE);
		
		taskList.addLast(task);
	}
	
	/**
	 * Removes all interruptible Tasks from the bottom of the list and
	 * adds given Task to the bottom.
	 * 
	 * @throws IllegalArgumentException
	 *         If task is not appropriate for this type of unit
	 */
	public void assignNow(Task task)
	{
		if (! task.isAcceptable(this))
			throw new IllegalArgumentException(TASK_NOT_ACCEPTABLE);
		
		cancelAssignments();
		taskList.addLast(task);
	}
	
	/**
	 * Adds given Task to the top of the list without changing others.
	 * 
	 * @throws IllegalArgumentException
	 *         If task is not appropriate for this type of unit
	 */
	public void assignNext(Task task)
	{
		if (! task.isAcceptable(this))
			throw new IllegalArgumentException(TASK_NOT_ACCEPTABLE);
		
		taskList.addFirst(task);
	}

	/**
	 * Called by a Task when the Task is complete.
	 * 
	 * This method should only be called from the classes Unit or Task
	 * or subclasses thereof.
	 */
	public void completeTask(Task task)
	{
		if (! task.equals(taskList.getFirst()))
			throw new IllegalArgumentException();
		
		taskList.removeFirst();
	}
	
	/**
	 * Runs a step of the Task at the top of the list, or the default Task
	 * if one is set.
	 */
	public void step()
	{
		recharge(1);
		
		if (!taskList.isEmpty())
		{
			taskList.getFirst().step(this);
		}
		else
		{
			if (defaultTask != null)
				defaultTask.step(this);
		}
	}

	/**
	 * Friend method used to keep instances of Unit in sync with a UnitLayer.
	 * 
	 * This method should only be called from the classes Unit or UnitLayer
	 * or subclasses thereof.
	 */
	public void setContainer(LayeredMap container)
	{
		this.container = container;
	}

	/**
	 * Friend method used to keep instances of Unit in sync with a UnitLayer.
	 * 
	 * This method should only be called from the classes Unit or UnitLayer
	 * or subclasses thereof.
	 */
	public void setPosition(Position pos)
	{
		this.previous = this.pos;
		this.pos = pos;
	}
	
	/**
	 * Friend method used to access mutable list of reserved positions
	 * held by this unit.
	 */
	public Set<Position> getReservations()
	{
		return reservations;
	}
	
	private static final String POS_NOT_SET =
		"Position not set/Unit not attached to a UnitLayer";
	
	private static final String TASK_NOT_ACCEPTABLE =
		"Task not for this unit type";
}
