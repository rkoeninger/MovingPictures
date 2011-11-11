package com.robbix.mp5.sb;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
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
		Properties props = System.getProperties();
		
		for (Object key : props.keySet())
		{
			textArea.append(key.toString());
			textArea.append("=\"");
			textArea.append(props.get(key).toString());
			textArea.append("\"\n");
		}
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ButtonListener());
		
		setLayout(new BorderLayout());
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		add(okButton, BorderLayout.SOUTH);
		setSize(400, 250);
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
