package distribution.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.DoubleSummaryStatistics;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import Utility.LineGraph;



/**
 * 
 * @author a1500
 *
 */
public class StatPresenter extends JFrame{
	
	private StatDistr statDistribution= null;

	//
//	private JFrame frame =null;
	private JPanel panel;
	private JTextArea areaDay=null;
	private JTextArea areaTotal=null;
	private JPanel panelWithImage=null;
	private boolean isPresenting=false;
	private Histogram histogram= null;
	//
	BufferedImage image =null;
	private int graphNumber=0;

	//

	private LineGraph histogramGraph;
	private LineGraph graph;

	private Map<String, double[]> map;

	private int currentCount=0;

	private String distributionName;
	
	
	
	
	/**
	 * Constructor
	 * @param statDistribution
	 */
	public StatPresenter(StatDistr statDistribution) {
		this.statDistribution= statDistribution;
		if (statDistribution.getDistribution().isHasTime()) {
			map= statDistribution.makeMap();
			setPresenting(true);
		}
		distributionName= statDistribution.getDistribution().getFullName();
		setup(300);
		setAlwaysOnTop(true);
		setVisible(true);
	}
	
	
	
	
	/**
	 * Sets of the panel used inside the statistics window
	 * where areas of values and  images of histograms are shown.
	 * @param dx positioning of the JFrame
	 */
	private void setup(int dx) {
		// main panel
		panel= new JPanel();
		panel.setLayout(new BorderLayout());

		add(panel);
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocation(dx, 300);
		setSize(500, 250);

		// Listen to the frame
		createWindowsListener();
		// MouseListener
		createMouseListener();
		// Mouse click
		createMouseAdapter();

		// The text areas inside the panel where values are displayed
		areaTotal=displayValues(-1,areaTotal);
		panel.add(areaTotal, BorderLayout.WEST);
		// For each day
		if (statDistribution.getDistribution().isHasTime()) {
			areaDay=displayValues(0, areaDay);
			areaDay.setBackground(Color.lightGray);
			areaDay.setForeground(Color.blue);

			panel.add( createPanelWithImage(),BorderLayout.EAST);
			panel.add(areaDay,BorderLayout.CENTER);
			//			panel.setLayout(new BorderLayout());
			JLabel labelBottom= new JLabel("Click for separate Histogram graph … Double-click for statistics plot");
			panel.add(labelBottom,BorderLayout.SOUTH);
		}

	}




	/**
	 * Presents the statistics on a Jframe
	 * @param dx X location on screen
	 */
	public void update(int dx, int time){
		
		// Continuous updating
		if (isPresenting) {
			displayValues(time, areaDay);
			if (time!=-1) {
				histogram=statDistribution.histograms[time];
				updatePanelHistogram(time);
				// Update the separate histogram window if visible
				if (histogramGraph!=null) {
					if (histogramGraph.getFrame().isVisible()) {
						showHistogramGraph();
					}
				}
			}
			panelWithImage.repaint();
			panel.repaint();
			repaint();
		}

	}



	/**
	 * Creates a mouse adapter for responding to mouse clicks. 
	 */
	private void createMouseAdapter(){
		
		// MouseListener
		addMouseListener(new MouseAdapter() {

			@Override
			/**
			 * Response to click or double-click
			 */
			public void mouseClicked(MouseEvent me) {
				//				float[] floatValues = Floats.toArray(Doubles.asList(values));
				// Double-click to show separate window with maximum, minimum, averages et cetera
				if (me.getClickCount()>1) {
					showStatisticsGraph();
				}
				else {
					showHistogramGraph();
				}
			}				

		});
	}
	
