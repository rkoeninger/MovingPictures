package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

import com.robbix.mp5.AsyncModuleListener;
import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;
import com.robbix.mp5.utils.RTreeNode;

public class SpriteViewer extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		boolean lazy = Arrays.asList(args).contains("-lazyLoadSprites");
		
		Sandbox.trySystemLookAndFeel();
		UnitFactory factory = UnitFactory.load(new File("./res/units"));
		Mediator.factory = factory;
		SpriteLibrary lib = SpriteLibrary.load(new File("./res/sprites"), lazy);
		lib.setAsyncModeEnabled(true);
		JFrame slViewer = new SpriteViewer(Game.of(lib, factory));
		slViewer.setIconImages(Sandbox.getWindowIcons());
		slViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		slViewer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SpriteLibrary lib;
	private UnitFactory factory;
	private JTree tree;
	private RTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JPopupMenu unloadPopup;
	private JPanel rightSidePanel;
	private JPanel controlPanel;
	private JSlider delaySlider;
	private JSlider hueSlider;
	private SpritePanel preview;
	private JMenuItem loadByNameMenuItem;
	private JMenuItem loadFromFileMenuItem;
	private JMenuItem unloadMenuItem;
	
	public SpriteViewer(final Game game)
	{
		super("Sprite Viewer");
		this.lib = game.getSpriteLibrary();
		this.factory = game.getUnitFactory();
		lib.addModuleListener(new LibraryListener());
		unloadPopup = new JPopupMenu();
		rootNode = new RTreeNode("Sprite Library");
		rootNode.set(lib);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setEditable(true);
		tree.setShowsRootHandles(false);
		tree.addMouseListener(new TreeListener());
		tree.addTreeSelectionListener(new TreeListener());
		buildTree();
		preview = new SpritePanel("Select a Sprite or SpriteGroup to Preview");
		delaySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 50);
		delaySlider.addChangeListener(new SliderListener());
		hueSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 359, 240);
		hueSlider.addChangeListener(new SliderListener());
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
		loadByNameMenuItem = new JMenuItem("By Name...");
		loadFromFileMenuItem = new JMenuItem("From File...");
		unloadMenuItem = new JMenuItem("Unload...");
		loadByNameMenuItem.addActionListener(new MenuListener());
		loadFromFileMenuItem.addActionListener(new MenuListener());
		unloadMenuItem.addActionListener(new MenuListener());
		moduleMenu.add(loadByNameMenuItem);
		moduleMenu.add(loadFromFileMenuItem);
		moduleMenu.addSeparator();
		moduleMenu.add(unloadMenuItem);
		menuBar.add(moduleMenu);
		
		setJMenuBar(menuBar);
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		setSize(500, 500);
		splitPane.setDividerLocation(200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private class LibraryListener implements AsyncModuleListener
	{
		public void moduleLoadStarted(ModuleEvent e)
		{
			buildTree();
		}
		
		public void moduleLoaded(ModuleEvent e)
		{
			buildTree();
		}
		
		public void moduleUnloaded(ModuleEvent e)
		{
			buildTree();
			preview.showNothing();
		}
	}
	
	private class MenuListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == loadByNameMenuItem)
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
			else if (e.getSource() == loadFromFileMenuItem)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Load Module");
				chooser.setCurrentDirectory(new File("./res/sprites"));
				chooser.setFileFilter(infoFiles);
				int result = chooser.showOpenDialog(SpriteViewer.this);
				
				if (result != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = chooser.getSelectedFile();
				try
				{
					lib.loadModule(file);
					buildTree();
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
			else if (e.getSource() == unloadMenuItem)
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
			}
		}
	}
	
	private class TreeListener extends MouseAdapter implements TreeSelectionListener
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() != MouseEvent.BUTTON3)
				return;
			
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			
			if (path == null)
				return;
			
			RTreeNode node = (RTreeNode) path.getLastPathComponent();
			
			if (! node.has(SpriteSet.class))
				return;
			
			tree.setSelectionPath(path);
			final SpriteSet set = node.get(SpriteSet.class);
			Point p = tree.getPopupLocation(e);
			final JMenuItem unloadMenuItem = new JMenuItem("Unload");
			unloadMenuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					lib.unloadModule(set.getName());
				}
			});
			unloadPopup.removeAll();
			unloadPopup.add(unloadMenuItem);
			
			if (p == null)
				p = e.getPoint();
			
			unloadPopup.show(tree, p.x, p.y);
		}
		
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
				int w = fp == null ? 0 : fp.getWidth();
				int h = fp == null ? 0 : fp.getHeight();
				preview.show(sprite, w, h);
			}
			else if (currentNode.has(SpriteGroup.class))
			{
				SpriteGroup group = currentNode.get(SpriteGroup.class);
				Footprint fp = currentNode.get(Footprint.class);
				int w = fp == null ? 0 : fp.getWidth();
				int h = fp == null ? 0 : fp.getHeight();
				preview.show(group, w, h);
			}
			else if (currentNode.has(SpriteSet.class))
			{
				SpriteSet set = currentNode.get(SpriteSet.class);
				String setName = set.getName();
				UnitType unitType = factory.getType(setName);
				Sprite sprite = null;
				Footprint fp = null;
				
				if (unitType != null)
				{
					sprite = lib.getDefaultSprite(unitType);
					fp = unitType.getFootprint();
					
					if (fp == null)
						fp = Footprint.VEHICLE;
				}
				else
				{
					sprite = lib.getDefaultAmbientSprite(setName);
				}
				
				int w = fp == null ? 0 : fp.getWidth();
				int h = fp == null ? 0 : fp.getHeight();
				preview.show(sprite, w, h);
			}
			else
			{
				preview.showNothing();
			}
		}
	}
	
	private class SliderListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent arg0)
		{
			if (arg0.getSource() == delaySlider)
			{
				preview.setDelay(delaySlider.getValue());
			}
			else if (arg0.getSource() == hueSlider)
			{
				preview.setHue(hueSlider.getValue());
				preview.repaint();
			}
		}
	}
	
	public void dispose()
	{
		preview.dispose();
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
			appendSpriteSet(name);
		
		tree.expandRow(0);
		validate();
	}
	
	private void appendSpriteSet(String moduleName)
	{
		SpriteSet spriteSet = lib.getAmbientSpriteSet(moduleName);
		
		if (spriteSet == null)
		{
			UnitType unitType = factory.getType(moduleName);
			spriteSet = lib.getUnitSpriteSet(unitType);
			
			if (spriteSet == SpriteSet.BLANK)
			{
				appendPendingSpriteSet(moduleName);
			}
			else
			{
				appendUnitSet(spriteSet, unitType);
			}
		}
		else if (spriteSet == SpriteSet.BLANK)
		{
			appendPendingSpriteSet(moduleName);
		}
		else
		{
			appendAmbientSet(spriteSet);
		}
	}
	
	private void appendPendingSpriteSet(String name)
	{
		RTreeNode pendingNode = new RTreeNode(name + " [Loading...]");
		append(rootNode, pendingNode);
	}
	
	private void appendAmbientSet(SpriteSet ambientSet)
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
	}
	
	private void appendUnitSet(SpriteSet unitSet, UnitType type)
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
			
			if (group.isEnumGroup())
			{
				Object[] enumVals = group.getEnumType().getEnumConstants();
				
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
	}
	
	private void append(MutableTreeNode parent, DefaultMutableTreeNode child)
	{
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
	}
	
	private static FileFilter infoFiles = new FileFilter()
	{
		public boolean accept(File file)
		{
			return file.getName().equals("info.xml") || file.isDirectory();
		}
		
		public String getDescription()
		{
			return "SpriteSet Info Files (info.xml)";
		}
	};
}
