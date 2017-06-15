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
	private final String variant = "100";
	private int count = 0;
	private boolean running = false;
	private int toOutputCount = 0;
	private int[] outputBuffer = new int[250];

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

		while (true) {
			while (!running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (count >= dataset.size())
				count = 0;
			outputBuffer[toOutputCount++] = dataset.get(count);
			if (toOutputCount == 250) {
				q.addToBuffer(outputBuffer);
				outputBuffer = new int[250];
				toOutputCount = 0;
			}
			count++;
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void pauseThread() throws InterruptedException {
		running = false;

	}

	@Override
	public void resumeThread() {
		running = true;
	}

	@Override
	public void stopConn() {
		running = false;

	}
}
