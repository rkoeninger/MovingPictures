package com.robbix.mp5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.EnumSpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;

/**
 * setVisible(true) needs to be called on instances of this class after
 * constructor is called.
 */
public class SpriteLibraryViewer extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		boolean lazy = Arrays.asList(args).contains("-lazyLoadSprites");
		
		Image smallIcon  = ImageIO.read(new File("./res/art/smallIcon.png"));
		Image mediumIcon = ImageIO.read(new File("./res/art/mediumIcon.png"));
		
		Sandbox.trySystemLookAndFeel();
		UnitFactory factory = UnitFactory.load(new File("./res/units"));
		Mediator.factory = factory;
		SpriteLibrary lib = SpriteLibrary.load(new File("./res/sprites"), lazy);
		JFrame slViewer = new SpriteLibraryViewer(Game.of(lib, factory));
		slViewer.setIconImages(Arrays.asList(smallIcon, mediumIcon));
		slViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		slViewer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SpriteLibrary lib;
	private UnitFactory factory;
	private JTree tree;
	private SLTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private PreviewPanel preview;
	
	public SpriteLibraryViewer(final Game game)
	{
		super("Sprite Library Viewer");
		this.lib = game.getSpriteLibrary();
		this.factory = game.getUnitFactory();
		lib.addModuleListener(new ModuleListener(){
			public void moduleLoaded(ModuleEvent e) {buildTree();}
			public void moduleUnloaded(ModuleEvent e) {buildTree();}
		});
		rootNode = new SLTreeNode("Sprite Library");
		rootNode.setSLObject(lib);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setEditable(true);
		tree.setShowsRootHandles(false);
		buildTree();
		preview = new PreviewPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new JScrollPane(tree));
		splitPane.setRightComponent(preview);
		JMenuBar menuBar = new JMenuBar();
		JMenu moduleMenu = new JMenu("Module");
		final JMenuItem loadByNameMenuItem = new JMenuItem("By Name...");
		final JMenuItem loadFromFileMenuItem = new JMenuItem("From File...");
		final JMenuItem unloadMenuItem = new JMenuItem("Unload...");
		moduleMenu.add(loadByNameMenuItem);
		moduleMenu.add(loadFromFileMenuItem);
		moduleMenu.addSeparator();
		moduleMenu.add(unloadMenuItem);
		menuBar.add(moduleMenu);
		setJMenuBar(menuBar);
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		setSize(500, 500);
		splitPane.setDividerLocation(0.5);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				preview.dispose();
			}
		});
		
		loadByNameMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String moduleList = JOptionPane.showInputDialog(
					SpriteLibraryViewer.this,
					"Module Name (separate multiple names with commas):",
					"Load Module(s)",
					JOptionPane.QUESTION_MESSAGE
				);
				
				if (moduleList == null)
					return;
				
				try
				{
					for (String moduleName : moduleList.split(","))
					{
						moduleName = moduleName.trim();
						lib.loadModule(moduleName);
					}
					buildTree();
					SpriteLibraryViewer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SpriteLibraryViewer.this,
						"Could not load module(s): " + moduleList + "\n" +
						ioe.getMessage(),
						"Load Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
		
		loadFromFileMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Load Module");
				chooser.setCurrentDirectory(new File("./res/sprites"));
				chooser.setFileFilter(new FileFilter(){
					public boolean accept(File file) {
						return file.getName().equals("info.xml") ||
							file.isDirectory();
					}
					public String getDescription() {
						return "SpriteSet Info Files (info.xml)";
					}
				});
				int result = chooser.showOpenDialog(SpriteLibraryViewer.this);
				
				if (result != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = chooser.getSelectedFile();
				try
				{
					lib.loadModule(file);
					buildTree();
					SpriteLibraryViewer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SpriteLibraryViewer.this,
						"Could not load file: " + file + "\n" +
						ioe.getMessage(),
						"Load Error",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
		});
		
		unloadMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String moduleList = JOptionPane.showInputDialog(
					SpriteLibraryViewer.this,
					"Module Name (separate multiple names with commas):",
					"Unload Module(s)",
					JOptionPane.QUESTION_MESSAGE
				);
				
				if (moduleList == null)
					return;
				
				for (String moduleName : moduleList.split(","))
				{
					moduleName = moduleName.trim();
					lib.unloadModule(moduleName);
				}
				
				buildTree();
				SpriteLibraryViewer.this.repaint();
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				SLTreeNode currentNode = (SLTreeNode)
					tree.getLastSelectedPathComponent();
				
				if (currentNode == null)
					return;
				
				if (currentNode.hasSprite())
				{
					preview.showSprite(currentNode.getSprite(), currentNode.isCentered(), currentNode.getFPW(), currentNode.getFPH());
				}
				else if (currentNode.hasSpriteGroup())
				{
					preview.showGroup(currentNode.getSpriteGroup(), currentNode.isCentered(), currentNode.getFPW(), currentNode.getFPH());
				}
				else
				{
					preview.showNothing();
				}
			}
		});
	}
	
	private void buildTree()
	{
		while (rootNode.getChildCount() > 0)
		{
			MutableTreeNode child = (MutableTreeNode) rootNode.getChildAt(0);
			treeModel.removeNodeFromParent(child);
		}
		
		List<String> setNames = new ArrayList<String>();
		setNames.addAll(lib.getLoadedAmbientModules());
		setNames.addAll(lib.getLoadedUnitModules());
		Collections.sort(setNames);
		
		for (String name : setNames)
		{
			appendSpriteSet(name, false);
		}
		
		tree.expandRow(0);
	}
	
	private void appendSpriteSet(String moduleName, boolean scroll)
	{
		SpriteSet spriteSet = lib.getAmbientSpriteSet(moduleName);
		
		if (spriteSet == null)
		{
			UnitType unitType = factory.getType(moduleName);
			spriteSet = lib.getUnitSpriteSet(unitType);
			appendUnitSet(spriteSet, unitType, scroll);
		}
		else
		{
			appendAmbientSet(spriteSet, scroll);
		}
	}
	
	private void appendAmbientSet(SpriteSet ambientSet, boolean scroll)
	{
		SLTreeNode setNode = new SLTreeNode(ambientSet.getName());
		setNode.setSLObject(ambientSet);
		append(rootNode, setNode);
		Object[] keys = ambientSet.getArgumentList();
		Arrays.sort(keys);
		
		for (Object key : keys)
		{
			SpriteGroup group = ambientSet.get(key);
			SLTreeNode groupNode = new SLTreeNode(key.toString());
			groupNode.setSLObject(group);
			append(setNode, groupNode);
			
			for (int i = 0; i < group.getSpriteCount(); ++i)
			{
				Sprite sprite = group.getSprite(i);
				SLTreeNode spriteNode = new SLTreeNode(String.valueOf(i));
				spriteNode.setSLObject(sprite);
				append(groupNode, spriteNode);
			}
		}
		
		if (scroll)
			tree.scrollPathToVisible(new TreePath(setNode.getPath()));
	}
	
	private void appendUnitSet(SpriteSet unitSet, UnitType type, boolean scroll)
	{
		Footprint fp = type.getFootprint();
		SLTreeNode setNode = new SLTreeNode(unitSet.getName());
		setNode.setSLObject(unitSet);
		append(rootNode, setNode);
		Object[] keys = unitSet.getArgumentList();
		
		for (Object key : keys)
		{
			Object[] keyArray = (Object[]) key;
			SpriteGroup group = unitSet.get(keyArray);
			SLTreeNode groupNode = new SLTreeNode(Arrays.toString(keyArray));
			groupNode.setSLObject(group);
			if (fp != null) groupNode.setCornerOffset(fp.getWidth(), fp.getHeight());
			else groupNode.setCornerOffset(1, 1);
			append(setNode, groupNode);
			
			if (group instanceof EnumSpriteGroup)
			{
				EnumSpriteGroup<?> enumGroup = ((EnumSpriteGroup<?>) group);
				Object[] enumVals = enumGroup.getEnumType().getEnumConstants();
				
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					SLTreeNode spriteNode = new SLTreeNode(enumVals[i].toString());
					spriteNode.setSLObject(sprite);
					if (fp != null) spriteNode.setCornerOffset(fp.getWidth(), fp.getHeight());
					else spriteNode.setCornerOffset(1, 1);
					append(groupNode, spriteNode);
				}
			}
			else
			{
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					SLTreeNode spriteNode = new SLTreeNode(String.valueOf(i));
					spriteNode.setSLObject(sprite);
					if (fp != null) spriteNode.setCornerOffset(fp.getWidth(), fp.getHeight());
					else spriteNode.setCornerOffset(1, 1);
					append(groupNode, spriteNode);
				}
			}
		}
		
		if (scroll)
			tree.scrollPathToVisible(new TreePath(setNode.getPath()));
	}
	
	private void append(MutableTreeNode parent, DefaultMutableTreeNode child)
	{
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
	}
	
	private static class SLTreeNode extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;
		
		private Object slObject;
		private boolean corner;
		private int fpWidth;
		private int fpHeight;
		
		public SLTreeNode(String name)
		{
			super(name);
		}
		
		public void setCornerOffset(int fpw, int fph)
		{
			corner = true;
			fpWidth = fpw;
			fpHeight = fph;
		}
		
		public boolean isCentered()
		{
			return !corner;
		}
		
		public int getFPW()
		{
			return fpWidth;
		}
		
		public int getFPH()
		{
			return fpHeight;
		}
		
		public void setSLObject(Object slObject)
		{
			this.slObject = slObject;
		}
		
		public boolean hasSprite()
		{
			return slObject != null && (slObject instanceof Sprite);
		}
		
		public boolean hasSpriteGroup()
		{
			return slObject != null && (slObject instanceof SpriteGroup);
		}
		
		public Sprite getSprite()
		{
			return (Sprite) slObject;
		}
		
		public SpriteGroup getSpriteGroup()
		{
			return (SpriteGroup) slObject;
		}
	}
	
	public static class PreviewPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private Sprite sprite;
		private SpriteGroup group;
		private int w;
		private int h;
		private int frame = 0;
		private int hue = 240;
		private boolean centered;
		private int fpw;
		private int fph;
		private int tileSize = 32;
		
		private JComponent drawPanel;
		private JPanel controlPanel;
		private JSlider delaySlider;
		private JSlider hueSlider;
		
		private Timer timer;
		
		public PreviewPanel()
		{
			this.hue = 240;
			
			delaySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 50);
			delaySlider.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent arg0)
				{
					timer.setDelay(delaySlider.getValue());
				}
			});
			hueSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 359, hue);
			hueSlider.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent arg0)
				{
					hue = hueSlider.getValue();
					drawPanel.repaint();
				}
			});
			drawPanel = new JComponent()
			{
				private static final long serialVersionUID = 1L;
				
				String blankMessage = "Select a Sprite or SpriteGroup to Preview";
				
				public void paintComponent(Graphics g)
				{
					g.setColor(Color.BLACK);
					
					if (sprite == null && group == null)
					{
						FontMetrics metrics = g.getFontMetrics();
						Rectangle2D rect = metrics.getStringBounds(blankMessage, g);
						g.drawString(
							blankMessage,
							(int) (getWidth()  / 2 - rect.getCenterX()),
							(int) (getHeight() / 2 - rect.getCenterY())
						);
						return;
					}
					
					if (timer.isRunning())
					{
						DecimalFormat formatter = new DecimalFormat("0.00");
						double fps = (1000.0 / timer.getDelay());
						String fpsString = "fps = " + (Double.isInfinite(fps) ? "Unlimited" : formatter.format(fps));
						FontMetrics metrics = g.getFontMetrics();
						Rectangle2D rect = metrics.getStringBounds(blankMessage, g);
						g.drawString(
							fpsString,
							(int) (4 - rect.getX()),
							(int) (4 - rect.getY())
						);
					}
					
					g.setColor(Color.DARK_GRAY);
					
					if (centered)
					{
						g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
						g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
					}
					else
					{
						int totalFPW = fpw * tileSize;
						int totalFPH = fph * tileSize;
						int x = (getWidth()  - totalFPW) / 2;
						int y = (getHeight() - totalFPH) / 2;
						
						for (int fpx = 0; fpx <= fpw; ++fpx)
							g.drawLine(x + fpx * tileSize, y - (tileSize/2), x + fpx * tileSize, y + fph * tileSize + (tileSize/2));
						
						for (int fpy = 0; fpy <= fph; ++fpy)
							g.drawLine(x - (tileSize/2), y + fpy * tileSize, x + fpw * tileSize + (tileSize/2), y + fpy * tileSize);
					}
					
					if (sprite != null)
					{
						paintSprite(g, sprite);
					}
					else if (group != null)
					{
						paintSprite(g, group.getFrame(frame % group.getFrameCount()));
					}
				}
				
				private void paintSprite(Graphics g, Sprite sprite)
				{
					Image img = sprite.getImage();
					int baseHue = sprite.getBaseTeamHue();
					if (baseHue != hue && baseHue >= 0 && baseHue < 360)
						img = Utils.recolorUnit((BufferedImage) img, baseHue, hue);
					int x = (getWidth()  - w) / 2 + sprite.getXOffset();
					int y = (getHeight() - h) / 2 + sprite.getYOffset();
					g.drawImage(img, x, y, null);
				}
			};
			
			JPanel controlPanelLabels = new JPanel();
			controlPanelLabels.setLayout(new GridLayout(2, 1));
			controlPanelLabels.add(new JLabel("Delay"));
			controlPanelLabels.add(new JLabel("Team Color"));
			JPanel controlPanelSliders = new JPanel();
			controlPanelSliders.setLayout(new GridLayout(2, 1));
			controlPanelSliders.add(delaySlider);
			controlPanelSliders.add(hueSlider);
			controlPanel = new JPanel();
			controlPanel.setLayout(new BorderLayout());
			controlPanel.add(controlPanelLabels, BorderLayout.WEST);
			controlPanel.add(controlPanelSliders, BorderLayout.CENTER);
			
			setLayout(new BorderLayout());
			add(drawPanel, BorderLayout.CENTER);
			add(controlPanel, BorderLayout.SOUTH);
			
			timer = new Timer(50, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					frame++;
					PreviewPanel.this.repaint();
				}
			});
		}
		
		public void dispose()
		{
			timer.stop();
		}
		
		public void setAnimationDelay(int delay)
		{
			timer.setDelay(delay);
		}
		
		public int getAnimationDelay()
		{
			return timer.getDelay();
		}
		
		public synchronized void showSprite(Sprite sprite, boolean centered, int fpw, int fph)
		{
			timer.stop();
			this.sprite = sprite;
			this.group = null;
			this.centered = centered;
			this.fpw = fpw;
			this.fph = fph;
			w = centered ? 0 : fpw * tileSize;
			h = centered ? 0 : fph * tileSize;
			drawPanel.repaint();
		}
		
		public synchronized void showGroup(SpriteGroup group, boolean centered, int fpw, int fph)
		{
			this.sprite = null;
			this.group = group;
			this.centered = centered;
			this.fpw = fpw;
			this.fph = fph;
			w = centered ? 0 : fpw * tileSize;
			h = centered ? 0 : fph * tileSize;
			frame = 0;
			if (group.getSpriteCount() > 1) timer.start();
			drawPanel.repaint();
		}
		
		public synchronized void showNothing()
		{
			timer.stop();
			this.sprite = null;
			this.group = null;
			drawPanel.repaint();
		}
	}
}
