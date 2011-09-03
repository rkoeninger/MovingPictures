package com.robbix.mp5;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.robbix.mp5.basics.CostMap;
import com.robbix.mp5.basics.FileFormatException;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.unit.Unit;

/**
 * Yes, it's a Utils class. I know.
 * 
 * It has commonly-used methods that don't fit anywhere in particular.
 * 
 * A shadow tint can be acquired by calling something like:
 * <code>
 *     getTranslucency(Color.BLACK, 0.5)
 * </code>
 */
public class Utils
{
	/**
	 * Globally accessible random number generator.
	 */
	public static final Random RNG = new Random();
	
	/**
	 * Returns a random int between [low, high]
	 */
	public static int randInt(int low, int high)
	{
		return low + RNG.nextInt(high - low + 1);
	}
	
	/**
	 * Regular expression specifying zero or more word characters.
	 */
	public static final String REGEX_ANY = "\\w*";
	
	public static boolean getTimeBasedSwitch(int delay, int prob)
	{
		return System.currentTimeMillis() / delay % prob == 0;
	}
	
	public static int getTimeBasedIndex(int delay, int size)
	{
		return (int) (System.currentTimeMillis() / delay % size);
	}
	
	public static final FileFilter BMP = new FileFilter()
	{
		public boolean accept(File file)
		{
			return file.isFile() && file.getName().endsWith(".bmp");
		}
	};
	
	public static Rectangle getWindowBounds()
	{
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
								  .getMaximumWindowBounds();
	}
	
