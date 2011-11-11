package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.robbix.mp5.Engine;
import com.robbix.mp5.Game;
import com.robbix.mp5.GameListener;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.MeteorShowerTrigger;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.sb.demo.Demo;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.PlayerStatus;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.TitleBar;
import com.robbix.mp5.ui.UnitStatus;
import com.robbix.mp5.ui.overlay.PlaceBulldozeOverlay;
import com.robbix.mp5.ui.overlay.PlaceFixtureOverlay;
import com.robbix.mp5.ui.overlay.PlaceResourceOverlay;
import com.robbix.mp5.ui.overlay.PlaceUnitOverlay;
import com.robbix.mp5.ui.overlay.SelectUnitOverlay;
import com.robbix.mp5.ui.overlay.SpawnMeteorOverlay;
import com.robbix.mp5.unit.Command;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;
import com.robbix.mp5.utils.AnimatedButton;
import com.robbix.mp5.utils.JListDialog;
import com.robbix.mp5.utils.JSliderMenuItem;
import com.robbix.mp5.utils.Utils;

/**
 * Sandbox test suite, main window.
 * 
 * @author bort
 */
public class Sandbox extends JApplet
{
	private static final long serialVersionUID = 1L;
	
	public void init()
	{
		try
		{
			String resDirPath = getParameter("resDir");
			if (resDirPath == null) resDirPath = "./../res";
			main(new String[]{"-resDir:" + resDirPath});
			frame.setVisible(false);
			this.setLayout(new BorderLayout());
			this.setJMenuBar(frame.getJMenuBar());
			this.add(panel, BorderLayout.CENTER);
			this.add(commandBar, BorderLayout.NORTH);
		}
		catch (Exception exc)
		{
			StringWriter excString = new StringWriter();
			exc.printStackTrace(new PrintWriter(excString));
			add(new JScrollPane(new JTextArea(excString.toString())));
		}
	}
	
	private static File resDir = new File("./res");
	
	private static Player currentPlayer;
	private static Game game;
	private static Engine engine;
	private static JFrame frame;
	private static Window fsWindow;
	private static DisplayPanel panel;
	private static JToolBar commandBar;
	private static Map<Integer, JMenuItem> playerMenuItems = new HashMap<Integer, JMenuItem>();
	private static Map<Command, AnimatedButton> commandButtons =
		new EnumMap<Command, AnimatedButton>(Command.class);
	
	private static JFrame slViewer, utViewer, sbPlayer;
	
	// References moved here so they can be called by addPlayer(int)
	private static JMenu playerMenu;
	private static ButtonGroup playerSelectButtonGroup;
	private static boolean showFrameRate = false;
	
	// Action command prefixes
	private static final String ACP_SELECT_PLAYER  = "selectPlayer-";
	private static final String ACP_ADD_UNIT       = "addUnit-";
	private static final String ACP_COMMAND_BUTTON = "commandButton-";
	
	private static Sprite edenIconSprite;
	private static Sprite plymouthIconSprite;
	private static ImageIcon neutralIcon;
	
	private static ActionListener listener = new MenuItemListener();
	
	private static JSliderMenuItem volumeSliderMenuItem;
	private static JSliderMenuItem throttleSliderMenuItem;
	private static JMenuItem pauseMenuItem;
	private static JMenuItem stepMenuItem;
	private static JMenuItem spriteLibMenuItem;
	private static JMenuItem unitLibMenuItem;
	private static JMenuItem soundPlayerMenuItem;
	private static JMenuItem exitMenuItem;
	private static JMenuItem showGridMenuItem;
	private static JMenuItem showShadowsMenuItem;
	private static JMenuItem showCostMapMenuItem;
	private static JMenuItem scrollBarsMenuItem;
	private static JMenuItem frameRateMenuItem;
	private static JMenuItem scrollSpeedMenuItem;
	private static JMenuItem addDisplayMenuItem;
	private static JMenuItem splitDisplayMenuItem;
	private static JMenuItem fullScreenMenuItem;
	private static JMenuItem spawnMeteorMenuItem;
	private static JMenuItem meteorShowerMenuItem;
	private static JMenuItem placeGeyserMenuItem;
	private static JMenuItem placeWallMenuItem;
	private static JMenuItem placeTubeMenuItem;
	private static JMenuItem placeBulldozeMenuItem;
	private static JMenuItem placeCommon1;
	private static JMenuItem placeCommon2;
	private static JMenuItem placeCommon3;
	private static JMenuItem placeRare1;
	private static JMenuItem placeRare2;
	private static JMenuItem placeRare3;
	private static JMenuItem playSoundMenuItem;
	private static JMenuItem playMusicMenuItem;
	private static JMenuItem setAudioFormatMenuItem;
	private static JMenuItem removeAllUnitsMenuItem;
	private static JMenuItem addPlayerMenuItem;
	private static JMenuItem aboutMenuItem;
	
