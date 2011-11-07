package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.ui.SoundBank;
import com.robbix.mp5.utils.ModuleEvent;
import com.robbix.mp5.utils.ModuleListener;

public class SoundPlayer extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		boolean lazy = Arrays.asList(args).contains("-lazyLoadSounds");
		
		Sandbox.trySystemLookAndFeel();
		SoundBank sounds = SoundBank.load(new File("./res/sounds"), lazy);
		Mediator.sounds = sounds;
		JFrame sPlayer = new SoundPlayer(Game.of(sounds));
		sPlayer.setIconImages(Sandbox.getWindowIcons());
		sPlayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sPlayer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SoundBank sounds;
	private JList list;
	private DefaultListModel listModel;
	private SoundWavePanel preview;
	private JPopupMenu unloadPopup;
	
	public SoundPlayer(Game game)
	{
		super("Sound Player");
		unloadPopup = new JPopupMenu();
		preview = new SoundWavePanel("Select a Sound Bite to preview");
		preview.setPreferredSize(new Dimension(150, 100));
		sounds = game.getSoundBank();
		sounds.addModuleListener(new ModuleListener(){
			public void moduleLoaded(ModuleEvent e){buildList();}
			public void moduleUnloaded(ModuleEvent e){buildList();}
		});
		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildList();
		JButton playButton = new JButton("Play");
		JToolBar controlPanel = new JToolBar();
		controlPanel.setFloatable(false);
		controlPanel.add(playButton);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(new JScrollPane(list));
		splitPane.setBottomComponent(preview);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu moduleMenu = new JMenu("Module");
		JMenuItem loadByNameMenuItem = new JMenuItem("By Name...");
		JMenuItem loadFromFileMenuItem = new JMenuItem("From File...");
		JMenuItem unloadMenuItem = new JMenuItem("Unload...");
		moduleMenu.add(loadByNameMenuItem);
		moduleMenu.add(loadFromFileMenuItem);
		moduleMenu.addSeparator();
		moduleMenu.add(unloadMenuItem);
		menuBar.add(moduleMenu);
		setJMenuBar(menuBar);
		
		setLayout(new BorderLayout());
		add(controlPanel, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
		setSize(300, 450);
		splitPane.setDividerLocation(250);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		playButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				play(list.getSelectedValue());
			}
		});
		
		list.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
				{
					play(list.getSelectedValue());
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
					int selectedIndex = list.locationToIndex(e.getPoint());
					list.setSelectedIndex(selectedIndex);
					final Object selected = listModel.getElementAt(selectedIndex);
					Point p = list.getPopupLocation(e);
					JMenuItem unloadMenuItem = new JMenuItem("Unload");
					unloadMenuItem.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							sounds.unloadModule(selected.toString());
							list.setSelectedIndices(new int[0]);
							preview.showNothing();
						}
					});
					unloadPopup.removeAll();
					unloadPopup.add(unloadMenuItem);
					
					if (p == null)
						p = e.getPoint();
					
					unloadPopup.show(list, p.x, p.y);
				}
			}
		});
		
		preview.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					play(list.getSelectedValue());
				}
			}
		});
		
		list.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					play(list.getSelectedValue());
				}
			}
		});
		
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				Object selected = list.getSelectedValue();
				
				if (selected != null)
					preview.show(sounds.getData(selected.toString()));
			}
		});
		
		loadByNameMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String moduleList = JOptionPane.showInputDialog(
					SoundPlayer.this,
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
						sounds.loadModule(moduleName);
					}
					buildList();
					SoundPlayer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SoundPlayer.this,
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
				chooser.setCurrentDirectory(new File("./res/sounds"));
				chooser.setFileFilter(new FileFilter(){
					public boolean accept(File file) {
						return file.getName().endsWith(".wav") ||
							file.isDirectory();
					}
					public String getDescription() {
						return "Sound Files (*.wav)";
					}
				});
				int result = chooser.showOpenDialog(SoundPlayer.this);
				
				if (result != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = chooser.getSelectedFile();
				try
				{
					sounds.loadModule(file);
					buildList();
					SoundPlayer.this.repaint();
				}
				catch (IOException ioe)
				{
					JOptionPane.showMessageDialog(
						SoundPlayer.this,
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
					SoundPlayer.this,
					"Module Name (separate multiple names with commas):",
					"Unload Module(s)",
					JOptionPane.QUESTION_MESSAGE
				);
				
				if (moduleList == null)
					return;
				
				for (String moduleName : moduleList.split(","))
				{
					moduleName = moduleName.trim();
					sounds.unloadModule(moduleName);
				}
				
				buildList();
				SoundPlayer.this.repaint();
			}
		});
	}
	
	private void play(Object name)
	{
		if (name != null)
		{
			sounds.start();
			sounds.play(name.toString(), preview.getCallback());
		}
	}
	
	private void buildList()
	{
		listModel.clear();
		Set<String> modules = sounds.getLoadedModules();
		String[] modulesArray = modules.toArray(new String[0]);
		Arrays.sort(modulesArray);
		
		for (String module : modulesArray)
		{
			listModel.addElement(module);
		}
		
		repaint();
	}
}
