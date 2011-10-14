package com.robbix.mp5.sb;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.robbix.mp5.Utils;
import com.robbix.mp5.ui.EnumSpriteGroup;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.unit.HealthBracket;

public class SpritePanel extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	private String defaultMessage;
	private String message;
	private Sprite[] sprites;
	private SpriteGroup group;
	
	private Timer timer;
	private int frame = 0;
	private int hue = 240;
	private int tileSize = 32;
	private int fpw;
	private int fph;
	private boolean grid = true;
	
	public SpritePanel(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
		timer = new Timer(50, new DoAnimation());
	}
	
	public void show(String message)
	{
		this.message = message;
		sprites = null;
		group = null;
		
		timer.stop();
		repaint();
	}
	
	public void show(Sprite sprite, int fpWidth, int fpHeight)
	{
		this.sprites = new Sprite[]{sprite};
		message = null;
		group = null;
		
		fpw = fpWidth;
		fph = fpHeight;
		
		timer.stop();
		repaint();
	}
	
	public void show(Sprite[] sprites, int fpWidth, int fpHeight)
	{
		this.sprites = sprites;
		message = null;
		group = null;
		
		fpw = fpWidth;
		fph = fpHeight;
		
		timer.stop();
		repaint();
	}
	
	public void show(SpriteGroup group, int fpWidth, int fpHeight)
	{
		if (group instanceof EnumSpriteGroup)
		{
			EnumSpriteGroup<?> enumGroup = (EnumSpriteGroup<?>) group;
			
			if (enumGroup.getEnumType().equals(HealthBracket.class))
			{
				show(group.getFrame(HealthBracket.GREEN.ordinal()), fpWidth, fpHeight);
				return;
			}
		}
		
		this.group = group;
		message = null;
		sprites = null;
		frame = 0;
		
		fpw = fpWidth;
		fph = fpHeight;
		
		if (group.getSpriteCount() > 1)
			timer.start();
		
		repaint();
	}
	
	public void showNothing()
	{
		message = null;
		sprites = null;
		group = null;
		
		timer.stop();
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		if (group != null)
		{
			drawBackground(g);
			drawSprite(g, group.getFrame(frame % group.getFrameCount()));
			
			if (timer.isRunning())
			{
				drawTimer(g, timer);
			}
		}
		else if (sprites != null)
		{
			drawBackground(g);
			
			for (Sprite sprite : sprites)
				drawSprite(g, sprite);
		}
		else
		{
			drawString(g, message != null ? message : defaultMessage);
		}
	}
	
	private void drawBackground(Graphics g)
	{
		g.setColor(Color.DARK_GRAY);
		
		if (grid)
		{
			if (fpw == 0 && fph == 0) drawCrosshair(g);
			                     else drawGrid(g, fpw, fph);
		}
	}
	
	private void drawCrosshair(Graphics g)
	{
		g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
		g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
	}
	
	private void drawGrid(Graphics g, int gridW, int gridH)
	{
		int totalFPW = gridW * tileSize;
		int totalFPH = gridH * tileSize;
		int x = (getWidth()  - totalFPW) / 2;
		int y = (getHeight() - totalFPH) / 2;
		
		for (int fpx = 0; fpx <= gridW; ++fpx)
			g.drawLine(
				x + fpx * tileSize, y - (tileSize/2),
				x + fpx * tileSize, y + gridH * tileSize + (tileSize/2)
			);
		
		for (int fpy = 0; fpy <= gridH; ++fpy)
			g.drawLine(
				x - (tileSize/2), y + fpy * tileSize,
				x + gridW * tileSize + (tileSize/2), y + fpy * tileSize
			);
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
	
	private void drawSprite(Graphics g, Sprite sprite)
	{
		boolean centered = fpw == 0 && fph == 0;
		int w = centered ? 0 : fpw * tileSize;
		int h = centered ? 0 : fph * tileSize;
		Image img = sprite.getImage();
		int baseHue = sprite.getBaseTeamHue();
		if (baseHue != hue && baseHue >= 0 && baseHue < 360)
			img = Utils.recolorUnit((BufferedImage) img, baseHue, hue);
		int x = (getWidth()  - w) / 2 + sprite.getXOffset();
		int y = (getHeight() - h) / 2 + sprite.getYOffset();
		g.drawImage(img, x, y, null);
	}
	
	private static DecimalFormat formatter = new DecimalFormat("0.00");
	
	private void drawTimer(Graphics g, Timer timer)
	{
		String fpsString = timer.getDelay() == 0
			? "Unlimited"
			: formatter.format(1000.0 / timer.getDelay());
		fpsString = "fps = " + fpsString;
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D rect = metrics.getStringBounds(fpsString, g);
		g.drawString(fpsString, (int)(4 - rect.getX()), (int)(4 - rect.getY()));
	}
	
	private class DoAnimation implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			frame++;
			repaint();
		}
	}
	
	public void dispose()
	{
		timer.stop();
	}
	
	public void setDelay(int millis)
	{
		timer.setInitialDelay(millis);
		timer.setDelay(millis);
	}
	
	public int getDelay()
	{
		return timer.getDelay();
	}
	
	public void setHue(int hue)
	{
		this.hue = hue;
	}
	
	public int getHue()
	{
		return hue;
	}
	
	public void setGridVisible(boolean grid)
	{
		this.grid = grid;
	}
	
	public boolean isGridVisible()
	{
		return grid;
	}
}
