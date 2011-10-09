package com.robbix.mp5.sb;

import java.awt.BorderLayout;
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
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import com.robbix.mp5.Game;
import com.robbix.mp5.Mediator;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.ui.SoundBank;

public class SoundPlayer extends JFrame
{
	public static void main(String[] args) throws IOException
	{
		Sandbox.trySystemLookAndFeel();
		SoundBank sounds = SoundBank.preload(new File("./res/sounds"));
		Mediator.sounds = sounds;
		JFrame sPlayer = new SoundPlayer(Game.of(sounds));
		sPlayer.setIconImages(Sandbox.getWindowIcons());
		sPlayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sPlayer.setVisible(true);
	}
	
	private static final long serialVersionUID = 1L;
	
	private SoundBank sounds;
	private JList soundList;
	private DefaultListModel listModel;
	
	public SoundPlayer(Game game)
	{
		super("Sound Player");
		sounds = game.getSoundBank();
		sounds.addModuleListener(new ModuleListener(){
			public void moduleLoaded(ModuleEvent e){buildList();}
			public void moduleUnloaded(ModuleEvent e){buildList();}
		});
		listModel = new DefaultListModel();
		soundList = new JList(listModel);
		soundList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildList();
		final JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				play(soundList.getSelectedValue());
			}
		});
		soundList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					play(soundList.getSelectedValue());
				}
			}
		});
		soundList.addKeyListener(new KeyAdapter()
		{
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					play(soundList.getSelectedValue());
				}
			}
		});
		JToolBar controlPanel = new JToolBar();
		controlPanel.setFloatable(false);
		controlPanel.add(playButton);
		setLayout(new BorderLayout());
		add(controlPanel, BorderLayout.NORTH);
		add(new JScrollPane(soundList), BorderLayout.CENTER);
		setSize(200, 250);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void play(Object name)
	{
		if (name != null)
		{
			sounds.playAnyway(name.toString());
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
