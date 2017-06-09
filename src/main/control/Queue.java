package main.control;

import java.util.ArrayList;

public class Queue {

	private ArrayList<Integer> buffer = new ArrayList<>();
	private boolean empty = true;
	private static Queue instance;

	private Queue() {

	}

	// insert value from sensor to the end of the buffer
	public void addToBuffer(int value) {
		buffer.add(value);
		empty = false;
		notify(); // **ellers er den pågældende tråd i getBuffer fanget??
	}

	// returns and clears the buffer
	public synchronized ArrayList<Integer> getBuffer() {

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
		ArrayList<Integer> placeHolder = new ArrayList<>();
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
