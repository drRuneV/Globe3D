package distribution;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Point;

import Colors.ColorInt;
import GeometryGL.Globe;
import Utility.Coordinate;
import basicLwjgl.Camera;
import basicLwjgl.CoreOpenGL;
import basicLwjgl.Vector3D;

public class Agent {
	
	private Coordinate c= null;
	private Vector3D position= null;
	private Vector3D velocity= new Vector3D();
	private Vector3D acceleration= new Vector3D();
	private Point gridLocation= new Point();
	private ColorInt color =  new ColorInt(Color.red);
	
	public Agent(Coordinate c) {
		this.c = c;
		setPosition(c);		
	}

	public Agent(Agent a, float dc) {
		c= new Coordinate(a.c) ;
		float dlat= (float) (0.5f-Math.random())*dc;
		float dlon= (float) (0.5f-Math.random())*dc;
		c.addLonLat(dlat, dlon); 
		setPosition(c);
	}
	
	public void setPosition(Coordinate co) {
		position= new Vector3D(CoreOpenGL.coordinateToPoint(co));
		position.scaleVector(Globe.Radius+5);
		
	}
	
	
	/**
	 * Updates the agent position using velocity and acceleration
	 * @param current the current field
	 * @param dt time step scaling
	 */
	public void update(CurrentField current, float dt) {
		float max= 0;
		Distribution d= current.v;
		int ix= c.findClosestIndex(d.getCoordinates(), 0.5f);
		// 
		if(ix!=-1) {
			int t= current.getCount();//ix/(wh);
			int y= ix/d.width;
			int x= ix-y*d.width ;
			if ( current.field[x][y][t]!=null  ) {
				gridLocation.setLocation(x, y);
				max = current.field[x][y][t].length()*1;
				//  Original
				acceleration.equals(current.field[x][y][t]);
				velocity.plus(acceleration.scaledTo(dt));	// V= v0+at
				//  Simpler
//				velocity.equals(current.field[x][y][t]);
				velocity.perturbation(0.1f); // makeGauss(0.1f); // random perturbation
				//  Constrain velocity
				if(velocity.length()> max) {
					velocity.scaleVector(max/velocity.length());
				}
				// dP= V×t+½×a×t×t
//				Vector3D vector = velocity.scaledTo(dt).add(acceleration.scaledTo(0.5f*dt*dt)) ;
				position.plus(velocity.scaledTo(dt));//vector);
			}
		}
		//  Update coordinate
		c=Camera.coordinateFromPoint(180, position);

	}
	
	public void draw(CurrentField current) {
		
		CoreOpenGL.setGLStraightenDrawing();
		
		float value = current.field[gridLocation.x][gridLocation.y][current.getCount()].length();

		Vector3D p = position; 
		glPointSize(6+ value*50.0f);

//		color = current.getGradient().retrieveColor(value);//new ColorInt(Color.red);
		
		float c[]= color.convertToFloat();
		glColor4f(c[0], c[1] , c[2], c[3]);
		//		dTo= field[x][y][t].scaledTo(sc) ;
		glBegin(GL_POINTS);
		glVertex3f(p.x,p.y,p.z);
		glEnd();
		
//		System.out.println("v "+velocity.information()+" c "+
//		 Camera.coordinateFromPoint(180, position));

	}
	
	

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}

	public ColorInt getColor() {
		return color;
	}

	public void setColor(ColorInt color) {
		this.color = color;
	}

	public Vector3D getPosition() {
		return position;
	}

	public void setPosition(Vector3D position) {
		this.position = position;
	}

	public Vector3D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3D velocity) {
		this.velocity = velocity;
	}

	public Coordinate getC() {
		return c;
	}

	public void setC(Coordinate c) {
		this.c = c;
	}

}
