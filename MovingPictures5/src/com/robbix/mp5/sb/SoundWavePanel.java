package com.robbix.mp5.sb;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import javax.swing.JComponent;

import com.robbix.mp5.utils.SampleBuffer;
import com.robbix.mp5.utils.SampleStream;

public class SoundWavePanel extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	private String defaultMessage;
	private String message;
	private SampleBuffer buffer;
	private double playbackProgress = -100;
	private boolean grid = true;
	
	public SoundWavePanel(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
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
		
		if (buffer.getChannelCount() == 1)
		{
			this.buffer = this.buffer.copy();
			this.buffer.mixToMono();
		}
		
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
		return new SampleStream.Callback()
		{
			public void starting()
			{
				playbackProgress = 0;
				repaint();
			}
			
			public void progress(double progress)
			{
				playbackProgress = progress;
				repaint();
			}
			
			public void complete()
			{
				playbackProgress = -100;
				repaint();
			}
		};
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
	private static Color PLAYHEAD = new Color(159, 63, 15);
	
	private void drawBuffer(Graphics g, SampleBuffer buffer)
	{
		int w = getWidth();
		int h = getHeight();
		
		if (grid)
		{
			g.setColor(Color.DARK_GRAY);
			g.drawLine(0, h / 2, w - 1, h / 2); // zero-line
		}
		
		g.setColor(WAVE);
		drawWave(g, buffer.getChannel(0));
		
		if (playbackProgress >= 0)
		{
			g.setColor(PLAYHEAD);
			int xProgress = (int) (playbackProgress * w);
			g.drawLine(xProgress, 0, xProgress, h);
		}
		
		g.setColor(Color.BLACK);
		drawDuration(g, buffer);
	}
	
	private void drawWave(Graphics g, float[] wave)
	{
		int w = getWidth();
		int h = getHeight();
		
		float positionRatio = wave.length / (float) w;
		int iPrevious = 0;
		int xPrevious = 0;
		int yPrevious = h / 2;
		
		for (int x = 0; x < w; ++x)
		{
			int i = (int) (x * positionRatio);
			
			if (i != iPrevious)
			{
				int y = (int) (((wave[i] * h) + h) / 2);
				g.drawLine(x, y, xPrevious, yPrevious);
				yPrevious = y;
				xPrevious = x;
			}
			
			iPrevious = i;
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
	
	public void setGridVisible(boolean grid)
	{
		this.grid = grid;
		repaint();
	}
	
	public boolean isGridVisible()
	{
		return grid;
	}
}
