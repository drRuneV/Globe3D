package basicLwjgl;

import static org.lwjgl.opengl.GL11.GL_AMBIENT_AND_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_MATERIAL;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glColorMaterial;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glMaterialf;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;

import Basic.Vector2D;
import Colors.ColorGradient;
import Colors.ColorInt;
import Colors.GradientPanel;
import GeometryGL.CoordinateMesh;
import GeometryGL.CrossSection;
import GeometryGL.DistListHandler;
import GeometryGL.DistributionGL;
import GeometryGL.GeoGrid;
import GeometryGL.Globe;
import GeometryGL.SelectBox;
import GeometryGL.TopographyGL;
import Utility.Coordinate;
import distribution.Agent;
import distribution.CurrentField;
import distribution.Distribution;
import distribution.Topography;
import ucar.nc2.NetcdfFile;

public class SceneGL {
	
	public CoordinateMesh mesh= null;
	public CurrentField cfield= null;
	public CrossSection crossSection= null;
	public Globe globe= null;
	public TopographyGL topoGL= null;
	public Agent agent= null;
	private ArrayList<Agent> agentList= new ArrayList<>();
	// Distribution list
	public DistListHandler dlistHandler= null;
	public DistributionGL selectedDistGL= null;
	// Index of selected distribution
	public int index= 0;
	// Only used during adding of distributions to avoid crash
//	boolean showDistribution= true;
	private int count=0;
	private int step=2;
	// Flags
	private boolean useMesh=false  ;
	private boolean useCrossSection=false ;
	private boolean useVisibleDist= true;
	private boolean useDistribution= true;
	private boolean useDistributionInfo= true;

	// 
	SelectBox box= new SelectBox(90, 90, 90, 90); 
	private SelectBox disTextBox= new SelectBox(90, 90, 90, 90);
	private SelectBox helpTextBox= new SelectBox(30,30, 350, 300);
	private boolean mouseIsOccupied= false  ;	
	/**
	 * Constructor
	 */
	public SceneGL() {
		
		dlistHandler= new DistListHandler();

		mesh= new CoordinateMesh(5, 5, new Coordinate(90,-180),new Coordinate(-90, 180));
		
		disTextBox= new SelectBox(Display.getWidth()/2+ 100, Display.getHeight()/2-200, 300, 40) ;
		disTextBox.setFillColour(new ColorInt(0.4f, 0.5f, 0.5f, 0.3f));
		
		
//		defineCrossSection();
	}

	
	public void defineCurrent() {
		String path="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		String file = "physics.nc" ;
		NetcdfFile ncfile = Distribution.testNetCDFFile(path,file);
		// 
		cfield= new CurrentField(ncfile);
		// Agents
		agent= new Agent(new Coordinate(66, -5));
		int n= 100 ;
		for(int ix=0; ix< n; ix++) {
			Agent a= new Agent(agent,0.1f);
			agentList.add(a);			 
		}
		 
	}

	/**
	 * Default cross-section
	 */
	public void defineCrossSection() {
		// CrossSection
		Coordinate c1 = new Coordinate(75, -5) ;
		Coordinate c2 = new Coordinate(60, 5) ;
		crossSection= new  CrossSection(c1, c2, 100);
		crossSection.getGeoLine().setAlpha(0.8f);
		crossSection.getGeoLine().setColor(java.awt.Color.black);
		crossSection.getGeoLine().setLineWith(2.0f);
		
		
	}
	
