package main.control;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public interface Sensor {
	
	public void init();
	
	public ArrayList<Double> getValues();
}
