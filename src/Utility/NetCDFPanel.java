package Utility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import GeometryGL.DistributionGL;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import distribution.Distribution;
//import javafx.scene.layout.Border;

public class NetCDFPanel extends JPanel{

	String fileName;

	JList<String> list;
	private DefaultListModel<String> listModel;
	private ArrayList<String> names;
	private int selectedIndex;
	//
	// JFrame
	JFrame frame=null;
	
	// 
	private Distribution distribution= null;
	private DistributionGL disGL= null;
//	private SceneGL sceneGL= null;
	private int listIndex =  0;

	private NetcdfFile ncfile=null;
//	ArrayList<DistributionGL> disList= null;

	protected boolean isGenerated= false;
	protected boolean isClosed= false;

	private Color bgColor= new Color(111,130,150,255);

	/**
	 * Constructor
	 * @param fileName
	 */
	public NetCDFPanel(String fileName, Distribution dis , int listIndex){
		this.distribution = dis ;
		this.listIndex  = listIndex ;
		this.fileName= fileName;
//		this.sceneGL = sceneGL;
		isClosed= false;
		isGenerated= false;

			try {
				ncfile = NetcdfFile.open(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// The netCDF is okay
			if (ncfile!=null) {
				// loop through all variables
				List<Variable> variables= ncfile.getVariables();
				names= new ArrayList<String>();
				String vn= "";
				for (Variable var : variables) {
//					if (var.getDimensions().size()>2 && var.getDimensionsString().contains("X")) {
						if (var.getDimensions().size()>2 ) {
						vn= var.getFullName();
						names.add(vn);
					}
				}
				// Setup GUI and add names of the variables
				setup(names);

			}

	}
	
	
	private void setup(ArrayList<String> names){

		// Use a list model to add the names of the variables of strings
		listModel = new DefaultListModel<String>();
		for (String vn : names) {
			listModel.addElement(vn);
		}

		list = new JList<String>(listModel);
		list.setBackground(Color.lightGray);
		list.setFont(new Font("Arial", Font.PLAIN, 16));
//		list.setLayout

		// Button to add distributions
		JButton btApply= new JButton("Add Distribution"); 
		btApply.setMaximumSize(new Dimension(80, 40));
		btApply.setFont(new Font("Arial", Font.PLAIN, 14));

		// Action for the apply button
		btApply.addActionListener(new ActionListener() {
			
			// 
			@Override
			/**
			 * Add another distribution to the list of distributions
			 */
			public void actionPerformed(ActionEvent e) {
				selectedIndex= Math.max(0, list.getSelectedIndex());
				String name = names.get(selectedIndex) ; 
				distribution= new  Distribution(name, ncfile);
				// Add distribution to list of distributions
				if (distribution!=null ) {
					disGL=new DistributionGL(distribution, listIndex);
//					
//					sceneGL.addDistribution(distribution); // Caused trouble
					isGenerated= true;
					System.out.println("added distribution ListSize: "+ listIndex);
				}
			}
		});

		//Layout of GUI
		//			Container contentPane = this.getContentPane();
		//			contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));

		//
		JPanel panel= new JPanel(new BorderLayout());
		panel.add(btApply, BorderLayout.SOUTH);
		panel.add(list,  BorderLayout.CENTER);
//		panel.setBackground(bgColor);
		// Label 
		String name= fileName.substring(0, 8)+"...."+fileName.substring(fileName.length()-12);
		JLabel label= new JLabel(name);
		label.setFont(new Font("Arial", Font.ITALIC, 16));
		panel.add( label ,BorderLayout.NORTH);
		//
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);

		//			contentPane.add(panel);
	}
		
		
	
	public void show(){

	  frame= new JFrame();
	  frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	  frame.setSize(300, 250);
	  
	  frame.setTitle("-  NetCDF: ");
	  String text= ""+fileName.substring(0, 8)+"...."+fileName.substring(fileName.length()-18);
	  frame.setVisible(true);
	  frame.add(this);
	  frame.setAlwaysOnTop(true);
	  // 
	  frame.addWindowListener(new WindowListener() {
		
		@Override
		public void windowOpened(WindowEvent e) {
		}
		
		@Override
		public void windowIconified(WindowEvent e) {
		}
		
		@Override
		public void windowDeiconified(WindowEvent e) {
		}
		
		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
		@Override
		public void windowClosing(WindowEvent e) {

		}
		
		@Override
		public void windowClosed(WindowEvent e) {
			isClosed= true;
			System.out.println(" WINDOW closing");
		}
		
		@Override
		public void windowActivated(WindowEvent e) {
		}
	});

	}

	
	public static void main(String[] args) {

//		  String path="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/sildyear1.nc";
		  String path="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/physics.nc";
		  
		  Distribution d= null;
		  NetCDFPanel p= new NetCDFPanel(path, d,0);
		  if (p.isGenerated) {
			if (p.getDistribution()!=null) {
				System.out.println("DISTRIBUTION name "+d.getFullName());
			}
			else  {
				System.out.println(" No distribution yet ! –! –! –!");
			}
		}
		  /*
		  try {
			p.ncfile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
//		  
		  p.show();
//		  JFrame frame= new JFrame();
//		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		  frame.setSize(300, 350);
//		  frame.setTitle("-  NetCDF: "+path.substring(0, 8)+"...."+path.substring(path.length()-15));
//		  frame.setVisible(true);
//		  frame.add(p);
//		  frame.
	
	}


	/**
	 * @return the isGenerated
	 */
	public boolean isGenerated() {
		return isGenerated;
	}


	/**
	 * @param isGenerated the isGenerated to set
	 */
	public void setGenerated(boolean isGenerated) {
		this.isGenerated = isGenerated;
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
	 * @return the isClosed
	 */
	public boolean isClosed() {
		return isClosed;
	}


	/**
	 * @param isClosed the isClosed to set
	 */
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}


	/**
	 * @return the disGL
	 */
	public DistributionGL getDisGL() {
		return disGL;
	}



}
