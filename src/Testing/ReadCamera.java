package Testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.amazonaws.util.IOUtils;

import Utility.Coordinate;
import Utility.FileUtility;
import basicLwjgl.Camera;

// // 
// // 
public class ReadCamera {
	
//	String path =   "./res/Camera list1.txt";
	static String defaultFile= "./res/Camera list 1.txt";
	String filename= null;
	
	public ReadCamera(String filename) {
		this.filename=filename;
		openCameraFile(filename);
	}
	

	public static void savingCameraFile(ArrayList<Camera> cameraList){
		String sep= System.lineSeparator();
		String path = "./res/" ;
		String filename= FileUtility.openFileFromDialogue(path, "Open netCDF file…");
		//  
		if ( !filename.isEmpty() ) {
			StringBuilder sb= new StringBuilder() ;
			String line = "" ;
			// latitude long angle height…
			for (Camera camera : cameraList) {
				Coordinate c= camera.getCoordinate() ;
				line = String.format("%.2f %.2f %.1f %.1f",c.getLat(), c.getLon(), 
						camera.getAngle(),camera.getHeight());
				sb.append(line+sep);
			}
			// Writing to file
			FileUtility.writeTextFile(new File(filename), sb);
			System.out.println("Was trying to write:  "+sb.toString());
			
			
		}
	}
	
	
	public static ArrayList<Camera> openCameraFile(String filename) {
		String  name= (filename!=null) ? filename: defaultFile ;
		ArrayList<Camera> cameraList= new ArrayList<>();
		
		File file =  new File(name);
		if (file!=null) {

			try(FileInputStream inputStream = new FileInputStream(file)) {     
				String textToScan = IOUtils.toString(inputStream);
				// Creates a camera list from lines of text
				cameraList= scanLinesOfText(textToScan);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println(" Cannot find the file: "+name);
			} catch (IOException e) {
				System.out.println(" Cannot handle the file: "+name);
				e.printStackTrace();
			}
		}
		return cameraList;
	}
	
	
	
	
	
	
	public static ArrayList<Camera> scanLinesOfText(String text){
		Scanner sc = new Scanner(text);
		ArrayList<Camera> list= new ArrayList<>();
		//

		int n=0;
		String line="";
		while(sc.hasNextLine()){
			line= sc.nextLine();
			Camera c= parseCamera(line);
			list.add(c);
			n++;
		}
		sc.close();

		return list;
	}
	
	private static Camera parseCamera(String line) throws  NumberFormatException{

		Coordinate c = new Coordinate(0,0) ;
		Scanner sc= new Scanner(line);
		String field="";

		// First latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
		// Then Longitude
		field= sc.next().replaceAll(",", ".");
		float  lon= Float.parseFloat(field);
		c.setLon(lon);
		// angle
		field= sc.next().replaceAll(",", ".");
		float  angle= Float.parseFloat(field);
		// distance 
		field= sc.next().replaceAll(",", ".");
		float   distance= Float.parseFloat(field);

		// Camera
		Camera camera= new Camera(c, distance);
		camera.setAngle(angle);
//		camera.setHeight(distance);
		// 
		sc.close();
		
		return camera;
	}

	
		


	public static void main(String[] args) {
		 ArrayList<Camera> list= new ArrayList<>() ;
		 list =  ReadCamera.openCameraFile(null);
		
		 for (Camera camera : list) {
			System.out.println( camera.info() );
		}
		
		
	}

}
