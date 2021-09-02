package basicLwjgl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Rectangle;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Quadric;
import org.lwjgl.util.glu.Sphere;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import static org.lwjgl.util.glu.GLU.*;

import GeometryGL.Globe;
import Basic.TextureImage;
import Utility.Coordinate;
import Utility.FileUtility;
import distribution.Distribution;
import distribution.Topography;
import ucar.nc2.NetcdfFile;


/**
 * Simple OpenGL Code for Testing Various Issues
 * All methods are static
 * @author Admin
 *
 */
public class OpenGLTesting {


public static Globe createsGlobe(Texture texture){
	
	Globe g = new Globe(400, 256, 512, new Vector3D(), texture) ;
	return g;
}

/**
 * Creates a topography within given coordinates using the etopo2.nc file
 * @param c1 upper left coordinate
 * @param c2 lower right coordinate
 * @return a topography object
 */
public static Topography createTopography(Coordinate c1,Coordinate c2){
//	Coordinate c1= new Coordinate(80, 0);
//	Coordinate c2= new Coordinate(60, 20);//180);

	String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
	String topoFile= "etopo2.nc";	// btdata

	//		String testName= "etopo1_bedrock 90 30 -30 30 .nc";	// Band1
	//		Topography topography= new Topography(path+testName,"Band1", c1,c2,1);

	Topography topography= new Topography(path+topoFile,"btdata", c1,c2,2);

	return topography;
}

public static Distribution testDistribution(){
	String name="HERbiom";	// "CFbiom";	
	// String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/" ;
	String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/November 2020/";
	
	String file="her.nc";	//ibm2felt 
	NetcdfFile ncfile = Distribution.testNetCDFFile(path,"her.nc");
	Distribution distribution= new Distribution(name, ncfile);
	return distribution;
}

public static Distribution testOpenDistribution(){
	String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/November 2020/";
	Distribution d= null;
	FileUtility.openAndHandleNetCDF(path,d);
	return d;
}






public static void drawTexture(){

//	loadTexture();
	

}
	
	
/**
 * Creates a pixel format we did 24 bits depth resolution
 * @return a pixel format
 */
static PixelFormat createPixelFormat() {
	PixelFormat pixelFormat= new PixelFormat(0, 24, 0);
	//		pixelFormat.withAlphaBits(8).withDepthBits(24);

	return pixelFormat;
}



/**
 * Get some information about the pixel format and display
 * @param pixelFormat the current pixel format used
 * @return a string specifying different information
 */
static String getPixelInformation(PixelFormat pixelFormat) {
	String s="Pixel: ";

	//		pixelFormat= Display.
	int alpha= pixelFormat.getAlphaBits();
	int bits=  pixelFormat.getBitsPerPixel();
	int depth=  pixelFormat.getDepthBits();
	int stencil=  pixelFormat.getStencilBits();
	String adapter = Display.getAdapter();

	//    	DisplayMode  mode= Display.getDisplayMode();
	s+= "alphaBits "+alpha+" bitsPerPixel "+bits+" depth "+depth+" stencil "+stencil+" ";
	s+= adapter;

	return s;
}







static void drawOrigin(float w, float opacity){


	//glDisable(GL_LIGHTING);
	glDisable(GL_TEXTURE_2D);


	//	glEnable(GL_COLOR_MATERIAL);
	//	glColorMaterial(GL_FRONT_AND_BACK ,GL_DIFFUSE);
	//
	Vector3D p= new Vector3D(-w, 1, -w);
	Vector3D pz= new Vector3D(-w, 1, w);
	Vector3D pxz= new Vector3D(w, 1, w);
	Vector3D px= new Vector3D(w, 1, -w);

	// Transparency «•»–«•»–«•»–«•» 
	glEnable(GL_BLEND);     
	glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);

	glColor4f( 0.5f,0.5f, 0.5f, opacity*0.2f);

	glBegin(GL_LINE_LOOP);
	{
		glVertex3f( p.x, p.y, p.z);
		glVertex3f( pz.x, pz.y, pz.z);
		glVertex3f( pxz.x, pxz.y, pxz.z);
		glVertex3f( px.x, px.y, px.z);
	}
	glEnd();

	glColor4f( 0.5f,0.9f, 0.5f, opacity);

	glPolygonMode(GL_FRONT, GL_FILL);
	//      glPolygonMode(GL_BACK, GL_FILL);

	glBegin(GL_QUADS);
	{
		glVertex3f( p.x, p.y, p.z);
		glVertex3f( pz.x, pz.y, pz.z);
		glVertex3f( pxz.x, pxz.y, pxz.z);
		glColor4f( 0.5f,0.5f, 0.9f, 0.250f);
		glVertex3f( px.x, px.y, px.z);
	}

	glEnd();

	glDisable(GL_COLOR_MATERIAL);
	glDisable(GL_BLEND);
}

/**
 * Draw coordinate system accesses
 * @param length the length of each axis
 */
static void drawAxis(float length){
	Vector3D xa= new Vector3D(length, 0, 0);
	Vector3D ya= new Vector3D(0, length, 0);
	Vector3D za= new Vector3D(0, 0, length);
	
	glLineWidth(0.01f);
	
	glDisable(GL_TEXTURE_2D);
	//x red
	glColor4f(1.0f,0f, 0f ,0.50f);
	glBegin(GL_LINES);
		glVertex3f(0,0,0);
		glVertex3f(xa.x,xa.y,xa.z);
	glEnd();
	//y green
	glColor4f(.0f, 1f, 0f ,0.50f);
	glBegin(GL_LINES);
		glVertex3f(0,0,0);
		glVertex3f(ya.x,ya.y*0.8f, ya.z);
	glEnd();
	//z blue
	glColor4f(.0f,0f, 1f ,1.0f);
	glBegin(GL_LINES);
		glVertex3f(0,0,0);
		glVertex3f(za.x,za.y,za.z);
	glEnd();
	
}

