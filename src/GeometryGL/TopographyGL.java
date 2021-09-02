package GeometryGL;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import Colors.ColorInt;
import Utility.Coordinate;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.Vector3D;
import distribution.Distribution;
import distribution.Topography;


public class TopographyGL extends GeoGrid{

	Topography topography= null;	
	private int resolution=4;
//	private int[][] dlist;
	private TopoGrid[] topoGrid= null;
//	private ArrayList<TopographyGL> topoList= new ArrayList<>();
	
	ColorInt[] colors=null;

	// Slick image to convert to the gradient image
	Image image;
	// private int listbase=0;
	
/**
 *
 */
class TopoGrid{
	Rectangle r;
	int dlist=-1;
	Coordinate center;
	int res=1;
	TopoGrid[] subGrid= null;
	int listbase=0;

	
	/**
	 * Constructor
	 * @param r rectangle defined the index inside the topography
	 * @param res resolution
	 * @param dlist the display list identifier
	 */
	public TopoGrid(Rectangle r,int res,int dlist) {
		this.r=r;
		this.res= res ;
		this.dlist = dlist ;
		calculateCentre();
		if (res>=2){
	//				int baseList  =  glGenLists(1); // (dlist)*4+1 ;
		subGrid= defineLOD(subGrid,res, new Point(r.x,r.y),r.width,r.height);   //  res/2
		System.out.println("res: "+res+" created inside grid "+" subGrid: "+subGrid);
		}
	}
		
	public String info(){
		String s="res "+res+" rec "+r.x+":"+r.y+":"+r.width+":"+r.height;
		return s;
	}

		
/**
 * Calculates the centre of the topoGrid in geographical coordinates.
 */
	public void calculateCentre(){
		Coordinate upperLeft= new Coordinate(coordinate[r.x][r.y]);
		Coordinate lowerRight= new Coordinate(coordinate[r.x+r.width-1][r.y+r.height-1]);
		float lat= (upperLeft.getLat() + lowerRight.getLat())/2;
		float lon= (upperLeft.getLon()+ lowerRight.getLon())/2;
		center= new Coordinate(lat, lon);
	}

	public boolean callTheList(float d, Vector3D camPos){
		boolean useSmaller= false;
	
		// Calculate distance 									
		Vector3D p=CoreOpenGL.latLonToPoint(center.getLat(),center.getLon() ).scaledTo(radius);
		float distance  =  camPos.subtract(p).length();
		int count= 0;
		
		//  Call the innermost lists with high resolution if they exist
		if (distance< d) {
			if (subGrid!=null){
				for (TopoGrid grid : subGrid) {
				// 	useSmaller =  useSmaller || 
					useSmaller= grid.callTheList(d*0.8f, camPos);
				}
			}
			// Call the top layer list
			else {
				glCallList(dlist);
				useSmaller=true ;
//				System.out.println(" Just called the innermost list "+ res+" dlist="+dlist);
			}
		}
	
		return useSmaller;
	}
		
