package distribution;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import Colors.ColorGradient;
import Colors.ColorInt;
import GeometryGL.CoordinateMesh;
import GeometryGL.DistributionGL;
import GeometryGL.GeoGrid;
import GeometryGL.Globe;
import Utility.Clock;
import Utility.Coordinate;
import basicLwjgl.Camera;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.SlickGraphic;
import basicLwjgl.Vector3D;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

//Variable :u(time=365, s_rho=30, eta_u=124, xi_u=168) averaged u-momentum component
//Variable : v(time=365, s_rho=30, eta_v=123, xi_v=169) averaged v-momentum component
//Variable : lat_u(eta_u=124, xi_u=168)
//Variable : lat_v(eta_v=123, xi_v=169)



/**
 * CurrentField
 * 
 * @author Admin
 *
 */
public class CurrentField {
	
	Distribution v= null;
	Distribution u= null; 
	String coordinateNames[]= { "lat_u", "lon_u", "lat_v", "lon_v"};
	// This is the vector field
	Vector3D field[][][] = null;
	// This is the value, i.e. either current magnitude or maybe temperature
	float values[][][] = null;
	int width= 0;
	int height= 0;
	// Color
	
	
	private Coordinate coordinate[];
	int ushape[];
	int vshape[];
	//
	private boolean pause= true ;
	private boolean useAnimation= true;
//	private CoordinateMesh vMesh;   // for test drawing
	private GeoGrid grid= null;
	private int count= 0;
	private int renderCount= 0;
	private int renderCount2= 0;
	private Clock clock= null;
	// Visual
	private ColorGradient gradient= null;
	private float scale= 20f; //  visual scaling of final vector
	private float linew= 2.0f;
	//  Grid step, i.e. 1 for all cells
	private int stepInterval=4 ;
	// Magnitude Threshold for when current is visible
	private float visualThreshold=0.01f;
	private float pSize = 1;
	

	/**
	 * Constructor
	 * @param ncfile
	 */
	public CurrentField(NetcdfFile ncfile) {
		u= new Distribution(ncfile) ;
		v= new Distribution(ncfile) ;
		u.alternativeCreate("u", "lat_u", "lon_u");
		v.alternativeCreate("v", "lat_v", "lon_v");
		
		// The shape is the size of all dimensions, e.g. time depth height with
		// ushape[]= {time=365, s_rho=30, eta_u=124, xi_u=168}
		ushape=u.getShape();
		vshape=v.getShape();
		//  We test with the v to start with
		coordinate= v.getCoordinates();
		width= vshape[3]; // should be – Math.min(vshape[3],ushape[3])
		height = vshape[2];// …
		
		gradient= CoreOpenGL.defineGradient(0.6f, 0, "current2.txt");
		clock= new Clock(2010);
		// 
		// GeoGrid
		grid = new GeoGrid(coordinate,v.width, v.height, Globe.Radius+1, "default1.txt") ;
		grid.createVertexes();
//		grid.createColours();
		// 
		renderCount= 0;// (count==0) ? 0 : renderCount; 
		renderCount2=(int) scale*2 ;// (count==0) ? (int) (scale*dt*0.5f) : renderCount2 ; // opposite phase…
		
	
		createField();
		createValues(false); // true if temperature available and used for colouring
	}

	
	
