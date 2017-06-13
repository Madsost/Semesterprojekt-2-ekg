package main.control;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import main.MainApp;
import main.model.DatabaseConn;

public class TestSensor extends Thread implements Sensor {
	private ArrayList<Integer> dataset = new ArrayList<>();
	private final String variant = "60";
	private int count = 0;
	private boolean running = false;

	// s�tter instansen op af Queue s� det er den samme som databaseConn tilg�r
	private Queue q = Queue.getInstance();

	@Override
	public void init() {
		System.out.println("Opsætter testsensor: " + this.getClass().getName());
		try {
			String fileName = "resources/EKGdata_" + variant + ".txt";
			File file = new File(fileName);
			Scanner sc = new Scanner(new FileReader(file));
			ArrayList<Double> temp = new ArrayList<>();
			while (sc.hasNext()) {
				temp.add(Double.parseDouble(sc.nextLine()));
			}

			for (double floating : temp) {
				int integer = (int) Math.round(floating * 10000);
				// System.out.println(floating);
				dataset.add(integer);
			}
			// for(int tal : dataset){
			// System.out.println(tal);
			// }
			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		running = true;
		System.out.println("Starter sensor-tråd: " + this.getClass().getName());

		while (running) {
			if (count >= dataset.size())
				count = 0;
			q.addToBuffer(dataset.get(count));
			count++;
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * public static void main(String[] args) { Sensor s = new TestSensor();
	 * Thread sensorThread = new Thread(s); sensorThread.run();
	 * 
	 * }
	 */
}
