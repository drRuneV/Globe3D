/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic;

import java.awt.Color;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author runev
 */
public class PointSprite {
    
    private int size;
    private boolean animation;
    private Color color;
    private int count;
    private Vector2D position;
     

    public Vector2D getPosition() {
        return position;
    }
    
    public PointSprite(Vector2D position){
        size=3;
        color=new Color(1.0f, .90f, .10f);
        animation=false;
        count=0;
        this.position = position;
    }

    /**  Constructor
     * @param position
     * @param size – the number of points drawn
     * @param animation - The sprite is animated
     * @param color */
    public PointSprite(Vector2D position, int size,boolean animation,Color color){
        this.position= position;
        this.animation=animation;
        this.color=color;
        this.size=size;
        count=0;
        
    }
    
    /**
     * 
     * @param at 
     */
    public void draw(Vector2D at){
        float alpha=1.0f;
        float alpha2=1.0f;
        float ds=1.0f/size;
        position=at;
        count++;

//        System.out.println("ds "+ds);
        alpha2*= animation ?  Math.abs( Math.cos(count*0.01f)) : 0.5f;
        //System.out.println("a "+alpha2);                 
        //test
        //at.scale((float) (1.001f-0.0011f*alpha));//*Math.random()));
        
      //  System.out.println("color "+color.toString());
        glColor4f(color.getRed()*1f/255f, color.getGreen()/255f, color.getBlue()/255f, 0.3f*alpha2);
             
        
        glPushMatrix();
        glTranslatef(position.getX(), position.getY() , 0);//position.getX());
         for(int ix=size; ix>-1; ix--)
         {             
	//            alpha=  Math.max(0.02f, alpha2-ds*ix*0.4f);
	//             System.out.println("a "+alpha+"..."+ds*ix);
            glPointSize(0.5f+ 5*0.5f*ix); //never PointSize inside glBegin-glEnd !
            glBegin(GL_POINTS);
             //glColor4f(color.getRed()*0.1f, color.getGreen(), color.getBlue(), 0.3f*alpha);
             glVertex3f(0,0,0 );
            glEnd();
//             System.out.println(ix+" alpha="+alpha);
         }
         
         count= (Math.sin(count)==0) ? 0: count;
        
        glPopMatrix();
        
    }
    
    public void setAnimation(boolean a){
         animation=a;
    }

    public void setColor(Color color) {
        this.color=color ;
    }

    public Color getColor() {
        return color;
    }

    
    
}
