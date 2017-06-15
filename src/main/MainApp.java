package main;

import main.control.EKGSensor;
import main.control.GuiController;
import main.control.Sensor;
import main.control.TestSensor;
import main.model.DatabaseConn;
import javafx.application.Application;

public class MainApp {
	private static boolean running = false;
	private static Sensor s = null;
	private static boolean testing = true;
	private static Thread sensorThread = null;
	private static DatabaseConn dtb = DatabaseConn.getInstance();

	private static void run() {
		while (running) {
			boolean examRunning = dtb.isExamRunning();
			boolean appRunning = dtb.isAppRunning();
			if (!examRunning && sensorThread.isAlive()) {
				try {
					s.pauseThread();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (examRunning && !sensorThread.isAlive()) {
				sensorThread.start();
			} else if (!examRunning && sensorThread.isAlive()) {
				s.resumeThread();
			}
			if (!appRunning) {
				s.stopConn();
				System.exit(0);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void init() {
		dtb.setAppRunning(true);
		if (testing)
			s = new TestSensor();
		// if not test
		else
			s = new EKGSensor();
		running = true;
		s.init();
		sensorThread = new Thread(s);
		sensorThread.setDaemon(true);
		sensorThread.setName("Sensor tråd");
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
}

// yolo Dr. Vobs til tjeneste