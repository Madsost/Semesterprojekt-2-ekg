package main.model;

import java.util.ArrayList;

import main.control.Queue;

public class DatabaseConn {

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

}
