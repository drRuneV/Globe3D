package basicLwjgl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import Colors.ColorInt;

public class Light {
	
	Vector3D position= new Vector3D(500,500,1);
	Vector3D direction= new Vector3D(  -1.0f, -1.0f, -1.0f);
	
	ColorInt ambient= new ColorInt(0.1f, 0.1f, 0.1f, 1.0f);
	ColorInt diffuse= new ColorInt(Color.gray);
	ColorInt specular= new ColorInt(0.8f, 0.9f, 0.9f, 1.0f);
	
	float spotExponent= 20;
	float spotCutoff=   40;
	float fade[]= new float[]{ 0.0f, 0.001f, 0.0001f} ;
	boolean isDirection= true;
	boolean isSpot=false;
	private int id=0;
	private boolean useRotating=false;

	
	/**
	 * Constructor
	 */
	public Light(int id, Vector3D position	, boolean isDirection, boolean isSpot) {
		this.id = id ;
		this.position = position ;
		this.isDirection= isDirection;
		this.isSpot= isSpot;
		
	}
	
	/**
	 * Sets all like properties equal to the given light
	 * @param light light to set equal to
	 */
	public void justLike(Light light) {
		position.equals(light.position);
		direction.equals(light.direction);
		ambient=light.ambient;
		specular= light.specular ;
		diffuse= light.diffuse ;
		spotExponent = light.spotExponent ;
		spotCutoff= light.spotCutoff ;
		isSpot = light.isSpot ;
		isDirection = light.isDirection ;
		for (int i = 0; i < fade.length; i++) {
			fade[i]= light.fade[i] ;
		}

	}
	

	
	/**
	 * 
	 */
	private void defineTheLight(){
		int dir0pos1= (isDirection) ? 0:1 ;
		direction.norm();
		Vector3D vector= (isDirection) ? new Vector3D(direction) : new Vector3D(position);
		
		glEnable(GL_LIGHTING);
//		glLightModel(GL_LIGHT_MODEL_AMBIENT, toBuffer(ambient));
		glEnable(GL_LIGHT0+id);
		
		
		if (isSpot) {
			glLight(GL_LIGHT0+id , GL_POSITION , toBuffer(position, 1));
			glLight(GL_LIGHT0+id , 	 GL_SPOT_DIRECTION, toBuffer(direction, 0));
		}
		else {
			glLight(GL_LIGHT0+id , GL_POSITION ,toBuffer(vector, dir0pos1));// seems to work when using position even if it is direction
		}
		glLight(GL_LIGHT0+id , GL_AMBIENT ,  toBuffer(ambient));
		glLight(GL_LIGHT0+id , GL_DIFFUSE ,  toBuffer(diffuse));
		glLight(GL_LIGHT0+id , GL_SPECULAR , toBuffer(specular));

	}

	
	
	
	/**
	 * Updates changes in the light
	 * @param count  frame counter
	 */
	public void update(long count){
		
		defineTheLight();
		
		if (useRotating) {
			position.rotateHorizontal(0.5f);
			direction.rotateHorizontal(0.5f);
		}
	}
	
	
	
