package main.control;

import java.util.ArrayList;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class Queue {

	private ArrayList<Integer> buffer = null;
	private boolean empty = true;
	private static Queue instance;

	/**
	 * 
	 */
	private Queue() {
		buffer = new ArrayList<>();
	}

	// insert value from sensor to the end of the buffer
	// called from sensor to put values in the Queue
	/**
	 * 
	 * @param value
	 */
	public synchronized void addToBuffer(int[] value) {
		if (!empty) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (int number : value) {
			buffer.add(number);
		}
		empty = false;
		notify();
	}

	// returns and clears the buffer
	/**
	 * 
	 * @return
	 */
	public synchronized ArrayList<Integer> getBuffer() {
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

	/**
	 * 
	 * @return
	 */
	public static Queue getInstance() {
		if (instance == null)
			instance = new Queue();
		return instance;
	}

}
