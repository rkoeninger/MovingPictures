import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TileTree
{
	private TileTreeNode root = new TileTreeNode(true);
	
	public void put(Tile tile)
	{
		root.getEnsureNode(tile.familyNE, true)
			.getEnsureNode(tile.familyNW, true)
			.getEnsureNode(tile.familySW, true)
			.getEnsureNode(tile.familySE, false).getTiles().add(tile);
	}

	public List<Tile> get(int familyNE, int familyNW, int familySW, int familySE)
	{
		return root.getEnsureNode(familyNE, true)
				   .getEnsureNode(familyNW, true)
				   .getEnsureNode(familySW, true)
				   .getEnsureNode(familySE, false).getTiles();
	}
	
	private static class TileTreeNode
	{
		private Map<Integer, TileTreeNode> branches;
		private ArrayList<Tile> leaf;
		
		public TileTreeNode(boolean branch)
		{
			if (branch)
			{
				branches = new HashMap<Integer, TileTreeNode>();
			}
			else
			{
				leaf = new ArrayList<Tile>();
			}
		}
		
		public boolean isBranch()
		{
			return branches != null;
		}
		
		public TileTreeNode getEnsureNode(int family, boolean branch)
		{
			TileTreeNode node = branches.get(family);
			
			if (node != null)
			{
				if (node.isBranch() != branch)
					throw new Error("was supposed to be a branch");
				
				return node;
			}
			
			node = new TileTreeNode(branch);
			branches.put(family, node);
			return node;
		}
//		
//		public TileTreeNode addNode(int family, boolean branch)
//		{
//			TileTreeNode node = new TileTreeNode(branch);
//			branches.put(family, node);
//			return node;
//		}
//		
//		public TileTreeNode getNode(int family)
//		{
//			return branches.get(family);
//		}
//		
		public List<Tile> getTiles()
		{
			if (leaf == null) throw new NullPointerException();
			
			return leaf;
		}
	}
}
