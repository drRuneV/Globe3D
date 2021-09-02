package basicLwjgl;

import org.lwjgl.util.Color;
import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;



/**
 * An OpenGL material class very basic
 *
 */
public class GLMaterial {

	Color ambient;
	Color diffuse;
	Color emission;
	Color specular;
	
	int shininess=20; // was 60
	
	/**
	 * Constructor
	 */
	public GLMaterial() {
		// 
		ambient= new  Color(5, 5 , 5, 255); // was 50
		diffuse= new  Color(100, 100, 100, 255);
		emission= new Color(0, 0, 0, 255);
		specular= new  Color(255, 255, 55, 255);
	}
	


	/**
	 * Defines all material properties by setting OpenGL material functions
	 */
	public void applyMaterial(){
	
		FloatBuffer am= toFloatBuffer(ambient);
		FloatBuffer dm= toFloatBuffer(diffuse);
		FloatBuffer sm= toFloatBuffer(specular);
		FloatBuffer em=toFloatBuffer(emission);
				
		glMaterial( GL_FRONT_AND_BACK, GL_AMBIENT  , am  );
		glMaterial( GL_FRONT_AND_BACK, GL_DIFFUSE , dm);
		glMaterial( GL_FRONT_AND_BACK, GL_SPECULAR , sm);
		glMaterial( GL_FRONT_AND_BACK, GL_EMISSION, em);
		glMateriali( GL_FRONT_AND_BACK, GL_SHININESS ,shininess );
	}



	/**
	 * Define one of the colour components using a standard awt.Java colour
	 * @param color the colour java.awt.Color
	 * @param which which of the colour components do we want to set
	 */
	public void define(java.awt.Color color, String which){
		
		switch(which.toLowerCase()){
		case "ambient":
			ambient.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			break;
	
		case "diffuse":
			diffuse.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			break;
	
		case "emission":
			emission.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			break;
	
		case "specular":
			specular.set(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			break;
		}
	}



	/**
	 * Converts colour to float buffer
	 * @param c
	 * @return
	 */
	private FloatBuffer toFloatBuffer(Color c){
		FloatBuffer  buffer=BufferUtils.createFloatBuffer(4);

		buffer.put(c.getRed());
		buffer.put(c.getGreen());
		buffer.put(c.getBlue());
		buffer.put(c.getAlpha() );
		buffer.flip();
		
		return buffer;
	}
	
	
	/**
	 * @return the ambient
	 */
	public Color getAmbient() {
		return ambient;
	}


	/**
	 * @param ambient the ambient to set
	 */
	public void setAmbient(Color ambient) {
		this.ambient = ambient;
	}


	/**
	 * @return the diffuse
	 */
	public Color getDiffuse() {
		return diffuse;
	}


	/**
	 * @param diffuse the diffuse to set
	 */
	public void setDiffuse(Color diffuse) {
		this.diffuse = diffuse;
	}


	/**
	 * @return the emission
	 */
	public Color getEmission() {
		return emission;
	}


	/**
	 * @param emission the emission to set
	 */
	public void setEmission(Color emission) {
		this.emission = emission;
	}


	/**
	 * @return the specular
	 */
	public Color getSpecular() {
		return specular;
	}


	/**
	 * @param specular the specular to set
	 */
	public void setSpecular(Color specular) {
		this.specular = specular;
	}


	/**
	 * @return the shininess
	 */
	public int getShininess() {
		return shininess;
	}


	/**
	 * @param shininess the shininess to set
	 */
	public void setShininess(int  shininess) {
		this.shininess = shininess;
	}
	


	
}

