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
	private double threshold = 500;
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
	 * @param toSeries
	 * @return
	 */
	public int calculatePulse(ArrayList<Double> toSeries) {
		ArrayList<Double> calcDataset2 = new ArrayList<>();
		ArrayList<Double> inputDataset = toSeries;

		// Folder alt data fra sættet vi tog fra databasen med vores båndpass
		// filter
		for (double data : inputDataset) {
			calcDataset2.add(Filter.doFilter(data));
		}

		zcross = 0.0;
		pre = -1;
		post = -1;

		// sætter længden på sættet indne vi beregner en puls
		length = calcDataset2.size();

		// Regner pulsen for det filteret signal
		for (int n = 1; n < length; n++) {

			if (calcDataset2.get(n - 1) <= threshold)
				pre = 1;
			else
				pre = -1;
			if (calcDataset2.get(n) <= threshold)
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
			calcDataset = new ArrayList<>();
			ArrayList<Double> inputDataset = dtb.getData(1250);
			if (inputDataset == null || inputDataset.size() == 0) {
				return -1;
			}

			// Folder alt data fra sættet vi tog fra databasen med vores
			// båndpass
			// filter
			for (double data : inputDataset) {
				double temp = Filter.doFilter(data);
				calcDataset.add(temp);
			}

			double max = 0;
			for (double data : calcDataset) {
				max = (data > max) ? data : max;
			}

			threshold = 0.8 * max;
			System.out.println(threshold);

			// sætter længden på sættet inden vi beregner en puls
			length = calcDataset.size();
			System.out.println(length);

			zcross = 0.0;

			for (int n = 1; n < length; n++)

			{
				pre = -1;
				post = -1;
				if (calcDataset.get(n - 1) > threshold)
					pre = 1;
				if (calcDataset.get(n) > threshold)
					post = 1;
				zcross += (Math.abs(pre - post) / 2);
				System.out.println("\tZCROSS: " + zcross);
			}
			result = (int) (60 * zcross / (4 * length / 250));
			System.out.println(result);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	// pre = -1; // post = -1; result = 0;

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

				Thread.sleep(4000);
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