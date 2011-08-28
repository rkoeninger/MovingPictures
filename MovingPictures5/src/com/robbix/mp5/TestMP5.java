package com.robbix.mp5;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.robbix.mp5.ai.task.MineRouteTask;
import com.robbix.mp5.ai.task.SteerTask;
import com.robbix.mp5.basics.Filter;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.CommandButton;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.PlayerStatus;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.UnitStatus;
import com.robbix.mp5.ui.overlay.BuildStructureOverlay;
import com.robbix.mp5.ui.overlay.PlaceBulldozeOverlay;
import com.robbix.mp5.ui.overlay.PlaceResourceOverlay;
import com.robbix.mp5.ui.overlay.PlaceTubeOverlay;
import com.robbix.mp5.ui.overlay.PlaceUnitOverlay;
import com.robbix.mp5.ui.overlay.PlaceWallOverlay;
import com.robbix.mp5.ui.overlay.SelectUnitOverlay;
import com.robbix.mp5.ui.overlay.SpawnMeteorOverlay;
import com.robbix.mp5.unit.Cargo;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;

/**
 * Test code for MovingPictures 5.
 * 
 * @author bort
 *
 */
public class TestMP5
{
	private static final String DEFAULT_TILE_SET = "newTerraDirt";
	private static final String DEFAULT_MAP = "plain";
	private static final File RES_DIR = new File("./res");
	
	private static Player currentPlayer;
	
	private static Game game;
	private static Engine engine;
	
	private static JFrame frame;
	private static Map<Integer, JMenuItem> playerMenuItems =
		new HashMap<Integer, JMenuItem>();
	
	public static void main(String[] args) throws IOException
	{
		Map<String, Method> demos = getDemos();
		
		/*-------------------------------------------------------------------*
		 * Parse command-line arguments
		 */
		boolean lazyLoadSprites = false;
		boolean lazyLoadSounds  = false;
		boolean soundOn         = false;
		boolean musicOn         = false;
		String demoName         = null;
		String mapName          = null;
		String tileSetName      = null;
		
		for (int a = 0; a < args.length; ++a)
		{
			int colonIndex = Math.max(args[a].indexOf(':'), 0);
			String option = args[a].substring(colonIndex + 1);
			
			if (args[a].equals("-lazyLoadSprites"))
			{
				lazyLoadSprites = true;
			}
			else if (args[a].equals("-lazyLoadSounds"))
			{
				lazyLoadSounds = true;
			}
			else if (args[a].equals("-soundOn"))
			{
				soundOn = true;
			}
			else if (args[a].equals("-soundOff"))
			{
				soundOn = false;
			}
			else if (args[a].equals("-musicOn"))
			{
				musicOn = true;
			}
			else if (args[a].equals("-musicOff"))
			{
				musicOn = false;
			}
			else if (args[a].startsWith("-demo:") && demos.containsKey(option))
			{
				if (demoName != null || mapName != null)
					throw new IllegalArgumentException("Duplicate map/demos");
				
				demoName = option;
			}
			else if (args[a].startsWith("-map:"))
			{
				if (demoName != null || mapName != null)
					throw new IllegalArgumentException("Duplicate map/demos");
				
				mapName = option;
			}
			else if (args[a].startsWith("-tileSet:"))
			{
				if (tileSetName != null)
					throw new IllegalArgumentException("Duplicate tile sets");
				
				tileSetName = option;
			}
		}
		
		if (tileSetName == null)
			tileSetName = DEFAULT_TILE_SET;
		
		if (mapName == null)
		{
			if (demoName == null)
			{
				mapName = DEFAULT_MAP;
			}
			else
			{
				mapName = getDemoMaps().get(demoName);
				
				if (mapName == null)
					throw new Error("no map defined for " + demoName);
			}
		}
		
		/*-------------------------------------------------------------------*
		 * Load map, units, sprites, cursors
		 * Create players
		 * Create engine
		 * Init Mediator
		 * Setup demo
		 */
		game = Game.load(
			RES_DIR,
			mapName,
			tileSetName,
			lazyLoadSprites,
			lazyLoadSounds
		);
		engine = new Engine(game);
		Mediator.initMediator(game);
		
		if (soundOn) Mediator.sounds.start();
		if (musicOn) Mediator.sounds.playMusic("ep1");
		
		currentPlayer = game.getDefaultPlayer();
		
		if (demoName != null)
		{
			Method setupDemo = demos.get(demoName);
			
			if (setupDemo == null)
				throw new IllegalArgumentException(demoName + " not found");
			
			try
			{
				setupDemo.invoke(null, game);
			}
			catch (Exception exc)
			{
				throw new Error("Failed to setup demo", exc);
			}
		}
		
		Utils.trySystemLookAndFeel();
		
		/*-------------------------------------------------------------------*
		 * Prepare live-updating status labels
		 */
		final JLabel unitStatusLabel = new JLabel();
		unitStatusLabel.setText("Args: " + Arrays.toString(args));
		unitStatusLabel.setPreferredSize(new Dimension(100, 20));
		
		final UnitStatus unitStatus = new UnitStatus()
		{
			Unit myUnit = null;
			
			Object lock = new Object();
			
			/* Init */
			{
				new Timer(100, new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						synchronized(lock)
						{
							if (myUnit != null)
							{
								unitStatusLabel.setText(
									myUnit.getStatusString());
							}
							else
							{
								unitStatusLabel.setText("");
							}
						}
					}
				}).start();
			}
			
