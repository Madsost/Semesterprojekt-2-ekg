package main;

import main.control.Calculator;
import main.control.EKGSensor;
import main.control.GuiController;
import main.control.Sensor;
import main.control.TestSensor;
import javafx.application.Application;

public class MainApp {
	private static boolean running = false;
	private static Calculator cal = null;
	private static Sensor s = null;
	private static boolean testing = true;
	private static Thread sensorThread = null;

	private static void run() {
		sensorThread = new Thread(s);
		sensorThread.setDaemon(true);
		sensorThread.start();
		sensorThread.setName("Sensor tråd");
		cal = new Calculator();

		while (running) {
			// beregn puls

			// vent
		}
	}

	private static void init() {
		// if test
		if (testing)
			s = new TestSensor();

		// if not test
		else
			s = new EKGSensor();
		s.init();
	}

	public static void start() {
		running = true;

		run();
	}

	public static void stop() {
		running = false;
	}

	public static void main(String[] args) {
		// tråd til GUI
		Thread guiThread = new Thread() {
			public void run() {
				Application.launch(GuiController.class);
			}
		};
		guiThread.start();
		init();
		run();
	}

	public static void pauseSensor() {
		try {
			s.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void cont() {
		s.notify();
	}

}

// yolo Dr. Vobs til tjeneste