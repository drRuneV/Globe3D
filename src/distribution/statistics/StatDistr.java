package distribution.statistics;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

//import org.omg.Messaging.SyncScopeHelper;

import distribution.Distribution;
import Colors.ColorInt;
import ucar.nc2.NetcdfFile;
//import visualised.VisualDistribution;



/**
 * 
 * @author a1500
 *
 */
public class StatDistr {
	
	public DoubleSummaryStatistics ss;
	private Distribution  distribution;
	private Point range;
	private int times;

	Histogram[] histograms = null ;
	Histogram totalHistogram= null;

	// Several histograms, one for each time step  «•»histogram as a separate object !«•»
//	int[][] columns;
//	int hIColorntervals= 120;
	
	Point centerMass[]; 
	StatDistr statistics[];
	// These values are for all values in the distribution
	private double std=0;
	private double median=0;
	private double step=1;
	// This is a image for a simple drawing of the histogram
	BufferedImage image =null;
	// Information string
	private String information="";
	boolean acceptZeros=false;
	private boolean useMask=false;
	private boolean useMedian=false;
	//
	private StatPresenter statPresenter= null;
	private boolean isPresenting=false;
	
//	private JFrame frame =null;
	
	

	/**
	 * Constructor
	 * @param distribution
	 * @param range
	 */
	public StatDistr (Distribution distribution, Point range,boolean fullDataset, boolean useM){
		this.distribution=distribution;
		this.range=range;
		// Times is the number of time step included in the given range 
		this.times= (range.y-range.x)/(distribution.getWH());
		
		// Do not calculate median for large distribution
		useMedian= (distribution.getWidth()<300 && useM);
		
		// Sets all the values 
		initiateSummary(range.x,range.y);
		
		// calculate statistics
		generateStatistics();
		//
		if (fullDataset  && distribution.isHasTime()) {
			testMap();
		}
		
	}
	
	/**
	 * 
	 * @param args
	 *
	public static void main(String[] args) {
		// String p="C:/Users/Documents/Eclipse Workspace/netCDF files/";
		String p= "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		NetcdfFile ncfileher;
		try {
			ncfileher = NetcdfFile.open(p+"sildyear1.nc");
			VisualDistribution vd= new VisualDistribution("HERbiom", ncfileher);
			Point r= new Point(0,vd.getValues().length);
			StatDistr statistics = new StatDistr(vd,r,true);
//			statistics.presenting(20, 0);
			for (int i = 0; i < 100; i++) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				statistics.presenting(200+10*i, i);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //sildyear1
		
	}
*/

	/**
	 * Generate statistics including STD and median
	 */
	public void generateStatistics(){

		if (ss.getCount()==0) {
		information="All values were either zero or Fill values!"; 
		}
		
		// • Please only start calculating values if there were some •
		else{
		
		// Must calculate standard deviation explicitly
		long mNow=  System.currentTimeMillis();
		std=calculateSTD();
		// Calculating medium ; takes a lot of time for big distributions!!!
		median=  (useMedian) ? calculateMedian(): 0;	//«•»
		
		//Histogram
		totalHistogram= new Histogram(selectAndFilter(range.x, range.y ));
		
		
		long timeTookms= (long) ((System.currentTimeMillis()- mNow));
		// System.out.println(" ->Calculating std+median took "+timeTookms+" ms");

		//«•»!Stack overflow was created for distributions without time because this piece of 
		//«•»code went into infinite recursion.Only distributions with time should enter here
		// Pre-calculate centre of mass and histogram  
		if (times == distribution.getTime() &&  distribution.isHasTime()) {
			mNow=  System.currentTimeMillis();

			centerMass= new Point[distribution.getTime()];
			// This calculates centre of mass and histograms for all time steps
			calcCenterMass();	
//			makeHistograms();
			createHistograms(distribution.getTime());

			timeTookms= (long) ((System.currentTimeMillis()- mNow));
//			System.out.println(" ->Calculating centre mass/histogram and statistics took "+timeTookms+" ms");


			mNow=  System.currentTimeMillis();
			//• Recursion Occurs here! -> We create new statistics objects for each time step
			// The constructor is called  typically ... 364 times!
			statistics= new StatDistr[times];
			//
			for (int i = 0; i < distribution.getTime(); i++) {
				int from= i*distribution.getWH();
				int to=  (i+1)*distribution.getWH();
				Point ra= new Point(from, to);
				statistics[i]= new StatDistr(distribution, ra,false, useMedian);
			}
			timeTookms= (long) ((System.currentTimeMillis()- mNow));
			System.out.println(" ->Calculating multiple 365 statistics took "+timeTookms+" ms");

		}
		
	}
	}

	
	/**
	 * puts all the values into double summary statistics
	 */
	private void initiateSummary(int from,int to){
		float v;
		ss= new DoubleSummaryStatistics();
		

		for (int i = from; i < to ;i++) {//distribution.getValues().length; i++) {
			v= distribution.getValues()[i];
			if (  criterion(v, i) ){
				ss.accept(v);
			}
		}
		
	}
	
