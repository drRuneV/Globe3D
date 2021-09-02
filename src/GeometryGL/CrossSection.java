package GeometryGL;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.util.ArrayList;

import Basic.Vector2D;
import Colors.ColorGradient;
import Colors.ColorInt;
import GeometryGL.Gui.CrossSGraph;
import Utility.Coordinate;
import Utility.LineGraph;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.Vector3D;
import distribution.Distribution;


public class CrossSection {

	private GeoLine geoLine= null;
	private Coordinate from= null;
	private Coordinate to= null;
	private int number = 100;
	private Coordinate coordinates[]=null ;
	private int[] indexs= null;
	private float[] values=null;
	private CrossSGraph graph = null ;
	LineGraph lineGraph=null;
	private float distance= 0;
	
	
	
	/**
	 * Constructor
	 * @param from start coordinate
	 * @param to end coordinate
	 */
	public CrossSection(Coordinate from, Coordinate to,int number) {
		this.from = from ;
		this.to = to ;
		this.number = number ;
		// 
		define();
		
	}


	/**
	 * @param from
	 * @param to
	 * @param number
	 */
	public void define(){
		geoLine= new GeoLine(20, from, to,1);
		coordinates =  Coordinate.interpolateBetween(from, to, number);
		indexs=null;
		distance = geoLine.getDistance() ; // this is in kilometre
		
		graph= new  CrossSGraph(300, 00, 200, 120, number) ;
	}
	

	/**
	 * Draws the cross-section
	 * @param d the distribution used for drawing
	 */
	public void draw(ArrayList<DistributionGL> disList, DistributionGL disGL,boolean useDistribution ){

		Distribution d = disGL.getDistribution() ;
		ColorGradient cg = disGL.getGeoGrid().getGradient();
		// Create indexes for faster performance
		if (indexs==null) {
			createIndexes(d.getCoordinates());
		}
		// Measure values along the line for this time step
		values= measureValues(d);
		// 
		geoLine.draw(Color.red);
		//
		if (useDistribution) {

			drawAlong(cg, values);
			//
			graph.update();
			graph.draw();
			graph.draw(cg, values);

			// JFrame adding new distributions
			if (lineGraph!=null) {
				if (disList.size()> lineGraph.getSeries().getSeriesCount()) {
					DistributionGL last = disList.get(disList.size()-1) ;
					String title =  last.getDistribution().getFullName();
					float v[] = measureValues(last.getDistribution()) ;
					lineGraph.addSerie(v, title);
				}
			}
			// JFrame update
			for (int i = 0; i < disList.size(); i++) {
				Distribution dis = disList.get(i).getDistribution();
//				updateLineGraph(dis,i);
			}
		}
	}


	/**
	 * Updates the line graph drawn on a JFrame 
	 * @param d distribution
	 */
	public void updateLineGraph(Distribution d,int number) {
		float value[]= measureValues(d) ;
		// JFrame drawing
		if (lineGraph==null) {
			String unit = d.getUnit();
			String title = d.getFullName();
			String name = d.getName() ;
			lineGraph= new LineGraph(values, title, name,"Coordinate", unit);
		}
		else {
			lineGraph.update(value, d.getDateStrings()[d.getCount()], number);
		}
	}
	
	
	
