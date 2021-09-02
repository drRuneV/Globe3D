package basicLwjgl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.util.glu.GLU.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Basic.Vector2D;
import GeometryGL.GeoGrid;
import GeometryGL.GeoLine;
import GeometryGL.Globe;
import Utility.Coordinate;
import ucar.ma2.ForbiddenConversionException;


/**
 * Camera class
 * @author Rune Vabø
 * •
 * 
 *
 */
public class Camera {

	String name = "" ;
	Vector3D position= null;
	Vector3D lookAt= null;
	Vector3D direction= null;
	private Coordinate coordinate= null;
	Coordinate lookAtCo= null;
	private float angle= 40;
	float dw= 0.02f;
	float db= 0.02f;
	private float floating= 0.01f ;
	private boolean useFloating= true;
	private float criticalDistance = 390;
	private float distance = 400 ;
	private float height= 100;
	private boolean useRotate= true;
	private boolean useGlide= true;
	private boolean useStepRotation= false ;
	private String text="";
	//
	Vector3D velocity= null;
	long count= 0;
	private long lastCount=0;
	//
	Light light= null;
	boolean useLight= true;
	
	
	
		
	
	/**
	 * Constructor
	 */
	public Camera(Coordinate coordinate, float height) {
		this.coordinate=coordinate;
		this.height = height;
		define();
		lookAt= new Vector3D(0,0,0);
		velocity= new Vector3D(-0.10f,-0.10f,-0.10f);
		direction=  new Vector3D();
		setupLight();
		
	}

	public Camera(Camera c) {
		setCamera(c);
	}
	
	public Camera() {
		position= new Vector3D();
		lookAt= new Vector3D();
		velocity= new Vector3D();
		direction= new Vector3D();
		light= new Light(2, new Vector3D(position), false, false);
	}
	
	public void define(){
		
		position = CoreOpenGL.latLonToPoint(coordinate.getLat(), coordinate.getLon());
		position.scaleVector(Globe.Radius+height);
	}

	
	/**
		 * 
		 */
		public void setupLight() {
			light= new Light(2, new Vector3D(position), true , true );
			light.ambient.blue=  90;
//			light.ambient.green= 40;
			light.spotCutoff= 70;
			light.spotExponent= 40;
	//		light.darken(0.2f);
		}

	public void setCamera(Camera cam){
		position= new Vector3D(cam.position) ;
		lookAt=  new Vector3D(cam.lookAt);
		velocity= new  Vector3D(cam.velocity);
		direction=new  Vector3D();
		coordinate= new Coordinate(cam.toCoordinate());
		angle= cam.angle;
		height = cam.height ;
	}
	
	/**
	 * Creates a new list of cameras by interpolating between 
	 * cameras in the given list
	 * @param cameralist list of cameras to interpolate between
	 * @return a new list of cameras ×100 larger
	 */
	public Camera[] interpolateCameras(ArrayList<Camera> cameralist){
		int n= 200;
		Camera list[]= new Camera[(cameralist.size()-1) *n] ;
		// 		
		for (int c = 1; c < cameralist.size(); c++) {
			//  Interpolate Position lookAt an angle
			Camera cam1= cameralist.get(c-1);
			Camera cam2= cameralist.get(c);
			createCameras(cam1, cam2, n, list, false , c);
		}
		
		
		return list;
	}
	
