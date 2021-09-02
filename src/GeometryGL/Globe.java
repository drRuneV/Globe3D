package GeometryGL;

import javax.script.CompiledScript;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.opengl.Texture;

import Colors.ColorGradient;
import Colors.ColorInt;
import Utility.Coordinate;
import basicLwjgl.Camera;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.Vector3D;
import distribution.Topography;
import Basic.Vector2D;


/**
 * 
 * @author Admin
 *
 */
public class Globe {

	public static float Radius  = 400 ;
	private float radius=400;
		
	private int nLat= 1024;
	private int nLon= 2048;
	private Texture texture= null;
	private Vector3D position= new Vector3D();
	private int displayList=-1;
	private boolean doRecreate= true;
	private Camera lastCamera= null;
	// 
	public float factor = 1;//   0.976f;
	public float factor2 =  1;// 0.977f ;
	// 
	private boolean useTexture= true;
	private boolean useFill=true;
	static float PI= (float) Math.PI;
	static float EARTHR=6371f;// km;
	//
//	private ColorGradient gradient= new ColorGradient(1, 0);
	//  Used to set the height on the surface
	private Topography topography= null;
	// Different step in different areas to optimise
	private byte[] latStep= null;
	private byte[][] lonStep= null;
	private boolean useCamera= false ;
//	private Camera camera= null;
	int keyTimeCount= 0;
	long lastTime= 0;
	//  Camera angle*distance when last recreated
	private float lastDistanceUpdate=0;
	// 
	static float heightScale=0.002f; // scaling of the height from topography 
	
	
	
	/**
	 * Constructor
	 * @param radius
	 * @param nLat
	 * @param nLon
	 * @param p
	 * @param tx
	 */
	public Globe(float radius, int nLat, int nLon, Vector3D p, Texture tx) {
		this.radius=radius;
		this.nLat=nLat;
		this.nLon=nLon;
		this.position.equals(p);
		this.texture = tx;		
		displayList= glGenLists(1);
		lastCamera=new Camera();
//		
	}
	
	
	/**
	 * Handling the drawing of the globe with a display list
	 */
	public void displayListHandler(Camera camera){

		// Texture
		if (useTexture && texture!=null) {
			glEnable(GL_TEXTURE_2D);
		}
		else {
			glDisable(GL_TEXTURE_2D);
		}

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glPolygonMode(GL_BACK,GL_FILL);
	    glPolygonMode(GL_FRONT, (useFill) ? GL_FILL:GL_LINE);
	    
	    // 
	    
	    
	    if (doRecreate) 	{
	    	glDeleteLists(displayList, 1);
	    	if (useCamera) {
	    		defineSteps2(camera);
	    	}
	    	else {
	    		defineSteps();
	    	}
	    	Vector3D[][] v= prepareVertexes();
	    	Vector3D[][] n= prepareNormals(v);
	    	Vector2D[][] t= preparedTextureCoordinates();
	    	//  Display List
	    	glNewList(displayList, GL_COMPILE_AND_EXECUTE);
	    	draw(v,n,t);
	    	glEndList();
	    	doRecreate=  false;
	    }
		else {
			glCallList(displayList);
		}
	    
	    doRecreate= (useCamera) ? determineRecreate(camera) : doRecreate;
 
	} 
	

	/**
	 * Determines whether we are going to recreate the globe based 
	 * on the change in the camera
	 * @param camera the current camera
	 * @return true if we need to change
	 */
	private boolean determineRecreate(Camera camera) {
		boolean y= false  ;
		Coordinate c = lastCamera.toCoordinate();
		float dla= 	Math.abs( c.getLat()- camera.toCoordinate().getLat());
		float dlo = Math.abs( c.getLon()- camera.toCoordinate().getLon() );
		float dh = 	Math.abs( lastCamera.getHeight()-camera.getHeight() );
		float da =  Math.abs( lastCamera.getAngle()- camera.getAngle() );
		float dc= 	Math.abs( (camera.getAngle()*camera.getDistance()) - lastDistanceUpdate) ;
		boolean theSame = (dc< 1);
		
		y= (dla>10 || dlo>20 || dh>50 || da>20)  && !theSame;		
				
		if (y) {
			lastCamera.setCamera(camera);
//			System.out.println(" Recreated GlobeGrid at "+ camera.getCoordinate().toString());
		}
		
		return y;
	}