	public void createField() {
		field= new Vector3D[width][height][ushape[0]] ; // ushape[0] is time
		Coordinate ufrom = new Coordinate(u.upper) ;
		Coordinate uto= new Coordinate(u.upper);
		Coordinate vfrom= new Coordinate(v.upper);
		Coordinate vto= new Coordinate(v.upper);
		// v u have different shapes
		int wh= width*height ; // for v … !
		int widthU= ushape[3];
		int heightU= ushape[2];
		int whU= widthU*heightU ; // for u … !
		float valueU= 0;
		float valueV= 0;
		Vector3D pu1= new Vector3D(); //  first coordinate
		Vector3D pu2= new Vector3D(); // second coordinate
		Vector3D pu= new Vector3D();  // vector between
		Vector3D pv1= new Vector3D();
		Vector3D pv2= new Vector3D();
		Vector3D pv= new Vector3D(); 
		Vector3D vector= new Vector3D();


		// eta_v=123, xi_v=169 ! vs U y=124,x=168
		// Assume that u is the component along the x direction in U coordinate grid
		// Assume that v is the component along the y direction in V coordinate grid
		for (int t=0; t< v.time; t++) {
			for (int y = 0; y < height-1; y++) { // v has the smallest height 123 vs 124
				for (int x = 0; x < widthU-1; x++) { // u has the smallest width 168 vs 169
					//  u is directed along x, i.e. x->x+1
					ufrom = u.getCoordinates()[x+y*widthU] ;
					uto= 	u.getCoordinates()[x+1 +y*widthU] ;
					valueU= u.getValues()[x+y*widthU+t*whU] ;
					pu1= GeoGrid.latLonToPoint(ufrom.getLat(), ufrom.getLon());
					pu2= GeoGrid.latLonToPoint(uto.getLat(), uto.getLon());
					pu= pu2.subtract(pu1).normalised().scaledTo(valueU);

					// v is directed along y, i.e. y->y+1
					vfrom= v.getCoordinates()[x+y*width] ;
					vto=   v.getCoordinates()[x+(y+1)*width] ;
					valueV=v.getValues()[x+y*width +t*wh] ;
					pv1= GeoGrid.latLonToPoint(vfrom.getLat(), vfrom.getLon());
					pv2= GeoGrid.latLonToPoint(vto.getLat(), vto.getLon());
					pv= pv2.subtract(pv1).normalised().scaledTo(valueV);
					// Final vector scaled
					vector= pu.add(pv);// .scaledTo(scale); // taken care of when drawing!
					field[x][y][t] = new Vector3D(vector);

					// 
					//				System.out.println(String.format("indexU :x %d y %d %d V: %d",x,y, (x+y*widthU),(x+y*width)));

					// showValues(valueU, valueV, vector);
				}
			}
		}

	}
	
