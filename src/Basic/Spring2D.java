/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Basic;

import Basic.Vector2D;


/**
 *
 * @author runev
 */
public class Spring2D extends Spring{

    public Spring2D(Vector2D position, float dx, float k, int Number, int fixInterval,int level) {
        super(position, dx, k, Number, fixInterval,level);
    }
    
  
    @Override
    public void calculate(){
        int j1,j2, j3,j4;
        Vector2D acceleration;
        
        deviationDx=0;
        
         for(int ix=0; ix< agents.size(); ix++){
             
            if (fixInterval>0 && ix%fixInterval==0) continue;    
            //
            j1=  (ix==0) ? (agents.size()-1): ix-1;
            j2=  (ix==agents.size()-1) ?  0 : ix+1;
            
            j3=  (ix==0) ?  (agents.size()-2): ix-2;
            j3=  (ix==1) ?  (agents.size()-1): j3;
            j4=  (ix==agents.size()-1) ?  1 : ix+2;
            j4=  (ix==agents.size()-2) ?  0 : j4;

            acceleration=forceBetween(ix, agents.get(j1).getPosition());
            acceleration.plus(forceBetween(ix, agents.get(j2).getPosition()));
             setDx(dx*2);
            acceleration.plus(forceBetween(ix, agents.get(j3).getPosition()));
            acceleration.plus(forceBetween(ix, agents.get(j4).getPosition()));
            setDx(dx*0.5f);
            acceleration.plus(forceBetween(ix, position).scale2((float) (1.1f/Number+0.0f*Math.sin(0.01*age))));
    
//            if( ! Main.getAgents().isEmpty()) {
//                acceleration.plus(forceBetween(ix, Main.getAgents().get(0).getPosition()).scale2(0.01f));
//            }
            
            //heavy
//            if(ix%4==0) acceleration.scale(0.01f);  
            
            agents.get(ix).setAcceleration(acceleration);
         }
         
    }

            
    }
    