	/*
	public void drawGraph2D(ColorGradient g,float[] values ){

		CoreOpenGL.project2D();
		//
		int height=120;
		Vector2D p= new Vector2D(180, 250); 
		ColorInt ci= new ColorInt();
		//  
		glPushMatrix();
		glTranslatef(p.x, p.y, 0); 

		glLineWidth(2.0f);
		glColor4f(0.1f, 0.1f , 0.1f , 0.80f);
		glBegin(GL_LINES);
		{
			glVertex2f(0,0 );
			glVertex2f(0,-height );
			glVertex2f(0,0 );
			glVertex2f(number+10,0);
		}
		glEnd();
		
		
		//  Plot
		glLineWidth(1.0f);
		glBegin(GL_LINES);
		for (int i = 0; i < values.length; i++) {
			
			ci=  g.retrieveColor(values[i]);
			float [] c= ci.convertToFloat();
			float h= Math.min(height, Math.max(0,values[i]*2) );

			glColor4f(c[0],c[1],c[2],c[3]);
			glVertex2f(i, -1);
			glVertex2f(i, -h);
		}
		glEnd();
		
		glColor4f(0.2f, 0.2f , 0.2f , 0.50f);
		glBegin(GL_POLYGON);
		{
			glVertex2f(0,0 );
			glVertex2f(0,-height );
			glVertex2f(number+10,-height);
			glVertex2f(number+10,0);
		}
		glEnd();
		
		glPopMatrix();
	}
	*/


	/**
	 * 
	 * @param cg
	 * @param values
	 */
	public void drawAlong(ColorGradient cg, float[] values) {
		glLineWidth(2.0f);
		Vector3D v2= new Vector3D();
		
		glBegin(GL_TRIANGLE_STRIP);
		
		for (int i = 0; i < values.length; i++) {

			ColorInt ci= new ColorInt(cg.retrieveColor(values[i])); // if not new gradient will be changed!
			ci.darker(0.8f);
			float [] c= ci.convertToFloat();
			//			System.out.println(c.toString()+" v:"+values[i]);
			Vector3D v = CoreOpenGL.coordinateToPoint(coordinates[i] );
			v2.equals(v);
			float h= Globe.Radius+ geoLine.getHeight()+0.1f+ values[i]*0.5f;
			v.scaleVector(h);
			v2.scaleVector(h-values[i]*0.5f);
			
			glColor4f(c[0],c[1],c[2],c[3]);
			glVertex3f(v.x,v.y ,v.z );
			glVertex3f(v2.x,v2.y ,v2.z );
		}
		
		
		glEnd();
	}
	

	/**
	 * Measures values from a distribution along the cross-section
	 * @param d the distribution to measure from
	 * @return a list of floatingpoint values
	 */
	public float[] measureValues(Distribution d) {
		
		float values[] = new float[number] ;
		int ix=-1;
		// 
		for (int i = 0; i < values.length; i++) {
//			c = coordinates[i] ;
//			ix= c.findClosestIndex(d.getCoordinates(), 0.9f);
			ix = indexs[i] ;
			// Inside
			if (ix!=-1) {
				ix+= d.getCount()*d.getWH();
				values[i] = d.getValues()[ix] ;
			}
			else {
				values[i]=0;
			}
		}
		return values;
	}
	
	private void createIndexes(Coordinate[] coordinateGrid){
		indexs= new int[number] ;

		for (int i = 0; i < number; i++) {
			indexs[i]= coordinates[i].findClosestIndex(coordinateGrid, 0.9f);
		}
	}


	/**
	 * @return the geoLine
	 */
	public GeoLine getGeoLine() {
		return geoLine;
	}


	/**
	 * @param geoLine the geoLine to set
	 */
	public void setGeoLine(GeoLine geoLine) {
		this.geoLine = geoLine;
	}


	/**
	 * @return the coordinates
	 */
	public Coordinate[] getCoordinates() {
		return coordinates;
	}


	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(Coordinate[] coordinates) {
		this.coordinates = coordinates;
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
	 * @return the to
	 */
	public Coordinate getTo() {
		return to;
	}


	/**
	 * @param to the to to set
	 */
	public void setTo(Coordinate to) {
		this.to = to;
	}


	/**
	 * @return the graph
	 */
	public CrossSGraph getGraph() {
		return graph;
	}


	/**
	 * @param graph the graph to set
	 */
	public void setGraph(CrossSGraph graph) {
		this.graph = graph;
	}


	/**
	 * @return the values
	 */
	public float[] getValues() {
		return values;
	}


	/**
	 * @param values the values to set
	 */
	public void setValues(float[] values) {
		this.values = values;
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


}
