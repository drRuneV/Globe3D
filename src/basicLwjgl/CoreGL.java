package basicLwjgl;


import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Point;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;

import Basic.Spring;

//import com.sun.glass.events.KeyEvent;

import GeometryGL.SelectBox;


public class CoreGL extends BasicGame {
	

	private  Spring spring= null;
	private int count=0;
	java.awt.Point mouseP = new java.awt.Point();
	String input = "" ;
	SelectBox box= null;
	
	public CoreGL(String title) {
		super(title);
		box = new SelectBox(5, 50, 50, 50) ;
	}
	
	

	public static void main(String[] args) throws SlickException {
		CoreGL program= new CoreGL(" NORWECOM.E2E 3D Visualisation ");

		AppGameContainer ap = new AppGameContainer(program);

		ap.setDisplayMode(900, 600, false);
		ap.start();

	}



	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		System.out.println("clicking the mouse");
		box.setLocation(x, y);
		
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		int dx= newx-oldx  ;
		if (dx>5) {
			System.out.println("Drag dx= "+dx);
		}
//		System.out.println("oldx : " + oldx+" new x"+ newx ); 
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		mouseP.setLocation(newx, newy);
		box.setSelected(box.contains(mouseP));
		// 
		if (box.isSelected()) {
			box.setLocation(mouseP.x-box.width/2, mouseP.y-box.height/2);
		}
		
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		
	}

	@Override
	public void mouseWheelMoved(int change) {
		
	}

	@Override
	public void keyPressed(int key, char c) {
		input += c;
		System.out.println("pressed the key: "+ c);
		
		if(key == Keyboard.KEY_DELETE){
			input="";
		}
			
	}

	@Override
	public void keyReleased(int key, char c) {
		System.out.println("released the key: "+ c);
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		
//		spring.update(count*0.01f);
		g.setColor(Color.red);
		g.drawRect(50, 50, 300, 25);
		g.setColor(Color.lightGray);
		g.drawString(input, 51, 55);
		
		if (input.toLowerCase().contains("berry")) {
			g.setColor( Color.blue);
			Circle ci = new Circle(mouseP.x, mouseP.y, 40) ;
			g.draw(ci);
		}
		
		box.draw(g);
		
	count++;
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		container.getGraphics().setAntiAlias(true);
		container.setShowFPS(true);
			
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		// TODO Auto-generated method stub
		
	}

}
