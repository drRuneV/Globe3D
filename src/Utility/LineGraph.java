package Utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

//import visualised.VisualDistribution;

/**
 */
public class LineGraph {


	private ChartPanel chartPanel;
	private JFreeChart chart;
	private XYSeriesCollection series;
	public  XYSeries  serie;
	private JFrame frame= null;
	private boolean frameWasClosed=false;
	//
	static final Color[] lineColour= {Color.blue.darker(), Color.red.darker(), 
			new Color(0,120,0),  //dark green
			new Color(90,0,90), //dark purple
			Color.darkGray};
	
	private boolean useIndicateCircles=false;      
	
	/**
	 * Constructor
	 * This comp constructor enables the legends to be removed
	 * @param values
	 * @param title
	 * @param xtitle
	 * @param unit
	 * @param noLegend
	 */
	public LineGraph(float[] values ,String title,String xtitle,String unit,boolean noLegend) {
		this(values,title,xtitle,unit);
		if (noLegend) {
			chart.removeLegend();
		}
		
	}

	/**
	 * Constructor
	 * @param vales the values to be plotted
	 * @param title the title of the chart
	 * @param name the name of the data serie
	 * @param xtitle the X axis label
	 * @param unit the Y axis unit
	 */
	public LineGraph(float[] values ,String title,String name,String xtitle,String unit) {
		this(values,name,xtitle,unit);
		chart.setTitle(title);
	}
	
	
	/**
	 * Constructor.
	 * Constructs a line graph with the values and necessary titles unit string.
	 * @param values the list of floatingpoint values
	 * @param title the title of the graph
	 * @param xtitle the X axis label
	 * @param unit the unit of the y-axis, i.e. the values.
	 */
	public LineGraph(float[] values ,String title,String xtitle,String unit) {
		
		// Main series
		serie= new XYSeries(title);
		fillDataSerie(values,serie);
		// collection of series
		series= new XYSeriesCollection(serie);
		
		createChart(title, xtitle, unit);
		
        
        defineColour(0,false);
                
	}

