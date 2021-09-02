package Utility;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.amazonaws.util.IOUtils;

import Colors.ColorInt;
import distribution.Distribution;




/**
 * PolygonCoordinate Is a class that holds a list of geographical coordinates, a polygon. 
 * It also has a ID –number associated. 
 * The colour is used for colouring inside the polygon when drawn by other classes. 
 * It has a list of points, integer XY, generated from the coordinates used for graphics 
 * and interaction.
 * 
 * @author a1500
 *
 */
public class PolygonCoordinate {

	private Coordinate coordinates[] ;
	private int number=-1;
	private Color color= new Color(10,60,70,10);
	// The corresponding pixel points within a distribution image
	private Polygon points= new Polygon();

	
	
	/**
	 * Constructor
	 * @param textToScan text string to scan from
	 */
	public PolygonCoordinate(String textToScan){
		createCoordinates(textToScan);
		
	}


	/**
	 * Defines the polygon Color
	 */
	private void defineColor(){
		Color c= getColor();
		ColorInt colorI= new ColorInt(c);
		int n=(int) (Math.random()*80);
		colorI.red+=n+number;
		n=(int) (Math.random()*100);
		colorI.green+=n+number;
		n=(int) (Math.random()*120);
		colorI.blue+=n+number;
		colorI.alpha+= number;
		
		c= colorI.makeColorAlpha();
		setColor(c);
	}

	
	/**
	 * Constructor
	 * @param file text file to scan from
	 * @param number the polygon number
	 * 
	 */
	public PolygonCoordinate(File file,int number) {
		this.number=number;
		try(FileInputStream inputStream = new FileInputStream(file)) {     
			String textToScan = IOUtils.toString(inputStream);
			createCoordinates(textToScan);
			defineColor();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(" Cannot handle the file");
			e.printStackTrace();
		}
	}

	



	/**
	 * Constructor
	 * @param file text file to scan from
	 */
	public PolygonCoordinate(File file) {
		this(file,-1);
	}
	
	public static void main(String[] args) {
		
		String path="C:/Users/a1500/Documents/Eclipse Workspace/TestNetCDF/res/corners_neighbours_nordic.txt";//polygon file.txt";
		
		File file= new File(path);
		
		
		try(FileInputStream inputStream = new FileInputStream(path)) {     
//			String textToScan = IOUtils.toString(inputStream);
			for (int i = 0; i < 65; i++) {
				
			PolygonCoordinate polygon= new PolygonCoordinate(file,i);
//			polygon.createCoordinates(textToScan);
//			System.out.println(textToScan);
			System.out.println(polygon.getCoordinates().length);
			System.out.println("point : "+polygon.getPoints().npoints);
			
			for (Coordinate c : polygon.getCoordinates()) {
				System.out.println(i+" -> "+c.toString());
			}
			
			}

		} catch (FileNotFoundException e) {
//			e.printStackTrace();
		} catch (IOException e1) {
//			e1.printStackTrace();
			System.out.println("in exception");
		}
		

				
	}

	/**
	 * Creates the polygon of coordinates from a text string 
	 * @param textToScan text string to scan from
	 */
	private void createCoordinates(String textToScan) {
		ArrayList<Coordinate> co= scanLinesOfText(textToScan);
		coordinates= (Coordinate[]) co.toArray(new Coordinate[co.size()]);
	}
	
	
	/**
	 * Scans a text for coordinates, one coordinate at each line
	 *   
	 * @param text the text to scan
	 */
	private ArrayList<Coordinate> scanLinesOfText(String text){
		Scanner sc = new Scanner(text);
		//		Coordinate c=null;
		String line="";
		int n=0;
		ArrayList<Coordinate> co= new ArrayList<>();
		// Read of the header 1st if file
		if (number>-1) {
			line= sc.nextLine();
		}
		// 
		while(sc.hasNextLine()){
			line= sc.nextLine();

			//  Accommodate both types of files to parse
			Coordinate c=(number>-1) ? parseOneLine(line) :  parseCoordinate(line);
			if (c!=null) {
				co.add(c);
				n++;
			}
		}
		sc.close();

		return co;
	}
	
	

	
	
	
	