	/**
	 * Drawing the globe
	 * @param factor
	 * @param factor2
	 */
	public void draw(Vector3D[][] v, Vector3D[][] n, Vector2D[][] t) {

		int i, j, la0;
		// •! related to intervals being less than number of latitudes (n-1)
//		float dty = 1.0f / (nLat-1); 
//		float dtx = 1.0f / (nLon);  //  step in texture , was: /nLon
		
		// • «» • Problem with texture is probably because it is not power of 2
		// • «» • By pure happenstance image.width/factor becomes 4000-> 4098 (4096 is correct)
		//   «»
		float dd=  factor; //  testing bug in texture
//		float ddd = factor2;
		//
		// Texture indexes
		float ty= 0, ty2= 0;
		float tx = 0;
		float x,y,z;
		float x1,y1,z1;
		
//		material();

		int from= latStep[0];
		int lastdy= from;
		//  latitude ......
		for(int laix = from; laix < nLat+1; laix+= latStep[laix] ){
			i= laix; 
			int dy = latStep[laix] ;
			//  Remember last step in latitude
			dy= (dy!= lastdy) ? lastdy : dy;
			lastdy= (laix>dy-1) ? latStep[laix-dy] : dy;
			//  Previous Latitude South/below
			la0 =  laix - dy;
			
			glBegin(GL_TRIANGLE_STRIP);
			// longitude
			for(int lojx=0; lojx < nLon+1; lojx+=lonStep[laix][lojx]) {

				j = (lojx==nLon) ? 0: lojx ; //Circular boundery – to connect triangle strip

				tx= t[laix][lojx].x;
				ty= t[laix][lojx].y;
				ty2=t[la0][lojx].y;
				
				x=v[i-dy][j].x; // Latitude below  
				y=v[i-dy][j].y;
				z=v[i-dy][j].z;
				x1=v[i][j].x;	// 
				y1=v[i][j].y;
				z1=v[i][j].z;

				//  «•» ! I had a problem with normal vectors here! «•»
				//  «•» fixed November 22. Normal vectors pre-calculated.
				// 	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	
				//P1 (page 111 in the OpenGL super Bible) //Label2
				glTexCoord2f(tx*dd, ty2*1);
				glNormal3f(n[la0][j].x, n[la0][j].y, n[la0][j].z);
				glVertex3f(x,y,z);

				// P2
				glTexCoord2f(tx*dd,ty*1);
				glNormal3f(n[i][j].x, n[i][j].y, n[i][j].z);
				glVertex3f(x1,y1,z1);
			}


			glEnd();

		}
	}
	
