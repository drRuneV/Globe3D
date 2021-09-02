package GeometryGL;

import static org.lwjgl.opengl.GL11.*;

import java.awt.event.KeyEvent;
import java.io.File;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Color;
import org.newdawn.slick.opengl.Texture;

import Colors.ColorGradient;
import Colors.ColorInt;
import Utility.Coordinate;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.OpenGLTesting;
import basicLwjgl.Vector3D;
import Colors.GradientPanel;
import distribution.Distribution;

//  • idea: we could use indexes to avoid looping through positions which we do not need
//  like land areas	–	index[i] – this would be the indexing in the distribution

public class GeoGrid {
	
	static float PI= (float) Math.PI;
	//
	private Coordinate center= null;
	protected Coordinate coordinate[][]= null;
	protected Vector3D vertex[][]=null;
	protected ColorInt colors[][]= null;
	protected int width=1;	// longitude
	protected int height=1;	// latitude
	protected float radius=1;
	
	// Graphics 
	protected boolean useFill= true;
	protected boolean useLight = false ;
	protected float opacity= 0.7f;
	//
	protected ColorGradient gradient= new ColorGradient(1, 0);
	private int defaultGN=0;
	// 
	protected int displayList=-1; // Remember to set to -1 in order to work!
	protected boolean doRecreate= true;
	protected boolean useTexture= false;
	protected Texture texture= null;
	
	
	/**
	 * Constructor
	 * @param coordinate
	 * @param width
	 * @param height
	 * @param radius
	 */
	public GeoGrid(Coordinate[] coordinate, int width,int height, float radius, String name){
		this.width=width;
		this.height = height ;
		this.coordinate= createCoordinates(coordinate);//new Coordinate[width][height];
		this.radius= radius;
		vertex= new Vector3D[width][height] ;
		colors= new ColorInt[width][height];
		defineGradient((float) (1*Math.pow(10, 9)),0,name);
	}


	public GeoGrid(int width,int height, float radius){
		this.width=width;
		this.height = height ;
//		this.coordinate= createCoordinates(coordinate);//new Coordinate[width][height];
		this.radius= radius;
		vertex= new Vector3D[width][height] ;
		colors= new ColorInt[width][height];
		createColours();	
	}
	
	
	/**
	 * 
	 */
	public void defineGradient(float max, float min,String name) {
		System.out.println("max= "+max);
		gradient=new  ColorGradient(max,min);
		GradientPanel gp= new GradientPanel(gradient);
		gp.openGradientInformation(new File("./res/Gradients/"+name), gradient);
		gradient.define();
//		gradient.importFrom();
	}


	/**
	 * Makes a regular coordinate grid from the given coordinates
	 * @param coo the coordinates
	 * @return a new two-dimensional coordinate grid
	 */
	public Coordinate[][] createCoordinates(Coordinate[] coo){
		Coordinate[][] c= new Coordinate[width][height];
		int ic= 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				c[x][y]=new Coordinate(coo[ic]);
				ic++;
			}
		}
		
		return c;
	}
	
	
	/**
	 * Creates the list of colour objects to be used.
	 * All colours are transparent black.
	 */
	public void createColours(){
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				colors[x][y] = new ColorInt(0.0f,0.0f,0.0f,0.0f) ; // Transparent black
			}
		}
		
	}
	
	
	/**
	 * Creates all the grid position in 3D space for the given coordinates
	 */
	public void createVertexes(){

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float lon= coordinate[x][y].getLon();
				float lat= coordinate[x][y].getLat();
				vertex[x][y]= new Vector3D(latLonToPoint(lat, lon)) ;
				vertex[x][y].scaleVector(radius); ;
			}
		}
	}
	
	/**
	 * Creates all the grid position in 3D space for the given coordinates 
	 * using heights.
	 */
	public void createVertexesWithHeights(float[] heights, float scale){
		float scaleLand = scale*1.0f ;
		float sc= 1;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float lon= coordinate[x][y].getLon();
				float lat= coordinate[x][y].getLat();
				vertex[x][y]= new Vector3D(latLonToPoint(lat, lon)) ;
				sc = (heights[x+y*width]>0) ? scaleLand: scale ;
				vertex[x][y].scaleVector(radius);
				vertex[x][y].scaleVector((radius+ heights[x+y*width]*sc)/radius) ;
			}