	/**
 * Reads a lines of text and finds a coordinate
 * @param line the text line to scan
 * @return a new coordinate
 */
private Coordinate parseCoordinate(String line) throws  NumberFormatException{

	Coordinate c= new Coordinate(0, 0);
	Scanner sc= new Scanner(line);
	String field="";
	
	while(sc.hasNext()){
		// latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
		// Longitude
		field= sc.next().replaceAll(",", ".");
		float  lon= Float.parseFloat(field);
		c.setLon(lon);
	}
	// Close the scanner
	sc.close();

	return c;
}



/**
 * Reads one line of text
 * @param line the text to parse
 * @return a new coordinate if it is the same number
 * @throws NumberFormatException
 */
private Coordinate parseOneLine(String line) throws  NumberFormatException{

	Coordinate c= null;
	Scanner sc= new Scanner(line);
	String field="";

	// The number
	field= sc.next();
//	System.out.println(field);
	int nr= Integer.parseInt(field);
	// Read the line if it is the same number
	if (number==nr) {
		c=new Coordinate(0, 0);

		// Skip the area
		field= sc.next(); 

		//  Longitude
		field= sc.next().replaceAll(",", ".");
		float  lon= Float.parseFloat(field);
		c.setLon(lon);
		// Then latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
	}
	// Close the scanner
	sc.close();

	return c;
}


private boolean isTheSameNumber(String line) {
	boolean theSame=false;
	Scanner sc= new Scanner(line);
	// The number
	String field= sc.next();
	int nr= Integer.parseInt(field);
	theSame=(number==nr);
	sc.close();
	
	return theSame;
}



/**
 * Creates an ArrayList of coordinates
 * @return a array list of the coordinates
 */
	public ArrayList<Coordinate> getArrayListOfCoordinates(){
	Coordinate[] coordinates= getCoordinates(); // Meaning polygon of coordinates
	// Create a new list from the coordinates
	ArrayList<Coordinate> caList= new ArrayList<Coordinate>( Arrays.asList(coordinates));
	return caList;
}

	
	
	/**
	 * Generates the corresponding points (corresponding to geographical coordinates) 
	 * within the distribution image based on the list of coordinates.
	 * @param distribution the VisualDistribution
	 */
//	public void generatePointPolygon(Distribution distribution){
//		
//		// Go through the list of coordinates and generate points
//		for (Coordinate coord : coordinates) {
//
//			//Label3
//			int ix= -1;//coord.findClosestIndex(distribution.getCoordinates(), 0.1f);
//			float threshold=0.1f;
//			//  Find index corresponding to the coordinate
//			// Do several attempts to find indexes, changing the threshold gradually
//			while (ix==-1 && threshold<0.5){
//				ix=  coord.findClosestIndex(distribution.getCoordinates(), threshold);
//				threshold+= 0.01;
//			}
////			System.out.println("threshold= "+threshold+ " ix:"+ ix);
//
//			// Find the XY in distribution space
//			Point p=distribution.XYFromIndex(ix);
//			// Find the correct pixel within the image, taking into account projections
//			Point pim = distribution.pixelFromXY(p.x, p.y, 1, false) ;
//
//			// The index found is in the distribution space, while the polygon is in image space
//			// •– We have found points within the distribution image at scale 1! •–
//			if (ix!=-1) {
//				points.addPoint(pim.x,pim.y);
//			}
////			System.out.println(coord.toString()+" becomes Polygon "+pim.x+" "+pim.y);
//		}
//	}
//	
	
	
	/**
	 * Calculates the average point of the polygon
	 * @return a point representing the middle of the polygon
	 */
//	public Point calculateAveragePoint(Distribution d,int scale){
//		Point  p= calculateAveragePoint();
//		p.x= p.x*scale +d.getPixelRegion().x;
//		p.y= p.y*scale +d.getPixelRegion().y;
//
//		return p;
//	}
//	
	
	/**
	 * Calculates the average point of the polygon
	 * @return a point representing the middle of the polygon
	 */
	public Point calculateAveragePoint(){
		Point  p= new Point();

		for (int i = 0; i < points.npoints; i++) {
			p.x+= points.xpoints[i];	
			p.y+= points.ypoints[i];	
		}

		p.x /= points.npoints ;
		p.y /= points.npoints ;

		return p;
	}
	

	/**
	 * Calculates the average coordinate based on this polygon.
	 * @return an average coordinate
	 */
	public Coordinate  calculateAverage(){
		Coordinate c= new Coordinate(0,0);
		float lat=0;
		float lon= 0 ;
		int n = 0 ;
		for (Coordinate coordinate : coordinates) {
			lat+=coordinate.lat;
			lon+=coordinate.lon;
			n++;
		}
		lat/=n;
		lon/=n;
		
		c.lat=lat;
		c.lon=lon;

		return c;
	}
	
	
	// ==================================================
	// Getters and setters	//
	// ==================================================

/**
 * @return the polygon
 */
public Coordinate[] getCoordinates() {
	return coordinates;
}

/**
 * @param polygon the polygon to set
 */
public void setCoordinates(Coordinate[] polygon) {
	this.coordinates = polygon;
}

/**
 * @return the number
 */
public int getNumber() {
	return number;
}

/**
 * @param number the number to set
 */
public void setNumber(int number) {
	this.number = number;
}



/**
 * @return the points
 */
public Polygon getPoints() {
	return points;
}



/**
 * @param points the points to set
 */
public void setPoints(Polygon points) {
	this.points = points;
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
	
	

}
