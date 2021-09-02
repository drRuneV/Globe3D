package Colors;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/**
 * 
 *
 */
public final class GraphicsUtil {

	
	public static String[] colorNames=new String[]{
			"red",	"green",	"blue",		
			"orange",	"purple",	"pink"
	};
	private static int brewNumber=0; 
	
	public static Color[] fixedColours= {Color.red.darker(), new Color(50, 50, 50, 190), new Color(0,250,0,150),  
				Color.blue,Color.orange, Color.darkGray };
	//Color.green, Color.red.darker(),
	

	/**
	 * Resizes a buffered image and returns a new one
	 * 
	 * @param image - The image to be rescaled
	 * @param width - The new desired with 
	 * @param height - The new desired height
	 * @return - A rescaled new BufferedImage
	 */
	public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
		int type= image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
       
       width=   Math.max(1, width);
       height=  Math.max(1, height);
       
       BufferedImage resizedImage = new BufferedImage(width, height,type);
       Graphics2D g = resizedImage.createGraphics();
       //
       //
     //clear
       g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
       Color transparent = new Color(0, true);
       g.setColor(transparent);
       g.fillRect(0,0,width,height);
       //reset composite
       g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
       //
//		 g.setBackground(transparent);
//	     g.clearRect(0, 0, resizedImage.getWidth(), resizedImage.getHeight());
		 