	/**
	 * @param title
	 * @param xtitle
	 * @param unit
	 */
	private void createChart(String title, String xtitle, String unit) {
		// Create the actual line chart  
		 chart = ChartFactory.createXYLineChart(title, xtitle, unit, series);
        // we put the chart into a panel
        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.white);
        
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(new Color(200, 210, 210, 255));
        chart.getTitle().setFont(new Font("Tahoma", Font.BOLD, 16));
        // default size
        Dimension d= new  Dimension(400, 340);
        chartPanel.setPreferredSize(d);
	}

	
	/**
	 * Enables definition of the axis. Especially restrict the maximum.
	 * @param maxY
	 */
	public void defineTheAxis(double maxY){
		NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis(0);
		ValueAxis rangeAxis =  ((XYPlot) chart.getPlot()).getRangeAxis();
		rangeAxis.setRange(0.0, maxY);
//        domain.setRange(0.00, 100.00);
//        domain.setTickUnit(new NumberTickUnit(0.1));
		domain.setVerticalTickLabels(true);
	}

	
	/**
	 * Sets the axis to automatic range for the y-axis.
	 */
	public void axisIsAutoRange(){
		((XYPlot) getChart().getPlot()).getRangeAxis().setAutoRange(true);
	}
	
	
	/**
	 * Convert integer table to float
	 * @param values
	 * @return
	 */
	public static float[] convertToFloat(int[] values) {
		
		float[] val= new float[values.length];
		int j=0;
		
		for (int  i : values) {
			val[j++]=i;
		}
		return val;
	}
	
	/**
	 * Fills the data series with the given values
	 * @param values the values were going to fill in
	 * @param ser the chart serie
	 */
    private void fillDataSerie(float[] values, XYSeries ser ) {
    	ser.clear();
    	
    	for (int i = 0; i < values.length; i++) {
			ser.add(i, values[i]);
		}
    	
    	
//    	System.out.println("InsertedSerie: "+ser.getItemCount());//getColumnCount());
    }
    
    private void defineColour(int n	,boolean useFill){
    	//
    	n= Math.min(n, lineColour.length-1);
    	XYPlot plot = chart.getXYPlot();
        test(plot);
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    	int w=3;
        Shape circle = new Ellipse2D.Double(-w+n, -w+n, 2*w-n, 2*w-n);
        
        renderer.setSeriesShape(n,circle);
        renderer.setSeriesPaint(n, lineColour[n]);
        // Filled symbols if not so many points
//        if (serie.getItemCount()< 200) {
//        if (useFill) {		
        renderer.setUseFillPaint(useFill);
        renderer.setSeriesShapesFilled(n, false);
        renderer.setSeriesShapesVisible(n, useFill);
        renderer.setUseOutlinePaint(useFill);
        renderer.setSeriesOutlinePaint(n, Color.gray);
//        }
        
        plot.getDomainAxis().setLabelFont(new Font("Times", Font.PLAIN, 12));
        ValueAxis range = plot.getRangeAxis();
//        range.setLowerBound(0.0);
//        range.setUpperBound(10e5);
        //  
        plot.setRenderer(renderer);
    }
    

	/**
     * Adds another series to the collection of series
     * @param values the values for the serie
     * @param title the title for this serie
     */
    public void addSerie(float[] values,String title){

    	XYSeries  dataSerie= new XYSeries(title);

    	fillDataSerie(values, dataSerie);

    	if (series.getSeries().contains(dataSerie)) {
    		System.out.println("Already containing the serie");
    	}
    	else {
    		series.addSeries(dataSerie);
    		int n=series.getSeriesCount();
    		defineColour(n-1,false);
    	}
    }
    
    
    /**
     * Updates the main series with a new set of values
     * @param values the array of floats
     * @param title the chart title
     * @param sn the series number
     */
    public void update(float[] values,String title,int sn){

    	if (frame==null) {
    		show();
    	}
    	// Making visible again
    	else if (!frame.isVisible()) {
//			show();
    		frame.setVisible(true);  // May 5:
		}

    	if (sn< series.getSeriesCount()) {

    		XYSeries theSerie = series.getSeries(sn);
    		theSerie.clear();
    		if (sn==0) {
    			chart.setTitle(title);
    		}
    		
    		fillDataSerie(values, theSerie);

    	}
    }
    	
    
    /**
     * 
     * @param d
     * @return
     */
    public BufferedImage createImage(Dimension  d){
		// Create a image of the chart to display
    	BufferedImage imageChart= chart.createBufferedImage(d.width,d.height);
    	return imageChart;
    }


    /**
     * Saves all the values from the series to a text file
     */
    public void saveValues(){

    	File file=FileUtility.createFileFromFileDialogue("Saving the values of the series", ".txt",frame);
    	
    	// Create the lines of text and write them to the file
    	if (file!=null) {

    		StringBuilder sb=createStringLinesOfValues();
    		FileUtility.writeTextFile(file, sb);

    		//    		try {
    		//    			FileWriter fileWriter = new FileWriter(file);
    		//    			BufferedWriter bw = new BufferedWriter(fileWriter);
    		//    			bw.write(sb.toString());

    		System.out.println("Information writing the information"+sb.toString());

    		//    			bw.flush();
    		//    			bw.close();
    		//    			fileWriter.close();
    		//    		} catch (IOException e) {
    		//    			e.printStackTrace();
    		//    		}

    	}



    	
    	
    	
 

    }

	/**
	 * 
	 */
	private StringBuilder  createStringLinesOfValues() {
		
		//Build the string
    	StringBuilder sb = new StringBuilder();
    	String sep= System.lineSeparator();

    	//
    	String line;
    	//Name
    	String format=(true) ? "%.9f": "%.2f";
    	
    	//Header
    	String the1stLine = "x \t" ;
    	String[] names=  extractNames();
    	for (String name: names) {
			the1stLine+=name+"\t "  ;
		}
    	line= String.format("%s",the1stLine);
    	sb.append(line+sep);
    
    	//  Generate the data from the series
    	double[][] data = generateData();
    	XYSeries  mainSerie=(XYSeries) series.getSeries().get(0);
    	int row= mainSerie.getItemCount();
    	int ns =  series.getSeriesCount();
    	
    	// Write each row
         for (int i=0; i< row; i++)
         {
        	 // 
        	 double x = ((XYDataItem) mainSerie.getItems().get(i)).getXValue();
        	 // 
        	 line= String.format("%.1f \t ",x);
        	 // Each column for each serie
        	 for (int c = 0; c < ns; c++) {
        		 double y =data[i][c];//item.getYValue();
        		 line+=String.format(format+" \t",y);
        	 }

        	 //        	 double y1 =(double) item.getY();

        	 System.out.println("out : "+line);
//        	 System.out.println("x= "+x+ " y= "+y);
        	 sb.append(line+sep);
         }
    	    	
    	
    	return sb;
	}
    
    
    
	/**
	 * Generates data in a row by colum
	 * @return a two-dimensional array of y values for all series
	 */
	private double[][] generateData() {
		//
		XYSeries  mainSerie=(XYSeries) series.getSeries().get(0);
		int row=  mainSerie.getItemCount();
		int ns =  series.getSeriesCount();

		double[][] data= new double[row][ns];
		// Rows
		for (int r=0; r< row; r++)
		{
			// Serie – column
			for (int s = 0; s < ns; s++) {
				XYSeries  serie=(XYSeries) series.getSeries().get(s);
				XYDataItem item = (XYDataItem) serie.getItems().get(r);
				double y =(double) item.getY();
				data[r][s]=y;
			}
		}

		return data;
	}

	

	/**
     * Extracts all the names from the series and put them in a list of strings
     * @return a list of strings
     */
	private String[] extractNames() {
    	String[] names=new String[series.getSeriesCount()];
    	List<XYSeries> list = series.getSeries();
    	int i=0;
    	for (XYSeries s : list) {
    		names[i]= s.getKey().toString();
			i++;
		}
    	
    	return names;
	}

	private MouseMotionListener createMouseListener() {
		return new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent me) {
				frame.toFront();
				frame.requestFocusInWindow();
				chartPanel.requestFocusInWindow();
//				System.out.println("… mouse was moved in the line graph");
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	/**
	 * @return
	 */
    private WindowListener createWindowlessForFrame() {
		return new WindowListener() {



@Override
public void windowOpened(WindowEvent arg0) {
}

@Override
public void windowIconified(WindowEvent arg0) {
}

@Override
public void windowDeiconified(WindowEvent arg0) {
}

@Override
public void windowDeactivated(WindowEvent arg0) {
}

@Override
public void windowClosing(WindowEvent arg0) {
	frameWasClosed=true;
}

@Override
public void windowClosed(WindowEvent arg0) {
	frameWasClosed=true;
}

@Override
public void windowActivated(WindowEvent arg0) {
}
};
	}

	/**
	 * @return
	 */
	private KeyListener createKeyListener() {
		return new  KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				boolean controlDown= e.isControlDown();
				boolean shiftDown = e.isShiftDown();
				boolean only = !controlDown && !shiftDown ;
				int key= e.getKeyCode();
				// Used for rescaling the frame
				Dimension dimension= frame.getSize();
				// Use for accessing the range axis (y)
				ValueAxis rangeAxis =  ((XYPlot) chart.getPlot()).getRangeAxis();
				// Used for accessing the frame in order to move it 
				Point position=frame.getLocation();
				
				//   Save Values
				if(key == KeyEvent.VK_S  && controlDown){
					saveValues();
				}
				// Point indicator on/off  
				else if(key == KeyEvent.VK_C){
					useIndicateCircles= !useIndicateCircles;
					for (int i = 0; i < series.getSeriesCount(); i++) {
						defineColour(i, useIndicateCircles);
					}
				}
				// y-axis is automatic or not…
				else if(key == KeyEvent.VK_A){
					boolean auto= rangeAxis.isAutoRange();
					rangeAxis.setAutoRange(!auto);
				}
				// Increase axis
				else if(key == KeyEvent.VK_UP  && only){
					double maxY=rangeAxis.getRange().getUpperBound();
					rangeAxis.setRange(0.0, maxY*2f);
				} 
				// Decrease axis
				else if(key == KeyEvent.VK_DOWN && only){
					double maxY=rangeAxis.getRange().getUpperBound();
					rangeAxis.setRange(0.0, maxY*0.5f);
				}
				//  Decrease the height of the frame
				else if(key == KeyEvent.VK_DOWN && shiftDown){
					dimension.height-=2;
					frame.setSize(dimension);
				}
				//  Increase the height of the frame
				else if(key == KeyEvent.VK_UP && shiftDown){
					dimension.height+=2;
					frame.setSize(dimension);
				}
				// Decrease the width of the frame
				else if(key == KeyEvent.VK_LEFT && shiftDown){
					dimension.width-=2;
					frame.setSize(dimension);
				}
				// Increase the width of the frame
				else if(key == KeyEvent.VK_RIGHT && shiftDown){
					dimension.width+=2;
					frame.setSize(dimension);
				}
				else if(key == KeyEvent.VK_PLUS){
					dimension.width*=1.5f;	
					dimension.height*= 1.5f;
					frame.setSize(dimension);
				}
				else if(key == KeyEvent.VK_MINUS){
					dimension.width*= 0.66666f;
					dimension.height*= 0.66666f;
					frame.setSize(dimension);
				}
			
			
				//  Move the frame
				else if(controlDown){
					if(key == KeyEvent.VK_UP) position.y-=2;
					if(key == KeyEvent.VK_DOWN) position.y+=2;
					if(key == KeyEvent.VK_LEFT) position.x-=2;
					if(key == KeyEvent.VK_RIGHT) position.x+=2;
					frame.setLocation(position);
				}



				
			}
		};
	}
	
    
	/**
	 * Shows the line graph in a separate JFrame.
	 * The JFrame is created here.
	 * 
	 */ 	// We could add parameters for the look out of the frame maybe
	public void show(){
		
		frame =new JFrame()  ;
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		frame.add(chartPanel);
		frame.pack();
		
		// Add a menu here
	
		KeyListener[] k=   chartPanel.getKeyListeners();
		KeyListener keyboardListener=createKeyListener();
		frame.addKeyListener( keyboardListener);
		chartPanel.addKeyListener(keyboardListener);
		chartPanel.addMouseMotionListener(createMouseListener());
	
		frame.addWindowListener(createWindowlessForFrame());
		
		frame.setSize(500, 400);
		frame.setMinimumSize(new Dimension(450, 350));
		frame.setVisible(true);
	}

	/**
	 * Shows the frame at a specific location
	 * @param p the pixel position to place the frame
	 */
    public void show(Point p){
    	show();
    	frame.setLocation(p);
    }
    
    
    
    /**
	* Shows the frame at a specific location with a title 
	 * @param p the pixel position to place the frame
     * @param title The title in the frame
     */
    public void show(Point p,String title){
    	show();
    	frame.setLocation(p);
    	frame.setTitle(title);
    }

    /**
     * Enable setting the title and location of the frame
     * @param p The location of the frame
     * @param title The title on frame
     */
    public void setFrameTitleAndLocation(Point p,String title){
    	frame.setLocation(p);
    	frame.setTitle(title);
    }

    
 // Getters and setters:
//  ==================================================
    
    
    
    
    
	/**
	 * @param plot
	 */
	private void test(XYPlot plot) {
		//
		Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[1] = new Color(0,0,0); 
		paintArray[2] = new Color(0x00, 0xBB, 0x00);
		paintArray[3] = new Color(0xEE, 0xAA, 0x00);
	
		plot.setDrawingSupplier(new DefaultDrawingSupplier(
				paintArray,
				DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		//
	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	/**
	 * @param chartPanel the chartPanel to set
	 */
	public void setChartPanel(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
	}

	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @return the series
	 */
	public XYSeriesCollection getSeries() {
		return series;
	}

	/**
	 * @return the serie
	 */
	public XYSeries getSerie() {
		return serie;
	}

	/**
	 * @return the frameWasClosed
	 */
	public boolean isFrameWasClosed() {
		return frameWasClosed;
	}

	/**
	 * @param frameWasClosed the frameWasClosed to set
	 */
	public void setFrameWasClosed(boolean frameWasClosed) {
		this.frameWasClosed = frameWasClosed;
	}
	
	

	   

    
}
