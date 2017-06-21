package main.control;

import java.util.ArrayList;

import main.util.Filter;

/**
 * Queue-klasse, grænseflade mellem sensor og database.
 * 
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
 *
 */
public class Queue {

	private ArrayList<Double> buffer = null;
	private boolean empty = true;
	private static Queue instance;

	/**
	 * Privat konstruktør. Kaldes, første gang der kaldes
	 * <code>Queue.getInstance()</code>.
	 */
	private Queue() {
		buffer = new ArrayList<>();
	}

	/**
	 * insert value from sensor to the end of the buffer, called from sensor to
	 * put values in the Queue
	 * 
	 * @param value
	 *            array med input-værdier.
	 */
	public synchronized void addToBuffer(double[] value) {
		if (!empty) { /* 1A */
			try {
				wait();
			} catch (InterruptedException e) { /* 1B */
				e.printStackTrace();
			}
		}
		for (double number : value) { /* 1C */
			buffer.add(Filter.doNotch(number));
		}
		empty = false;
		notify();
	}

	/**
	 * returns and clears the buffer
	 * 
	 * @return de 250 målinger der ligger i bufferen som en ArrayList af
	 *         doubles.
	 */
	public synchronized ArrayList<Double> getBuffer() {
		// if the buffer is empty, the thread is put to sleep
		if (empty) { /* 2A */
			try {
				wait();
			} catch (InterruptedException e) { /* 2B */
				e.printStackTrace();
			}
		}
		// clear the buffer and returns
		empty = true;
		ArrayList<Double> placeHolder = new ArrayList<>();
		placeHolder = buffer;
		buffer = new ArrayList<>();
		notify();
		return placeHolder;
	}

	/**
	 * Laver en instans af køen hvis den ikke er instantiereret.
	 * 
	 * @return instansen af Queue - altid den samme.
	 */
	public static Queue getInstance() {
		if (instance == null)
			instance = new Queue();
		return instance;
	}

}
