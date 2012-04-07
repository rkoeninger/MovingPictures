package com.robbix.mp5.mapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class Mapper
{
	private static int maxW;
	private static int maxH;
	
	private static JFrame frame;
	private static MapDisplayPanel panel;
	private static JLabel statusLabel;

	private static void showStatus(String str)
	{
		statusLabel.setText(str);
	}
	
	public static void main(String[] args) throws IOException
	{
		try
		{
			DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment()
												.getDefaultScreenDevice()
												.getDisplayMode();
			maxW = (dm.getWidth()  * 4) / 5;
			maxH = (dm.getHeight() * 4) / 5;
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Map map = new Map(16, 16);
		
		statusLabel = new JLabel();
		
		panel = new MapDisplayPanel(map);
		
		frame = new JFrame("Mapper");
		frame.setLayout(new BorderLayout());
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.add(statusLabel, BorderLayout.SOUTH);
		frame.setMaximumSize(new Dimension(maxW, maxH));
		scrollPane.setMaximumSize(new Dimension(maxW, maxH));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		HotKeys hks = new HotKeys();
		hks.add(NEW);
		hks.add(OPEN);
		hks.add(SAVE);
		hks.add(ZOOM_IN);
		hks.add(ZOOM_OUT);
		hks.add(LARGE_BRUSH);
		hks.add(MEDIUM_BRUSH);
		hks.add(SMALL_BRUSH);
		hks.add(XLARGE_BRUSH);
		hks.add(XXLARGE_BRUSH);
		hks.add(FEATHERED_BRUSH);
		hks.add(SQUARE_BRUSH);
		
		frame.addKeyListener(hks);
		
		panel.repaint();
	}

	private static final KeyStroke CTRL_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
	private static final KeyStroke CTRL_O = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
	private static final KeyStroke CTRL_N = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
	private static final KeyStroke EQUALS = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0);
	private static final KeyStroke PLUS   = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0);
	private static final KeyStroke MINUS  = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0);
	private static final KeyStroke UNDERSCORE = KeyStroke.getKeyStroke(KeyEvent.VK_UNDERSCORE, 0);
	private static final KeyStroke NUM_1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0);
	private static final KeyStroke NUM_2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, 0);
	private static final KeyStroke NUM_3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, 0);
	private static final KeyStroke NUM_4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, 0);
	private static final KeyStroke NUM_5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, 0);
	private static final KeyStroke LETTER_F = KeyStroke.getKeyStroke(KeyEvent.VK_F, 0);
	private static final KeyStroke LETTER_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
	
	private static final HotKey FEATHERED_BRUSH = new HotKey(LETTER_F)
	{
		public void hotkeyTyped()
		{
			panel.feathered = !panel.feathered;
			showStatus(panel.feathered ? "Feathered Brush" : "Flat Brush");
		}
	};

	private static final HotKey SQUARE_BRUSH = new HotKey(LETTER_S)
	{
		public void hotkeyTyped()
		{
			panel.square = !panel.square;
			showStatus(panel.square ? "Square Brush" : "Round Brush");
		}
	};
	
	private static final HotKey SMALL_BRUSH = new HotKey(NUM_1)
	{
		public void hotkeyTyped()
		{
			panel.brushSize = 1;
			showStatus("Brush Size 1");
		}
	};

	private static final HotKey MEDIUM_BRUSH = new HotKey(NUM_2)
	{
		public void hotkeyTyped()
		{
			panel.brushSize = 2;
			showStatus("Brush Size 2");
		}
	};

	private static final HotKey LARGE_BRUSH = new HotKey(NUM_3)
	{
		public void hotkeyTyped()
		{
			panel.brushSize = 3;
			showStatus("Brush Size 3");
		}
	};

	private static final HotKey XLARGE_BRUSH = new HotKey(NUM_4)
	{
		public void hotkeyTyped()
		{
			panel.brushSize = 4;
			showStatus("Brush Size 4");
		}
	};

	private static final HotKey XXLARGE_BRUSH = new HotKey(NUM_5)
	{
		public void hotkeyTyped()
		{
			panel.brushSize = 5;
			showStatus("Brush Size 5");
		}
	};
	
	private static final HotKey ZOOM_IN = new HotKey(EQUALS, PLUS)
	{
		public void hotkeyTyped()
		{
			if (panel.tileSize < 128)
			{
				panel.setTileSize(panel.tileSize * 2);
				showStatus("Zoom: " + (int)(Math.log(panel.tileSize) / Math.log(2) - 5));
			}
		}
	};

	private static final HotKey ZOOM_OUT = new HotKey(MINUS, UNDERSCORE)
	{
		public void hotkeyTyped()
		{
			if (panel.tileSize > 1)
			{
				panel.setTileSize(panel.tileSize / 2);
				showStatus("Zoom: " + (int)(Math.log(panel.tileSize) / Math.log(2) - 5));
			}
		}
	};
	
	private static final HotKey SAVE = new HotKey(CTRL_S)
	{
		public void hotkeyTyped()
		{
			JFileChooser fileChooser = new JFileChooser(".");
			fileChooser.setFileFilter(chooserFilter);
			
			int result = fileChooser.showSaveDialog(frame);
			
			if (result == JFileChooser.APPROVE_OPTION)
			{
				File mapFile = fileChooser.getSelectedFile();
				
				if (panel.map != null)
				{
					try
					{
						if (!mapFile.getName().endsWith(".jom"))
						{
							mapFile = new File(mapFile.getAbsolutePath() + ".jom");
						}
							
						panel.map.save(mapFile);
						frame.setTitle("Mapper - " + mapFile.getName());
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}
	};

	private static final HotKey OPEN = new HotKey(CTRL_O)
	{
		public void hotkeyTyped()
		{
			JFileChooser fileChooser = new JFileChooser(".");
			fileChooser.setFileFilter(chooserFilter);
			
			int result = fileChooser.showOpenDialog(frame);
			
			if (result == JFileChooser.APPROVE_OPTION)
			{
				File mapFile = fileChooser.getSelectedFile();
				
				Map openedMap = null;
				
				try
				{
					openedMap = Map.load(mapFile);
					frame.setTitle("Mapper - " + mapFile.getName());
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				panel.setTileSize(32);
				panel.setMap(openedMap);
			}
		}
	};

	private static final HotKey NEW = new HotKey(CTRL_N)
	{
		public void hotkeyTyped()
		{
			String wString = JOptionPane.showInputDialog(frame, "Width?");
			
			if (wString == null)
				return;
			
			String hString = JOptionPane.showInputDialog(frame, "Height?");
			
			if (hString == null)
				return;
			
			int w;
			int h;
			
			try
			{
				w = (wString.trim().length() > 0) ? Integer.parseInt(wString) : 8;
				h = (hString.trim().length() > 0) ? Integer.parseInt(hString) : 8;
			}
			catch (NumberFormatException nfe)
			{
				JOptionPane.showMessageDialog(frame, "Not a number");
				return;
			}
			
			if (w < 4 || h < 4)
			{
				JOptionPane.showMessageDialog(frame, "Must be at least 4x4");
				return;
			}
			
			panel.setTileSize(32);
			panel.setMap(new Map(w, h));
			frame.setTitle("Mapper - [Untitled]");
		}
	};
	
	private static final FileFilter chooserFilter = new FileFilter()
	{
		public boolean accept(File file)
		{
			return file.getName().endsWith(".jom") || file.isDirectory();
		}
		
		public String getDescription()
		{
			return "JOutpost Map Files (*.jom)";
		}
	};
}
