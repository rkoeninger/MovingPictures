import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

public class Tile
{
	public static boolean blendOnDownsample = true;
	public static boolean antialiasOnUpsample = false;//TODO: fix
	
	public static final Tile NULL = new Tile(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB), 0);
	
	public final int familyNE, familyNW, familySW, familySE;
	
	private Map<Integer, Image> scaledImages = new HashMap<Integer, Image>();
	
	private TileGroup group;
	
	public void setGroup(TileGroup group)
	{
		this.group = group;
	}
	
	public TileGroup getGroup()
	{
		return group;
	}
	
	public Point getGroupPosition()
	{
		if (group == null)
			return null;
		
		return group.getPosition(this);
	}
	
	public boolean isGroupMember()
	{
		return group != null;
	}
	
	public Tile(Image img, int familyNE, int familyNW, int familySW, int familySE)
	{
		scaledImages.put(0, img);
		this.familyNE = familyNE;
		this.familyNW = familyNW;
		this.familySW = familySW;
		this.familySE = familySE;
	}
	
	public Tile(Image img, int family)
	{
		scaledImages.put(0, img);
		this.familyNE = family;
		this.familyNW = family;
		this.familySW = family;
		this.familySE = family;
	}
	
	public int getFamily()
	{
		if (isTransition())
			throw new IllegalStateException("not a plain tile");
		
		return familyNE;
	}
	
	public int getNEFamily()
	{
		return familyNE;
	}

	public int getNWFamily()
	{
		return familyNW;
	}

	public int getSWFamily()
	{
		return familySW;
	}

	public int getSEFamily()
	{
		return familySE;
	}
	
	public boolean isPlain()
	{
		return familyNE == familyNW
			&& familyNW == familySW
			&& familySW == familySE;
	}
	
	public boolean isTransition()
	{
		return !isPlain();
	}
	
	public Image getImage()
	{
		return getImage(0);
	}
	
	/**
	 * @param scale
	 * 
	 *     -2 = 0.25x
	 *     -1 = 0.5x
	 *     +0 = 1x
	 *     +1 = 2x
	 *     +2 = 4x
	 *     
	 *     etc.
	 */
	public Image getImage(int scale)
	{
		BufferedImage scaledImage = (BufferedImage) scaledImages.get(scale);
		
		if (scaledImage != null)
			return scaledImage;
		
		if (scale == 0)
			throw new Error("no base image");
		
		if (scale > 0)
		{
			/*
			 * Get image that's half as big and scale up by two
			 */
			BufferedImage baseImage = (BufferedImage) getImage(scale - 1);
			Raster baseRaster = baseImage.getRaster();

			scaledImage = new BufferedImage(32 << scale, 32 << scale, BufferedImage.TYPE_INT_ARGB);
			WritableRaster scaledRaster = scaledImage.getRaster();
			int[] pixel1 = new int[4];
			int[] pixel2 = new int[4];
			int[] pixel3 = new int[4];
			int[] pixel4 = new int[4];
			pixel1[3] = 255;
			pixel2[3] = 255;
			pixel3[3] = 255;
			pixel4[3] = 255;
			
			for (int x = 0; x < baseImage.getWidth();  ++x)
			for (int y = 0; y < baseImage.getHeight(); ++y)
			{
				if (antialiasOnUpsample)
				{
					baseRaster.getPixel(x, y, pixel1);
					
					if (x < baseImage.getWidth() - 1)
					{
						baseRaster.getPixel(x + 1, y, pixel2);
						
						pixel2[0] = (pixel1[0] + pixel2[0]) / 2;
						pixel2[1] = (pixel1[1] + pixel2[1]) / 2;
						pixel2[2] = (pixel1[2] + pixel2[2]) / 2;
					}
					else
					{
						System.arraycopy(pixel1, 0, pixel2, 0, 4);
					}
					
					if (y < baseImage.getHeight() - 1)
					{
						baseRaster.getPixel(x, y + 1, pixel2);
						
						pixel3[0] = (pixel1[0] + pixel3[0]) / 2;
						pixel3[1] = (pixel1[1] + pixel3[1]) / 2;
						pixel3[2] = (pixel1[2] + pixel3[2]) / 2;
					}
					else
					{
						System.arraycopy(pixel1, 0, pixel3, 0, 4);
					}
					
					if (x < baseImage.getWidth() - 1 && y < baseImage.getHeight() - 1)
					{
						baseRaster.getPixel(x + 1, y + 1, pixel2);
						
						pixel4[0] = (pixel1[0] + pixel4[0]) / 2;
						pixel4[1] = (pixel1[1] + pixel4[1]) / 2;
						pixel4[2] = (pixel1[2] + pixel4[2]) / 2;
					}
					else
					{
						System.arraycopy(pixel1, 0, pixel4, 0, 4);
					}
					
					scaledRaster.setPixel(x * 2    , y * 2    , pixel1);
					scaledRaster.setPixel(x * 2 + 1, y * 2    , pixel2);
					scaledRaster.setPixel(x * 2    , y * 2 + 1, pixel3);
					scaledRaster.setPixel(x * 2 + 1, y * 2 + 1, pixel4);
				}
				else
				{
					baseRaster.getPixel(x, y, pixel1);
					scaledRaster.setPixel(x * 2    , y * 2    , pixel1);
					scaledRaster.setPixel(x * 2 + 1, y * 2    , pixel1);
					scaledRaster.setPixel(x * 2    , y * 2 + 1, pixel1);
					scaledRaster.setPixel(x * 2 + 1, y * 2 + 1, pixel1);
				}
			}
		}
		else
		{
			/*
			 * Get image that's twice as big and scale down by half
			 */
			BufferedImage baseImage = (BufferedImage) getImage(scale + 1);
			Raster baseRaster = baseImage.getRaster();

			scaledImage = new BufferedImage(32 >> -scale, 32 >> -scale, BufferedImage.TYPE_INT_ARGB);
			WritableRaster scaledRaster = scaledImage.getRaster();
			int[] pixel1 = new int[4];
			int[] pixel2 = new int[4];
			int[] pixel3 = new int[4];
			int[] pixel4 = new int[4];
			pixel1[3] = 255;
			pixel2[3] = 255;
			pixel3[3] = 255;
			pixel4[3] = 255;
			
			for (int x = 0; x < scaledImage.getWidth();  ++x)
			for (int y = 0; y < scaledImage.getHeight(); ++y)
			{
				baseRaster.getPixel(x * 2    , y * 2    , pixel1);
				
				if (blendOnDownsample)
				{
					baseRaster.getPixel(x * 2 + 1, y * 2    , pixel2);
					baseRaster.getPixel(x * 2    , y * 2 + 1, pixel3);
					baseRaster.getPixel(x * 2 + 1, y * 2 + 1, pixel4);
					
					pixel1[0] = (pixel1[0] + pixel2[0] + pixel3[0] + pixel4[0]) / 4;
					pixel1[1] = (pixel1[1] + pixel2[1] + pixel3[1] + pixel4[1]) / 4;
					pixel1[2] = (pixel1[2] + pixel2[2] + pixel3[2] + pixel4[2]) / 4;
				}
				
				scaledRaster.setPixel(x, y, pixel1);
			}
		}
		
		scaledImages.put(scale, scaledImage);
		
		return scaledImage;
	}
}