/**
 * 
 */
static void testTriangle(TextureImage textureImage) {
	
	float w= 300f;
	
	Color.white.bind();
	textureImage.getTexture().bind(); // or glBind(texture.getTextureID());
	
	
	 glDisable(GL_CULL_FACE);
	 glDisable(GL_LIGHTING);
//	 glEnable(GL_TEXTURE_2D);
	 //
      glBegin(GL_TRIANGLES);
      glColor3f(0.0f, 0.0f, 1.0f);
      glTexCoord2f(0,0);
      glVertex3f(-1*w, 0, -1*w);
      
      glColor3f(0.0f, 1.0f, 0.0f);
      glTexCoord2f(0,1);
      glVertex3f(1*w, 0, -1*w);
      
      glColor3f(1.0f, 0.0f, 0.0f);
      glTexCoord2f(1,0);
      glVertex3f(0.0f, w, -1*w);
      glEnd();
      
      glDisable(GL_TEXTURE_2D);
}

static void simpleSphere(){
	
	glPushMatrix();
//	glLoadIdentity();
	glTranslated(0, 60, 0);
	
	glColor3f(0.1f, 0.4f, 0.9f);
	Sphere s = new Sphere();
	s.draw(100.0f, 20, 16);
	
	glPopMatrix();
}

/**
 * @param count 
 * 
 */
static void drawSphere(long count, float radius, Vector3D position, Texture texture) {
	
	position = (position== null) ? new Vector3D() : position ;
	 
	  glEnable(GL_LIGHTING);
//      glEnable(GL_CULL_FACE);
      
      if (texture!=null) {
      setupTexture(texture);
      
//       tryQuadratic(texture);
    		   
      }
      
    //Sphere
//     glColor4f(0.80f, 0.40f, 0.0f, 0.8f);
     glMaterialf(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE, 1.0f);
     
      int facets= 32; 
	        Sphere s = new Sphere();            // an LWJGL class
	        s.setOrientation(GLU.GLU_OUTSIDE);  // normals point outwards
	        s.setNormals(GL_AUTO_NORMAL);
//	        s.setTextureFlag(true);             // generate texture coords
//	        s.setDrawStyle(GL_FILL);
	        
	        
	        glPushMatrix();
	        {
	        	float h= (float) Math.cos(count*0.1)*15*0 ;
	        	glTranslatef(position.x, position.y+h,position.z);
	        	glRotatef(-90f, 1,0,0);
	        	glRotatef(count*0.1f, 0,0,1);    // rotate the sphere to align the axis vertically
		        s.draw(radius, facets, facets);              // run GL commands to draw sphere
	        }
	        glPopMatrix();
}
	
	
/**
 * Draw a cylinder 
 * @param position
 * @param texture
 */
static void drawCylinder(Vector3D position, Texture texture){
		Cylinder cy= new Cylinder();
		int facets= 64;
		float radius=150;
		float h= radius*2;
	
		if (texture!=null) {
			setupTexture(texture);
		}
		else{
			glDisable(GL_TEXTURE_2D);
			
		}
	
		//  glColor4f(0.80f, 0.40f, 0.0f, 0.8f);
		cy.setOrientation(GLU.GLU_INSIDE);// OUTSIDE);  // normals point outwards
		cy.setNormals(GL_AUTO_NORMAL);
		cy.setTextureFlag(true);             // generate texture coords
		//	        s.setDrawStyle(GL_FILL);
	
		glPushMatrix();
		{
			//	        	float h= (float) Math.cos(count*0.1)*15*0 ;
			glTranslatef(position.x, position.y, position.z);
			glRotatef(90f, 1,0,0);
			//	        	glRotatef(*0.1f, 0,0,1);    // rotate the sphere to align the axis vertically
			cy.draw(radius, radius,h, facets,facets);              // run GL commands to draw sphere
		}
		glPopMatrix();
	}
	
	
/**
 * @param texture
 */
private static void setupTexture(Texture texture) {
	glEnable(GL_TEXTURE_2D);
//     Color.white.bind();
      glBindTexture(GL_TEXTURE_2D,texture.getTextureID()); 
//      glTexParameter();
      glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S, GL_CLAMP); //GL_CLAMP
      glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T, GL_CLAMP);
}



/**
 * 
 */
private static void tryQuadratic(Texture texture) {
	Quadric q= new Quadric();
	
	q.setNormals(GLU_SMOOTH);
	q.setTextureFlag(true);
	q.setOrientation(GLU_OUTSIDE);
//	q.
//  glBindTexture(GL_TEXTURE_2D, texID);    // texID is the texture ID of a
//                                           previously generated texture
//  gluSphere(q, 1.0f, 24, 24);
	

}




/**
 * @param texture
 */
public static void setupTextureAndCull(Texture texture) {
	// Texture
    if (texture!=null) {
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D,texture.getTextureID());
	}
    else {
    glDisable(GL_TEXTURE_2D);
    }
    
//    glDisable(GL_CULL_FACE);
       glEnable(GL_CULL_FACE);
    
//    glEnable(GL_COLOR_MATERIAL);
//    glColorMaterial(GL_FRONT_AND_BACK,GL_AMBIENT_AND_DIFFUSE);
////    glColor4f(1.0f, 0.9f, 0f ,1.0f);
//    glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS,60);
//    
    
}


/**
 * Makes a scissor display
 * @param r
 */
static void scissor(Rectangle r){
	glEnable(GL_SCISSOR_TEST);
	glScissor(r.x,r.y,r.width,r.height);
}



}