	/**
	 * Defines a spotlight/Pointlight
	 */
	public void lightProperties(){
		
		glEnable(GL_LIGHT0+id);
		
		if(isSpot){
			glLightf(GL_LIGHT0+id ,  GL_SPOT_EXPONENT, spotExponent  );
			glLightf(GL_LIGHT0+id ,  GL_SPOT_CUTOFF,    spotCutoff);
			glLight(GL_LIGHT0+id , 	 GL_SPOT_DIRECTION, toBuffer(direction, 0));
			glLight(GL_LIGHT0+id , GL_POSITION , toBuffer(position, 1));
//			glLight(GL_LIGHT0+id , 	 GL_SPOT_, toBuffer(direction, 0));
		}
		else {
			glLightf(GL_LIGHT0+id ,  GL_SPOT_CUTOFF,  180);
			glLight(GL_LIGHT0+id , GL_POSITION , toBuffer(position, 1));
		}

		//– For Both Spot and Omni
		glLightf(GL_LIGHT0+id , GL_CONSTANT_ATTENUATION, fade[0]);//Attenu.Cons*(1+Attenu.Variation*cos(BlinkFreq*BlinkCount)) );
		glLightf(GL_LIGHT0+id , GL_LINEAR_ATTENUATION,   fade[1]);//Attenu.Lin*(1+ Attenu.Variation*cos(BlinkFreq*BlinkCount)) );
		glLightf(GL_LIGHT0+id , GL_QUADRATIC_ATTENUATION,fade[2]);//Attenu.Quad*(1+Attenu.Variation*cos(BlinkFreq*BlinkCount)) );

//		glLightf(GL_LIGHT0+id , GL_CONSTANT_ATTENUATION, 1.0f);//Attenu.Cons*(1+Attenu.Variation*cos(BlinkFreq*BlinkCount)) );
//		glLightf(GL_LIGHT0+id , GL_LINEAR_ATTENUATION,   1.10f);//Attenu.Lin*(1+ Attenu.Variation*cos(BlinkFreq*BlinkCount)) );
//		glLightf(GL_LIGHT0+id , GL_QUADRATIC_ATTENUATION, 0.001f);//Attenu.Quad*(1+Attenu.Variation*cos(BlinkFreq*BlinkCount)) );

	}
	
	public void darken(float s){
		fade[1]*=s;
		fade[2]*=s;
		lightProperties();
	}
	
	/**
	 * Sets ATTENUATION
	 * @param c
	 * @param l
	 * @param q
	 */
	public void setFade(float c, float l, float q) {
		fade[0]= c ;
		fade[1]= l;
		fade[2]= q ;
		lightProperties();
	}
	
	
	
	
	
	/**
	 * Define the position and direction given the latitude and longitude
	 * @param latitude
	 * @param distance
	 */
	public void defineAt(float lat, float lon, float distance){

		Vector3D p=  CoreOpenGL.latLonToPoint(lat, lon).scaledTo(distance);
		position.equals(p);
		
		// Unit vector  
//		position= new Vector3D( (float) Math.cos(lat) ,(float) Math.sin(lat) , 0);
		direction.equals(position.scaledTo(-1));
//		position.scaleVector(distance);
		System.out.println("Position: "+position.information());
		System.out.println("Direction: "+direction.information());
	}
	
	
	/**
	 * 
	 * @return
	 */
	public FloatBuffer toBuffer(ColorInt color){
		FloatBuffer b= BufferUtils.createFloatBuffer(4);
		float c[]= color.convertToFloat();
		b.put(c[0]).put(c[1]).put(c[2]).put(c[3]).flip();
		return b;
	}

	/**
	 * Convert position to FloatBuffer
	 * @param p
	 * @param isPosition  0 for direction, 1 for position
	 * @return
	 */
	public FloatBuffer toBuffer(Vector3D p,int isPosition){
		FloatBuffer b= BufferUtils.createFloatBuffer(4);
//		float c[]= color.convertToFloat();
//		System.out.println("ID "+id+" isPos "+1.0f*isPosition);
		
		b.put(p.x).put(p.y).put(p.z).put(1.0f*isPosition).flip();
		return b;
	}



	/**
	 * @return the useRotating
	 */
	public boolean isUseRotating() {
		return useRotating;
	}



	/**
	 * @param useRotating the useRotating to set
	 */
	public void setUseRotating(boolean useRotating) {
		this.useRotating = useRotating;
	}



	/**
	 * @return the specular
	 */
	public ColorInt getSpecular() {
		return specular;
	}



	/**
	 * @param specular the specular to set
	 */
	public void setSpecular(ColorInt specular) {
		this.specular = specular;
	}



	/**
	 * @return the spotExponent
	 */
	public float getSpotExponent() {
		return spotExponent;
	}



	/**
	 * @param spotExponent the spotExponent to set
	 */
	public void setSpotExponent(float spotExponent) {
		this.spotExponent = spotExponent;
	}



	/**
	 * @return the spotCutoff
	 */
	public float getSpotCutoff() {
		return spotCutoff;
	}



	/**
	 * @param spotCutoff the spotCutoff to set
	 */
	public void setSpotCutoff(float spotCutoff) {
		this.spotCutoff = spotCutoff;
	}



	
	 

}