       g.drawImage(image, 0, 0, width, height, null);
       g.dispose();
       return resizedImage;
    }
	
	public static void resizeImageOn(BufferedImage image,BufferedImage onImage, int width, int height) {

		Graphics2D g = onImage.createGraphics();
		//
		//
		//clear
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		Color transparent = new Color(0, true);
		g.setColor(transparent);
		g.fillRect(0,0,width,height);
		//reset composite
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		g.drawImage(image, 0, 0, width, height, null);
//		g.dispose();
	}
		
	
	

	/**
	 * Selects a index for the gradient based on the name of the distribution
	 * @param name The name of the distribution
	 * @return An index used to generate a gradient
	 */
	public static int mappingGradientToName(String name){
		int index=0;
		if (name.contains("cal")) {
			index=0;
		}
		else if (name.contains("mac")){
			index=1;
		}
		else if (name.contains("her")){
			index=2;
		}
		else if (name.contains("topo")){
			index=9;
		}
		else if (name.contains("blue")){
			index=6;
		}
		
		
		return index;
	}
	
	/**
	 * 
	 * @param type
	 * @param max
	 * @param min
	 * @return
	 */
	public static ColorGradient produceGradient(int type, float[] maxmin){
		ColorGradient cg= new ColorGradient(maxmin[0],maxmin[1]);
		
		Color c;
		Color c2;
		switch (type) {
		case 0:
			produceColorGradientStandard(cg);
			cg.setName("Standard Colour");
			break;
		case 1: //Reddish
			c= new Color(120, 0, 40, 255);
			c2= new Color(200, 50, 40 , 255);
			produceGradientFrom2Colors(cg, c2, c);
			cg.setName("Reddish");
			break;

		case 2: //Green
			c= new Color(10, 105, 10, 255);
			c2= new Color(60, 200, 60 , 255);
//			produceGradientFrom2Colors(cg, c2,c);
			//This produces a mossy green kind of gradient
			produceGradientFromColor(cg,Color.green.darker(),new int[]{ -60,-40 ,-150});
			cg.setName("Green");			
			break;
			
			
		case 3: //Bluish
			c= new Color(0, 0, 150, 255);
			produceGradientFromColor(cg, c);
			cg.setName("Bluish");
			break;

		case 4: //Yellow
			c= new Color(150, 150, 0, 255);
			produceGradientFromColor(cg, c);
			cg.setName("Yellow");
			break;

		case 5: //  Greyscale
			c= new Color(125, 125, 125, 255);
			produceGradientFromColor(cg, c);
			cg.setName("Greyscale");
			//Brown//			
//			c=  ColorGradient.BROWN_MOUNTAIN;
//			produceGradientFromColor(cg, c);
			cg.setName("BROWN_MOUNTAIN");
			break;
			
		case  6: //
			c=  ColorGradient.GREY_BLUE;
			produceGradientFrom2Colors(cg, c, Color.blue.darker());
			cg.setName("GREY_BLUE");
			break;

		case  7: //
			c= new Color( (int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255), 255);
			produceGradientFromColor(cg, c);
			System.out.println("Color: "+ c.toString());
			cg.setName("Random...");
			break;
			
		case 8:
			/*
			//produceDefaultTopoGradient(cg);
			brewNumber++;
			brewNumber= (brewNumber>=6) ? 0 :brewNumber;
//			int r= (int) (Math.random()*6);
			System.out.println("Color: " + colorNames[brewNumber]+" : "+ brewNumber); 
			ColorSet  cs= new ColorSet(colorNames[brewNumber]);
			produceGradientDiscreetFrom(cg, cs.colors);
			cg.setName(String.format("Discrete: "+colorNames[brewNumber]));
			break;
			*/ 

		case 9:
			break;
			
		default:
			
			produceColorBlue(cg);
			break;
		}
			
		return cg;
	}

	/**
	 * Produces a ColourGradient which is a shading of the given colour from lighter to darker
	 * @param cg - The ColourGradient we want to set
	 * @param c - The colour we want to use
	 */
	public static void produceGradientFromColor(ColorGradient cg, Color c) {
		// Clear the indexes first
		cg.getColorIndex().clear();

		// 
		ColorInt ci= new ColorInt(c);
		cg.insert(Color.white, 0);
		

		cg.insert(ci.darkerColor(1.8f),10);
		cg.insert(ci.darkerColor(1.5f),50);
		cg.insert(ci.darkerColor(1.25f),100);
		cg.insert(ci,150);
		cg.insert(ci.darkerColor(0.75f), 200);
		cg.insert(ci.darkerColor(0.5f), 255);
		//
		cg.interpolate(false);
	}
	
	
	/**
	 * Producers ColourGradient by shifting RGB values
	 * @param cg the ColourGradient to be produced 
	 * @param c the final colour
	 * @param shift the RGB shift
	 */
	public static void produceGradientFromColor(ColorGradient cg, Color c, int[]  shift) {
		//Clear the indexes first
		cg.getColorIndex().clear();
		int step= 50;
		int intervals= 255;
		ColorInt ci= new ColorInt(Color.white);
		ColorInt cioorigin= new ColorInt(c);
//		cg.insert(Color.white, 0);
		cg.insert(ci, 0);
		for (int i = step; i < intervals; i+=step) {
			ci.shiftRGB(shift[0],shift[1],shift[2]);
			cg.insert(ci,i);
//			System.out.println("Colour inserted is "+ci.toString());
		}
		cg.insert(ci.makeShiftRGB(shift[0],shift[1],shift[2]),255);
			
	}

	/**
	 * Produces a ColourGradient which is a shading of the given colour from lighter to darker
	 * @param cg - The ColourGradient we want to set
	 * @param c - The colour we want to use
	 */
	public static void produceGradientFrom2Colors(ColorGradient cg, Color c, Color c2) {
		// Clear the indexes first
		cg.getColorIndex().clear();
		//
		ColorInt ci= new ColorInt(c);
		ColorInt ci2= new ColorInt(c2);
		cg.insert(Color.white, 0);
		
		cg.insert(ci.darkerColor(1.5f),50);
		cg.insert(ci.makeColor(),100);
		cg.insert(ci2.darkerColor(1.5f), 150);
		// another colour
		cg.insert(ci2,200);
		cg.insert(ci2.darkerColor(0.3f),255);
		//
		cg.interpolate(true);
	}

	/**
	 * 
	 * @param cg
	 * @param c
	 */
	public static void produceGradientDiscreetFrom(ColorGradient cg, ArrayList<Color> c) {
		// Clear the indexes first
		cg.getColorIndex().clear();
		//
		int step= 256/c.size();
		int ix=0;
		cg.insert((Color.white),0);
		for (int i = 0; i < cg.getColors().length; i+=step) {
			ix= i/step;
			if(ix>0){
				cg.insert(c.get(ix-1), i-1);
			}
			if(ix< c.size()) cg.insert(c.get(ix), i);
			
		}
		//Make sure the last Color is inserted
		cg.insert((c.get(c.size()-1)),255);

		cg.interpolate(true);
	}


	/**
	 * Produces a white – blue – green – orange – red – dark red Gradient
	 * @param cg -  the ColourGradient to be set
	 */
	public static void produceColorGradientStandard(ColorGradient cg){
		cg.insert((Color.white),0);
		cg.insert(ColorGradient.GREY_BLUE, 50);
		cg.insert(Color.blue, 100);
		cg.insert(Color.green, 150);
		cg.insert(Color.yellow, 175);
		cg.insert(Color.orange,200);
		cg.insert(Color.red, 225);
		cg.insert(Color.red.darker(),255);
		//
		cg.setTheOpacityLinear();
		cg.interpolate(false);
		cg.setName("Standard Color");
	}
	

	/**
	 * Produces a special gradient for production( where some of the values negative)
	 * making values around zero white and very transparent..
	 * @param color
	 * @param min
	 * @param max
	 * @return
	 */
	public static ColorGradient produceProductionGradient(Color color, float min, float max){
		ColorGradient cg= new ColorGradient(max, min);
		int m= cg.findIndex(0);
		ColorInt c= new ColorInt(ColorGradient.GREY_BLUE);
		
		cg.insert(new Color(0, 0, 90, 255), 0);  //very dark blue
		cg.insert(new Color(0, 10,150, 200), 50);  //
		
		cg.insert(new Color(255, 255, 255, 20),m);  //
		
		cg.insert(new Color(255, 100, 10, 100), 200);  //orange
		cg.insert(new Color(255, 10, 10, 250), 255);  //red
		cg.interpolate(true);
		
		return cg;
	}


	
	private static void produceColorBlue(ColorGradient cg) {
		Color c= ColorGradient.GREY_BLUE; // new Color(0,0,0,255);
		// 
		cg.insert((Color.white),0);
		cg.insert(c.brighter().brighter(), 50);
		cg.insert(c.brighter(), 100);
		cg.insert(c, 150);
		cg.insert(c.darker(),200);
		cg.insert(Color.blue.darker(), 255);
		//
		cg.interpolate(true);
	}

	/**
	 * 
	 * @param cg
	 */
	public static void produceDefaultTopoGradient(ColorGradient cg,int opacity){
		int m= cg.findIndex(0);
		int d500= cg.findIndex(-500);
//		System.out.println(" Index at depth 0 and 500 is:"+m+":"+ d500);
		cg.insert(new Color(0, 0, 50,   opacity), 0);  //very dark blue
		cg.insert(new Color(0, 0, 120,  opacity), 40);  //dark blue
		cg.insert(new Color(0, 90,150,  opacity), 80);  //gray blue
		//Specific at 500 m depth
		cg.insert( ColorInt.makeColorOpaque(ColorGradient.SILVER_Blue, opacity), d500);
		// very light blue
		cg.insert(new Color(200, 200, 220,opacity ), m-5);
		// White close to shore
		cg.insert(ColorInt.makeColorOpaque(Color.white,opacity), m-1);
		// Land colour starts here
		int landOpacity=255;
		cg.insert(new Color( 50, 70 ,10, landOpacity), m+5);  //
		cg.insert(new Color( 70, 90 ,10, landOpacity), m+8);  //moss Green
		cg.insert(ColorInt.makeColorOpaque(Color.orange, landOpacity), 190);
		cg.insert(ColorInt.makeColorOpaque(ColorGradient.BROWN_MOUNTAIN, landOpacity), 250); //Brown mountain 
		cg.insert(new Color(250, 250, 255, landOpacity), 255);  
		cg.interpolate(true);
	}
	
	/**
	 * Produces a topography gradient  with a greyish look
	 * @param cg The ColourGradient to be set
	 * @param opacity The opacity for each of the colour in the index list of this gradient
	 */
	public static void produceDefaultTopoGradientGrey(ColorGradient cg,int opacity){
		int m= cg.findIndex(0);
		int d500= cg.findIndex(-500);
//		System.out.println(" Index at depth 0 and 500 is:"+m+":"+ d500);
		cg.insert(new Color(40, 40, 50,   opacity), 0);  //very dark blue
		cg.insert(new Color(80, 80, 100,  opacity), 40);  //dark blue
		cg.insert(new Color(100, 110, 120,  opacity), 80);  //gray blue
		//Specific at 500 m depth
		cg.insert( new Color(130,130,140,opacity), d500);
		// very light blue
		cg.insert(new Color(200, 200, 210,opacity ), m-5);
		// White close to shore
		cg.insert(ColorInt.makeColorOpaque(Color.white,opacity), m-1);
		// Land colour starts here
		int landOpacity=255;
		cg.insert(new Color( 50, 70 ,10, landOpacity), m+5);  //
		cg.insert(new Color( 70, 90 ,10, landOpacity), m+8);  //moss Green
		cg.insert(ColorInt.makeColorOpaque(Color.orange, landOpacity), 190);
		cg.insert(ColorInt.makeColorOpaque(ColorGradient.BROWN_MOUNTAIN, landOpacity), 250); //Brown mountain 
		cg.insert(new Color(250, 250, 255, landOpacity), 255);  
		cg.interpolate(true);
	}
	
	/**
	 *  Create a BufferedImage from a rectangular region on the screen.
	 *
	 *  @param	 region region on the screen to create image from
	 *  @return	image the image for the given region
	 *  @exception AWTException see Robot class constructors
	 */
	public static BufferedImage createRobotImage(Rectangle region)
		throws AWTException
	{
		BufferedImage image = new Robot().createScreenCapture( region );
		return image;
	}
	
	/**
	 * Creates a transparent image of the current image with some criteria.
	 * If the sum of the RGB components in the image is higher than a given colour (lighter)
	 * Then the opacity is set to a minimum opacity. Elsewhere the opacity is set to the given opacity.
	 * @param image The image to change to a transparent 1
	 * @param opacity the opacity to set for the image
	 * @param minOpacity the opacity set for lighter pixels
	 * @param color the colour to check against
	 * @return a image with more transparency
	 */
	public static BufferedImage createTransparentImage(BufferedImage image, int opacity,int minOpacity,Color color){
		int rgb=0;
		ColorInt c= new ColorInt();
//		BufferedImage bm= new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 1; y < image.getHeight()-1; y++) {
		for (int x = 1; x < image.getWidth()-1; x++) {
			
			rgb= image.getRGB(x, y);			
			c.set(new Color(rgb));
			c.alpha= (c.isLighterThan(color)) ? minOpacity: opacity;
			rgb= c.toInt();
			image.setRGB(x, y, rgb);
		}
		}
		return image;
	}
	
	/*
	 *  Create a BufferedImage for AWT components.
	 *  This will include Swing components JFrame, JDialog and JWindow
	 *  which all extend from Component, not JComponent.
	 *  //© http://www.camick.com/java/source/ScreenImage.java
	 *  @param  component AWT component to create image from
	 *  @return	image the image for the given region
	 *  @exception AWTException see Robot class constructors
	*/
	public static BufferedImage createImageOfComponent(Component component)
		throws AWTException
	{
		Point p = new Point(0, 0);
		SwingUtilities.convertPointToScreen(p, component);
		Rectangle region = component.getBounds();
		region.x = p.x;
		region.y = p.y;
		return createRobotImage(region);
	}
	

	/**
	 * Enables setting a file location using a file dialogue
	 * @param component
	 * @return
	 */
	public static File saveImagePath(Component component,String ext){
		File file= null;

		JFileChooser fcd = new JFileChooser();
		fcd.setDialogTitle(" Set a location and filename for saving images");
		fcd.setFont(new Font("Times", Font.PLAIN, 14));
		fcd.showSaveDialog(component);
		if (fcd.getSelectedFile()!=null) {
			file =  new File( fcd.getSelectedFile().getAbsoluteFile()+ext);
		}

		return file;
	}
	
	public static File openImagePath(Component component){
		File file= null;

		JFileChooser fcd = new JFileChooser();
		fcd.setDialogTitle("Open images");
		fcd.setFont(new Font("Times", Font.PLAIN, 14));
		fcd.showOpenDialog(component);
		file =  fcd.getSelectedFile();
		return file;
	}

	

	/**
	 * 
	 * @param component
	 * @return
	 */
	public static File saveImageOfComponent(Component component){
		BufferedImage imagesave = null;
		File file= null;
		// Create a BufferedImage from the component as it looks
		try {
			imagesave = createImageOfComponent(component);
		} catch (AWTException e2) {
			e2.printStackTrace();
		}	
//		fcd.showSaveDialog(component);
		try {
			file =  saveImagePath(component, ".png");
			ImageIO.write(imagesave, "png", file);//fcd.getSelectedFile().);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return file;
	}

}


