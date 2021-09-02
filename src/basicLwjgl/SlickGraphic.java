package basicLwjgl;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Point;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;


import Basic.Vector2D;
import Colors.ColorInt;

// Graphics enabling the use of slick library
public class SlickGraphic extends org.newdawn.slick.Graphics{
	
	Vector2D  position=  new Vector2D(50,200);
	TrueTypeFont fontTTF18;
	TrueTypeFont fontTTF20;
	String text="";
	int width=10;
	int height=10;
	
	Color color=  new Color(0.5f, 0.6f, 0.6f, 0.8f);
	/*	Courier
	 * 	Courier Italic,	Courier Bold,	Courier Bold Italic,	Helvetica,	Helvetica Italic,
	 * 	Helvetica Bold,	Helvetica Bold Italic,	Monospace Symbol,
	 *  Times,	Times Italic,	Times Bold,	Times Bold Italic,	Symbol
	 *  */
	
	// load a default java font
	static Font awtFontCuI40 = new Font("Courier Italic", Font.PLAIN, 40);
	static Font awtFontTNR30 = new Font("Times New Roman", Font.PLAIN, 30);
	static Font awtFontTNR20 = new Font("Times New Roman", Font.PLAIN, 20);
	static Font awtFontTNR24 = new Font("Times New Roman", Font.PLAIN, 24);
	static Font awtFontTNR18 = new Font("Times New Roman", Font.PLAIN, 18);
	static Font awtFontAR20 = new Font("Aerial", Font.PLAIN, 20);
	static TrueTypeFont font24TTF = new TrueTypeFont(awtFontTNR24, true);
	
	Font font= new Font("Aerial", Font.PLAIN, 20);
	// 
	static public java.awt.Color	SILVER_Green = new  java.awt.Color(130,137,111,255);
	static public java.awt.Color	SILVER_Blue= new  java.awt.Color(111,130,150,255);
		
	
	/**
	 * Constructor 
	 * @param width
	 * @param height
	 * @param position
	 * @param text
	 */
	public SlickGraphic(int width, int height){
		super(width,height); //•–•–•– 
//		this.position=position;
//		this.text=text;
		this.width=width;
		this.height=height;
		fontTTF18 = new TrueTypeFont(awtFontTNR18, true);
		fontTTF20 = new TrueTypeFont(awtFontTNR20, true);

	}
	
	public void draw(String text){
		
		fontTTF18.drawString(position.getX(), position.getY(), text,color);
	}
	
	/**
	 * Display some text string
	 * @param text 
	 * @param x pixel x position
	 * @param y
	 */
	public void drawString(String text, int x, int y){
		java.awt.Color c= greyColourAt(new Point(x, y), false);
		Color pix= new Color( c.getRed(),c.getGreen(), c.getBlue(), c.getAlpha());
		pix.a= 0.8f;
		fontTTF18.drawString(x, y, text,pix);
	}
	
	public void drawString(String text, Point p , java.awt.Color c){
		org.newdawn.slick.Color cc= new Color(c.getRed(), c.getGreen(),
				c.getBlue(), c.getAlpha());
		fontTTF18.drawString(p.x, p.y, text, cc);
	}
	/**
	 * Draws a string at the given position in a given colour and hint about size
	 * @param text the text to draw
	 * @param p position
	 * @param c Color
	 * @param size fontsize hint 
	 */
	public void drawString(String text, Point p , java.awt.Color c,int size){
		org.newdawn.slick.Color cc= new Color(c.getRed(), c.getGreen(),
				c.getBlue(), c.getAlpha());
		if (size==18) {
			fontTTF18.drawString(p.x, p.y, text, cc);
		}
		else {
			fontTTF20.drawString(p.x, p.y, text, cc);			
		}
	}

	/**
	 * Draws a string in a given TrueType font
	 * @param text text to draw
	 * @param p position on screen
	 * @param c Java colour
	 * @param fttf TrueType font to use
	 */
	public void drawString(String text, Point p , java.awt.Color c,TrueTypeFont fttf){
		org.newdawn.slick.Color cc= new Color(c.getRed(), c.getGreen(),
				c.getBlue(), c.getAlpha());
		fttf.drawString(p.x, p.y, text, cc);
	}

	public void drawString(String text, Point p , java.awt.Color c, Font font){
		org.newdawn.slick.Color cc= new Color(c.getRed(), c.getGreen(),
				c.getBlue(), c.getAlpha());
		TrueTypeFont fTTF = new TrueTypeFont(font, true); // Warning! This takes time !
		fTTF.drawString(p.x, p.y, text, cc);
	}


	/**
	 * 
	 * @param p
	 * @param inverted
	 * @return
	 * @throws AWTException 
	 */
	public java.awt.Color greyColourAt(Point p, boolean inverted) {
		org.newdawn.slick.Color pix= getPixel(p.x,p.y );
				// ?• org.newdawn.slick.Graphics.

		ColorInt ci= new ColorInt(pix.r,pix.g,pix.b,1.0f);
		//		ci.invert();
		java.awt.Color g= ci.greyscaleColor();
		float f= 0.3f;
		java.awt.Color compare= new  java.awt.Color(f,f,f);
		java.awt.Color c=ColorInt.blackOrWhite(g, compare);
		
//		setColor(pix);
//		fillRect(p.x, p.y-60, 40, 20);
		return c;
	}
	
	/**
	 * 
	 */
	public void animatedCircle(Vector2D p,float radius,float w) {
		float rr= (float) Math.cos(w*0.05f)*radius;
		setColor(color);
		drawOval(p.getX()-rr/2,  p.getY(), rr, radius);
	}

	/**
	 * @param p
	 */
	public void drawARectangle(Vector2D p,boolean fill) {
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		int y=Display.getHeight()- (int)p.y;
		org.newdawn.slick.Color c= getPixel((int)p.x,y);
		org.newdawn.slick.Color c2=  new Color(200,200,200,20);//Color.green);
		c.a= 100;
		
		setColor(c2);
		if (fill) {
			fillRect(p.x, y-10, 400, 50);
		}
		else{
			drawRect(p.x, y-10, 400, 50);
		}
	}
	
	public void setFontSize(int size){
		font=new Font(font.getFontName(), Font.PLAIN, size);
		fontTTF18=  new TrueTypeFont(font, true );
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	
	

}
