package Colors;

import java.awt.Color;

/**
 * 	Class for representing  individual colour components for full control
 * @author Admin
 *
 */
public class ColorInt {
	
	 public int  red;
	 public int  green;    
	 public int  blue;
	 public int  alpha;

	/**
	 * Constructors
	 */
	public ColorInt() {
		red=0;
		blue = 0;
		green = 0;
		alpha = 0;
	}


	/**
	 * Constructor from separate integers
	 */
	public ColorInt(int red, int green, int blue, int alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	/**
	 * Constructor from float values
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public ColorInt(float red, float green, float blue, float alpha) {
		this.red = (int) (red*255);
		this.green = (int) (green*255);
		this.blue = (int) (blue*255);
		this.alpha = (int) (alpha*255);
	}


	/**
	 *   Constructor from another  ColorInt 
	 */
	public ColorInt(ColorInt c) {
    	this.red= c.red;
    	this.green= c.green;
    	this.blue= c.blue;
    	this.alpha= c.alpha;
    }

	/**
	 * Construct the colour components according to java.awt.Color
	 * @param color
	 */
	public ColorInt(Color color){
		this.red=  color.getRed();       //(int ) 1.0f*(color.getRed()/255);
		this.green= color.getGreen();
		this.blue= color.getBlue();
		this.alpha= color.getAlpha();
	}

	
	/**
	 * Construct the colour components according to an integer of combined RGBA components
	 * as it is represented in a BufferedImage
	 * @param rgba - the combined RGBA components
	 */
	public ColorInt(int rgba){
		Color color= new Color(rgba,true);
		this.red=   color.getRed();   
		this.green= color.getGreen();
		this.blue=  color.getBlue();
		this.alpha= color.getAlpha();
	}

	/**
	 * Converts and produces a new slick colour
	 * @return a new slick Color
	 */
	public org.newdawn.slick.Color convertToSlick(){
		org.newdawn.slick.Color c= new org.newdawn.slick.Color(red, green, blue, alpha);
		return c;
	}
	
	/**
	 * Converting to float
	 * @return an array of floats
	 */
	public float[] convertToFloat(){
		float[] c=new float[4];
		c[0] =  1.0f*red/255;
		c[1] =  1.0f*green/255;
		c[2] =  1.0f*blue/255;
		c[3] =  1.0f*alpha/255;
		return c;
	}
	
	/**
	 * Sets the colour according to a standard Java java.awt.Color.
	 * @param color - a java.awt.Color
	 */
	public void set(Color color){
		this.red=  color.getRed();
		this.green= color.getGreen();
		this.blue= color.getBlue();
		this.alpha= color.getAlpha();
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Color("+red+","+green+","+blue+", "+alpha+");\n";
	}


	/**
	 * Makes a standard opaque AWT Color
	 * @return - a AWT Colors
	 */
	public Color makeColor(){
		Color c= new Color(this.toInt()); //!! Makes an opaque colour !
		return c;
	}

	/**
	 * Makes a standard opaque AWT Color with transparency
	 * @return - a AWT Colors
	 */
	public Color makeColorAlpha(){
		clamp();
		Color c= new Color(this.red, this.green,this.blue,this.alpha);
		return c;
	}

	/**
	 * Checks if the colour is lighter than the requested one
	 * @param c the colour to check against
	 * @return returns true if the colour is lighter
	 */
	public boolean isLighterThan(Color c){
		boolean result=false;
		int value= red + blue + green;
		int value2 = c.getRed()+c.getBlue()+c.getGreen() ;
		// Lighter ..the higher the values as towards white
		result=(value>value2);
		return result;
	}
	
	
	/**
	 *	Checks if  colour1 is lighter than colour2
	 * @param c1 the first colour
	 * @param c2 second color
	 * @return true if the first colour is lighter than the second color
	 */
	static public boolean isLighterThan(Color c1,Color c2){
		boolean result=false;
		int value1= c1.getRed()+c1.getBlue()+c1.getGreen() ;
		int value2 = c2.getRed()+c2.getBlue()+c2.getGreen() ;
		// Lighter ..the higher the values are towards white
		result=(value1>value2);
		return result;
	}
	
	/**
	 * Returns a black-or-white colour depending on whether the first colour is 
	 * lighter than the second color. 
	 * @param c the colour of reference
	 * @param c2 the colour to check against
	 * @return a colour which is black if c lighter than c2. Otherwise white
	 */
	static public Color blackOrWhite(Color c, Color c2){
		Color color= null;
		color = (ColorInt.isLighterThan(c, c2)) ? Color.black: Color.white ;
		
		return color;
	}
	
	public Color greyscaleColor(){
		int m = (red+green+blue)/3 ;
		Color color= new Color(m, m, m);
		return color;
	}
		
	public void greyscale(){
		int m = (red+green+blue)/3 ;
		red=m;
		green=m;
		blue=m;
	}
	
	/**
	 * 
	 * @param color
	 * @param opacity
	 * @return
	 */
	public static Color makeColorOpaque(Color color,int opacity){
		Color c= new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
		return c;
	}
	
	
	/**
	 * Merge to colours together
	 * @param rgb1
	 * @param rgb2
	 * @return
	 */
	public static int merge(int rgb1, int rgb2){
		ColorInt ci= new ColorInt(rgb1);
		ColorInt ci2= new ColorInt(rgb2);
		float a1= 1.0f*ci.alpha/255.0f;
		float a2= 1.0f*ci2.alpha/255.0f;
		float a= a1+a2;
//		if(a<2  && a>0) System.out.println("alpha values:"+a+"= "+a1+"+"+a2);
		a1/=a;
		a2/=a;
		// Weight each component with its alpha value !! <<>
		ci.red= (int) ( (ci.red*a1+ ci2.red*a2)/1.0f);
		ci.green= (int) ( (ci.green*a1+ ci2.green*a2)/1.0f);
		ci.blue= (int) ( (ci.blue*a1 +ci2.blue*a2)/1.0f);
		//What to do about the alpha?
		ci.alpha= (int)(( ci.alpha+ci2.alpha)/2.0f); // Math.min(ci.alpha, ci2.alpha);   // (( ci.alpha+ci2.alpha)/2.0f); // 
		ci.clamp();
		return ci.toInt();
	}
	
	public static int mergeWeight(int rgb1, int rgb2,float w){
		ColorInt ci= new ColorInt(rgb1);
		ColorInt ci2= new ColorInt(rgb2);
		float w2= 1-w;
		
		// Waight each component according to value
		ci.red= (int) ( (ci.red*w+ ci2.red*w2)/1.0f);
		ci.green= (int) ( (ci.green*w+ ci2.green*w2)/1.0f);
		ci.blue= (int) ( (ci.blue*w +ci2.blue*w2)/1.0f);
		//What to do about the alpha?
		ci.alpha= (int)( ci.alpha*w+ci2.alpha*w2); // Math.min(ci.alpha, ci2.alpha);   // (( ci.alpha+ci2.alpha)/2.0f); // 
		ci.clamp();
		return ci.toInt();
	}
	
	
	/**
	 * 
	 */
	public void invert(){
		this.red= 255-this.red;
		this.green= 255-this.green;
		this.blue= 255-this.blue;
	}
	
	
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public static Color invert(Color c){
		Color icolur= new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue(),c.getAlpha());
		return icolur;		
	}
	
