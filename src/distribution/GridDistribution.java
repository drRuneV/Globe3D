/**
 * 
 */
package distribution;

import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import Utility.Coordinate;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.ArrayDouble.D3.IF;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 */
public abstract class GridDistribution {

	// This class should have a netCDF inspector as an separate object
	// NetCDFInspector netCDFInspector;
	
	
		// Width height and time is the dimensions of a grid distribution
		protected int width;
		protected int height;
		protected int time;
		// The values or the data
		protected float values[];
		// The dates as hours since 1950
		protected int dates[];
		// Corresponding string representation of the dates
		protected String[] dateStrings;
		
		protected float maxMin[];
		protected Coordinate upper;
		protected Utility.Coordinate lower;
		
		// Coordinates will be one set with the size with×height
		private Coordinate coordinates[];
		// Area
		protected float[] area =null;
		// Volume
		protected float[] waterVolume =null;
		
		// Name
		protected String name="N";
		// Resolution in longitude and latitude
		// «•» … Please move this into topography as a special case with fixed resolution
		private float resLon; 
		private float resLat;
		
		// This is a handle into a netCDF file
		protected NetcdfFile ncfile = null;
		protected String filePath="";
		
		protected String unit;
		//
		protected String message="";
		
		

		// ==================================================
		
		
		
		/**
		 * 
		 */
		public GridDistribution(NetcdfFile  ncfile) {
			this.ncfile = ncfile;
		}



		/**
		 * Constructor
		 * Creates a new grid distribution
		 * The netCDF file is checked based on the filename
		 * @param filename The fileName of the netCDF to check for
		 * @param vname 
		 */
		public GridDistribution(String filename, String vname) {
			 checkNetCDF(filename);
		}

		/**
		 * Copies all variables from another grid distribution
		 * @param gd The grid distribution to copy from
		 */
		public void copyFrom(GridDistribution gd){
			this.setFilePath(gd.getFilePath());
			this.setCoordinates(gd.getCoordinates());
			this.setArea(gd.getArea());
			this.setLower(gd.getLower());
			this.setMaxMin(gd.getMaxMin());
			this.setMessage(gd.getMessage());
			this.setName(gd.getName());
			this.setNcfile(gd.getNcfile());
			this.setResLat(gd.getResLat());
			this.setResLon(gd.getResLon());
			this.setTime(gd.getTime());
			this.setUnit(gd.getUnit());
			this.setUpper(gd.getUpper());
			this.setValues(gd.getValues());
			this.setHeight(gd.getHeight());
			this.setWidth(gd.getWidth());
			this.setDates(gd.getDates());
		}
		
		
		abstract public void copyAllFrom(GridDistribution gd);
		
		
		/**
		 * 
		 * @param fileName
		 */
		public void checkNetCDF(String fileName){

			try {
				ncfile = NetcdfFile.open(fileName);
				//				 The netCDF is okay
				if (ncfile!=null) { 
					filePath= fileName;
				}
			} 
			catch (IOException e) {
//				e.printStackTrace();
				message= "The netCDF file: \n"+fileName +"\n  is not accessible or does not exist";
				System.out.println(message);
//				 JOptionPane.showMessageDialog(null, message);
			}
		}


		// «•» use the statistics class instead moved in to this class
		public float[] findMaxMin(){
			maxMin= new float[]{-1000.0f,1000000.0f};

			for (int i = 0; i < values.length; i++) {
				maxMin[0]= (values[i]> maxMin[0])? values[i] : maxMin[0];
				maxMin[1]= (values[i]< maxMin[1])? values[i] : maxMin[1];
			}

			return maxMin;			
		}
//

		// ==================================================
		// Implemented methods
		// ==================================================


		/* (non-Javadoc)
		 * @see distribution.DataField#analyseNetCDF(ucar.nc2.NetcdfFile)
		 */
		//«•»
		//Each derived class should implement this  in order to analyse the file for relevant data
		
		
		