	/**
	 * Creates a list of cameras by interpolating between 2 cameras
	 * @param cam1 first camera
	 * @param cam2 2nd camera
	 * @param n number of cameras in between
	 * @param list the list of cameras to insert into
	 * @param extra 
	 * @param c index in the list
	 */
	public void createCameras(Camera cam1, Camera cam2, int n, Camera[] list, boolean extra, int c){
		Vector3D p0 = new Vector3D(cam1.position);
		Vector3D to= cam2.position.subtract(p0);
		Vector3D look0 = new Vector3D(cam1.lookAt);
		Vector3D lookto = cam2.lookAt.subtract(look0);
		float angle0= cam1.angle;
		float da= cam2.angle-angle0;
		float dh= cam2.height-cam1.height ;
		float  dlat= cam2.coordinate.getLat()-cam1.coordinate.getLat() ;
		float dlon= cam2.coordinate.getLon()-cam1.coordinate.getLon() ;

		float dx= 1.0f/n;
		// Create #n new cameras
		for (int i = 0; i < n; i++) {
			Camera camera= new Camera(cam1);
			camera.angle+= da*dx*i;
			camera.height+= dh*dx*i ;
			camera.coordinate.addLonLat(dlon*dx*i, dlat*dx*i);
			camera.define();
			camera.lookAt.plus(lookto.scaledTo(i*dx));
			list[(c-1)*n+i]= new Camera(camera);
		}

		// Redefine finer resolution for the first 10 points 
		int dn = 5 ;
		float dxx= dx/dn;

		for (int i = 0; i < dn; i++) {
			Camera camera= new Camera(cam1);
			camera.angle+= da*dxx*i;
			camera.height+= dh*dxx*i ;
			camera.coordinate.addLonLat(dlon*dxx*i, dlat*dxx*i);
			camera.define();
			camera.lookAt.plus(lookto.scaledTo(i*dxx));
			list[(c-1)*n+i]= new Camera(camera);
		}

	}

	
	
	//-------------------------SetCamera()---------------------------------------
	/**
	 * Set the camera using :org.lwjgl.util.glu
	 */
	public void setGLCamera()
	{
	 glMatrixMode(GL_MODELVIEW);
	 glLoadIdentity();
	 gluLookAt( position.x ,position.y, position.z,  lookAt.x, lookAt.y, lookAt.z,   0.0f, 1.0f, 0.0f);
	}
	
	
	/**
	 * Rotate
	 */
	public void rotate(){
		// Horizontal rotation
		float az= lookAt.getAzimut();
		float av = lookAt.getTilt();
		position.minus(lookAt);
//		 position.rotateHorizontal(-az);
//		 position.rotateVert(-av);		
		position.rotateHorizontal(dw);
//  		 position.rotateHorizontal(az);
//  		 position.rotateVert(av);		
		position.plus(lookAt);

		// # We should try to keep the height when rotating
		float h= position.length();
//		float dh= (position.length()/(height+ position.length()));
//		position.scaleVector(dh);
		
		// Direction
		direction= position.unitTo(lookAt);
		float tilt= direction.scaledTo(-1).getTilt();

		// Check singularity at the North and South Pole
		if (tilt>89.9 && db>0) {
			position.rotateVert(-0.1f);
			db*= -0.01f;
//			System.out.println("tilt:"+tilt+" db:"+db);
		}
		else if(tilt< -89.8  && db<0){
			db*=-0.01;
		}
		//  Normal rotation around the look at
		else {
			position.minus(lookAt);
			position.rotateVert(db);
			position.plus(lookAt);
		}
	}
	
	
	/** 
	 * @param shift
	 */
	public void zoom(boolean shift, float amount) {
		float sc= 0.999f;
		sc= (shift) ? -sc: sc;//(1/sc) : sc ;
		// Scaling camera moves it closer or away from the lookAt
		Vector3D to =  lookAt.subtract(position);
		if (lookAt.notZero()) {
			position.plus(to.normalised().scaledTo(sc*amount));  //  June 15
		}
		
		// Velocity , to keep moving smoothly
		float d= (float) Math.min( Math.max(0.1f,(distance*distance)/(1000*1000)), 10);
		Vector3D  u= to.normalised().scaledTo(1*d*amount);
		if (shift) u.scaleVector(-1*d);
		velocity.plus(u);			
	}