	/**
	 * Create and add a MouseListener to the frame
	 */
	private void createMouseListener(){
		
		// Create a new listener
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				repaint();
				panelWithImage.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				showHistogramGraph();
			}
			
		});
	}
	

	/**
	 * Shows the histogram in a separate graph
	 */
	private void showHistogramGraph() {
		String title= "Histogram #"+ histogram.count +" max:"+histogram.max;
	
		Histogram h= statDistribution.histograms[currentCount];
		title=distributionName+ " H #"+ h.count +" max:"+h.max;
		float[] values=LineGraph.convertToFloat(h.columns);
		String frameTitle = "Histogram for: "+statDistribution.getDistribution().getFullName() + " "+
				statDistribution.getDistribution().getUnit();
		Point pos= this.getLocationOnScreen();
		pos.x+=this.getWidth();
	
		//Create a new graph
		if (histogramGraph==null) {
			histogramGraph= new LineGraph(values, title, "Interval", "number",true);
			histogramGraph.show(pos,frameTitle);
			histogramGraph.defineTheAxis(400);
		}
		else{
			histogramGraph.update(values, title, 0);
			if (histogramGraph.getFrame().getTitle().isEmpty()) {
				histogramGraph.setFrameTitleAndLocation(pos, frameTitle);
			}
		}
	
		graphNumber++;
		//every week
		//					for (int i = 1; i < statDistribution.histograms.length; i+=7) {
		//						title= i+" H #"+ h.count +" max:"+h.max;
		//						Histogram hh= statDistribution.histograms[i];
		//						histogramGraph.addSerie(LineGraph.convertFromFloat(hh.columns), title);
		//					}
	}




	/**
	 * @param pos
	 */
	private void showStatisticsGraph() {
		Point pos= this.getLocationOnScreen();
		pos.y+=this.getHeight();
		
		
		String[] names= new String[]{"Min","Avg", "Median", "Std"};
		float[]  max= Floats.toArray(Doubles.asList(map.get("Max")));
		String title = "Statistics for "+ statDistribution.getDistribution().getFullName() ;
		graph= new LineGraph(max, title,"Max", "Day", statDistribution.getDistribution().getUnit());
	
		//				graph.addSerie(Floats.toArray(Doubles.asList(map.get("Min") )), "Min");
		//				graph.addSerie(Floats.toArray(Doubles.asList(map.get("Average") )), "Average");
		for (String name : names) {
			graph.addSerie(Floats.toArray(Doubles.asList(map.get(name) )),name);
		}
		graph.show(pos, "Basic statistics");
	}




	/**
	 * 
	 */
	private void createWindowsListener() {
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {					
			}			
			
			public void windowIconified(WindowEvent arg0) {					
			}			
			@Override
			public void windowDeiconified(WindowEvent arg0) {					
			}			
			@Override
			public void windowDeactivated(WindowEvent arg0) {					
//				setPresenting(false);
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				setPresenting(false);
				statDistribution.setPresenting(false);
				dispose();//				=null;
				System.out.println("closing the statistics window");
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
			}			
			@Override
			public void windowActivated(WindowEvent arg0) {
				statDistribution.setPresenting(true);
				setPresenting(true);
			}
		});
	}

			
		
	/**
	 * Displays values in a text area
	 * @param time the current time step
	 * @param area the text area to display on
	 * @return the text area filled out
	 */
	private JTextArea displayValues(int time, JTextArea area ) {
		
		StatDistr statistics[] = statDistribution.getStatistics();		
		StatDistr stat=  (time<0) ?  statDistribution: statistics[time];
		
		DoubleSummaryStatistics dss= stat.ss;//(time<0) ? statDistribution.ss : statistics[time].ss ;
		String title= (time<0) ? "Total":  stat.getDistribution().getDateStrings()[time];
		String format1= (dss.getMax()> 10000  ) ?  "%.1e"  :"%.2f";
		
		// Create or clean the area
		if (area== null) {
		area= new JTextArea();
		area.setBackground(Color.lightGray.brighter() );
		area.setFont(new Font("Serif", Font.ITALIC, 15));
		area.setEditable(false);
		area.setPreferredSize(new Dimension(150, 120));
//		System.out.println("Area created! "+area);
		}
		else{
			area.setText("");
		}

		area.append(title);
		area.append(String.format("\nMax   =  "+ format1+"\n", dss.getMax()));
		area.append(String.format("Min   = %.4f \n", dss.getMin()));
		area.append(String.format("Average= "+format1+"\n", dss.getAverage()));
		area.append(String.format("Median = "+format1+"\n", stat.getMedian()));
		area.append(String.format("Std   = "+format1+"\n",stat.getStd()));
		area.append(String.format("Count:%d \n", dss.getCount()));
		String infoZeros= (statDistribution.acceptZeros) ? "Accepting zeros": "No zeros accepted";
		area.append(String.format(infoZeros+"\n"));
		
		return area;
	}

	
	
	
	/**
	 * Draws the histogram  for the current time step on an image and repaint 
	 * the panel whether image is located
	 * @param t the current time step
	 */
	private void updatePanelHistogram(int t){
		image = histogram.drawHistogram();//statDistribution.histograms[t].drawHistogram() ;
		panelWithImage.repaint();
		currentCount=t;
	}
	

	
	
	
	/**
	 * Creates the panel where the image with the histogram will be showed
	 * @return
	 */
	private JPanel createPanelWithImage(){

		// Create a panel where image can be put
		if (panelWithImage==null) {
			panelWithImage= new JPanel(){

				@ Override
				/**
				 * Repaint method for panel
				 */
				public void paintComponent(Graphics g) {
					// Clear graphics of the panel
					Graphics2D gPanel = (Graphics2D) g;
					gPanel.setBackground(Color.black);
					if(image!=null){
						gPanel.clearRect(0, 0, image.getWidth(), image.getHeight());
						gPanel.drawImage(image,0, 0,null,null);
						int  dy= image.getHeight()+5;
						gPanel.drawImage(statDistribution.getTotalHistogram().drawHistogram(),0, dy,null,null);
//						gPanel.drawImage(statDistribution.getDistribution());
					}
				}

			};

		}
		panelWithImage.setPreferredSize(new Dimension(170, 120));
//		panelWithImage.setToolTipText(" Click for separate Histogram graph … Double-click for statistics plot");


		return panelWithImage;
	}




	/**
	 * @return the isPresenting
	 */
	public boolean isPresenting() {
		return isPresenting;
	}




	/**
	 * @param isPresenting the isPresenting to set
	 */
	public void setPresenting(boolean isPresenting) {
		this.isPresenting = isPresenting;
	}


	
	
	
	
}