	/**
	 * Grace the values associated with each cell in the field, i.e. magnitude of the vector
	 * @param useTemperature if we are to use temperature as the value of the field
	 */
	public void createValues(boolean useTemperature) {
		values= new float[width][height][ushape[0]] ; // ushape[0] is time
		
		// 
		for (int t=0; t< v.time; t++) {
			for (int y = 0; y < height-1; y++) { // v has the smallest height 123 vs 124
				for (int x = 0; x < u.width-1; x++) { // u has the smallest width 168 vs 169
				values[x][y][t]= field[x][y][t].length() ;	
				}
			}
		}
		
	}
	
	
	/**
	 * Renders the current field as lines with points
	 * @param change if we are going to change the field, i.e. count for this rendering
	 * @param psize point size to use
	 */
	public void render(boolean change, float psize, float camHeight){
		
		setGLStraightenDrawing();

		// 
		//  Remember the point size
		pSize = psize ;

		//  Counting
		if (change && ! isPause() ){
			count(1);			
		}
		// Testing, counting even when isPause 
		if (isPause() && renderCount==0 && isUseAnimation()) {
			count(1);
		}
		count= (count> v.time-1) ? 0: (count<0) ? v.time-1: count;

		// Convenience
		int t  = count ;	
		int widthU= ushape[3];
		Vector3D v[][]= grid.getVertex();
		Vector3D dTo = new Vector3D() ;
		Vector3D dFrom = new Vector3D() ;

		// Render counting
		float  dt= 4;	// counting in 4 × scale steps for visual effect
		
		renderCount++;
		renderCount2++;
		int maxRender = (int) (scale*dt) ;
		renderCount = (renderCount> scale*dt) ? 0  : renderCount;
		renderCount2 = (renderCount2> scale*dt) ? 0  : renderCount2;
//		System.out.println("renderCount "+renderCount+" 2 "+renderCount2);
		
		//  Scale is Max , sc is the length of the vector (increasing) towards maxRender
		float sc1 = (isPause() && isUseAnimation()) ?  renderCount/dt : scale ;
		float sc2 = (isPause() && isUseAnimation()) ?  renderCount2/dt : scale ;
		float sc= sc1;
		int rc= renderCount;
		float maxLength= 15;
		
		// 
		int step = (camHeight> 200) ? ((camHeight> 400) ? 3: 2) : 1 ;
		boolean isShift= false;
		

		glLineWidth(linew);
		ColorInt color= new ColorInt();
		//  Looping through coordinate grid drawing one line for each vector
		glBegin(GL_LINES);
		for (int y = 0; y < height-step; y+=step) { // following v
			for (int x = 0; x < widthU-step; x+=step) { //  u has the smallest width168, field undefined for larger x
				float value=values[x][y][t];//field[x][y][t].length();
				if (value> visualThreshold) {
					// Try every 2nd for visual effect…? 
					isShift= ((x%2==0 || y%2==0)  && false ) ; 
					sc= (isShift) ? sc2 : sc1 ;
					rc = (isShift) ? renderCount2 : renderCount;
					sc = (value*sc> maxLength) ? maxLength/value : sc ;
					//  Color
					color = gradient.retrieveColor(value) ;
					float c[]= color.convertToFloat();
					c[3]*= (isUseAnimation()) ? 1-2.0f*(Math.abs(rc-maxRender*0.5f))/maxRender : 1;
					glColor4f(c[0], c[1] , c[2], c[3]);
					// Vertex
					dTo= field[x][y][t].scaledTo(sc) ;
					dFrom= field[x][y][t].scaledTo(sc*0.5f) ;
					glVertex3f(v[x][y].x+dFrom.x, v[x][y].y+dFrom.y, v[x][y].z+dFrom.z );
					glVertex3f(v[x][y].x+dTo.x, v[x][y].y+dTo.y, v[x][y].z+dTo.z);
				}
			}
		}
		glEnd();

		//  
		glPointSize(psize);
		glBegin(GL_POINTS);
		for (int y = 0; y < height-step; y+=step) { // following v
			for (int x = 0; x < widthU-step; x+=step) {
				
				float value=values[x][y][t];
				if (value>visualThreshold) {
					isShift= ((x%2==0 || y%2==0) && false ) ;
					color = gradient.retrieveColor(value) ;
					// Try every 2nd for visual effect…? 
					sc= (isShift) ? sc2 : sc1 ;
					rc = (isShift) ? renderCount2 : renderCount;
					sc = (value*sc> maxLength) ? maxLength/value : sc ;

					float c[]= color.convertToFloat();
					c[3]*= (isUseAnimation()) ? 1-2.0f*(Math.abs(rc-maxRender*0.5f))/maxRender : 1;
//					c[3]*= (isUseAnimation()) ? 1-1.0f*rc/maxRender : 1;
					glColor4f(c[0], c[1] , c[2], c[3]);
					dTo= field[x][y][t].scaledTo(sc) ;
					glVertex3f(v[x][y].x+dTo.x, v[x][y].y+dTo.y, v[x][y].z+dTo.z);
				}
			}
		}
		glEnd();


	}



