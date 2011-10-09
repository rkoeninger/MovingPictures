package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.basics.RTreeNode;
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
public class SpriteViewer extends JFrame
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
		JFrame slViewer = new SpriteViewer(Game.of(lib, factory));
		slViewer.setIconImages(Arrays.asList(smallIcon, mediumIcon));
		slViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		slViewer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SpriteLibrary lib;
	private UnitFactory factory;
	private JTree tree;
	private RTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JPanel rightSidePanel;
	private JPanel controlPanel;
	private JSlider delaySlider;
	private JSlider hueSlider;
	private SpritePanel preview;
	
	public SpriteViewer(final Game game)
	{
		super("Sprite Viewer");
		this.lib = game.getSpriteLibrary();
		this.factory = game.getUnitFactory();
		lib.addModuleListener(new ModuleListener(){
			public void moduleLoaded(ModuleEvent e) {buildTree();}
			public void moduleUnloaded(ModuleEvent e) {buildTree();}
		});
		rootNode = new RTreeNode("Sprite Library");
		rootNode.set(lib);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setEditable(true);
		tree.setShowsRootHandles(false);
		buildTree();
		preview = new SpritePanel("Select a Sprite or SpriteGroup to Preview");
		delaySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 50);
		delaySlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0)
			{
				preview.setDelay(delaySlider.getValue());
			}
		});
		hueSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 359, 240);
		hueSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0)
			{
				preview.setHue(hueSlider.getValue());
				preview.repaint();
			}
		});
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
		controlPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		rightSidePanel = new JPanel();
		rightSidePanel.setLayout(new BorderLayout());
		rightSidePanel.add(preview, BorderLayout.CENTER);
		rightSidePanel.add(controlPanel, BorderLayout.SOUTH);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new JScrollPane(tree));
		splitPane.setRightComponent(rightSidePanel);
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
					SpriteViewer.this,
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
					SpriteViewer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SpriteViewer.this,
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
				int result = chooser.showOpenDialog(SpriteViewer.this);
				
				if (result != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = chooser.getSelectedFile();
				try
				{
					lib.loadModule(file);
					buildTree();
					SpriteViewer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SpriteViewer.this,
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
					SpriteViewer.this,
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
				SpriteViewer.this.repaint();
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				RTreeNode currentNode = (RTreeNode)
					tree.getLastSelectedPathComponent();
				
				if (currentNode == null)
					return;
				
				if (currentNode.has(Sprite.class))
				{
					Sprite sprite = currentNode.get(Sprite.class);
					Footprint fp = currentNode.get(Footprint.class);
					preview.show(
						sprite,
						fp == null ? 0 : fp.getWidth(),
						fp == null ? 0 : fp.getHeight()
					);
				}
				else if (currentNode.has(SpriteGroup.class))
				{
					SpriteGroup group = currentNode.get(SpriteGroup.class);
					Footprint fp = currentNode.get(Footprint.class);
					preview.show(
						group,
						fp == null ? 0 : fp.getWidth(),
						fp == null ? 0 : fp.getHeight()
					);
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
		RTreeNode setNode = new RTreeNode(ambientSet.getName());
		setNode.set(ambientSet);
		append(rootNode, setNode);
		Object[] keys = ambientSet.getArgumentList();
		Arrays.sort(keys);
		
		for (Object key : keys)
		{
			SpriteGroup group = ambientSet.get(key);
			RTreeNode groupNode = new RTreeNode(key.toString());
			groupNode.set(group);
			append(setNode, groupNode);
			
			for (int i = 0; i < group.getSpriteCount(); ++i)
			{
				Sprite sprite = group.getSprite(i);
				RTreeNode spriteNode = new RTreeNode(String.valueOf(i));
				spriteNode.set(sprite);
				append(groupNode, spriteNode);
			}
		}
		
		if (scroll)
			tree.scrollPathToVisible(new TreePath(setNode.getPath()));
	}
	
	private void appendUnitSet(SpriteSet unitSet, UnitType type, boolean scroll)
	{
		Footprint fp = type.getFootprint();
		RTreeNode setNode = new RTreeNode(unitSet.getName());
		setNode.set(unitSet);
		append(rootNode, setNode);
		Object[] keys = unitSet.getArgumentList();
		
		for (Object key : keys)
		{
			Object[] keyArray = (Object[]) key;
			SpriteGroup group = unitSet.get(keyArray);
			RTreeNode groupNode = new RTreeNode(Arrays.toString(keyArray));
			groupNode.set(group, fp != null ? fp : Footprint.VEHICLE);
			append(setNode, groupNode);
			
			if (group instanceof EnumSpriteGroup)
			{
				EnumSpriteGroup<?> enumGroup = ((EnumSpriteGroup<?>) group);
				Object[] enumVals = enumGroup.getEnumType().getEnumConstants();
				
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					RTreeNode spriteNode = new RTreeNode(enumVals[i].toString());
					spriteNode.set(sprite, fp != null ? fp : Footprint.VEHICLE);
					append(groupNode, spriteNode);
				}
			}
			else
			{
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					RTreeNode spriteNode = new RTreeNode(String.valueOf(i));
					spriteNode.set(sprite, fp != null ? fp : Footprint.VEHICLE);
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
}
