package main.control;

import java.util.ArrayList;

public class Queue {

	private ArrayList<Integer> buffer = null;
	private boolean empty = true;
	private static Queue instance;

	private Queue() {
		buffer = new ArrayList<>();
	}

	// insert value from sensor to the end of the buffer
	// called from sensor to put values in the Queue
	public synchronized void addToBuffer(int value) {
		buffer.add(value);
		empty = false;
		//notify(); // **ellers er den pågældende tråd i getBuffer fanget??
	}

	// returns and clears the buffer
	public synchronized ArrayList<Integer> getBuffer() {
		// System.out.println("Forsøger at hente buffer: " +
		// this.getClass().getName());
		// if the buffer is empty, the thread is put to sleep
		if (empty) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// clear the buffer and returns
		empty = true;
		ArrayList<Integer> placeHolder = new ArrayList<>();
		placeHolder = buffer;
		buffer = new ArrayList<>();
		notify();
		return placeHolder;
	}

	public static Queue getInstance() {
		if (instance == null)
			instance = new Queue();
		return instance;
	}

}
