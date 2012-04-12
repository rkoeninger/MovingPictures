import java.awt.Point;
import java.awt.Rectangle;

public class RectangleBuilder
{
	public Rectangle r = null;
	
	public void add(Point p)
	{
		if (r == null)
		{
			r = new Rectangle(p.x, p.y, 1, 1);
		}
		else
		{
			int xMin = Math.min(r.x, p.x);
			int yMin = Math.min(r.y, p.y);
			int xMax = Math.max(r.x + r.width,  p.x + 1);
			int yMax = Math.max(r.y + r.height, p.y + 1);
			r = new Rectangle(xMin, yMin, xMax-xMin, yMax-yMin);
		}
	}
	
	public void add(Rectangle rect)
	{
		if (r == null)
		{
			r = new Rectangle(rect);
		}
		else
		{
			int xMin = Math.min(r.x, rect.x);
			int yMin = Math.min(r.y, rect.y);
			int xMax = Math.max(r.x + r.width,  rect.x + rect.width);
			int yMax = Math.max(r.y + r.height, rect.y + rect.height);
			r = new Rectangle(xMin, yMin, xMax-xMin, yMax-yMin);
		}
	}
}
