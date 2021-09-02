package GeometryGL;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Point;
import java.awt.Rectangle;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
//import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.Graphics;

import Basic.Vector2D;
import Colors.ColorInt;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.SlickGraphic;




public class SelectBox extends Rectangle{
	
//	Rectangle area;
	protected Vector2D position= null ; 
	protected ColorInt fillColour = new ColorInt(90, 90, 95, 150 ) ;
	protected ColorInt lineColour = new ColorInt(java.awt.Color.GRAY);
	protected ColorInt selectedColour = new ColorInt(5, 200, 20, 200);

	protected boolean selected = false;
	
	
	
	/**
	 * Constructor
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public SelectBox(int x,int y,int w,int h) {
		super(x,y,w,h);
		position = new Vector2D(x, y);
	}
	
	public SelectBox(Point p,int w,int h) {
		super(p.x,p.y,w,h);
		position = new Vector2D(x, y);
	}

	
	public void draw(){

		CoreOpenGL.project2D();
		//
		Vector2D p= new Vector2D(getX(),getY()); 
//		ColorInt ci= new ColorInt();
		float c[]= (selected) ? selectedColour.convertToFloat() : fillColour.convertToFloat();
		//  
		glPushMatrix();
		glTranslatef(p.x-1, p.y, 0); 
		glDisable(GL_DEPTH_TEST);

		glLineWidth(2.0f);
		glColor4f(c[0],c[1],c[2],c[3]);
		glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
		glBegin(GL_POLYGON);
		{
			glVertex2f(0,0 );
			glVertex2f(0,height );
			glVertex2f(width+2,height );
			glVertex2f(width+2,0);
		}
		glEnd();

		glPopMatrix();

		glEnable(GL_DEPTH_TEST);
		glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
	}
	
	/**
	 * Draws the rectangle 
	 * @param slick SlickGraphic for drawing
	 */
	public void draw(SlickGraphic slick){

		CoreOpenGL.project2D();

		org.newdawn.slick.Color cf= fillColour.convertToSlick() ;
		org.newdawn.slick.Color cl= (selected) ?  selectedColour.convertToSlick() : lineColour.convertToSlick() ;
		// 
		slick.setColor(cf);
		slick.fillRect((float)getX(),(float) getY(), (float)getWidth(),(float)getHeight());
		slick.setColor(cl);
		slick.drawRect((float)getX()-1, (float)getY()-1,(float) getWidth()+1,(float)getHeight()+1);

	}

	public void draw(Graphics g){

//		CoreOpenGL.project2D();

		org.newdawn.slick.Color cf= fillColour.convertToSlick() ;
		org.newdawn.slick.Color cl= (selected) ?  selectedColour.convertToSlick() : lineColour.convertToSlick() ;
		// 
		g.setColor(cf);
		g.fillRect((float)getX(),(float) getY(), (float)getWidth(),(float)getHeight());
		g.setColor(cl);
		g.drawRect((float)getX()-1, (float)getY()-1,(float) getWidth()+1,(float)getHeight()+1);

	}

	
	public boolean inside(Point p){
		boolean is= false;
		int xx= (int) (x+width);
		int yy= (int) (y+height);
				
		is = (p.x>x && p.x<xx && p.y>y  && p.y< yy) ;
		
		return is;
	}
	
	public boolean update(){
		int  mx= Mouse.getX();
		int  my= Display.getHeight()- Mouse.getY();
		Point p= new Point(mx, my);
		boolean control =CoreOpenGL.isCtrl();
		boolean contain = this.contains(mx, my) ;
		boolean mouseHas = Mouse.isButtonDown(0)  && contain ;
		selected= (selected && control) || (contain  && control)  || mouseHas;

		
//		System.out.println("Mouse "+mx+" "+my +" Contain "+contain+" control="+control);
//		System.out.println("my inside="+inside(p));
		
		// 
		if (selected) {
			this.x=mx-width/2;
			this.y=my-height/2;
//			System.out.println("selected Box "+mx+" : "+my);
		}
	
		return mouseHas;
	}

	
	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the position
	 */
	public Vector2D getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Vector2D position) {
		this.position = position;
	}

	/**
	 * @return the fillColour
	 */
	public ColorInt getFillColour() {
		return fillColour;
	}

	/**
	 * @param fillColour the fillColour to set
	 */
	public void setFillColour(ColorInt fillColour) {
		this.fillColour = fillColour;
	}

	/**
	 * @return the lineColour
	 */
	public ColorInt getLineColour() {
		return lineColour;
	}

	/**
	 * @param lineColour the lineColour to set
	 */
	public void setLineColour(ColorInt lineColour) {
		this.lineColour = lineColour;
	}

}
