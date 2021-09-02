package basicLwjgl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
// Slick
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.TrueTypeFont;

import Basic.Circle;
import Basic.TextureImage;
import Utility.Coordinate;
import Utility.FileUtility;
import Utility.NetCDFPanel;
//import basicGUI.NetCDFPanel;
import Colors.ColorGradient;
import Colors.ColorInt;
import Colors.GradientPanel;
import distribution.Distribution;
import distribution.Topography;
//import ecosystem.SurveyLines;
import Basic.Vector2D;
import GeometryGL.CoordinateMesh;
import GeometryGL.DistributionGL;
import GeometryGL.GeoGrid;
import GeometryGL.Globe;
import GeometryGL.TopographyGL;
import GeometryGL.Gui.CrossSectionInput;
import Testing.ReadCamera;

import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.BufferUtils;

/**
 * CoreOpenGL 
 * @author a1500
 *
 */
public class CoreOpenGL implements MouseListener, KeyListener {

	//  Display
	int width=1800;
	int height=800;
	//  
	static float PI= (float) Math.PI;

	GLMaterial material= new GLMaterial();

	//  Camera
	private Camera camera;
	ArrayList<Camera> cameralist= new ArrayList<>();
	Camera interpolCamList[]= null;
	private boolean useCameraList= false;
	private int countCam=0 ;
	private int camindex=0;
	//  Mode
	private int mode= 0; 

	// Objects in the scene
	public SceneGL sceneGL= null;

	public Globe globe= null;

	//	TopographyGL topoGL= null;

	// Mouse position
	private Vector2D mouseAt= new Vector2D();
	// Light
	Light fixLight= null;
	Light spotLight= null;
	static boolean useLight= true;
	//  Milliseconds since last keyboard event
	private long lastTime= 0;
	private long count=0;
	private int frames=0;
	// Time resolution
	static float dT= 0.1f ;

	// Texture 
	private TextureImage textureImage= null;
	//	private TextureImage textureImage2= null;
	private Texture texture= null;

	private BufferedImage image= null;
	//	private byte[] pixels=null;
	//	private ByteBuffer pixelBuffer ;

	//  Graphics enabling the use of slick library
	SlickGraphic slickGraphic=null;// 
	TrueTypeFont font;
	// Control
	private boolean keyDown= false;
	private boolean wasControl= false;
	//	private boolean wasCoo=false;
	private boolean useIndicator= true;
	private boolean useHelp= false ;
	private String characters="";
	private float  fps=0;
	private String pathNetCDF = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/November 2020/";
	private NetCDFPanel netCDFPanel = null;

	//	private DistributionGL selectedDistGL= null;
	private boolean useVisibleDist=false;
	private boolean useCameraFollowsCM= false;



	/**
	 * Constructor
	 */	
	public CoreOpenGL(){

		lastTime= System.currentTimeMillis();

		Coordinate c = new Coordinate(55, 5) ;
		camera= new Camera(c,300);	//   Camera defined first because used in the initialisation
		// •–•–•–Testing
		cameralist= ReadCamera.openCameraFile(null);

		initDisplay();
		initGL();

		//		Display.getParent().addKeyListener(this);

		// Slick specifics
		slickGraphic=new SlickGraphic(width, height);
		//		slickGraphic.setWorldClip(-width, 0, width, height-50);
		Graphics.setCurrent(slickGraphic);

		// Light	
		createLights();

		prepareObjects();

		count=0;

		// Start
		initialDraw(20, " – Rendering starts :  ");
		renderLoop();// 

	}



	/**
	 * Create the lights
	 */
	public void createLights() {
		// try North Pole
		fixLight= new Light(0, new Vector3D(), false   ,false  ); //directional, spot
		fixLight.defineAt(89.9f, 0, Globe.Radius+300);
		fixLight.position.z-=300; 
		fixLight.darken(0.01f);
		fixLight.lightProperties();
		fixLight.setUseRotating(false );
		createSpotlight();//   Remember not to call this before initiating OpenGL !
	}



	/**
	 * Define and create all objects in the environment
	 */
	public void prepareObjects() {

		// OpenGL context must first be created!
		initialDraw(2," Initiating Scene…");
		// SceneGL
		sceneGL= new SceneGL() ;
		// Current
		initialDraw(2," Defining current field …");
		sceneGL.defineCurrent();

		// CrossSection
		initialDraw(2," Defining CrossSection …");
		sceneGL.defineCrossSection();

		// Texture
		initialDraw(2," Opening texture image…");
		// The image used for texturing the globe
		openImageTexture();				
		initialDraw(2," Preparing texture image…");
		texture= getTextureFromImage(image); //  may be moving to globe?  	


		// Globe  
		initialDraw(2, " – Constructing the globe…  ");
		sceneGL.createGlobe(texture);
		globe= sceneGL.globe;

		// GeoGrid – Distribution	//Label1
		initialDraw(2, "– Opening a distribution –");
		//		Distribution distribution =  OpenGLTesting.testDistribution();
		//		sceneGL.dlistHandler.addDistribution(distribution);
		//		sceneGL.setSelectedDistGL(  sceneGL.dlistHandler.getDisList().get(0) );

		// Topography  
		initialDraw(2," Defining topography");
		//		 		sceneGL.defineTopographyGL(image);
	}