	/**
	 * Creates the Globe with texture
	 * @param texture the texture to use
	 */
	public void createGlobe(Texture texture){
		// Globe
		globe = OpenGLTesting.createsGlobe(texture);
		globe.grabFullTopography(new Coordinate(90, -180), new Coordinate(40, 10));		
	}
	

/**
 * Creates the Topography 
 * @param image BufferedImage representing the texture of the globe
 */
public void defineTopographyGL(BufferedImage image) {
	long lastTime= System.nanoTime() ;
	//  Forces specific number of grid cells
	float  xlon= (1800*Topography.ETPO2); 
	float  ylat=  (600*Topography.ETPO2);   
	int levels=2;
	
	Coordinate c1=  new Coordinate(80, -25);
	Coordinate c2=  new Coordinate(c1.getLat()-ylat , c1.getLon()+xlon );//50, 20);
	Topography topo =   OpenGLTesting.createTopography(c1,c2);
	
	topoGL=  new TopographyGL(topo, topo.getWidth(),topo.getHeight(),globe.getRadius(),levels);
	
	// Change the pixels of the texture image to transparent
	globe.changePixelsTransparent(topo.getUpper().getLat(),topo.getUpper().getLon(),
			topo.getLower().getLat(),topo.getLower().getLon(),image);
	//
	

	// 
	long timeTookms= (long) ((System.nanoTime()- lastTime)*0.000001);
	System.out.println("Time to create topography(ms): "+timeTookms);
	
}

//
///**
// * Adds a distribution to the list of distributions
// * @param distribution  the distribution to add
// */
//public void addDistribution(Distribution distribution) {
//	if (distribution!=null) {
//		DistributionGL dGL= new DistributionGL(distribution, disList.size());
//		disList.add(dGL);
//		showDistribution=false;
//		// Sets all distributions to time step 0
//		count= 0;
//		synchroniseDistributions(true);
//	}
//}


/**
 * Set All distributions to the time counter (the day) of the first
 * @param fromStart if all distributions are gonna be reset to time step 0
 */
//public void synchroniseDistributions(boolean fromStart){
//	int t= (fromStart) ? 0 : disList.get(0).getDistribution().getCount();
//	for (DistributionGL d : disList) {
//		int time = Math.min(d.getDistribution().getTime()-1, t);
//		d.getDistribution().setCount(time);
//	}
//}
//


/**
 * Changes to the next/previous distribution in the list.
 * Index keeps track of which distribution is selected.
 * @param di  step size
 * @return the selected distributionGL
 */
//public DistributionGL changeDistribution(int di) {
//	int max = disList.size()-1 ;
//	index+= di;
//	index= (index> max) ? 0 : (index<0) ? max:  index;
//	DistributionGL dGL=  (disList.isEmpty())? null : disList.get(index);
//	
//	System.out.println("Distribution : "+ dGL.getDistribution().getFullName());
//	return dGL;
//}

/**
 * Sets all distribution visible or invisible
 * @param visible visible or invisible
 */
//public void visibilityOfDistributions(boolean visible) {
//	for (DistributionGL distributionGL : disList) {
//		distributionGL.setVisible(visible);
//	}
//}

/**
 * Removes a distributionGL from the list
 * @param selected the distributionGL to remove
 * @return the first distribution in the list or null 
 */
//public DistributionGL removeDistribution(DistributionGL selected){
//	DistributionGL d= null ;
//	disList.remove(selected);
//	if (!disList.isEmpty()) {
//		d= disList.get(0);		
//	}
//	return d;
//}


/**
 * Displays the distribution name and date using slick 2D	
 * @param slickGraphic slick graphics
 * @param selected the currently selected distribution
 */
public void displayDistributionInfo(SlickGraphic  slickGraphic,Camera camera) {

	ArrayList<DistributionGL> disList = dlistHandler.getDisList(); // to simplify
	// 
	boolean outOfSight = camera.isOutOfSight(selectedDistGL.getGeoGrid().getCenter()) ;
	boolean isInside = displayIfInsideGrid(camera,slickGraphic);
	// 
	if (useDistribution  && isInside &&  !outOfSight) {

		int x= disTextBox.x;  // Display.getWidth()/2+ 100*( (isInside) ? 0 : 1) ;
		int y= disTextBox.y;// Display.getHeight()/2- 200;
		Point p = new Point(x+5,y) ;
		
		Point mp= new Point(p.x+10, p.y);
		java.awt.Color c= java.awt.Color.white;//slickGraphic.greyColourAt(mp, false);
		ColorInt ci=  new ColorInt(c);
		java.awt.Color cs= ColorInt.makeShiftRGB(c, -120, -100, 100); 
		ci.alpha= 50;
		java.awt.Color c2= ci.makeColorAlpha();
		int xx= p.x;
		
		// 
		mouseIsOccupied= mouseIsOccupied || disTextBox.update();
		disTextBox.draw(slickGraphic);
		disTextBox.height = 30+ 20* (disList.size()-1);

		// List of distributions  
		for (DistributionGL disGL : disList) {
			String s= disGL.info();
			p.x=xx;
			if (disGL==selectedDistGL && disList.size()>1) {
				p.x = xx-5 ;
				slickGraphic.drawString(s, p, cs, 20);
			}
			else if (disGL.isVisible()){
				slickGraphic.drawString(s, p, c);
			}
			else {
				slickGraphic.drawString(s, p, c2) ;
			}
			p.y+=20;
		}

	}

}

/**
 * Displays 2D information text about distribution value
 * @param camera the current camera
 * @param slick the slick graphics
 * @return true if the camera is within the distribution area and not too far away
 */
private boolean displayIfInsideGrid(Camera camera,SlickGraphic slick) {

	glDisable(GL_DEPTH_TEST); // enables drawing text on top of the rectangle in the same plane


	Coordinate c = camera.toCoordinate();
	ArrayList<DistributionGL> disList = dlistHandler.getDisList(); 
	DistributionGL selected = (!disList.isEmpty()) ?  disList.get(index) : null ;
	int ix = -1 ;

	// Displayed distribution values for selected distribution 
	if (selected!=null && camera.determineDistance(1.3f) ) {
		//		if (selected.getGeoGrid().isInside(c)){
		Distribution d= selected.getDistribution();
		ix= c.findClosestIndex(d.getCoordinates(), 0.9f);
		// If Inside distribution area
		if (ix!=-1) {
			ix+= d.getCount()*d.getWH();
			float value = d.getValues()[ix] ;
			String s= (value==d.getFillV()) ? "Land": String.format("%.2f %s", value,d.getUnit());
			//			System.out.println(" Is inside : "+c.toString()+" "+s);
			// 
			Point at= new Point(Display.getWidth()/2,Display.getHeight()/2- 40);
			java.awt.Color co= slick.greyColourAt(at, false);			
			slick.drawString(s, at,co);			
		}
	}
	return (ix!=-1);
}


/**
 * Drawing
 * @param camera
 * @param slick
 */
public void draw( Camera camera,SlickGraphic slick){

	// Topography
	if (topoGL!=null){
		drawTopography(camera);
	} 

	// Globe draw
	if (globe!=null) {


		if (CoreOpenGL.useLight) {
			glEnable(GL_LIGHTING);
		}
		else {
			glDisable(GL_LIGHTING);
		}
		// Drawing
		glDisable(GL_COLOR_MATERIAL);	    
		globe.displayListHandler(camera);
	}



	// Geographical lines
	if (mesh!=null  && useMesh) {
		mesh.draw();
	}

	// Current
	if (cfield!=null) {
		float psize=  (float) Math.max(3, Math.min(16, Math.pow( 200/camera.getHeight(),1.5f ) ));
		cfield.render(count%cfield.getStepInterval()==0, psize,camera.getHeight() );
		agent.update(cfield, 0.5f);
		agent.draw(cfield);
		for (Agent a : agentList) {
			a.update(cfield, 0.5f);
			a.draw(cfield);
		}
//		drawAgents();
	}

	//  Distributions
	ArrayList<DistributionGL> disList = dlistHandler.getDisList(); // to simplify
	if (!disList.isEmpty()) {

		glDisable(GL_LIGHTING);

		DistributionGL disGL =  disList.get(index);
		boolean outOfSight = camera.isOutOfSight(disList.get(index).getGeoGrid().getCenter()) ;
		if (useDistribution && !outOfSight) {
			drawDistributions();
		}
		//  CrossSection
		if (useCrossSection) {
			crossSection.draw(disList,disGL, useDistribution && !outOfSight);
		}


	}
	count++;

}


private void drawAgents() {

	CoreOpenGL.setGLStraightenDrawing();

	
//	glPointSize(5+ 10.0f);
	glPointSize(cfield.getpSize()+5);
	float c[]= agent.getColor().convertToFloat();
	glColor4f(c[0], c[1] , c[2], c[3]);
	
	glBegin(GL_POINTS);
	for (Agent a : agentList) {
		glVertex3f(a.getPosition().x, a.getPosition().y,a.getPosition().z);
	}
	glEnd();

}


/**
 * Draws all the distribution
 */
public void drawDistributions(){
	ArrayList<DistributionGL> disList = dlistHandler.getDisList(); // the list
	
	boolean isResolution = false ;
	
	// A list of distributions drawn if not pausing
	for (DistributionGL disGL : disList) {
		if (!disGL.isPause()  && count%step==0) {
			disGL.getDistribution().count(); //crocodile
		}
		if (disGL.isVisible()) {	
			disGL.render(true, disList.size(), (disGL==selectedDistGL));
			// Centre of mass
			Coordinate co= drawCMass(disGL);
			disGL.setCm(co); 
		}
	}
	
//	showDistribution= (count> 1) ? true : showDistribution ; //mechanism related to the netCDF frame!
}


private Coordinate drawCMass(DistributionGL disGL) {
	Coordinate co;
	Distribution d= disGL.getDistribution();
	int c  = d.getCount();
	Point p = d.getStatistics().getCenterMass()[c] ;
	int ix= d.indexFromXYT(p.x, p.y, c) - c*d.getWH(); 
	co = d.getCoordinates()[ix] ;	
//	System.out.println(ix+ " " + co.toString());
	return co;
}



public void drawTopography(Camera camera){
	//  
	glEnable(GL_LIGHTING);
	glDisable(GL_LIGHTING);
	glDisable(GL_BLEND);
	glDisable(GL_TEXTURE_2D);
	

	topoGL.displayListHandler(camera.position);
//	topoGL.indicateCoordinate(topoGL.getCenter(),globe.getRadius());

}

public void displayHelp(SlickGraphic slick){

	glDisable(GL_DEPTH_TEST); // enables drawing text on top of the rectangle in the same plane
	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);	
	