	/**
	 * Create a AWT Color which is a darker version of this.
	 * @param f - the amount of darkness. >1 represents lighter.
	 * @return - a new AWT Color which is a darker representation of this one 
	 */
	public Color darkerColor(float f){
		ColorInt c= new ColorInt(this); 
		c.darker(f);
		return c.makeColor();
	}

	/**
	 * Darkens the colour with a given factor
	 * @param f
	 */
	public void darker(float f){
		this.red*=f;
		this.green*= f;
		this.blue*= f;
		clamp();
	}
	
	
	/**
	 * Clamps of all colours to their allowed range 
	 */
	private void clamp(){
		this.red = Math.max(0, Math.min(red, 255));
		this.green= Math.max(0, Math.min(green, 255));
		this.blue= Math.max(0, Math.min(blue, 255));
		this.alpha= Math.max(0, Math.min(alpha, 255));
	}

	/**
	 * Creates new colour based on this colour and a change in each of the colour channels
	 * @param rgba - the change in each of the colour channels
	 * @param usealpha - whether we should change the alpha channel as well
	 * @return
	 */
	public ColorInt add(int rgba[],boolean usealpha){
			
		ColorInt c	= new ColorInt(this);
		c.red+= rgba[0];
		c.green+= rgba[1];
		c.blue+= rgba[2];
		if(usealpha) c.alpha+= rgba[3];
		c.clamp();
		
		return c;
	}


