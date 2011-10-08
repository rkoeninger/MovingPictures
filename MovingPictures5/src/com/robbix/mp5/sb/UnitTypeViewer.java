package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
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
		Image smallIcon  = ImageIO.read(new File("./res/art/smallIcon.png"));
		Image mediumIcon = ImageIO.read(new File("./res/art/mediumIcon.png"));
		
		Sandbox.trySystemLookAndFeel();
		UnitFactory factory = UnitFactory.load(new File("./res/units"));
		Mediator.factory = factory;
		SoundBank sounds = SoundBank.loadLazy(new File("./res/sounds"));
		SpriteLibrary lib = SpriteLibrary.loadLazy(new File("./res/sprites"));
		JFrame ufViewer = new UnitTypeViewer(Game.of(lib, factory, sounds));
		ufViewer.setIconImages(Arrays.asList(smallIcon, mediumIcon));
		ufViewer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ufViewer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SoundBank sounds;
	private SpriteLibrary lib;
	private UnitFactory factory;
	private JTree tree;
	private UFTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private PreviewPanel preview;
	
	public UnitTypeViewer(Game game)
	{
		super("UnitType Library Viewer");
		this.sounds = game.getSoundBank();
		this.lib = game.getSpriteLibrary();
		this.factory = game.getUnitFactory();
		rootNode = new UFTreeNode("UnitType Factory");
		rootNode.setUFObject(factory);
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
			}
		});
		
		loadFromFileMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		unloadMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				UFTreeNode currentNode = (UFTreeNode)
					tree.getLastSelectedPathComponent();
				
				if (currentNode == null)
					return;
				
				if (currentNode.hasUnitType())
				{
					preview.showUnitType(currentNode.getUnitType());
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
		UFTreeNode typeNode = new UFTreeNode(typeName);
		typeNode.setUFObject(type);
		append(rootNode, typeNode);
		
		if (type.isTankType())
		{
			UnitType chassisType = factory.getChassisType(type);
			UnitType turretType = factory.getTurretType(type);
			UFTreeNode chassisTypeNode = new UFTreeNode("Chassis: " + chassisType.getName());
			UFTreeNode turretTypeNode = new UFTreeNode("Turret: " + turretType.getName());
			chassisTypeNode.setUFObject(chassisType);
			turretTypeNode.setUFObject(turretType);
			append(typeNode, turretTypeNode);
			append(typeNode, chassisTypeNode);
		}
	}
	
	private void append(MutableTreeNode parent, DefaultMutableTreeNode child)
	{
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
	}
	
	private class UFTreeNode extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;
		
		private Object ufObject;
		
		public UFTreeNode(String name)
		{
			super(name);
		}
		
		public void setUFObject(Object ufObject)
		{
			this.ufObject = ufObject;
		}
		
		public Object getUFObject()
		{
			return ufObject;
		}
		
		public boolean hasUnitType()
		{
			return ufObject != null && ufObject instanceof UnitType;
		}
		
		public UnitType getUnitType()
		{
			return (UnitType) getUFObject();
		}
	}
	
	private class PreviewPanel extends JComponent
	{
		private static final long serialVersionUID = 1L;

		private UnitType type;
		private Sprite sprite;
		private Sprite turretSprite;
		private int fpw;
		private int fph;
		
		public PreviewPanel()
		{
			addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if (e.getClickCount() == 2 && type != null)
					{
						String ack =
							type.isTankType()
							? type.getAcknowledgement()
							: factory.getChassisType(type).getAcknowledgement();
						
						if (ack != null)
							sounds.play(ack);
					}
				}
			});
		}
		
		public void dispose()
		{
			
		}

		String blankMessage = "Select a UnitType to Preview";
		
		public void paintComponent(Graphics g)
		{
			g.setColor(Color.BLACK);
			
			if (type == null)
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
			
			StringBuilder info = new StringBuilder();
			info.append(type.getDisplayName()).append('\n');
			info.append(type.getName()).append('\n');
			
			if (!type.isTurretType())
			{
				info.append(type.getMaxHP() + " HP").append('\n');
				info.append(type.getArmor() + " Armor").append('\n');
			}
			
			if (type.isTurretType() || type.isGuardPostType())
			{
				info.append(type.getAttackRange() + " Range").append('\n');
				info.append(type.getDamage() + " Damage").append('\n');
			}
			
			drawString(g, info.toString(), 4, 4);
			
			if (sprite != null)
			{
				int x = getWidth()  / 2 - fpw * 16;
				int y = getHeight() / 2 - fph * 16;
				g.drawImage(
					sprite.getImage(),
					x + sprite.getXOffset(),
					y + sprite.getYOffset(),
					null
				);
				
				if (turretSprite != null)
				{
					g.drawImage(
						turretSprite.getImage(),
						x + turretSprite.getXOffset(),
						y + turretSprite.getYOffset(),
						null
					);
				}
			}
		}
		
		private void drawString(Graphics g, String string, int x, int y)
		{
			String[] lines = string.split("[\r\n\f]");
			FontMetrics metrics = g.getFontMetrics();
			
			y += metrics.getAscent();
			
			for (String line : lines)
			{
				g.drawString(line, x, y);
				y += metrics.getAscent() + 4;
			}
		}
		
		public void showUnitType(UnitType type)
		{
			this.type = type;
			
			if (type.isTankType())
			{
				UnitType chassisType = factory.getChassisType(type);
				UnitType turretType = factory.getTurretType(type);
				sprite = lib.getDefaultSprite(chassisType);
				turretSprite = lib.getDefaultSprite(turretType);
				fpw = chassisType.getFootprint().getWidth();
				fph = chassisType.getFootprint().getHeight();
			}
			else
			{
				sprite = lib.getDefaultSprite(type);
				turretSprite = null;
				Footprint fp = type.getFootprint();
				fpw = fp == null ? 1 : fp.getWidth();
				fph = fp == null ? 1 : fp.getHeight();
			}
			
			repaint();
		}
		
		public void showNothing()
		{
			type = null;
			sprite = null;
			turretSprite = null;
			repaint();
		}
	}
}
