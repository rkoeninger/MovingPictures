package com.robbix.utils;

import static com.robbix.utils.Direction.E;
import static com.robbix.utils.Direction.N;
import static com.robbix.utils.Direction.S;
import static com.robbix.utils.Direction.W;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Methods that accept robbix/utils objects as parameters refer to
 * this RGraphics' GridMetrics to determine placement.
 */
public class RGraphics extends Graphics2D
{
	private Graphics2D g;
	private GridMetrics gm;
	
	public RGraphics(Graphics2D g)
	{
		this.g = g;
		this.gm = new GridMetrics();
	}
	
	public RGraphics create()
	{
		Graphics created = g.create();
		
		if (created instanceof Graphics2D)
			return new RGraphics((Graphics2D) created);
		
		throw new ClassCastException("Only works for Graphics2D");
	}
	
	public void setGridMetrics(GridMetrics gridMetrics)
	{
		this.gm = gridMetrics != null ? gridMetrics : new GridMetrics();
	}
	
	public GridMetrics getGridMetrics()
	{
		return gm;
	}
	
	public void drawRect(Rectangle rect)
	{
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void fillRect(Rectangle rect)
	{
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void drawLine(Point a, Point b)
	{
		g.drawLine(a.x, a.y, b.x, b.y);
	}
	
	public void drawPosition(Position pos)
	{
		int x = pos.x * gm.tileSize + gm.xOffset;
		int y = pos.y * gm.tileSize + gm.yOffset;
		g.drawRect(x, y, gm.tileSize, gm.tileSize);
	}
	
	public void fillPosition(Position pos)
	{
		int x = pos.x * gm.tileSize + gm.xOffset;
		int y = pos.y * gm.tileSize + gm.yOffset;
		g.fillRect(x, y, gm.tileSize, gm.tileSize);
	}
	
	public void drawRegion(Region region)
	{
		int x = region.x * gm.tileSize + gm.xOffset;
		int y = region.y * gm.tileSize + gm.yOffset;
		g.drawRect(x, y, region.w * gm.tileSize, region.h * gm.tileSize);
	}
	
	public void fillRegion(Region region)
	{
		int x = region.x * gm.tileSize + gm.xOffset;
		int y = region.y * gm.tileSize + gm.yOffset;
		g.fillRect(x, y, region.w * gm.tileSize, region.h * gm.tileSize);
	}
	
	public void drawBorderRegion(BorderRegion region)
	{
		int x = region.x * gm.tileSize + gm.xOffset;
		int y = region.y * gm.tileSize + gm.yOffset;
		g.drawRect(x, y, region.w * gm.tileSize, region.h * gm.tileSize);
		
		if (region.w > 2 && region.h > 2)
		{
			g.drawRect(
				(region.x + 1) * gm.tileSize + gm.xOffset,
				(region.y + 1) * gm.tileSize + gm.yOffset,
				(region.w - 2) * gm.tileSize,
				(region.h - 2) * gm.tileSize
			);
		}
	}
	
	public void fillBorderRegion(BorderRegion region)
	{
		g.fillRect(
			region.x * gm.tileSize + gm.xOffset,
			region.y * gm.tileSize + gm.yOffset,
			region.w * gm.tileSize,
			gm.tileSize
		);
		
		if (region.h > 1)
		{
			g.fillRect(
				region.x * gm.tileSize + gm.xOffset,
				(region.y + region.h - 1) * gm.tileSize + gm.yOffset,
				region.w * gm.tileSize,
				gm.tileSize
			);
		}
		
		if (region.h > 2)
		{
			g.fillRect(
				region.x * gm.tileSize + gm.xOffset,
				(region.y + 1) * gm.tileSize + gm.yOffset,
				gm.tileSize,
				(region.h - 2) * gm.tileSize
			);
			
			if (region.w > 1)
			{
				g.fillRect(
					(region.x + region.w - 1) * gm.tileSize + gm.xOffset,
					(region.y + 1) * gm.tileSize + gm.yOffset,
					gm.tileSize,
					(region.h - 2) * gm.tileSize
				);
			}
		}
	}
	
	public void drawLShapedRegion(LShapedRegion region)
	{
		drawEdges(region.getFirstEnd(), Neighbors.allBut(region.getFirstLegDirection()));
		drawEdges(region.getSecondEnd(), Neighbors.allBut(region.getSecondLegDirection()));
		drawEdge(region.getElbow(), region.getFirstLegDirection());
		drawEdge(region.getElbow(), region.getSecondLegDirection());
		drawLeg(region.getElbow(), region.getFirstEnd());
		drawLeg(region.getElbow(), region.getSecondEnd());
	}
	
	private void drawLeg(Position from, Position to)
	{
		int tx0 = to.x * gm.tileSize + gm.xOffset;
		int ty0 = to.y * gm.tileSize + gm.yOffset;
		int tx1 = tx0 + gm.tileSize;
		int ty1 = ty0 + gm.tileSize;
		
		int fx0 = from.x * gm.tileSize + gm.xOffset;
		int fy0 = from.y * gm.tileSize + gm.yOffset;
		int fx1 = fx0 + gm.tileSize;
		int fy1 = fy0 + gm.tileSize;
		
		switch (Direction.getDirection(from, to))
		{
		case N: g.drawLine(fx0, fy0, tx0, ty1); g.drawLine(fx1, fy0, tx1, ty1); break;
		case S: g.drawLine(fx0, fy1, tx0, ty0); g.drawLine(fx1, fy1, tx1, ty0); break;
		case E: g.drawLine(fx1, fy0, tx0, ty0); g.drawLine(fx1, fy1, tx0, ty1); break;
		case W: g.drawLine(fx0, fy0, tx1, ty0); g.drawLine(fx0, fy1, tx1, ty1); break;
		}
	}
	
	public void fillLShapedRegion(LShapedRegion region)
	{
		fillPosition(region.getFirstEnd());
		fillPosition(region.getSecondEnd());
		fillPosition(region.getElbow());
		fillLeg(region.getElbow(), region.getFirstEnd());
		fillLeg(region.getElbow(), region.getSecondEnd());
	}
	
	private void fillLeg(Position from, Position to)
	{
		int tx0 = to.x * gm.tileSize + gm.xOffset;
		int ty0 = to.y * gm.tileSize + gm.yOffset;
		int tx1 = tx0 + gm.tileSize;
		int ty1 = ty0 + gm.tileSize;
		
		int fx0 = from.x * gm.tileSize + gm.xOffset;
		int fy0 = from.y * gm.tileSize + gm.yOffset;
		int fx1 = fx0 + gm.tileSize;
		int fy1 = fy0 + gm.tileSize;
		
		switch (Direction.getDirection(from, to))
		{
		case N: g.fillRect(tx0, ty1, fx1 - fx0, fy0 - ty1); break;
		case S: g.fillRect(fx0, fy1, fx1 - fx0, ty0 - fy1); break;
		case E: g.fillRect(fx1, fy0, tx0 - fx1, ty1 - ty0); break;
		case W: g.fillRect(tx1, ty0, fx0 - tx1, ty1 - ty0); break;
		}
	}
	
	private void drawEdge(Position pos, Direction dir)
	{
		int x0 = pos.x * gm.tileSize + gm.xOffset;
		int y0 = pos.y * gm.tileSize + gm.yOffset;
		int x1 = (pos.x + 1) * gm.tileSize + gm.xOffset;
		int y1 = (pos.y + 1) * gm.tileSize + gm.yOffset;
		
		switch (dir)
		{
		case N: g.drawLine(x0, y0, x1, y0); break;
		case S: g.drawLine(x0, y1, x1, y1); break;
		case E: g.drawLine(x1, y0, x1, y1); break;
		case W: g.drawLine(x0, y0, x0, y1); break;
		}
	}
	
	private void drawEdges(Position pos, Neighbors neighbors)
	{
		if (neighbors.has(N)) drawEdge(pos, N);
		if (neighbors.has(S)) drawEdge(pos, S);
		if (neighbors.has(E)) drawEdge(pos, E);
		if (neighbors.has(W)) drawEdge(pos, W);
	}
	
	public void drawImage(Image img, Position pos, int xOffset, int yOffset)
	{
		int x = pos.x * gm.tileSize + gm.xOffset + xOffset;
		int y = pos.y * gm.tileSize + gm.yOffset + yOffset; 
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		int w2 = gm.scale < 0 ? w >> -gm.scale : w << gm.scale;
		int h2 = gm.scale < 0 ? h >> -gm.scale : h << gm.scale;
		g.drawImage(
			img,
			x, y, x + w2, y + h2,
			0, 0, w, h,
			null
		);
	}
	
	public void drawImage(Image img, Position pos)
	{
		drawImage(img, pos, 0, 0);
	}
	
	public void drawImage(Image img, Position pos, Offset offset)
	{
		drawImage(img, pos, offset.dx, offset.dy);
	}
	
	public void drawString(String str, Position pos)
	{
		int x = pos.x * gm.tileSize + (gm.tileSize / 2) + gm.xOffset;
		int y = pos.y * gm.tileSize + (gm.tileSize / 2) + gm.yOffset;
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, g);
		int cx = (int) bounds.getCenterX();
		int cy = (int) bounds.getCenterY();
		g.drawString(str, x - cx, y - cy);
	}
	
	public void drawString(String str, Region region)
	{
		int w = region.w / 2 * gm.tileSize + (region.w % 2 == 0 ? 0 : gm.tileSize / 2);
		int h = region.h / 2 * gm.tileSize + (region.h % 2 == 0 ? 0 : gm.tileSize / 2);
		int x = region.x * gm.tileSize + w + gm.xOffset;
		int y = region.y * gm.tileSize + h + gm.yOffset;
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(str, g);
		int cx = (int) bounds.getCenterX();
		int cy = (int) bounds.getCenterY();
		g.drawString(str, x - cx, y - cy);
	}
	
	/*
	 * Boiler-plate methods. Implementations of abstract methods in
	 * Graphics and Graphics2D that are simply forwarded to the decorated
	 * Graphics2D object.
	 */
	public void draw(Shape s) { g.draw(s); }
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) { return g.drawImage(img, xform, obs); }
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) { g.drawImage(img, op, x, y); }
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) { g.drawRenderedImage(img, xform); }
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) { g.drawRenderableImage(img, xform); }
	public void drawString(String str, int x, int y) { g.drawString(str, x, y); }
	public void drawString(String str, float x, float y) { g.drawString(str, x, y); }
	public void drawString(AttributedCharacterIterator iterator, int x, int y) { g.drawString(iterator, x, y); }
	public void drawString(AttributedCharacterIterator iterator, float x, float y) { g.drawString(iterator, x, y); }
	public void drawGlyphVector(GlyphVector gv, float x, float y) { g.drawGlyphVector(gv, x, y); }
	public void fill(Shape s) { g.fill(s); }
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) { return g.hit(rect, s, onStroke); }
	public GraphicsConfiguration getDeviceConfiguration() { return g.getDeviceConfiguration(); }
	public void setComposite(Composite comp) { g.setComposite(comp); }
	public void setPaint(Paint paint) { g.setPaint(paint); }
	public void setStroke(Stroke s) { g.setStroke(s); }
	public void setRenderingHint(Key hintKey, Object hintValue) { g.setRenderingHint(hintKey, hintValue); }
	public Object getRenderingHint(Key hintKey) { return g.getRenderingHint(hintKey); }
	public void setRenderingHints(Map<?, ?> hints) { g.setRenderingHints(hints); }
	public void addRenderingHints(Map<?, ?> hints) { g.addRenderingHints(hints); }
	public RenderingHints getRenderingHints() { return g.getRenderingHints(); } 
	public void translate(int x, int y) { g.translate(x, y); }
	public void translate(double tx, double ty) { g.translate(tx, ty); }
	public void rotate(double theta) { g.rotate(theta); }
	public void rotate(double theta, double x, double y) { g.rotate(theta, x, y); }
	public void scale(double sx, double sy) { g.scale(sx, sy); }
	public void shear(double shx, double shy) { g.shear(shx, shy); }
	public void transform(AffineTransform Tx) { g.transform(Tx); }
	public void setTransform(AffineTransform Tx) { g.setTransform(Tx); }
	public AffineTransform getTransform() { return g.getTransform(); }
	public Paint getPaint() { return g.getPaint(); }
	public Composite getComposite() { return g.getComposite(); }
	public void setBackground(Color color) { g.setBackground(color); }
	public Color getBackground() { return g.getBackground(); }
	public Stroke getStroke() { return g.getStroke(); }
	public void clip(Shape s) { g.clip(s); }
	public FontRenderContext getFontRenderContext() { return g.getFontRenderContext(); }
	public Color getColor() { return g.getColor(); }
	public void setColor(Color c) { g.setColor(c); }
	public void setPaintMode() { g.setPaintMode(); }
	public void setXORMode(Color c1) { g.setXORMode(c1); }
	public Font getFont() { return g.getFont(); }
	public void setFont(Font font) { g.setFont(font); }
	public FontMetrics getFontMetrics(Font f) { return g.getFontMetrics(f); }
	public Rectangle getClipBounds() { return g.getClipBounds(); }
	public void clipRect(int x, int y, int width, int height) { g.clipRect(x, y, width, height); }
	public void setClip(int x, int y, int width, int height) { g.setClip(x, y, width, height); }
	public Shape getClip() { return g.getClip(); }
	public void setClip(Shape clip) { g.setClip(clip); }
	public void copyArea(int x, int y, int width, int height, int dx, int dy) { g.copyArea(x, y, width, height, dx, dy); }
	public void drawLine(int x1, int y1, int x2, int y2) { g.drawLine(x1, y1, x2, y2); }
	public void fillRect(int x, int y, int width, int height) { g.fillRect(x, y, width, height); }
	public void clearRect(int x, int y, int width, int height) { g.clearRect(x, y, width, height); }
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) { g.drawRoundRect(x, y, width, height, arcWidth, arcHeight); }
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) { g.fillRoundRect(x, y, width, height, arcWidth, arcHeight); }
	public void drawOval(int x, int y, int width, int height) { g.drawOval(x, y, width, height); }
	public void fillOval(int x, int y, int width, int height) { g.fillOval(x, y, width, height); }
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) { g.drawArc(x, y, width, height, startAngle, arcAngle); }
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) { g.fillArc(x, y, width, height, startAngle, arcAngle); }
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) { g.drawPolyline(xPoints, yPoints, nPoints); }
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) { g.drawPolygon(xPoints, yPoints, nPoints); }
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) { g.fillPolygon(xPoints, yPoints, nPoints); }
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) { return g.drawImage(img, x, y, observer); }
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) { return g.drawImage(img, x, y, width, height, observer); }
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) { return g.drawImage(img, x, y, bgcolor, observer); }
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) { return g.drawImage(img, x, y, width, height, bgcolor, observer); }
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) { return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer); }
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) { return g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer); }
	public void dispose() { g.dispose(); }
}