	//	byte[] pixels = new byte[size.x * size.y * 4];
	//	ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
	//	glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	//	buffer.get(pixels);
	//


	/**
	 * Initialises the lwjgl OpenGL display 
	 * 
	 */
	private  void initDisplay() {

		PixelFormat pixelFormat= OpenGLTesting.createPixelFormat();
		System.out.println( OpenGLTesting.getPixelInformation(pixelFormat) );

		//
		try {
			// 
			Display.setDisplayMode(new DisplayMode(width, height  ));
			Display.setLocation(10, 30);
			Display.setResizable(true);
			Display.setSwapInterval(1);
			//Display.sync(40);


			//        Display.setParent(Basicframe.getCanvas());
			Display.create(pixelFormat);

			Keyboard.create();
			Mouse.create();

		} catch (LWJGLException ex) {
			Logger.getLogger(CoreOpenGL.class.getName()).log(Level.SEVERE, null, ex);
		}

	}


	/**
	 * Initialises OpenGL the settings and states
	 */
	private void initGL(){

		defineGLProjection();

		glClearColor(0.15f,0.16f,0.16f, 0.9f );

		glEnable(GL_DEPTH_TEST);
		//
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);     
		glEnable(GL_POINT_SMOOTH);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_CULL_FACE);
		glEnable(GL_NORMALIZE);	// November 2020, essential for lightning it seems…

		glShadeModel(GL_SMOOTH);
		glHint(GL_POINT_SMOOTH_HINT,GL_NICEST);
		glHint(GL_LINE_SMOOTH_HINT,GL_NICEST);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		//		    	glLightModelf(GL_LIGHT_MODEL_LOCAL_VIEWER,GL_TRUE);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NICEST );
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NICEST);

		//		    	OpenGLTesting.scissor(new Rectangle(300, 200, 400, 200));

	}
	
	public static void setGLStraightenDrawing() {
		glDisable(GL_LIGHTING);
		glDisable(GL_COLOR_MATERIAL);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_CULL_FACE);
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	



	/**
	 * Sets a perspective projection using: gluPerspective
	 * The angle of the camera is used
	 */
	public void setGLPerspective(){
		// Select Projection Matrix (controls perspective)
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		// fovy, aspect ratio, zNear, zFar
		float aspect= 1.0f*( Display.getWidth()/(1.0f*Display.getHeight()) );
		float angle = (camera!=null)  ? camera.getAngle(): 50 ;

		//«•» ! reversing this to: 1f, -100f from: -1f, 100f,
		//was the trick to make the depth testing work right !! «•» ??? «•»
		gluPerspective(angle, aspect , 0.1f, -100f); 

		// Return to modelview matrix
		glMatrixMode(GL_MODELVIEW);
	}


	// –	testing  
	//			pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	//			pixels =image.getRaster().getDataBuffer();

	//			pixels = BufferTools.getByteData(image) ; // bytes
	//			pixelBuffer =ByteBuffer.allocateDirect(pixels.length*4);
	//			pixelBuffer=BufferUtils.createByteBuffer( pixels.length*4);
	//			pixelBuffer = ByteBuffer.wrap(pixels);
	//			System.out.println("Direct ByteBuffer:  "+Arrays.toString(pixelBuffer.array())); 




	/**
	 * Defines the projection using the display width and height
	 * Needs to be called when we change the size of the window
	 */
	public void defineGLProjection() {
		// Projection	
		glMatrixMode(GL_PROJECTION);   
		glLoadIdentity(); 
		// glOrtho(500,Display.getWidth(),0,Display.getHeight(),-10,1);
		glViewport(0, 0, Display.getWidth(),Display.getHeight());
	}



	private void openImageTexture() {
		String path =   "./res/L 8000x4000 n1.png";
		path = 	"./res/N 8192 x 4096.png";
		path = 	"./res/A small.png"; 
//		path = 	"./res/An 2.png"; // 
		//		path = 	"./res/A1 8192x4096 .png";
		//		path = "./res/A 8192× 4096 3km .png" ; //  higher resolution 
				path = "./res/N 4096 × 2048.png"; // use this	
		// super 16384×8192 3km.png";
		//		path = 	"./res/relief.png";
		//			String path =   "./res/World Text.png";

		// Nice 6x4k
		//		textureImage= new TextureImage("//res/World Text.png");//texture world1.png");//res/winter1.jpg");

		try {
			long lastFrame= System.nanoTime();
			image= ImageIO.read(new File(path));

			long timeTookms= (long) ((System.nanoTime()- lastFrame)*0.0000001);

			System.out.println(" Read image texture(ms) "+timeTookms);
			System.out.println("image: "+image.getWidth()+" × "+ image.getHeight());

		} 
		catch (IOException e) {
			System.out.println(" Problems reading image"+ image.toString());	
		}

	}



	/**
	 * @throws IOException 
	 * 
	 */
	public static Texture getTextureFromImage(BufferedImage image)  {
		Texture texture= null;

		try {
			texture = BufferedImageUtil.getTexture("", image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return texture;
	}



	/**
	 * Render continuously
	 */
	private void renderLoop() {

		//========================================
		while (!Display.isCloseRequested()){

			render();
		}

		//
		cleanUp();
	}



	/**
	 * Render everything
	 */
	public void render() {

		lastTime= System.currentTimeMillis();
		//			System.out.println(lastTime);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();


		// – Camera –
		camera.update();
		// Using interpolated camera
		if (useCameraList && interpolCamList!=null) {
			if (interpolCamList.length>1) {
				camera.setCamera(interpolCamList[countCam++]);
				countCam = (countCam>= interpolCamList.length ) ? 0: countCam ;
			}
		}
		//   camera follows central mass
		if (useCameraFollowsCM) {
			if(sceneGL.getSelectedDistGL()!=null) {
				Coordinate c= sceneGL.getSelectedDistGL().getCm(); // getGeoGrid().getCenter();
				camera.defineFromLatLong(c, Globe.Radius+camera.getHeight());
			}
			else if(sceneGL.agent!=null) {
				camera.defineFromLatLong(sceneGL.agent.getC(), Globe.Radius+camera.getHeight());
			}
		}


		// Must update the light every frame because the view is changing
		fixLight.update(count);
		// 
		if (spotLight!=null) {
			spotLight.update(count);
		}

		//					textureImage.drawTexture(new Vector2D(0,0), 1.0f, count*0.01f);
		//			OpenGLTesting.drawAxis(800);

		//Label2
		OpenGLTesting.setupTextureAndCull(texture);//Image.getTexture());

		//					OpenGLTesting.drawSphere(frames, 50, null , globe.getTexture());
		//					drawCylinder
		//					OpenGLTesting.simpleSphere();
		//		OpenGLTesting.drawSphere(frames, 10, spotLight.position, null);//globe.getTexture());

		// Material
		//			material.applyMaterial(); // problem with a light

		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

		if (globe.isDoRecreate()) {
			redefineGlobeTexture();
		}
		//  Drawing Scene
		sceneGL.draw(camera,slickGraphic);

		// Draw transparent object last
		// 	OpenGLTesting.drawOrigin(500,0.051f);

		// Slick2D
		draw2D();


		// Reset to perspective
		setGLPerspective();

		// –– Measure time –– 
		long timeTookms= 1+(long) ((System.currentTimeMillis()- lastTime)*1);

		// Display 		// crocodile 
		Display.update();
		frames++;
		displayTitle(frames, timeTookms);

		//  Do not update mouse or keyboard when the mouse is occupied
		if ( !sceneGL.isMouseIsOccupied()) {
			handleMouse();
			handleKeyboard();
		}
		sceneGL.setMouseIsOccupied(false );


		count++;
		// From time to time check this
		// This is a trick – which adds distribution to the distribution list.
		// For some reason it crashes in netCDF panel
		if (count%20==0) {
			checkDistribution();

			// In case window size is changed
			defineGLProjection();
			setGLPerspective();
		}
		// Trick for the graphics slick
		if (count%300==0) { // every 5 seconds when 60 frame/s
			if (Display.getWidth()!=width || Display.getHeight()!=height) {
				width = Display.getWidth() ;
				height =Display.getHeight();							
				slickGraphic=new SlickGraphic(width, height);
				Graphics.setCurrent(slickGraphic); // ?
			}
		}

	}

	public static ColorGradient defineGradient(float max, float min,String name) {
		//		System.out.println("max= "+max);
		ColorGradient gradient=new  ColorGradient(0,0);
		GradientPanel gp= new GradientPanel(gradient);
		gp.openGradientInformation(new File("./res/Gradients/"+name), gradient);
		gradient.setMax(max);
		gradient.setMin(min);
		gradient.define();
		return gradient;
	}


	/**
	 * Checks if a distribution has been added to the list of distributions
	 * in the netCDF panel
	 */
	private void checkDistribution(){
		if (netCDFPanel!=null) {
			//			if (netCDFPanel.isClosed() &&
			if (netCDFPanel.isGenerated()) {
				if (netCDFPanel.getDisGL()!=null) {
					Distribution  d= netCDFPanel.getDisGL().getDistribution();
					sceneGL.dlistHandler.addDistribution(d);
					netCDFPanel.setGenerated(false);
				}
			}
		}
	}





	/**
	 * @param frames
	 * @param timeTookms
	 * @return
	 */
	public void displayTitle(int frames, long timeTookms) {
		//  Information
		String info=  camera.information();
		String title = "Frames "+frames;
		fps+= 1000/timeTookms;
		if (frames%10==0) {
			title+= " " + "f/s=  "+ fps/10  + info;
			fps= 0;
			Display.setTitle(title);	
		}
	}


	/**
	 * Creates a spotlight which the camera can leave at any location
	 * Using the shortcut (Shift+l)
	 */
	public void createSpotlight() {
		spotLight= new Light(1, new Vector3D(), true ,true );//true, false);
		spotLight.defineAt(30, -40, Globe.Radius+100);
		spotLight.setSpotCutoff(60);
		spotLight.darken(0.01f); // <1 makes it lighter
		Vector3D d = spotLight.position.normalised().scaledTo(-1);
		//		d.rotatedHorizontal(85);
		spotLight.direction.equals(d);

		spotLight.setUseRotating(false );
		spotLight.specular.red= 20;
		spotLight.ambient.set(Color.gray);//  red= 50;
		spotLight.lightProperties();
	}


	/**
	 * 
	 */
	private void defineLight() {
		//light
		int ID=0;
		FloatBuffer pos= BufferUtils.createFloatBuffer(4);
		FloatBuffer direction= BufferUtils.createFloatBuffer(4);
		pos.put(10).put(4000).put(100).put(0f).flip();// 0 for direction, 1 for position

		double lat=toRad(90);
		Vector3D p= new Vector3D( (float) Math.cos(lat) ,(float) Math.sin(lat) , 0);
		p.scaleVector(2000.0f);
		//	p.rotateHorizontal(count*0.01f);
		direction.put(p.x).put(p.y).put(p.z).put(1f).flip();// 0 for direction, 1 for position


		FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
		ambientLight.put(.2f).put(.2f).put(.2f).put(1).flip();
		//specular diffuse
		FloatBuffer specularLight= BufferUtils.createFloatBuffer(4);
		FloatBuffer diffuseLight= BufferUtils.createFloatBuffer(4);
		specularLight.put(.9f).put(.9f).put(.9f).put(1).flip();
		diffuseLight.put(0.7f).put(0.7f).put(0.7f).put(1).flip();

		// Light  
		glEnable(GL_LIGHTING);
		//	glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientLight);
		//
		glEnable(GL_LIGHT0);
		//General Light  Properties ////////////
		glLight(GL_LIGHT0+ID , GL_POSITION , direction);
		glLight(GL_LIGHT0+ID , GL_AMBIENT ,  ambientLight);
		glLight(GL_LIGHT0+ID , GL_DIFFUSE ,  diffuseLight);
		glLight(GL_LIGHT0+ID , GL_SPECULAR , specularLight);

	}

	//
	///**
	// * 
	// */
	//public GeoGrid defineGeoGrid(Distribution dis) {
	//	int width = dis.getWidth();
	//	int height= dis.getHeight();
	//	
	//	String nameGr= String.format("default%d.txt", number+2);
	//	GeoGrid g= new GeoGrid(dis.getCoordinates(), width, height, globe.getRadius()+2,nameGr);
	//	g.createVertexes();
	//	return g;
	//}


	/**
	 * Creates the Topography 
	 *
public void defineTopographyGL() {
	//  Forces specific number of grid cells
	float  xlon= (1600*Topography.ETPO2); 
	float  ylat=  (400*Topography.ETPO2);   
	int levels=2;

	Coordinate c1=  new Coordinate(84, -25);
	Coordinate c2=  new Coordinate(c1.getLat()-ylat , c1.getLon()+xlon );//50, 20);
	Topography topo =   OpenGLTesting.createTopography(c1,c2);
	sceneGL.topoGL=  new TopographyGL(topo, topo.getWidth(),topo.getHeight(),globe.getRadius(),levels);
	// Change the pixels of the texture image to transparent
	tryChangePixels(topo.getUpper().getLat(),topo.getUpper().getLon(),
					topo.getLower().getLat(),topo.getLower().getLon());
}
	 */

	public void initialDraw(int n, String text){
		ColorInt c= new ColorInt(Color.white);
		ColorInt ctitle= new ColorInt(SlickGraphic.SILVER_Blue);
		String title= " Visualising NORWECOM.E2E (NetCDF viewer)" ;
		project2D();

		long timeTookms= (long) ((System.currentTimeMillis()- lastTime)*1);
		String time= String.format(" – Elapsed time : %.2f s",  (0.001f*timeTookms));


		for (int i = 0; i < n; i++) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
			Point p= new Point(Display.getWidth()/4, Display.getHeight()/4);
			slickGraphic.drawString(title, p, ctitle.makeColorAlpha(),SlickGraphic.awtFontCuI40);
			p.y+= 90;  p.x+=90;
			slickGraphic.drawString(text, p, c.makeColorAlpha(),SlickGraphic.awtFontTNR30);
			p.y+=40;
			slickGraphic.drawString(time, p, c.makeColorAlpha(),SlickGraphic.awtFontTNR24);
			Display.setTitle(title);
			Display.update();
		}
	}

	/**
	 * 
	 */
	public void draw2D() {

		project2D();

		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDisable(GL_CULL_FACE);
		glDisable(GL_LIGHTING);
		glDisable(GL_DEPTH_TEST);
		//	glEnable(GL_TEXTURE_2D);

		//	aSlickTextAt( String.format("Frames: %d", count), new Vector2D(5, 5));
		//		slickGraphic.draw(camera.getText());
		// 


		// Draw the topography ColourGradient image
		if (sceneGL!=null) {
			if (sceneGL.topoGL!=null) {
				int x=(int) (Display.getWidth()-90);
				int y=(int) (Display.getHeight()-300);
				slickGraphic.drawImage(sceneGL.topoGL.getGradientImage(), x,y);	
				if (count%20==0) {
					sceneGL.topoGL.convertGradientToImage();
				}
			}
		}

		// «•»«•» Problem with slick text, for some unknown reason we need to 
		// do this before we draw text ?
		Vector2D p=  new Vector2D(5, 5);//mouseAt); // new Vector2D(20,20);//
		slickGraphic.animatedCircle(p,10,camera.dw*count);

		//	slickGraphic.drawString(camera.getText(),5,Display.getHeight()-50);
		//	slickGraphic.drawARectangle(new Vector2D(2,50),true  );

		if (useIndicator) {
			indicateCentre();
		}
		// 
		//  Displaying information on distributions on the screen
		// •Pack everything into displayDistributionInfo 
		if (sceneGL.isUseDistribution() &&  sceneGL.isUseDistributionInfo()
				&&  sceneGL.getSelectedDistGL()!=null) {
			sceneGL.displayDistributionInfo(slickGraphic, camera);
		}
		// Show information on current field if distribution is not visible
		else if (sceneGL.isUseDistributionInfo()){ // using the same shortcut as for distribution,F3!
			sceneGL.cfield.displayInformation(camera, slickGraphic);
		}
		
		// Time date
		if (sceneGL.cfield!=null) {
			sceneGL.cfield.displayDate(slickGraphic);
		}
		// Help F1
		if (useHelp) {
			sceneGL.displayHelp(slickGraphic);
		}

		// testing$
		//	sceneGL.box.update();
		//	sceneGL.box.draw(slickGraphic);
		// 


		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
	}


	/**
	 * 
	 */
	public static void project2D() {
		glMatrixMode(GL_PROJECTION);   
		glLoadIdentity(); 
		glOrtho(0,Display.getWidth(),Display.getHeight(), 0, 1f, -1000);

		glMatrixMode(GL_MODELVIEW); 
		glLoadIdentity(); // Clear the model matrix
	}


	/**Show centre where camera is looking
	 * 
	 */
	public void indicateCentre() {

		glDisable(GL_DEPTH_TEST); // 		


		Point at= new Point(Display.getWidth()/2,Display.getHeight()/2);
		java.awt.Color c= (useLight) ?  slickGraphic.greyColourAt(at, false) :
			Color.white;

		displayCircle(0.1f, 10,0.6f, at, Color.red);
		displayCircle(0.05f, 20, 0.7f, at, c);

		// Trick 
		//	slickGraphic.setColor(new org.newdawn.slick.Color(0.5f, 0.5f, 0.5f, 0.5f));

		// Camera angle and distance determines visibility
		if (camera.determineDistance(1.4f)) {
			at.x-= 20;
			at.y+= 20; 
			slickGraphic.drawString(camera.shortInfo(),at, c);//0.9f, 0.8f, 0.8f, 0.7f));

			// Information on depth
			if (globe.getTopography()!=null) {
				Coordinate coo= camera.toCoordinate(); 
				float h=  globe.getTopography().getValueAtCoordinate(coo);
				String text= (h==0) ? "" : String.format("%.1f m", h);
				at.y+= 20;
				c.darker();
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				slickGraphic.drawString(text,at, c);
			}
		}


		//  Update graphics in case display has changed size
		slickGraphic.setWidth(Display.getWidth());
		slickGraphic.setHeight(Display.getHeight());
	}


	/**
	 * 
	 */
	public void aSlickTextAt(String text, Vector2D pos) {

		//	slickGraphic.setBackground(new org.newdawn.slick.Color(0.55f, 0.5f, 0.2f, 0.5f));
		slickGraphic.setColor(new org.newdawn.slick.Color(0.5f, 0.5f, 0.1f, 0.8f));
		font.drawString(pos.getX(), pos.getY(), text);
		//	slickGraphic.drawString(st, 5, 5);
		pos.shift(120, 30);
		//	animatedCircle(pos,30);

	}





	/**
	 * 
	 */
	public void displayCircle(float phase, float radius, float alpha,Point p, Color color) {
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//  
		glPushMatrix();
		glTranslatef(p.x,p.y,0);// Display.getWidth()/8, 100, 0);
		Circle c= new Circle((float) (Math.cos(count*phase)*radius),16);
		c.draw(color, alpha );
		glPopMatrix();

	}


	// CHECKING the keyboard

	private void handleKeyboard(){
		int ix=0;
		// Check which key is first pressed
		int keyEvent= Keyboard.getEventKey();
		wasControl= keyEvent==Keyboard.KEY_LCONTROL || keyEvent==Keyboard.KEY_RCONTROL;

		// Special key allows entering coordinate
		// 	boolean wasW =   keyEvent==Keyboard.KEY_W;
		//	wasCoo = wasCoo || wasW ;

		characters="";
		// We want repeated key events unless it is control	
		Keyboard.enableRepeatEvents(!wasControl);
		//	String info = String.format("ch="+  Keyboard.getEventCharacter()+ "# "+keyEvent +
		//			"control "+wasControl+" repeating "+Keyboard.areRepeatEventsEnabled());


		while (Keyboard.next()  && ix<10 ){
			char ch= Keyboard.getEventCharacter();
			characters+= ch; //(wasW || noKey) ? "" : ch;
			respondKeyboard(ix);
			ix++;
			if (keyEvent>0) {
				//			System.out.println(keyDown+" key: "+ch +" "+ keyEvent);
			}

			//		System.out.println(info);
		} 



		int length= characters.length();

		//	if (length>2) {
		//		System.out.println("l= " + length+" characters: " + characters);
		//		characters.trim();
		//		System.out.println("l= " + length+" characters: " + characters);
		//		
		//	}
		//	
		//	if (characters.contains("Sweden")) {
		//		System.out.println("contains " + characters);
		//	}

		// Updates the camera
		setGLPerspective();

	}

	/**
	 * Response to keyboard input
	 * @return
	 */
	private char respondKeyboard(int n) {
		char ch= Keyboard.getEventCharacter();
		int keyEvent= Keyboard.getEventKey();


		//
		// Only respond when the key is pressed down, not released
		// ==================================================
		if (Keyboard.getEventKeyState() ){

			//		boolean noKey= Keyboard.isKeyDown(Keyboard.CHAR_NONE); 
			//		long timeTookms= (long) ((System.currentTimeMillis()- lastTime)*1);
			boolean isRepeat =Keyboard.isRepeatEvent();
			//		System.out.println( Keyboard.getKeyName( Keyboard.getEventKey()) );

			boolean shift= isShift();
			boolean alt= isAlt();
			boolean control= isCtrl();
			boolean only= !shift && !alt && !control;

			//		System.out.println(timeTookms+" ms "+Keyboard.getEventCharacter()+" repeating = "+isRepeat);

			//		System.out.print("key:" + Keyboard.getKeyName(Keyboard.getEventKey()) +
			//		" Pressed:" + Keyboard.getEventKeyState());
			//        System.out.println("Key character code: 0x" + Integer.toHexString(character_code));
			//		System.out.println(" Key character: " + ch );
			//		System.out.println("Repeat event: " + Keyboard.isRepeatEvent());
			// 


			//  Check camera 1st
			boolean wasPressed= camera.respondKeyboard(keyEvent,keyDown,mode);
			
			// Check keyboard response for other objects
			if (!wasPressed) {
				wasPressed=globe.handleKeyboard(keyEvent,keyDown);

				if(!wasPressed) {
					wasPressed=sceneGL.respondKeyboard(keyEvent, keyDown,camera);

					if (sceneGL.topoGL!=null) {
						sceneGL.topoGL.handleKeyboard(keyDown);
					}
				}
			}

			// – – – – – – – – – – –– – – – – – – – – – –
			// Keyboard key responses
			// – – – – – – – – – – –– – – – – – – – – – –

			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
				Display.destroy();
				System.exit(0);
			}
			//  Collect camera positions
			else if (keyEvent==Keyboard.KEY_C && only && keyDown  && !isRepeat){
				Camera c= new Camera(camera);
				cameralist.add(c);		
				//			System.out.println(" camera list size =" + cameralist.size());
			}
			//  Clear List
			else if(keyEvent==Keyboard.KEY_C  && shift  && !control  && !alt){
				System.out.println("cameralist  size: "+cameralist.size());
				cameralist.clear();
				camindex= 0;			
			}
			// Open default camera list from file without dialogue
			else if(keyEvent==Keyboard.KEY_C && shift && control && keyDown){
				cameralist.clear();
				cameralist= ReadCamera.openCameraFile(null);
				camindex= 0;
			}
			// Saving camera list to file
			else if(Keyboard.isKeyDown(Keyboard.KEY_C) && shift && alt && keyDown){
				ReadCamera.savingCameraFile(cameralist);			
			}
			// Interpolate camera positions and animate
			else if (keyEvent== Keyboard.KEY_I  && keyDown ){
				useCameraList= !useCameraList;
				if (useCameraList && cameralist.size()>0) {
					interpolCamList= camera.interpolateCameras(cameralist);
				}
				else {
					interpolCamList= null;
				}			
			}
			// Toggle between camera positions
			else if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && keyDown  && !isRepeat){
				//			int n= Math.min(a, b);
				if (!cameralist.isEmpty()) {
					Camera c= cameralist.get(camindex);
					camera.setCamera(c);
					camindex++;
					camindex= (camindex>= cameralist.size()) ? 0 : camindex ;
				}
				camera.db= 0;
				camera.dw= 0;
			}
			// // Using light
			else if(Keyboard.isKeyDown(Keyboard.KEY_0)  &&  shift && keyDown ){
				useLight= !useLight;
			}
			// Testing –••–	manipulating texture image
			else if(Keyboard.isKeyDown(Keyboard.KEY_Q) && !isRepeat){
				Vector2D p  = camera.convertToGraphical(180) ;

				globe.changePixelsTransparent(p.getX(),p.getY(), p.getX()-10,p.getY()+10,image);

				//			tryChangePixels(p.getX(),p.getY(), p.getX()-10,p.getY()+10);
				// 
				redefineGlobeTexture();			
			}


			// Open distribution
			else if (Keyboard.isKeyDown(Keyboard.KEY_O)  && control && shift && keyDown) {
				String filename= FileUtility.openFileFromDialogue(pathNetCDF, "Open netCDF file…");
				if (!filename.isEmpty()) {
					// Open as a distribution
					netCDFPanel  = new NetCDFPanel(filename,null ,sceneGL.index);
					netCDFPanel.show(); 	// crocodile
				}
				keyDown= false;
			}
			// Use indicator
			else if ( keyEvent==Keyboard.KEY_N  && keyDown){
				useIndicator= !useIndicator;
			}


			// Help
			else if ( keyEvent== Keyboard.KEY_F1 && keyDown){
				useHelp = !useHelp;
			}
			// Mode controlling interaction
			else if ( keyEvent== Keyboard.KEY_F12 && keyDown){
				mode+= (mode==1) ? -1 : 1 ;
				System.out.println("mode:"+mode);
			}
			// Use a arrow keys for stepping days 
			else if (keyEvent==Keyboard.KEY_RIGHT && only && mode==1) {
				if(sceneGL.cfield!=null) {
					sceneGL.cfield.count(1);		
				}
			}
			else if (keyEvent==Keyboard.KEY_LEFT && only && mode==1) {
				if(sceneGL.cfield!=null) {
					sceneGL.cfield.count(-1);	
				}
			}
			//  CrossSection
			else if ( keyEvent==Keyboard.KEY_F2  && keyDown){
				CrossSectionInput csi= new CrossSectionInput(sceneGL.getCrossSection(), 
						vector2Point(mouseAt) ); 
			}

			// Set camera to look at the centre of topography–(move this to topography?)
			else if ( keyEvent==Keyboard.KEY_T  && control ){
				sceneGL.cameraToTopography(camera);
			}
			// Let the camera find a centre of mass of the distribution selected 
			else if ( keyEvent==Keyboard.KEY_D && control){
				if(sceneGL.selectedDistGL!=null) {
					Coordinate c= sceneGL.getSelectedDistGL().getCm(); // getGeoGrid().getCenter();
					camera.defineFromLatLong(c, Globe.Radius+camera.getHeight());
					camera.zoom(false , 2);
				}
				else if (sceneGL.agent!=null){
					Coordinate c = sceneGL.agent.getC();
					camera.defineFromLatLong(c, Globe.Radius+camera.getHeight());
				}
				useCameraFollowsCM= !useCameraFollowsCM;
			}
			// Leaves the spotlight where the camera light is
			else if (keyEvent==Keyboard.KEY_L  && shift) {
				spotLight.justLike(camera.light);
			}

			//  Once a key is down we may not want to respond repeatedly any more
			//  except for shift and control which we need to hold down
			keyDown= (shift||control ) ? true : false; 

			lastTime= System.currentTimeMillis();

			//		System.out.println("Resolution: lat:"+globe.getnLat()+ " lon:"+globe.getnLon());
		}
		// Key released
		else {
			keyDown = true ;	// Means that once the key is released it can be pressed Down again
			wasControl= false;
		}

		//	System.out.println("keyDown= "+keyDown);

		return ch;
	}



	/**
	 * Handles the mouse
	 */
	private void handleMouse(){
	
		boolean shift= isShift();
		boolean alt= isAlt();
		boolean control= isCtrl();
		//	boolean only= !shift && !alt && !control;
	
		boolean ifAction= (Mouse.next());
	
		int dx= Mouse.getDX();
		int dy= Mouse.getDY();
		//
		mouseAt.setxy(Mouse.getX(), Mouse.getY());
	
		if (ifAction){	

			//  Distribution
			sceneGL.respondMouse( camera);

			// Camera
			boolean needUpdate= camera.respondMouse(dx,dy) ;
			if (needUpdate) {
				setGLPerspective();
			}
		}
	}



	public void redefineGlobeTexture() {
		texture= getTextureFromImage(image);
		globe.setTexture(texture);
		globe.setDoRecreate(true);
	}


	public static Point vector2Point(Vector2D v){
		Point p= new Point((int)v.x, (int) v.y);
		return p;
	}

	/**
	 * Destroys the display and exit
	 */
	private static void cleanUp() {
		Display.destroy();
		System.exit(0);
	}


	/**
	 * Converting angles to radians
	 * @param angle the angle in degrees °
	 * @return the angle in radians
	 */
	public static float toRad(float angle){
		float rad= (float) ((angle/180)*Math.PI) ;
		return rad;
	}


	/**
	 * Converts latitude and longitude to a position in space
	 * @param lat 
	 * @param lon
	 * @return a Vector3D
	 */
	public  static Vector3D latLonToPoint(float lat,float lon){
		// First convert to radians
		lat= lat*PI/180;
		lon= 180-lon; // the texture starts on the other side…i.e. longitude 0 is at -x Axis
		lon= lon*PI/180;
		// 

		// 
		float L0  = (float) Math.cos(lat);  //  projection xz

		float x = (float) (Math.cos(lon)*L0);
		float y = (float) (Math.sin(lat)*1); // Radius=1
		float z=  (float) (Math.sin(lon)*L0); 

		Vector3D p= new Vector3D(x,y,z);	
		return p;
	}

	public static Vector3D coordinateToPoint(Coordinate c){
		Vector3D p= latLonToPoint(c.getLat(), c.getLon());
		return p;
	}





	/**
	 * Main	–	«•» «•» 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Starting OpenGL!");
		CoreOpenGL program= new CoreOpenGL();
	}



	@Override
	public void inputEnded() {
		// TODO Auto-generated method stub

	}



	@Override
	public void inputStarted() {
		// TODO Auto-generated method stub

	}



	@Override
	public boolean isAcceptingInput() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void setInput(Input input) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		System.out.println("clicking the mouse");

	}



	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		System.out.println("moving the mouse");

	}



	@Override
	public void mousePressed(int button, int x, int y) {
	}


	@Override
	public void mouseReleased(int button, int x, int y) {

	}



	@Override
	public void mouseWheelMoved(int change) {
		// TODO Auto-generated method stub

	}



	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("prison keyboard");
		// TODO Auto-generated method stub

	}



	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}



	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}



	/**
	 * Checks if the shift key is down
	 * @return true if shift is down
	 */
	public static boolean isShift(){
		boolean shift=Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		return shift;
	}



	public static boolean isCtrl(){
		boolean control=Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		return control;
	}



	public static boolean isAlt(){
		boolean alt= Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
		return alt;
	}



	/**
	 * @return the lastTime
	 */
	public long getLastTime() {
		return lastTime;
	}



	/**
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}



	/**
	 * @return the useLight
	 */
	public boolean isUseLight() {
		return useLight;
	}



	/**
	 * @param useLight the useLight to set
	 */
	public void setUseLight(boolean useLight) {
		this.useLight = useLight;
	}

	public Texture getTexture() {
		return texture;
	}



	public void setTexture(Texture texture) {
		this.texture = texture;
	}



	public int getMode() {
		return mode;
	}



	public void setMode(int mode) {
		this.mode = mode;
	}





}