	/**
	 * @param sf
	 * @param dt 
	 */
	public void move(float s) {
		float d= Globe.Radius/(100+distance) ;
		float sf= Math.max(0.96f, 0.999f- (0.05f*d*d) );
			
				// Math.min(0.99f , 1.0f - 1/(d) ));
		float dt = CoreOpenGL.dT ;
		
//		sf = Math.min(4, sf);
		
		// Damping speed 
		velocity.scaleVector(1*sf);
		if (count%100==0) {
//		 System.out.println(" camera moving with: "+sf+" d:"+d);
		}
		
		
		position.plus(velocity.scaledTo(1*dt*sf)); // p+= v*dt
		// Constrains 
		if (distance < criticalDistance ){ 
			velocity.scaleVector(-0.5f);
			position.plus(lookAt.unitTo(position).scaledTo(2));
		}
		else if (distance> 20* Globe.Radius) {
			velocity.equals( position.normalised().scaledTo(-200)) ;
		}
		// 
		if (velocity.length()>20 && height< Globe.Radius*2) {
			velocity.scaleVector(0.9f);
		}
		//   Avoid inside globe
		if (height< 1){
			position.scaleVector(1.01f);
			velocity.scaledTo(-0.1f);
			db= 0.01f;
		}											
	}

	/**
		 * Updates the position and other parameters of the camera
		 * … Called from the render loop in the core OpenGL
		 */
		public void update(){
			
			// Height above surface & distance from the Look at
			height= position.length()-Globe.Radius;
			distance=position.subtract(lookAt).length();
			coordinate = toCoordinate() ;
			// test
	//		Coordinate c = new Coordinate(60, 0.0f);
	//		float d=  GeoLine.calculateDistance(c, coordinate) ;
	//		System.out.println(" Distance to "+c.toString()+" = "+d+" km");
			
			
			// Scaling and constrain
			float tilt = position.getTilt() ;
			float l= (lookAt.length()==0) ? 10 : 1 ;
			float d= distance-getCriticalDistance() ;
			float sf= Math.max(0.9f,(1.0f - l/(l+d) ));
			
			// Constrains close to the North/South Pole
			db= (( (coordinate.getLat())>87||tilt> 87)  && db>0 )|| (coordinate.getLat()<-87 && db<0)
					? Math.min(Math.max(-0.02f,db), 0.02f) : db;
			// 
			db= Math.min(Math.max(-5, db), 5);
			dw= Math.min(Math.max(-5, dw), 5);
			
					
			// Rotate
			rotate();
			// Continuous rotation
			dw*= (useRotate) ?  1 : (useGlide) ?  0.999f*sf : 0;			
			db*= (useRotate) ?  1 : (useGlide) ?  0.999f*sf : 0; 
			
			// Move		
			move(sf);
			// 
			angle = Math.max(10, angle);
			angle = Math.min(95, angle);
			
			setGLCamera();
			count++;
			
			//  Light
			updateLight(Globe.Radius);
			// Text
			setText(info());
		}

	/**
	 * @param radius
	 */
		public void updateLight(float radius) {

			Vector3D p= position.scaledTo(1.1f);
			//			p.rotateHorizontal(30);
			//  
			if (count== 2) {
				light.setFade(.0f, 0.0010f, .000001f);
				light.darken(0.1f);
				light.lightProperties();
			}
			float d= p.length();
			if (d<radius) {
				p.scaleVector(radius/d);
			}
			light.position.equals(p);
			light.direction.equals(p.scaledTo(-1));

			light.update(count);
			if (useLight) {
				glEnable(GL_LIGHT2);
			}
			else {
				glDisable(GL_LIGHT2);
			}
	}

	
	public void stringToCoordinate(String s){
		float lat= 0, lon=0;
//		s.substring(1);
		String[]  st = s.split(" ", 2);
		lat= Float.parseFloat(st[0]);
		lon= Float.parseFloat(st[1]);
		Vector3D p= GeoGrid.latLonToPoint(lat, lon);
		setPosition(p.scaledTo(distance));
	}
	
