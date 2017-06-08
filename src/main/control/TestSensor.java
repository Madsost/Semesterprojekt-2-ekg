package main.control;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public class TestSensor implements Sensor, Observed {

	// sætter instansen op af Queue så det er den samme som databaseConn tilgår
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
