import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Direction
{
	public static final Direction E, NE, N, NW, W, SW, S, SE;
	
	public static final List<Direction> dirs;
	
	public final int dx, dy;
	
	private Direction(int dx, int dy)
	{
		this.dx = dx;
		this.dy = dy;
	}
	
	public static Direction get(int dx, int dy)
	{
		for (Direction d : dirs)
			if (d.dx == dx && d.dy == dy)
				return d;
		
		return null;
	}
	
	static
	{
		ArrayList<Direction> tempDirs = new ArrayList<Direction>(8);
		tempDirs.add(E  = new Direction(+1, +0));
		tempDirs.add(NE = new Direction(+1, -1));
		tempDirs.add(N  = new Direction(+0, -1));
		tempDirs.add(NW = new Direction(-1, -1));
		tempDirs.add(W  = new Direction(-1, +0));
		tempDirs.add(SW = new Direction(-1, +1));
		tempDirs.add(S  = new Direction(+0, +1));
		tempDirs.add(SE = new Direction(+1, +1));
		dirs = Collections.unmodifiableList(tempDirs);
	}
}