	public void defineSteps2(Camera cam){
		latStep= new byte[nLat+1] ;
		lonStep= new byte[nLat+1][nLon+1];
		float dla= 180.0f/(nLat+1); 
		float dlo= 360.0f/(nLon+1);
		Coordinate c= new Coordinate(cam.toCoordinate());
		
		byte g= (byte) Math.min(16, (nLat/16));// ( cam.isItClose(1.2f)) ? 
				//  (byte) Math.max(8,Math.min(16, (nLon/32)) ) : 32; // nLon512-> 16

		// 
		float angle= 40;// nLat/16;

		//  Latitude
		float latitude= 0;
		float dNorth= 0;
		for(int laix = 0; laix < nLat+1; laix++) {
			latitude= -90+laix*dla;
			latStep[laix]= 1;

			dNorth = Math.abs(latitude-c.getLat());
			// 
			if (cam.isItClose(0.5f)) {
				
				angle = (cam.isItClose(0.25f)) ? 15 : 30 ;
				if (dNorth> angle  ){// && laix%angle==0) {
					latStep[laix]=g;
				}
//				else if (dNorth> angle  ){//&& laix%2==0 ) {
//					latStep[laix]=2;	
//				}
				// ??Needs to be consistent with the step size g above ! otherwise gaps!  
				// because  Drawing starts at the South Pole !
				else {
					latStep[laix]=1;
				}
			}
			// Outside camera range -> course resolution
			else {// if (laix%g==0) {	
				
				latStep[laix]= (byte) ((cam.isItClose(1.5f)) ? g/2 : g);
			}
	
		
			// Longitude step depends both the longitude and latitude
			g= (byte) Math.min(16, (nLon/16));
			float longitude= 0;
			float dEast= 0;
			for(int loix = 0; loix < nLon+1; loix++) {
				longitude= 180-loix*dlo;
				dEast=Math.abs(longitude-c.getLon());
				lonStep[laix][loix] = 1;//  giveStep(general, loix);// general
				
				// 
				if (cam.isItClose(0.6f)) {
					angle = (cam.isItClose(0.3f)) ? 15 : 30 ;
					
					if (dEast> 80  && loix%8==0) {
						lonStep[laix][loix]= 8 ;
					}
					else if ((dEast> angle  && loix%4==0) || dNorth>angle) {
						lonStep[laix][loix]= 4 ;
					}
					else {
						lonStep[laix][loix]= 1 ;
					}
				}
				// Fixed grid further out, remember last distance to prevent global update then 
				else {// if (loix%g==0) {
					lonStep[laix][loix]=  (byte) ((cam.isItClose(1.5f)) ? g/2 : g); ;
					lastDistanceUpdate = cam.getAngle()* cam.getDistance() ;
				}
			}
		}
		
		// 
//		for(int laix = 0; laix < nLat+1; laix++) {
//			latitude= -90+laix*dla;
//			System.out.println(laix+":"+latitude+" step="+ latStep[laix]);
//		}

	}
	
	
	public  void defineSteps(Camera cam){
		latStep= new byte[nLat] ;
		lonStep= new byte[nLat][nLon+1];
		float dla= 180.0f/nLat; 
		float dlo= 360.0f/(nLon+1);
		float latitude= 0;
		float longitude= 0;		
		Coordinate c= new Coordinate(cam.toCoordinate());
		float dEast= 0;
		float dNorth= 0;
		// 
		byte general= (cam.getHeight()*cam.getAngle()> 25000) ? 
						32 : (byte) Math.max(8,Math.min(16, (nLon/32)));
		
		
		//  Latitude
		for(int laix = 0; laix < nLat; laix++) {
			latitude= -90+laix*dla;
			
//			latStep[laix]= giveStep(general, laix);
			latStep[laix]= 1;
			
			if (cam.determineDistance(1.2f)) {

				dNorth = Math.abs(latitude-c.getLat());
				// Very close
				if (dNorth< 10 ) {
					latStep[laix]=1;	
					//				System.out.println("north "+ dNorth);
				}
				else if (dNorth< 20 ) {
					if (latitude< 80  &&  laix%2==0 ) {
						latStep[laix]=2;	
					}
					else {
						latStep[laix]=1;
					}
					
				}
				else if (dNorth> 45 && laix%8==0) {
					latStep[laix]= (byte) 8;//Math.max(16 ,nLat/32);	
				}		
			}

			//  Longitude step depends both the longitude and latitude
			for(int loix = 0; loix < nLon+1; loix++) {
				
				lonStep[laix][loix] = giveStep(general, loix);// general
				
				longitude= 180-loix*dlo;
				dEast=Math.abs(longitude-c.getLon());

				if (cam.determineDistance(1.2f)) {


					if (dEast< 10 ){ //&& dNorth< 10){
						lonStep[laix][loix] = 1;	
					}
					else if (dEast <20 &&  loix%2==0) { //  && dNorth<10  
						lonStep[laix][loix] = 2;
					}
					else if (dEast <40 && dNorth<20 && loix%4==0) {
						//					lonStep[laix][loix] = 4;
					}
					else if (dEast> 90 ) {
						//					lonStep[laix][loix] = (byte) Math.max(16,nLon/16);
					}

					//				System.out.println(dEast);

				}
			}
		}
			
	}