//			System.out.println(" height used: "+heights[0+y*width]);
		}
		
		// this is done Globe: vertex[i][j].scaleVector((r+h)/r );
	}

	
	/*
	public void render(Distribution distribution){
		float x,y,z;
		float x1,y1,z1;
		Vector3D n= new Vector3D();
		Vector3D n1= new Vector3D();

		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);     
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);

		glPolygonMode(GL_BACK,(useFill) ? GL_FILL:GL_LINE);
		glPolygonMode(GL_FRONT, (useFill) ? GL_FILL:GL_LINE);
		glDisable(GL_CULL_FACE);


		//	    glLogicOp(GL_COPY);
		//	    glColorMask(false,true,true,true); // Just testing the effects «•»


		if (colors==null) {
			createColours();
		}
		if (getCenter()==null) {
			calculateCenter();			
		}

		// 
		int t=distribution.count();
		int wh=distribution.getWH();
		float values[] = distribution.getValues();

		//  – Grid loop – //  –	// –	// –	// –	// –	// 
		for(int iy = 1; iy < height; iy+=1) {

			glBegin(GL_TRIANGLE_STRIP);

			for (int ix = 0; ix < width; ix+=1) {

				//  To make height as a function of value
				//	    		v= values[ix+(iy-1)*width+t*wh];
				//	    		vertex[ix][iy].scaleVector( (float) ((radius+v*0.0000000001)/radius) );

				x=vertex[ix][iy-1].x;   
				y=vertex[ix][iy-1].y;
				z=vertex[ix][iy-1].z;
				x1=vertex[ix][iy].x;	// 
				y1=vertex[ix][iy].y;
				z1=vertex[ix][iy].z;
				// Normal vectors
				n.setTo(x, y, z);
				n.norm();
				n1.setTo(x1, y1, z1);
				n1.norm();

				// 	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	

				ColorInt color[]  = generateColour(values[ix+(iy-1)*width+t*wh],values[ix+iy*width+t*wh],distribution.getFillV()) ;
				//	    		System.out.println("color "+color[0] +" color1"+ color[1]);
				float c[]= color[0].convertToFloat();
				float c1[]= color[1].convertToFloat();

				//P1 (page 111 in the OpenGL super Bible) //Label2
				glColor4f(c[0],c[1],c[2],c[3]);
				glNormal3f(n.x,n.y, n.z);
				glVertex3f(x,y,z);//v[i-1][j].x,v[i-1][j].y,v[i-1][j].z);

				// P2
				glColor4f(c1[0],c1[1],c1[2],c1[3]);
				glNormal3f(n1.x,n1.y,n1.z);
				glVertex3f(x1,y1,z1);
				//	    		System.out.println("xyz:"+x+" "+y+" "+z+" "+x1+" "+y1+" "+z1);
			}
			glEnd();
		}
	}
	
	*/
	
	public void simpleRender(ColorInt color, int fill){
		
		Vector3D vx[][]= getVertex();
		glColor4f(color.red, color.green, color.blue, color.alpha);
		glColor4f(1.0f, 0.5f , 0.0f , 0.50f); // yellow


		glDisable(GL_LIGHTING);
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glPolygonMode(GL_FRONT_AND_BACK,  fill);

		glBegin(GL_QUADS);
		glVertex3f(vx[0][0].x,vx[0][0].y,vx[0][0].z );
		glVertex3f(vx[width-1][0].x,vx[width-1][0].y,vx[width-1][0].z );
		glVertex3f(vx[width-1][height-1].x,vx[width-1][height-1].y,vx[width-1][height-1].z );
		glVertex3f(vx[0][height-1].x,vx[0][height-1].y,vx[0][height-1].z );
		glEnd();




	}

	/**
	 * If a given coordinate is inside the grid
	 * @param c the given Coordinate to check for
	 * @return true or false
	 */
	public boolean isInside(Coordinate c){
		boolean is= false;
		float lat=c.getLat();
		float lon=c.getLon();
		Coordinate upperLeft= coordinate[0][0];
		Coordinate lowerRight= coordinate[width-1][height-1];
		if (lat> upperLeft.getLat() && lat< lowerRight.getLat() && 
				lon> upperLeft.getLon() && lon< lowerRight.getLon() ) {
			is= true;
		}
		
		return is;
	}
	
	
	/**
	 * Calculates the centre coordinate for this grid
	 */
	public void calculateCenter(){
		setCenter(new Coordinate(0, 0));
		int length = width*height;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				getCenter().addLonLat(coordinate[x][y].getLon(),coordinate[x][y].getLat());
			}
		}
		getCenter().setLat(getCenter().getLat()/length);
		getCenter().setLon(getCenter().getLon()/length);
		System.out.println(" centre coordinate:" +getCenter().toString());
	}


	/**
	 * Generate 2 colours for the 2 different values
	 * @param value0 first value
	 * @param value 2nd value
	 * @param landv the value for landmass
	 * @return 2 new colours ColorInt
	 */
	public ColorInt[] generateColour(float value0, float value,float landv){
		ColorInt[] color= new ColorInt[2] ;

		color[0] =new ColorInt( 0.0f,0.0f,0.0f,0); // Do not forget to create first !
		if (value0!= landv) {
			color[0]= gradient.retrieveColor(value0);
		}
		// 
		color[1] =new ColorInt(0.2f, 0.5f, 0.2f, 0.0f);
		if (value!= landv) {
			color[1]= gradient.retrieveColor(value);
		}

		return color;
	}

	
	
	public void handleKeyboard(boolean keyDown){
		boolean shift= CoreOpenGL.isShift();
		boolean alt= CoreOpenGL.isAlt();
		boolean control= CoreOpenGL.isCtrl();

		//  Wireframe or filled 
		if (Keyboard.isKeyDown(Keyboard.KEY_Z)  && shift && keyDown ){
			useFill=!useFill;
			doRecreate= true;
		}
		// Chooses the next default gradient from GradientPanel
		else if (Keyboard.isKeyDown(Keyboard.KEY_9)  && keyDown){
			float	max= gradient.getMax();
			GradientPanel g=  new GradientPanel(gradient);
			int size= g.getListOfGradients().size();
			defaultGN++;
			defaultGN= (defaultGN>  size-1) ? 0 : defaultGN ;

			ColorGradient cg= g.getListOfGradients().get(defaultGN);
			gradient.setColors(cg.getColors());
			gradient.setColorIndex(cg.getColorIndex());
			gradient.defineFromGradient(cg);
			gradient.reDefine();
		}

		
		else if (Keyboard.isKeyDown(Keyboard.KEY_T)  && !keyDown){
		}
		
	}



	
