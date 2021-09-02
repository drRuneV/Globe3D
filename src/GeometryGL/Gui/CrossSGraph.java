package GeometryGL.Gui;

import static org.lwjgl.opengl.GL11.*;

import Basic.Vector2D;
import Colors.ColorGradient;
import Colors.ColorInt;
import GeometryGL.SelectBox;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.SlickGraphic;

public class CrossSGraph extends SelectBox{

	// Number of sampling points along cross-section
	int number= 0;
	// Line with gives ×100 pixel with of plot
	int s=2;





	public CrossSGraph(int x, int y, int w, int h,int number) {
		this(x, y, w, h);
		this.number = number ;
	}



	/**
	 * Constructor
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public CrossSGraph(int x, int y, int w, int h) {
		super(x, y, w, h);
	}


	public void draw(ColorGradient cg,float[] values){

		CoreOpenGL.project2D();
		glDisable(GL_DEPTH_TEST);
		//
		Vector2D p= new Vector2D(getX(), getY()); 
		ColorInt ci= new ColorInt();
		//  
		glPushMatrix();
		glTranslatef(p.x, p.y, 0); 

		glLineWidth(2.0f);
		glColor4f(0.1f, 0.1f , 0.1f , 0.80f);
		glBegin(GL_LINES);
		{
			glVertex2f(0,0 );
			glVertex2f(0,height );	// vertical axis
			glVertex2f(0,height );
			glVertex2f(s*number+1, height); // horizontal bottom
		}
		glEnd();
		
		// Background 
		glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
		glColor4f(0.2f, 0.2f , 0.2f , 0.99f);
		glBegin(GL_POLYGON);
		{
			glVertex2f(0,0 );
			glVertex2f(0,height );
			glVertex2f(2*number+0,height);
			glVertex2f(2*number+0,0);
		}
		glEnd();



		//  Plot
		glLineWidth(s);
		glBegin(GL_LINES);
		for (int i = 0; i < values.length; i++) {

			ci=  cg.retrieveColor(values[i]);
			float [] c= ci.convertToFloat();
			float h= Math.min(height, Math.max(0,values[i]*2) );

			glColor4f(c[0],c[1],c[2],c[3]);
			glVertex2f(i*s, height-1);
			glVertex2f(i*s, height-h);
		}
		glEnd();

		
		glPopMatrix();
		
		glEnable(GL_DEPTH_TEST);
	}



	/**
	 * @return the s
	 */
	public int getS() {
		return s;
	}



	/**
	 * @param s the s to set
	 */
	public void setS(int s) {
		this.s = s;
	}

}
