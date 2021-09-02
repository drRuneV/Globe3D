package Utility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Colors.ColorGradient;
import distribution.Distribution;
import net.miginfocom.swing.MigLayout;
import ucar.nc2.NetcdfFile;

public class RescaleFrame extends JFrame{

	Distribution distribution= null;
	

	
	public RescaleFrame(Distribution distribution){
		this.distribution=distribution;
		setupFrame();		
	}
	
	private void setupFrame(){
        JLabel labelUnit= new JLabel("Unit");
        JLabel labelScale= new JLabel("Scaling");
        JLabel labelUnitnew= new JLabel(":");
        TextField unitText= new TextField(distribution.getUnit()); 
        TextField scaleText= new TextField("1.0");
        setLayout(new BorderLayout());
        JPanel panel= new JPanel(new MigLayout());
        //
        JButton button= new JButton("Apply");
        button.addActionListener(new ActionListener() {

        	// Action for the apply button 
			@Override
			public void actionPerformed(ActionEvent e) {
				float scale= Float.parseFloat(scaleText.getText().replace(',', '.'));
				String unit= unitText.getText();
				// 
				distribution.setUnit(unit);
				rescale(scale);
				//
				labelUnitnew.setText(unit);
				labelUnitnew.setForeground(Color.green);
				setTitle(distribution.getFullName()+" : "+ distribution.getUnit());
				ColorGradient c=distribution.getGradient();
				c.setMax(distribution.findMaxMin()[0]);
				c.reDefine();
			}
		});

		//
		panel.add(labelUnit);
		panel.add(unitText);
		panel.add(labelUnitnew,"wrap");
		
		panel.add(labelScale);
		panel.add(scaleText,"wrap,grow 3");
		panel.add(button);

	// Add the panel with the components to the centre
    add(panel, BorderLayout.CENTER);
	setAlwaysOnTop(true);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(400, 150);
    setLocation(400, 400);
    setVisible(true);
    setTitle(distribution.getFullName()+" : "+ distribution.getUnit());
	
	}
	
	/**
	 * Rescale the entire dataset
	 * @param scale the factor to scale the dataset with
	 */
	private void rescale(float scale){
		distribution.rescale(scale);
//		
//		float[] value = distribution.getValues() ;
//		float f=0;
//
//		for (int i = 0; i < value.length; i++) {
//			f= distribution.getValues()[i];
//			if (f!=distribution.getMissingV() && f!=distribution.getFillV() ) {
//			distribution.getValues()[i]*= scale;
//			System.out.println(f);
//		}
//		}

	}

	/**
	 * Re-scales automatically if the distribution has the unit ug C
	 */
	public void rescaleAutomatically(){
		// only for this unit 
		if (distribution.getUnit().contains("ug C")) {
			String unit= distribution.getUnit() ;
			unit= unit.replaceAll("ug", "g");
			System.out.println(unit);
			
			distribution.setUnit(unit);
			rescale(1/1000000f);
			ColorGradient c=distribution.getGradient();
			c.setMax(distribution.findMaxMin()[0]/2);
			c.reDefine();
			System.out.println("new Unit: "+distribution.getUnit());
		}
	}

	
//	public static void main(String[] args) {
//		
//		String p="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
//		NetcdfFile ncfile2;
//		try {
//			ncfile2 = NetcdfFile.open(p+"ibm2felt.nc");
//			VisualDistribution vdcal= new VisualDistribution("CFbiom", ncfile2);
//			RescaleFrame frame= new RescaleFrame(vdcal);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}