	//
	String text[] = { "M - Show Grid","N - Show indicator","O - Show cross-section",
			"F2 - Cross-section dialogue input ", 
			" 		- DISTRIBUTION -",
			"J - Change Distribution","D - Show distributions", 
			"Del - Delete distribution","V - Hide Distributions",
			"Shift+Ctrl+o - Open distribution ", "F3 – Show distribution info",
			
			"		- CAMERA -",
			"C - Collect camera ","I - Camera animation ","Shift+c - Clear camera list ",
			"Tab - Next Camera ", "R - Continuous rotation",
			"Shift+Ctrl+c - Open default camera list",
			"Shift+Alt+c - Save current camera list",
			"L - Camera Lights", "Shift+L - Spotlight" 
			
	};
	
	// Drawing
	helpTextBox.height=1 + text.length*25;
	helpTextBox.width = 350 ;
	
	// 
	ColorInt c=  new ColorInt(10,25,20, 120);//Color.green);
	helpTextBox.setFillColour(c);
	mouseIsOccupied= mouseIsOccupied || helpTextBox.update();	
	helpTextBox.draw(slick);

	java.awt.Color ct= java.awt.Color.white;//slick.greyColourAt(p, false);
	ColorInt textColour= new ColorInt(ct);//java.awt.Color.white);
	Point p= new Point(helpTextBox.x+4, helpTextBox.y);
	for (String tx : text) {
		slick.drawString(tx, p, textColour.makeColor(),slick.fontTTF20);
		p.y+= 25;
	}

//	slick.drawRect(x-1, y-1, w+1, h+1);

