package distribution.statistics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.DoubleSummaryStatistics;

public class Histogram {
	
	double[] data=null;
	int[] columns=null;
	int interval= 120;
	long count=0;
	double  step  ;
	int max=0;
	public DoubleSummaryStatistics ss;
	
	// This is a image for a simple drawing of the histogram
	BufferedImage image =null;
	private int average;
	

	/**
	 * Constructor
	 * @param data The data to use in the histogram 
	 */
	public Histogram(double[] data ) {
		this.data =data;
		// 
		ss= new DoubleSummaryStatistics();
		insertValuesStatistics();
		count =(int) ss.getCount();
		// Create the histogram
		makeHistograms();
	}
	
	
	
	

	/**
	 * Creates statistics of the data used for the histogram
	 */
	private void insertValuesStatistics() {
		for (double d : data) {
			ss.accept(d);
		}
	}





/**
 * Initialises histogram
 */
private void initiate(){
	step= (float) ((ss.getMax()-ss.getMin())/interval);

	// Create columns and inserts 0
	columns= new int[interval];
	for(int ix=0; ix<interval; ix++ ){
		columns[ix]=0;			
	}

}




/**
 * Makes the histogram from the data values
 */
private void makeHistograms(){

	if (ss.getCount()>0) {
		initiate();

		for (int i = 0; i < data.length; i++) {
			int ix = (int) ((data[i]-ss.getMin())/step);
			if (ix>=0 &&  ix<interval) {
				columns[ix]++;			
			}
		}
		//Find maximum
		for (int i : columns) {
			max= Math.max(max, i);
			average+=i;
		}
		average/=columns.length;
	}
	else{
		System.out.println("No data to make histogram of!");
	}
	
	
//	System.out.println("Maximum in histogram:"+max+" Average: "+average);
}


/**
 * Draw the histogram of the image
 * @return
 */
public BufferedImage drawHistogram(){

	// 
	//we could use the class line plot, but 1st we need 
	//copy integer array to float array like this:
	//  float[] =Arrays.stream(ints).asFloatStream().toArray(); -> Java 8 feature
	// we also need to make a method in plot line which can give us a image.
	// «•» «•» «•» «•»
	//
	float dy;
	int height= 90;
	float  heightFactor= (average> 2*height) ? 0.1f:   1;//height/average;

	Color col= new Color(250, 250, 255, 230);
	int color = col.getRGB();
	image = new BufferedImage(interval+5, height+5, BufferedImage.TYPE_INT_ARGB);
	//		if (histogram.length) {
	//		}
	Graphics2D graphic= image.createGraphics();
	// 
	for (int i = 1; i < interval+1 ;i++) {
		dy = columns[i-1]* heightFactor;//0.1f;
		dy = Math.min(dy, height);
		//			v = getMin()+i*step;			

		graphic.drawLine(i, height, i, height-(int) dy);
		graphic.drawOval(i-1, height-(int)dy-1, 2, 2 );
		//			for (int j = 0; j < dy; j++) {
		//				image.setRGB(i, height-(int) j, color);	
		//			}
	}

	int indexAverage= (int) ((boolean) (ss!=null) ? ss.getAverage()/step: (ss.getAverage()/step));
	indexAverage= Math.min(interval-1, indexAverage);
	// Grey background
	graphic.setColor(new Color(50,50,50,150) );
	graphic.fillRect(1,1, interval, height);
	// The histogram image
	graphic.drawImage(image,0, 0,null,null);

	// Information about total average
	String format1= (ss.getMax()> 10000  ) ?  "%.1e"  :"%.2f";
	graphic.setColor(new Color(250,250,250,150) );
	graphic.drawString(String.format("# %d", count), 5, 15);

	// Indicate the average by a red line
	graphic.setColor(Color.red);
	graphic.drawLine(indexAverage, height, indexAverage, 5);

	// Draw the string of average
	if (ss!=null) {
		graphic.setColor(Color.white);
		graphic.drawString(String.format(format1, ss.getAverage()), indexAverage, 45);
	}
	else{
		System.out.println("! No statistics for:");
	}


	return image;	
}





/**
 * @return the interval
 */
public int getInterval() {
	return interval;
}





/**
 * @param interval the interval to set
 */
public void setInterval(int interval) {
	this.interval = interval;
}



}