	/**
	 * The criterion for accepting values:
	 * not fill values or missing values or 0 values if we do not accept 0
	 * @param v
	 * @param i
	 * @return
	 */
	private boolean criterion(float v, int i){
		boolean ok= false;
		int t= i/distribution.getWH();
		int index= (i>=distribution.getWH()) ? i-distribution.getWH()*t: i ;
		
		boolean mask= ( !useMask); 
				// (useMask && distribution.getSelectedMask().getMask()[index]!=0) ;

		//  
		ok =  v!=distribution.getFillV() && v!=distribution.getMissingV()
				 && ( acceptZeros || (!acceptZeros && v!=0) ) 
				 && mask ;
		return ok;
	}

	
	
	
	
	/**
	 * Selects a range of values from the distribution
	 * and filter them according to a criterion and returns an array of doubles 
	 * @param from index from
	 * @param to index to
	 * @return a selection of values from distribution values
	 */
	private  double[] selectAndFilter(int from,int to){
		
		ArrayList<Double> list= new ArrayList<Double>();
		//
		for (int i = from; i < to; i++) {
			double v= distribution.getValues()[i];
			if (  criterion((float) v, i) ){
				list.add(v);
			}
		}

		double[] values= list.stream().mapToDouble(f->f).toArray();
		
//		List<float[]> floatList = Arrays.asList(distribution.getValues());
//		double[] doubleArray = floatList.stream()
//			    .mapToDouble(f->??){
//		        .filter(f-> f>0) // Or whatever default you want.
//			    .toArray();
		
		
		return values;
//		ArrayList<Float> targetList = new ArrayList<Float>;
//		 List<Integer> targetList = List.newArrayList(values);
//		List<Float> targetList = Arrays.asList(values);
//
//		List<Double> list = Arrays.stream(values).boxed().collect(Collectors.toList());
//		double[] doubleArray = floatList.stream()
//		    .mapToDouble(f->) // Or whatever default you want.
//		    .toArray();
	}
	
	
	/**
	 * Create all histograms
	 * @param n  the number of histograms
	 */
	public void createHistograms(int n) {
		
		int from=0;
		int to=0;
		int wh=distribution.getWH();
		int timeSteps= distribution.getTime();
		histograms= new Histogram[n];
		
		for (int t = 0; t < timeSteps; t++) {
			from= t*wh;
			to  = (t+1)*wh ;
			histograms[t]= new Histogram(selectAndFilter(from,to));
		}
		
	}
	
	
	
	/**
	 * Making map of different values
	 * @return
	 */
	public Map<String,double[]> makeMap(){
		
		Map<String, double[]> map= new TreeMap<String, double[]>();
		String[] names= new String[]{"Max","Min","Avg", "Median", "Std"};

		int n= statistics.length;
		double[] max= new double[n];
		double[] min= new double[n];
		double[] average= new double[n];
		double[] median= new double[n];
		double[] std= new double[n];
		
		int i=0;
		for ( StatDistr stat : statistics) {
			max[i]= stat.getMax();
			min[i]= stat.getMin();
			average[i]= stat.getAverage();
			median[i]=stat.getMedian();
			std[i]= stat.getStd();
			i++;
		}
		
		map.put(names[0], max);
		map.put(names[1], min);
		map.put(names[2], average);
		map.put(names[3], median);
		map.put(names[4], std);
		
		return map;
		
	}
	
	private void testMap(){
		Map<String, double[]> map= makeMap();

		//
//		for (String key : map.keySet()){
//	        iterate over keys
//	        System.out.println(key+" "+map.get(key));
//	    }
	        
		
	    for (double[] value : map.values()){
	        //iterate over values
	    	int n=0;
	    	for (double d : value) {
				n++;
//	    		System.out.println(n+" value "+d);
			}
	    }
	}


