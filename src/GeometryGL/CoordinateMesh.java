package GeometryGL;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.glDisable;

import java.awt.Color;
import java.util.ArrayList;

import Utility.Coordinate;

public class CoordinateMesh {

	private int latInterval=5;
	private int lonInterval=20;
	private Color color= Color.black;
	private ArrayList<GeoLine> latList= new ArrayList<>();
	private ArrayList<GeoLine> lonList= new ArrayList<>();

	
	
	/**
	 * Constructor
	 * @param latInterval
	 * @param lonInterval
	 */
	public CoordinateMesh(int latInterval, int lonInterval,Coordinate from, Coordinate to){
		this.latInterval= latInterval;
		this.lonInterval= lonInterval;
		createMesh(from.getLat(),to.getLat(),from.getLon(), to.getLon(),this.latInterval);
	}

	
	/**
	 * Creates all lines along latitudes and longitudes 
	 */
	public void createMesh(float north, float south,float east,float west,float off){
		// Along latitudes, from North to South
		for (int lat = (int) (north-off); lat > south-off; lat-=latInterval) {
			Coordinate from= new Coordinate(lat, east);
			Coordinate to =  new Coordinate(lat, west);
			
			GeoLine g= new GeoLine(100, from,to,5);
			Color c = Color.white ;
			c = (lat==0||lat==60) ? Color.red : c ;
			c = (lat< -60) ? Color.black:  c ; 
			g.setColor(c);			
			g.setAlpha( (lat==0||lat==60) ? 0.5f:  0.3f);
			
			latList.add(g);			
		}
		
		// Along longitudes
		for (int lon = (int) east; lon < west; lon+=lonInterval) {
			Coordinate from= new Coordinate(north, lon);
			Coordinate to =  new Coordinate(south, lon);
			
			GeoLine g= new GeoLine(100, from,to,5);
			g.setColor( (lon==0) ? Color.red:  Color.white);
			g.setAlpha( (lon==0||lon==-180) ? 0.5f:  0.3f);
			
			lonList.add(g);			
		}
		System.out.println("Mesh created "+ latList.size()+" lon "+lonList.size());

	}
	
	public void draw(){
		
		glDisable(GL_LIGHTING);
		
		// latitudes
		for (GeoLine geoLine : latList) {
			geoLine.draw(color);			
		}
		// longitudes
		for (GeoLine geoLine : lonList) {
			geoLine.draw(color);			
		}
	}


	public Color getColor() {
		return color;
	}


	public void setColor(Color color) {
		this.color = color;
	}
}
