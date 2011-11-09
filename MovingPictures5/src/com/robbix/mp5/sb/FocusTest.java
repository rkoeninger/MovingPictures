package com.robbix.mp5.sb;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

public class FocusTest
{
	static enum State {SPLIT, LEFT, RIGHT}
	static State state = State.SPLIT;
	
	public static void main(String[] args) throws IOException
	{
//		final javax.swing.JTextArea area1 = new javax.swing.JTextArea(), area2 = new JTextArea();
//		final Component left = area1, right = area2;
		
		com.robbix.mp5.Game game = com.robbix.mp5.Game.load(new File("./res"), "16-16-plain", "newTerraSand", true, true);
		com.robbix.mp5.Mediator.initMediator(game);
		game.addPlayer(new com.robbix.mp5.player.Player(1, "1", 1));
		game.getMap().putUnit(game.getUnitFactory().newUnit("pScout", 1), game.getMap().getBounds().getCenter());
		game.newDisplay();
		final com.robbix.mp5.ui.DisplayPanel panel1 = game.getDisplay(0);
		final com.robbix.mp5.ui.DisplayPanel panel2 = game.getDisplay(1);
		final Component left = panel1.getView(), right = panel2.getView();
		
		final JSplitPane splitPane = new JSplitPane(HORIZONTAL_SPLIT, left, right);
		final JFrame frame = new JFrame();
		frame.add(splitPane);
		frame.setSize(512, 512);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JMenuItem splitMenuItem = new JMenuItem("Split");
		final JMenuItem leftMenuItem = new JMenuItem("Left");
		final JMenuItem rightMenuItem = new JMenuItem("Right");
		final JMenu splitMenu = new JMenu("Split");
		splitMenu.add(leftMenuItem);
		splitMenu.add(rightMenuItem);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(splitMenu);
		frame.setJMenuBar(menuBar);
		splitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (state != State.SPLIT)
				{
					frame.remove(left);
					frame.remove(right);
					splitPane.setLeftComponent(left);
					splitPane.setRightComponent(right);
					frame.add(splitPane);
					splitMenu.remove(splitMenuItem);
					splitMenu.add(leftMenuItem);
					splitMenu.add(rightMenuItem);
					frame.validate();
					state = State.SPLIT;
				}
			}
		});
		leftMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (state == State.SPLIT)
				{
					frame.remove(splitPane);
					frame.add(left);
					splitMenu.remove(leftMenuItem);
					splitMenu.remove(rightMenuItem);
					splitMenu.add(splitMenuItem);
					frame.validate();
					state = State.LEFT;
				}
			}
		});
		rightMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (state == State.SPLIT)
				{
					frame.remove(splitPane);
					frame.add(right);
					splitMenu.remove(leftMenuItem);
					splitMenu.remove(rightMenuItem);
					splitMenu.add(splitMenuItem);
					frame.validate();
					state = State.RIGHT;
				}
			}
		});
		frame.setVisible(true);
//		splitPane.setDividerSize(4);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setFocusTraversalKeysEnabled(false);
		splitPane.setOneTouchExpandable(true);
		splitPane.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				System.out.println("splitPane: focus gained");
			}
			
			public void focusLost(FocusEvent e)
			{
				System.out.println("splitPane: focus lost");
			}
		});
		left.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				System.out.println("splitPane.left: focus gained");
			}
			
			public void focusLost(FocusEvent e)
			{
				System.out.println("splitPane.left: focus lost");
			}
		});
		right.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				System.out.println("splitPane.right: focus gained");
			}
			
			public void focusLost(FocusEvent e)
			{
				System.out.println("splitPane.right: focus lost");
			}
		});
	}
}