	/**
	 * Attempts to set swing look and feel to the local system's
	 * native theme. Returns true if successful.
	 */
	public static boolean trySystemLookAndFeel()
	{
		try
		{
			String className = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(className);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Returns the provided list of elements as a Set. Duplicate entries in
	 * the list will be dropped.
	 */
	public static <T> Set<T> asSet(T... elements)
	{
		Set<T> set = new HashSet<T>(elements.length);
		
		for (T element : elements)
			set.add(element);
		
		return set;
	}
	
	/**
	 * Compares two Positions for z-order.
	 * 
	 * Positions are first sorted by y-coordinate, second by x-coordinate,
	 * top to bottom, left to right.
	 */
	public static final Comparator<Position> Z_ORDER_POS =
	new Comparator<Position>()
	{
		private final int A =  1;
		private final int B = -1;
		
		public int compare(Position a, Position b)
		{
			if      (a.y > b.y) return A;
			else if (a.y < b.y) return B;
			
			if      (a.x > b.x) return A;
			else if (a.x < b.x) return B;
			
			return 0;
		}
	};

	/**
	 * Compares two Units for z-order.
	 * 
	 * Sorted just like Z_ORDER_POS but all the structures on a row
	 * come after all the vehicles on the same row.
	 */
	public static final Comparator<Unit> Z_ORDER_UNIT =
	new Comparator<Unit>()
	{
		private final int A =  1;
		private final int B = -1;
		
		public int compare(Unit a, Unit b)
		{
			final Position posA = a.getPosition();
			final Position posB = b.getPosition();
			
			if (posA.y > posB.y) return A;
			if (posA.y < posB.y) return B;
			
			final boolean structA = a.isStructure() && !a.isMine();
			final boolean structB = b.isStructure() && !b.isMine();
			
			if ( structA && !structB) return A;
			if (!structA &&  structB) return B;
			
			if (posA.x > posB.x) return A;
			if (posA.x < posB.x) return B;
			
			if (b.isMine()) return A;
			if (a.isMine()) return B;
			
			return 0; // Should this be possible?
		}
	};
	
	/**
	 * Constructs a description path from the given elements.
	 * 
	 * Description paths are typically used in hash tables to
	 * identify sprites, sounds or events based on a set of
	 * criteria.
	 * 
	 * For instance, a sprite description path might look like:
	 * 
	 * <pre>
	 *     getPath(unitType, unitDir, animationFrame) -> "scout/north/3"
	 * </pre>
	 * 
	 * Which represents the sprite for a {@code scout} unit,
	 * facing {@code north} and on the 4th (index = {@code 3})
	 * frame of its movement animation.
	 */
	public static String getPath(Object... parts)
	{
		if (parts == null || parts.length == 0)
			return "";
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < parts.length - 1; ++i)
			if (parts[i] != null)
			{
				String partString = parts[i].toString();
				
				if (partString.matches("\\s"))
					throw new IllegalArgumentException(
						"Path can't have whitespace"
					);
				
				builder.append(partString);
				
				if (parts[i + 1] != null)
					builder.append('/');
			}
		
		Object lastPart = parts[parts.length - 1];
		
		if (lastPart != null)
		{
			if (builder.charAt(builder.length() - 1) != '/')
				builder.append('/');
			
			builder.append(lastPart);
		}
		
		return builder.toString();
	}

	/**
	 * Color object representing total transparency.
	 */
	public static final Color CLEAR = new Color(0, 0, 0, 0);

	/**
	 * Color object representing dark orange - #7f3f00.
	 */
	public static final Color DARK_ORANGE = new Color(127, 63, 0);

	/**
	 * Color object representing dark orange - #ff7f00.
	 */
	public static final Color ORANGE = new Color(255, 127, 0);
	
	/**
	 * A set containing ORANGE and DARK_ORANGE.
	 */
	public static final Set<Color> ORANGE_AND_DARK_ORANGE =
		Collections.unmodifiableSet(asSet(ORANGE, DARK_ORANGE));
	
	/**
	 * Returns the HTML color code (#000000) for the given Color.
	 */
	public static String getColorCode(Color color)
	{
		return "#" + Integer.toHexString(color.getRGB()).substring(2);
	}
	
	/**
	 * Gets a random color - a color whose red, green and blue components
	 * are random values 0-255.
	 */
	public static Color getRandomColor()
	{
		return new Color(RNG.nextInt(256), RNG.nextInt(256), RNG.nextInt(256));
	}
	
	/**
	 * Gets a random primary color - a color whose red, green and blue
	 * components are each either 0 or 255.
	 * 
	 * Could be one of: BLACK, RED, BLUE, GREEN, CYAN, MAGENTA, YELLOW, WHITE.
	 */
	public static Color getRandomPrimaryColor()
	{
		return new Color(RNG.nextBoolean() ? 255 : 0,
						 RNG.nextBoolean() ? 255 : 0,
						 RNG.nextBoolean() ? 255 : 0); 
	}
	
	/**
	 * Gets the RGB-component wise inversion of given Color.
	 */
	public static Color invert(Color color)
	{
		return new Color(255 - color.getRed(),
						 255 - color.getGreen(),
						 255 - color.getBlue(),
						 color.getAlpha());
	}

	/**
	 * Gets which Color is more different than the given Color: Black or White.
	 */
	public static Color getBlackWhiteComplement(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return avg < 128 ? Color.WHITE : Color.BLACK;
	}

	/**
	 * Gets a grayscale Color of darkness {@code gray}. {@code gray} must be
	 * between 0 and 1, mutally inclusive.
	 */
	public static Color getGrayscale(double gray)
	{
		if (gray < 0 || gray > 1)
			throw new IllegalArgumentException("Out of range: " + gray);
		
		int grayInt = (int) (gray * 255);
		return new Color(grayInt, grayInt, grayInt);
	}
	
	/**
	 * Gets the gray scale of given Color.
	 */
	public static Color getGrayscale(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return new Color(avg, avg, avg);
	}

	/**
	 * Gets the gray scale of given Image.
	 */
	public static BufferedImage getGrayscale(Icon icon)
	{
		BufferedImage copy = new BufferedImage(
			icon.getIconWidth(),
			icon.getIconHeight(),
			BufferedImage.TYPE_INT_RGB
		);
		WritableRaster out = copy.getRaster();
		
		icon.paintIcon(null, copy.getGraphics(), 0, 0);
		
		int[] pixel = new int[4];
		
		for (int x = 0; x < out.getWidth();  ++x)
		for (int y = 0; y < out.getHeight(); ++y)
		{
			out.getPixel(x, y, pixel);
			pixel[0] = pixel[1] = pixel[2] = (pixel[0] + pixel[1] + pixel[2]) / 3;
			out.setPixel(x, y, pixel);
		}
		
		return copy;
	}
	
	/**
	 * Gets the complement of the gray scale of given Color.
	 */
	public static Color getGrayscaleComplement(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return new Color(255 - avg, 255 - avg, 255 - avg);
	}
	
	/**
	 * Gets a Color with the same RGB components as the given Color,
	 * but with the specified translucentcy value in the range [0,255].
	 */
	public static Color getTranslucency(Color base, int a)
	{		
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
	}

	/**
	 * Gets a Color with the same RGB components as the given Color,
	 * but with the specified translucentcy value in the range [0.0,1.0].
	 */
	public static Color getTranslucency(Color base, float a)
	{
		float[] rgb = new float[3];
		base.getColorComponents(rgb);
		
		return new Color(rgb[0], rgb[1], rgb[2], a);
	}
	
	/**
	 * Returns a BufferedImage with the alpha value of all pixels scaled
	 * by the specified factor.
	 * 
	 * Unlike the other getTranslucency methods, the alpha values are not
	 * <b>set</b> to the given factor, they are <b>multiplied</b> by it.
	 * 
	 * The returned image is a new image only if the given image does
	 * not have an alpha channel.
	 */
	public static BufferedImage getTranslucency(
		BufferedImage img,
		float aFactor)
	{
		if (aFactor == 1)
			return img;
		
		if (aFactor < 0 || aFactor > 1)
			throw new IllegalArgumentException();
		
		BufferedImage copy = new BufferedImage(
			img.getWidth(),
			img.getHeight(),
			BufferedImage.TYPE_INT_ARGB
		);
		
		if (aFactor == 0)
			return copy;
		
		WritableRaster srcRaster = img.getRaster();
		WritableRaster outRaster = copy.getRaster();
		
		float[] pixel = new float[4];
		
		boolean alpha = img.getColorModel().hasAlpha();
		
		for (int y = 0; y < copy.getHeight(); ++y)
		for (int x = 0; x < copy.getWidth();  ++x)
		{
			srcRaster.getPixel(x, y, pixel);
			
			if (alpha)
			{
				pixel[3] *= aFactor;
			}
			else
			{
				pixel[3] = aFactor * 255;
			}
			
			outRaster.setPixel(x, y, pixel);
		}
		
		return copy;
	}
	
	/**
	 * Gets a new BufferedImage of the specified size with all pixels having
	 * the given color.
	 */
	public static BufferedImage getBlankImage(Color color, int w, int h)
	{
		BufferedImage img = new BufferedImage(
			w,
			h,
			BufferedImage.TYPE_INT_ARGB
		);
		
		WritableRaster raster = img.getRaster();
		
		int[] pixel = new int[]{
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			color.getAlpha()
		};
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			raster.setPixel(x, y, pixel);
		
		return img;
	}
	
	/**
	 * Gets a new BufferedImage with an alpha channel that has the pixel
	 * data from the given image copied into it.
	 * 
	 * If the given BufferedImage already has an alpha channel, it is
	 * simply returned.
	 */
	public static BufferedImage getAlphaImage(BufferedImage img)
	{
		if (img.getColorModel().hasAlpha())
			return img;
		
		int w = img.getWidth();
		int h = img.getHeight();
		
		BufferedImage copy = new BufferedImage(
			w,
			h,
			BufferedImage.TYPE_INT_ARGB
		);
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			copy.setRGB(x, y, img.getRGB(x, y));
		
		return copy;
	}

	/**
	 * Replaces all pixels with Colors in the the Set backgroundColors
	 * with replacementColor.
	 * 
	 * This method is most useful when replacing the background Color
	 * of a no-alpha sprite with zero-alpha pixels.
	 * 
	 * @see Utils.CLEAR
	 */
	public static BufferedImage replaceColors(
		BufferedImage img,
		Set<Color> backgroundColors,
		Color replacementColor)
	{
		boolean hasAlpha = img.getColorModel().hasAlpha();
		int replacementCode = replacementColor.getRGB();
		
		for (int y = 0; y < img.getHeight(); ++y)
		for (int x = 0; x < img.getWidth();  ++x)
		{
			Color color = new Color(img.getRGB(x, y), hasAlpha);
			
			if (backgroundColors.contains(color))
			{
				img.setRGB(x, y, replacementCode);
			}
		}
				
		return img;
	}

	/**
	 * Scales a BufferedImage down by 1/2.
	 */
	public static BufferedImage shrink(BufferedImage img)
	{
		BufferedImage copy = new BufferedImage(
			img.getWidth() / 2,
			img.getHeight() / 2,
			BufferedImage.TYPE_INT_RGB
		);
		
		int[] pixel1 = new int[3];
		int[] pixel2 = new int[3];
		int[] pixel3 = new int[3];
		int[] pixel4 = new int[3];
		
		WritableRaster src = img.getRaster();
		WritableRaster out = copy.getRaster();
		
		for (int x = 0; x < copy.getWidth();  ++x)
		for (int y = 0; y < copy.getHeight(); ++y)
		{
			src.getPixel(x * 2,     y * 2,     pixel1);
			src.getPixel(x * 2 + 1, y * 2,     pixel2);
			src.getPixel(x * 2,     y * 2 + 1, pixel3);
			src.getPixel(x * 2 + 1, y * 2 + 1, pixel4);
			
			pixel1[0] += pixel2[0] + pixel3[0] + pixel4[0];
			pixel1[1] += pixel2[1] + pixel3[1] + pixel4[1];
			pixel1[2] += pixel2[2] + pixel3[2] + pixel4[2];
			
			pixel1[0] /= 4;
			pixel1[1] /= 4;
			pixel1[2] /= 4;
			
			out.setPixel(x, y, pixel1);
		}
		
		return copy;
	}
	
	/**
	 * Loads a CostMap from a bitmap image. The CostMap will be the
	 * same size as the bitmap is in pixels. The value at each position
	 * in the CostMap will be the average of the three color components
	 * of each pixel in the bitmap.
	 * 
	 * @throws IOException If there is any problem reading from file.
	 */
	public static CostMap loadCostMap(File file) throws IOException
	{
		BufferedImage bitmap = (BufferedImage) ImageIO.read(file);
		WritableRaster raster = bitmap.getRaster();
		CostMap costMap = new CostMap(bitmap.getWidth(), bitmap.getHeight());
		float[] pixel = new float[4];
		double div = 3 * 255;
		
		for (int y = 0; y < costMap.getHeight(); ++y)
		for (int x = 0; x < costMap.getWidth();  ++x)
		{
			raster.getPixel(x, y, pixel);
			double gray = (pixel[0] + pixel[1] + pixel[2]) / div;
			costMap.setScaleFactor(x, y, gray);
		}
		
		return costMap;
	}
	
	/*
	 * A set of convience methods to implement C++ "friend" style access rules.
	 * 
	 * The checkCaller() methods are used to ensure that only certain classes
	 * (and perhaps subclassses thereof) are able to call a method.
	 * 
	 * Typical use:
	 * <code>
	 *     public void friendsOnly()
	 *     {
	 *         Friends.checkCaller(MyOneFriend.class, MyOtherFriend.class);
	 *         
	 *         // this.getClass() is automatically on the list
	 *         // it can be denied by calling:
	 *         Friends.checkCallerNot(ThisClass.class);
	 *         
	 *         ...method contents...
	 *     }
	 * </code>
	 */
	
	/**
	 * Throws an IllegalAccessError if the method that calls this method
	 * was called by any class not on the given whiteList.
	 * 
	 * By default, subclasses of classes on the whiteList are considered
	 * acceptable.
	 */
	public static void checkCaller(Class<?>... whiteList)
	{
		/*
		 * Make sure stack traces are filled in at
		 * entry points to this class, not any deeper.
		 */
		internalCheck(new Throwable(), true, whiteList);
	}

	/**
	 * Throws an IllegalAccessError if the method that calls this method
	 * was called by any class not on the given whiteList.
	 * 
	 * If subs is true, subclasses of classes on the whiteList
	 * are considered acceptable.
	 */
	public static void checkCaller(boolean subs, Class<?>... whiteList)
	{
		/*
		 * Make sure stack traces are filled in at
		 * entry points to this class, not any deeper.
		 */
		internalCheck(new Throwable(), subs, whiteList);
	}
	
	/**
	 * Internal method that makes sure the caller is on the whiteList.
	 *
	 * Typical call stack should look like this:
	 * 
	 * questionableCaller() > curiousMethod() > checkCaller() > internalCheck()
	 */
	private static void internalCheck(Throwable thrown,
	boolean subclasses, Class<?>... whiteList)
	{
		Class<?> callingClass, containingClass;
		
		try
		{
			final StackTraceElement[] trace = thrown.getStackTrace();
			callingClass = Class.forName(trace[2].getClassName());
			containingClass = Class.forName(trace[1].getClassName());
		
			for (Class<?> clazz : whiteList)
				if ((subclasses && clazz.isAssignableFrom(callingClass))
				|| (!subclasses && clazz.equals(callingClass)))
					return;
			
			if (containingClass.isAssignableFrom(callingClass))
				return;
		}
		catch (ClassNotFoundException classNotFound)
		{
			throw new Error("How could class not be loaded?", classNotFound);
		}
		
		throw new IllegalAccessError(String.format(
			"%1$s is not a friend of %2$s",
			callingClass,
			containingClass));
	}

	/**
	 * Throws an IllegalAccessError if the method that calls this method
	 * was called by any class on the given blackList.
	 * 
	 * By default, subclasses of classes on the blackList are also considered
	 * unacceptable.
	 */
	public static void checkCallerNot(Class<?>... blackList)
	{
		/*
		 * Make sure stack traces are filled in at
		 * entry points to this class, not any deeper.
		 */
		internalCheckNot(new Throwable(), true, blackList);
	}

	/**
	 * Throws an IllegalAccessError if the method that calls this method
	 * was called by any class on the given blackList.
	 * 
	 * If subs is true, subclasses of classes on the blackList
	 * are also considered unacceptable.
	 */
	public static void checkCallerNot(boolean subs, Class<?>... blackList)
	{
		/*
		 * Make sure stack traces are filled in at
		 * entry points to this class, not any deeper.
		 */
		internalCheckNot(new Throwable(), subs, blackList);
	}

	/**
	 * Internal method that makes sure the caller is not on the blackList.
	 *
	 * Typical call stack should look like this:
	 * 
	 * questionableCaller() > curiousMethod() > checkCaller() > internalCheck()
	 */
	private static void internalCheckNot(Throwable thrown,
	boolean subclasses, Class<?>... blackList)
	{
		try
		{
			final StackTraceElement[] trace = thrown.getStackTrace();
			Class<?> callingClass = Class.forName(trace[2].getClassName());
			Class<?> containingClass = Class.forName(trace[1].getClassName());
			
			for (Class<?> clazz : blackList)
				if ((subclasses && clazz.isAssignableFrom(callingClass))
				|| (!subclasses && clazz.equals(callingClass)))
					throw new IllegalAccessError(String.format(
						"%1$s is banned from calling %2$s",
						callingClass,
						containingClass));
		}
		catch (ClassNotFoundException classNotFound)
		{
			throw new Error("How could class not be loaded?", classNotFound);
		}
	}
	
	/*
	 * XML handling methods, including XPath-like lookup methods.
	 */
	
	/**
	 * Loads a org.w3c.dom.Document from the specified XML file, validating
	 * it against referenced schema if {@code validate} is true.
	 */
	public static Document loadXML(File xmlFile, boolean validate)
	throws IOException
	{
		DocumentBuilderFactory parserFactory =
			DocumentBuilderFactory.newInstance();
		parserFactory.setIgnoringComments(true);
		parserFactory.setIgnoringElementContentWhitespace(true);
		parserFactory.setValidating(validate);

		try
		{
			DocumentBuilder parser = parserFactory.newDocumentBuilder();
			parser.setErrorHandler(new ErrorHandler()
			{
				public void error(SAXParseException exc)
				throws SAXException
				{
					throw new SAXException(exc);
				}
				
				public void fatalError(SAXParseException exc)
				throws SAXException
				{
					throw new SAXException(exc);
				}
				
				public void warning(SAXParseException exc)
				throws SAXException
				{
					throw new SAXException(exc);
				}
			});
			
			return parser.parse(xmlFile);
		}
		catch (ParserConfigurationException e)
		{
			throw new Error(e);
		}
		catch (SAXException e)
		{
			throw new FileFormatException(xmlFile, e.getMessage());
		}
	}
	
	/**
	 * Returns the node that matches the given path from the given root node.
	 * Returns null if none do.
	 * 
	 * If multiple nodes match the path, the first one found will be returned.
	 * Which node that is with respect to the order in the file is not
	 * guaranteed.
	 */
	public static Node getNode(Node root, String... path)
	throws FileFormatException
	{
		Node currentNode = root;
		
		for (int p = 0; p < path.length; ++p)
		{
			NodeList children = currentNode.getChildNodes();
			boolean found = false;
			
			for (int c = 0; !found && c < children.getLength(); ++c)
			{
				Node child = children.item(c);
				
				if (child.getNodeName().matches(path[p]))
				{
					currentNode = child;
					found = true;
				}
			}
			
			if (!found)
				throw new IllegalArgumentException("Node not found " + root.getTextContent());
		}
		
		return currentNode;
	}
	
	/**
	 * Returns the text content of the node that matches the given path from
	 * the given root node.
	 * 
	 * If multiple nodes match the path, the first one found will be returned.
	 * Which node that is with respect to the order in the file is not
	 * guaranteed.
	 */
	public static String getValue(Node root, String... path)
	throws FileFormatException
	{
		Node node = getNode(root, path);
		
		if (node == null)
			return null;
		
//		if (node.hasChildNodes())
//			throw new FileFormatException("Node has children");
		
		return node.getTextContent().trim();
	}
	
	/**
	 * Returns a list of nodes that match the given path from the given
	 * root node. Returns empty list if none do.
	 * 
	 * Currently, the implementation of this method only branches the search
	 * path on the second to last branch, so all nodes in the returned list
	 * will be immediate siblings.
	 */
	public static List<Node> getNodes(Node root, String... path)
	{
		ArrayList<Node> results = new ArrayList<Node>();
		Node currentNode = root;
	
		for (int p = 0; p < path.length - 1; ++p)
		{
			NodeList children = currentNode.getChildNodes();
			Node nextNode = null;
			
			for (int c = 0; c < children.getLength(); ++c)
			{
				Node child = children.item(c);
				
				if (child.getNodeName().matches(path[p]))
				{
					nextNode = child;
					break;
				}
			}
			
			if (nextNode == null)
				return results;

			currentNode = nextNode;
		}
		
		NodeList children = currentNode.getChildNodes();
		
		for (int c = 0; c < children.getLength(); ++c)
		{
			Node child = children.item(c);
			
			if (child.getNodeName().matches(path[path.length - 1]))
			{
				results.add(child);
			}
		}
		
		return results;
	}

	/**
	 * Returns a list of values of the nodes that match the given path from
	 * the given root node. Returns empty list if none do.
	 */
	public static List<String> getValues(Node root, String... path)
	throws FileFormatException
	{
		ArrayList<String> results = new ArrayList<String>();
		Node currentNode = root;
	
		for (int p = 0; p < path.length - 1; ++p)
		{
			NodeList children = currentNode.getChildNodes();
			Node nextNode = null;
			
			for (int c = 0; c < children.getLength(); ++c)
			{
				Node child = children.item(c);
				
				if (child.getNodeName().matches(path[p]))
				{
					nextNode = child;
					break;
				}
			}
			
			if (nextNode == null)
				return results;

			currentNode = nextNode;
		}
		
		NodeList children = currentNode.getChildNodes();
		
		for (int c = 0; c < children.getLength(); ++c)
		{
			Node child = children.item(c);
			
			if (child.getNodeName().matches(path[path.length - 1]))
			{
				if (child.hasChildNodes())
					throw new IllegalArgumentException("Node has children");
				
				results.add(child.getTextContent().trim());
			}
		}
		
		return results;
	}
	
	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Throws FileFormatException if attribute is not present.
	 * Returns empty string if
	 * attribute has empty string definition in the XML.
	 */
	public static String getAttribute(Node node, String name)
	throws FileFormatException
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
		
		return attr.getTextContent();
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not present. Returns empty string
	 * if attribute has empty string definition in the XML.
	 */
	public static String getAttribute(Node node, String name, String defaultValue)
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			return defaultValue;
		
		String attrString = attr.getTextContent();
		
		if (attrString == null || attrString.isEmpty())
			return defaultValue;
		
		return attrString;
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not an int.
	 */
	public static int getIntAttribute(Node node, String name)
	throws FileFormatException
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
		
		String text = attr.getTextContent();
		
		if (text == null || text.isEmpty())
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
			
		return Integer.parseInt(text);
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not presentor is empty.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not an int.
	 */
	public static int getIntAttribute(Node node, String name, int defaultValue)
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			return defaultValue;
		
		String attrString = attr.getTextContent();
		
		if (attrString == null || attrString.isEmpty())
			return defaultValue;
		
		return Integer.parseInt(attrString);
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not present or is empty.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not a double.
	 */
	public static double getFloatAttribute(Node node, String name)
	throws FileFormatException
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
		
		String text = attr.getTextContent();
		
		if (text == null || text.isEmpty())
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
			
		return Double.parseDouble(text);
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not present or is empty.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not a double.
	 */
	public static double getFloatAttribute(Node node, String name, double defaultValue)
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			return defaultValue;
		
		String attrString = attr.getTextContent();
		
		if (attrString == null || attrString.isEmpty())
			return defaultValue;
		
		return Double.parseDouble(attrString);
	}

	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not present or is empty.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not a double.
	 */
	public static boolean getBooleanAttribute(Node node, String name)
	throws FileFormatException
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
		
		String text = attr.getTextContent();
		
		if (text == null || text.isEmpty())
			throw new IllegalArgumentException(String.format(
				"Attribute \"%1$s\" expected on <%2$s>",
				name,
				node.getNodeName()
			));
			
		return Boolean.parseBoolean(text);
	}
	
	/**
	 * Gets the value of the attribute by the given name for the specified
	 * node.
	 * 
	 * Returns defaultValue if attribute is not present or is empty.
	 * 
	 * @throws NumberFormatException If attribute is defined and has a value,
	 *                               but value is not a double.
	 */
	public static boolean getBooleanAttribute(Node node, String name, boolean defaultValue)
	{
		NamedNodeMap attrs = node.getAttributes();
		
		if (attrs == null)
			throw new IllegalArgumentException("node not an element");
		
		Node attr = attrs.getNamedItem(name);
		
		if (attr == null)
			return defaultValue;
		
		String attrString = attr.getTextContent();
		
		if (attrString == null || attrString.isEmpty())
			return defaultValue;
		
		return Boolean.parseBoolean(attrString);
	}
}