			public void showStatus(Unit unit)
			{
				synchronized(lock)
				{
					myUnit = unit;
				}
			}
		};
		
		final JLabel playerStatusLabel = new JLabel();
		playerStatusLabel.setText(currentPlayer.getStatusString());
		playerStatusLabel.setPreferredSize(new Dimension(100, 20));
		
		final PlayerStatus playerStatus = new PlayerStatus()
		{
			Player myPlayer = null;
			
			Object lock = new Object();
			
			/* Init */
			{
				new Timer(100, new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						synchronized (lock)
						{
							if (myPlayer != null)
							{
								playerStatusLabel.setText(
									myPlayer.getStatusString());
							}
							else
							{
								playerStatusLabel.setText("");
							}
						}
					}
				}).start();
			}
			
			public void showStatus(Player player)
			{
				synchronized (lock)
				{
					myPlayer = player;
				}
			}
		};
		
		/*-------------------------------------------------------------------*
		 * Load command button icons
		 */
		final ActionListener commandButtonListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				game.getDisplay().fireCommandButton(e.getActionCommand());
			}
		};
		
		final JToolBar commandBar = new JToolBar();
		commandBar.setFloatable(false);
		commandBar.setRollover(true);
		commandBar.setOrientation(JToolBar.VERTICAL);
		
		for (File iconDir : new File(RES_DIR, "art/commandButtons").listFiles())
		{
			if (!iconDir.isDirectory()) continue;
			
			ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>();
			
			for (File iconFile : iconDir.listFiles(Utils.BMP))
			{
				icons.add(new ImageIcon(Utils.shrink(ImageIO.read(iconFile))));
			}
			
			if (icons.isEmpty()) continue;
			
			CommandButton button = new CommandButton(icons);
			button.setActionCommand(iconDir.getName());
			button.addActionListener(commandButtonListener);
			button.setFocusable(false);
			commandBar.add(button);
		}
		
		/*-------------------------------------------------------------------*
		 * Build UI
		 */
		final DisplayPanel panel = game.getDisplay();
		panel.setUnitStatus(unitStatus);
		panel.setPlayerStatus(playerStatus);
		panel.setShowGrid(false);
		panel.setShowUnitLayerState(false);
		panel.setShowTerrainCostMap(false);
		panel.setShowTerrainCostValues(true);
		panel.pushOverlay(new SelectUnitOverlay());
		panel.showStatus(currentPlayer);
		
		final JMenuBar menuBar = new JMenuBar();
		final JMenu engineMenu = new JMenu("Engine");
		final JMenu displayMenu = new JMenu("Display");
		final JMenu terrainMenu = new JMenu("Terrain");
		final JMenu unitMenu = new JMenu("Units");
		final JMenu playerMenu = new JMenu("Players");
		final JMenuItem pauseMenuItem = new JMenuItem("Pause");
		final JMenuItem stepMenuItem = new JMenuItem("Step Once");
		stepMenuItem.setEnabled(false);
		final JMenuItem throttleMenuItem = new JMenuItem("Unthrottle");
		throttleMenuItem.setEnabled(engine.isThrottled());
		final JMenuItem exitMenuItem = new JMenuItem("Exit");
		final JMenu disastersMenu = new JMenu("Disasters");
		final JMenuItem spawnMeteorMenuItem = new JMenuItem("Spawn Meteor");
		final JMenuItem meteorShowerMenuItem = new JMenuItem("Meteor Shower");
		final JMenuItem placeWallMenuItem = new JMenuItem("Place Wall");
		final JMenuItem placeTubeMenuItem = new JMenuItem("Place Tube");
		final JMenuItem placeBulldozeMenuItem = new JMenuItem("Bulldoze");
		final JMenu placeResourceMenu = new JMenu("Add Ore");
		final JMenuItem placeCommon1 = new JMenuItem("Common Low");
		final JMenuItem placeCommon2 = new JMenuItem("Common Med");
		final JMenuItem placeCommon3 = new JMenuItem("Common High");
		final JMenuItem placeRare1 = new JMenuItem("Rare Low");
		final JMenuItem placeRare2 = new JMenuItem("Rare Med");
		final JMenuItem placeRare3 = new JMenuItem("Rare High");
		final JMenu soundMenu = new JMenu("Sound");
		final JMenuItem playSoundMenuItem = new JCheckBoxMenuItem("Play Sounds");
		final JMenuItem playMusicMenuItem = new JCheckBoxMenuItem("Play Music");
		playSoundMenuItem.setSelected(game.getSoundBank().isRunning());
		playMusicMenuItem.setSelected(game.getSoundBank().isMusicPlaying());
		final JMenu addUnitMenu = new JMenu("Add");
		final JMenu buildStructureMenu = new JMenu("Build");
		final JMenuItem removeAllUnitsMenuItem = new JMenuItem("Remove All");
		final JMenuItem addPlayerMenuItem = new JMenuItem("Add Player...");
		playerMenu.add(addPlayerMenuItem);
		playerMenu.addSeparator();
		
		/*-------------------------------------------------------------------*
		 * Add player select menu items
		 */
		final ButtonGroup playerSelectButtonGroup = new ButtonGroup();
		
		final ActionListener playerSelectListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectPlayer(Integer.valueOf(e.getActionCommand()));
			}
		};
		
		for (Player player : game.getPlayers())
		{
			final JMenuItem playerSelectMenuItem =
				new JRadioButtonMenuItem(player.getName());
			playerSelectButtonGroup.add(playerSelectMenuItem);
			
			if (currentPlayer.equals(player))
				playerSelectMenuItem.setSelected(true);
			
			playerSelectMenuItem.setActionCommand(
				String.valueOf(player.getID())
			);
			playerMenu.add(playerSelectMenuItem);
			playerSelectMenuItem.addActionListener(playerSelectListener);
			playerMenuItems.put(player.getID(), playerSelectMenuItem);
		}
		
		/*-------------------------------------------------------------------*
		 * Add place unit menu items
		 */
		final List<String> names = game.getUnitFactory().getUnitNames();
		final List<String> types = game.getUnitFactory().getUnitTypes();
		
		// I know it's a bubble sort
		for (int a = 0; a < names.size(); ++a)
		{
			for (int b = 0; b < names.size(); ++b)
			{
				if (names.get(a).compareTo(names.get(b)) < 0)
				{
					String temp;
					temp = names.get(a);
					names.set(a, names.get(b));
					names.set(b, temp);
					temp = types.get(a);
					types.set(a, types.get(b));
					types.set(b, temp);
				}
			}
		}
		
		final ActionListener addUnitListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				game.getUnitFactory().setDefaultOwner(currentPlayer);
				panel.pushOverlay(new PlaceUnitOverlay(
					game.getUnitFactory(),
					e.getActionCommand()
				));
			}
		};
		
		for (int i = 0; i < types.size(); ++i)
		{
			final JMenuItem addUnitMenuItem = new JMenuItem(names.get(i));
			addUnitMenu.add(addUnitMenuItem);
			addUnitMenuItem.setActionCommand(types.get(i));
			addUnitMenuItem.addActionListener(addUnitListener);
		}
		
		/*-------------------------------------------------------------------*
		 * Add build structure menu items
		 */
		final ActionListener buildStructureListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				game.getUnitFactory().setDefaultOwner(currentPlayer);
				panel.pushOverlay(new BuildStructureOverlay(
					game.getUnitFactory(),
					e.getActionCommand()
				));
			}
		};
		
		for (int i = 0; i < types.size(); ++i)
		{
			UnitType unitType = game.getUnitFactory().getType(types.get(i));
			
			if (unitType.isStructureType() || unitType.isGuardPostType())
			{
				final JMenuItem buildStructureMenuItem = new JMenuItem(names.get(i));
				buildStructureMenu.add(buildStructureMenuItem);
				buildStructureMenuItem.setActionCommand(types.get(i));
				buildStructureMenuItem.addActionListener(buildStructureListener);
			}
		}

		/*-------------------------------------------------------------------*
		 * Add display option menu items
		 */
		final List<String> options = panel.getOptionNames();
		
		ActionListener displayOptionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.setOptionValue(
					e.getActionCommand(),
					!panel.getOptionValue(e.getActionCommand())
				);
			}
		};
		
		for (int i = 0; i < options.size(); ++i)
		{
			final JMenuItem panelOptionMenuItem = new JMenuItem(options.get(i));
			displayMenu.add(panelOptionMenuItem);
			panelOptionMenuItem.setActionCommand(options.get(i));
			
			if (options.get(i).equals("Grid"))
				panelOptionMenuItem.setAccelerator(
					KeyStroke.getKeyStroke(
						KeyEvent.VK_G,
						KeyEvent.ALT_DOWN_MASK
					)
				);
			
			panelOptionMenuItem.addActionListener(displayOptionListener);
		}
		
		pauseMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0)
		);
		stepMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)
		);
		
		placeResourceMenu.add(placeCommon1);
		placeResourceMenu.add(placeCommon2);
		placeResourceMenu.add(placeCommon3);
		placeResourceMenu.add(placeRare1);
		placeResourceMenu.add(placeRare2);
		placeResourceMenu.add(placeRare3);
		
		engineMenu.add(pauseMenuItem);
		engineMenu.add(throttleMenuItem);
		engineMenu.add(stepMenuItem);
		engineMenu.addSeparator();
		engineMenu.add(exitMenuItem);
		soundMenu.add(playSoundMenuItem);
		soundMenu.add(playMusicMenuItem);
		terrainMenu.add(placeWallMenuItem);
		terrainMenu.add(placeTubeMenuItem);
		terrainMenu.add(placeBulldozeMenuItem);
		terrainMenu.add(placeResourceMenu);
		disastersMenu.add(spawnMeteorMenuItem);
		disastersMenu.add(meteorShowerMenuItem);
		unitMenu.add(addUnitMenu);
		unitMenu.add(buildStructureMenu);
		unitMenu.addSeparator();
		unitMenu.add(removeAllUnitsMenuItem);
		
		menuBar.add(engineMenu);
		menuBar.add(displayMenu);
		menuBar.add(soundMenu);
		menuBar.add(terrainMenu);
		menuBar.add(disastersMenu);
		menuBar.add(playerMenu);
		menuBar.add(unitMenu);
		
		Image smallIcon = ImageIO.read(new File(RES_DIR, "art/smallIcon.png"));
		Image mediumIcon = ImageIO.read(new File(RES_DIR, "art/mediumIcon.png"));
		
		JPanel statusesPanel = new JPanel();
		statusesPanel.setLayout(new GridLayout(2, 1));
		statusesPanel.add(unitStatusLabel);
		statusesPanel.add(playerStatusLabel);
		
		frame = new JFrame("Moving Pictures");
		frame.setJMenuBar(menuBar);
		frame.setLayout(new BorderLayout());
