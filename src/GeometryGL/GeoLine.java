package GeometryGL;

import java.awt.Color;

import Colors.ColorInt;
import Utility.Coordinate;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.Vector3D;

import static org.lwjgl.opengl.GL11.*;


/**
 * 
 * @author Admin
 *
 */
public class GeoLine {


	private Color color = Color.white;
//	private Color red = Color.red;
	private float lineWith= 0.5f;
	private float alpha= 0.8f;  
	
	//  segments #
	private int n= 18;
	private Coordinate from= null;
	private Coordinate to= null;
	private float height = 1f ;
	// List of the n points
	private Vector3D[] list= null;

	private float distance=0; // distance between coordinates in kilometres
	
	/**
	 * Constructor
	 * @param n Number of segments in the line
	 * @param from Starting coordinate
	 * @param to Ending coordinate
	 * @param height height above surface
	 */
	public GeoLine(int n,Coordinate from, Coordinate to,float height) {
		this.n=n;
		this.from= from;
		this.to= to;
		this.height = height ;
		list= new Vector3D[n];
		create();
		calculateDistance();
	}
	
	/**
	 * Creates the list of points used for the geographical line
	 */
	public void create(){

		Coordinate c[]= Coordinate.interpolateBetween(from, to, n);

		for (int i = 0; i < n; i++) {
			Vector3D p= CoreOpenGL.latLonToPoint(c[i].getLat(), c[i].getLon()) ;
			p.scaleVector(Globe.Radius+ height);
			list[i]= new Vector3D(p);
		}

	}
	
	/**
	 * Calculate the distance of the Geo line
	 */
	public void calculateDistance(){
		Vector3D pf= CoreOpenGL.latLonToPoint(from.getLat(), from.getLon());
		Vector3D pt= CoreOpenGL.latLonToPoint(to.getLat(), to.getLon());
		float angle = pf.getAngleBetween(pt);
		distance = (float) (angle*Coordinate.TORAD) *Globe.EARTHR;  
	}
	
	
	/**
	 * Calculate the distance between 2 coordinates in kilometres
	 * Using straightforward formula: ARC=angle*Radius
	 * @param c1 coordinate from
	 * @param c2 according to
	 * @return the distance between
	 */
	public static float calculateDistance(Coordinate c1, Coordinate c2){
		Vector3D pf= CoreOpenGL.latLonToPoint(c1.getLat(), c1.getLon());
		Vector3D pt= CoreOpenGL.latLonToPoint(c2.getLat(), c2.getLon());
		float angle = pf.getAngleBetween(pt);
		float d= (float) (angle*Coordinate.TORAD) *Globe.EARTHR;
		return d;
	}
	
	
	
	public void draw(Color col){
		
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);     
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		glDisable(GL_COLOR_MATERIAL);

		color= (col==null) ? color: col ;
		ColorInt c= new ColorInt(color);
		glLineWidth(lineWith);
		glBegin(GL_LINE_STRIP);
			glColor4f(c.red, c.green, c.blue, alpha);
			for (Vector3D v : list) {
				glVertex3f(v.x,v.y ,v.z );
			}		
			
		glEnd();
		
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the alpha
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * @return the from
	 */
	public Coordinate getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(Coordinate from) {
		this.from = from;
	}

	/**
	 * @return the list
	 */
	public Vector3D[] getList() {
		return list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(Vector3D[] list) {
		this.list = list;
	}

	/**
	 * @return the lineWith
	 */
	public float getLineWith() {
		return lineWith;
	}

	/**
	 * @param lineWith the lineWith to set
	 */
	public void setLineWith(float lineWith) {
		this.lineWith = lineWith;
	}

	/**
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}
	
	
	
	


}