	private void defineSteps(){
			latStep= new byte[nLat+1] ;
			lonStep= new byte[nLat+1][nLon+1];
			float dla= 180.0f/(nLat+1); 
			float dlo= 360.0f/(nLon+1);
			float latitude= 0;
			float longitude= 0;
			byte general= (byte) Math.min(16,(nLon/32));
			
			// Latitude
			for(int laix = 0; laix < nLat+1; laix++) {
				
				latStep[laix]=1;
				
				latitude= -90+laix*dla;
				
				//  starting at the South Pole
				if (latitude< -20 && laix%8==0){
					latStep[laix]=8;
				}
				else if (latitude< 20 &&  laix%4==0){
					latStep[laix]=4;
				}
				else if (latitude< 50 &&  laix%2==0){
					latStep[laix]=2;
				}

//				else if (latitude> 80) {
//					latStep[laix]=1;				
//				}
				
					
				//  Longitude step depends both the longitude and latitude
				for(int loix = 0; loix < nLon+1; loix++) {
					longitude= 180-loix*dlo;
					lonStep[laix][loix] = 1; // general
					//  Try Siberia
					if (longitude> 50  && longitude< 180  && latitude< 80   ) {

						if (longitude< 80 && latitude< 80  && loix%2==0) {
							lonStep[laix][loix] = 2;
						}
						else if (latitude< 20 && loix%4==0){
							lonStep[laix][loix] = 4;
						}
						else if ( loix%8==0) {
							lonStep[laix][loix] = 8;
						}
					}
					// Southern hemisphere
					else if (latitude< 0 && loix%4==0) {
						lonStep[laix][loix] =4;
					}
					// South of 40°
					else if (latitude< 40 && loix%2==0) {
						lonStep[laix][loix] =2;
					}
					// North Pole
					else  if (latitude> 88 && loix%16==0) {
						lonStep[laix][loix] =16;
					}
					// North of 84°	
					else  if (latitude> 84 && loix%8==0) {
						lonStep[laix][loix] =8;
					}
					// North of 80°
					else  if (latitude> 80 && loix%4==0) {
						lonStep[laix][loix] =4;
					}

				}
			}			
			
		}


	/**
	 * @param general
	 * @param ix
	 */
	public byte giveStep(byte general, int ix) {
		byte step=1;
		if (ix%general==0) {
			step=general;
		}
		else if(ix%general/2==0) {
			step=(byte) (general/2);
		}
		else if (ix%general/4==0) {
			step=(byte) (general/4);
		}
		else if (ix%general/8==0) {
			step=(byte) (general/8);
		}
		return step;
	}


