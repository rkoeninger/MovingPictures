package com.robbix.mp5;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.CommandButton;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.DisplayPanelView;
import com.robbix.mp5.ui.PlayerStatus;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.TitleBar;
import com.robbix.mp5.ui.UnitStatus;
import com.robbix.mp5.ui.overlay.BuildStructureOverlay;
import com.robbix.mp5.ui.overlay.PlaceBulldozeOverlay;
import com.robbix.mp5.ui.overlay.PlaceGeyserOverlay;
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
		{
			tileSetName = DEFAULT_TILE_SET;
		}
		
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
		
		Utils.trySystemLookAndFeel();
		
		/*-------------------------------------------------------------------*
		 * Load map, units, sprites, cursors
		 * Create players
		 * Create engine
		 * Init Mediator
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
		
		TitleBar titleBar = new TitleBar()
		{
			public void showFrameNumber(int frameNumber, double frameRate)
			{
				frame.setTitle(String.format(
					"Moving Pictures - [%1$d] %2$.2f fps",
					frameNumber,
					frameRate
				));
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
			if (!iconDir.isDirectory())
				continue;
			
			ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>();
			
			for (File iconFile : iconDir.listFiles(Utils.BMP))
			{
				icons.add(new ImageIcon(Utils.shrink(ImageIO.read(iconFile))));
			}
			
			if (icons.isEmpty())
				continue;
			
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
		panel.setTitleBar(titleBar);
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
		final JMenuItem scrollBarsMenuItem = new JMenuItem("Scroll Bars");
		final JMenu disastersMenu = new JMenu("Disasters");
		final JMenuItem spawnMeteorMenuItem = new JMenuItem("Spawn Meteor");
		final JMenuItem meteorShowerMenuItem = new JMenuItem("Meteor Shower");
		final JMenuItem placeGeyserMenuItem = new JMenuItem("Place Geyser");
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
			{
				panelOptionMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_G,
					KeyEvent.ALT_DOWN_MASK
				));
			}
			
			panelOptionMenuItem.addActionListener(displayOptionListener);
		}
		
		displayMenu.add(scrollBarsMenuItem);
		
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
		terrainMenu.add(placeGeyserMenuItem);
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
		
		Rectangle windowBounds = Utils.getWindowBounds();
		
		frame = new JFrame("Moving Pictures");
		frame.setJMenuBar(menuBar);
		frame.setLayout(new BorderLayout());
//		frame.add(commandBar, BorderLayout.EAST);
		frame.add(game.getView(), BorderLayout.CENTER);
		frame.add(statusesPanel, BorderLayout.SOUTH);
//		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImages(Arrays.asList(smallIcon, mediumIcon));
		frame.pack();
		frame.setSize(
			Math.min(frame.getWidth(),  windowBounds.width),
			Math.min(frame.getHeight(), windowBounds.height)
		);
		frame.setLocation(
			windowBounds.x + (windowBounds.width  - frame.getWidth())  / 2,
			windowBounds.y + (windowBounds.height - frame.getHeight()) / 2
		);
		
		scrollBarsMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DisplayPanelView view = game.getView();
				view.showScrollbars(! view.areScrollBarsVisible());
			}
		});
		
		meteorShowerMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				game.getTriggers().add(new MeteorShowerTrigger(1, 300));
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
		
		placeGeyserMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				panel.pushOverlay(new PlaceGeyserOverlay());
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
		 * Setup demo
		 * Start mechanics
		 */
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
		
		engine.play();
		frame.setVisible(true);
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
			map.putUnit(factory.newUnit("pScout"), new Position(x, y));
		}
		
		for (int x = 12; x < 30; x += 2)
		for (int y = 13; y < 19; y += 2)
		{
			map.putUnit(factory.newUnit("pResidence"), new Position(x, y));
		}
		
		map.putUnit(factory.newUnit("eVehicleFactory"), new Position(12, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"), new Position(16, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"), new Position(20, 1));
		map.putUnit(factory.newUnit("eVehicleFactory"), new Position(24, 1));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(12, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(16, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(20, 5));
		map.putUnit(factory.newUnit("eStructureFactory"), new Position(24, 5));
		map.putUnit(factory.newUnit("eCommonSmelter"), new Position(12, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"), new Position(16, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"), new Position(20, 9));
		map.putUnit(factory.newUnit("eCommonSmelter"), new Position(24, 9));
		
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
		factory.setDefaultOwner(player1);
		
		player1.addResource(ResourceType.COMMON_ORE, 50000);
		player1.addResource(ResourceType.RARE_ORE,   50000);
		player1.addResource(ResourceType.FOOD,       50000);
		
		Unit convec1     = factory.newUnit("eConVec");
		Unit convec2     = factory.newUnit("eConVec");
		Unit convec3     = factory.newUnit("eConVec");
		Unit convec4     = factory.newUnit("eConVec");
		Unit earthworker = factory.newUnit("eEarthworker");
		
		convec1.setCargo(Cargo.newConVecCargo("eVehicleFactory"));
		convec2.setCargo(Cargo.newConVecCargo("eStructureFactory"));
		convec3.setCargo(Cargo.newConVecCargo("eCommonSmelter"));
		convec4.setCargo(Cargo.newConVecCargo("eCommandCenter"));
		
		map.putUnit(convec1,     new Position(9,  7));
		map.putUnit(convec2,     new Position(10, 7));
		map.putUnit(convec3,     new Position(11, 7));
		map.putUnit(convec4,     new Position(12, 7));
		map.putUnit(earthworker, new Position(10, 9));
	}
	
	public static String mapMineRouteDemo()
	{
		return "bigPlain";
	}
	
	public static void setupMineRouteDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Player player1 = new Player(1, "Mining Operation", 200);
		game.addPlayer(player1);
		selectPlayer(1);
		
		List<Unit> mines    = new ArrayList<Unit>();
		List<Unit> smelters = new ArrayList<Unit>();
		
		for (int x = 2; x < 44; x += 6)
		for (int y = 2; y < 20; y += 6)
		{
			Unit smelter = factory.newUnit("eCommonSmelter", player1);
			map.putUnit(smelter, new Position(x, y));
			map.putTube(new Position(x + 5, y + 1));
			map.putTube(new Position(x + 2, y + 4));
			map.putTube(new Position(x + 2, y + 5));
			smelters.add(smelter);
		}
		
		map.putUnit(factory.newUnit("eCommandCenter", player1), new Position(44, 2));
		
		for (int x = 2;  x < 46; x += 4)
		for (int y = 38; y < 45; y += 4)
		{
			ResourceDeposit res = ResourceDeposit.get2BarCommon();
			map.putResourceDeposit(res, new Position(x + 1, y));
			Unit mine = factory.newUnit("eCommonMine", player1);
			map.putUnit(mine, new Position(x, y));
			mines.add(mine);
		}
		
		Unit truck;
		int truckIndex = 0;
		
		Collections.shuffle(mines);
		Collections.shuffle(smelters);
		
		for (Unit mine : mines)
		for (Unit smelter : smelters)
		{
			if (Utils.randInt(0, 5) % 6 == 0)
			{
				Position pos = new Position(
					truckIndex % 42 + 2,
					truckIndex / 42 + 24
				);
				
				truck = factory.newUnit("eCargoTruck", player1);
				truck.assignNow(new MineRouteTask(mine, smelter));
				map.putUnit(truck, pos);
			}
			
			truckIndex++;
		}
		
		SpriteLibrary lib = game.getSpriteLibrary();
		
		try
		{
			lib.loadModule("eCargoTruck");
			lib.loadModule("eCommandCenter");
			lib.loadModule("eCommonSmelter");
			lib.loadModule("eCommonMine");
			lib.loadModule("aResource");
			lib.loadModule("aStructureStatus");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		game.getView().center(new Position(24, 26));
	}
	
	public static String mapCombatDemo()
	{
		return "bigPlain";
	}
	
	public static void setupCombatDemo(Game game)
	{
		LayeredMap map = game.getMap();
		UnitFactory factory = game.getUnitFactory();
		
		Position center = new Position(
			map.getWidth() / 2,
			map.getHeight() / 2
		);
		
		Player player1 = new Player(1, "Axen", 320);
		Player player2 = new Player(2, "Emma", 200);
		Player player3 = new Player(3, "Nguyen", 40);
		Player player4 = new Player(4, "Frost", 160);
		Player player5 = new Player(5, "Brook", 95);
		
		game.addPlayer(player1);
		game.addPlayer(player2);
		game.addPlayer(player3);
		game.addPlayer(player4);
		game.addPlayer(player5);
		
		for (int x = 1; x <= 15; ++x)
		for (int y = 1; y <= 11; ++y)
		{
			Unit tank = factory.newUnit("pMicrowaveLynx", player1);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}
		
		for (int x = 36; x <= 46; ++x)
		for (int y = 1;  y <= 15;  ++y)
		{
			Unit tank = factory.newUnit("eLaserLynx", player2);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}

		for (int x = 1;  x <= 11;  ++x)
		for (int y = 31; y <= 46; ++y)
		{
			Unit tank = factory.newUnit("pRPGLynx", player3);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}

		for (int x = 31; x <= 46; ++x)
		for (int y = 36; y <= 46; ++y)
		{
			Unit tank = factory.newUnit("eRailGunLynx", player4);
			map.putUnit(tank, new Position(x, y));
			tank.assignNow(new SteerTask(center));
		}
		
		for (int x = 20; x <= 29; ++x)
		for (int y = 20; y <= 29; ++y)
		{
			Unit tank = factory.newUnit("eAcidCloudLynx", player5);
			map.putUnit(tank, new Position(x, y));
		}
		
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
		
		game.getView().center(center);
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