/*
	public static void main(String[] args) {
		
		Distribution  d= OpenGLTesting.testDistribution();
		int width = d.getWidth();
		int height= d.getHeight();
		float radius= 500; 
		GeoGrid geoGrid= new GeoGrid(d.getCoordinates(), width, height, radius,"name");
//		geoGrid.createVertexes();
//		geoGrid.displaySomeValues();
		for (int i = 0; i < geoGrid.width; i++) {
			for (int j = 0; j < geoGrid.height; j++) {
				System.out.println(i+":"+j+" "+geoGrid.getCoordinate()[i][j].toString());
			}
		}
		geoGrid.calculateCenter();
		System.out.println(" centre "+geoGrid.getCenter());
		
		Coordinate c= new Coordinate(0, 5);
		for (int i = 0; i < 90; i++) {
			c.setLat(i);
			c.setLon(-20+i*0.4f);
			boolean is= geoGrid.isInside(c);
			System.out.println("Inside "+c.toString()+" = "+is);
		}
	}
	*/

	/**
	 * Converts latitude and longitude to a position in space
	 * @param lat 
	 * @param lon
	 * @return a Vector3D
	 */
	public  static Vector3D latLonToPoint(float lat,float lon){
		// First convert to radians
		lat= lat*PI/180;
		lon= 180-lon; // the texture starts on the other side…i.e. longitude 0 is at -x Axis
		lon= lon*PI/180;
		float L0  = (float) Math.cos(lat);  //  projection xz
			
		float x = (float) (Math.cos(lon)*L0);
		float y = (float) (Math.sin(lat)*1); // Radius=1
		float z=  (float) (Math.sin(lon)*L0); 

		Vector3D p= new Vector3D(x,y,z);	
		return p;
	}

	public void indicateCoordinate(Coordinate c, float radius){
		Vector3D position= latLonToPoint(c.getLat(), c.getLon());
		position.scaleVector(radius*1.001f);
		Vector3D position2 = position.scaledTo(1.05f) ;
		
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glColor4f(1.0f, 0.0f , 0.0f , .70f);
		float r=(float) (Math.random()*4);
		glPointSize(r*2.0f);
		glBegin(GL_POINTS);
//		for (int i = 0; i < 2; i++) {
			glVertex3f(position.x,position.y ,position.z);
			glVertex3f(position2.x,position2.y ,position2.z);
//		}
		glEnd();

		glBegin(GL_LINES);
		glVertex3f(position.x,position.y ,position.z);
		glVertex3f(position2.x,position2.y ,position2.z);
		glEnd();
		
		
		
//		System.out.println("pos "+position.information());
		
//		glBegin(GL_TRIANGLES);
//		glVertex3f(position.x,position.y ,position.z );
//		glVertex3f(position.x+2,position.y ,position.z );
//		glVertex3f(position.x,position.y+2 ,position.z );
//		glEnd();
	}
	
	
	
	
	
	public void displaySomeValues() {
		String s="", sv;
		int ix= 0;
		if (coordinate!=null) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					s=String.format(" = %s", vertex[x][y].information() );
//					sv=String.format("Value=%.2f", );
					System.out.println(x+":"+y+  " c:"+coordinate[x][y].toString()+s );
					ix++;
				}
			}
		}		
	}


	/**
	 * @return the gradient
	 */
	public ColorGradient getGradient() {
		return gradient;
	}


	/**
	 * @param gradient the gradient to set
	 */
	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}


	/**
	 * @return the center
	 */
	public Coordinate getCenter() {
		return center;
	}


	/**
	 * @param center the center to set
	 */
	public void setCenter(Coordinate center) {
		this.center = center;
	}


	/**
	 * @return the colors
	 */
	public ColorInt[][] getColors() {
		return colors;
	}


	/**
	 * @param colors the colors to set
	 */
	public void setColors(ColorInt[][] colors) {
		this.colors = colors;
	}


	/**
	 * @return the vertex
	 */
	public Vector3D[][] getVertex() {
		return vertex;
	}


	/**
	 * @param vertex the vertex to set
	 */
	public void setVertex(Vector3D[][] vertex) {
		this.vertex = vertex;
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


	/**
	 * @return the coordinate
	 */
	public Coordinate[][] getCoordinate() {
		return coordinate;
	}


	/**
	 * @param coordinate the coordinate to set
	 */
	public void setCoordinate(Coordinate[][] coordinate) {
		this.coordinate = coordinate;
	}
	

}
