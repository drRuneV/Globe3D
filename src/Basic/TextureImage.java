package Basic;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import Basic.Vector2D;



public class TextureImage {

	private Texture texture;
	private int width=1;
	private int height=1;

	
	/**
	 * Constructor 
	 * @param file a path to image file
	 */
	public TextureImage(String file){
		String s = "res/plancton960x623.jpg";
		s=  (file.length()<2) ? s: file ;
		loadTexture(s);
		
	}
	
	/**
	 * Uploading the texture from a image file
	 * @param file The file to load the texture from
	 */
	private void loadTexture(String file) {
		String ext= (file.contains("jpg")) ? "JPG" : "PNG" ;

		try {
			// load texture from PNG file
			texture = TextureLoader.getTexture(ext, 
					ResourceLoader.getResourceAsStream(file));//"res/plancton960x623.jpg"));



			System.out.println("Texture loaded: "+texture);
			System.out.println(">> Image width: "+texture.getImageWidth());
			System.out.println(">> Image height: "+texture.getImageHeight());
			System.out.println(">> Texture width: "+texture.getTextureWidth());
			System.out.println(">> Texture height: "+texture.getTextureHeight());
			System.out.println(">> Texture ID: "+texture.getTextureID());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Draw the texture at the given position scaled and rotated
	 * @param position
	 * @param scale
	 * @param angle
	 */
	public void drawTexture(Vector2D position,float scale, float angle) {
		int w= (int) (texture.getImageWidth()*scale);
		int h= (int) (texture.getImageHeight()*scale);
		Dimension dimension= new Dimension(w, h);
		Rectangle r= new Rectangle(position.asPoint(), dimension);
		drawTexture(r, angle);
	}
	
	/**
	 * Drawing the texture
	 * @param r Rectangle to draw within
	 * @param angle Rotation angle
	 */
	public void drawTexture(Rectangle r, float angle) {
		
		//Texture
		glEnable(GL_TEXTURE_2D);
		
//	    glEnable(GL_CULL_FACE); //this created a buggy behaviour where the geometry was invisible
		 glDisable(GL_CULL_FACE);
		 glDisable(GL_LIGHTING);
		 glDisable(GL_COLOR_MATERIAL);
		 glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
		 
		Color.white.bind();
//		texture.bind(); // or 
		glBindTexture(GL_TEXTURE_2D,texture.getTextureID());
		
		//Transform coordinate system
//		glMatrixMode(GL_MODELVIEW);
//		glLoadIdentity(); // ?
		glPushMatrix();
		glTranslatef(r.x,0f, r.y);//+r.width/2, r.y+r.height/2,0f);
		glRotatef(angle, 0,1,0);
		//
		float dw= r.width/2;
		float dh= r.height/2;
		float y = 00 ;
		float sc=1f;

//		System.out.println(" Drawing texture at: "+r.toString()+" "+dw);
		
//		glColor4f(1.0f,0f, 0f ,1.0f);
		//		draw2D(dw, dh);
		glBegin(GL_QUADS);
			glTexCoord2f(0,0);
			glVertex3f(-dw*sc,y,-dh);
	
			glTexCoord2f(1,0);			
			glVertex3f(dw*sc,y,-dh);
	
			glTexCoord2f(1,1);			
			glVertex3f(dw,0,dh);
	
			glTexCoord2f(0, 1);
			glVertex3f(-dw,0,dh);//dx+x2,100+texture.getTextureHeight());
		glEnd();
		
//		dw= 200; 
//		glDisable(GL_TEXTURE_2D);
//		glDisable(GL_LIGHTING);
//		  glBegin(GL_TRIANGLES);
//	      glColor3f(0.0f, 0.0f, 1.0f);
//	      glTexCoord2f(0,0);
//	      glVertex3f(-1*dw, 0, -1*dw);
//	      
//	      glColor3f(0.0f, 1.0f, 0.0f);
//	      glTexCoord2f(0,1);
//	      glVertex3f(1*dw, 0, -1*dw);
//	      
//	      glColor3f(1.0f, 0.0f, 0.0f);
//	      glTexCoord2f(1,0);
//	      glVertex3f(0.0f, dw, -1*dw);
//	      glEnd();
	   
	
		glPopMatrix();

		glDisable(GL_TEXTURE_2D);
	}

	/**
	 * @param dw
	 * @param dh
	 */
	private void draw2D(float dw, float dh) {
		glBegin(GL_QUADS);
			glTexCoord2f(0,0);
			glVertex2f(-dw,-dh);
			
			glTexCoord2f(1,0);			
			glVertex2f(dw,-dh);
			
			glTexCoord2f(1,1);			
			glVertex2f(dw,dh);
			
			glTexCoord2f(0,1.0f);
			glVertex2f(-dw,dh);//dx+x2,100+texture.getTextureHeight());
		glEnd();
	}
	
	

	/**
	 * @return the texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}


	
}



