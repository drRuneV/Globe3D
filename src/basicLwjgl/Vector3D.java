package basicLwjgl;

import java.util.Random;

/**
 * Vector3D
 * @author Admin
 *
 */
public class Vector3D {

	public static final float PI= (float) Math.acos(-1);
	public static final float ToRAD =PI/180;
	public static final float ToDEG =180/PI;
	//
	public float x=0;
	public float y=0;
	public float z=0;


	/**
	 * Constructor
	 * All values are zero (0.0f,0.0f,0.0f)
	 */
	public Vector3D() {
	}


	/**
	 * Constructor
	 * @param x the X component
	 * @param y the Y component
	 * @param z the Z component
	 */
	public Vector3D(float x,float y,float z) {
		this.x=x;
		this.y=y;
		this.z=z;
	}


	/**
	 * Constructor
	 * @param v vector to set values from
	 */
	public Vector3D(Vector3D v) {
		x=v.x;
		y=v.y;
		z=v.z;
	}

	/**
	 * Information string about this vector
	 * @return a string
	 */
	public String information(){
		String s = "Vector: " ;
		s+=String.format("%.2f, %.2f, %.2f , a=%.2f° ", x,y,z,getTilt());
		return s;
	}

	/**
	 * Add another vector to this one and return a new one
	 * @param vector
	 * @return
	 */
	public Vector3D add(Vector3D vector){
		Vector3D v=new Vector3D(this);
		v.plus(vector);
		return v;
	}

	/**
	 * 
	 * @param P
	 */
	public void plus(Vector3D P){
		x+=P.x;
		y+=P.y;
		z+=P.z;
	}

	/**
	 * Combines 2 vectors by adding them and returns a new one
	 * @param v1 a vector
	 * @param v2 another vector
	 * @return 2 vectors added together
	 */
	public static Vector3D combine(Vector3D v1,Vector3D v2){
		Vector3D sum=v1;
		sum.plus(v2);
		return sum;
	}

	/**
	 * 
	 * @param P
	 * @return
	 */
	public Vector3D multiply(Vector3D P){
		Vector3D v= new Vector3D();
		v.x= x*P.x;
		v.y= y*P.y;
		v.z= z*P.z;

		return v;
	}


	/**
	 * Sets all components to 0
	 */
	public void setZero(){
		x= 0;  y= 0;  z= 0;
	}

	/**
	 * 
	 * @param xx
	 * @param yy
	 * @param zz
	 */
	public void setTo(float xx, float yy, float zz){
		x=xx; y=yy; z=zz;
	}

	public void equals(Vector3D p){
		x=p.x;
		y=p.y;
		z=p.z;
	}


	/**
	 * Calculate the length of the vector
	 * @return the length of the vector
	 */
	public float length(){
		return (float) (Math.sqrt(x*x+y*y+z*z));
	}

	/**
	 * Returns an array of float 
	 * @return an array of float 
	 */
	public float[] toFloatArray(){
		 float[] f = new  float[3];
		 f[0]=x;
		 f[1]=y;
		 f[2]=z;
		 return f;
	}

	/**
	 * Normalises this vector
	 */
	public void norm(){
		float L= length();
		if(L!=0)
		{ x/=L; y/=L;  z/=L;
		}
	}

	/**
	 * Gives a normalised vector of this one
	 * @return a new normalised vector
	 */
	public Vector3D normalised(){
		Vector3D v= new Vector3D(this);
		v.norm();     
		return v;
	}


	/**
	 * Scale this vector
	 * @param sc factor to scale with
	 */
	public void scaleVector(float sc){
		x= sc*x;  y= sc*y;  z= sc*z;
	}


	/**
	 * Scales this vector and returns a new one
	 * @param scale
	 * @return
	 */
	public Vector3D scaledTo(float scale){
		Vector3D v= new Vector3D(this);
		v.scaleVector(scale);
		return v;
	}

	/**
	 * Calculates the dot product
	 * @param v the vector to calculate with
	 * @return the dot product
	 */
	float dot(Vector3D v){
		return(x*v.x+ y*v.y+ z*v.z);
	}	      



	/**
	 * Calculate the Cross product with another vector
	 * 
	 * @return a new vector which is the cross product of this one and the incoming
	 */
	public Vector3D cross(Vector3D vector){
		Vector3D v= new Vector3D();

		v.x= y*vector.z - z*vector.y;
		v.y= z*vector.x - x*vector.z;
		v.z= x*vector.y - y*vector.x;

		return v;
	}



