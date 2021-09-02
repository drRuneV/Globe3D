/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package Basic;

import java.awt.Color;

/**
 *
 * @author runev
 */
public class Agent {
    private Vector2D position=new Vector2D();
    private Vector2D velocity=new Vector2D();
    private Vector2D acceleration=new Vector2D();
    private PointSprite sprite;//=new PointSprite(position, 5, true, Color.yellow);;
 
    public int age=0;
    
 public Agent(){
      position.setZero();
      velocity.setZero();
      acceleration.setZero();       
      sprite= new PointSprite(position, 4, false, Color.yellow);
 }

    public Agent(Vector2D position, Vector2D velocity, Vector2D acceleration, PointSprite sprite) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.sprite = sprite;
    }
 
 
 
 /**
  * 
  * P= V'*t+1/2*a*t*t ,  V= a*t
  * @param time 
  */
 private  void move(float time){
     /**  Vo t */
     Vector2D frameSpeed= new Vector2D(velocity);
      frameSpeed.scale(time);
      /**  V=a*t */
      velocity.plus(acceleration.scale2(time));
      position.plus(frameSpeed.add(acceleration.scale2(time*time)));            
     // System.out.println("vel "+velocity.toString());
 }

 public void basicUpdater(float time){
          
      move(time);
      acceleration.setZero();

      sprite.draw(position);
      age++;
 }

    /**
     *
     * @param time
     */
 public void update(float time){
      calculate();
      move(time);
      acceleration.setZero();

      sprite.draw(position);
      age++;
      if(age>1000) sprite.setAnimation(true);
//      System.out.println("updated agent");
 }
 
 private void calculate(){
//     jVector2D direction  =      position.subtract(Main.getCenter());
//     double distance= direction.lenght();
//     
//     if (distance>5){
//         direction.norm();
//         acceleration.plus(direction.scale2((float) (-1000/(distance)*0.1d)));
         System.out.println("d>200 acc="+acceleration.lenght());
//     }
     
 }

    /**
     *
     * @return 
     */
    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public Vector2D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    public PointSprite getSprite() {
        return sprite;
    }

    public void setSprite(PointSprite sprite) {
        this.sprite = sprite;
    }
 
 

    @Override
    public String toString() {
        return "position="+position.toString() +" velocity="+ velocity.toString()+" acceleration="+ acceleration.toString() ;
    }
    
}
