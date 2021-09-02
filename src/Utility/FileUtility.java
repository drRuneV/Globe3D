package Utility;

import java.awt.Component;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import distribution.Distribution;



public class FileUtility {

	
	
	
	/**
	 * Creates a file from a file dialogue for saving 
	 * @param title the title of the dialogue
	 * @param extension expected file extension
	 * @return a new file
	 */
	public static File createFileFromFileDialogue(String title,String extension,Component parent ){

		String initialDirectory="./res/"; //relative path to current directory
		//Saving Dialogue
		JFileChooser fcd = new JFileChooser(initialDirectory);
		// Title and font
		fcd.setDialogTitle(title);
		fcd.setFont(new Font("Times", Font.PLAIN, 14));
		

		//    	System.out.println("Current directory: "+fcd.getCurrentDirectory().getAbsolutePath());
		//fcd.setal
		fcd.showSaveDialog(parent);

		// Return the file from the dialogue
		File file = null;
		if (fcd.getSelectedFile()!=null) {
			String name= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
			if (!name.contains(extension)) {
				name+=extension;
			}
			file =  new File(name );
		}
		return file;
	}
	
	
	public static void writeTextFile(File file,StringBuilder sb){

		if (file!=null) {
			try {
				FileWriter fileWriter = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fileWriter);
				//				StringBuilder sb=createStringLinesOfValues();
				bw.write(sb.toString());

				//    			System.out.println("Information writing the information"+sb.toString());
				//
				bw.flush();
				bw.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static String openFileFromDialogue(String path, String title){
		String filename="";
		String initialDirectory= (path!=null) ?path : "./res/"; //relative path to current directory
		JFileChooser fcd = new JFileChooser(initialDirectory);
		fcd.setDialogTitle(title);
		fcd.showOpenDialog(null );

		// Return the file
		if (fcd.getSelectedFile()!=null) {
			filename= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
		}
		return filename;
	}
	

	/**
	 * Opens a netCDF file triggered from a keyboard shortcut (Ctrl+Shift+o)
	 */
	public static NetCDFPanel openAndHandleNetCDF(String path,Distribution distribution) {
		NetCDFPanel netCDFPanel= null;
//		String initialDirectory= (path!=null) ?path : "./res/"; //relative path to current directory
//		JFileChooser fcd = new JFileChooser(initialDirectory);
//		fcd.setDialogTitle("Open netCDF file...");
//		fcd.showOpenDialog(null );

		String filename= openFileFromDialogue(path, "Open netCDF file…");
		if (!filename.isEmpty()) {
			// Open as a distribution
			netCDFPanel = new NetCDFPanel(filename,distribution,0 );
			netCDFPanel.show();
//		}
			
		}
		// Return the file
//		if (fcd.getSelectedFile()!=null) {
//			String filename= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
			// Open as a survey line based on the name
/*			if (filename.contains("survey") && distribution!=null) {
				SurveyLines surveyLine= new SurveyLines(filename,distribution.getCoordinates());
				//setSurveyLine(surveyLine);					
				ecosystem.setSurveyLine(surveyLine);						
			} */
			//Open as a distribution
//			netCDFPanel = new NetCDFPanel(filename,distribution);
//			netCDFPanel.show();
//		}
		return netCDFPanel;
	}
	
	public static void main(String[] args) {
		FileUtility u=new FileUtility();
		String path = "C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/November 2020/";
		NetCDFPanel netCDFPanel= u.openAndHandleNetCDF(path,null );
//		String name=netCDFPanel.getDistribution().getFullName();
//		System.out.println(name);
		
		
	}
	
	
	
}
