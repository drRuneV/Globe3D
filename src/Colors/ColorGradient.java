package Colors;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;



//Offers a ColourGradient with 256 colours represented 
//
//The colours can be defined by inserting a few colours at certain indexes, given by the map.
//The other colours in the gradient are then interpolated.


public class ColorGradient {
	static public Color BROWN_MOUNTAIN =new Color(120, 60, 0, 255);  //Brown mountain
	static public Color MOSS_Green= new Color( 70, 90 ,10, 255);  //moss Green
	static public Color	GREY_BLUE=new Color(0, 90, 150, 255);  //gray blue
	static public Color	SILVER_Green = new  Color(130,137,111,255);
	static public Color	SILVER_Blue= new  Color(111,130,150,255);
	//
	private String name="ColorGradient name";
	// The array of colours in the gradient
	private ColorInt[] colors;
	// Defined colours fixed at certain indexes; These are the colours which defines a gradient
	private Map<Integer, ColorInt> colorIndex;
	// The maximum value which the last element in the array corresponds to
	private float max;
	// The minimum value which the first element in the array corresponds to
	private float min;
	// The value corresponding to one interval in the colour array 
	private float step;
	//
	// In which range the opacity apply
	public Point opacityRange= new Point(0, 200);
	// What is the limit of the maximum and minimum opacity
	public Point opacityMinMax= new Point(5, 200);
	//
	public int opacityBelowMin=0;
	// Whether opacity mask is to be used
	private boolean useOpacityMask=true;
	// Logarithmic scale
	private boolean useLogarithmic=false;
		

	//	private String type="linear"; 

	// An image of the drawing of the ColourGradient
	private BufferedImage image= null;
	private int offy=20;
	private boolean useBackground=true;
	// Use this if we need to send the signal that the gradient has been changed
	public boolean wasChanged=false;
	
	//
	private Point location= new Point();

