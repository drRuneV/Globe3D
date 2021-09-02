/**
 * 
 */
package Colors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//import interaction.Interact;
//import interaction.InteractGradient;
import net.miginfocom.swing.MigLayout;

/**
 * @author Rune Vabø
 *
 */
public class GradientPanel extends JPanel implements ActionListener {

	/**
	 */
	private static final long serialVersionUID = 1L;
	//
	private ColorGradient gradient = null;
	

	ArrayList<ColorGradient> listOfGradients= new ArrayList<>(); 
	
	private JLabel lmax = new JLabel("Max | Min:");
	private JLabel lopacityRange = new JLabel("Opacity Range:");
	private JLabel lopacityMinMax = new JLabel("Opacity Max | Min:");
	private JSpinner spinRangeMin;
	private JSpinner spinRangeMax;
	private JSpinner spinOpacityMin;
	private JSpinner spinOpacityMax;
	private JSpinner spinOpacityBelowMin;
	private JSpinner spinStepwise;
	private JSpinner spinDefault;

	
	private JButton btStepwise = new JButton("Make stepwise |•|");
	private JButton btSave = new JButton("Save");
	private JButton btOpen = new JButton("Open");

	private JTextField textFieldMax;
	private JTextField textFieldMin;
	private JCheckBox cboxUseMask = new JCheckBox("Opacity mask ");
	private JCheckBox cboxLogarithmic = new JCheckBox("non-linear");
	private JPanel panelImage; // = new JPanel(true);
	private JPanel panelValue = new JPanel(true);
	private JPanel panelBottom = new JPanel(true);
	private JLabel labelBottom = new JLabel("Keys: Copy Ctrl+c | Paste Ctrl+v | New Colour c | Insert i | Delete Del | ");
	private JLabel lInformation;
	private int interval = 8;
	// Indexes within the gradient according to the mouse
	private int nodeIndex=0;
	private int index=0;
//	private Interact interact =null;
	private InteractGradient interactGradient =null;
	// Range of mouse interaction in pixels for colour selection of nodes 
	private int mrange = 3;


	/**
	 * Constructs a new gradients panel
	 * 
	 * @param gradient	the gradient to be manipulated
	 */
	public GradientPanel(ColorGradient gradient) {
		// super();
		this.gradient = gradient;
		gradient.define();
		
		
		//
		createComponents();
		createDefaults();
		
		//
		btOpen.setMnemonic((KeyEvent.VK_O &  KeyEvent.CTRL_MASK));
		

		// Set action listeners
		spinRangeMax.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				readComponents();
			}
		});
		// Use the same listeners for most/all spinners
		spinRangeMin.addChangeListener(spinRangeMax.getChangeListeners()[0]);
		spinOpacityMax.addChangeListener(spinRangeMax.getChangeListeners()[0]);
		spinOpacityMin.addChangeListener(spinRangeMax.getChangeListeners()[0]);
		spinStepwise.addChangeListener(spinRangeMax.getChangeListeners()[0]);
		spinOpacityBelowMin.addChangeListener(spinRangeMax.getChangeListeners()[0]);
		
		textFieldMin.addActionListener(this);
		textFieldMax.addActionListener(this);
		cboxUseMask.addActionListener(this);
		cboxLogarithmic.addActionListener(this);
		
