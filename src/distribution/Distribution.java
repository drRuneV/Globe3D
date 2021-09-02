package distribution;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Utility.Clock;
import Colors.ColorGradient;
import Utility.Coordinate;
//import NetCdf.NetCDFAccess;
import distribution.statistics.StatDistr;
import Utility.RescaleFrame;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;



public class Distribution extends GridDistribution {

	private boolean hasTime;
	//
	private boolean hasDepth= false;
	//
	private boolean isBiomass=true;
	// Statistics for this distribution
	private StatDistr statistics;
	// Parameters from the netCDF file
	private NetCDFParameters parameters;
	// The mask used for selecting areas 
//•	protected SelectionMask selectedMask= null;
	// Information about the area this distributions covers 
//•	protected AreaInformation areaInfo=null;
	// Biomass
//•	protected BiomassMeasure biomass= null;
	// Tracker for the current time/count
	protected int count=0;

	//
	protected boolean useLeapYear=true;
	//
//•	protected ProgressFrame progressFrame= null;

	//
	String netCDFPath="";

	private ColorGradient gradient= new ColorGradient(1, 0);
	// This is the shape of the variable, e.g. 100x200x365
	private int[] shape;
	


	/**
	 * Inner class for parameters 
	 * @author a1500
	 *
	 */
	public class NetCDFParameters{
		
		public float scalefactor=1;
		public float fillV;
		public float missingV;
		public String fullName;
		
	}
	
	
	// ===============================================
	// 				Constructors					//
	
	/**
	 * Constructor 
	 * @param ncfile
	 */ // only used in the netCDF access «•»
	public Distribution(NetcdfFile ncfile) {
		super(ncfile);
	}

	

	/**
	 * Constructs a new distribution given the name of a netCDF viable and the netCDF file.
	 * 
	 * @param name The name of a netCDF variable
	 * @param ncfile the netCDF file where we look for the variable
	 */
	public Distribution(String name,NetcdfFile ncfile ) {

		super(ncfile);
		netCDFPath=ncfile.getLocation();


		long lastTime= (long) System.currentTimeMillis();

		// The netCDF is okay
		if (ncfile!=null) {

			Variable var= ncfile.findVariable(name);

			//If the variable exists
			if (var!=null)
				generateData(name, ncfile, var,"lat","lon");
			else {
				System.out.println("cannot find variableNetCDF :"+name);	
			}

			//Final message
			long timeTookms= (long) ((System.currentTimeMillis()-lastTime));
			message= String.format("Generating all data took:"+timeTookms+"  ms");
			System.out.println(message);
			//			progressFrame.update(message);

		}
	}
	
	
	/**
	 * Alternative creation to constructor. 
	 * Let us hint about the specific names of coordinates which may 
	 * be different for each variable like in current. 
	 * @param name variable name to look for
	 * @param hintlat hint about the latitude name , (e.g., u_lat & v_lat & rho_lat)
	 * @param hintlon hint about the longitude name
	 */
	public void alternativeCreate(String name, String hintlat, String hintlon) {

		netCDFPath=ncfile.getLocation();

		// The netCDF is okay
		if (ncfile!=null) {

			Variable var= ncfile.findVariable(name);

			//If the variable exists
			if (var!=null)
				generateData(name, ncfile, var, hintlat, hintlon);
			else {
				System.out.println("cannot find variableNetCDF :"+name);	
			} 
		}			

	}



