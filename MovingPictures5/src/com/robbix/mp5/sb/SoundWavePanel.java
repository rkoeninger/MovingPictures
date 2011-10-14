package com.robbix.mp5.sb;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.JComponent;

import com.robbix.mp5.basics.SampleBuffer;
import com.robbix.mp5.basics.SampleStream;

public class SoundWavePanel extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	private String defaultMessage;
	private String message;
	private SampleBuffer buffer;
	private SampleStream.Callback callback;
	private double playbackProgress = -100;
	
	public SoundWavePanel(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
		
		callback = new SampleStream.Callback()
		{
			public void starting()
			{
				playbackProgress = 0;
				System.out.println("starting");
				repaint();
			}
			
			public void progress(double progress)
			{
				playbackProgress = progress;
				System.out.println(progress);
				repaint();
			}
			
			public void complete()
			{
				playbackProgress = -100;
				System.out.println("complete");
				repaint();
			}
		};
	}
	
	public void show(String message)
	{
		this.message = message;
		buffer = null;
		repaint();
	}
	
	public void show(SampleBuffer buffer)
	{
		this.buffer = buffer;
		message = null;
		repaint();
	}
	
	public void showNothing()
	{
		message = null;
		buffer = null;
		repaint();
	}
	
	public SampleStream.Callback getCallback()
	{
		return callback;
	}
	
	public void paintComponent(Graphics g)
	{
		if (buffer != null)
		{
			drawBuffer(g, buffer);
		}
		else
		{
			drawString(g, message != null ? message : defaultMessage);
		}
	}
	
	private static Color WAVE = new Color(0, 47, 127);
	private static Color PLAYHEAD = new Color(127, 47, 15);
	
	private void drawBuffer(Graphics g, SampleBuffer buffer)
	{
		float[] samples = buffer.getChannel(0);
		int w = getWidth();
		int h = getHeight();
		float positionRatio = w / (float) samples.length;
		int xPrevious = 0;
		int yPrevious = h / 2;
		
		g.setColor(Color.BLACK);
		drawDuration(g, buffer);
		g.setColor(Color.DARK_GRAY);
		g.drawLine(0, h / 2, w - 1, h / 2);
		g.setColor(WAVE);
		
		for (int i = 0; i < samples.length; ++i)
		{
			int x = (int) (i * positionRatio);
			
			if (x != xPrevious)
			{
				int y = (int) (((samples[i] * h) + h) / 2);
				g.drawLine(x, y, xPrevious, yPrevious);
				yPrevious = y;
			}
			
			xPrevious = x;
		}
		
		if (playbackProgress >= 0)
		{
			g.setColor(PLAYHEAD);
			int xProgress = (int) (playbackProgress * w);
			g.drawLine(xProgress, 0, xProgress, h);
		}
	}
	
	private void drawString(Graphics g, String message)
	{
		g.setColor(Color.BLACK);
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D rect = metrics.getStringBounds(message, g);
		g.drawString(
			message,
			(int) (getWidth()  / 2 - rect.getCenterX()),
			(int) (getHeight() / 2 - rect.getCenterY())
		);
	}
	
	private static DecimalFormat formatter = new DecimalFormat("0.00");
	
	private void drawDuration(Graphics g, SampleBuffer buffer)
	{
		int w = getWidth();
		int sampleLength = buffer.length();
		float rate = buffer.getSampleRate();
		String durString = formatter.format(sampleLength / rate);
		durString += "s";
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D rect = metrics.getStringBounds(durString, g);
		g.drawString(durString, (int)(w - rect.getWidth() - 4), (int)(4 - rect.getY()));
	}
}
