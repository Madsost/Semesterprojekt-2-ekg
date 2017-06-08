package main.control;

import java.util.ArrayList;

public class Queue {

	ArrayList<Double> buffer = new ArrayList<Double>();
	boolean empty = true;
	static Queue instance;

	private Queue() {

	}

	// insert value from sensor to the end of the buffer
	public void addToBuffer(double value) {
		buffer.add(value);
		empty = false;
		notify(); // **ellers er den pågældende tråd i getBuffer fanget??
	}

	// returns and clears the buffer
	public synchronized ArrayList<Double> getBuffer() {

		// if the buffer is empty, the thread is put to sleep
		while (empty) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// clear the buffer and returns
		empty = true;
		ArrayList<Double> placeHolder = new ArrayList<Double>();
		placeHolder = buffer;
		buffer.clear();
		notify();
		return placeHolder;
	}

	public static Queue getInstance() {
		if (instance == null)
			instance = new Queue();
		return instance;
	}

}