	public void generateData(String name, NetcdfFile ncfile, Variable var, String hintlat,String hintlon) {
		setName(name);

		// Find the attributes of scale and unit
		parameters= new NetCDFParameters();
		defineAttributes(var);

		// Generate coordinate objects
		generateCoordinateLatLong(var,hintlat,hintlon);

		// Generate area
		generateArea();

		// Is this a biomass?
		isBiomass= ( var.getFullName().contains("bio") &&  var.getRank()<4) ;//?
		if (isBiomass) {
			System.out.println(var.getName()+" is biomass…");
		}

		//  Time/date
		hasTime = containsTime(var);
		if (hasTime) {
			Variable variableTime = findTimeVariable();
			generateTimeVariables(name, ncfile, variableTime);
		}

		//Only one date 1970
		else{
			dates=new int[1];
			dates[0]=Clock.daysBetween1950And1970*24;
			dateStrings= new String[1];
			dateStrings[0]= Clock.createADate(dates[0]);
		}


		//Create the full dataset
		createFullDataSet(var);

		// Statistics //  elephant July 7 took away the comments
		if (getValues().length<899000000) {//
			//					progressFrame.update("Generating statistics…");
			statistics= new StatDistr(this, new Point(0,this.getValues().length),true,false);
		}
		//				 Area
		//				areaInfo= new AreaInformation(this);
		//					if (!hasDepth) { //Specifically for physics with depth
		//				}
		// Biomass is calculated in VisualDistribution
		// November 1, 2018: change from distribution to VisualDistribution
		//				progressFrame.update("ok statistics, area information and biomass…");
	}



	/**
	 * 
	 */
	private boolean isLeapYear(Variable var) {
		boolean  useLeap =true;

		List<Attribute> at= var.getAttributes();
		for (Attribute attribute : at) {
			String na= attribute.getFullName().toLowerCase();
			if(na.contains("calendar")   && attribute.isString()){
				String calendarType= attribute.getStringValue();

				useLeap  = (calendarType.contains("noleap")) ? false : true;
			}
		}

		return useLeap;
	}



	@Override
	public void copyAllFrom(GridDistribution gd) {
		this.copyFrom(gd);
	}

	/**
	 * Copies data from another distribution
	 * @param d The distribution to copy from
	 */
	public void copyFromAnother(Distribution d){
		copyAllFrom(d);
		this.setStatistics(d.getStatistics());
		this.setParameters(d.getParameters());
	}

	

	/**
	 * Main class for testing
	 * @param args
	 */
	public static void main(String[] args) {
		String s =" August-20 :as well as July 19";
		s ="1995-01-01:01 ";
		
		String testName= "etopo1_bedrock 90 30 -30 30 .nc";
//		String topoFile= "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/etopo2.nc";
		String topoFile= "etopo2.nc";
		
		String name="CFbiom";	// "HERbiom"
		String p="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		NetcdfFile ncfile = testNetCDFFile(p,"ibm2felt.nc");
		Distribution distribution= new Distribution(name, ncfile);

		int x,y,z;
		int t= 5;
		for (int i = 0; i < distribution.getCoordinates().length; i+=100) {
			Coordinate c= distribution.getCoordinates()[i];
			float value = distribution.getValues()[i+t] ;
			System.out.println(i+" "+c.toString()+" Value= "+value);
		}
		
	}



