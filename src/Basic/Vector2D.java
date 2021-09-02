/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic;

import java.awt.Point;

/**
 *
 * @author runev
 */
public class Vector2D {
        public float x;
        public float y;

        //------------------
        /**  Constructors.... */
        public Vector2D() {
            x=0; y=0;
        }
        
        public Vector2D(Vector2D v) {
            this.x=v.x;
            this.y=v.y;
        }
        
        //----------------------------------
        public Vector2D(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public Vector2D(double x, double y) {
        	this.x = (float) x;
            this.y = (float) y;
		}

        public Point asPoint(){
        	Point p= new Point((int)this.x, (int)this.y);
        	return p;
        }
        
		/**
         * Adds this V with another vector and returns a new one 
         * this vector is not changed
         * @param vector
         * @return 
         */
        public Vector2D add(Vector2D vector){ 
            Vector2D v= new Vector2D(this.x, this.y);
            v.plus(vector);
            return v;            
        }
        
        /**
         * Values to this vector
         * @param dx
         * @param dy
         */
        public void shift(float dx,float dy){
        	x+=dx;
        	y+=dy;        	
        }
        
        /**
         * Subtracts a vector from this one and returns a new one
         * @param vector to subtract
         * @return a new vector
         */
        public Vector2D subtract(Vector2D vector){   
            Vector2D v= new Vector2D(this.x, this.y);
            v.minus(vector);
            return v;            
        }
        
        public void plus(Vector2D v){
            this.x+=v.x;
            this.y+=v.y;
        }
        
         public void minus(Vector2D v){
            this.x-=v.x;
            this.y-=v.y;
        }
                
         public void setxy(float x, float y){
             this.x=x;
             this.y=y;             
         }
         
        public void  setZero(){
        x=0; y=0; 
       }

        /**  Gives Unit Vector from this Point to Target */
      public Vector2D UnitTo(Vector2D target)
      {
       Vector2D v= new Vector2D(target);
       v.minus(this);
       v.norm();       
       return(v);
      }
        
      /**  The length of the vector */
      public double lenght()
      {
        return(Math.sqrt(x*x+y*y));
      } 
      
      
      /**  Normalizes the vector */
      public void norm()
      {
       double L= lenght();
       if(L!=0){ 
           x/=L; y/=L; 
       }
      }//
        
        
      
      /**  Rotates in Degrees */
    public void rotate(float dth) {
        float yy=y, xx=x;
        x=  (float) (xx * Math.cos(Math.toRadians(dth)) - yy * Math.sin(Math.toRadians(dth)));
        y=  (float) (xx * Math.sin(Math.toRadians(dth)) + yy * Math.cos(Math.toRadians(dth)));        
      }
    
   
    
      /**
       * Scales the vector with a factor
       * @param sc 
       */
      public void scale(float sc)
      {
       x= sc*x;  y= sc*y;  
      }////::::::::////

      /**
       * Scales the vector and returns a new vector
       * @param sc
       * @return 
       */
      public Vector2D scale2(float sc)
      {
       Vector2D v= new Vector2D(x, y);
       v.scale(sc);
       return v;  
      }////::

      /**  Calculates the dot product */
      float dot(Vector2D V)
      {
       return(x*V.x+ y*V.y);

      }////::::::::////


      /**  Calculates the cross product */
      Vector2D cross(Vector2D V)
      {
       Vector2D Res= new Vector2D(); 
       return Res;
      }////::::::::////

    @Override
    public String toString() {
        return "x="+x+" y="+y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
      
    
      

    }//

    