	public static void main(String[] args) throws IOException
	{
		/*-------------------------------------------------------------------------------------[*]
		 * Parse command-line arguments
		 */
		boolean lazyLoadSprites  = true;
		boolean lazyLoadSounds   = true;
		boolean asyncLoadSprites = true;
		boolean soundOn          = false;
//		boolean musicOn          = false;
		String demoName          = null;
		String mapName           = "16-16-plain";
		String tileSetName       = "newTerraDirt";
		
		for (String arg : args)
		{
			int colonIndex = Math.max(arg.indexOf(':'), 0);
			String option = arg.substring(colonIndex + 1);
			
			if      (arg.equals("-lazyLoadSprites"))  lazyLoadSprites  = true;
			else if (arg.equals("-lazyLoadSounds"))   lazyLoadSounds   = true;
			else if (arg.equals("-asyncLoadSprites")) asyncLoadSprites = true;
			else if (arg.equals("-syncLoadSprites"))  asyncLoadSprites = false;
			else if (arg.equals("-soundOn"))          soundOn          = true;
			else if (arg.equals("-soundOff"))         soundOn          = false;
//			else if (arg.equals("-musicOn"))          musicOn          = true;
//			else if (arg.equals("-musicOff"))         musicOn          = false;
			else if (arg.startsWith("-demo:"))        demoName         = option;
			else if (arg.startsWith("-map:"))         mapName          = option;
			else if (arg.startsWith("-tileSet:"))     tileSetName      = option;
			else if (arg.startsWith("-resDir:"))      resDir           = new File(option);
		}
		
		Demo demo = null;
		
		if (demoName != null)
		{
			Map<String, Demo> demos = Demo.getDemos();
			
			if (!demos.containsKey(demoName))
				throw new IllegalArgumentException("Demo does not exist");
			
			demo = demos.get(demoName);
			mapName = demo.getMapName();
		}
		
		Sandbox.trySystemLookAndFeel();
		
		/*-------------------------------------------------------------------------------------[*]
		 * Load map, units, sprites, cursors
		 * Create players
		 * Create engine
		 * Init Mediator
		 */
		game = Game.load(resDir, mapName, tileSetName, lazyLoadSprites, lazyLoadSounds);
		game.getSpriteLibrary().setAsyncModeEnabled(asyncLoadSprites);
		engine = new Engine(game);
		Mediator.initMediator(game);
		Mediator.soundOn(soundOn);
		currentPlayer = game.getDefaultPlayer();
		game.getSoundBank().setVolume(0.5f);
		
		/*-------------------------------------------------------------------------------------[*]
		 * Prepare live-updating status labels
		 */
		UnitStatusLabel unitStatusLabel = new UnitStatusLabel();
		PlayerStatusLabel playerStatusLabel = new PlayerStatusLabel();
		
		TitleBar titleBar = new TitleBar()
		{
			public void showFrameNumber(int frameNumber, double frameRate)
			{
				if (showFrameRate)
				{
					frame.setTitle(String.format(
						"Moving Pictures - [%1$d] %2$.2f fps",
						frameNumber,
						frameRate
					));
				}
			}
		};
		
		/*-------------------------------------------------------------------------------------[*]
		 * Build UI
		 */
		panel = game.getDisplay();
		panel.setUnitStatus(unitStatusLabel);
		panel.setPlayerStatus(playerStatusLabel);
		panel.setTitleBar(titleBar);
		panel.pushOverlay(new SelectUnitOverlay());
		panel.showStatus(currentPlayer);
		
		throttleSliderMenuItem = new JSliderMenuItem(0, 200, 45);
		Hashtable<Integer, JLabel> paintLabels = new Hashtable<Integer, JLabel>();
		paintLabels.put(0, new JLabel("Unthrottled"));
		paintLabels.put(throttleSliderMenuItem.getMaximum(), new JLabel("Crawl"));
		throttleSliderMenuItem.setLabelTable(paintLabels);
		throttleSliderMenuItem.setPaintLabels(true);
		throttleSliderMenuItem.setPreferredSize(new Dimension(10, 100));
		volumeSliderMenuItem = new JSliderMenuItem(0, 100, 50);
		volumeSliderMenuItem.setPreferredSize(new Dimension(10, 100));
		pauseMenuItem          = new JMenuItem("Pause");
		stepMenuItem           = new JMenuItem("Step Once");
		spriteLibMenuItem      = new JMenuItem("Sprite Library");
		unitLibMenuItem        = new JMenuItem("Unit Library");
		soundPlayerMenuItem    = new JMenuItem("Sound Player");
		exitMenuItem           = new JMenuItem("Exit");
		showGridMenuItem       = new JMenuItem(panel.isShowingGrid() ? "Hide Grid" : "Show Grid");
		showShadowsMenuItem    = new JMenuItem(panel.isShowingShadows() ? "Hide Shadows" : "Show Shadows");
		showCostMapMenuItem    = new JMenuItem(panel.isShowingCostMap() ? "Show Surface" : "Show Cost Map");
		scrollBarsMenuItem     = new JMenuItem("Scroll Bars");
		frameRateMenuItem      = new JMenuItem("Frame Rate");
		scrollSpeedMenuItem    = new JMenuItem("Scroll Speed");
		addDisplayMenuItem     = new JMenuItem("Add Display");
		splitDisplayMenuItem   = new JMenuItem("Split Display");
		fullScreenMenuItem     = new JMenuItem("Full Screen");
		spawnMeteorMenuItem    = new JMenuItem("Spawn Meteor");
		meteorShowerMenuItem   = new JMenuItem("Meteor Shower");
		placeGeyserMenuItem    = new JMenuItem("Place Geyser");
		placeWallMenuItem      = new JMenuItem("Place Wall");
		placeTubeMenuItem      = new JMenuItem("Place Tube");
		placeBulldozeMenuItem  = new JMenuItem("Bulldoze");
		placeCommon1           = new JMenuItem("Common Low");
		placeCommon2           = new JMenuItem("Common Med");
		placeCommon3           = new JMenuItem("Common High");
		placeRare1             = new JMenuItem("Rare Low");
		placeRare2             = new JMenuItem("Rare Med");
		placeRare3             = new JMenuItem("Rare High");
		playSoundMenuItem      = new JCheckBoxMenuItem("Play Sounds");
		playMusicMenuItem      = new JCheckBoxMenuItem("Play Music");
		setAudioFormatMenuItem = new JMenuItem("Set Format...");
		removeAllUnitsMenuItem = new JMenuItem("Remove All");
		addPlayerMenuItem      = new JMenuItem("Add Player...");
		aboutMenuItem          = new JMenuItem("About");
		
		final JMenuBar menuBar        = new JMenuBar();
		final JMenu engineMenu        = new JMenu("Engine");
		final JMenu displayMenu       = new JMenu("Display");
		final JMenu terrainMenu       = new JMenu("Terrain");
		final JMenu unitMenu          = new JMenu("Units");
		final JMenu disastersMenu     = new JMenu("Disasters");
		final JMenu placeResourceMenu = new JMenu("Add Ore");
		final JMenu soundMenu         = new JMenu("Sound");
		final JMenu addUnitMenu       = new JMenu("Add");
		final JMenu helpMenu          = new JMenu("Help");
		stepMenuItem.setEnabled(false);
		throttleSliderMenuItem.setValue(engine.getDelay());
		playerMenu = new JMenu("Players");
		playSoundMenuItem.setSelected(game.getSoundBank().isRunning());
		playerMenu.add(addPlayerMenuItem);
		playerMenu.addSeparator();
		
		/*-------------------------------------------------------------------------------------[*]
		 * Add player select menu items
		 */
		playerSelectButtonGroup = new ButtonGroup();
		
		for (Player player : game.getPlayers())
		{
			final JMenuItem playerSelectMenuItem =
				new JRadioButtonMenuItem(player.getName());
			playerSelectButtonGroup.add(playerSelectMenuItem);
			
			if (currentPlayer.equals(player))
				playerSelectMenuItem.setSelected(true);
			
			playerSelectMenuItem.setActionCommand(
				ACP_SELECT_PLAYER + String.valueOf(player.getID())
			);
			playerMenu.add(playerSelectMenuItem);
			playerSelectMenuItem.addActionListener(listener);
			playerMenuItems.put(player.getID(), playerSelectMenuItem);
		}
		
		game.addGameListener(new SandboxListener());
		
		/*-------------------------------------------------------------------------------------[*]
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
		
		for (int i = 0; i < types.size(); ++i)
		{
			final JMenuItem addUnitMenuItem = new JMenuItem(names.get(i));
			addUnitMenu.add(addUnitMenuItem);
			addUnitMenuItem.setActionCommand(ACP_ADD_UNIT + types.get(i));
			addUnitMenuItem.addActionListener(listener);
		}
		
		throttleSliderMenuItem.addChangeListener((ChangeListener) listener);
		volumeSliderMenuItem  .addChangeListener((ChangeListener) listener);
		spriteLibMenuItem     .addActionListener(listener);
		unitLibMenuItem       .addActionListener(listener);
		soundPlayerMenuItem   .addActionListener(listener);
		showGridMenuItem      .addActionListener(listener);
		showShadowsMenuItem   .addActionListener(listener);
		showCostMapMenuItem   .addActionListener(listener);
		scrollSpeedMenuItem   .addActionListener(listener);
		scrollBarsMenuItem    .addActionListener(listener);
		frameRateMenuItem     .addActionListener(listener);
		addDisplayMenuItem    .addActionListener(listener);
		splitDisplayMenuItem  .addActionListener(listener);
		fullScreenMenuItem    .addActionListener(listener);
		meteorShowerMenuItem  .addActionListener(listener);
		spawnMeteorMenuItem   .addActionListener(listener);
		playSoundMenuItem     .addActionListener(listener);
		playMusicMenuItem     .addActionListener(listener);
		setAudioFormatMenuItem.addActionListener(listener);
		placeCommon1          .addActionListener(listener);
		placeCommon2          .addActionListener(listener);
		placeCommon3          .addActionListener(listener);
		placeRare1            .addActionListener(listener);
		placeRare2            .addActionListener(listener);
		placeRare3            .addActionListener(listener);
		placeBulldozeMenuItem .addActionListener(listener);
		placeGeyserMenuItem   .addActionListener(listener);
		placeWallMenuItem     .addActionListener(listener);
		placeTubeMenuItem     .addActionListener(listener);
		removeAllUnitsMenuItem.addActionListener(listener);
		pauseMenuItem         .addActionListener(listener);
		stepMenuItem          .addActionListener(listener);
		exitMenuItem          .addActionListener(listener);
		addPlayerMenuItem     .addActionListener(listener);
		aboutMenuItem         .addActionListener(listener);
		
		pauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAUSE, 0));
		stepMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		
		engineMenu.add(pauseMenuItem);
		engineMenu.add(throttleSliderMenuItem);
		engineMenu.add(stepMenuItem);
		engineMenu.addSeparator();
		engineMenu.add(spriteLibMenuItem);
		engineMenu.add(unitLibMenuItem);
		engineMenu.add(soundPlayerMenuItem);
		engineMenu.addSeparator();
		engineMenu.add(exitMenuItem);
		displayMenu.add(showGridMenuItem);
		displayMenu.add(showShadowsMenuItem);
		displayMenu.add(showCostMapMenuItem);
//		displayMenu.add(scrollBarsMenuItem);
		displayMenu.add(frameRateMenuItem);
//		displayMenu.add(scrollSpeedMenuItem);
//		displayMenu.add(fullScreenMenuItem);
//		displayMenu.add(addDisplayMenuItem);
//		displayMenu.add(splitDisplayMenuItem);
		soundMenu.add(playSoundMenuItem);
//		soundMenu.add(playMusicMenuItem);
		soundMenu.addSeparator();
		soundMenu.add(volumeSliderMenuItem);
		soundMenu.addSeparator();
		soundMenu.add(setAudioFormatMenuItem);
		terrainMenu.add(placeWallMenuItem);
		terrainMenu.add(placeTubeMenuItem);
		terrainMenu.add(placeGeyserMenuItem);
		terrainMenu.add(placeBulldozeMenuItem);
		placeResourceMenu.add(placeCommon1);
		placeResourceMenu.add(placeCommon2);
		placeResourceMenu.add(placeCommon3);
		placeResourceMenu.add(placeRare1);
		placeResourceMenu.add(placeRare2);
		placeResourceMenu.add(placeRare3);
		terrainMenu.add(placeResourceMenu);
		disastersMenu.add(spawnMeteorMenuItem);
		disastersMenu.add(meteorShowerMenuItem);
		unitMenu.add(addUnitMenu);
		unitMenu.addSeparator();
		unitMenu.add(removeAllUnitsMenuItem);
		helpMenu.add(aboutMenuItem);
		
		menuBar.add(engineMenu);
		menuBar.add(displayMenu);
		menuBar.add(soundMenu);
		menuBar.add(terrainMenu);
		menuBar.add(disastersMenu);
		menuBar.add(playerMenu);
		menuBar.add(unitMenu);
		menuBar.add(helpMenu);
		
		JPanel statusesPanel = new JPanel();
		statusesPanel.setLayout(new GridLayout(2, 1));
		statusesPanel.add(unitStatusLabel);
		statusesPanel.add(playerStatusLabel);
		
		Image img = ImageIO.read(new File(resDir, "art/edenIcon.png"));
		edenIconSprite = new Sprite(img, 240, 0, 0);
		img = ImageIO.read(new File(resDir, "art/plymouthIcon.png"));
		plymouthIconSprite = new Sprite(img, 240, 0, 0);
		img = ImageIO.read(new File(resDir, "art/neutralIcon.png"));
		neutralIcon = new ImageIcon(img);
		
		commandBar = loadCommandBar();
		
		Rectangle windowBounds = Utils.getWindowBounds();
		
		frame = new JFrame("Moving Pictures");
		frame.setJMenuBar(menuBar);
		frame.setLayout(new BorderLayout());
		frame.add(commandBar, BorderLayout.NORTH);
		frame.add(game.getDisplay().getView(), BorderLayout.CENTER);
		frame.add(statusesPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImages(getWindowIcons());
		frame.pack();
		
		boolean maximize = frame.getWidth()  > windowBounds.width
						&& frame.getHeight() > windowBounds.height;
		
		frame.setSize(
			Math.min(frame.getWidth(),  windowBounds.width),
			Math.min(frame.getHeight(), windowBounds.height)
		);
		
		if (maximize)
		{
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		else
		{
			frame.setLocation(
				windowBounds.x + (windowBounds.width  - frame.getWidth())  / 2,
				windowBounds.y + (windowBounds.height - frame.getHeight()) / 2
			);
		}
		
		/*-------------------------------------------------------------------------------------[*]
		 * Setup demo
		 * Start mechanics
		 */
		if (demo != null)
		{
			demo.setup(game);
			selectPlayer(demo.getStartingPlayerID());
		}
		
		engine.play();
		frame.setVisible(true);
	}
	