//• – • How to draw text strings using slick() 
//String s= String.format("Count: "+ count);
//g.drawString(s, 5, 25);
//Color Color1= new Color(Color.blue);
//g.drawGradientLine(point.getX(), point.getY(), Color1, point2.getX(), point2.getY(), Color2);
//g.draw(circle);
//
//

/* Code to observe keystrokes which did not work out:
{
int ix=0;
// Check which key is first pressed
int keyEvent= Keyboard.getEventKey();
char ch= Keyboard.getEventCharacter();
wasControl= keyEvent==Keyboard.KEY_LCONTROL || keyEvent==Keyboard.KEY_RCONTROL;
boolean noKey= Keyboard.isKeyDown(Keyboard.CHAR_NONE);
// Special key allows entering coordinate
boolean wasW =   keyEvent==Keyboard.KEY_W;
wasCoo = wasCoo || wasW ;
int max = 7 ; //  e.g.: (-15 -30)
int length= characters.length();

Keyboard.enableRepeatEvents(!wasControl && !wasCoo);
//Keyboard.enableRepeatEvents(!wasCoo);


do {
	if (wasCoo  && !Keyboard.getEventKeyState() ) {
		if (!wasW || !noKey) {
			characters+= ch; //(wasW || noKey) ? "" : ch;
			System.out.println( Keyboard.getKeyName( Keyboard.getEventKey()) );
		}
		System.out.println("l= " + length+" characters: " + characters);
		if (length>= max || Keyboard.getEventKey()== Keyboard.KEY_RETURN) {
//			camera.stringToCoordinate(characters);
			wasCoo = false;
			characters  = "" ;
		}
	}
	else {
		respondKeyboard(ix);
	}
	ix++;

}while (Keyboard.next()  && ix<1 );	

// Updates the camera
setGLPerspective();

}
*/