	/**
	 * @return
	 */
	public static NetcdfFile testNetCDFFile(String p,String filename) {
		
		NetcdfFile ncfile = null;
		try {
			ncfile = NetcdfFile.open(p+filename);
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
		return ncfile;
	}


	/**
	 * Calculate the volume for the entire area the distribution covers
	 * @param topography
	 * @return
	 */
	public float[] calculateTheVolume(Topography topography){
		float[] volume= new float[area.length];
		boolean sea =false;
		
		for (int i = 0; i < volume.length; i++) {
			Coordinate co= getCoordinates()[i];
			sea=getValues()[i] != getFillV(); 
			if (sea) {
			volume[i]=area[i]* topography.getValueAtCoordinate(co)*(-1);
			}
		}
//		System.out.println(" Total water volume:");
		return volume;
	}
	
	
	//volume= area * topo.getValueAtCoordinate(coord)*(-0.001f); //cubic kilometres km3
	
	/**
	 * 
	 * @param var
	 */
	private void defineAttributes(Variable var) {
		List<Attribute> at= var.getAttributes();
		for (Attribute attribute : at) {
			String na= attribute.getFullName().toLowerCase();
			if(na.contains("scale")){
				parameters.scalefactor= attribute.getNumericValue().floatValue();
			}
			if(na.contains("unit")){
				unit= attribute.getStringValue();
			}

			if(na.contains("fill")){
				parameters.fillV= attribute.getNumericValue().floatValue();
			}

			if(na.contains("missing")){
				parameters.missingV= attribute.getNumericValue().floatValue();
			}//
			//long_name
			if(na.contains("name") && na.length()> 4){
				parameters.fullName= attribute.getStringValue();
			}
			
		}
//		System.out.println("scalefactor="+parameters.scalefactor+" Unit:"+unit);
		message = "Defined attributes…this" ;
	}



	/**
	 * Create the full dataset with all dimensions
	 * @param var
	 */
	private void createFullDataSet(Variable var) {
		
		// We find the number of dimensions from the shape or dimensions
		// The whole dataset is used so origin is 0,0,...
		int origin[] = new int[var.getShape().length]; // 3 with time… 4 with depth
		for (int i = 0; i < origin.length; i++) origin[i]= 0;	//so origin is 0,0,...
		
//		String dimensionNames= var.getDimensionsString();
		hasDepth= var.getRank()>3;
//		int withDepth= (hasDepth) ? 1: 0;
		
		// Since we are taking the whole dataset the size is simply the shape.
		// Think about the shape as the "shape of an object" 2×5×6 a box et cetera
		// The length of each dimension
		// In the case of the NORWECOM.E2E model the shape is typically: T:364 Y:123 X:168
		// The size is a array of 3 integers then, 364, 123, 168
		int size[]=  var.getShape();
		shape= var.getShape(); //  store the shape in the distribution
		
		// If depth dimension also: only one depth channel!.
		// Assumes: time depth y x
		// Origin determines which!:December 18
		if (hasDepth) {
			size[1]=1;
		}

		// Here we actually creating the data,this can be tricky because the sequence can vary
		// It is ASSUMED! that the dataset is arranged like this: Time Y X
		// the last one, x, is defined to be the width but keep in mind that this is more 
		// like the latitude 
		createData(var, origin, size);

		// Set the right values for the with/height/time dimension of the dataset
		// «•» Here we could actually look more directly for the corresponding dimensions!?
		
		// «•» If there is another dimension like depth, we have to take care of this here!
		// «•» The name of the dimensions will ultimately matter!!!
		// Data: [float temp(time=365, s_rho=30, eta_rho=124, xi_rho=169);
		
		// Use a index depending on the length of the shape to set the with/height/time
		int shapeix= var.getShape().length;
		
		// ASSUMES that width is the last variable
		setWidth(var.getShape(shapeix-1)); //2 if 3 dimensions
		
		// ASSUMES that height is the 2nd last variable
		setHeight(var.getShape(shapeix-2)); // 1
		
		
		// ASSUMES that time is the first variable //3rd last variable if it is there
		if (hasTime){  //(shapeix-3)==0) {
			setTime(var.getShape(0));	
		}
		// If there is no time, time is still one because there is one dataset
		else setTime(1);
	}


	/**
	 * Creates the Data set
	 * @param variable The netCDF variable we are creating the dataset for
	 * @param origin The starting point of the data we want to select
	 * @param size The size of the selection
	 * @return
	 */
	public boolean createData(Variable variable,int[] origin,int[] size){
		boolean isOkay=true;
		
		// access the attribute to get the missing value and replace this with -1
		// !!
		
		//Create the data
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
				values[i]=  data.getFloat(i)*parameters.scalefactor;
			}
	
		}
		catch (InvalidRangeException | IOException e) {
			// TODO Auto-generated catch block
			isOkay=false;
			e.printStackTrace();
		}
		return isOkay;
	
	}



	/**
		 * Try to find out if this variable contains time dimension
		 * 
		 * @param var - The variable we are checking
		 * @return - true if this variable contains a dimension starting with the specific name "T",
		 * of the first,0, dimension is called time  and the number of dimension is greater than 2
		 */
		private boolean containsTime(Variable var) {
	//		String allnames="";
			boolean containTime= false;
			ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) var.getDimensions();
			for(int i=0; i< dimensions.size() ;i++){
				containTime= (containTime || dimensions.get(i).getFullName().startsWith("T"));
	//			allnames +=dimensions.get(i).getFullName()+" ";
			}
			// Check specifically if the first  dimension is called time	: December 18
			containTime= containTime || dimensions.get(0).getFullName().contains("time");
	//		is3D= (allnames.contains("T")  && dimensions.size()>2);
			containTime= (containTime && dimensions.size()>2);
			String s= (containTime) ?"contains time!":"DOES NOT contain TIME!";
			System.out.println(this.getFullName()+" " +s);
			return containTime;
		}



	/**
	 * Tries to find the time variable 
	 * @return a netCDF time variable
	 */
	private Variable findTimeVariable() {
				
		List<Variable> variable=  ncfile.getVariables();
		String timeName="";
		// Search for variable names containing, time or T
		for (Variable v : variable) {
			String name= v.getFullName();
			int dimensions=v.getRank();

			if ((name.toLowerCase().contains("time")|| name.startsWith("T") ) &&  dimensions<2) {
				timeName=name;
			}
		}
		
		if (timeName!="") {
			System.out.println(name+":: We found the time name: "+ timeName);
		}
		else {
			System.out.println("Could not find the name time!!");
		}
		
		Variable variableTime= ncfile.findVariable(timeName);
		System.out.println(name+":: The variable: "+ variableTime);
		
		
		
		return variableTime;
	}



	/**
	 * @param name
	 * @param ncfile
	 * @param variableTime
	 */
	private void generateTimeVariables(String name, NetcdfFile ncfile, Variable variableTime) {
		// If we have a time variable
		if (variableTime!=null) {
	
			useLeapYear =   isLeapYear(variableTime);
			extractDates(ncfile, variableTime );
			if (!useLeapYear) {
				Clock clock= new Clock(dates[0], useLeapYear);
				int numberOfLeapYears= clock.numberOfLeapYears();
				String noleapYearString="";
				for (int d = 0; d < dates.length; d++) {
					noleapYearString=dateStrings[d];
					dates[d]+= numberOfLeapYears*24;
					dateStrings[d]= Clock.createADate(dates[d]);
				}
				//							System.out.println(noleapYearString+" --> Adjusted date:  "+ dateStrings[d]);
			}
		}
		else  {
			System.out.println("| ! Cannot find TIME variable !! for: "+name);
		}
	}



	/**
	 * 
	 * @param var
	 */
	private void generateCoordinateLatLong(Variable var,String hintlat, String hintlon) {
		//  Deal with associated dimensions which we expect
		List<Variable> variable=  ncfile.getVariables();
		String latitudeName="";
		String longitudeName="";
		// String of all the names of dimension for this variable (e.g. distribution)
		String dimNamesMain= var.getDimensionsString();
		boolean latWasFound= false; 
		boolean lonWasFound= false;

		// Search for variable names containing, lat and lon		
		for (Variable v : variable) {
			String name= v.getFullName();
			// will contain all the names of the dimensions for this variable
			String dimName= v.getDimensionsString();
			int nd=v.getRank();


			/* 
			 •–• we need to check that the dimensions in the latitude/longitude valuables
			 the same as in the distribution variable !! •• !! February 2021.
			 In this way we avoid picking the wrong latitude variable since there are 4 
			 different in the physics. 
			 The latitude variable must have a dimension(rank)<3 and contain a dimension name
			 
			 */
			
			// try to find latitude
			if (name.toLowerCase().contains(hintlat) && nd<3) {
				if (dimNamesMain.contains(dimName)) {
					latitudeName=name;
					latWasFound= true;
					System.out.println(var.getFullName()+" vs "+v.getName()+" dims: "
							+dimNamesMain+"  contain  "+ " "+ dimName);
				}
			}

			// try to find longitude
			if (name.toLowerCase().contains(hintlon) && nd<3) {
				if (dimNamesMain.contains(dimName)) {
					longitudeName=name;
					lonWasFound= true;
					System.out.println(var.getFullName()+" vs "+v.getName()+" dims: "
							+dimNamesMain+"  contain  "+ " "+ dimName);
				}

			}	
			else System.out.println("in Distribution:generateCoordinateLatLong() "
					+ "could not match "+name+" "+hintlon+" or "+dimNamesMain+" "+dimName);

		}
				
		Variable varlon= ncfile.findVariable(longitudeName); //Long");
		Variable varlat= ncfile.findVariable(latitudeName); //Latitude
		// 
//		System.out.println("latitude: "+varlat.getName()+" rank: "+varlat.getRank()+
//				" "+varlat.getDimensionsString());

		// Generate coordinates
		if (varlon!=null && varlat!=null) {
			extractNetCDFCoordinates(ncfile, varlat, varlon);
			message= "Distribution coordinates generated";
			System.out.println(message);
		}
		else System.out.println("generateCoordinateLatLong(): Could not find the variable names for the coordinates!!");
	}
	
	
	/**
	 * Generates the area for each grid cell
	 */
	private void generateArea(){

		//  Deal with associated dimensions which we expect
		List<Variable> variable=  ncfile.getVariables();
		String pnName="";
		String pmName="";
		// Search for variable names containing, lat and lon		
		for (Variable v : variable) {
			String name= v.getFullName();
			int dimensions=v.getRank();

			if (name.toLowerCase().contains("pn") && dimensions<3) {
				pnName=name;
			}

			if (name.toLowerCase().contains("pm") && dimensions<3) {
				pmName=name;
			}			
		}

		boolean found = (pnName!="" && pmName!="") ;

		if (found) {
			Variable varPM= ncfile.findVariable(pmName);
			Variable varPN= ncfile.findVariable(pnName);


			long lastFrame = System.currentTimeMillis();



			// Generate 
			if (varPM!=null && varPN!=null) {
				float scalef = findScaleFactor(varPM); //assume the same scaling for PN
				Array dataPM;
				Array dataPN;
				try {
					// Get all the data of 
					dataPM = varPM.read();
					dataPN = varPN.read();
					//Variables have the same length =width*height
					int length = (int) dataPM.getSize();
					area= new float[length]; //width*height
					float a=0;
					for (int i = 0; i < length; i++) {
						a=  dataPM.getFloat(i)*scalef *  dataPN.getFloat(i)*scalef;
						area[i]=1/a;								
						//					System.out.println("Generating area!");
					}
					long timeTookms= (long) ((System.currentTimeMillis()- lastFrame ));
					message = "Area generated…# "+area.length+" ms:" +timeTookms;

				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Found area variables: "+pmName+" "+pnName);
		}
		else {
			System.out.println("Unable to generate area !");
		}
	}

	
	/**
	 * Gives a the name  with year
	 * @return the full name followed by the year
	 */
	public String giveNameWithYear(){
		Clock clock= new Clock(getDates()[0],false);
		int year=clock.getCalendar().get(Calendar.YEAR);
		String name= getFullName() +String.format(" %d",year);
		return name;
	}

	
	/*
	 * Find the corresponding index for a given date string
	 */
	public static int findDateIndexFrom(String s,String splitter,String[] dateStrings){
		int index= -1;
		String sp[]= s.toLowerCase().split(splitter);

		// 
		if (sp.length>1) {
			String month= String.format(sp[0]);
			String day=   sp[1];
			int m= Clock.indexOfMonth(month)+1;
			if (m<10) {
				month= "0"+m;
			}
			else month= ""+m;

			// Format of the date string
			s = String.format(month+"-"+day);
			System.out.println("Corrected string: month day "+s);

			boolean wasFound=false;
			// Find the index by:
			//Loop through all the dates strings to find the corresponding text string
			for (int i = 0; i < dateStrings.length  && !wasFound; i++) {

				if (dateStrings[i].contains(s)){
					System.out.println("Found correspondence:"+dateStrings[i]+" "+s);
					index=i;
					wasFound=true;
				}
			}

		}
		return index;
	}

	
	
	public float findMaximum(){
		 float max= 0;
		 
		 for (float f : values) {
			max= (f>max) ? f:max ;
		}
		 
		 return max;
	}
	
	
	
	
	
	
	/**
	 * Evaluates if the index is above the maximum possible for this distribution 
	 * @param index the index to be evaluated
	 * @return the maximum allowed index
	 */
	public int theMaxIndex(int index){
		int indexMax= Math.min(index, values.length-1);		
		
		return indexMax;
	}
	


	/**
	 * Finds the XY point within the distribution given an index
	 * @param index the index
	 * @return a point corresponding to the XY within the distribution
	 */
	public Point XYFromIndex(int index){
		int t= index/getWH();	// t=0 if index less than wh!
		int indexXY= index-t*getWH();
		int y= indexXY/getWidth() ;
		int x= indexXY- y*getWidth();

		Point p= new Point(x,y);
		return p;
	}
	
	
	
	/**
	 * 
	 * Converts XYT to index
	 * @param x
	 * @param y
	 * @param t
	 * @return
	 */
	public int indexFromXYT(int x,int y,int t){
		int index= x+y*getWidth()+ t*getWH();
		return index;
	}
	
	/**
	 * Extracts one frame of values for the given time
	 * @param t the time
	 * @return an array of floats
	 */
	public float[] extractValues(int t){
		float[] v= new float[getWH()];
		for (int i = 0; i < v.length; i++) {
			v[i]= values[i+t*getWH()];		
		}
		return v;
	}
	
	
	/**
	 * Re-scales the distribution by using the RescaleFrame.
	 * 
	 * @param automatic whether the rescaling is done automatically
	 */
	public void rescaleValues(boolean automatic){

		RescaleFrame frame= new RescaleFrame(this);
		if (automatic) {
			frame.rescaleAutomatically();
			frame.dispose();
			frame.setVisible(false);
		}
		// Redefines the biomass in order to take into account new values
//		if (isHasTime()) {
//			setBiomass(new BiomassMeasure(this));
//		}
		//  We must update statistics, histogram
//•  getStatistics().createHistograms(time);//generateStatistics();
		//		distribution.recalculateBiomass(0);

		//  Accumulated distribution must be updated
//		if (isUseAccumulatedValues()) {
//			for (AccumulatedDistribution ad: getAccumulatedValuesList()) {
//				ad.calculate(true);
//			}
		}


	/**
	 * Re-scales automatically if the distribution has the unit ug C
	 */
	public void rescaleAutomatically(){
		// only for this unit 
		if (getUnit().contains("ug C")) {
			String unit= getUnit() ;
			unit= unit.replaceAll("ug", "g");
			
			setUnit(unit);
			rescale(1/1000000f);
			ColorGradient c=getGradient();
			c.setMax(findMaxMin()[0]/2);
			c.reDefine();
		}
	}

	
	/**
	 * Rescale is all the values
	 * @param scale The value to rescale with
	 */ 
	public void rescale(float scale){
		float f=0;

		for (int i = 0; i < getValues().length; i++) {
			f= getValues()[i];
			if (f!=getMissingV() && f!=getFillV() ) {
				getValues()[i]*= scale;
			}
		}
	}




	// ==================================================
	// Getters and setters
	// ==================================================

	
	
	public int count() {
		count++;
		count =  Math.min(count, getTime());
		count= (count==getTime()) ? 0 : count;
		return count;
	}



	/**
		 * @return the progressFrame
		 */
	//	public ProgressFrame getProgressFrame() {
	//		return progressFrame;
	//	}
	
	
	
		public int length() {
			return this.getValues().length;
		}



	// ==================================================
	// Getters and setters
	// ==================================================
	
	
	
	/**
	 * @return the hasTime
	 */
	public boolean isHasTime() {
		return hasTime;
	}



	/**
	 * @param hasTime the hasTime to set
	 */
	public void setHasTime(boolean hasTime) {
		this.hasTime = hasTime;
	}



	/**
	 * @return the scalefactor
	 */
	public float getScalefactor() {
		return parameters.scalefactor;
	}



	/**
	 * @param scalefactor the scalefactor to set
	 */
		public void setScalefactor(float scalefactor) {
		this.parameters.scalefactor = scalefactor;
	}




	/**
	 * @return the fillV
	 */
	public float getFillV() {
		return parameters.fillV;
	}



	/**
	 * @param fillV the fillV to set
	 */
	public void setFillV(float fillV) {
		this.parameters.fillV = fillV;
	}



	/**
	 * @return the missingV
	 */
	public float getMissingV() {
		return parameters.missingV;
	}



	/**
	 * @param missingV the missingV to set
	 */
	public void setMissingV(float missingV) {
		this.parameters.missingV = missingV;
	}
	


	public String getFullName() {
		return parameters.fullName;
	}



	public void setFullName(String fullName) {
		this.parameters.fullName = fullName;
	}




	/**
	 * @return the parameters
	 */
	public NetCDFParameters getParameters() {
		return parameters;
	}



	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(NetCDFParameters parameters) {
		this.parameters = parameters;
	}



	/**
	 * @return the statistics
	 */
	public StatDistr getStatistics() {
		return statistics;
	}



	/**
	 * @param statistics the statistics to set
	 */
	public void setStatistics(StatDistr statistics) {
		this.statistics = statistics;
	}



	

	
	// ==================================================
	// Implemented interface methods
	// ==================================================
	

	/**
	 * @return the selectedMask
	 */
//	public SelectionMask getSelectedMask() {
//		return selectedMask;
//	}



	/**
	 * @param selectedMask the selectedMask to set
	 */
//	public void setSelectedMask(SelectionMask selectedMask) {
//		this.selectedMask = selectedMask;
//	}



	/**
	 * @return the biomass
	 */
//	public BiomassMeasure getBiomass() {
//		return biomass;
//	}



	/**
	 * @param biomass the biomass to set
	 */
//	public void setBiomass(BiomassMeasure biomass) {
//		this.biomass = biomass;
//	}



	


	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}



	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}



	/**
	 * @return the hasDepth
	 */
	public boolean isHasDepth() {
		return hasDepth;
	}



	/**
	 * @param hasDepth the hasDepth to set
	 */
	public void setHasDepth(boolean hasDepth) {
		this.hasDepth = hasDepth;
	}



	/**
	 * @return the isBiomass
	 */
	public boolean isBiomass() {
		return isBiomass;
	}



	/**
	 * @param isBiomass the isBiomass to set
	 */
	public void setBiomass(boolean isBiomass) {
		this.isBiomass = isBiomass;
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


	public int[] getShape() {
		return shape;
	}



	public void setShape(int[] shape) {
		this.shape = shape;
	}


}

//
//String possibleNamesLat[] = {"Latt","latitude",""};
//String possibleNamesLong[] = {"Long",""};
//Variable varlat; 
//for (String latName : possibleNamesLat) {
//	varlat=ncfile.findVariable(latName);
//	if (varlat!=null) break;
//}
//for (String lonName : possibleNamesLong) {
//	varlat=ncfile.findVariable(latName);
//	if (varlat!=null) break;
//}
//
