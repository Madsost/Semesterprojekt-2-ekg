package main.control;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public class TestSensor implements Sensor, Observed {

	// s�tter instansen op af Queue s� det er den samme som databaseConn tilg�r
	Queue q = Queue.getInstance();
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public ArrayList<Double> getValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attachListener(ActionListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void detachListener(ActionListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notification(String string) {
		// TODO Auto-generated method stub
		
	}

}