	/**
	 * Delete all the display list including the nested grids
	 * Recursion occurs in this method. «•»–«•»
	 */
	public void deleteDisplayList(){
		glDeleteLists(dlist ,1);
		// Nested grids
	if ( subGrid!=null) {
		for (TopoGrid grid : subGrid) {
			glDeleteLists(grid.dlist ,1);
			grid.deleteDisplayList();	//  Recursion !«•» 
			}
		}
	}

/**
 * Create a new display list and call the render() method.
 * The portion/region rendered is the content of the display list
 * Thus the render method is only called when creating display list.
 */
	public void createDisplayList() {
		//  Display List
	glNewList(dlist, GL_COMPILE_AND_EXECUTE);
	render(res,r);
	glEndList();
	System.out.println("Created a display list with resolution:"+res);
	
	//  Create the display lists of the sub grids nested within this grid
	if (subGrid!=null) {
		for (TopoGrid grid : subGrid) {
			grid.createDisplayList();	// Recursion for sub grids inside this topo
			System.out.println(" Created innermost list" + " res="+res);
			}
		}
	
	}
	}
	
	
/**
 * Constructor
 * @param topography the topography to be used
 * @param width the width of the topography – longitude
 * @param height the height of the topography – latitude
 * @param radius the distance from the origin corresponding to the radius of the globe
 * @param resolution the number of subdivisions of the grid, resolution×resolution
 */
public TopographyGL(Topography topography,int width, int height, float radius,int resolution) {
		super(width, height, radius);
		this.topography = topography ;
		this.resolution=resolution;
		
		// Displaylist
		displayList= glGenLists(1);
		
		
		// Create a  two-dimensional coordinate grid 
		coordinate=createCoordinates(topography.getLatitude(), topography.getLongitude());
		// Grid positions for the topography; all points are considered 
		// it is the rendering that shifts resolution
		createVertexesWithHeights(topography.getValues(), Globe.heightScale);//0.0025f);
//		float scale=0.002f; // scaling of the height from topography

		// Create several topo–grids , the number of the innermost is given by resolution×resolution
		// Each level is 2×2
		topoGrid=defineLOD(topoGrid,resolution, new Point(0,0),width,height);
//		System.out.println(topoGrid);

		
		// Gradient
		defineGradient(2300, -4200,"topo4 d2.txt");// aOcean1.txt");
		gradient.setUseOpacityMask(false);
		convertGradientToImage();
		// Create Colors
		colors=createColorArray();
		

//		displaySomeValues();
	}
	
	
	/**
	 * Constructor
	 * @param coordinate
	 * @param width
	 * @param height
	 * @param radius
	 */
//	public TopographyGL(Coordinate[] coordinate, int width, int height, float radius) {
//		super(coordinate, width, height, radius);
//	}
	
	
	/**
	 * Defines a subsection of a topography grid, a TopoGrid
	 * @param topoG the TopoGrid to be created
	 * @param res the resolution,
	 * @param offset starting index position in the topography data
	 * @param w width
	 * @param h height
	 * @return a new TopoGrid grid
	 */
	private TopoGrid[] defineLOD(TopoGrid[] topoG, int res,Point offset,int w,int h) {
		int n=2;//res;
		topoG= new TopoGrid[n*n];
		// Creates 2×2 sub grids 
		for (int y = 0; y < n; y++) {
			for (int x = 0; x < n; x++) {
				Rectangle r=   new Rectangle(offset.x+x*w/2,offset.y+y*h/2, w/2,h/2);
//						offset.x+(x+1)*w/2,offset.y+(y+1)*h/2);
				int displayList=  glGenLists(1);
				topoG[x+y*n]=  new TopoGrid(r, res/2, displayList);//x+y*n+1+base);
				System.out.println("created Lod grid "+topoG[x+y*n].info());
			}
		}
		return topoG;
	}


	/**
	 * Creates an array of colours following the two-dimensional grid.
	 * One colour for each vertex, defined by the ColourGradient given a height.
	 * @return a one-dimensional array of ColorInt 
	 */
	private ColorInt[] createColorArray() {
		ColorInt c[]= new ColorInt[topography.getValues().length];
		float value;
		int index= 0;
		float  h= 0;
		float dark= 1;
		for (int y = 0; y < height; y++) { 		// latitude
			for (int x = 0; x < width; x++) {	// longitude
				index=x+y*width;
				value= topography.getValues()[index];
				h= (x>0 ) ? (topography.getValues()[index-1]-value) :1 ;
				dark= Math.abs( (float) Math.cos(h/50) );
				
				c[index]=  new ColorInt( gradient.retrieveColor(value) );
				c[index].darker(dark);//dark);
//				System.out.println(index+" h="+h+" dark "+dark);
//				System.out.println("value="+value+" color "+ c[index].toString());
			}
		}
		
		return c;
	}


