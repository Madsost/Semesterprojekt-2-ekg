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
 * <h1>Hovedprogram</h1> Starter programmet, kalder
 * <code>Application.launch</code>, der starter javaFX applicationen,
 * <code>init()</code> og <code>run()</code>.
 * 
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
 */
public class MainApp {
	private static boolean running = false;
	private static Sensor s = null;
	private static boolean testing = true;
	private static Thread sensorThread = null;
	private static DatabaseConn dtb = DatabaseConn.getInstance();

	/**
	 * Undersøger løbende om programmet skal fortsætte og om der er en
	 * undersøgelse igang --> styrer sensor forbindelsen herefter.
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
				} else if (examRunning && sensorThread.isAlive()) {
					s.resumeThread();
				}
				if (!appRunning) {
					stopApp();
				}

				Thread.sleep(500);

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sikrer god afslutning på program.
	 */
	public static void stopApp() {
		if (s != null) {
			s.stopConn();
		}
		try {
			dtb.setAppRunning(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dtb.stopConn();
		System.exit(0);
	}

	/**
	 * Opsætter sensoren og tildeler den en tråd. Sætter
	 * <code>setAppRunning</code> i databasen til sand.
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
	 * Indgang for programmet.
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
		guiThread.start();
		init();
		run();
	}
}