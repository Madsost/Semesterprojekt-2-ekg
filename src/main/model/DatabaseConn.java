package main.model;

import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.control.Observed;
import main.control.Queue;

public class DatabaseConn implements Observed{

	private List<ActionListener> lyttere = new ArrayList<ActionListener>();
	
	static DatabaseConn instance;
	Queue q = Queue.getInstance();

	private DatabaseConn() {
		
	}

	public static DatabaseConn getInstance() {
		if (instance == null)
			instance = new DatabaseConn();
		return instance;
	}

	// put the raw data from the sensor in the database
	public void addData(ArrayList<Double> data) {

	}

	// put the calculated pulse in the database
	public void addPulse(int pulse) {

	}

	public ArrayList<Integer> getData(int length) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		// fetch "length" numbers of measurement from the
		// database and put it in an ArrayList

		return list;
	}

	public int getPulse() {
		int pulse = 0;

		// get the pulse from the database

		return pulse;
	}

	@Override
	public void attachListener(ActionListener l) {
		lyttere.add(l);
		
	}

	@Override
	public void detachListener(ActionListener l) {
		lyttere.remove(l);
		
	}

	@Override
	public void notification(String string) {
		ActionEvent event = new ActionEvent(this, 0, string);
		for(Iterator<ActionListener> i = lyttere.iterator(); i.hasNext();){
			ActionListener l = (ActionListener)i.next();
			l.actionPerformed(event);

		}
		
	}

}