	/**
	 * Creates a two-dimensional coordinate grid from latitudes and longitude
	 * @param lats array of latitudes
	 * @param lons array of longitudes
	 * @return a two-dimensional coordinate array
	 */
	public Coordinate[][] createCoordinates(float[] lats,float[] lons){
		Coordinate[][] c= new Coordinate[width][height];
//		int ic= 0;
		for (int y = 0; y < height; y++) { 		// latitude
			for (int x = 0; x < width; x++) {	// longitude
				c[x][y]=new Coordinate(lats[y],lons[x]);
//				ic++;
			}
		}

		return c;
	}
	
	
	
	public void render(int step,Rectangle region){
		float x,y,z;
    	float x1,y1,z1;
    	Vector3D n= new Vector3D();
    	Vector3D n1= new Vector3D();

//		glDisable(GL_TEXTURE_2D);
//		glEnable(GL_BLEND);     
//		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
	    glPolygonMode(GL_BACK,(useFill) ? GL_FILL:GL_LINE);
	    glPolygonMode(GL_FRONT, (useFill) ? GL_FILL:GL_LINE);
	    glDisable(GL_CULL_FACE);
	    
	    

	    //  Calculates the centre coordinate
	    if (getCenter()==null) {
	    	calculateCenter();
		}
	    
	    float v= 0;
//	    for(int iy = step; iy < height-step+1; iy+=step) {
	    	for(int iy = region.y+ step; iy < region.y+region.height+1; iy+=step) {

	    	glBegin(GL_TRIANGLE_STRIP);
	    	
	    	for (int ix = region.x; ix < region.x+region.width+1; ix+=step) {
	    		if (ix<width && iy< height) {
					

	    		//    		j = (iy==nLon) ? 0: iy ;
	    		//    		ty= 1-dy*ix ;  // Texture image starts at bottom
	    		//    		ty2= 1-dy*i0 ;
	    		//    		tx = 1-dx*iy ; //Longitude goes opposite way of texture image !

	    		x=vertex[ix][iy-step].x;   
	    		y=vertex[ix][iy-step].y;
	    		z=vertex[ix][iy-step].z;
	    		x1=vertex[ix][iy].x;	// 
	    		y1=vertex[ix][iy].y;
	    		z1=vertex[ix][iy].z;  
	    		// Normal vectors, 
	    		// October 2020 – we need a normal vectors to point from surface!!
	    		Vector3D p = new Vector3D(x, y, z);
	    		Vector3D p1 = new Vector3D(x1, y1, z1);
	    		Vector3D p2 = (ix+step< width) ? 
	    					new Vector3D(vertex[ix+step][iy])  : new Vector3D(p1);
	    		Vector3D d1= p.unitTo(p1); 
	    		Vector3D d2= p.unitTo(p2);
	    		n.equals(d1.cross(d2));
	    		n1.equals(n);
	    		
	    		// 	This did not work. Everything became white.
//	    		n.setTo(x, y, z); //	n.norm();
//	    		n1.setTo(x1, y1, z1); //n1.norm();

	    		// 	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	«•»	
	    		float c[]= colors[ix+ (iy-step)*width].convertToFloat();
	    		float c1[]= colors[ix+ iy*width].convertToFloat();

	    		//P1 (page 111 in the OpenGL super Bible) //Label2
//	    		glTexCoord2f(tx*dd, ty2*ddd);
	    		glColor4f(c[0],c[1],c[2],c[3]);
//	    		glColor4f(0.5f, 0.2f, 0,1.0f); // just for testing: June 16
	    		
	    		glNormal3f(n.x,n.y, n.z);
	    		glVertex3f(x,y,z);

	    		// P2
//	    		glTexCoord2f(tx*dd,ty*ddd);
	    		glColor4f(c1[0],c1[1],c1[2],c1[3]);
	    		glNormal3f(n1.x,n1.y,n1.z);
	    		glVertex3f(x1,y1,z1);
	    		
//	    		System.out.println("xyz:"+x+" "+y+" "+z+" "+x1+" "+y1+" "+z1);
	    	}
	    	}//  if 
	    	glEnd();
	    }
}

	
	/**
	 * Handling the drawing of the GeoGrid with a display list
	 */
	public void displayListHandler(Vector3D camPos){
		
		// Texture
		if (useTexture && texture!=null) {
			glEnable(GL_TEXTURE_2D);
		}
		else {
			glDisable(GL_TEXTURE_2D);
		}

		// Create display list 
		if (doRecreate) {
			// Delete all lists 1st
			glDeleteLists(displayList, 1);
			for (int i = 0; i < topoGrid.length; i++) {
				glDeleteLists(topoGrid[i].dlist ,1);
				topoGrid[i].deleteDisplayList();
				System.out.println("List deleted:"+topoGrid[i].dlist);
			}


			//  Create all Display List
			//  This is the whole upper rough topography,resolution means jumping across every n't cell 
			glNewList(displayList, GL_COMPILE_AND_EXECUTE);
			render(resolution,new Rectangle(width, height));
			glEndList();
			// TopoGrids, the higher resolution grids which may further contain sub grids
			for (int i = 0; i < topoGrid.length; i++) {
				topoGrid[i].createDisplayList();
			}

			doRecreate= false;
			System.out.println("TopographyGL display list created !");
		}
		
		// Use display list generated
		else {		
			boolean useSmaller= false;
			float shiftDistance= 50 ;
			float shiftDistance2= 20;
			
			//  Check all the topo grids
			for (TopoGrid topo : topoGrid) {

				Vector3D p =  CoreOpenGL.latLonToPoint(topo.center.getLat(), 
						topo.center.getLon() ).scaledTo(radius);
				float distance = camPos.subtract(p).length();
				//			System.out.println("Distance to topography: "+distance);
				
				// Try calling a smaller list first
				if (distance < shiftDistance) {
					//  Try calling a smaller list, inside the sub grid first
					if (topo.subGrid!=null) {
						for (TopoGrid grid : topo.subGrid) {
							// useSmaller= useSmaller || 
							useSmaller=	grid.callTheList(shiftDistance2, camPos);
//							
//							// Calculate distance again									
//							p=CoreOpenGL.latLonToPoint(grid.center.getLat(), 
//									grid.center.getLon() ).scaledTo(radius);
//							distance  =  camPos.subtract(p).length();;
//							//  Call the innermost list	
//							if (distance< shiftDistance2) {
//								glCallList(grid.dlist);
//								useSmaller=true ;
//								System.out.println(" Just to call the innermost list ");
//							}
						}
					}
//					System.out.println("topo.subGrid =null");
//					glCallList(topo.list);
					if (!useSmaller) {
						p= CoreOpenGL.latLonToPoint(topo.center.getLat(), topo.center.getLon());
						p.scaleVector(-0.5f);
						glPushMatrix();
						glTranslatef(p.x, p.y, p.z);
						glCallList(topo.dlist);
						glPopMatrix();
						useSmaller=true ;
					}
				}
			}
			
			
			// Call list for the total TopoGL
			if (false    ||  !useSmaller) {
				Vector3D p= CoreOpenGL.latLonToPoint(getCenter().getLat(), getCenter().getLon());
				p.scaleVector(-10);
				glPushMatrix();
//				glTranslatef(p.x, p.y, p.z);
				glCallList(displayList);
				glPopMatrix();
				
//				System.out.println("Calling the TopoGL List");
			}
		}
	}
	
	/**
	 * Converts the ColourGradient image to a slick image
	 * in order to be able to draw it into 2D using slick
	 */
	public void convertGradientToImage(){
		Texture texture= null;
		try {
			texture = BufferedImageUtil.getTexture("", gradient.getImage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		image=  new Image(texture);
	}


	public Image getGradientImage() {
		return image;
	}
	
	

}
