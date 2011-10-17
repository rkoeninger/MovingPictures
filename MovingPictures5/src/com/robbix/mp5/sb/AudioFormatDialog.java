package com.robbix.mp5.sb;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.AudioFormat;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.robbix.mp5.ui.SoundBank;

public class AudioFormatDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private static Integer[] sampleRates =
	{
		48000,
		44100,
		22050,
		16000,
		11025,
		8000
	};
	
	private static Integer[] sampleSizes =
	{
		32,
		24,
		16,
		8
	};
	
	public static AudioFormat showDialog(Component parent, AudioFormat initFormat)
	{
		if (initFormat == null)
			initFormat = SoundBank.DEFAULT_OUT_FORMAT;
		
		Frame frame = JOptionPane.getFrameForComponent(parent);
		
		AudioFormatDialog dialog = new AudioFormatDialog(frame, initFormat);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		dialog.dispose();
		
		return dialog.cancelled ? null : dialog.getFormat();
	}
	
	private JComboBox sampleRateComboBox;
	private JComboBox sampleSizeComboBox;
	private JCheckBox monoCheckBox;
	private JCheckBox signedCheckBox;
	private JCheckBox bigEndianCheckBox;
	private JButton okButton;
	private JButton cancelButton;
	
	private boolean cancelled;
	
	private AudioFormatDialog(Frame frame, AudioFormat format)
	{
		super(frame, "Select Audio Format", true);
		
		if (! format.getEncoding().toString().contains("PCM"))
			throw new IllegalArgumentException("Must be PCM");
		
		sampleRateComboBox = new JComboBox(sampleRates);
		sampleRateComboBox.setSelectedItem(44100);
		sampleSizeComboBox = new JComboBox(sampleSizes);
		sampleSizeComboBox.setSelectedItem(16);
		monoCheckBox = new JCheckBox("Mono", false);
		signedCheckBox = new JCheckBox("Signed", true);
		bigEndianCheckBox = new JCheckBox("Big Endian", false);
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		
		justifyInputs();
		
		setLayout(new GridLayout(7, 1));
		add(sampleRateComboBox);
		add(sampleSizeComboBox);
		add(monoCheckBox);
		add(signedCheckBox);
		add(bigEndianCheckBox);
		add(okButton);
		add(cancelButton);
		pack();
		
		sampleSizeComboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				justifyInputs();
			}
		});
		
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelled = false;
				setVisible(false);
			}
		});
		
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelled = true;
				setVisible(false);
			}
		});
	}
	
	public AudioFormat getFormat()
	{
		return new AudioFormat(
			(Integer) sampleRateComboBox.getSelectedItem(),
			(Integer) sampleSizeComboBox.getSelectedItem(),
			monoCheckBox.isSelected() ? 1 : 2,
			signedCheckBox.isSelected(),
			bigEndianCheckBox.isSelected()
		);
	}
	
	private void justifyInputs()
	{
		Object selected = sampleSizeComboBox.getSelectedItem();
		boolean eightBit = Integer.valueOf(8).equals(selected);
		
		bigEndianCheckBox.setEnabled(! eightBit);
		signedCheckBox.setEnabled(eightBit);
		
		if (! eightBit)
			signedCheckBox.setSelected(true);
	}
}