	/**
	 * Gives Unit Vector from this Point to Target
	 * @param target the target vector(position)
	 * @return a new vector which is the unit vector between this and target
	 */
	public Vector3D unitTo(Vector3D target){
		Vector3D v= target.subtract(this).normalised();//new Vector3D(target);
		// v.subtract(this).norm();
		return v;
	}

	/**
	 * Subtracts a vector from this vector
	 * @param vector the vector to subtract from this
	 */
	public void minus(Vector3D vector){
		this.x -= vector.x  ;
		this.y -= vector.y  ;
		this.z -= vector.z  ;
	}

	/**
	 * Subtracts a vector from this and creates a new one without affecting this
	 * @param vector the vector to be subtracted
	 * @return a new vector 
	 */
	public Vector3D subtract(Vector3D vector) {
		Vector3D v= new Vector3D(this);
		v.minus(vector);
		return v;
	}

	/** 
	 * Sets a random vector
	 */
	public void setRandom(float Max){
		x= (float) ((0.5f-Math.random())*Max);
		y= (float) ((0.5f-Math.random())*Max);
		z= (float) ((0.5f-Math.random())*Max);  
	}


	/**
	 * Makes a random Gaussian
	 */
	public void makeGauss(float f){
		float length= length();
		Random r= new Random();
		x= (float) (r.nextGaussian()*length*f);
		y= (float) (r.nextGaussian()*length*f);
		z= (float) (r.nextGaussian()*length*f);
	}

	public void perturbation(float f){
		f= length()*f ;
		x+= (float) ((0.5f-Math.random())*f);
		y+= (float) ((0.5f-Math.random())*f);
		z+= (float) ((0.5f-Math.random())*f);  
	}

	/**
	 * Returns a pertubated vector , of fac % deviation 
	 */
	public Vector3D pertubated(float fac){
		Vector3D v= new Vector3D();
		//	       Res.x= x+ MakeRandSign()*(x*fac);
		//	       Res.y= y+ MakeRandSign()*(y*fac);
		//	       Res.z= z+ MakeRandSign()*(z*fac);

		return v;
	}


	/**
	 *
	 */
	public Vector3D randTurn(float dAzimut, float dTilt){
		Vector3D v= new Vector3D(this);
		//
		//	       v.RotateHoriz(MakeRandSign()*dAzimut);
		//	       v.RotateVert(MakeRandSign()*dTilt);
		//
		return v;
	}

	/**
	 * Return true if vector != 0
	 */
	public boolean notZero(){
		boolean not0;
		not0= (x!=0  ||  y!=0 || z!=0);  
		return(not0);
	}




	/**
	 * Calculate Right-Vector in xz-plane
	 */
	public Vector3D getRightVec(float roll){
		Vector3D v= new Vector3D(this);

		v.rotateHorizontal(90);  
		v.rotateVert(roll);
		v.y=0;

		return v.normalised();
	}



	//////////////  Calculate Up-Vector relative to this
	//	      Vector3D GetUpVec(float roll)
	//	      {
	//	       Vector3D Res;
	//	       Vector3D VRight= this.GetRightVec(roll);
	//
	//	       Res= this.cross(VRight)*(-1);
	//
	//	       return( Res.Normalised() );
	//	      }////::::::::////
	//

	/**
	 *Relative Azimut Angle between this and V
	 */
	public float getRelAzimut(Vector3D v){
		float angle= ( v.getAzimut()- this.getAzimut() );
		if(angle>180)  angle-=360;
		if(angle<-180) angle+=360;
		return( angle );
	}

	/**
	 * Relative Tilt between this and V
	 */
	public float getRelTilt(Vector3D v){
		float tilt= ( v.getTilt()- this.getTilt() );
		return( tilt );
	}

	/**
	 *Get angle between this vector and v (always+)
	 */
	public float getAngleBetween(Vector3D v){
		float ang=0;
		float M=  v.length()*length();
		float dot= dot(v);

		//// Dot(V) må være <= M ellers Math.cos(>1) som gir domain error!!
		if(M!=0 && Math.abs(dot/M)<1 ) ang=(float) Math.acos( dot/M );

		return( ToDEG*ang );
	}

	//
	//	      float GetVerticalAngle(Vector3D P)
	//	      {
	//	       float phi;
	//	       return(phi);
	//	      }
	////::::::::////

