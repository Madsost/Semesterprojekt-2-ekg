package main.control;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public interface Sensor {
	
	public void init();
	
	public ArrayList<Double> getValues();
	
	public void attachListener(ActionListener l);
	
	public void detachListener(ActionListener l);
	
	public void notification();	
}