	private void setGLStraightenDrawing() {
		glDisable(GL_LIGHTING);
		glDisable(GL_COLOR_MATERIAL);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	
	private void indicateFront(Vector3D p) {
		glPushMatrix();
		glBegin(GL_POINT);
		glVertex3f(p.x,p.y ,p.z );
		glEnd();		
		glPopMatrix();
	}



	public void displayInformation(Camera camera, SlickGraphic slick) {
			
			glDisable(GL_DEPTH_TEST); // enables drawing text on top of the rectangle in the same plane
				
			
			Coordinate c = camera.toCoordinate();
			String unit= (v.getUnit().contains("meter")  && v.getUnit().contains("sec") ) ?
					"m/s" : v.getUnit();
			
			// Displayed distribution values for selected distribution 
			if (camera.determineDistance(1.3f) ) {
				
				int ix= c.findClosestIndex(v.getCoordinates(), 0.9f);
				int ix2= c.findClosestIndex(u.getCoordinates(), 0.9f);
				
				// If Inside distribution area
				if (ix!=-1 && ix2!=-1) {
					ix+= getCount()*v.getWH();
					ix2+= getCount()*u.getWH();
					
					float valueV = v.getValues()[ix] ;
					float valueU = u.getValues()[ix] ;
					String s= (valueV==v.getFillV()) ? "": String.format("%.2f , ", valueV); // "Land"
					String su= (valueU==u.getFillV()) ? "": String.format("%.2f %s", valueU,unit);
					s+=su;
					//			System.out.println(" Is inside : "+c.toString()+" "+s);
					// 
					Point at= new Point(Display.getWidth()/2,Display.getHeight()/2- 40);
					java.awt.Color co= slick.greyColourAt(at, false);			
					slick.drawString(s, at,co);			
				}
			
		}
	}
	
	
	
	/**
	 * Displays the date of the current at the top of the screen
	 * @param slick the slick graphics
	 */
	public void displayDate(SlickGraphic slick) {
		String date = clock.whichMonth();//  getDateString() ;
		date+= " "+clock.getDay();

		// Enables drawing text on top of the rectangle in the same plane
		glDisable(GL_DEPTH_TEST);
		Point at= new Point(Display.getWidth()/2, 40);
		java.awt.Color co= slick.greyColourAt(at, false);			
		slick.drawString(date, at,co);			
	}
	
	
	
	// –// –// –// –// –// –// –// –// –// –// –// –// –// –// –//   
	
	
	
	/*
	public static void main(String[] args) {
		String path="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		String file = "physics.nc" ;
		NetcdfFile ncfile = Distribution.testNetCDFFile(path,file);
		// 
		CurrentField field= new CurrentField(ncfile);
		
		
//		f.inspectFile(ncfile);
		// 
		Coordinate cu[]=field.u.getCoordinates();
		Coordinate cv[]=field.v.getCoordinates();
		System.out.println("cu "+cu.length+ " cv "+cv.length);
		field.createField();
		System.out.println("cu "+cu.length+ " cv "+cv.length);
		
//		int c= 10;
//		int index, vindex;
		/*
		for(int iy=0; iy< field.vshape[2]-s; iy+=s) { // y
			for(int ix=0; ix< field.vshape[3]; ix+=1) {  // x
				
				vindex= ix+iy*field.vshape[3]; 
				uindex= ix+iy*field.ushape[3];
//				System.out.println(vindex+" u "+ cu[uindex]+" v "+ cv[vindex].toString());
//				System.out.println();
			}
		}
	}
	*/	
	
	private void showValues(float u, float v, Vector3D c) {
		System.out.println("Values u: "+u+ " v: "+v+"  "+c.information() );
	}



	private String inspectFile(NetcdfFile ncfile) {

		String information="Variables: "; 

		//All variables
		ArrayList<Variable> variables= (ArrayList<Variable>) ncfile.getVariables();
		
		// loop through all variables
		for (Variable variable : variables) {
			information+= variable.getDescription();

			System.out.println("\n Variable : "+variable.getNameAndDimensions()+
					" "+variable.getDescription());
			
		}

		return information;
	}



//
//	public CoordinateMesh getvMesh() {
//		return vMesh;
//	}
//
//
//
//
//	public void setvMesh(CoordinateMesh vMesh) {
//		this.vMesh = vMesh;
//	}
	
	
	public void count(int i) {
		count+=i;
		clock.stepDay(i);
	}



	public GeoGrid getGrid() {
		return grid;
	}




	public void setGrid(GeoGrid grid) {
		this.grid = grid;
	}



	public ColorGradient getGradient() {
		return gradient;
	}



	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}



	public int getCount() {
		return count;
	}



	public void setCount(int count) {
		this.count = count;
	}



	public float getScale() {
		return scale;
	}



	public void setScale(float scale) {
		this.scale = scale;
	}



	public boolean isPause() {
		return pause;
	}



	public void setPause(boolean pause) {
		this.pause = pause;
	}



	public int getStepInterval() {
		return stepInterval;
	}



	public void setStepInterval(int stepInterval) {
		this.stepInterval = stepInterval;
	}



	public float getLinew() {
		return linew;
	}



	public void setLinew(float linew) {
		this.linew = linew;
	}



	public boolean isUseAnimation() {
		return useAnimation;
	}



	public void setUseAnimation(boolean useAnimation) {
		this.useAnimation = useAnimation;
	}



	public float getVisualThreshold() {
		return visualThreshold;
	}



	public void setVisualThreshold(float visualThreshold) {
		this.visualThreshold = visualThreshold;
	}



	public Clock getClock() {
		return clock;
	}



	public void setClock(Clock clock) {
		this.clock = clock;
	}



	public Coordinate[] getCoordinate() {
		return coordinate;
	}



	public void setCoordinate(Coordinate[] coordinate) {
		this.coordinate = coordinate;
	}



	public float getpSize() {
		return pSize;
	}



	public void setpSize(float pSize) {
		this.pSize = pSize;
	}



	
}