	public void defineFromLatLong(Coordinate c,float radius){
		Vector3D p = CoreOpenGL.latLonToPoint(c.getLat(), c.getLon());
		p.scaleVector(radius);
		position.equals(p);
	}


	
	/**
	 * Creates a coordinate of this camera position
	 * @return a new coordinate
	 */
	public Coordinate toCoordinate(){
		Vector2D v= convertToGraphical(180);
		Coordinate c = new Coordinate(v.x, v.y) ;
		return c;
	}
	
	/**
	 * Converts position  vector to corresponding latitude and longitude
	 * Must correct for the offset of longitude corresponding to the texture
	 * @return
	 */
	public Vector2D convertToGraphical(float offset){

		Vector3D u = position.normalised();
		
		float lat= (float) Math.asin(u.y); //R=1 since the unit vector 
		float lon= (float) Math.asin(u.z/Math.cos(lat)) ; //  Vector3D at line 365 -90..90
				
		//  Convert from radians
		lat*= 180/(Math.PI);
		lon*= 180/(Math.PI);
		// Longitude must be treated separately
		if (u.x<0) {
			lon = 180- lon ;
		}
		if (lon<0) {
			lon+=360;
		}
		
		//  Remember that the OpenGL longitude rotates westward while opposite for the globe
		//  In addition the Texture starts at -180°… 0 …  180°
		
		//  Consider texture offset
		lon= offset-lon;// lon= 180-lon
		
		Vector2D p =  new Vector2D(lat,lon);
		return p;
	}

	public static Coordinate coordinateFromPoint(float offset,Vector3D p) {
		Vector3D u= p.normalised();

		float lat= (float) Math.asin(u.y); //R=1 since the unit vector 
		float lon= (float) Math.asin(u.z/Math.cos(lat)) ; //  Vector3D at line 365 -90..90

		//  Convert from radians
		lat*= 180/(Math.PI);
		lon*= 180/(Math.PI);
		// Longitude must be treated separately
		if (u.x<0) {
			lon = 180- lon ;
		}
		if (lon<0) {
			lon+=360;
		}

		//  Remember that the OpenGL longitude rotates westward while opposite for the globe
		//  In addition the Texture starts at -180°… 0 …  180°

		//  Consider texture offset
		lon= offset-lon;// lon= 180-lon

		Coordinate c = new Coordinate(lat, lon);
		return c;
	}
	
	public boolean isItClose(float f){
		boolean isCloser = 0.001f*angle*height< 20*f ; // 50° and h=200 -> 10,000
//		System.out.println(angle+"×"+height+"<"+20000*f+" ? = "+isCloser);
		return isCloser;
	}

	public boolean determineDistance(float f){
		
		
		return (getDistance()< (1000*f- getAngle()*10) ); 
	}
	
	/**
	 * 
	 */
	public void defineTheDefault() {
		lookAt.setZero();
		useRotate=false;
		db= 0;
		dw= 0;
		setCriticalDistance(Globe.Radius-5) ;
	}

	public String information() {
			String info  = " Camera " ;
			//  info+=String.format("%.2f ° pos %.2f %.2f %.2f ", angle, position.x, position.y,position.z);
			info+=String.format("%.2f ° Distance %.2f   Height %.2f", angle,distance,height);
			info+=String.format(" Lat %.2f Lon %.2f " , coordinate.getLat(), coordinate.getLon()); 
			return info;
		}

	/**
	 * Creates a string with camera information
	 * @return
	 */
	public String info(){
		Vector2D lalo = convertToGraphical(180) ;
		String s= String.format("Cam: %.2f °N %.2f °E  a=%.2f°  h=%.1f", lalo.getX(), lalo.getY(),angle,height);
		return s;
	}

	/**
	 * Creates a string of short information about latitude and longitude
	 * @return 
	 */
	public String shortInfo(){
		Vector2D lalo = convertToGraphical(180) ;
		String s= String.format("%.1f °N %.1f °E", lalo.getX(), lalo.getY());
		return s;
	}

