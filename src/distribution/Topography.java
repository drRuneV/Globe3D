package distribution;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Utility.Coordinate;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class Topography extends GridDistribution{
	

	// If we have a regular grid we do not need the coordinates as a two-dimensional matrix,
	// but rather as 2 separate one-dimensional arrays for latitude and longitude.
	boolean isRegular=true;
	public static  float ETPO1 = 1.0f/60.0f;// 0.01666666666666667f;
	public static  float ETPO2 = 2.0f/60.0f;// 0.033333f;
	

	float latitude[];
	float longitude[];
	protected float res= 0.1f;
	
	/**
	 * @param ncfile 
	 * 
	 */
	public Topography(NetcdfFile ncfile) {
		super(ncfile);
	}



	/**
	 * Constructor
	 * Constructs a new topography based on a filename and variable name 
	 * @param filename The filename of the netCDF files
	 * @param vname The variable name of the topography data within the netCDF file
	 */
public Topography(String filename, String vname, Coordinate cFrom, Coordinate cTo,int etopo) {
		super(filename, vname);
		// Making data based on the Etopo2 data
//		Coordinate coo[]  = {new Coordinate(88, -32), new Coordinate(40, 110)};
		Coordinate coo[]  = {cFrom,cTo};
		// 
		res= (etopo==2) ? ETPO2: ETPO1 ;
		setResLat(res);
		setResLon(res);

		
		System.out.println("resolution is now:"+res);
		makeEtopo2DataTopography(vname, coo);
	}
	
	// «•»  «•»  «•» This is hardcoded to a specific area and should be changed
	// This method should really be in topography where the boundary of a distribution
	// Or the distribution object itself is delivered in order to find the right coordinates.


	@Override
	public void copyAllFrom(GridDistribution gd) {
		this.copyFrom(gd);
	}

	/**
	 * Copies data from another topography
	 * @param topo The topography to copy from
	 */
	public void copyFromAnother(Topography topo){
		copyAllFrom(topo);
		this.setLatitude(topo.getLatitude());
		this.setLongitude(topo.getLongitude());
		this.setRegular(topo.isRegular);
		this.setRes(topo.getRes());
	}
	//Label3

	
	/**
	 * Generates the Etopo2 topography data from the given netCDF for variable name and coordinates range
	 * @param vname The netCDF variable name where the topography data are
	 * @param coo The coordinate range
	 */
	private void makeEtopo2DataTopography(String vname, Coordinate coo[]){

		//the resolution of the file is 2 minutes which corresponds to 2°/60
		//2/60= 0,03333333333333 
		//		res= ETPO2;// 2.0f/60.0f;

		// 
		//The grid distribution has checked if this file is okay
		if (ncfile!=null) {

			//			String name="btdata"; //<•><•><•><•>  

			boolean wasFound= lookForVariable(ncfile, vname);
			if ( wasFound ){
				System.out.println("The Etopo2 "+vname+" was found ");
				//We have to define the topography
				defineRegular(coo, new float[]{res,res});
				// Create the values from the variable
				Variable variable = ncfile.findVariable(vname);		
				createData(variable);

				//				displaySomeValues();
			}
		}
	}



	/**
	 * 
	 */
	public void displaySomeValues() {
		int i=0;
		
//		for (float f : latitude) {
//			System.out.println(f+" °N");
//		}
		System.out.println("latitudes #  "+latitude.length);
		System.out.println("longitudes#  "+longitude.length);
		System.out.println("values#  "+values.length);

		Coordinate c= new Coordinate(60,-2);
		c.addLonLat(0, -0.05f);
		float depth=getValueAtCoordinate(c);
		System.out.println("Depth at the c: "+depth+" m "+  c.toString() );
		depth=getValueAtCoordinate(lower);
		System.out.println("Depth at the lower: "+depth+" m "+  lower.toString() );

		for (float v : getValues()) {
			if (i%1000000==0) {
				System.out.println("# "+i+" "+v+" m");
			}
			i++;
		}
	}
	
	/**
	 * Defines a regular grid for the topography
	 * @param coo - The coordinates of the upper and lower corner
	 * @param res
	 */
	public void defineRegular(Coordinate coo[],float res[]){
		// Set coordinates and resolution
		upper= coo[0];
		lower= coo[1];
		setResLat(res[0]);
		setResLon(res[1]);
		
		// Establish the actual positions in the values
		// This assumes the latitude starts at 90° and longitude -180° !
		int y0 = (int) ((90.0-upper.getLat())/getResLat());
		int y1=(int) ((90.0-lower.getLat())/getResLat());
		int x0 =(int) ((upper.getLon()+180.0)/getResLon());
		int x1= (int) ((lower.getLon()+180.0)/getResLon());


		// Define all relevant dimensions
		width = x1- x0;
		height= y1-y0;
		setTime(1);
		
		// Create latitude and longitude
		latitude= new float[height];
		longitude= new float[width];
		// Generate coordinates
		// «•» This was detected to be wrong at March 2020 !
		// «•» latitude must decrease from the upper
		// «•» apparently these variables are never used
		for (int i = 0; i < width; i++) {
			longitude[i]= upper.getLon()+getResLon()*i ;
		}
		for (int j = 0; j < height; j++) {
			latitude[j]  = upper.getLat()-getResLat()*j;	// minus here !
		}
		
		//
		if (true) {
			Coordinate[] coordinates = new Coordinate[width*height]; //width*height
			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					int ix = i+j*width;
					coordinates[ix]= new Coordinate( latitude[j],longitude[i]);
				}
			}
		}
	}
	
	
	// ==================================================
	// Implemented method
	// ==================================================
	