	/**
	 * Get azimut where th=0 is parallel to xax!
	 */
	public float getAzimut(){
		float La= (float) Math.sqrt(x*x+z*z);
		float th=0;
		//Vector3D xax= {1,0,0}, XZPrj={x,0,z};
		// th= xax.Angle( XZPrj);         // if z<0 th+=180;

		if(La!=0)
		{
			if(x>0 )      th= (float) (ToDEG*Math.asin(z/La));     // Gives 0..90 & 0..-90(z<0)
			else          th= (float) (180-ToDEG*Math.asin(z/La)); // Gives 90..180..270
			if(th==0)     th= (float) (ToDEG*Math.acos(x/La));     // Gives 0 or 180
			else if(th<0) th+=360;                  // Ajusts 0...-90 to 360...270
		}
		th= (th==360) ? 0: th;

		return(th);
	}

	/**
	 *  Get angle between yax and this vector
	 */
	public float get360Tilt(){
		float tilt=0;
		float th= this.getAzimut();

		if      ( length() ==0 ) tilt=0;
		else if (x>=0)
		{        tilt= (float) (ToDEG*Math.asin(y/length()));
		if(y<0) tilt+=360;
		}
		else     tilt= (float) (180-ToDEG*Math.asin(y/length()));

		tilt= (tilt==360) ? 0: tilt;


		return(tilt);
	}

	/**
	 * Get angle between yax and this vector
	 */
	public float getTilt(){
		float l= length();
		float tilt= (l==0) ? 0 :  (float) (ToDEG*Math.asin(y/l) ) ;
		return tilt;
		//if(y<0) ph*=-1; always positive
	}





	/**
	 * Rotates horizontally around Y axis :  around yax in xz-plane
	 * @param dth
	 */
	public void rotateHorizontal(float dth){
		float xx=x, zz=z;
		x=  (float) (xx*Math.cos(dth*ToRAD)- zz*Math.sin(dth*ToRAD));
		z=  (float) (xx*Math.sin(dth*ToRAD)+ zz*Math.cos(dth*ToRAD));
	}

	/**
	 * Rotates around yax: xz-plane and create a new one
	 */
	public Vector3D rotatedHorizontal(float dth){
		Vector3D v= new Vector3D(this);
		float xx= v.x;
		float zz= v.z;

		v.x=  (float) (xx*Math.cos(dth*ToRAD)- zz*Math.sin(dth*ToRAD));
		v.z= (float) (xx*Math.sin(dth*ToRAD)+ zz*Math.cos(dth*ToRAD));

		return v;
	}

	/**
	 *  Rotates around Xax: yz-plane
	 */
	public void rotateX(float dth)
	{
		float yy=y, zz=z;
		y=  (float) (yy*Math.cos(dth*ToRAD)- zz*Math.sin(dth*ToRAD));
		z=  (float) (yy*Math.sin(dth*ToRAD)+ zz*Math.cos(dth*ToRAD));
	}

	/**
	 *	Rotates around Zax: yx-plane: Rel to Xax
	 */
	public void rotateZ(float dth)
	{
		float yy=y, xx=x;
		x=  (float) (xx*Math.cos(dth*ToRAD)- yy*Math.sin(dth*ToRAD));
		y=  (float) (xx*Math.sin(dth*ToRAD)+ yy*Math.cos(dth*ToRAD));
	}

	/**
	 * Rotates (tilts) vector(+dth) towards yaxis
	 */
	public void rotateVert(float dth){
		float yOld=y;
		float XZ=0;
		//Horizontal Projection
		float XZo= (float) Math.sqrt(x*x+ z*z);

		y=   (float) (XZo*Math.sin(dth*ToRAD)+ yOld*Math.cos(dth*ToRAD));
		XZ=  (float) (XZo*Math.cos(dth*ToRAD)- yOld*Math.sin(dth*ToRAD));
		if( XZo!=0){
			x*=  XZ/XZo;
			z*=  XZ/XZo;
		}
	}


	//
	//	      ///////// Moves a point relative to a direction ////////
	//	      void MoveRel(float side, float forw, float up, Vector3D Dir)
	//	      {
	//	        Vector3D DirNorm= Dir.Normalised();
	//
	//	        ///// Move Up //////   Absolute up= along y
	//	        y+=  up;
	//	        
	//	        /////// Move Forward
	//	        *(this)+= (DirNorm*forw);
	//
	//	        //////// Move Sideway   // Absolute sideway= along xz-plane
	//	        DirNorm.RotateHoriz(90);
	//	        DirNorm.y=0;
	//	        *(this)+= (DirNorm*side);
	//
	//	      }////::::::::////
	//
	//






}
