package com.robbix.mp5;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.UnitType;

/**
 * setVisible(true) needs to be called on instances of this class after
 * constructor is called.
 */
public class SpriteLibraryViewer extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private SpriteLibrary lib;
	private JTree tree;
	private SLTreeNode rootNode;
	private PreviewPanel preview;
	
	public SpriteLibraryViewer(SpriteLibrary lib)
	{
		super("Sprite Library Viewer");
		this.lib = lib;
		loadTree();
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		preview = new PreviewPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(tree);
		splitPane.setRightComponent(preview);
		add(splitPane);
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
					preview.showSprite(currentNode.getSprite());
				}
				else if (currentNode.hasSpriteGroup())
				{
					preview.showGroup(currentNode.getSpriteGroup());
				}
				else
				{
					preview.showNothing();
				}
			}
		});
	}
	
	private void loadTree()
	{
		rootNode = newLibraryNode(lib);
		rootNode.setSLObject(lib);
		
		for (String ambientSetName : lib.getLoadedAmbientModules())
		{
			SpriteSet ambientSet = lib.getAmbientSpriteSet(ambientSetName);
			SLTreeNode setNode = newSetNode(ambientSet);
			setNode.setSLObject(ambientSet);
			append(rootNode, setNode);
			Object[] keys = ambientSet.getArgumentList();
			Arrays.sort(keys);
			
			for (Object key : keys)
			{
				SpriteGroup group = ambientSet.get(key);
				SLTreeNode groupNode = newNode(key.toString());
				groupNode.setSLObject(group);
				append(setNode, groupNode);
				
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					SLTreeNode spriteNode = newNode(String.valueOf(i));
					spriteNode.setSLObject(sprite);
					append(groupNode, spriteNode);
				}
			}
		}
		
		for (String unitSetName : lib.getLoadedUnitModules())
		{
			UnitType type = Mediator.factory.getType(unitSetName);
			SpriteSet unitSet = lib.getUnitSpriteSet(type);
			SLTreeNode setNode = newSetNode(unitSet);
			setNode.setSLObject(unitSet);
			append(rootNode, setNode);
			Object[] keys = unitSet.getArgumentList();
//			Arrays.sort(keys);
			
			for (Object key : keys)
			{
				Object[] keyArray = (Object[]) key;
				SpriteGroup group = unitSet.get(keyArray);
				SLTreeNode groupNode = newNode(Arrays.toString(keyArray));
				groupNode.setSLObject(group);
				append(setNode, groupNode);
				
				for (int i = 0; i < group.getSpriteCount(); ++i)
				{
					Sprite sprite = group.getSprite(i);
					SLTreeNode spriteNode = newNode(String.valueOf(i));
					spriteNode.setSLObject(sprite);
					append(groupNode, spriteNode);
				}
			}
		}
	}
	
	private static SLTreeNode newLibraryNode(SpriteLibrary lib)
	{
		return new SLTreeNode("Sprite Library");
	}
	
	private static SLTreeNode newSetNode(SpriteSet set)
	{
		return new SLTreeNode(set.getName());
	}
	
	private static SLTreeNode newNode(String name)
	{
		return new SLTreeNode(name);
	}
	
	private static void append(MutableTreeNode parent, MutableTreeNode child)
	{
		parent.insert(child, parent.getChildCount());
	}
	
	private static class SLTreeNode extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;
		
		private Object slObject;
		
		public SLTreeNode(String name)
		{
			super(name);
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
	
	public static class PreviewPanel extends JComponent
	{
		private static final long serialVersionUID = 1L;
		
		private Sprite sprite;
		private SpriteGroup group;
		private int frame = 0;
		
		private Timer timer;
		
		public PreviewPanel()
		{
			timer = new Timer(50, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					frame++;
					PreviewPanel.this.repaint();
				}
			});
			timer.start();
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
		
		public synchronized void showSprite(Sprite sprite)
		{
			this.sprite = sprite;
			this.group = null;
			repaint();
		}
		
		public synchronized void showGroup(SpriteGroup group)
		{
			this.sprite = null;
			this.group = group;
			frame = 0;
			repaint();
		}
		
		public synchronized void showNothing()
		{
			this.sprite = null;
			this.group = null;
			repaint();
		}
		
		public synchronized void paintComponent(Graphics g)
		{
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
			int w = img.getWidth (null);
			int h = img.getHeight(null);
			int x = (getWidth()  - w) / 2;
			int y = (getHeight() - h) / 2;
			g.drawImage(img, x, y, null);
		}
	}
}