	private Vector2D[][] preparedTextureCoordinates(){

		//  Remember that longitudes moves to the right on the map 
		// while the coordinate system rotates towards the left, x-axis towards the z-axis pointing outwards
		// … Therefore we had to address the texture from the right side

		
		Vector2D[][] texture= new Vector2D[nLat+1][nLon+1];
		// 	•! related to intervals being less than number of latitudes (n-1)
		float dty = 1.0f / (nLat-1+1); 
		float dtx = 1.0f / nLon;  //  step in texture 
		// Texture indexes
		float ty= 0,  tx = 0;

		// Latitude
		for(int laix = 0; laix < nLat+1; laix++ ){ 

			for(int lojx = 0; lojx < nLon+1; lojx++){ //nLon+1 becomes correct when drawing! 

				ty= 1-dty*laix ;  // Texture image starts at bottom
				tx = 1-dtx*lojx ; //Longitude goes opposite way of texture image !
				texture[laix][lojx] = new Vector2D(tx, ty);
			}
		}

		return texture;
	}

	
	/**
	 * Creates the normal vectors for each vertex
	 * @param v array of vertexes
	 * @return array of normal vectors
	 */
	private Vector3D[][] prepareNormals(Vector3D[][] v) {
		Vector3D[][] n= new Vector3D[nLat+1][nLon];
		float x0=0,  y0=0,  z0=0;
		float x=0, y=0, z=0;
		Vector3D normal= new Vector3D();

		// Latitude
		for(int laix = 0; laix < nLat+1; laix++) {

			for(int loix = 0; loix < nLon; loix++) {

				//  vertex
				x=v[laix][loix].x;	// 
				y=v[laix][loix].y;
				z=v[laix][loix].z;
				//  Special for the North Pole				
				if (laix==0) {
					n[laix][loix]= new Vector3D(x, y, z);
				}
				// General
				else {

					x0=v[laix-1][loix].x; // Latitude before/above  
					y0=v[laix-1][loix].y;
					z0=v[laix-1][loix].z;
					//  Three points in vertex define surface normal
					Vector3D p = new Vector3D(x0, y0, z0);
					Vector3D p1 = new Vector3D(x, y, z);
					// Circular
					Vector3D p2 =(loix< nLon-1) ? new Vector3D(v[laix][loix+1])  : 
						new Vector3D(v[laix][0]); 
					
					Vector3D d1= p.unitTo(p1);  //p -> p1
					Vector3D d2= p.unitTo(p2);	//p1 -> p2 
					// Normal vectors , using surface normals from crossproduct
					normal.equals(d1.cross(d2));
					n[laix][loix]=new Vector3D(normal);
				}
			} // lon
		} // lat


		return n;
	}

	/**
	 * Pre-calculates all the vertexes used for the surface of the globe
	 * @return a two-dimensional grid of (x,y,z) vertexes
	 */
	private Vector3D[][] prepareVertexes(){
		float lat1;
		float L1;
		float lng, x, y, z;
		float pi2= (float) (2*Math.PI);
		//  Define double list of vertexes 
		Vector3D[][] vertex = new Vector3D[nLat+1][nLon];

		float r = radius;	//  use this to incorporate topography !
		float h=0;		// height from topography
		
		
		//  Latitude
		for(int i = 0; i < nLat+1; i++) {
			// • «•»  (nLat-1)
			// there is one less interval (nLat-1) of latitudes than the number of latitudes!
			// • but then the coordinates does not seem to be right ..?
			// r = (float) ((float) radius-Math.random()*20);//(Math.cos(lng)*radius) ;
			lat1 = (float) (Math.PI * (-0.5f + (float) 1.0f*i / (nLat-1+1)));
			L1 =  (float) Math.cos(lat1)*r; // Horizontal projection


			//  Remember that longitudes moves to the right on the map 
			// while the coordinate system rotates towards the left, x-axis towards the z-axis pointing outwards
			// … Therefore we had to address the texture from the right side


			// •	Long	•	–	•	–	•	–	•	–	•	–	•
			for(int j = 0; j < nLon; j++) {
				// dLong 0..2PI 
				lng = (float)pi2 * (float) (j)/ nLon;
				// Spherical coordinates
				x = (float) (Math.cos(lng)*L1);

				y = (float) (Math.sin(lat1)*r);

				z= (float) (Math.sin(lng)*L1);

				vertex[i][j]=new Vector3D(x, y, z);
				if (topography!=null) {
					// !«•» remember the difference between the location in 
					//  the grid and the texture
					// PI-lng ends correctly !
					Coordinate c=new Coordinate(lat1, PI-lng,true );
					h= topography.getValueAtCoordinate(c)*heightScale;
					vertex[i][j].scaleVector((r+h)/r );
//					System.out.println(h+"=m"+" at "+c.toString());
				}

				//				System.out.println(i+" "+j+" XYZ "+x+" "+y+" "+z);
			}
		}


		return vertex;
	}

	
	/**
	 * Opens entire topography within given coordinates.
	 * This enables reading depth values interactively as well as changing the heights in 3D
	 * @param c1 coordinate from
	 * @param c2 coordinate to
	 */
	public void grabFullTopography(Coordinate c1,Coordinate c2){  
		String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
		String topoFile= "etopo2.nc";	// btdata

		//		String testName= "etopo1_bedrock 90 30 -30 30 .nc";	// Band1
		//		Topography topography= new Topography(path+testName,"Band1", c1,c2,1);

		topography= new Topography(path+topoFile,"btdata", c1,c2,2);

	}
	
