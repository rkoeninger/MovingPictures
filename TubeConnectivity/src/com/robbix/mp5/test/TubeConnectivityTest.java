package com.robbix.mp5.test;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class TubeConnectivityTest
{
	static class Stopwatch
	{
		long time;
		
		public void start()
		{
			time = System.nanoTime();
		}
		
		public void stop()
		{
			System.out.println((System.nanoTime() - time) / 1000000.0);
			time = 0;
		}
	}
	
	static Stopwatch watch = new Stopwatch();
	
	static enum Tool
	{
		TUBE,
		SOURCE,
		COMMAND_CENTER,
		STRUCTURE,
		STRUCTURE2,
		STRUCTURE3,
		STRUCTURE4,
		STRUCTURE5,
		STRUCTURE6,
		STRUCTURE7
	}
	
	static Tool tool = Tool.TUBE;

	static GameMap map = new GameMap(48, 48);
	
	public static void main(String[] args)
	{
		final DisplayPanel panel = new DisplayPanel(map);
		panel.setTileSize(12);
		panel.setTool(tool);
		
		KeyAdapter keyListener = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode())
				{
				case KeyEvent.VK_1:
					tool = Tool.TUBE;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_2:
					tool = Tool.SOURCE;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_3:
					tool = Tool.COMMAND_CENTER;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_4:
					tool = Tool.STRUCTURE;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_5:
					tool = Tool.STRUCTURE2;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_6:
					tool = Tool.STRUCTURE3;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_7:
					tool = Tool.STRUCTURE4;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_8:
					tool = Tool.STRUCTURE5;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_9:
					tool = Tool.STRUCTURE6;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_0:
					tool = Tool.STRUCTURE7;
					panel.setTool(tool);
					break;
					
				case KeyEvent.VK_G:
					panel.showGrid = !panel.showGrid;
					break;
					
				case KeyEvent.VK_S:
					if (e.isControlDown())
					{
						try
						{
							watch.start();
							map.save(new File("savemap"));
							System.out.println("Map saved");
							watch.stop();
						}
						catch (IOException e1)
						{
							watch.stop();
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Whoops");
						}
					}
					break;
					
				case KeyEvent.VK_O:
					if (e.isControlDown())
					{
						try
						{
							watch.start();
							map = GameMap.load(new File("savemap"));
							panel.setMap(map);
							System.out.println("Map opened");
							watch.stop();
						}
						catch (IOException e1)
						{
							watch.stop();
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, "Whoops");
						}
					}
					break;
				}
				
				panel.repaint();
			}
		};
		
		MouseAdapter mouseListener = new MouseAdapter()
		{
			Position prev = null;
			int prevButton = 0;
			Position start = null;
			
			public void mousePressed(MouseEvent e)
			{
				Position pos = new Position(e.getX() / panel.tileSize, e.getY() / panel.tileSize);
				
				prev = pos;
				prevButton = e.getButton();
				
				if (start == null)
					start = pos;
				
				if (!map.contains(pos))
					return;
				
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					switch (tool)
					{
					case TUBE:
						watch.start();
						map.putTube(pos);
						watch.stop();
						break;
						
					case SOURCE:
						map.putSource(pos);
						break;

					case COMMAND_CENTER:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_3_BY_2, true), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;
						
					case STRUCTURE:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_1_BY_2, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;
						
					case STRUCTURE2:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_2_BY_2, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;

					case STRUCTURE3:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_3_BY_2, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;

					case STRUCTURE4:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_3_BY_3, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;

					case STRUCTURE5:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_4_BY_3, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;

					case STRUCTURE6:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_5_BY_4, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;

					case STRUCTURE7:
						try
						{
							watch.start();
							map.put(new Unit(Footprint.STRUCT_1_BY_1, false), pos);
							watch.stop();
						}
						catch (IndexOutOfBoundsException exc)
						{
							watch.stop();
							JOptionPane.showMessageDialog(null, "bad spot");
						}
						break;
						
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON2)
				{
					Unit unit = map.getUnit(pos);
					
					if (unit != null)
					{
						System.out.println(unit.isConnected() ? "alive" : "dead");
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
					watch.start();
					map.remove(pos);
					watch.stop();
				}
				
				panel.repaint();
			}
			
			public void mouseReleased(MouseEvent e)
			{
				prev = null;
				prevButton = 0;
				start = null;
			}
			
			public void mouseEntered(MouseEvent e)
			{
				panel.setToolPosition(new Position(
					e.getX() / panel.tileSize,
					e.getY() / panel.tileSize
				));
				panel.repaint();
			}
			
			public void mouseExited(MouseEvent e)
			{
				panel.setToolPosition(null);
				panel.repaint();
			}
			
			public void mouseMoved(MouseEvent e)
			{
				Position pos = new Position(
					e.getX() / panel.tileSize,
					e.getY() / panel.tileSize
				);
				
				if (!pos.equals(panel.getToolPosition()))
				{
					panel.setToolPosition(pos);
					panel.repaint();
				}
			}
			
			public void mouseDragged(MouseEvent e)
			{
				Position pos = new Position(e.getX() / panel.tileSize, e.getY() / panel.tileSize);
				
				if ((!map.contains(pos)) || (prev != null && prev.equals(pos)))
					return;
				
				if (!pos.equals(panel.getToolPosition()))
				{
					panel.setToolPosition(pos);
					panel.repaint();
				}
				
				prev = pos;
				
				if (e.isControlDown() && !pos.isInLine(start))
					return;
				
				if (tool == Tool.TUBE)
				{
					if (prevButton == MouseEvent.BUTTON1)
					{
						watch.start();
						map.putTube(pos);
						watch.stop();
					}
				}
				
				if (prevButton == MouseEvent.BUTTON3)
				{
					watch.start();
					map.remove(pos);
					watch.stop();
				}
				
				panel.repaint();
			}
		};
		
		final JFrame frame = new JFrame("Tube Connectivity");
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		panel.addKeyListener(keyListener);
		panel.addMouseListener(mouseListener);
		panel.addMouseMotionListener(mouseListener);
	}
}