	/**
		 * 
		 * @param keyEvent
		 * @param keyDown 
		 */
		public boolean respondKeyboard(int keyEvent, boolean keyDown,int mode){
	
			boolean shift = CoreOpenGL.isShift();
			boolean alt= CoreOpenGL.isAlt();
			boolean control= CoreOpenGL.isCtrl();
			boolean only= !shift && !alt && !control;
			boolean wasPressed= true;
			boolean useArrows=(mode==0);
	
			float f= floating ; // The amount of continuous floating 0.08f;
			 // Factor to slow down rotation
			float whenRotating = (useRotate) ? 0.2f : (useGlide) ? 1: 2 ;
			
	
//			System.out.println(keyDown+" key: "+Keyboard.getEventCharacter()+" "+ keyEvent+
//					"->"+Keyboard.KEY_R);
//			
			// Automatic rotation
			if (keyEvent==Keyboard.KEY_R && only && keyDown) {
				useRotate = !useRotate;
				dw= 0.02f;	// Need some rotation to start with
			}
			//  Automatic gliding
			if (keyEvent==Keyboard.KEY_G && only && keyDown) {
				useGlide = !useGlide ;
			}
			
			
			// Amount of response
			else if(keyEvent==Keyboard.KEY_F && keyDown  && !alt && !control){
				floating+= (shift)  ? -0.001f : 0.002f;
				floating = Math.max(Math.min(floating,0.99f ), 0.01f) ;
				System.out.println("floating: "+floating);
			}
			//  Use Light
			else if (keyEvent==Keyboard.KEY_L && only  &&  keyDown) { 
				useLight= !useLight ;
			}
			// Light response
			else if (keyEvent==Keyboard.KEY_E  && keyDown){
				 
//					light.ambient.shiftRGB(0, 5, 15); // testing
					//			light.specular.shiftRGB(10, 0, 0);
					light.fade[1]*= (shift) ? 0.9f: 2f; 
					light.fade[2]*= (shift) ? 0.9f: 2f;
					light.lightProperties();
//					light.update(count);
			}

			//  Location is a reference point for moving to specific locations
			else if (keyEvent==Keyboard.KEY_F10 && only) {
				position= CoreOpenGL.latLonToPoint(80, 0);
				position.scaleVector(500);
				angle = 50;
				dw=0;
				db=0;
				useRotate= false;
				useGlide= false;				
			}
			// Step rotation is for enabling voice interaction !
			else if (keyEvent==Keyboard.KEY_F10 && shift) {
				useStepRotation=! useStepRotation;
				System.out.println("usestepRotation= "+useStepRotation);
			} 
			
			// rotating 1° East
			else if (keyEvent==Keyboard.KEY_RIGHT && alt) {
				coordinate.addLonLat(1, 0);
				define();
			} 
			// rotating 1° left
			else if (keyEvent==Keyboard.KEY_LEFT && alt) {
				coordinate.addLonLat(-1, 0);
				define();
			} 
			// rotating 1° North
			else if (keyEvent==Keyboard.KEY_UP && alt) {
				coordinate.addLonLat(0, 1);
				define();
			} 
			// rotating 1° South
			else if (keyEvent==Keyboard.KEY_DOWN && alt) {
				coordinate.addLonLat(0, -1);
				define();
			} 

						
			// 	Rotation
			else if (keyEvent==Keyboard.KEY_LEFT && only && useArrows) {
				if (useStepRotation) {
					coordinate.addLonLat(-1, 0);
					coordinate.constrains();
					define();
				}
				else {
					dw-=-f*2*whenRotating; // because 360° is twice as 180°
				}
			}
	
			else if (keyEvent==Keyboard.KEY_RIGHT && only && useArrows) {
				if (useStepRotation) {
					coordinate.addLonLat(1, 0);
					coordinate.constrains();
					define();
				}
				else {
					dw+=-f*2*whenRotating;
				}
			}
			else if (keyEvent==Keyboard.KEY_UP && only ) {
				if (useStepRotation) {
					coordinate.addLonLat(0, 1);
					coordinate.constrains();
					define();
				}
				else {
					db+= f*whenRotating;
				}
			}
			// 
			else if (keyEvent==Keyboard.KEY_DOWN && only ) {
				if (useStepRotation) {
					coordinate.addLonLat(0, -1);
					coordinate.constrains();
					define();
				}
				else {
					db+= -f*whenRotating;
				}
			}
			
			// Camera angle 
			else if (keyEvent==Keyboard.KEY_UP && shift) {
				angle++;
			}
			else if (keyEvent==Keyboard.KEY_DOWN && shift) {
				angle--;
			}

			// Zoom
			else if (keyEvent==Keyboard.KEY_SPACE ) {
				zoom(shift, (control) ? 16: 4);
			}
			else if (keyEvent==Keyboard.KEY_B){
				zoom(true,6f);
			}
			//  
			else if (keyEvent==Keyboard.KEY_LEFT && keyDown && control ){
	//			float sc= (Keyboard.isRepeatEvent()) ? 0.04f: 0.02f;
				lookAt.rotateHorizontal(1);
	//			position.rotateHorizontal(45);
			}
			else if (keyEvent==Keyboard.KEY_RIGHT && keyDown && control){
	//			float sc= (Keyboard.isRepeatEvent()) ? 0.04f: 0.02f;
				lookAt.rotateHorizontal(-1);
	//			position.rotateHorizontal(-45);
			}
			// lookAt Rotate
			else if(keyEvent== Keyboard.KEY_UP  && control ){
				lookAt.rotateVert(1);
			}
			else if( keyEvent== Keyboard.KEY_DOWN  && control ){
				lookAt.rotateVert(-1);
			}
			
			
			//  Resetting camera to centre of Globe
			else if ( (keyEvent==Keyboard.KEY_HOME || keyEvent==Keyboard.KEY_H) && keyDown && only){
				if (lookAt.notZero()) {
					coordinate = Camera.coordinateFromPoint(180, lookAt) ;
					define();
					lookAt.setZero();
					velocity.setZero();
					db= 0;
					dw= 0;
					setCriticalDistance(Globe.Radius-5) ;
				}
			}
			//  Setting camera to look at surface
			else if (keyEvent==Keyboard.KEY_A && keyDown && only){
				lookAt.equals(position.normalised().scaledTo(Globe.Radius));
				setCriticalDistance(5) ;
			}
			// stop
			else if (keyEvent==Keyboard.KEY_S && only){
				db= 0;
				dw= 0;
				useRotate=false;
			}
	
	
			//  Camera position using geographical coordinates
			// Norway
			else if(keyEvent==Keyboard.KEY_1){
				defineTheDefault();
				coordinate.setLat(65);
				coordinate.setLon(0); // to prevention 
				position= CoreOpenGL.latLonToPoint(65, 0);
				position.scaleVector(700);
				angle = 50 ;
				System.out.println("co:"+coordinate.toString()+" p:"+position.x);
			}
			// Barents Sea
			else if(keyEvent==Keyboard.KEY_2){
				defineTheDefault();
				position= CoreOpenGL.latLonToPoint(75, 10);
				position.scaleVector(600);
				angle = 50 ;    	
			}
			//  Africa
			else if(keyEvent==Keyboard.KEY_3){
				defineTheDefault();
				useRotate=true ;
				position= CoreOpenGL.latLonToPoint(0, 0);
				position.scaleVector(1000);
			}
			// Europe
			else if(keyEvent==Keyboard.KEY_4){
				defineTheDefault();
				position= CoreOpenGL.latLonToPoint(40, 10);
				position.scaleVector(600);
				angle = 60 ;    	
			}
			// Atlantic
			else if(keyEvent==Keyboard.KEY_5){
				defineTheDefault();
				position= CoreOpenGL.latLonToPoint(45, -30);
				position.scaleVector(700);
				angle = 40 ;    	
			}
			// North Pole
			else if (keyEvent==Keyboard.KEY_6){
				defineTheDefault();
				dw = 0.1f ;
				db = 0 ;
				useRotate=true ;
				position= CoreOpenGL.latLonToPoint(88, -50);
				position.scaleVector(800);
			}
			//  Norwegian coast
			else  if(keyEvent==Keyboard.KEY_7){
				
				useRotate=false;
				db= -0.1f;
				position= CoreOpenGL.latLonToPoint(61, 3);
				position.scaleVector(500);
				angle = 60 ;
				lookAt.equals(position.normalised().scaledTo(400));
				setCriticalDistance(5) ;			
			}
				
			// Canada
			else if (keyEvent==Keyboard.KEY_8){
				defineTheDefault();
				position= CoreOpenGL.latLonToPoint(70, -170);
				position.scaleVector(600);
				angle = 50 ;    	
			}
			else {
				wasPressed= false;
			}
			
	
			return wasPressed;
		}

