package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.basics.RTreeNode;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.UnitFactory;
import com.robbix.mp5.unit.UnitType;

public class UnitTypeViewer extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		Sandbox.trySystemLookAndFeel();
		UnitFactory factory = UnitFactory.load(new File("./res/units"));
		Mediator.factory = factory;
		SoundBank sounds = SoundBank.loadLazy(new File("./res/sounds"));
		SpriteLibrary lib = SpriteLibrary.loadLazy(new File("./res/sprites"));
		JFrame ufViewer = new UnitTypeViewer(Game.of(lib, factory, sounds));
		ufViewer.setIconImages(Sandbox.getWindowIcons());
		ufViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ufViewer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SoundBank sounds;
	private SpriteLibrary lib;
	private UnitFactory factory;
	private JTree tree;
	private RTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private InfoPanel infoPanel;
	
	public UnitTypeViewer(Game game)
	{
		super("UnitType Viewer");
		sounds = game.getSoundBank();
		lib = game.getSpriteLibrary();
		lib.addModuleListener(new ModuleListener(){
			public void moduleLoaded(ModuleEvent e)
			{
				String name = e.getModuleName();
				
				if (name.equals(infoPanel.type.getName()))
				{
					infoPanel.loadArtButton.setVisible(false);
					infoPanel.showSpritesFor(infoPanel.type);
				}
			}
			public void moduleUnloaded(ModuleEvent e)
			{
				String name = e.getModuleName();
				
				if (name.equals(infoPanel.type.getName()))
				{
					infoPanel.loadArtButton.setVisible(true);
					infoPanel.preview.show("Art not loaded");
				}
			}
		});
		factory = game.getUnitFactory();
		rootNode = new RTreeNode("UnitTypes");
		rootNode.set(factory);
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setEditable(true);
		tree.setShowsRootHandles(false);
		buildTree();
		infoPanel = new InfoPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new JScrollPane(tree));
		splitPane.setRightComponent(infoPanel);
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
				infoPanel.dispose();
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
				
				if (currentNode.has(UnitType.class))
				{
					infoPanel.show(currentNode.get(UnitType.class));
				}
				else
				{
					infoPanel.showNothing();
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
		
		List<String> typeNames = factory.getUnitTypes();
		Collections.sort(typeNames);
		
		for (String name : typeNames)
		{
			appendUnitType(name);
		}
		
		tree.expandRow(0);
	}
	
	private void appendUnitType(String typeName)
	{
		UnitType type = factory.getType(typeName);
		RTreeNode typeNode = new RTreeNode(typeName);
		typeNode.set(type);
		append(rootNode, typeNode);
		
		if (type.isTankType())
		{
			UnitType chassisType = factory.getChassisType(type);
			UnitType turretType = factory.getTurretType(type);
			RTreeNode chassisTypeNode = new RTreeNode("Chassis: " + chassisType.getName());
			RTreeNode turretTypeNode = new RTreeNode("Turret: " + turretType.getName());
			chassisTypeNode.set(chassisType);
			turretTypeNode.set(turretType);
			append(typeNode, turretTypeNode);
			append(typeNode, chassisTypeNode);
		}
	}
	
	private void append(MutableTreeNode parent, DefaultMutableTreeNode child)
	{
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
	}
	
	private boolean isArtLoaded(UnitType type)
	{
		if (type.isTankType())
		{
			return lib.isLoaded(type.getChassisTypeName())
				&& lib.isLoaded(type.getTurretTypeName());
		}
		else
		{
			return lib.isLoaded(type.getName());
		}
	}
	
	private boolean loadArt(UnitType type)
	{
		try
		{
			if (type.isTankType())
			{
				lib.loadModule(type.getChassisTypeName());
				lib.loadModule(type.getTurretTypeName());
			}
			else
			{
				lib.loadModule(type.getName());
			}
			
			return true;
		}
		catch (IOException ioe)
		{
			return false;
		}
	}
	
	private class InfoPanel extends JComponent
	{
		private static final long serialVersionUID = 1L;
		
		public SpritePanel preview;
		private JTable table;
		private DefaultTableModel tableModel;
		public UnitType type;
		private JButton playAckButton;
		public JButton loadArtButton;
		
		public InfoPanel()
		{
			preview = new SpritePanel("Select a UnitType to view info");
			preview.setPreferredSize(new Dimension(100, 150));
			playAckButton = new JButton("Play Ack");
			playAckButton.setEnabled(false);
			playAckButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (type != null)
					{
						String ack = type.getAcknowledgement();
						
						if (ack != null)
							sounds.playAnyway(ack);
					}
				}
			});
			loadArtButton = new JButton("Load Art");
			loadArtButton.setVisible(false);
			loadArtButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (type != null)
					{
						if (loadArt(type))
						{
							showSpritesFor(type);
							loadArtButton.setVisible(false);
						}
						else
						{
							System.err.println("couldn't load sprites for " + type.getName());
						}
					}
				}
			});
			preview.setLayout(null);
			preview.add(playAckButton);
			playAckButton.setBounds(new Rectangle(new Point(4, 4), playAckButton.getPreferredSize()));
			preview.add(loadArtButton);
			loadArtButton.setBounds(new Rectangle(new Point(4, 4 + playAckButton.getPreferredSize().height + 4), playAckButton.getPreferredSize()));
			tableModel = new DefaultTableModel();
			tableModel.addColumn("Name");
			tableModel.addColumn("Value");
			table = new JTable(tableModel);
 			
			setLayout(new BorderLayout());
			add(preview, BorderLayout.NORTH);
			add(new JScrollPane(table), BorderLayout.CENTER);
		}
		
		private void clearTable()
		{
			while (tableModel.getRowCount() > 0)
				tableModel.removeRow(0);
		}
		
		private void appendTableRow(String name, Object value)
		{
			tableModel.addRow(new Object[]{name, value});
		}
		
		public void showSpritesFor(UnitType type)
		{
			Footprint fp = type.getFootprint();
			
			if (fp == null)
				fp = Footprint.VEHICLE;
			
			Sprite[] sprites;
			
			if (type.isTankType())
			{
				sprites = new Sprite[]{
					lib.getDefaultSprite(factory.getChassisType(type)),
					lib.getDefaultSprite(factory.getTurretType(type))
				};
			}
			else
			{
				sprites = new Sprite[]{lib.getDefaultSprite(type)};
			}
			
			preview.show(sprites, fp.getWidth(), fp.getHeight());
		}
		
		public void show(UnitType type)
		{
			this.type = type;
			
			clearTable();
			appendTableRow("Type Name", type.getName());
			appendTableRow("Civ", type.getCiv());
			appendTableRow("HP", type.getMaxHP());
			appendTableRow("Armor", type.getArmor());
			appendTableRow("Damage", type.getDamage());
			appendTableRow("Attack Range", type.getAttackRange());
			appendTableRow("Sight Range", type.getSightRange());
			appendTableRow("Reload Time", type.getWeaponChargeCost());
			appendTableRow("Cost", type.getCost());
			appendTableRow("Ack", type.getAcknowledgement());
			
			boolean artLoaded = isArtLoaded(type);
			
			playAckButton.setEnabled(type.getAcknowledgement() != null);
			loadArtButton.setVisible(! artLoaded);
			
			if (artLoaded)
			{
				showSpritesFor(type);
			}
			else
			{
				preview.show("Art not loaded");
			}
		}
		
		public void showNothing()
		{
			type = null;
			clearTable();
			preview.showNothing();
		}
		
		public void dispose()
		{
			preview.dispose();
		}
	}
}
