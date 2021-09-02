package GeometryGL;

import java.util.ArrayList;

import distribution.Distribution;

public class DistListHandler {
	
	// Distribution list  
	private ArrayList<DistributionGL> disList= new ArrayList<>();
	private DistributionGL selected= null;
	private int index=0;
	private int count=0;
	
	
	
	/**
	 * Constructor
	 */
	public DistListHandler() {
		
	}
	

/**
 * Adds a distribution to the list of distributions
 * @param distribution  the distribution to add
 */
public void addDistribution(Distribution distribution) {
	if (distribution!=null) {
		DistributionGL dGL= new DistributionGL(distribution, disList.size());
		disList.add(dGL);
//		showDistribution=false;
		// Sets all distributions to time step 0
		count= 0;
		synchroniseDistributions(true);
	}
}
	
	/**
	 * Set All distributions to the time counter (the day) of the first
	 * @param fromStart if all distributions are gonna be reset to time step 0
	 */
	public void synchroniseDistributions(boolean fromStart){
		int t= (fromStart) ? 0 : disList.get(0).getDistribution().getCount();
		for (DistributionGL d : disList) {
			int time = Math.min(d.getDistribution().getTime()-1, t);
			d.getDistribution().setCount(time);
		}
	}
	
	
	/**
	 * Removes a distributionGL from the list
	 * @param selected the distributionGL to remove
	 * @return the first distribution in the list or null 
	 */
	public void removeDistribution(){
		disList.remove(selected);
		if (!disList.isEmpty()) {
			selected = disList.get(0) ;
		}
	}
	

/**
 * Sets all distribution visible or invisible
 * @param visible visible or invisible
 */
public void visibilityOfDistributions(boolean visible) {
	for (DistributionGL distributionGL : disList) {
		distributionGL.setVisible(visible);
	}
}
	
	
/**
 * Changes to the next/previous distribution in the list.
 * Index keeps track of which distribution is selected.
 * @param di  step size
 * @return the selected distributionGL
 */
public DistributionGL changeDistribution(int di) {
	int max = disList.size()-1 ;
	index+= di;
	index= (index> max) ? 0 : (index<0) ? max:  index;
	DistributionGL dGL=  (disList.isEmpty())? null : disList.get(index);
	selected = dGL ;
	
	System.out.println("Distribution : "+ dGL.getDistribution().getFullName());
	return dGL;
}


/**
 * @return the disList
 */
public ArrayList<DistributionGL> getDisList() {
	return disList;
}


/**
 * @param disList the disList to set
 */
public void setDisList(ArrayList<DistributionGL> disList) {
	this.disList = disList;
}


/**
 * @return the index
 */
public int getIndex() {
	return index;
}


/**
 * @param index the index to set
 */
public void setIndex(int index) {
	this.index = index;
}


/**
 * @return the selected
 */
public DistributionGL getSelected() {
	return selected;
}


/**
 * @param selected the selected to set
 */
public void setSelected(DistributionGL selected) {
	this.selected = selected;
}	
	

}