//==================================================
/*

/**
 * Opens a netCDF file triggered from a keyboard shortcut (Ctrl+Shift+o)
 */
/*
private void openAndHandleNetCDF() {
	String initialDirectory="./res/"; //relative path to current directory
	JFileChooser fcd = new JFileChooser(initialDirectory);
	fcd.setDialogTitle("Open netCDF file...");
	fcd.showOpenDialog(this);

	// Return the file
	if (fcd.getSelectedFile()!=null) {
		String filename= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
		// Open as a survey line based on the name
		if (filename.contains("survey") && distribution!=null) {
			SurveyLines surveyLine= new SurveyLines(filename,distribution.getCoordinates());
			//setSurveyLine(surveyLine);					
			ecosystem.setSurveyLine(surveyLine);						
		}
		//Open as a distribution
		else{
			NetCDFPanel netCDFPanel = new NetCDFPanel(filename,this);
			netCDFPanel.show();
		}
	}
}


	// –	testing  
//			pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//			pixels =image.getRaster().getDataBuffer();
				
//			pixels = BufferTools.getByteData(image) ; // bytes
//			pixelBuffer =ByteBuffer.allocateDirect(pixels.length*4);
//			pixelBuffer=BufferUtils.createByteBuffer( pixels.length*4);
//			pixelBuffer = ByteBuffer.wrap(pixels);
//			System.out.println("Direct ByteBuffer:  "+Arrays.toString(pixelBuffer.array())); 
    



// 
 * Keyboard.poll();
  while (Keyboard.next()){
    int keyCode = Keyboard.getEventKey();
    char keyChar = Keyboard.getEventCharacter();
    boolean pressed = Keyboard.getEventKeyState();
    boolean down = Keyboard.isRepeatEvent();
    long time = Keyboard.getEventNanoseconds();
    KeyInputEvent evt = new KeyInputEvent(keyCode, keyChar, pressed, down);
    evt.setTime(time);
    listener.onKeyEvent(evt);
  }
 */

