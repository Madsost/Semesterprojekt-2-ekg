package main.control;

import java.sql.SQLException;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import main.model.DatabaseConn;
import main.util.Filter;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class Calculator implements Runnable {
	private ArrayList<Double> calcDataset = new ArrayList<>();
	private DatabaseConn dtb = DatabaseConn.getInstance();

	private int result = -1;
	private double zcross = 0.0;
	private double threshold = 8000;
	private int fs = 250;
	private int pre = -1;
	private int post = -1;
	private int length = -1;
	private boolean running = false;

	/**
	 * 
	 */
	public Calculator() {

	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public int calculatePulse(ArrayList<Integer> input) {

		ArrayList<Integer> inputDataset = input;

		// Folder alt data fra sættet vi tog fra databasen med vores båndpass
		// filter
		for (int data : inputDataset) {
			calcDataset.add(Filter.doFilter(data));
		}

		zcross = 0.0;
		threshold = 8000;
		fs = 250;
		pre = -1;
		post = -1;
		length = calcDataset.size();

		// sætter længden på sættet indne vi beregner en puls
		length = calcDataset.size();

		// Regner pulsen for det filteret signal
		for (int n = 1; n < length; n++) {

			if (calcDataset.get(n - 1) <= threshold)
				pre = 1;
			else
				pre = -1;
			if (calcDataset.get(n) <= threshold)
				post = 1;
			else
				post = -1;
			zcross = zcross + (Math.abs(pre - post) / 2);
			result = (int) Math.round(60 * zcross / ((2 * length) / fs));
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public int calculatePulse() {
		try {
			ArrayList<Integer> inputDataset = dtb.getData(1250);

			// Folder alt data fra sættet vi tog fra databasen med vores
			// båndpass
			// filter
			for (int data : inputDataset) {
				calcDataset.add(Filter.doFilter(data));
			}

			zcross = 0.0;
			threshold = 8000;
			fs = 250;
			pre = -1;
			post = -1;
			length = calcDataset.size();

			// sætter længden på sættet indne vi beregner en puls
			length = calcDataset.size();

			// Regner pulsen for det filteret signal
			for (int n = 1; n < length; n++) {

				if (calcDataset.get(n - 1) <= threshold)
					pre = 1;
				else
					pre = -1;
				if (calcDataset.get(n) <= threshold)
					post = 1;
				else
					post = -1;
				zcross = zcross + (Math.abs(pre - post) / 2);
				result = (int) Math.round(60 * zcross / ((2 * length) / fs));
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void setStage(Stage dialogStage) {

	}

	/**
	 * 
	 */
	@Override
	public void run() {
		running = true;
		while (true) {
			try {
				while (!running) {
					Thread.sleep(200);
				}
				int pulse = calculatePulse();
				if (pulse != -1) {
					dtb.addPulse(pulse);
				}

				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	public void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * 
	 */
	public void resumeThread() {
		running = true;
	}
}