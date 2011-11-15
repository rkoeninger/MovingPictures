package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AboutDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	public static void showDialog(Frame owner, boolean modal)
	{
		AboutDialog dialog = new AboutDialog(owner, modal);
		dialog.setVisible(true);
	}
	
	private AboutDialog(Frame owner, boolean modal)
	{
		super(owner, "About Moving Pictures", modal);
		JTextArea textArea = new JTextArea();
		textArea.append("Java Version: ");
		textArea.append(System.getProperty("java.runtime.version", "Unkown"));
		textArea.append("\n");
		textArea.append("Java Home: ");
		textArea.append(System.getProperty("sun.boot.library.path", "Unkown"));
		textArea.append("\n");
		textArea.append("Operating System: ");
		textArea.append(System.getProperty("os.name", "Unkown"));
		textArea.append("\n");
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ButtonListener());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(okButton);
		
		setLayout(new BorderLayout());
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		setSize(440, 180);
		setLocationRelativeTo(owner);
	}
	
	private class ButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			AboutDialog.this.setVisible(false);
		}
	}
}