	glEnable(GL_DEPTH_TEST);
}


public void cameraToTopography(Camera camera){

	if (topoGL!=null) {
		if (topoGL.getCenter()!=null) {
			Vector3D p= TopographyGL.latLonToPoint(topoGL.getCenter().getLat(), topoGL.getCenter().getLon());
			p.scaleVector(globe.getRadius());
			camera.setLookAt(p);
			camera.setCriticalDistance(10);
			//					lookAt.equals(position.normalised().scaledTo(400));
			//					criticalDistance = 10 ;
		}
	}
}

public boolean respondMouse(Camera camera){
	boolean didRespond= false;
	boolean alt= CoreOpenGL.isAlt();
	
	if (selectedDistGL!=null) {
		GeoGrid g= selectedDistGL.getGeoGrid();

		if (g.isInside(camera.getCoordinate()) && alt) {
			// MouseWheel  
			int dw=Mouse.getDWheel();
			step+=dw/120;
			step = Math.max(1, step) ;
			//		System.out.println("w="+step);
		}
	} 
	
	
	return didRespond;
}

/**
 * Scene responding to keyboard shortcuts, 
 * for distribution , current ,cross-section, mesh, gradient,
 * @param keyEvent 
 * @param keyDown  
 * @return
 */
public boolean respondKeyboard(int keyEvent, boolean keyDown,Camera camera){
	boolean isRepeat =Keyboard.isRepeatEvent();

	boolean shift = CoreOpenGL.isShift();
	boolean alt= CoreOpenGL.isAlt();
	boolean control= CoreOpenGL.isCtrl();
	boolean only= !shift && !alt && !control;
	boolean wasPressed= true;

	// Keyboard handler for selected GeoGrid
	if (selectedDistGL!=null) {
		if (selectedDistGL.getGeoGrid() !=null) {
			selectedDistGL.getGeoGrid().handleKeyboard(keyDown);			
		}
	}
	
	
	// Use mesh
	if (keyEvent== Keyboard.KEY_M && only && keyDown && !isRepeat ){
		setUseMesh(!isUseMesh());
	}
	//  CrossSection
	else if (keyEvent==Keyboard.KEY_O  && only && keyDown) {
		useCrossSection=!useCrossSection;
	}
	
	// ColorGradient
	else if(Keyboard.isKeyDown(Keyboard.KEY_H)  && control  && !isRepeat){
		ColorGradient g= null;
		GeoGrid geoGrid= (selectedDistGL!=null) ? selectedDistGL.getGeoGrid() : null ;
		
		// Change gradient for field. Either there are no distribution or choose not to use
		if ((!useDistribution  || selectedDistGL==null) && cfield!=null) {
			g= cfield.getGradient();
		}
		// Change gradient for distribution or topography
		else {
			g= (!shift && geoGrid!=null ) ? geoGrid.getGradient(): 
			(topoGL!=null) ? topoGL.getGradient() : null ;
		}
		// If we got hold of a gradient,show the GradientPanel 
		if (g!=null ) {
			GradientPanel panel= new GradientPanel(g);
			panel.show(g.getName());
		}
	}
			
	// – Distribution –
	
	// Use distribution on/off
	else if (keyEvent==Keyboard.KEY_D   && only && keyDown){
		useDistribution= !useDistribution;
		dlistHandler.synchroniseDistributions(true);
	}
	// Display distribution information
	else if (keyEvent==Keyboard.KEY_F3 &&  keyDown) {
		useDistributionInfo =!useDistributionInfo;
	}
	// Visibility of distributions
	else if (keyEvent==Keyboard.KEY_V  && keyDown){
		useVisibleDist= !useVisibleDist;
		dlistHandler.visibilityOfDistributions(useVisibleDist);

		try {
			selectedDistGL.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Deletes the current distribution
	else if (Keyboard.isKeyDown(Keyboard.KEY_DELETE)  && keyDown){
		dlistHandler.removeDistribution();
		selectedDistGL = dlistHandler.getSelected(); // redundancy !
		if (selectedDistGL!=null) {
			dlistHandler.visibilityOfDistributions(true);
		}
	}
	// Pausing distributions – on/off –
	else if ( keyEvent==Keyboard.KEY_P  && keyDown){
		for (DistributionGL d : dlistHandler.getDisList() ) {
			boolean pause= ! d.isPause();
			d.setPause(pause);
		}
		// Also pausing current field
		if (cfield!=null) {
			boolean pause= ! cfield.isPause();
			cfield.setPause(pause);
		}
	}
	// Automatically rescale distribution (relevant for zooplankton)
	else if(Keyboard.isKeyDown(Keyboard.KEY_R) && control && keyDown  &&  !isRepeat){
		Distribution d = selectedDistGL.getDistribution() ;
		d.rescaleAutomatically(); // rescaleValues(true);
	}
			
	
	// Change which one is the selected distribution
	else if (keyEvent== Keyboard.KEY_J  && keyDown){
		if (!dlistHandler.getDisList().isEmpty()) {
			int di= (shift) ? -1 : 1;
			selectedDistGL= dlistHandler.changeDistribution(di);
			dlistHandler.visibilityOfDistributions(useVisibleDist);
			selectedDistGL.setVisible(true);
			// Synchronised pausing !
			for (DistributionGL d : dlistHandler.getDisList()) {
				d.setPause(dlistHandler.getDisList().get(0).isPause());
			}
		}
	}
	// Change current field visual linewidth
	else if (keyEvent==Keyboard.KEY_COMMA ) {
		cfield.setLinew( (shift) ? cfield.getLinew()*0.8f :cfield.getLinew()*1.25f);
		float maxmini= Math.max(0.125f,  Math.min( 8, cfield.getLinew() ) );
		cfield.setLinew(maxmini);
		System.out.println(" field line width: "+cfield.getLinew());
		
	}
	// Change current field scale
	else if (keyEvent==Keyboard.KEY_PERIOD ) {
		cfield.setScale( (shift) ? cfield.getScale()-1 : cfield.getScale()+1);
		float maxmin= Math.max(1, Math.min(40, cfield.getScale()) );
		cfield.setScale(maxmin);		
		System.out.println(" field scale: "+cfield.getScale());
		
	}
	// Change current field step interval 
	else if (keyEvent==Keyboard.KEY_MINUS && keyDown) {
		int step = cfield.getStepInterval() ;
		cfield.setStepInterval( (shift) ? step+1 : step-1 );
		cfield.setStepInterval(Math.max(1, cfield.getStepInterval()) );		
		System.out.println(" field step: "+cfield.getStepInterval());
	}
	//  Animate current field
	else if (keyEvent==Keyboard.KEY_F5 && keyDown) {
		cfield.setUseAnimation( !cfield.isUseAnimation() );
	}
	else if (keyEvent==Keyboard.KEY_APOSTROPHE && keyDown) {
		cfield.setVisualThreshold( (shift)  ? cfield.getVisualThreshold()*0.5f :cfield.getVisualThreshold()*2 );
		System.out.println("field threshold "+cfield.getVisualThreshold());
	}
	else if (keyEvent==Keyboard.KEY_F11 && keyDown) {
		agent.setPosition(camera.getCoordinate());
		for (Agent a : agentList) {
			Coordinate c= camera.getCoordinate();
			c.perturbation(0.1f);
			a.setPosition(c);			
		}
	}
	
	else {
		wasPressed= false;
	}
	
	
	return wasPressed;
}


//  |––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––|   
//  
//	|––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––|

	/**
	 * @return the mesh
	 */
	public CoordinateMesh getMesh() {
		return mesh;
	}


	/**
	 * @param mesh the mesh to set
	 */
	public void setMesh(CoordinateMesh mesh) {
		this.mesh = mesh;
	}


	/**
	 * @return the globe
	 */
	public Globe getGlobe() {
		return globe;
	}


	/**
	 * @param globe the globe to set
	 */
	public void setGlobe(Globe globe) {
		this.globe = globe;
	}




	/**
	 * @return the crossSection
	 */
	public CrossSection getCrossSection() {
		return crossSection;
	}

	/**
	 * @param crossSection the crossSection to set
	 */
	public void setCrossSection(CrossSection crossSection) {
		this.crossSection = crossSection;
	}

	/**
	 * @return the useMesh
	 */
	public boolean isUseMesh() {
		return useMesh;
	}

	/**
	 * @param useMesh the useMesh to set
	 */
	public void setUseMesh(boolean useMesh) {
		this.useMesh = useMesh;
	}

	/**
	 * @return the selectedDistGL
	 */
	public DistributionGL getSelectedDistGL() {
		return selectedDistGL;
	}

	/**
	 * @param selectedDistGL the selectedDistGL to set
	 */
	public void setSelectedDistGL(DistributionGL selectedDistGL) {
		this.selectedDistGL = selectedDistGL;
	}

	/**
	 * @return the useCrossSection
	 */
	public boolean isUseCrossSection() {
		return useCrossSection;
	}

	/**
	 * @param useCrossSection the useCrossSection to set
	 */
	public void setUseCrossSection(boolean useCrossSection) {
		this.useCrossSection = useCrossSection;
	}

	/**
	 * @return the useVisibleDist
	 */
	public boolean isUseVisibleDist() {
		return useVisibleDist;
	}

	/**
	 * @param useVisibleDist the useVisibleDist to set
	 */
	public void setUseVisibleDist(boolean useVisibleDist) {
		this.useVisibleDist = useVisibleDist;
	}

	/**
	 * @return the useDistribution
	 */
	public boolean isUseDistribution() {
		return useDistribution;
	}

	/**
	 * @param useDistribution the useDistribution to set
	 */
	public void setUseDistribution(boolean useDistribution) {
		this.useDistribution = useDistribution;
	}

	/**
	 * @return the box
	 */
	public SelectBox getBox() {
		return box;
	}

	/**
	 * @param box the box to set
	 */
	public void setBox(SelectBox box) {
		this.box = box;
	}

	/**
	 * @return the disTextBox
	 */
	public SelectBox getDisTextBox() {
		return disTextBox;
	}

	/**
	 * @param disTextBox the disTextBox to set
	 */
	public void setDisTextBox(SelectBox disTextBox) {
		this.disTextBox = disTextBox;
	}

	/**
	 * @return the mouseIsOccupied
	 */
	public boolean isMouseIsOccupied() {
		return mouseIsOccupied;
	}

	/**
	 * @param mouseIsOccupied the mouseIsOccupied to set
	 */
	public void setMouseIsOccupied(boolean mouseIsOccupied) {
		this.mouseIsOccupied = mouseIsOccupied;
	}

	public boolean isUseDistributionInfo() {
		return useDistributionInfo;
	}

	public void setUseDistributionInfo(boolean useDistributionInfo) {
		this.useDistributionInfo = useDistributionInfo;
	}

}