	/**
	 * Presents the statistics on a Jframe, continuously.
	 * @param dx X location on screen
	 * @param time the time step/day to use for updating
	 */
	public void presenting(int dx, int time){

		// Create and set up the frame if not existing
		if (statPresenter==null || !isPresenting) {
			statPresenter= new StatPresenter(this);
			statPresenter.setTitle(getDistribution().getFullName());
			setPresenting(true);
		}

		// Continuous updating
		statPresenter.update(dx, time);
		
	}
	
	
		
	/**
	 *   Calculates the centre of mass for all time steps
	 */
	public void calcCenterMass(){
		for (int t = 0; t < centerMass.length; t++) {
			centerMass[t]= calcCenterMass(t);
		}

	}
	
	/**
	 * Calculates the centre of mass for the current time step
	 * @param time - the given time
	 * @return - the centre of mass
	 */
	private Point calcCenterMass(int time){
		float v;
		Point cm= new Point();
		float vx=0;
		float vy=0;
		float total=0;
		int wh= distribution.getWH();
		
		for (int x = 0; x < distribution.getWidth(); x++) {
			for (int y = 0; y < distribution.getHeight(); y++) {
				v = distribution.getValues()[x+y*distribution.getWidth()+ time*wh];
				//What to do with negative values??
				if (v!=distribution.getFillV() && v!=distribution.getMissingV() && v>0) {
				total+=v;
				vx+= x*v;
				vy+= y*v;
				}
			}
			cm.x=  (int) (vx/total);
			cm.y=  (int) (vy/total);
//			System.out.println("cm: "+cm.x+" : "+cm.y);
		}
		
		return cm;
	}

	
	/**
	 * 
	 */

	
	
	/**
	 * Calculates the standard deviation
	 * @return
	 */
	private double calculateSTD(){
		double std=0;
		
		double square=0;
		float v=0;
		int  n=0;
//		for (float v: distribution.getValues()){
		for (int i = range.x; i < range.y; i++) {
			v= distribution.getValues()[i];
			if (criterion(v, i)){
			square+= Math.pow(v-ss.getAverage(), 2);
			n++;
			}
		}
		if (n>0) {
		square/= n;
		std= Math.sqrt(square);
		}
		else System.out.println("No values met the criteria! when calculating standard deviation");
		
		return std;
	}

	/**
	 * Calculates the median by sorting the values
	 * So far only done for all the values and not for each time step
	 * «•»This takes a lot of time and should maybe be done using the histograms «•»
	 *  
	 * @return
	 */
		private double calculateMedian(){
			double m=0;
			float v=0;
	//		int i=0;
			ArrayList<Float> list= new ArrayList<>();
			
			// Calculate within the range
			for (int i = range.x; i < range.y; i++) {
				v= distribution.getValues()[i];
				if (criterion(v, i)){
					list.add(v);
				}
			}
			
			if (!list.isEmpty()) {
				list.sort(null);
				m= list.get(list.size()/2) ; // the middle
//				System.out.println("Calculating median:"+m+" count:"+list.size()+" of "+distribution.getValues().length);
			}
			else System.out.println("No values met the criteria! when calculating the median");
	//		
	//		for (Float float1 : list) {
	//			System.out.println(float1+"\n"); // Value seems to be sorted!!!.
	//		}
	//		
			list.clear();
			
			return m;
		}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StatDistr [average=" + ss.getAverage() + ", sum=" + ss.getSum() + ", std=" + std + ", min=" + 
				ss.getMin() + ", max=" + ss.getMax()+ ", step=" + step + ", count=" + ss.getCount() + "]";
	}
//
//	/**
//	 * @return the ss
//	 */
//	public DoubleSummaryStatistics getSs() {
//		return ss;
//	}

//
//	/**
//	 * @param ss the ss to set
//	 */
//	public void setSs(DoubleSummaryStatistics ss) {
//		this.ss = ss;
//	}


	/**
	 * @return the centerMass
	 */
	public Point[] getCenterMass() {
		return centerMass;
	}


	/**
	 * @param centerMass the centerMass to set
	 */
	public void setCenterMass(Point[] centerMass) {
		this.centerMass = centerMass;
	}



	/**
	 * @return the std
	 */
	public double getStd() {
		return std;
	}
	
	

	
	public double getMedian() {
		return median;
	}