	private static class MenuItemListener implements ActionListener, ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if (e.getSource() == throttleSliderMenuItem)
			{
				float val = throttleSliderMenuItem.getValue();
				float max = throttleSliderMenuItem.getMaximum();
				val /= max;
				val *= val;
				val *= max;
				engine.setDelay((int) val);
			}
			else if (e.getSource() == volumeSliderMenuItem)
			{
				int current = volumeSliderMenuItem.getValue();
				int max = volumeSliderMenuItem.getMaximum();
				game.getSoundBank().setVolume(current / (float) max);
			}
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == spriteLibMenuItem)
			{
				if (slViewer == null)
				{
					slViewer = new SpriteViewer(game);
					slViewer.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					
					try
					{
						slViewer.setIconImages(getWindowIcons());
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				
				slViewer.setVisible(true);
			}
			else if (e.getSource() == unitLibMenuItem)
			{
				if (utViewer == null)
				{
					utViewer = new UnitTypeViewer(game);
					utViewer.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				
					try
					{
						utViewer.setIconImages(getWindowIcons());
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				
				utViewer.setVisible(true);
			}
			else if (e.getSource() == soundPlayerMenuItem)
			{
				if (sbPlayer == null)
				{
					sbPlayer = new SoundPlayer(game);
					sbPlayer.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					
					try
					{
						sbPlayer.setIconImages(getWindowIcons());
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				
				sbPlayer.setVisible(true);
			}
			else if (e.getSource() == showGridMenuItem)
			{
				panel.setShowGrid(!panel.isShowingGrid());
				showGridMenuItem.setText(panel.isShowingGrid()
					? "Hide Grid"
					: "Show Grid"
				);
			}
			else if (e.getSource() == showShadowsMenuItem)
			{
				panel.setShowShadows(!panel.isShowingShadows());
				showShadowsMenuItem.setText(panel.isShowingShadows()
					? "Hide Shadows"
					: "Show Shadows"
				);
			}
			else if (e.getSource() == showCostMapMenuItem)
			{
				panel.setShowCostMap(!panel.isShowingCostMap());
				showCostMapMenuItem.setText(panel.isShowingCostMap()
					? "Show Surface"
					: "Show Cost Map"
				);
			}
			else if (e.getSource() == scrollSpeedMenuItem)
			{
				for (;;)
				{
					int scrollSpeed = game.getDisplay().getView().getScrollSpeed();
					
					String result = JOptionPane.showInputDialog(
						frame,
						"Scroll Speed (1+)",
						String.valueOf(scrollSpeed)
					);
					
					if (result == null)
						return;
					
					try
					{
						scrollSpeed = Integer.parseInt(result);
						game.getDisplay().getView().setScrollSpeed(scrollSpeed);
						break;
					}
					catch (NumberFormatException nfe)
					{
						continue;
					}
				}
			}
			else if (e.getSource() == scrollBarsMenuItem)
			{
				game.getDisplay().getView().showScrollBars(
					! game.getDisplay().getView().areScrollBarsVisible());
				frame.validate();
			}
			else if (e.getSource() == frameRateMenuItem)
			{
				showFrameRate = !showFrameRate;
				
				if (!showFrameRate)
				{
					frame.setTitle("Moving Pictures");
				}
			}
			else if (e.getSource() == fullScreenMenuItem)
			{
				final GraphicsDevice screen = frame.getGraphicsConfiguration().getDevice();
				
				if (screen.getFullScreenWindow() == null)
				{
					final DisplayMode prevMode = screen.getDisplayMode();
					final DisplayMode mode = screen.isDisplayChangeSupported()
						? (DisplayMode) JListDialog.showDialog(
							screen.getDisplayModes(),
							prevMode
						)
						: null;
					
					if (fsWindow == null)
					{
						final JButton closeButton = new JButton("Exit Fullscreen");
						closeButton.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								fsWindow.setVisible(false);
							}
						});
						commandBar.add(closeButton);
						
						fsWindow = new JWindow();
						fsWindow.setLayout(new BorderLayout());
						fsWindow.add(panel.getView(), BorderLayout.CENTER);
						fsWindow.add(commandBar, BorderLayout.NORTH);
						
						fsWindow.addWindowListener(new WindowAdapter()
						{
							public void windowClosed(WindowEvent e)
							{
								if (mode != null)
									screen.setDisplayMode(prevMode);
								
								commandBar.remove(closeButton);
								frame.add(panel.getView(), BorderLayout.CENTER);
								frame.add(commandBar, BorderLayout.NORTH);
							}
						});
					}
					
					if (mode != null)
						screen.setDisplayMode(mode);
					
					screen.setFullScreenWindow(fsWindow);
				}
				else if (screen.getFullScreenWindow() == fsWindow)
				{
					screen.setFullScreenWindow(null);
					fsWindow = null;
				}
			}
			else if (e.getSource() == addDisplayMenuItem)
			{
				JFrame frame2 = new JFrame("Moving Pictures - Additional View");
				final DisplayPanel panel2 = new DisplayPanel(
					game.getMap(),
					game.getSpriteLibrary(),
					game.getTileSet(),
					game.getCursorSet()
				);
				game.addDisplay(panel2);
				panel2.pushOverlay(new SelectUnitOverlay());
				panel2.showStatus(currentPlayer);
				frame2.add(panel2.getView());
				frame2.setSize(500, 500);
				frame2.setVisible(true);
				frame2.addWindowListener(new WindowAdapter()
				{
					public void windowClosed(WindowEvent e)
					{
						game.removeDisplay(panel2);
					}
				});
			}
			else if (e.getSource() == splitDisplayMenuItem)
			{
				engine.pause();
				final DisplayPanel panel2 = new DisplayPanel(panel);
				game.addDisplay(panel2);
				frame.remove(panel.getView());
				JSplitPane splitPane = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT,
					false,
					panel.getView(),
					panel2.getView()
				);
				splitPane.setResizeWeight(0.5);
				frame.add(splitPane, BorderLayout.CENTER);
				frame.validate();
				splitPane.setDividerLocation(splitPane.getWidth() / 2);
				engine.pause();
			}
			else if (e.getSource() == meteorShowerMenuItem)
			{
				game.getTriggers().add(new MeteorShowerTrigger(1, 300));
				Mediator.playSound("savant_meteorApproaching");
			}
			else if (e.getSource() == spawnMeteorMenuItem)
			{
				panel.pushOverlay(new SpawnMeteorOverlay());
			}
			else if (e.getSource() == playSoundMenuItem)
			{
				Mediator.soundOn(playSoundMenuItem.isSelected());
			}
			else if (e.getSource() == playMusicMenuItem)
			{
				if (playMusicMenuItem.isSelected())
				{
					
				}
				else
				{
					
				}
			}
			else if (e.getSource() == setAudioFormatMenuItem)
			{
				AudioFormat format = game.getSoundBank().getFormat();
				
				for (;;)
				{
					format = AudioFormatDialog.showDialog(frame, format);
					
					if (format == null)
						return;
					
					Line.Info info = new DataLine.Info(SourceDataLine.class, format);
					
					if (! AudioSystem.isLineSupported(info))
						continue;
					
					game.getSoundBank().setFormat(format);
					return;
				}
			}
			else if (e.getSource() == placeCommon1)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get1BarCommon()));
			}
			else if (e.getSource() == placeCommon2)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get2BarCommon()));
			}
			else if (e.getSource() == placeCommon3)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get3BarCommon()));
			}
			else if (e.getSource() == placeRare1)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get1BarRare()));
			}
			else if (e.getSource() == placeRare2)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get2BarRare()));
			}
			else if (e.getSource() == placeRare3)
			{
				panel.pushOverlay(new PlaceResourceOverlay(ResourceDeposit.get3BarRare()));
			}
			else if (e.getSource() == placeBulldozeMenuItem)
			{
				panel.pushOverlay(new PlaceBulldozeOverlay());
			}
			else if (e.getSource() == placeGeyserMenuItem)
			{
				panel.pushOverlay(new PlaceFixtureOverlay(LayeredMap.Fixture.GEYSER));
			}
			else if (e.getSource() == placeWallMenuItem)
			{
				panel.pushOverlay(new PlaceFixtureOverlay(LayeredMap.Fixture.WALL));
			}
			else if (e.getSource() == placeTubeMenuItem)
			{
				panel.pushOverlay(new PlaceFixtureOverlay(LayeredMap.Fixture.TUBE));
			}
			else if (e.getSource() == pauseMenuItem)
			{
				engine.pause();
				stepMenuItem.setEnabled(!engine.isRunning());
				pauseMenuItem.setText(
					engine.isRunning()
					? "Pause"
					: "Resume"
				);
			}
			else if (e.getSource() == stepMenuItem)
			{
				engine.step();
			}
			else if (e.getSource() == exitMenuItem)
			{
				System.exit(0);
			}
			else if (e.getSource() == removeAllUnitsMenuItem)
			{
				int result = JOptionPane.showConfirmDialog(
					frame,
					"Are you sure?",
					"Remove All Units",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				
				if (result != JOptionPane.YES_OPTION)
					return;
				
				game.getMap().clearAllUnits();
			}
			else if (e.getSource() == addPlayerMenuItem)
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
				selectPlayer(newPlayer.getID());
			}
			else if (e.getSource() == aboutMenuItem)
			{
				AboutDialog.showDialog(frame, true);
			}
			else if (e.getActionCommand().startsWith(ACP_SELECT_PLAYER))
			{
				selectPlayer(Integer.valueOf(e.getActionCommand().substring(ACP_SELECT_PLAYER.length())));
			}
			else if (e.getActionCommand().startsWith(ACP_ADD_UNIT))
			{
				game.getUnitFactory().setDefaultOwner(currentPlayer);
				panel.pushOverlay(new PlaceUnitOverlay(
					game.getUnitFactory(),
					 e.getActionCommand().substring(ACP_ADD_UNIT.length())
				));
			}
			else if (e.getActionCommand().startsWith(ACP_COMMAND_BUTTON))
			{
				String cmd = e.getActionCommand();
				cmd = cmd.substring(ACP_COMMAND_BUTTON.length());
				game.getDisplay().fireCommandButton(Command.valueOf(cmd));
			}
		}
	}
	
	private static class UnitStatusLabel extends JLabel
	implements UnitStatus, ActionListener
	{
		private static final long serialVersionUID = 1L;
		Unit myUnit = null;
		Timer timer;
		
		public UnitStatusLabel()
		{
			setPreferredSize(new Dimension(100, 20));
			timer = new Timer(100, this);
			timer.start();
		}
		
		public synchronized void actionPerformed(ActionEvent e)
		{
			setText(myUnit != null ? myUnit.getStatusString() : "");
		}
		
		public synchronized void showStatus(Unit unit)
		{
			myUnit = unit;
			showCommandButtons(unit);
		}
	}
	
	private static class PlayerStatusLabel extends JLabel
	implements PlayerStatus, ActionListener
	{
		private static final long serialVersionUID = 1L;
		Player myPlayer = null;
		Timer timer;
		
		public PlayerStatusLabel()
		{
			setPreferredSize(new Dimension(100, 20));
			timer = new Timer(100, this);
			timer.start();
		}
		
		public synchronized void actionPerformed(ActionEvent e)
		{
			setText(myPlayer != null ? myPlayer.getStatusString() : "");
		}
		
		public synchronized void showStatus(Player player)
		{
			Player oldMyPlayer = myPlayer;
			myPlayer = player;
			
			if (player != null && !player.equals(oldMyPlayer))
				selectPlayer(player.getID());
		}
	}
	
	// Call after Game.addPlayer() to sync with sandbox UI
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
	
	private static class SandboxListener implements GameListener
	{
		public void playerAdded(Player player)
		{
			String name = player.getName();
			JMenuItem playerSelectMenuItem = new JRadioButtonMenuItem(name);
			playerSelectButtonGroup.add(playerSelectMenuItem);
			playerSelectMenuItem.setActionCommand(String.valueOf(player.getID()));
			playerMenu.add(playerSelectMenuItem);
			playerSelectMenuItem.addActionListener(listener);
			playerSelectMenuItem.setSelected(true);
			playerMenuItems.put(player.getID(), playerSelectMenuItem);
		}
	}
	
	private static JToolBar loadCommandBar() throws IOException
	{
		JToolBar commandBar = new JToolBar();
		commandBar.setOrientation(SwingConstants.HORIZONTAL);
		commandBar.setFloatable(false);
		commandBar.setRollover(true);
		
		File[] iconDirs = new File(resDir, "art/commandButtons").listFiles();
		Arrays.sort(iconDirs, Utils.FILENAME);
		
		for (File iconDir : iconDirs)
		{
			if (!iconDir.isDirectory())
				continue;
			
			ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>();
			
			File[] iconFiles = iconDir.listFiles(Utils.BMP);
			Arrays.sort(iconFiles, Utils.FILENAME);
			
			for (File iconFile : iconFiles)
			{
				icons.add(new ImageIcon(Utils.shrink(ImageIO.read(iconFile))));
			}
			
			if (icons.isEmpty())
				continue;
			
			String command = Utils.camelCaseToAllCaps(iconDir.getName());
			AnimatedButton button = new AnimatedButton(command, icons);
			button.setToolTipText(iconDir.getName());
			button.setActionCommand(ACP_COMMAND_BUTTON + command);
			button.addActionListener(listener);
			button.setFocusable(false);
			commandButtons.put(Command.valueOf(command), button);
		}
		
		String[] extraCommands = {"KILL", "IDLE", "BUILD", "DOCK", "MINE"};
		
		for (String command : extraCommands)
		{
			AnimatedButton button = new AnimatedButton(command);
			button.setActionCommand(ACP_COMMAND_BUTTON + command);
			button.addActionListener(listener);
			button.setFocusable(false);
			commandButtons.put(Command.valueOf(command), button);
		}
		
		commandBar.add(new JLabel(neutralIcon));
		return commandBar;
	}
	
	private static void showCommandButtons(Unit unit)
	{
		commandBar.removeAll();
		
		if (unit == null)
		{
			commandBar.add(new JLabel(neutralIcon));
			commandBar.repaint();
			frame.validate();
			return;
		}
		
		UnitType type = unit.getType();
		Sprite sprite = "Eden".equals(type.getCiv()) ? edenIconSprite : plymouthIconSprite;
		commandBar.add(new JLabel(new ImageIcon(sprite.getImage(unit.getOwner().getColorHue()))));
		ArrayList<AnimatedButton> buttons = new ArrayList<AnimatedButton>();
		
		for (Command cmd : type.getCommands())
			buttons.add(commandButtons.get(cmd));
		
		AnimatedButton[] buttonArray = buttons.toArray(new AnimatedButton[0]);
		Arrays.sort(buttonArray, new AnimatedButtonComparator());
		
		for (AnimatedButton button : buttonArray)
			commandBar.add(button);
		
		commandBar.repaint();
		frame.validate();
	}
	
	private static class AnimatedButtonComparator implements Comparator<AnimatedButton>
	{
		public int compare(AnimatedButton a, AnimatedButton b)
		{
			if ( a.hasIcons() && !b.hasIcons()) return -1;
			if (!a.hasIcons() &&  b.hasIcons()) return  1;
			
			return a.getActionCommand().compareTo(b.getActionCommand());
		}
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
	
	/**
	 * Attempts to set swing look and feel to the local system's
	 * native theme. Returns true if successful.
	 */
	public static boolean trySystemLookAndFeel()
	{
		try
		{
			String className = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(className);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static List<Image> getWindowIcons() throws IOException
	{
		return Arrays.asList(
			(Image) ImageIO.read(new File(resDir, "art/smallIcon.png")),
			(Image) ImageIO.read(new File(resDir, "art/mediumIcon.png"))
		);
	}
}
