/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic;

import java.awt.Color;
import java.util.Vector;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
//import org.lwjgl.util.Point;


/**
 *
 * @author runev
 */
public class Circle {

    private float radius;
    private Color color;
    private Vector<Vector2D>  points;
    private int n=8;
    private int count=0;
    private final boolean animated= false;

    
    
    public Circle(float radius,int N) {
        this.radius=radius;
        color= new Color(1.0f, 1.0f, .0f);
        color.darker();
        n=N;
        generate(N);
    }
    
    /**
     * 
     * @param N 
     */
    private void generate(int N){
        float   x,y;
        double  angle= 2*Math.PI/N;
        double r=radius;
        
        if (points==null) points= new Vector<>();
        else points.removeAllElements();        
         
//        if (ix%2!=0) r=radius;
        //else r= radius*(0.5+0.4*Math.sin(0.002f*count));//*angle;//Math.sin(ix*angle);
        for(int ix=0; ix< N; ix++){
        	y= (float) (r*Math.sin(ix*angle));
        	x= (float) (r*Math.cos(ix*angle));           
        	points.add(new Vector2D(x,y));
        }
         count++;
    }
    
    /**
     * 
     */
    public  void draw(Color color,float alpha){
                 
         if(animated) generate(n);
         glColor4f(color.getRed()/255f, color.getGreen()/255f,color.getBlue()/255f, alpha);
         glLineWidth(0.2f);
         glBegin(GL_LINE_LOOP);
         for(int ix=0; ix< points.size(); ix++)
         {
//        	 dx= 1.0f*ix/points.size();
        	 glVertex2f(points.get(ix).getX(), points.get(ix).getY()); 
         }
         glEnd();
    }

    
    public void draw() {
		draw(Color.white, 0.9f);		
	}

	public void setRadius(float radius) {
        this.radius = radius;
        generate(points.size());  
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public Vector<Vector2D> getPoints() {
        return points;
    }

    public void setN(int n) {
        this.n = n;
        generate(n);
    }

    public int getN() {
        return n;
    }
    
    
}