	public void setMedian(double median) {
		this.median = median;
	}


	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}


	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}


	/**
	 * @return the information
	 */
	public String getInformation() {
		return information;
	}


	/**
	 * @param information the information to set
	 */
	public void setInformation(String information) {
		this.information = information;
	}


	/**
	 * @return the acceptZeros
	 */
	public boolean isAcceptZeros() {
		return acceptZeros;
	}


	/**
	 * @param acceptZeros the acceptZeros to set
	 */
	public void setAcceptZeros(boolean acceptZeros) {
		this.acceptZeros = acceptZeros;
	}


	/**
	 * @return the useMask
	 */
	public boolean isUseMask() {
		return useMask;
	}


	/**
	 * @param useMask the useMask to set
	 */
	public void setUseMask(boolean useMask) {
		this.useMask = useMask;
	}

	public double getAverage() {
		return ss.getAverage();
	}

	
	public double getMax() {
		return ss.getMax();
	}


	public double getMin() {
		return ss.getMin();
	}



	/**
	 * @return the isPresenting
	 */
	public boolean isPresenting() {
		return isPresenting;
	}


	/**
	 * @return the useMedian
	 */
	public boolean isUseMedian() {
		return useMedian;
	}


	/**
	 * @param useMedian the useMedian to set
	 */
	public void setUseMedian(boolean useMedian) {
		this.useMedian = useMedian;
	}

	/**
	 * @return the ss
	 */
	public DoubleSummaryStatistics getSs() {
		return ss;
	}

	/**
	 * @param ss the ss to set
	 */
	public void setSs(DoubleSummaryStatistics ss) {
		this.ss = ss;
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

	/**
	 * @return the histograms
	 */
	public Histogram[] getHistograms() {
		return histograms;
	}

	/**
	 * @param histograms the histograms to set
	 */
	public void setHistograms(Histogram[] histograms) {
		this.histograms = histograms;
	}

	/**
	 * @return the statistics
	 */
	public StatDistr[] getStatistics() {
		return statistics;
	}

	/**
	 * @param statistics the statistics to set
	 */
	public void setStatistics(StatDistr[] statistics) {
		this.statistics = statistics;
	}

	/**
	 * @param isPresenting the isPresenting to set
	 */
	public void setPresenting(boolean isPresenting) {
		this.isPresenting = isPresenting;
	}

	/**
	 * @return the totalHistogram
	 */
	public Histogram getTotalHistogram() {
		return totalHistogram;
	}

	/**
	 * @param totalHistogram the totalHistogram to set
	 */
	public void setTotalHistogram(Histogram totalHistogram) {
		this.totalHistogram = totalHistogram;
	}

	

}



//(Obs: Arrays.copyOf må brukes i stedet for (Float[]) cast)
//
//@Test
//public void test2() {
//    float[] values = {1f, 3.4f, 5.6f};
//    Object[] v1 = IntStream
//            .range(0, values.length)
//            .mapToObj(i -> values[i])
//            .parallel()
//            .filter(d -> criterion(d))
//            .toArray();
//    Float[] f1 = (Float[])Arrays.copyOf(v1, v1.length, Float[].class);
//    Object[] v2 = IntStream
//            .range(0, values.length)
//            .mapToObj(i -> new IndexedFloat(values[i], i))
//            .parallel()
//            .filter(d -> criterion(d.f, d.idx))
//            .map(d-> d.f).toArray();
//    Float[] f2 = Arrays.copyOf(v2, v2.length, Float[].class);
//}
//
//// Helper class for float/index holder
//class IndexedFloat {
//
//    public float f;
//    public int idx;
//
//    public IndexedFloat(float f, int idx) {
//        this.f = f;
//        this.idx = idx;
//    }
//}
//
//static boolean criterion(float f) {
//    return f > 1.5f;
//}
//static boolean criterion(float f, int i) {
//    return f > 1.5f && i < 3;
//}
//

//public void test2() {
//    float[] values = {1f, 3.4f, 5.6f};
//    Float[] v1 = (Float[])IntStream
//            .range(0, values.length)
//            .mapToObj(i -> values[i])
//            .parallel()
//            .filter(d -> criterion(d))
//            .toArray();
//    
//    Float[] v2 = (Float[])IntStream
//            .range(0, values.length)
//            .mapToObj(i -> new IndexedFloat(values[i], i))
//            .parallel()
//            .filter(d -> criterion(d.f, d.idx))
//            .map(d-> d.f).toArray();
//}

// Helper class for float/index holder
//class IndexedFloat {
//
//    public float f;
//    public int idx;
//
//    public IndexedFloat(float f, int idx) {
//        this.f = f;
//        this.idx = idx;
//    }
//}
//
//static boolean criterion(float f) {
//    return f > 1.5f;
//}
//static boolean criterion(float f, int i) {
//    return f > 1.5f && i < 3;
//}
