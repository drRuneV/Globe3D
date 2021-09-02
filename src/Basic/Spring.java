/*
 * To change this license header, choose License Headers in Project Properties.

 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic;

import Basic.Circle;
import Basic.PointSprite;
import Basic.Vector2D;

import java.awt.Color;
import java.util.Vector;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
//import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author runev
 */
/**
 * @author Admin
 *
 */
public class Spring {
    protected Vector2D position;
    protected float dx;
    protected Vector<Agent>  agents=null;
    protected float k;
    protected int Number;
    protected float damp;
    protected boolean rubber=true;
    protected float deviationDx;
    protected int level;
    
    protected PointSprite spriteCenter;
    protected Circle circle;
    protected int age;
    protected int fixInterval;
    
    /**
     * 
     * @param position

     * @param dx
     * @param k
     * @param Number 
     */
    public Spring(Vector2D position, float dx, float k, int Number,int fixInterval,int level) {
        this.position = position;
        this.dx = dx;
        this.k = k;
        this.Number = Number;
        this.fixInterval=fixInterval;
        this.damp= 0.999f;
        this.level=level;
        this.spriteCenter= new PointSprite(position, 1+Number/24, true, Color.RED);
        circle=new Circle(20, 16);
        age=0;
      //  agents=new Vector<Agent>();
        generate();
        
    }
  

    /*
     * Generates a list of agents representing the spring
     * 
     */
    protected void generate(){
        float radius= (float) (dx*Number/(2*Math.PI));
        
        if (agents==null) agents= new Vector<Agent>();
        else agents.removeAllElements();        
        
        distribute(radius*1.6f);
    }

    
    /**
     * @param time 
     */
    public void update(float time){
        Vector2D v;
        calculate();
        
        position.setZero();
            
        
        for(int ix=0; ix< agents.size(); ix++){
            
            /*if (position.subtract(LWJGLApplication1.getCenter()).lenght()> 450)
            agents.get(ix).update(time);//basicUpdater(time);
            else  
              */  agents.get(ix).basicUpdater(time);
            
            
            // center of mass 
            position.plus(agents.get(ix).getPosition());
            
            // dampening of  speed
            v= agents.get(ix).getVelocity();
            v.scale(damp);
            agents.get(ix).setVelocity(v);
        }

        
//	         
        age++;
        position.scale(1.0f/Number);
        drawLines();
        
    }
    
    /*
     * Draws lines between the elements of the string
     */
    public void drawLines(){
        
        
        float v;

        glBegin(GL_LINE_LOOP);
        for(int ix=0; ix< agents.size(); ix++){
            v= (float) agents.get(ix).getVelocity().lenght();
            v= Math.min(1, v/20);
            glColor4f(v,1-v,1-v*v,0.25f);//1.0f*ix/Number,1-1.0f*ix/Number,0.5f*ix/Number, 0.5f);
            glVertex2f( agents.get(ix).getPosition().getX(), agents.get(ix).getPosition().getY());
//            glVertex2f( position.getX(), position.getY()); 
        }
        
        glEnd();
//        spriteCenter.draw(position);
//        glTranslatef(position.getX(),position.getY(),0);
//        circle.draw();	
//        
       glPushMatrix();
            GL11.glTranslatef(position.getX(),position.getY(),0);//Display.getWidth()/2f, Display.getHeight()/2f,0);
            glRotatef((float) (0.5f), 0, 0, 1); 
            //glScalef((float) radius, (float) radius, 1);
            circle.setRadius(deviationDx*0.5f);
            circle.draw();
        glPopMatrix();
        
    }

    
    
    /**
     * calculates the acceleration for each element in the spring
     */
    public void calculate(){
        int j1,j2;
        Vector2D acceleration;
        float dxX=dx;
        deviationDx=0;
        
         for(int ix=0; ix< agents.size(); ix++){
             acceleration= new Vector2D();
             
            if (fixInterval>0 && ix%fixInterval==0) continue;    
            
            for(int le=1; le< level+1; le++)
            {
              j1=   ix-le;
              j2=   ix+le;
              
              if (j1<0)                j1=(agents.size()+j1);
              if (j2>agents.size()-1)  j2=j2-agents.size();

             setDx(dxX*le);
             acceleration.plus(forceBetween(ix, agents.get(j1).getPosition()));
             acceleration.plus(forceBetween(ix, agents.get(j2).getPosition()));
            }
            
            setDx(dxX);
            acceleration.plus(forceBetween(ix, position).scale2((float) (1.1f/Number+0.0f*Math.sin(0.01*age))));
    
//            if( ! Main.getAgents().isEmpty()) {
//                acceleration.plus(forceBetween(ix, Main.getAgents().get(0).getPosition()).scale2(0.01f));
//            }
            
            //heavy
//            if(ix%4==0) acceleration.scale(0.01f);  
            
            agents.get(ix).setAcceleration(acceleration);
         }
         
         deviationDx/=(Number*3);
    }
    
    /**
     * 
     * @param i
     * @param j
     * @return 
     */
     protected Vector2D forceBetween(int i ,Vector2D pos){
        Vector2D acceleration;
        Vector2D atPos;
        double distance;
        double force=0;

        atPos= agents.get(i).getPosition();
        acceleration= pos.subtract(atPos);
        distance= acceleration.lenght();            
        //If distance is extended the acceleration is towards the other
        force=  (distance-dx)*k;
        //force= Math.signum(force)* Math.pow(force,1.01);
        if (rubber && distance<dx) force=0;
        
        acceleration.norm();
        acceleration.scale((float) force);

        deviationDx+= Math.abs(distance-dx);
        
        return acceleration;
     }

     
     public void distribute(float parameter){
          distributeCircle(parameter);
     }

     
     protected void distributeCircle(float radius){
         float x,y;
         double  angle= 2*Math.PI/Number;
         double r;
         Agent agent;
         Vector2D pos;

         
         for(int ix=0; ix< Number; ix++){
//              else r= radius*(0.5+0.4*Math.sin(0.002f*count));//*angle;//Math.sin(ix*angle);
           r= radius*0.9 +0.5*radius*(Math.cos(ix*angle)*Math.cos(ix*angle) );
           y= (float) (r*Math.sin(ix*angle));//*Math.cos(ix*angle) );
           x= (float) (r*Math.cos(ix*angle));  
           

           pos= new Vector2D(position.getX()+x,position.getY()+y);
           agent= new Agent();
           agent.setPosition(pos);
           agents.add(agent);
         }
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public void setK(float k) {
        this.k = k;
    }

    public void setNumber(int Number) {
        this.Number = Number;
    }

    public void setDamp(float damp) {
        this.damp = damp;
    }

    public float getDx() {
        return dx;
    }

    public float getK() {
        return k;
    }

    public int getNumber() {
        return Number;
    }

    public float getDamp() {
        return damp;
    }
     

     
     
    public Vector<Agent> getAgents() {
        return agents;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public void setRubber(boolean rubber) {
        this.rubber = rubber;
    }

    public Vector2D getPosition() {
        return position;
    }

    public boolean isRubber() {
        return rubber;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    
    
 }
    
// <•><•><•><•><•><•><•><•><•><•>    