	/**
	 * Returns the RGBA value representing the color.
	 * In a BufferedImage pixels are such integers.
	 * (Bits 24-31 are alpha, 16-23 are red, 8-15 are green, 0-7 are blue).
	 * @return - an integer representation of the colour
	 */
	public int toInt() {
		Color color = new Color(red, green, blue, alpha);
		return color.getRGB();
	}

	
	/**
	 * Returns the RGBA value representing the Opaque version of the color.
	 * In a BufferedImage pixels are such integers.
	 * @return - an integer representation of the colour
	 */
	public int toIntOpaque() {
		Color color = new Color(red, green, blue, 255);
		return color.getRGB();
	}
	
	/**
	 * Creates a greyscale Color representation of the alpha/opacity value.
	 * We want white to represent transparency and black opacity, hence inverted.
	 * @return - an integer representation of a greyscale colour of the opacity.
	 */
	public int alphaAsGreyscale(){
		Color color = new Color(255-alpha, 255-alpha, 255-alpha, 255);
		return color.getRGB();
	}

	/**
	 * Shifts the colour in RGB space
	 * @param r the red shift
	 * @param g the green shift
	 * @param b the blue shift 
	 */
	public void shiftRGB(int r, int  g,int b){
		red+=r;
		green+=g;
		blue+=b;
		clamp();
	}
	
	/**
	 * Makes a AWT Colour by shifting the current colour in RGB space 
	 * @param r the red shift
	 * @param g the green shift
	 * @param b the blue shift 
	 */
	public Color makeShiftRGB(int r, int  g,int b){
		ColorInt c= new ColorInt(this);
		c.shiftRGB(r, g, b);
//		int[] rgb= new int[]{r,g,b}; 
//		c.add(rgb,false);
		return  c.makeColor();
	}

	/**
	 * Static method to shift a colour in RGB space
	 * @param color the colour to shift
	 * @param r red shift
	 * @param g green shift
	 * @param b blue shift
	 * @return a new AWT colours shifted in RGB space
	 */
public static Color makeShiftRGB(Color color,int r, int  g,int b){
		ColorInt c = new ColorInt(color) ;
		return c.makeShiftRGB(r, g, b);
	}

/**
 * Makes a transparency version of a given Color
 * @param color the colour to make transparent
 * @param a the alpha value
 * @return the color with a new alpha value
 */
public static Color transparencyOf(Color color, int a){
	ColorInt c= new ColorInt(color);
	c.alpha=a;
	return c.makeColorAlpha();
}

/**
 * Makes a transparent version of a given colour 
 * @param color the color is an integer
 * @param a the transparency, alpha value
 * @return a new colour
 */
public static int transparencyFrom(int color, float a){
	ColorInt ci= new  ColorInt(color);
	ci.alpha=(int) (a*255);
	
	return ci.toInt(); 
}

//================================================== getters and setters

	public int getRed() {
		return red;
	}


	public void setRed(int red) {
		this.red = red;
	}


	public int getGreen() {
		return green;
	}


	public void setGreen(int green) {
		this.green = green;
	}


	public int getBlue() {
		return blue;
	}


	public void setBlue(int blue) {
		this.blue = blue;
	}


	public int getAlpha() {
		return alpha;
	}


	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	
}