//		frame.add(commandBar, BorderLayout.EAST);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(statusesPanel, BorderLayout.SOUTH);
		frame.setResizable(false);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setIconImages(Arrays.asList(smallIcon, mediumIcon));
		frame.setVisible(true);
		
		meteorShowerMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				engine.addTrigger(new MeteorShowerTrigger(1, 300));
				game.getSoundBank().play("savant_meteorApproaching");
			}
		});
		
		spawnMeteorMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new SpawnMeteorOverlay());
			}
		});
		
		playSoundMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (playSoundMenuItem.isSelected())
				{
					game.getSoundBank().start();
				}
				else
				{
					game.getSoundBank().stop();
					game.getSoundBank().flush();
				}
			}
		});
		
		playMusicMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (playMusicMenuItem.isSelected())
				{
					game.getSoundBank().playMusic("ep1");
				}
				else
				{
					game.getSoundBank().killMusic();
				}
			}
		});
		
		placeCommon1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get1BarCommon()));
			}
		});
		
		placeCommon2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get2BarCommon()));
			}
		});
		
		placeCommon3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get3BarCommon()));
			}
		});
		
		placeRare1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get1BarRare()));
			}
		});
		
		placeRare2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get2BarRare()));
			}
		});
		
		placeRare3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get3BarRare()));
			}
		});
		
		placeBulldozeMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceBulldozeOverlay());
			}
		});
		
		placeWallMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceWallOverlay());
			}
		});
		
		placeTubeMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceTubeOverlay());
			}
		});
		
		removeAllUnitsMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int result = JOptionPane.showConfirmDialog(
					frame,
					"Are you sure?",
					"Remove All Units",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				
				if (result != JOptionPane.YES_OPTION)
					return;
				
				game.getMap().clearAllUnits();
			}
		});
		
		pauseMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				engine.pause();
				stepMenuItem.setEnabled(!engine.isRunning());
				pauseMenuItem.setText(
					engine.isRunning()
					? "Pause"
					: "Resume"
				);
			}
		});
		
		throttleMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				engine.toggleThrottle();
				throttleMenuItem.setText(
					engine.isThrottled()
					? "Unthrottle"
					: "Throttle"
				);
			}
		});
		
		stepMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				engine.step();
			}
		});
		
		exitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		
		addPlayerMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(frame, "Player Name");
				
				if (name == null)
					return;
				
				int colorHue;
				
				for (;;)
				{
					String colorHueString = JOptionPane.showInputDialog(
						frame,
						"Player Color\nMust be integer 0 to 359"
					);
					
					if (colorHueString == null)
						return;
					
					try
					{
						colorHue = Integer.valueOf(colorHueString);
					}
					catch (NumberFormatException nfe)
					{
						continue;
					}
					
					if (colorHue >= 0 && colorHue <= 359)
						break;
				}
				
				Player newPlayer = new Player(game.getPlayers().size(), name, colorHue);
				game.addPlayer(newPlayer);
				final JMenuItem playerSelectMenuItem =
					new JRadioButtonMenuItem(name);
				playerSelectButtonGroup.add(playerSelectMenuItem);
				playerSelectMenuItem.setActionCommand(
					String.valueOf(newPlayer.getID()));
				playerMenu.add(playerSelectMenuItem);
				playerSelectMenuItem.addActionListener(playerSelectListener);
				playerSelectMenuItem.setSelected(true);
				currentPlayer = newPlayer;
				game.getUnitFactory().setDefaultOwner(currentPlayer);
			}
		});
		
		/*--------------------------------------------------------------------*
		 * Start mechanics
		 */
		engine.play();
	}
	
	private static void selectPlayer(int playerID)
	{
		Player player = game.getPlayer(playerID);
		currentPlayer = player;
		Mediator.factory.setDefaultOwner(player);
		game.getDisplay().showStatus(currentPlayer);
		JMenuItem menuItem = playerMenuItems.get(playerID);
		
		if (menuItem != null)
			menuItem.setSelected(true);
	}
	
	public static List<String> getAvailableTileSets()
	{
		List<String> tileSets = new ArrayList<String>();
		
		for (File file : new File("./res/tileset").listFiles())
		{
			if (file.isDirectory() && new File(file, "plain").exists())
			{
				tileSets.add(file.getName());
			}
		}
		
		return tileSets;
	}
	
	public static List<String> getAvailableMaps()
	{
		List<String> maps = new ArrayList<String>();
		
		for (File file : new File("./res/terrain").listFiles())
		{
			String fileName = file.getName();
			
			if (file.isFile() && fileName.endsWith(".txt"))
			{
				maps.add(fileName.substring(0, fileName.length() - 4));
			}
		}
		
		return maps;
	}
	
	public static Map<String, Method> getDemos()
	{
		Map<String, Method> demos = new HashMap<String, Method>();
		
		for (Method method : TestMP5.class.getDeclaredMethods())
		{
			int paramCount = method.getParameterTypes().length;
			String methodName = method.getName();
			
			if (methodName.startsWith("setup")
			 && methodName.endsWith("Demo")
			 && paramCount == 1
			 && method.getParameterTypes()[0].equals(Game.class))
			{
				char firstChar = Character.toLowerCase(methodName.charAt(5));
				methodName = methodName.substring(6, methodName.length());
				methodName = firstChar + methodName;
				
				demos.put(methodName, method);
			}
		}
		
		return demos;
	}
	
	public static Map<String, String> getDemoMaps()
	{
		Map<String, String> demoMaps = new HashMap<String, String>();
		
		for (Method method : TestMP5.class.getDeclaredMethods())
		{
			int paramCount = method.getParameterTypes().length;
			String methodName = method.getName();
			
			if (methodName.startsWith("map")
			 && methodName.endsWith("Demo")
			 && paramCount == 0)
			{
				char firstChar = Character.toLowerCase(methodName.charAt(3));
				String demoName = methodName.substring(4, methodName.length());
				demoName = firstChar + demoName;
				
				String mapName = null;
				
				try
				{
					mapName = (String) method.invoke(null, (Object[])null);
				}
				catch (Exception exc)
				{
					throw new Error("could not get map name for demo");
				}
				
				demoMaps.put(demoName, mapName);
			}
		}
		
		return demoMaps;
	}
	
	public static String mapMeteorDemo()
	{
		return "widePlain";
	}
	
	public static void setupMeteorDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Player player1 = new Player(1, "Targets", 45);
		
		game.addPlayer(player1);
		
		selectPlayer(1);
		
		factory.setDefaultOwner(player1);
		
		for (int x = 0; x < 12; ++x)
		for (int y = 0; y < 20; ++y)
		{
			map.addUnit(factory.newUnit("pScout"), new Position(x, y));
		}
		
		for (int x = 12; x < 30; x += 2)
		for (int y = 13; y < 19; y += 2)
		{
			map.addUnit(factory.newUnit("pResidence"), new Position(x, y));
		}
		
		map.addUnit(factory.newUnit("eVehicleFactory"), new Position(12, 1));
		map.addUnit(factory.newUnit("eVehicleFactory"), new Position(16, 1));
		map.addUnit(factory.newUnit("eVehicleFactory"), new Position(20, 1));
		map.addUnit(factory.newUnit("eVehicleFactory"), new Position(24, 1));
		map.addUnit(factory.newUnit("eStructureFactory"), new Position(12, 5));
		map.addUnit(factory.newUnit("eStructureFactory"), new Position(16, 5));
		map.addUnit(factory.newUnit("eStructureFactory"), new Position(20, 5));
		map.addUnit(factory.newUnit("eStructureFactory"), new Position(24, 5));
		map.addUnit(factory.newUnit("eCommonSmelter"), new Position(12, 9));
		map.addUnit(factory.newUnit("eCommonSmelter"), new Position(16, 9));
		map.addUnit(factory.newUnit("eCommonSmelter"), new Position(20, 9));
		map.addUnit(factory.newUnit("eCommonSmelter"), new Position(24, 9));
		
		SpriteLibrary lib = game.getSpriteLibrary();
		
		try
		{
			lib.loadModule("pScout");
			lib.loadModule("pResidence");
			lib.loadModule("eVehicleFactory");
			lib.loadModule("eStructureFactory");
			lib.loadModule("eCommonSmelter");
			lib.loadModule("aDeath");
			lib.loadModule("aMeteor");
			lib.loadModule("aStructureStatus");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public static String mapFactoryDemo()
	{
		return "widePlain";
	}
	
	public static void setupFactoryDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Player player1 = new Player(1, "Factories", 275);
		
		game.addPlayer(player1);
		
		selectPlayer(1);
		
		player1.addResource(ResourceType.COMMON_ORE, 50000);
		player1.addResource(ResourceType.RARE_ORE,   50000);
		player1.addResource(ResourceType.FOOD,       50000);
		
		Unit convec1 = factory.newUnit("eConVec", player1);
		Unit convec2 = factory.newUnit("eConVec", player1);
		Unit convec3 = factory.newUnit("eConVec", player1);
		Unit convec4 = factory.newUnit("eConVec", player1);
		
		convec1.setCargo(Cargo.newConVecCargo("eVehicleFactory"));
		convec2.setCargo(Cargo.newConVecCargo("eStructureFactory"));
		convec3.setCargo(Cargo.newConVecCargo("eCommonSmelter"));
		convec4.setCargo(Cargo.newConVecCargo("eCommandCenter"));
		
		map.addUnit(convec1, new Position(9,  7));
		map.addUnit(convec2, new Position(10, 7));
		map.addUnit(convec3, new Position(11, 7));
		map.addUnit(convec4, new Position(12, 7));
	}
	
	public static String mapMineRouteDemo()
	{
		return "widePlain";
	}
	
	public static void setupMineRouteDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Player player1 = new Player(1, "Mining Operation", 200);
		
		game.addPlayer(player1);
		
		selectPlayer(1);
		
		ResourceDeposit res1 = ResourceDeposit.get1BarCommon();
		ResourceDeposit res2 = ResourceDeposit.get2BarCommon();
		ResourceDeposit res3 = ResourceDeposit.get3BarCommon();
		ResourceDeposit res4 = ResourceDeposit.get1BarCommon();
		ResourceDeposit res5 = ResourceDeposit.get2BarCommon();
		ResourceDeposit res6 = ResourceDeposit.get3BarCommon();
		
		map.putResourceDeposit(res1, new Position(22, 2));
		map.putResourceDeposit(res2, new Position(22, 8));
		map.putResourceDeposit(res3, new Position(22, 14));
		map.putResourceDeposit(res4, new Position(26, 2));
		map.putResourceDeposit(res5, new Position(26, 8));
		map.putResourceDeposit(res6, new Position(26, 14));
		
		Unit smelter1 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter2 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter3 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter4 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter5 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter6 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter7 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter8 = factory.newUnit("eCommonSmelter", player1);
		Unit smelter9 = factory.newUnit("eCommonSmelter", player1);
		
		Unit mine1 = factory.newUnit("eCommonMine", player1);
		Unit mine2 = factory.newUnit("eCommonMine", player1);
		Unit mine3 = factory.newUnit("eCommonMine", player1);
		Unit mine4 = factory.newUnit("eCommonMine", player1);
		Unit mine5 = factory.newUnit("eCommonMine", player1);
		Unit mine6 = factory.newUnit("eCommonMine", player1);
		
		Unit truck1  = factory.newUnit("eCargoTruck", player1);
		Unit truck2  = factory.newUnit("eCargoTruck", player1);
		Unit truck3  = factory.newUnit("eCargoTruck", player1);
		Unit truck4  = factory.newUnit("eCargoTruck", player1);
		Unit truck5  = factory.newUnit("eCargoTruck", player1);
		Unit truck6  = factory.newUnit("eCargoTruck", player1);
		Unit truck7  = factory.newUnit("eCargoTruck", player1);
		Unit truck8  = factory.newUnit("eCargoTruck", player1);
		Unit truck9  = factory.newUnit("eCargoTruck", player1);
		Unit truck10 = factory.newUnit("eCargoTruck", player1);
		Unit truck11 = factory.newUnit("eCargoTruck", player1);
		Unit truck12 = factory.newUnit("eCargoTruck", player1);
		Unit truck13 = factory.newUnit("eCargoTruck", player1);
		Unit truck14 = factory.newUnit("eCargoTruck", player1);
		Unit truck15 = factory.newUnit("eCargoTruck", player1);
		Unit truck16 = factory.newUnit("eCargoTruck", player1);
		Unit truck17 = factory.newUnit("eCargoTruck", player1);
		Unit truck18 = factory.newUnit("eCargoTruck", player1);
		Unit truck19 = factory.newUnit("eCargoTruck", player1);
		Unit truck20 = factory.newUnit("eCargoTruck", player1);
		Unit truck21 = factory.newUnit("eCargoTruck", player1);
		Unit truck22 = factory.newUnit("eCargoTruck", player1);
		Unit truck23 = factory.newUnit("eCargoTruck", player1);
		Unit truck24 = factory.newUnit("eCargoTruck", player1);
		Unit truck25 = factory.newUnit("eCargoTruck", player1);
		Unit truck26 = factory.newUnit("eCargoTruck", player1);
		Unit truck27 = factory.newUnit("eCargoTruck", player1);
		Unit truck28 = factory.newUnit("eCargoTruck", player1);
		Unit truck29 = factory.newUnit("eCargoTruck", player1);
		Unit truck30 = factory.newUnit("eCargoTruck", player1);
		Unit truck31 = factory.newUnit("eCargoTruck", player1);
		Unit truck32 = factory.newUnit("eCargoTruck", player1);
		Unit truck33 = factory.newUnit("eCargoTruck", player1);
		Unit truck34 = factory.newUnit("eCargoTruck", player1);
		Unit truck35 = factory.newUnit("eCargoTruck", player1);
		Unit truck36 = factory.newUnit("eCargoTruck", player1);
		
		map.addUnit(smelter1, new Position(2,  2));
		map.addUnit(smelter2, new Position(2,  8));
		map.addUnit(smelter3, new Position(2,  14));
		map.addUnit(smelter4, new Position(8,  2));
		map.addUnit(smelter5, new Position(8,  8));
		map.addUnit(smelter6, new Position(8,  14));
		map.addUnit(smelter7, new Position(14, 2));
		map.addUnit(smelter8, new Position(14, 8));
		map.addUnit(smelter9, new Position(14, 14));
		
		map.putTube(new Position(7,  3));
		map.putTube(new Position(13, 3));
		map.putTube(new Position(7,  9));
		map.putTube(new Position(13, 9));
		map.putTube(new Position(7,  15));
		map.putTube(new Position(13, 15));
		map.putTube(new Position(4,  6));
		map.putTube(new Position(4,  7));
		map.putTube(new Position(4,  12));
		map.putTube(new Position(4,  13));
		map.putTube(new Position(16, 18));
		map.putTube(new Position(17, 18));
		map.putTube(new Position(18, 18));
		map.putTube(new Position(19, 18));
		map.putTube(new Position(20, 18));
		map.putTube(new Position(21, 18));
		map.putTube(new Position(22, 18));
		map.putTube(new Position(23, 18));
		map.putTube(new Position(24, 18));
		
		map.addUnit(factory.newUnit("eCommandCenter", player1), new Position(25, 17));
		
		map.addUnit(mine1, new Position(21, 2));
		map.addUnit(mine2, new Position(21, 8));
		map.addUnit(mine3, new Position(21, 14));
		map.addUnit(mine4, new Position(25, 2));
		map.addUnit(mine5, new Position(25, 8));
		map.addUnit(mine6, new Position(25, 14));
		
		map.addUnit(truck1,  new Position(2,  6));
		map.addUnit(truck2,  new Position(3,  6));
		map.addUnit(truck3,  new Position(4,  6));
		map.addUnit(truck4,  new Position(5,  6));
		map.addUnit(truck5,  new Position(6,  6));
		map.addUnit(truck6,  new Position(7,  6));
		map.addUnit(truck7,  new Position(8,  6));
		map.addUnit(truck8,  new Position(9,  6));
		map.addUnit(truck9,  new Position(10, 6));
		map.addUnit(truck10, new Position(11, 6));
		map.addUnit(truck11, new Position(12, 6));
		map.addUnit(truck12, new Position(13, 6));
		map.addUnit(truck13, new Position(2,  12));
		map.addUnit(truck14, new Position(3,  12));
		map.addUnit(truck15, new Position(4,  12));
		map.addUnit(truck16, new Position(5,  12));
		map.addUnit(truck17, new Position(6,  12));
		map.addUnit(truck18, new Position(7,  12));
		map.addUnit(truck19, new Position(8,  12));
		map.addUnit(truck20, new Position(9,  12));
		map.addUnit(truck21, new Position(10, 12));
		map.addUnit(truck22, new Position(11, 12));
		map.addUnit(truck23, new Position(12, 12));
		map.addUnit(truck24, new Position(13, 12));
		map.addUnit(truck25, new Position(2,  18));
		map.addUnit(truck26, new Position(3,  18));
		map.addUnit(truck27, new Position(4,  18));
		map.addUnit(truck28, new Position(5,  18));
		map.addUnit(truck29, new Position(6,  18));
		map.addUnit(truck30, new Position(7,  18));
		map.addUnit(truck31, new Position(8,  18));
		map.addUnit(truck32, new Position(9,  18));
		map.addUnit(truck33, new Position(10, 18));
		map.addUnit(truck34, new Position(11, 18));
		map.addUnit(truck35, new Position(12, 18));
		map.addUnit(truck36, new Position(13, 18));
		
		truck1 .assignNow(new MineRouteTask(mine1, smelter1));
		truck2 .assignNow(new MineRouteTask(mine4, smelter1));
		truck3 .assignNow(new MineRouteTask(mine2, smelter2));
		truck4 .assignNow(new MineRouteTask(mine5, smelter2));
		truck5 .assignNow(new MineRouteTask(mine3, smelter3));
		truck6 .assignNow(new MineRouteTask(mine6, smelter3));
		truck7 .assignNow(new MineRouteTask(mine1, smelter4));
		truck8 .assignNow(new MineRouteTask(mine4, smelter4));
		truck9 .assignNow(new MineRouteTask(mine2, smelter5));
		truck10.assignNow(new MineRouteTask(mine5, smelter5));
		truck11.assignNow(new MineRouteTask(mine3, smelter6));
		truck12.assignNow(new MineRouteTask(mine6, smelter6));
		truck13.assignNow(new MineRouteTask(mine1, smelter7));
		truck14.assignNow(new MineRouteTask(mine4, smelter7));
		truck15.assignNow(new MineRouteTask(mine2, smelter8));
		truck16.assignNow(new MineRouteTask(mine5, smelter8));
		truck17.assignNow(new MineRouteTask(mine3, smelter9));
		truck18.assignNow(new MineRouteTask(mine6, smelter9));
		truck19.assignNow(new MineRouteTask(mine1, smelter1));
		truck20.assignNow(new MineRouteTask(mine4, smelter1));
		truck21.assignNow(new MineRouteTask(mine2, smelter2));
		truck22.assignNow(new MineRouteTask(mine5, smelter2));
		truck23.assignNow(new MineRouteTask(mine3, smelter3));
		truck24.assignNow(new MineRouteTask(mine6, smelter3));
		truck25.assignNow(new MineRouteTask(mine1, smelter4));
		truck26.assignNow(new MineRouteTask(mine4, smelter4));
		truck27.assignNow(new MineRouteTask(mine2, smelter5));
		truck28.assignNow(new MineRouteTask(mine5, smelter5));
		truck29.assignNow(new MineRouteTask(mine3, smelter6));
		truck30.assignNow(new MineRouteTask(mine6, smelter6));
		truck31.assignNow(new MineRouteTask(mine1, smelter7));
		truck32.assignNow(new MineRouteTask(mine4, smelter7));
		truck33.assignNow(new MineRouteTask(mine2, smelter8));
		truck34.assignNow(new MineRouteTask(mine5, smelter8));
		truck35.assignNow(new MineRouteTask(mine3, smelter9));
		truck36.assignNow(new MineRouteTask(mine6, smelter9));
		
		SpriteLibrary lib = game.getSpriteLibrary();
		
		try
		{
			lib.loadModule("eCargoTruck");
			lib.loadModule("eCommonSmelter");
			lib.loadModule("eCommonMine");
			lib.loadModule("aResource");
			lib.loadModule("aStructureStatus");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public static String mapCombatDemo()
	{
		return "widePlain";
	}
	
	public static void setupCombatDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Player player1 = new Player(1, "Axen", 320);
		Player player2 = new Player(2, "Emma", 200);
		Player player3 = new Player(3, "Nguyen", 40);
		Player player4 = new Player(4, "Frost", 160);
		Player player5 = new Player(5, "Brook", 270);
		Player player6 = new Player(6, "Kraft", 95);
		
		game.addPlayer(player1);
		game.addPlayer(player2);
		game.addPlayer(player3);
		game.addPlayer(player4);
		game.addPlayer(player5);
		game.addPlayer(player6);
		
		Position destination = new Position(22, 13);
		
		for (int x = 1; x <= 7; ++x)
		for (int y = 2; y <= 5; ++y)
		{
			Unit tankMW = factory.newUnit("pMicrowaveLynx", player1);
			map.addUnit(tankMW, new Position(x, y));
			tankMW.assignNow(new SteerTask(destination));
		}
		
		for (int x = 22; x <= 28; ++x)
		for (int y = 2;  y <= 5;  ++y)
		{
			Unit tankRPG = factory.newUnit("pRPGLynx", player4);
			map.addUnit(tankRPG, new Position(x, y));
			tankRPG.assignNow(new SteerTask(destination));
		}
		
		for (int x = 2;  x <= 7;  ++x)
		for (int y = 18; y <= 18; ++y)
		{
			Unit tankSN = factory.newUnit("pSupernovaLynx", player3);
			map.addUnit(tankSN, new Position(x, y));
			tankSN.assignNow(new SteerTask(destination));
		}
		
		for (int x = 18; x <= 23; ++x)
		for (int y = 9;  y <= 11; ++y)
		{
			Unit tankL = factory.newUnit("eLaserLynx", player2);
			map.addUnit(tankL, new Position(x, y));
		}
		
		for (int x = 18; x <= 20; ++x)
		for (int y = 12; y <= 14; ++y)
		{
			Unit tankRG = factory.newUnit("eRailGunLynx", player2);
			map.addUnit(tankRG, new Position(x, y));
		}
		
		for (int x = 21; x <= 23; ++x)
		for (int y = 13; y <= 14; ++y)
		{
			Unit tankRG = factory.newUnit("eRailGunLynx", player2);
			map.addUnit(tankRG, new Position(x, y));
		}
		
		map.addUnit(factory.newUnit("eAcidCloudLynx", player2), new Position(20, 8));
		map.addUnit(factory.newUnit("eAcidCloudLynx", player2), new Position(21, 8));
		map.addUnit(factory.newUnit("eAcidCloudLynx", player2), new Position(22, 8));
		
		map.addUnit(factory.newUnit("pMicrowaveGuardPost", player2), new Position(21, 12));
		map.addUnit(factory.newUnit("eCommandCenter",      player2), new Position(25, 15));
		map.addUnit(factory.newUnit("eStructureFactory",   player2), new Position(19, 15));
		map.addUnit(factory.newUnit("eVehicleFactory",     player2), new Position(25, 10));
		
		map.putTube(new Position(27, 14));
		map.putTube(new Position(24, 16));
		
		map.putWall(new Position(17, 19));
		map.putWall(new Position(17, 18));
		map.putWall(new Position(17, 17));
		map.putWall(new Position(17, 16));
		map.putWall(new Position(17, 9));
		map.putWall(new Position(17, 8));
		map.putWall(new Position(18, 8));
		map.putWall(new Position(23, 8));
		map.putWall(new Position(24, 8));
		map.putWall(new Position(25, 8));
		map.putWall(new Position(26, 8));
		map.putWall(new Position(27, 8));
		
		SpriteLibrary lib = game.getSpriteLibrary();
		
		try
		{
			lib.loadModule("eLynxChassis");
			lib.loadModule("pLynxChassis");
			lib.loadModule("eLaserSingleTurret");
			lib.loadModule("eRailGunSingleTurret");
			lib.loadModule("eAcidCloudSingleTurret");
			lib.loadModule("pMicrowaveSingleTurret");
			lib.loadModule("pRPGSingleTurret");
			lib.loadModule("pSupernovaSingleTurret");
			lib.loadModule("pMicrowaveGuardPost");
			lib.loadModule("eStructureFactory");
			lib.loadModule("eVehicleFactory");
			lib.loadModule("eCommonSmelter");
			lib.loadModule("aDeath");
			lib.loadModule("aRocket");
			lib.loadModule("aAcidCloud");
			lib.loadModule("aSupernovaExplosion");
			lib.loadModule("aStructureStatus");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
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