	/**
	 * Camera response to Mouse interaction
	 */
	public boolean respondMouse(int dx,int dy){
		
		boolean shift= CoreOpenGL.isShift();
		boolean alt= CoreOpenGL.isAlt();
		boolean control= CoreOpenGL.isCtrl();
		boolean only= !shift && !alt && !control;
		boolean needUpdate= false;
		float whenRotating = (useRotate) ? 0.2f : 1 ;

		int max= (useRotate) ? 4 : 10;
		dx= Math.min(Math.max(dx,-max), max);
		dy= Math.min(Math.max(dy,-max), max);
		int sx= (dx<0) ? -1 : 1;
		int sy= (dy<0) ? -1 : 1;


		/**  Mouse Left–Down*/
		if (Mouse.isButtonDown(0) ){

			dw+=sx*dx*dx*0.0005f;
			db-=sy*dy*dy*0.0005f;
		}
		// MouseWheel  
		int dw=Mouse.getDWheel();
		if (dw!=0 && !alt) {
			if (shift) {
				respondMouseWheel(dw);
			}
			else if(only){
				zoom((dw>0),5);
				needUpdate= true;
			}
		}
		return needUpdate;
	}
	
	/**
	 * Response to the mouse wheel
	 * @param dx
	 */
	public void respondMouseWheel(int dx){
		angle+=-dx/120f;
		angle= Math.max(angle, 5);
		angle= Math.min(angle, 120);
		
	}
	
	