//		spinDefault.addChangeListener(spinRangeMax.getChangeListeners()[0]);

		//ChangeListener to spin default gradients
		spinDefault.addChangeListener(new ChangeListener() {	 //Label2
			
			@Override
			public void stateChanged(ChangeEvent e) {
				int nDefault=Integer.parseInt(spinDefault.getValue().toString());
				nDefault=  Math.min(nDefault, listOfGradients.size()-1);
				defineGradientFromDefault(nDefault);
				update();
				setFrameTitle();
				System.out.println("the name is:"+gradient.getName());
			}
		});
		
		// Set up action Listener for button stepwise
		btStepwise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ColorGradient cg = gradient.makeStepwise(interval);
				gradient.setColorIndex(cg.getColorIndex());
				gradient.setColors(cg.getColors());
				gradient.reDefine();
				update();
			}
		});
		// save Image Button
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveTheGradient();
			}
		});
		//open image Button
		btOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//
				File file= accessGradientFileDialogue(null, "open");
				openGradientInformation(file, gradient);
				gradient.define();
			}
		});
		
		// Layout the GUI according to MigLayout
		layoutGUI();
		setParameters();
	}

	
	private void defineGradientFromDefault(int n) {
		ColorGradient g= listOfGradients.get(n);
		//
		gradient.defineFromGradient(g);
		// This gives the result of actually changing the colour indexes in the list of ColourGradient!!!
		gradient.setColorIndex(g.getColorIndex());
		gradient.setColors(g.getColors());
		// 
		gradient.reDefine();

//		gradient = listOfGradients.get(n);
		setParameters();
		update();
	}
	
	/**
	 * Create all the components for this panel
	 * @param gradient
	 */
	private void createComponents() {
		//
		textFieldMax = new JTextField(String.format("%.2f", gradient.getMax()));
		textFieldMin = new JTextField(String.format("%.2f", gradient.getMin()));
		// lInformation
		lInformation = new JLabel();
//		
		// 
		cboxUseMask.setToolTipText("Opacity is linearly interpolated between Max and Min within Range");
		
		

		// Set limitations of spinners according to Gradient opacity
		SpinnerNumberModel model1 = new SpinnerNumberModel(gradient.getOpacityRange().x, 0, 255, 1);
		SpinnerNumberModel model2 = new SpinnerNumberModel(gradient.getOpacityRange().y, 0, 255, 1);
		SpinnerNumberModel model3 = new SpinnerNumberModel(gradient.getOpacityMinMax().x, 0, 255, 1);
		SpinnerNumberModel model4 = new SpinnerNumberModel(gradient.getOpacityMinMax().y, 0, 255, 1);
		SpinnerNumberModel model5 = new SpinnerNumberModel(8, 2, 32, 2);
		SpinnerNumberModel model6 = new SpinnerNumberModel(0, 0, 255, 5);
		SpinnerNumberModel model7 = new SpinnerNumberModel(0, 0, 15, 1);
		
		// Range Opacity
		spinRangeMin = new JSpinner(model1);
		spinRangeMin.setToolTipText("Low range of opacity values");
		spinRangeMax = new JSpinner(model2);
		spinRangeMax.setToolTipText("Top Range of opacity values");
		// Opacity max/min
		spinOpacityMin = new JSpinner(model3);
		spinOpacityMin.setToolTipText(" Minimum opacity value from range min");
		spinOpacityMax = new JSpinner(model4);
		spinOpacityMax.setToolTipText(" Maximum opacity value - "
				+ "	What is the opacity of the colour at position Top Range "+gradient.getOpacityRange().y);
		
		
		//
		spinStepwise = new JSpinner(model5);
		spinStepwise.setToolTipText("Intervals");
		//
		spinDefault = new JSpinner(model7);
		spinDefault.setToolTipText("Default gradients from files with names \"default0.txt default1.txt ...\"");
		//opacity below minimum
		spinOpacityBelowMin = new JSpinner(model6);
		spinOpacityBelowMin.setToolTipText("Opacity fixed below low range ");
		spinOpacityBelowMin.setBackground(Color.LIGHT_GRAY);
	}

	//	
	//	if(Files.exists(Paths.get(filePathString))) { 
	//	     do something
	//	}
		
		
		
		/**
		 * Creates the default gradients from file
		 */
		private void createDefaults(){ //Label1 // Tiger
	
			// All the files in the world list
			String filename= "./res/Gradients/default";
	
			//  Add the current gradient as the first one
			ColorGradient g0= new ColorGradient(gradient);
			listOfGradients.add(g0);
			// 
			for (int i = 0; i < 20; i++) {
				String path=filename+ String.format("%d", i)+".txt";
				File f=new File(path);
				// Add another gradient if the file exists 
				if (f.exists()) {
	//			System.out.println(f.toString()+"  do exist "+ path);
				ColorGradient g= new ColorGradient(1,0);
				openGradientInformation(f, g);
				listOfGradients.add(g);
				}
				//
			}
	
			//		gradient = g ;
			update();
			
		}


	/**
	 * 
	 */
	private void layoutGUI() {
		setLayout(new BorderLayout());

		setupImagePanel();
		this.add(panelImage, BorderLayout.WEST);
		this.add(panelValue, BorderLayout.CENTER);
		this.add(panelBottom,BorderLayout.SOUTH);
		// 
		panelBottom.add(labelBottom);
		labelBottom.setForeground(Color.red);

		//
		panelValue.setLayout(new MigLayout());// GridLayout(2, 0, 2,2));
		JLabel lDefault=new JLabel("Default Gradient: ");
		//addcomponents
		panelValue.add(lDefault);
		panelValue.add(spinDefault,"wrap");
		panelValue.add(lmax);
		panelValue.add(textFieldMax);
		panelValue.add(textFieldMin, "wrap , grow 2");
		panelValue.add(cboxLogarithmic,"wrap");
		panelValue.add(cboxUseMask, "wrap");
		panelValue.add(lopacityRange);
		panelValue.add(spinRangeMax);
		panelValue.add(spinRangeMin, "wrap");
		panelValue.add(lopacityMinMax);
		panelValue.add(spinOpacityMax);
		panelValue.add(spinOpacityMin, "wrap");
		panelValue.add(spinOpacityBelowMin, "wrap");
		// Button
		panelValue.add(btStepwise, "span 2");
		panelValue.add(spinStepwise, "span 2 ,wrap");
		panelValue.add(btSave, "grow 1");
		panelValue.add(btOpen, "span 2");

		// set up the font for all the components
		Font font1 = new Font("Times", Font.BOLD, 14);
		for (Component c : panelValue.getComponents()) {
			c.setFont(font1);
		}
		labelBottom.setFont(new Font("Times", Font.PLAIN, 12)); 
		

		update();
	}

	/**
	 * Sets up the panel where the image  is.
	 */
	private void setupImagePanel(){

		//
		panelImage= new JPanel(){

			@ Override
			public void paintComponent(Graphics g) {
				// Clear graphics of the panel
				Graphics2D gPanel = (Graphics2D) g;
				gPanel.setBackground(Color.lightGray);
				gPanel.clearRect(0, 0, this.getWidth(), this.getHeight());
				gPanel.drawImage(gradient.getImage(),5, 0,null, null);
				

				int h=gradient.getImage().getHeight();
				int w=gradient.getImage().getWidth();

//				int ix = interactGradient.getSelectedIndex();
				int ix  = index ;
				// Indicate all colour indexes graphical by a small circle
					Graphics2D gr =  (Graphics2D) gradient.getImage().createGraphics();
					for (Integer keyIndex : gradient.getColorIndex().keySet()) {
						ColorInt color=  gradient.getColorIndex().get(keyIndex);
						Color c= color.makeColor();
						gr.setColor(c);
						//  The horizontal size of the node indicate if  mouse is close by
						int xSize= ( Math.abs(ix-keyIndex)<mrange   ) ?  20:  5;
						gr.fillOval(w-25, h-keyIndex-gradient.getOffy()-1, xSize , 5+xSize/10);//);, arg1, arg2, arg3);
//						System.out.println("key:"+key);
					}
//				}
			}
			
		};
		
		// Interaction for keyboard and mouse
		setupInteract();
		//• MouseWheelListeners
//		panelImage.addMouseWheelListener(interactGradient);
		
		//•Mouse Motion
		panelImage.addMouseMotionListener(interactGradient);
		panelImage.addMouseMotionListener(new MouseAdapter() {

			
			@Override
			/**
			 * Mouse is moving across the image panel
			 */
			public void mouseMoved(MouseEvent me) {
				boolean controlDown= me.isControlDown();
				int x= me.getX();
				boolean pickNode=(x> gradient.getImage().getWidth()/2);
				//				panelImage.setRequestFocusEnabled(true); // does not work!
				// 
				frameInFocus();
				
				panelImage.requestFocus();
				
				// The index of the gradient where the mouse is
				index= interactGradient.indexAtMouse();
				// Find the nearby nodeIndex
				if (!interactGradient.ismDown() || controlDown ) { // crocodile
					nodeIndex=  gradient.nearbyNodeIndexAround(index, mrange*2 );
					interactGradient.setSelectedIndex(nodeIndex);
				}

				
				
				// Display information about index and values depending on x-position
				if (index>-1 && index<256) {
					String str=String.format("# %d v=%.1f", index, gradient.findValueFromIndex(index)); 
							
					str+= (pickNode) ? String.format(" N: %d", nodeIndex) : "" ;
							//String.format("# %d Node: %d", index,nodeIndex):str;
					lInformation.setText(str);
					String tooltip= (pickNode) ? "Click to change the current NodeColour or drag mouse": 
										"Click to insert a new colour, or use keys… or: Space, 0-9 for predefined gradients";
					panelImage.setToolTipText(tooltip);
				}

				gradient.reDefine();
				repaint();
				setParameters();
			}

		});
		
		//Mouse Click
		panelImage.addMouseListener(interactGradient);

		// KeyListeners
//		panelImage.addKeyListener(interact);
		panelImage.addKeyListener(interactGradient);
		panelImage.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				repaint();
				setParameters();
			}
		});
		
		panelImage.setPreferredSize(new Dimension(gradient.getImage().getWidth()+10,this.getHeight() ));
		panelImage.setFocusable(true);
		panelImage.setLayout(new BorderLayout());	
		panelImage.add(lInformation, BorderLayout.SOUTH);

	}


	/**
	 * @return
	 */
	private void setupInteract() {
//		interact=  new Interact(null,this);
//		interact.setGradient(gradient);
//		interact.setActive(true);
		interactGradient= new InteractGradient(null,this);
		interactGradient.setGradient(gradient);
//		interactGradient.setActive(true);
		// Remember the last gradient as well
		interactGradient.setLastGradient( new ColorGradient(gradient));
		
	}

	/**
	 * 
	 */
	private void frameInFocus() {
		JFrame frame = (JFrame) this.getTopLevelAncestor();
		frame.toFront();
		frame.requestFocusInWindow();
	}


	/**
	 * Reads all the data from GUI and set these into the gradient object
	 */
	private void readComponents() {
		float max = Float.parseFloat(textFieldMax.getText().replace(',', '.'));
		float min = Float.parseFloat(textFieldMin.getText().replace(',', '.'));
		int opacityRangeMin = Integer.parseInt(spinRangeMin.getValue().toString());
		int opacityRangeMax = Integer.parseInt(spinRangeMax.getValue().toString());
		int opacityMax = Integer.parseInt(spinOpacityMax.getValue().toString());
		int opacityMin = Integer.parseInt(spinOpacityMin.getValue().toString());
		int opacityBelowMin = Integer.parseInt(spinOpacityBelowMin.getValue().toString());
		
		interval = Integer.parseInt(spinStepwise.getValue().toString());
		//
		String text= String.format("Low Range where v=%.2f", gradient.findValueFromIndex(gradient.getOpacityRange().x));
		spinRangeMin.setToolTipText(text);
		// 
		spinOpacityMax.setToolTipText(" Maximum opacity value - "
				+ "	What is the opacity of the colour at position Top Range "+gradient.getOpacityRange().y);
		
		
		//
		gradient.setMax(max);
		gradient.setMin(min);
		gradient.setOpacityRange(new Point(opacityRangeMin, opacityRangeMax));
		gradient.setOpacityMinMax(new Point(opacityMin, opacityMax));
		gradient.opacityBelowMin = opacityBelowMin;
		boolean mask= cboxUseMask.isSelected();
		gradient.setUseOpacityMask(mask);
		gradient.setUseLogarithmic(cboxLogarithmic.isSelected());
		// visible
		spinRangeMax.setEnabled(mask);
		spinRangeMin.setEnabled(mask);
		spinOpacityMax.setEnabled(mask);
		spinOpacityMin.setEnabled(mask);
		spinOpacityBelowMin.setEnabled(mask);

		gradient.reDefine();

		repaint();
	}

	/**
	 * Sets the parameters from the gradient into the GUI components
	 */
	private void setParameters() {
		// Set the values from the gradient
		textFieldMax.setText(String.format("%.2f", gradient.getMax()));
		textFieldMin.setText(String.format("%.2f", gradient.getMin()));
		cboxUseMask.setSelected(gradient.isUseOpacityMask());
		cboxLogarithmic.setSelected(gradient.isUseLogarithmic());
		spinRangeMin.getModel().setValue(gradient.getOpacityRange().x);
		spinRangeMax.getModel().setValue(gradient.getOpacityRange().y);
		spinOpacityMax.getModel().setValue(gradient.opacityMinMax.y);
		spinOpacityMin.getModel().setValue(gradient.opacityMinMax.x);
		spinOpacityBelowMin.getModel().setValue(gradient.opacityBelowMin);

		//String text= spinRangeMin.getToolTipText()+gradient.;
		
		//
		
		setFrameTitle();
	}


	/**
	 * 
	 */
	private void setFrameTitle() {
		JFrame frame = (JFrame) this.getTopLevelAncestor();
		if (frame != null) {
			frame.setTitle(gradient.getName());
			// System.out.println(gradient.getName());
		}
	}

	/**
	 * 
	 */
	private void update() {
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		// setParameters
		panelImage.repaint();

	}
	//

	/**
	 * Shows the gradient panel in a swing window
	 */
	public void show(String  title) {
		{
			JFrame frame = new JFrame(gradient.getName());
			frame.setSize(460, 380);
			frame.setMinimumSize(new Dimension(450, 350));
			frame.add(this);
			frame.setLocation(600, 300);
			frame.setTitle(title);//gradient.getName());
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		}
	}
	
	
	/**
	 * 
	 */
	public void saveTheGradient(){
	    StringBuilder sb = new StringBuilder();
	    String sep= System.lineSeparator();

	    //
	    String line;
	    //Name
	  	  //line= gradient.getName();
	    //sb.append("Name:"+line+sep);
	    // Max
	    String format=(gradient.getMax()< 1.0f) ? "%.8f": "%.2f";
	    line= String.format("Max: "+format , gradient.getMax());
	    sb.append(line+sep);
	    // Min
	    line= String.format("Min: "+format , gradient.getMin());
	    sb.append(line+sep);        // sb.append(System.lineSeparator());

	    //
	    line= String.format("OpacityRangeTop: %d"  , gradient.getOpacityRange().y);
	    sb.append(line+sep);
	    //
	    line= String.format("OpacityRangeLow: %d"  , gradient.getOpacityRange().x);
	    sb.append(line+sep);
	    //
	    line= String.format("OpacityMin: %d"  , gradient.getOpacityMinMax().x);
	    sb.append(line+sep);
	    //
	    line= String.format("OpacityMax: %d"  , gradient.getOpacityMinMax().y);
	    sb.append(line+sep);
	    //
	    line= String.format("OpacityBelow: %d"  , gradient.getOpacityBelowMin());
	    sb.append(line+sep);
	    //
	    line= String.format("OpacityMask: %b"  , gradient.isUseOpacityMask());
	    sb.append(line+sep);
	    //
	    line= String.format("useLogarithmic: %b"  , gradient.isUseLogarithmic());
	    sb.append(line+sep);

	    // Write all the colour nodes
	    sb.append("Colours:"+sep);
		for (Integer key : gradient.getColorIndex().keySet()) {
			ColorInt color=gradient.getColorIndex().get(key);
			int c= color.toInt();
			line= String.format("%d ; %d", key, c) ;
			sb.append(line+sep);
		}
	    

	    // Write gradient information to a file
		File file =  accessGradientFileDialogue(null,"save");
	    if (file!=null) {
	    	 //Name
	    	String name= file.getName();
	    	int endIndex= name.indexOf(".txt");
			name= name.substring(0, endIndex);
	    			sb.append("Name:"+name );
		  

	    	try {
	    		FileWriter fileWriter = new FileWriter(file);
	    		BufferedWriter bw = new BufferedWriter(fileWriter);
	    		bw.write(sb.toString());

	    		System.out.println("Information writing the information"+sb.toString());
	    		//
	    		bw.flush();
	    		bw.close();
				// The image file
				saveImage(file.getAbsolutePath());
				// Information of the gradient .txt

	    		//			fileWriter.flush();
	    		fileWriter.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}

	
	private void saveImage(String fileName) {
		File file = new File(fileName.replace(".txt", ".png")); 		// GraphicsUtil.saveImagePath(getComponent(0));
		try {
			if (file != null) {
				ImageIO.write(gradient.drawGradientOnly(), "png", file);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * Open a gradient file Dialogue either for saving or opening.
	*/
	public File accessGradientFileDialogue(String path,String what) {
		boolean isSave=  (what.contains("save"));
		File file =  null;
		String initialDirectory="./res/Gradients/"; //relative path to current directory
		JFileChooser fcd = new JFileChooser(initialDirectory);
		String inf = (isSave) ? " Save the gradient ": "Open a gradient .txt file";
		
		
		fcd.setDialogTitle(inf);
		fcd.setFont(new Font("Times", Font.PLAIN, 14));

		//Saving
		if (isSave) {
			System.out.println("Current directory: "+fcd.getCurrentDirectory().getAbsolutePath());
			fcd.showSaveDialog(this);
		}
		//Opening
		else fcd.showOpenDialog(this);
		
		// Return the file
		if (fcd.getSelectedFile()!=null) {
			String name= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
			if (!name.contains(".txt")) {
				name+=".txt";
			}
			file =  new File(name );//
		}

			return file;
		}


	/**
	 * Opens a file and reads information about a gradient
	 */
	public void openGradientInformation(File file, ColorGradient cg){
		StringBuilder sb = new StringBuilder();
		String line = null;

		//
		if(file!=null) try {
			Scanner sc = new Scanner(file);
			int n=0;
			// Clear all nodes
			cg.clear(Color.black);
			//
			while(sc.hasNextLine()){
				line= sc.nextLine();
				sb.append(line);
				//	        	System.out.println(n+" : "+line);
				n++;
				handleLine(line,cg);
			}
			sc.close();


		} catch (IOException e) {
			e.printStackTrace();
		}


	}

//	
//	if(Files.exists(Paths.get(filePathString))) { 
//	     do something
//	}
	
	
	
	/**
	 * Handles the reading of one line of text
	 * @param line
	 */
	private void handleLine(String line, ColorGradient cg) {
		int ix= line.indexOf(":");
		
		String s= (ix< line.length())  ?  line.substring(ix+1).replace(",", ".") : "";
		int value=0 ;

		s=s.trim();
		
		if (line.contains("Name")  ) {
			String name= s;
			cg.setName(name);			
		}
		
		else if (line.contains("Max") && !line.contains("Opacity") ) {
			float  f= Float.parseFloat(s);
			cg.setMax(f);
		} 
		else if(line.contains("Min") && !line.contains("Opacity") ){
			float  f= Float.parseFloat(s);
			cg.setMin(f);
		}
		// Opacity
		else if (line.contains("OpacityRangeTop")){
			value= Integer.parseInt(s);
			//Int32.TryParse(s, 0);
			 
			cg.setOpacityRange( new Point( cg.getOpacityRange().x ,value));
		}
		else if (line.contains("OpacityRangeLow")){
			value= Integer.parseInt(s);
			cg.setOpacityRange( new Point(value, cg.getOpacityRange().y ));
		}
		else if (line.contains("OpacityMin")){
			value= Integer.parseInt(s);
			cg.setOpacityMinMax(new Point( value, cg.getOpacityMinMax().y));
		}
		else if (line.contains("OpacityMax")){
			value= Integer.parseInt(s);
			cg.setOpacityMinMax(new Point( cg.getOpacityMinMax().x,value));
		}
		else if (line.contains("OpacityBelow")){
			value= Integer.parseInt(s);
			cg.setOpacityBelowMin(value);
		}
		else if (line.contains("OpacityMask")){
			boolean isTrue= (s.contains("true")) ? true :false ;
			cg.setUseOpacityMask(isTrue);
		}
		else if (line.contains("useLogarithmic")){
			boolean isTrue= (s.contains("true")) ? true :false ;
			cg.setUseLogarithmic(isTrue);
		}
		// Read one colour node 
		else if (line.contains(";")) {
			ix= line.indexOf(";")+0;
			s= line.substring(0,ix-1);
			int keyIndex= Integer.parseInt(s);
			s=  line.substring(ix+1).trim() ;
			
			int color= Integer.parseInt(s) ;
			ColorInt c=new ColorInt(color);
			cg.insert(c, keyIndex);
//			System.out.println("Inserting colour: at"+keyIndex+" "+c);
		}
		
		//
		setParameters();

	}


		/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("starting gradient panel");

		ColorGradient gr = new ColorGradient(100, 8);
		GraphicsUtil.produceColorGradientStandard(gr);// GradientDiscreetFrom(gr,
														// ColorSet("orange").colors);
		GradientPanel p = new GradientPanel(gr);
		p.show("Gradient");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		readComponents();
	}


	/**
	 * @return the spinDefault
	 */
	public JSpinner getSpinDefault() {
		return spinDefault;
	}


	/**
	 * @param spinDefault the spinDefault to set
	 */
	public void setSpinDefault(JSpinner spinDefault) {
		this.spinDefault = spinDefault;
	}


	/**
	 * @return the listOfGradients
	 */
	public ArrayList<ColorGradient> getListOfGradients() {
		return listOfGradients;
	}


	/**
	 * @return the panelImage
	 */
	public JPanel getPanelImage() {
		return panelImage;
	}

}