		// We cannot close the netCDF files because it is shared among different distributions!
//		/* (non-Javadoc)
//		 * @see java.lang.Object#finalize()
//		 */
//		@Override
//		protected void finalize() throws Throwable {
//			// TODO Auto-generated method stub
//			super.finalize();
//			if (ncfile != null ) try {
//				ncfile.close();
//				System.out.println("\n netCDF file is closing "); 
//			} catch (IOException ioe) {
//				System.out.println("trying to close " + filePath + ioe.getMessage());
//			}
//		
//		}
//

		/* (non-Javadoc)
		 * @see distribution.DataField#lookForVariable(ucar.nc2.NetcdfFile, java.lang.String)
		 */
		public boolean lookForVariable(NetcdfFile ncfile, String name) {
			boolean isPresent;
			Variable variable = ncfile.findVariable(name);
			isPresent= (variable!=null);

			if(!isPresent){
				System.out.println("we are NOT able to find the variable  "+ name); 
			}
			else {  System.out.println("The variable "+name+" was found ");
			}

			return isPresent;
		}







		/**
		 * 
		 */ //not used
		private void generate(){
			values = new float[width*height*time];
			coordinates = new Coordinate[width*height];
			dates = new int[time] ;
			
			// Generate coordinates
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
				coordinates[i+j*width] = new Coordinate(upper);
				coordinates[i+j*width].addLonLat(resLon*i, resLat*j);
				}
			}
			//Alternatively: values = new float[width][height];
			
		}

		/**
		 * Generate the dates from the netCDF variable T "Time"
		 * @param nc the netCDF file
		 * @param dateVar The date/time variable
		 * @return An array of integers as time is represented in the netCDF file
		 */
		public void extractDates(NetcdfFile nc, Variable dateVar) {
			Array dataDate;

			// Special check for time format which can be seconds since 1948: January 9
			boolean isSeconds1948 = determineDateDefinition1948(dateVar);
			int hoursBetween1948And1950= Utility.Clock.daysBetween19481950*24;

			try {
				dataDate= dateVar.read();
				int length = (int) dataDate.getSize();
				// Special consideration for physical data with different to date definition
				time= length;
				dates= new int[length];
				dateStrings= new String[length];
				// For each integer "hour since 1950", define a date
				for (int i = 0; i < length; i++) {
					int hour1950 = dataDate.getInt(i);
					// Convert seconds since 1948 to hours since 1950
					if (isSeconds1948) {
						int hour1948 = hour1950/(3600);
//						System.out.println(i+": second"+hour1950 +"  ->hour: " +hour1948 );
						hour1950 = hour1948- hoursBetween1948And1950 ;
					}
					dates[i]= hour1950;
					dateStrings[i]= Utility.Clock.createADate(hour1950);
				}
				System.out.println(length+" dates generated for "+name);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}



		/**
		 * Determine if the data definition is the seconds since 1948 or not.
		 * @param dateVar the netCDF a date variable
		 * @return true if this method believes dates are defined as seconds since 1948
		 */
		private boolean determineDateDefinition1948(Variable dateVar) {
			boolean is1948=false;			// :units = "seconds since 1948-01-01 00:00:00";
			List<Attribute> at= dateVar.getAttributes();
			for (Attribute attribute : at) {
				String na= attribute.getFullName().toLowerCase();
				String unitName="";

				if(na.contains("unit")){
					unitName= attribute.getStringValue();
					is1948 = (unitName.contains("seconds since 1948")) ;
				}
			}
			return is1948;
		}



		public Coordinate[] extractNetCDFCoordinates(NetcdfFile nc, Variable lat, Variable lon) {

			float scalef = findScaleFactor(lat);

			//NetCDF array
			Array datalat;
			Array datalon;
			try {
				// Get all the data of latitude and longitude
				datalat = lat.read();
				datalon=  lon.read();
				//Latitude and longitude's have the same length =width*height
				int length = (int) datalon.getSize();
				coordinates = new Coordinate[length]; //width*height
				for (int i = 0; i < length; i++) {
					coordinates[i]= new Coordinate(datalat.getFloat(i)*scalef,datalon.getFloat(i)*scalef);
				}
				// July 11, 2021
				upper= new Coordinate(coordinates[0]) ;
				lower= new Coordinate(coordinates[length-1]) ;
				
			}

			catch (IOException e) {
				e.printStackTrace();
			}
			return coordinates;
		}


		/** Find the scale factor from the attribute in the variable
		 * @param var
		 * @param scalef
		 * @return
		 */
		protected float findScaleFactor(Variable var) {
			float scalef=1;
			
			// 
			if (var.getAttributes()!=null) {
				
			for (int i = 0; i < var.getAttributes().size(); i++) {
				if (var.getAttributes().get(i).getFullName().contains("scale")) {
					scalef=  var.getAttributes().get(i).getNumericValue().floatValue();
					System.out.println("Scale factor "+var.getFullName()+ "= "+scalef);
				}
				}
			}
			return scalef;
		}

			
			// ==================================================
			// Getters and setters
			// ==================================================

		public int getWidth() {
			return width;
		}


		public void setWidth(int width) {
			this.width = width;
		}


		public int getHeight() {
			return height;
		}


		public void setHeight(int height) {
			this.height = height;
		}
		
		/**
		 * Calculates the width × height
		 * @return the width × height
		 */
		public int getWH(){
			return width*height;
		}


		public int getTime() {
			return time;
		}


		public void setTime(int time) {
			this.time = time;
		}


		public float[] getValues() {
			return values;
		}


		public void setValues(float[] values) {
			this.values = values;
		}

		
		
		/**
		 * @return the dates
		 */
		public int[] getDates() {
			return dates;
		}


		/**
		 * @param dates the dates to set
		 */
		public void setDates(int[] dates) {
			this.dates = dates;
		}
		


		/**
		 * @return the dateStrings
		 */
		public String[] getDateStrings() {
			return dateStrings;
		}


		/**
		 * @param dateStrings the dateStrings to set
		 */
		public void setDateStrings(String[] dateStrings) {
			this.dateStrings = dateStrings;
		}


		public Coordinate[] getCoordinates() {
			return coordinates;
		}


		public void setCoordinates(Coordinate[] coordinates) {
			this.coordinates = coordinates;
		}


		public Coordinate getUpper() {
			return upper;
		}


		public void setUpper(Coordinate upper) {
			this.upper = upper;
		}


		public Coordinate getLower() {
			return lower;
		}


		public void setLower(Coordinate lower) {
			this.lower = lower;
		}


		public float getResLon() {
			return resLon;
		}


		public void setResLon(float resLon) {
			this.resLon = resLon;
		}


		public float getResLat() {
			return resLat;
		}


		public void setResLat(float resLat) {
			this.resLat = resLat;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}


		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}




		public NetcdfFile getNcfile() {
			return ncfile;
		}


		public void setNcfile(NetcdfFile ncfile) {
			this.ncfile = ncfile;
		}
		


		public float[] getMaxMin() {
			return maxMin;
		}


		public void setMaxMin(float[] maxMin) {
			this.maxMin = maxMin;
		}


		/**
		 * @return the unit
		 */
		public String getUnit() {
			return unit;
		}


		/**
		 * @param unit the unit to set
		 */
		public void setUnit(String unit) {
			this.unit = unit;
		}


		/**
		 * @return the filePath
		 */
		public String getFilePath() {
			return filePath;
		}


		/**
		 * @param filePath the filePath to set
		 */
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}


		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}


		/**
		 * @param message the message to set
		 */
		public void setMessage(String message) {
			this.message = message;
		}


		/**
		 * @return the area
		 */
		public float[] getArea() {
			return area;
		}


		/**
		 * @param area the area to set
		 */
		public void setArea(float[] area) {
			this.area = area;
		}


		/**
		 * @return the waterVolume
		 */
		public float[] getWaterVolume() {
			return waterVolume;
		}


		/**
		 * @param waterVolume the waterVolume to set
		 */
		public void setWaterVolume(float[] waterVolume) {
			this.waterVolume = waterVolume;
		}

		
		
}