	// ==================================================
	
	/**
	 * @return the position
	 */
	public Vector3D getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Vector3D position) {
		this.position = position;
	}

	/**
	 * @return the lookAt
	 */
	public Vector3D getLookAt() {
		return lookAt;
	}

	/**
	 * @param lookAt the lookAt to set
	 */
	public void setLookAt(Vector3D lookAt) {
		this.lookAt = lookAt;
	}

	/**
	 * @return the angle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * @param angle the angle to set
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * @return the velocity
	 */
	public Vector3D getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector3D velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the distance
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}

	/**
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return the criticalDistance
	 */
	public float getCriticalDistance() {
		return criticalDistance;
	}

	/**
	 * @param criticalDistance the criticalDistance to set
	 */
	public void setCriticalDistance(float criticalDistance) {
		this.criticalDistance = criticalDistance;
	}

	public boolean isOutOfSight(Coordinate center) {
		boolean yes= false;
		Vector3D p= CoreOpenGL.latLonToPoint(center.getLat(), center.getLon());
		float a= position.getAngleBetween(p);
//		System.out.println(" Angle between : "+a+"°");
		
		yes = (a>angle) ;
		
		return yes;
	}

	/**
	 * @return the useFloating
	 */
	public boolean isUseFloating() {
		return useFloating;
	}

	/**
	 * @param useFloating the useFloating to set
	 */
	public void setUseFloating(boolean useFloating) {
		this.useFloating = useFloating;
	}

	/**
	 * @return the coordinate
	 */
	public Coordinate getCoordinate() {
		return coordinate;
	}

	/**
	 * @param coordinate the coordinate to set
	 */
	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	/**
	 * @return the count
	 */
	public long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(long count) {
		this.count = count;
	}

	

}
