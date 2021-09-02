package GeometryGL.Gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import GeometryGL.CrossSection;
import Utility.Coordinate;
import Utility.PolygonCoordinate;
import net.miginfocom.swing.MigLayout;

public class CrossSectionInput extends JFrame{

	private String line;

	private String title = "Cross-section Coordinates";
	private JLabel labelTitle= new JLabel(title);
	private JLabel label= new JLabel("Enter coordinates as Lat Lon");
	private JTextArea coordinateText= new JTextArea("64.0 0.0 \n"+"78.0 -10");
	private JButton btApply= new JButton("Apply");  

	private CrossSection crossSection=null;




	/**
	 * Constructor
	 * @param crossSection Transit line to operate on
	 */
	public CrossSectionInput(CrossSection crossSection, Point location) {

		//Set border layout for the frame
		setLayout(new BorderLayout());
		JPanel panel= new JPanel(new MigLayout());
		// 
		labelTitle.setForeground(Color.red);
		panel.add(labelTitle,"wrap");
		panel.add(label, "wrap");
		panel.add(coordinateText, "wmin 150 ,hmin 60, wrap");
		panel.add(btApply);
		//
		// Add the panel with the components to the centre
		add(panel, BorderLayout.CENTER);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//  Location on screen
		int dy= 190; 
		setSize(320, dy);
		setLocation(location.x, location.y);
		setVisible(true);
		setTitle(title );


		//  Insert coordinates from the transect line
		if (crossSection!=null) {
			Coordinate c1=  crossSection.getFrom();
			Coordinate c2=  crossSection.getTo();
				String sp  = String.format("%.2f %.2f \n%.2f  %.2f ", c1.getLat(), c1.getLon() ,c2.getLat(),c2.getLon()) ;
				coordinateText.setText(sp.replaceAll(",", "."));
			}


		// Action Listener for button
		btApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				readCoordinate(crossSection);
			}			
			
		});
		
		// KeyListener for the text field enabling VK_ENTER to trigger
		coordinateText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}			
			@Override
			public void keyReleased(KeyEvent arg0) {			
			}			
			@Override
			public void keyPressed(KeyEvent e) {
				int  key = e.getKeyCode();
				if(key == KeyEvent.VK_ENTER){
					readCoordinate(crossSection);
				}
				
			}
		});
			
		

	}
	
	public void readCoordinate(CrossSection cs) {
		String text=coordinateText.getText();
		PolygonCoordinate pco= new PolygonCoordinate(text);
		Coordinate co[] = pco.getCoordinates();
		//
		for (Coordinate coordinate : co) {
			System.out.println("#n: "+co.length);
			System.out.println(coordinate.toString());
		}
		//				
		if (cs!=null && co.length>1) {
			cs.setFrom(co[0]);
			cs.setTo(co[1]);	// receiveCoordinate(co[1]);
			cs.define();
		}
	}

	/**
	 * 
	 * @param line The line to parse
	 * @param n the line number
	 * 		//do not really need this if we use polygon coordinate object
	 */
	private Coordinate parseCoordinate(String line,int n) {

		Coordinate c= new Coordinate(0, 0);
		Scanner sc= new Scanner(line);
		String field="";
		//		while(sc.hasNext()){
		// First latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
		// Then Longitude
		field= sc.next();
		float  lon= Float.parseFloat(field);
		c.setLon(lon);
		sc.close();

		return c;
	}



	public static void main(String[] args) {
		CrossSectionInput t= new CrossSectionInput(null, new Point(500, 400));

	}

}
