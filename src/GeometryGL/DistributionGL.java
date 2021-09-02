package GeometryGL;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glVertex3f;

import Colors.ColorInt;
import Utility.Clock;
import Utility.Coordinate;
import basicLwjgl.Vector3D;
import distribution.Distribution;

public class DistributionGL {
	
	Distribution distribution= null;
	GeoGrid geoGrid= null;
	private int number= 0;
	private boolean visible= true;
	private boolean pause= false;
	//  Centre of mass
	private Coordinate cm=null ;
	
	/**
	 * Constructor
	 * @param distribution distribution to use
	 */
	public DistributionGL(Distribution distribution, int number) {
		this.distribution=distribution;
		this.number = number;
		geoGrid=defineGeoGrid(distribution);
		calculateColorAndCenter();
	}
	
	public GeoGrid defineGeoGrid(Distribution dis) {
		int width = dis.getWidth();
		int height= dis.getHeight();
		float dh = 0.4f ;	// offset above surface
		String name = String.format("default%d.txt", number+2);
		GeoGrid g= new GeoGrid(dis.getCoordinates(), width, height, Globe.Radius+dh+number*dh, name );
		g.createVertexes();
		return g;
	}


	/**
	 * Renders a distribution as a GeoGrid
	 * @param useFill if geometry is to be drawn filled
	 * @param number number of distributions in the scene
	 */
	public void render(boolean useFill,int number,boolean selected){

		float x,y,z;
			float x1,y1,z1;
			Vector3D n= new Vector3D();
			Vector3D n1= new Vector3D();

			glDisable(GL_LIGHTING);
			glDisable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);     
			glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);

			glPolygonMode(GL_BACK,(useFill) ? GL_FILL:GL_LINE);
			glPolygonMode(GL_FRONT, (useFill) ? GL_FILL:GL_LINE);
			glDisable(GL_CULL_FACE);

			// 
			int t=distribution.getCount();//  count();
			int wh=distribution.getWH();
			float values[] = distribution.getValues();
			int height =geoGrid.getHeight();
			int width= geoGrid.getWidth();

			int sy = (number>1 && !selected) ? ( (number>3) ? 3 : 2) : 1 ;
			int sx=  (number>2 && !selected) ?  2: 1 ;
			
			//  – Grid loop – //  –	// –	// –	// –	// –	// 
			for(int iy = sy; iy < height; iy+=sy) {
				
				iy= (iy>height-1) ? height-1 : iy ;

				glBegin(GL_TRIANGLE_STRIP);

				for (int ix = 0; ix < width; ix+=sx) {
					ix= (ix>width-1) ? width-1 : ix ;
					
					float value=values[ix+iy*width+t*wh];
//					if (value==distribution.getFillV()) {
//						continue ;
//					}

					//  To make height as a function of value
//					float v= values[ix+(iy-1)*width+t*wh];
//					float radius= Globe.Radius;
//					geoGrid.vertex[ix][iy-1].scaleVector( (float) ((radius+v*0.0001)/radius) );

					x=geoGrid.getVertex()[ix][iy-sy].x;   
					y=geoGrid.getVertex()[ix][iy-sy].y;
					z=geoGrid.getVertex()[ix][iy-sy].z;
					x1=geoGrid.getVertex()[ix][iy].x;	// 
					y1=geoGrid.getVertex()[ix][iy].y;
					z1=geoGrid.getVertex()[ix][iy].z;
					// Normal vectors
					n.setTo(x, y, z);
					n.norm();
					n1.setTo(x1, y1, z1);
					n1.norm();

					// 	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	

					
					ColorInt color[]  = geoGrid.generateColour(values[ix+(iy-sy)*width+t*wh],value,distribution.getFillV()) ;
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
				}
				glEnd();
			}
		
	}

	/**
	 * Create a list of colours for 
	 */
	public void calculateColorAndCenter() {
		if (geoGrid.getColors()==null) {
			geoGrid.createColours();
		}
		if (geoGrid.getCenter()==null) {
			geoGrid.calculateCenter();			
		}
	}

	/**
	 * @return the geoGrid
	 */
	public GeoGrid getGeoGrid() {
		return geoGrid;
	}

	/**
	 * @param geoGrid the geoGrid to set
	 */
	public void setGeoGrid(GeoGrid geoGrid) {
		this.geoGrid = geoGrid;
	}

	/**
	 * @return the distribution
	 */
	public Distribution getDistribution() {
		return distribution;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}

	public String info() {
		
		int t  = distribution.getCount() ;
		boolean isTime=(distribution.getDates()!=null);  
		int hours = (isTime) ?  distribution.getDates()[t]: 0;
		Clock clock= new Clock(hours,true);
		String  daymonth = clock.dayMonthYearString();
//		String 	date=	distribution.getDateStrings()[t] ;
		String ts=String.format("%d", t);
		String s= distribution.getFullName()+" "+ ( (isTime) ? daymonth : ts); 
		return s;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the pause
	 */
	public boolean isPause() {
		return pause;
	}

	/**
	 * @param pause the pause to set
	 */
	public void setPause(boolean pause) {
		this.pause = pause;
	}


	public Coordinate getCm() {
		return cm;
	}

	public void setCm(Coordinate cm) {
		this.cm = cm;
	}

}