	/**
	 * @param lat
	 * @param lon
	 * @param lat2
	 * @param lon2
	 * @param image 
	 */
	public void changePixelsTransparent(float lat, float lon, float lat2, float lon2, BufferedImage image) {
		Graphics2D g=(Graphics2D) image.getGraphics();
		//  pixel position given longitude
		int px = (int) ((lon+180)*(image.getWidth()/360.0f)); 
		int px2 = (int) ((lon2+180)*(image.getWidth()/360.0f));
		int py = (int) ((int) image.getHeight()- (lat+90)*(image.getHeight()/180.0f));
		int py2 = (int) ((int)image.getHeight()- (lat2+90)*(image.getHeight()/180.0f));
		// 
//		ColorInt transparentColor= new ColorInt(120,120,0,0);
//		int color= transparentColor.toInt();
		int theNewColour= 0;
		
		// it is possible to write something into the image we use as a texture for the globe 
		drawTextOnTexture(g,"Some text string");
		// 
		int index= 0;
		float opacity  = 0.0f ;
		System.out.println("pixelPosition: "+px+" : "+py);
		for (int x = px; x < px2; x++){
			for (int y = py; y < py2; y++) {
				theNewColour= ColorInt.transparencyFrom(image.getRGB(x, y), opacity); 
				image.setRGB(x,y,theNewColour);	
				//				BufferTools.putInBuffer(theNewColour, pixelBuffer);
				//				BufferTools.changePixel(index, col, pixels);
			}
		}
		//		pixelBuffer.flip();

		//		glTexSubImage2D(GL_TEXTURE_2D, 0,px,py, px2-px, py2-py, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);


		//		 for(int y = 0; y < image.getHeight(); y++){
		//             for(int x = 0; x < image.getWidth(); x++){
		//                 int pixel = pixels[y * image.getWidth() + x];
		//                 buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
		//                 buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
		//                 buffer.put((byte) (pixel & 0xFF));               // Blue component
		//                 buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
		//             }
		//         }
		//
		//         buffer.flip();

		//		glTexSubImage2D,
		//
		//		private ByteBuffer textureBuffer;
		//	     
		//	        textureBuffer = BufferUtils.createByteBuffer(512*512*4);
		//	        textureBuffer.order(ByteOrder.nativeOrder());           //is this line necessary.  I remove it and it does nothing!
		//	        for (int i = 0; i < 512*512; i ++) {
		//	            textureBuffer.put((byte) 0);
		//	            textureBuffer.put((byte) 0);
		//	            textureBuffer.put((byte) 255);
		//	            textureBuffer.put((byte) 255);
		//	        }
		//	        textureBuffer.flip();
		//
		
		
		setTexture(CoreOpenGL.getTextureFromImage(image) );
		setDoRecreate(true);			
	
	}