//	@Override
//	public boolean lookForVariable(NetcdfFile ncfile, String name) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	
	/**
	 * Get a value at a given coordinate
	 * @param c - The coordinate where we want  the value
	 * @return The value at the given coordinate
	 */
	public float getValueAtCoordinate(Coordinate c){
		float v=0;
		// Index is relative to the upper and leftmost coordinate
		int y = (int) ((upper.getLat()-c.getLat())/getResLat());
		int x=  (int) ((c.getLon()- upper.getLon())/getResLon());
		int index=x+y*getWidth();
		// we have to check x,y separately! -> && x<width && y<height) 
		if (index>=0 && index<(width*height-1) && x<width && y<height) {
			v = values[index];
		}
		else{
//			System.out.println("x:"+x+" widthmax:"+width); 
//			System.out.println(x+":"+y+" index= " + index+" at:"+c.toString()); 
		}
		
		return v;
	}
	
	/**
	 * Creates the data values of this topography extracted from the given netCDF variable
	 * Values are created on the basis of the upper and lower coordinates as well as the resolution given.
	 * @param variable - The netCDF variable where the data is.
	 * @return True if data are created successfully. 
	 */
	public boolean createData(Variable variable){
		boolean isOkay=true;
		// Establish the actual positions in the values
		// This assumes the latitude starts at 90° and longitude -180° !
		int fromy = (int) ((90.0-upper.getLat())/getResLat());
		int toy=(int) ((90.0-lower.getLat())/getResLat());
		int fromx =(int) ((upper.getLon()+180.0)/getResLon()); //i.e.: -170 -> from=10/res 
		int tox= (int) ((lower.getLon()+180.0)/getResLon());

		int[] origin =  new int[] {fromy,fromx};
		int[] size =	new int[] {toy-fromy,tox-fromx};
		
		System.out.println("res"+getResLon());
		System.out.println("size:"+size[0]+":"+size[1]);

		// Create the data 
		// <•><•><•><•>  //Label1
		Array data;
		try {
			// Get the portion of data requested
			data = variable.read(origin ,size);

			// Create the float array where we put the data
			// The Array is two-dimensional with latitudes along the x-axis ?? !
			int length = (int) data.getSize();
			values =   new float[length];
			for (int i = 0; i < length; i++) {
				values[i]=  data.getFloat(i);
			}

		}
		catch (InvalidRangeException | IOException e) {
			// TODO Auto-generated catch block
			isOkay=false;
			e.printStackTrace();
		}
		
		return isOkay;
	}
	
	/*
	public boolean createValues(Variable variable, int fromx, int fromy,int tox, int toy){
		boolean isOkay=true;
		isOkay= createData(variable);
		return isOkay;
	}

	
	public boolean createValues(Variable variable, Coordinate[] coo) {
		// Establish the actual positions in the values
		int lat0 = (int) ((90.0-coo[0].getLat())/0.034f);
		int lat1=(int) ((90.0-coo[1].getLat())/0.034f);
		int lon0 =(int) ((coo[0].getLon()+180.0)/0.034f);
		int lon1= (int) ((coo[1].getLon()+180.0)/0.034f);
		return createValues(variable, lon0, lat0, lon1, lat1);
	}
	*/
	
	public static void main(String[] args) {
		Coordinate c1= new Coordinate(90, -20);
		Coordinate c2= new Coordinate(0, 20);//180);

		String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		String topoFile= "etopo2.nc";	// btdata
		
//		String testName= "etopo1_bedrock 90 30 -30 30 .nc";	// Band1
//		Topography topography= new Topography(path+testName,"Band1", c1,c2,1);
		
		Topography topography= new Topography(path+topoFile,"btdata", c1,c2,2);
//		topography.showTopoOnPanel();
	}
	

	

	/**
	 * Draws the values in a BufferedImage
	 * @param useRelief
	 * @param l
	 * @param gradient 
	 * @param image 
	 */
	//This method should be moved to another class:VisualTopo
	/*
	public void drawValues(float l, ColorGradient gradient, BufferedImage image) {
		int x;		//Label1
		int y;
		int width=  getWidth();
		int height= getHeight();

		// Please set the values here
		for (int i = 0; i < getValues().length-1; i++) {
			y= i/width;
			x= i- y*width;

			// Must create a completely new colour each time
			ColorInt color= new ColorInt( gradient.retrieveColorInt(getValues()[i]));

			if (l!=0) {
				color.darker(darken(l, i));
			}
			if (x< width && y<height) {
				image.setRGB(x, y ,  color.toInt()); //image.getHeight()-y-1,  color);
			}
		}
	}
	*/
	

	/**
	 * Is this a regular grid
	 * @return true if regular
	 */
	public boolean isRegular() {
		return isRegular;
	}


	/**
	 * 
	 * @param isRegular
	 */
	public void setRegular(boolean isRegular) {
		this.isRegular = isRegular;
	}



	/**
	 * @return the latitude
	 */
	public float[] getLatitude() {
		return latitude;
	}



	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(float[] latitude) {
		this.latitude = latitude;
	}



	/**
	 * @return the longitude
	 */
	public float[] getLongitude() {
		return longitude;
	}



	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(float[] longitude) {
		this.longitude = longitude;
	}



	/**
	 * @return the res
	 */
	public float getRes() {
		return res;
	}



	/**
	 * @param res the res to set
	 */
	public void setRes(float res) {
		this.res = res;
	}


	}