	/**
	 * 
	 * @param max - The maximum value
	 * @param min  - The minimum value
	 */
	public ColorGradient(float max,float min) {
		colors= new ColorInt[256];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = new ColorInt();
		}
		colorIndex= new TreeMap<Integer, ColorInt>();
		this.max= max;
		this.min= min;
		step = (max-min)/256.0f;
//		clear(Color.black,100);
		clear(Color.black);
		createBasic();
		createImage();
	}
	
	/**
	 * Constructor
	 * @param c
	 */
	public ColorGradient(ColorGradient  cg) {
		this(1,0);
		defineFromGradient(cg);
		this.setColorIndex(cg.getColorIndex());
		this.setColors(cg.getColors());
		
		reDefine();
	}
	
	
	/**
	 * Defines the parameters of this gradient from another gradient
	 * @param c
	 */
	public void defineFromGradient(ColorGradient c){
		this.name=c.name;
		this.max=c.max;
		this.min=c.min;
		this.offy=c.offy;
		this.opacityBelowMin=c.opacityBelowMin;
		this.opacityMinMax=c.opacityMinMax;
		this.opacityRange=c.getOpacityRange();
		this.useBackground=c.useBackground;
		this.useLogarithmic=c.useLogarithmic;
		this.useOpacityMask = c.useOpacityMask ;
		this.step = c.step ;
		
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s="ColorGradient [name=" + name + ", max=" + max + ", min=" + min + ", step=" + step + "] \n";
		return s;				
	}


	/**
	 * Defines the gradient. The step value is redefined, then colours are interpolated
	 * and the gradient is redrawn.
	 */
	public void define(){
		step = (max-min)/256.0f;
		if (useOpacityMask) {
			setOpacityWithMask();	
		}
		interpolate(!useOpacityMask);
		drawGradient();
		wasChanged=true;
	}


	/**
	 * Defines the step value and draws the image of the gradient
	 * This method must be called whenever we change the maximum or minimum
	 * because we then have to redefine the step and redraw the image
	 */
	public void reDefine(){
		//step = (max-min)/256.0f;
		define();
		createImage();
	}
	

	/**
	 * Defines the size of the image the gradient is drawn upon, recreates it and draws it
	 */
	private void createImage(){
		int width= 100;
		int height=256+offy*2+2; //22+22;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		drawGradient();
	}

	/**
	 * Changes the colour scale by a factor
	 * @param f The factor to change with
	 */
	public void stretch(float f){
		setMax(getMax()*f);
		setMin(getMin()*f);   // April 2019			
		reDefine();
	}


	/**
		 * Shifts all the colour indexes to more red
		 */
		public void shiftRed(){
			for (Integer key : colorIndex.keySet()) {
				colorIndex.get(key).shiftRGB(5, -1, -1);
				System.out.println("value : " + key); 
			}
	
	////		
	//		for (int i = 0; i < colors.length; i++) {
	//			colors[i].shiftRGB(5, -1, -1);
	//			}
	//		
		}



	public void shiftDark(int d){
			for (Integer key : colorIndex.keySet()) {
				colorIndex.get(key).shiftRGB(-5*d, -5*d, -5*d);
				System.out.println("value : " + key); 
			}
	}


		

	/**
	 * Shifts all the colour indexes to more green
	 */
	public void shiftGreen(){
		for (Integer key : colorIndex.keySet()) {
			colorIndex.get(key).shiftRGB(-1,5, -1);
			System.out.println("value : " + key); 
		}
	}
	


	/**
	 * Shifts all the colour indexes to more blue
	 */
	public void shiftBlue(){
		for (Integer key : colorIndex.keySet()) {
			colorIndex.get(key).shiftRGB(-1,-1, 5);
			System.out.println("value : " + key); 
		}
	}
	
	
	/**
	 * Creates a very basic blue gradient
	 */
	private void createBasic() {
		insert((Color.white),0);
		insert((Color.BLUE),255);
		interpolate(false);
	}



	// not used?
	public void createTypical(int type){
		
		insert((Color.lightGray),0);//0,0,0,a), 0); //white
//		insert((Color.lightGray),20);//0
		insert(ColorGradient.GREY_BLUE, 90);
		insert(Color.green, 150);
		insert(Color.orange,200);
		insert(Color.red, 225);
		insert(Color.red.darker(), 255);
		//
		//
//		setOpacityLinear(0, 255);
		interpolate(true);
	}

	/**
	 * Clears all the colours in the gradient.
	 * @param c - The colour we want to clear with.
	 */
	public void clear(Color c){
		for (int i = 0; i < colors.length; i++) {
			colors[i].set(c);
			}
		colorIndex.clear();
	}

	/**
	 * Clears the colour with a specific Color and alpha value
	 * @param c the colour to clear with
	 * @param a the alpha value to clear with
	 */
	public void clear(Color c,int a){
		for (int i = 0; i < colors.length; i++) {
			colors[i].set(c);
			colors[i].alpha=a;			
		}
		colorIndex.clear();
	}

	/**
	 * Identifies a node in the ColorIndex Map nearby the given index
	 * @param index
	 * @param range
	 * @return
	 */
	public int  nearbyNodeIndexAround(int index,int range){
		int node=-1;
		Integer ix	= Integer.valueOf(index);
		for(int i=ix-range; i< ix+range; i++){
			if ((getColorIndex().containsKey(Integer.valueOf(i))) ) {
				node= i;
				break;
			}
		}
		return node;
	}


	/**
		 * Interpolate between the colours in the index array
		 * @param useAlpha
		 */
		public void interpolate(boolean useAlpha){
	
			ColorInt color2;
			ColorInt color1;
			
			int[] index = new int[colorIndex.size()];
			int i=0;
	
			// Find all the indexes we want to interpolate between
			for (Integer key : colorIndex.keySet()) {
				index[i]= key;
				i++;
			}
			// The dColor is the difference of 2 neighbouring colours in the index colourmap 
			float dColor[]= new float[4];
			// The interval is the difference in the array between 2 colours
			int interval;	
			// 
			for (i = 0; i < index.length-1; i++) {
				color1 =colorIndex.get(index[i]);
				color2 =colorIndex.get(index[i+1]);
	
				// We interpolate each of the colour Component across the interval
				// Need to use floatingpoint values during interpolation to get smooth transitions
				interval = index[i+1]-index[i];
	//			System.out.println("interval"+i+" = "+interval); 
				dColor[0] = (float)(color2.getRed() - color1.getRed())/(1.0f*interval);
				dColor[1] = (float)(color2.getGreen() - color1.getGreen())/(1.0f*interval);
				dColor[2] = (float)(color2.getBlue() - color1.getBlue())/(1.0f*interval);
				dColor[3] = (float)(color2.getAlpha() - color1.getAlpha())/(1.0f*interval);
	//			System.out.println("dColor rgba "+ dColor[0] + " "+dColor[1]+" "+dColor[2]+" "+
	//					dColor[3]+" interval:"+interval +"  ix "+index[i]+" to "+index[i+1]); 
				for (int j = 0; j < interval+1; j++) {
					colors[index[i]+j].red = (int) (color1.red+j*dColor[0]);
					colors[index[i]+j].green = (int) (color1.green+j*dColor[1]);
					colors[index[i]+j].blue = (int) (color1.blue+j*dColor[2]);
					if(useAlpha) colors[index[i]+j].alpha = (int) (color1.alpha+j*dColor[3]);
				}
			}
	
		}

	/**
	 * 
	 * @param steps
	 * @return
	 */
	public ColorGradient makeStepwise(int steps){
		int di= 256/steps;
		int lastIndex=0;
		ColorGradient cg = new ColorGradient(max, min);
		for (int i = 0; i < colors.length; i+=di) {
			if(i<256 && (i+di-1)<256){
			cg.insert(colors[i], i);
			cg.insert(colors[i], i+di-1);
			lastIndex=i;
			}
			//32*7= 224  32*8= 256
			if (lastIndex< 255) {
				cg.insert(colors[lastIndex], 255);
			}
		}
		cg.interpolate(false);

		return cg;
	}
	//25.6/8= 3.2 25.6/6= 4.26666666666666 42.6*5= 213 42.6*7= 298.199999999999 

	/**
	 * 
	 * @return
	 */  // Not really used! 
	public ColorGradient makeStepwise(){
		
		ColorGradient cg = new ColorGradient(max, min);
		Iterator<Map.Entry<Integer, ColorInt>> it = colorIndex.entrySet().iterator();
		
		int n =0;
		n= it.next().getKey() ; //should be zero
		//		
		for (Integer key : colorIndex.keySet()) {
			ColorInt color=colorIndex.get(key);
			cg.insert(color, key);
			// Insert the same colour just below the next one
			if (it.hasNext() ) {
			n = it.next().getKey() ; 
			cg.insert(color,n-1);
			}
			System.out.println(" Inserting colour: "+color+"  at "+key+" & "+n);
		}
		//
		it= cg.colorIndex.entrySet().iterator() ;
		while (it.hasNext()) {
		    Map.Entry<Integer, ColorInt> pair = it.next();
			
			System.out.println(pair.getKey()+"="+ pair.getValue().toInt());
		}
		
		cg.interpolate(false);
		
		return cg;
	}


	/**
	 *Inserts a colour node at the given index
	 * @param c - The java.awt.Color to be inserted
	 * @param index - The index where the colour is to be inserted
	 */
	public void insert(Color c, int index){
	
		if (index<256 && index>=0) {
			ColorInt color = new ColorInt(c); 
			colors[index].set(c);
			colorIndex.put(index, color);			
		}		
	}


	/**
	 * Inserts a colour node at the given index
	 * @param c - The ColorInt to be inserted
	 * @param index - The index where the colour is to be inserted
	 */
	public void insert(ColorInt c, int index){
	
		if (index<256 && index>=0) {
			ColorInt color = new ColorInt(c); 
			colors[index].set(color.makeColor());
			colorIndex.put(index, color);			
		}		
	}


	/**
	 * Finds the index of a given value.
	 * @param value - The value we want to find the index of 
	 * @return
	 */
	public int findIndex(float value) {
		int index = (useLogarithmic)  ? retrieveLogarithmic(value) :
			(int) Math.max(0, Math.min(255, ((value-min)/step))) ;

		return index;
	}
	
	/**
	 * Finds the value of the given index
	 * @param index the valuer we want to find the index
	 * @return the given value at the given index
	 */
	public float findValueFromIndex(int index){
		float v= index*step+min;
		return v;
	}



	/**
	 * Retrieves the value given a colour
	 * @param color The colour we want to find the value of the
	 * @return This corresponding value of this colour in the gradient
	 */
	public float retrieveValueFromColor(int color){
		float value=0;
		boolean found=false;
		int c=0;
		for (int i = 0; i < colors.length  && !found; i++) {
			c = colors[i].toInt();
			if (color==c){
				value= min+step*i;
				found  = true;
			}
	
		}
		return value;
		
	}


	/**
	 * Retrieves the colour from the array of colours given a value
	 * @param value - the value
	 * @return	- An int  representation of the corresponding colour
	 */
	public int retrieveColorInt(float value){
		int index = findIndex(value); 
		return colors[index].toInt();
	}



	/**
	 * Retrieves the colour from the array of colours given a value
	 * @param value - the value
	 * @return	- The colour corresponding to the value
	 */
	public ColorInt retrieveColor(float value){
		
		int index = findIndex(value);
//		if (index>255) {
//			System.out.println("Index to big…"+index+" non-linear:"+useLogarithmic+" v:"+value );
//		}
//		index= (index<0) ? 0:index;
		return colors[index];
	}


	/**
	 * 
	 * @param value
	 * @return
	 */
	private int retrieveLogarithmic(float value){
		int index=0;
		double maxmax = max*max;
//		float v= (float) (Math.pow(value, 2)/maxmax);
		float v= (float)(255* Math.log10(value)/ max);
		
		index = (int) Math.max(0, Math.min(255, v) );
		return index;
	}
	

	/**
	 * Make sure the opacity setting does not go out of range
	 */
	public void clampOpacity(){
		opacityMinMax.x= Math.min( Math.max(0,opacityMinMax.x) , 255);
		opacityMinMax.y= Math.min( Math.max(0,opacityMinMax.y) , 255);
		opacityRange.x=  Math.min( Math.max(0,opacityRange.x) , 255);
		opacityRange.y=  Math.min( Math.max(0,opacityRange.y) , 255);
	}



	/**
	 * 
	 * @return
	 */
	public String stringOfColors(){
		String s="";
		
		// Go through all the colour indexes and make a text string		
		for (Integer key : colorIndex.keySet()) {
			ColorInt color=colorIndex.get(key);
			String sc= color.toString();
			s+= sc+"";
			}
		
		return s;
	}
	
	//	int r = (argb>>16)&0xFF; //int g = (argb>>8)&0xFF; //	int b = (argb>>0)&0xFF;
	
	//not used?
	private void fillImage(BufferedImage b){
		Graphics2D g= b.createGraphics();
		g.setColor(new Color(0.2f,0.2f,0.2f,0.2f));
//		g.fillRoundRect(0, offy-6, b.getWidth()-0, b.getHeight()-offy, 15,15);
		g.drawRoundRect(0, offy-6, b.getWidth()-0, b.getHeight()-offy, 15,15);
		g.dispose(); 
	}

	/**
	 * Import from an external image
	 * @param bi BufferedImage to import from
	 */
	private void importImage(BufferedImage bi){
		int  di=8;
		int  maxy=256;
		if (bi !=null) {
			clear(Color.black);
			for (int i = 0; i < colors.length+1  && i<maxy; i+=di) {
				ColorInt c= new ColorInt(bi.getRGB(10, bi.getHeight()-i-1));
				insert(c, i);
			}
			// insert index 255 also
			ColorInt c= new ColorInt(bi.getRGB(10, 0));
			insert(c, 255);
			//8*16= 128  8*22= 176  8*30= 240 8*34= 272 8*32= 256 8*31= 248
			define();
		}
	}

	/**
	 * Import the gradient from a file
	 * @param file the file to import from
	 */
	public void importFrom(File file) {

		if (file!=null) {

			try {
				BufferedImage bi=null;
				bi= ImageIO.read(file);
				if (bi!=null) {
					
				//Rescale the image 1st
				BufferedImage biScale= GraphicsUtil.resizeImage(bi, 
						getImage().getWidth(), 256+ getOffy());
				importImage(biScale);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("no valid image file selected");
			}
		}

	}



	/**
	 * 
	 * @return
	 */ ////Label2
	public BufferedImage drawGradientOnly(){
		int w=20;
		BufferedImage bi = new BufferedImage(w, 256, BufferedImage.TYPE_INT_ARGB); 
		// Loop through all the colours 
		for (int j = 0; j < colors.length; j++) {
			int color = colors[j].toIntOpaque();
			// The colours in the ColourGradient
			for (int i = 0; i < w ;i++) {
				bi.setRGB(i, bi.getHeight()-j-1, color);
			}
			//Set meta information about how the gradient is defined as well:
			

		}
		return bi;
	}

	
	/**
		 * Draw the gradient on a BufferedImage
		 * @param b
		 */
		public void drawGradient(){
			int color=0;
			int greyscale=0;
			int awidth=7;
			int width= (int) (image.getWidth()*0.3f);
			int miny=0;
			offy=20;
			
			if (useBackground) {
				fillImage(image);
			}
			
			// Loop through all the colours 
			for (int j = 0; j < colors.length; j++) {
				color = colors[j].toIntOpaque();
				greyscale = colors[j].alphaAsGreyscale();
				
				// This is offset from the bottom of the image
				// Painting is done from this position and upward
				miny= Math.max(0, image.getHeight()-j-1-offy);
				
				// Opacity as Greyscale
				for (int i = 2; i < awidth; i++) {
					image.setRGB(i, miny, greyscale);
				}
				// The colours in the ColourGradient with transparency
				for (int i = awidth+2; i < awidth+6 ;i++) {
					image.setRGB(i, miny, colors[j].toInt());	
				}
				// The colours in the ColourGradient
				for (int i = awidth+8; i < width ;i++) {
					image.setRGB(i, miny, color);	
				}
			}
			// Draw labels
			drawText(image, width-3 );
		
		}


	/**
	 * Draw tick marks and labels for this gradient 
	 * @param b - the BufferedImage to draw upon
	 * @param x - the horizontal position within the image where ticks should be drawn
	 * @param off - offset pixels at the top and at the bottom
	 */
	private void drawText(BufferedImage b,int x){
		Graphics2D g= b.createGraphics();
		String text=""; 
		int stepValue= colors.length/8;//colorIndex.size();
		int tickLength=6; 
		String format1= (max> 10000  ) ?  "%.1e"  :"%.2f";
		

		// rendering hint
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

		
		g.setColor(new Color(120,120, 150));
		int y;
		// Draw the tick marks
		for (int j = 0; j < colors.length+1; j+= stepValue) {
			y= b.getHeight()-j-1-offy;
			g.drawLine(x, y,   x+tickLength, y);
		}
	
		g.setFont(new Font("Calibri", Font.PLAIN, 15));
		// Draw the text strings with the values
		g.setColor(new Color(0,0,0,255
				));
		for (int j = 0; j < colors.length+1; j+= stepValue) {
//			text= String.valueOf(min + step*j);
			
			text = String.format(format1,min+step*j);
			y= b.getHeight()-j-0-offy;
			g.drawString(text, x+tickLength+4, y+3);
		}
		
		g.dispose();
	}


	/**
	 * Sets the opacity linearly between 0 and 255
	 */
	public void setTheOpacityLinear(){
		for (int i = 0; i < colors.length; i++) {
			colors[i].alpha = Math.min(255, i);//Math.min(opacityMinMax.y, Math.max(opacityMinMax.x, i));
		}
	}



	/**
	 * Sets the opacity linearly limited by maximum and minimum values and a range
	 */
	public void setOpacityWithMask() {
		int from = Math.max(0, opacityRange.x);
		int to =   Math.min(opacityRange.y, colors.length);
		
		//First set everything below minimum
		for (int i = 0; i < from; i++) {
			colors[i].alpha=opacityBelowMin;
		}
		//Then set everything above the maximum
		for (int i = to; i < 256; i++) {
			colors[i].alpha=opacityMinMax.y;
		}
		
		//Then set alpha values linear within the range
		int dv= Math.max(opacityMinMax.y-opacityMinMax.x, 1);
		float dx= (float) 1.0f *dv/(to-from);
//		System.out.println("dv:"+dv+" dx:"+dx);
		for (int i = from; i < to; i++) {
			colors[i].alpha = (int) (opacityMinMax.x+ dx*(i-from));//Math.min(opacityMinMax.y, Math.max(opacityMinMax.x, i));
		}
	}



	/*
		//
		public static void main(String[] args) {
	
	
			ColorGradient gradient= new ColorGradient(100, 0);
			int a= 25;
			gradient.clear(Color.white,a);
			
			gradient.insert((Color.white),0);//0,0,0,a), 0); //white
			gradient.insert(ColorGradient.GREY_BLUE, 90);
			gradient.insert(Color.green, 150);
			gradient.insert(Color.orange,200);
			gradient.insert(new Color(255,0,0,a), 255);
			//
			gradient.interpolate(true);
			gradient.setTheOpacityLinear();
			
			// This is the value we try – and colour we retrieve
			// •<>•<>•<>•<>•<>•<>•<>•<>•<>•<>•
			
			// rgb = 65536 * r + 256 * g + b;
	//		}
			
			ColorGradient g2= new ColorGradient(100,0);
			ColorGradient g3= new ColorGradient( 100,0);
			ColorGradient g4= new ColorGradient( 1000000,00);
			
//			GraphicsUtil.produceGradientDiscreetFrom(g2, new ColorSet("blue").colors);
			GraphicsUtil.produceGradientDiscreetFrom(g3, new ColorSet("orange").colors);
//			GraphicsUtil.produceGradientDiscreetFrom(g4, new ColorSet("purple").colors);
			g4= gradient.makeStepwise(8);
			
			gradient.test();
			g2.test();
			g3.test();
			g4.test();
		}
		*/

	
	/**
		 * 
		 */
		public void test(){
	
			drawGradient();
			
//			JPanel p= new JPanel(image,image.getWidth()+80,image.getHeight()+50,false);
//			
//			JFrame frame= new ImageFrame("Gradient",p, new Rectangle());
//			frame.setLocation(50, 90);
//			frame.setMinimumSize(p.getSize());
	//		frame.add(new JPanel()); 
		}


	public ColorInt[] getColors() {
		return colors;
	}

	public void setColors(ColorInt[] colors) {
		this.colors = colors;
	}

	public BufferedImage getImage() {
		return image;
	}


	public void setImage(BufferedImage image) {
		this.image = image;
	}


	/**
	 * @return the opacityRange
	 */
	public Point getOpacityRange() {
		return opacityRange;
	}



	/**
	 * @param opacityRange the opacityRange to set
	 */
	public void setOpacityRange(Point opacityRange) {
		this.opacityRange = opacityRange;
	}



	/**
	 * @return the opacityMinMax
	 */
	public Point getOpacityMinMax() {
		return opacityMinMax;
	}



	/**
	 * @param opacityMinMax the opacityMinMax to set
	 */
	public void setOpacityMinMax(Point opacityMinMax) {
		this.opacityMinMax = opacityMinMax;
	}



	/**
	 * @return the opacityBelowMin
	 */
	public int getOpacityBelowMin() {
		return opacityBelowMin;
	}



	/**
	 * @param opacityBelowMin the opacityBelowMin to set
	 */
	public void setOpacityBelowMin(int opacityBelowMin) {
		this.opacityBelowMin = opacityBelowMin;
	}



	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public void setName(String name) {
		this.name = name;
	}



	public String getName() {
		return name;
	}



	public void setMax(float max) {
		this.max = max;
	}



	public float getMax() {
		return max;
	}



	public Map<Integer, ColorInt> getColorIndex() {
		return colorIndex;
	}


	public void setColorIndex(Map<Integer, ColorInt> colorIndex) {
		this.colorIndex = colorIndex;
	}



	/**
	 * @return the useOpacityMask
	 */
	public boolean isUseOpacityMask() {
		return useOpacityMask;
	}



	/**
	 * @param useOpacityMask the useOpacityMask to set
	 */
	public void setUseOpacityMask(boolean useOpacityMask) {
		this.useOpacityMask = useOpacityMask;
	}



	/**
	 * @return the useLogarithmic
	 */
	public boolean isUseLogarithmic() {
		return useLogarithmic;
	}



	/**
	 * @param useLogarithmic the useLogarithmic to set
	 */
	public void setUseLogarithmic(boolean useLogarithmic) {
		this.useLogarithmic = useLogarithmic;
	}



	/**
	 * @return the useBackground
	 */
	public boolean isUseBackground() {
		return useBackground;
	}



	/**
	 * @param useBackground the useBackground to set
	 */
	public void setUseBackground(boolean useBackground) {
		this.useBackground = useBackground;
	}

	/**
	 * @return the offy
	 */
	public int getOffy() {
		return offy;
	}

	/**
	 * @param offy the offy to set
	 */
	public void setOffy(int offy) {
		this.offy = offy;
	}

	/**
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

	

	//	int r = (argb>>16)&0xFF; //int g = (argb>>8)&0xFF; //	int b = (argb>>0)&0xFF;


}