	/**
	 * Draws a text string on the image
	 * @param g Graphics handle to the image
	 */
	public void drawTextOnTexture(Graphics2D g, String s) {
		g.setFont(new  Font("Times", Font.BOLD, 20));
		g.setColor(Color.red);
		//		g.drawString(s, px, py);//image.getHeight()/2);
		//		g.drawOval(px, py-50, 50, 50);
	}
	
	
	/**
	 * Handling keyboard
	 * @param lastTime 
	 */
	public boolean handleKeyboard(int keyEvent, boolean keyDown){
		
		boolean control= CoreOpenGL.isCtrl();
		boolean alt= CoreOpenGL.isAlt();
		boolean shift= CoreOpenGL.isShift();
		long timeTookms= (long) ((System.currentTimeMillis()- lastTime)*1);
		keyTimeCount+= timeTookms  ;
		boolean didRespond= true;
		boolean wasRecently =  (keyTimeCount< 250 );//(timeTookms< 40);
		boolean isRepeat =Keyboard.isRepeatEvent() || wasRecently;
//		System.out.println(keyTimeCount+" ms "+timeTookms+" c:"+Keyboard.getEventCharacter()+
//				" repeating = "+isRepeat);

		doRecreate= true;
	
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Z) && keyDown ){
			useFill=!useFill;
			doRecreate= false;
		}
		// Using camera to define resolution 
		else if (Keyboard.isKeyDown(Keyboard.KEY_K)  && keyDown){
			useCamera=!useCamera;
		}
		else if (keyEvent==	Keyboard.KEY_T && keyDown){
			useTexture= !useTexture;
			doRecreate= false;
		}
		//				Factor:0.9759958 Factor2:0.97699577
		//  Resolution
		else if (Keyboard.isKeyDown(Keyboard.KEY_X)  && keyDown  ){ //&& !wasRecently){
			nLat= (int) ((shift) ?   nLat*2 : nLat*0.5f);
			nLon= (int) ((shift) ?	 nLon*2:  nLon*0.5f);
			nLat = Math.min(Math.max(64,nLat), 1024);
			nLon= Math.min(Math.max(64,nLat), 2048);
			System.out.println("Globe lat×long "+ nLat+" × "+ nLon+" ");
//			System.out.println(keyTimeCount+" ms "+Keyboard.getEventCharacter()+" repeating = "+isRepeat);
			keyTimeCount= (shift) ? 0: keyTimeCount ; //reset
		}
		else {
			doRecreate= false;
			didRespond= false;
		}
	
	
		nLat = Math.max(nLat, 16);
		nLon = Math.max(nLon, 16);
	
		lastTime = System.currentTimeMillis() ;
		
		return didRespond;
	}


	/**
		 * 
		 */
		public void material() {
			//  Color
	//		glEnable(GL_COLOR_MATERIAL);
	//		glColorMaterial(GL_FRONT_AND_BACK,GL_AMBIENT_AND_DIFFUSE);
		    glColor4f(0.60f, .60f, 0.70f ,1.0f);
		    glMaterialf(GL_FRONT, GL_SHININESS, 40);
	//
		}


	// ==================================================
	/**
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	/**
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

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
	 * @return the nLat
	 */
	public int getnLat() {
		return nLat;
	}

	/**
	 * @param nLat the nLat to set
	 */
	public void setnLat(int nLat) {
		this.nLat = nLat;
	}

	/**
	 * @return the nLon
	 */
	public int getnLon() {
		return nLon;
	}

	/**
	 * @param nLon the nLon to set
	 */
	public void setnLon(int nLon) {
		this.nLon = nLon;
	}


	/**
	 * @return the doRecreate
	 */
	public boolean isDoRecreate() {
		return doRecreate;
	}


	/**
	 * @param doRecreate the doRecreate to set
	 */
	public void setDoRecreate(boolean doRecreate) {
		this.doRecreate = doRecreate;
	}


	/**
	 * @return the topography
	 */
	public Topography getTopography() {
		return topography;
	}


	/**
	 * @param topography the topography to set
	 */
	public void setTopography(Topography topography) {
		this.topography = topography;
	}



}
