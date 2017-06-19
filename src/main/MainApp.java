package main;

import main.control.EKGSensor;
import main.control.GuiController;
import main.control.Sensor;
import main.control.TestSensor;
import main.model.DatabaseConn;

import java.sql.SQLException;
import java.util.ConcurrentModificationException;

import javafx.application.Application;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class MainApp {
	private static boolean running = false;
	private static Sensor s = null;
	private static boolean testing = false;
	private static Thread sensorThread = null;
	private static DatabaseConn dtb = DatabaseConn.getInstance();

	/**
	 * 
	 */
	private static void run() {
		try {
			while (running) {
				boolean examRunning = dtb.isExamRunning();
				boolean appRunning = dtb.isAppRunning();
				if (!examRunning && sensorThread.isAlive()) {
					try {
						s.pauseThread();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (examRunning && !sensorThread.isAlive()) {
					Thread.sleep(1000);
					sensorThread.start();
				} else if (!examRunning && sensorThread.isAlive()) {
					s.resumeThread();
				}
				if (!appRunning) {
					s.stopConn();
					dtb.stopConn();
					System.exit(0);
				}

				Thread.sleep(500);

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static void init() {
		try {
			dtb.setAppRunning(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// tråd til GUI
		Thread guiThread = new Thread() {
			public void run() {
				Application.launch(GuiController.class);
			}
		};
		try{
		guiThread.start();}catch(ConcurrentModificationException e){System.out.println("hej fra main");}
		init();
		run();
	}
}