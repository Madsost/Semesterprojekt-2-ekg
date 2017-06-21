package main.control;

import java.sql.SQLException;
import java.util.ArrayList;

import main.model.DatabaseConn;
import main.util.Filter;

/**
 * Pulsberegner-klasse.
 * 
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
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
	 * Beregner pulsen på baggrund af datasættet givet som parameter. Kaldes af EKGHistoryViewController.
	 * 
	 * @param toSeries
	 *            datasæt, der skal beregnes puls på.
	 * @return den beregnede puls
	 */
	public int calculatePulse(ArrayList<Double> toSeries) {
		ArrayList<Double> calcDataset2 = new ArrayList<>();
		ArrayList<Double> inputDataset = toSeries;

		if (inputDataset == null || inputDataset.size() == 0) { /* 1A */
			return -1;
		}

		// Folder alt data fra sættet vi tog fra databasen med vores båndpass
		// filter
		for (double data : inputDataset) { /* 1B */
			calcDataset2.add(Filter.doFilter(data));
		}

		double max = 0;
		for (double data : calcDataset2) { /* 1C */
			max = (data > max) ? data : max;
		}

		double threshold2 = 0.8 * max;

		double zcross2 = 0.0;
		int pre2 = -1;
		int post2 = -1;

		int result2 = 0;

		// sætter længden på sættet indne vi beregner en puls
		int length2 = calcDataset2.size();

		// Regner pulsen for det filteret signal
		for (int n = 1; n < length2; n++) { /* 1D */
			pre2 = -1;
			post2 = -1;
			if (calcDataset2.get(n - 1) > threshold2) /* 1E */
				pre2 = 1;
			if (calcDataset2.get(n) > threshold2) /* 1F */
				post2 = 1;
			zcross2 = zcross2 + (Math.abs(pre2 - post2) / 2);
		}
		result2 = (int) (60 * zcross2 / (2 * length2 / fs));
		return result2;
	}

	/**
	 * Beregner pulsen på baggrund af data fra databasen. Kaldes i EKGViewController.
	 * 
	 * @return den beregnede puls
	 */
	public int calculatePulse() {
		try {
			calcDataset = new ArrayList<>();
			ArrayList<Double> inputDataset = dtb.getData(1250);
			if (inputDataset == null || inputDataset.size() == 0) { /* 2A */
				return -1;
			}

			// Folder alt data fra sættet vi tog fra databasen med vores
			// båndpass
			// filter
			for (double data : inputDataset) { /* 2B */
				double temp = Filter.doFilter(data);
				calcDataset.add(temp);
			}

			double max = 0;
			for (double data : calcDataset) { /* 2C */
				max = (data > max) ? data : max;
			}
			threshold = 0.8 * max;

			// sætter længden på sættet inden vi beregner en puls
			length = calcDataset.size();

			zcross = 0.0;

			for (int n = 1; n < length; n++) { /* 2D */
				pre = -1;
				post = -1;
				if (calcDataset.get(n - 1) > threshold) /* 2E */
					pre = 1;
				if (calcDataset.get(n) > threshold) /* 2F */
					post = 1;
				zcross += (Math.abs(pre - post) / 2);
			}
			result = (int) (60 * zcross / (2 * length / fs));
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Kaldes af <code>Thread.start()</code>
	 */
	@Override
	public void run() {
		running = true;
		while (true) {
			try {
				while (!running) { /* 3A */
					Thread.sleep(200);
				}
				int pulse = calculatePulse();
				if (pulse != -1) { /* 3B */
					dtb.addPulse(pulse);
				}

				Thread.sleep(4000);
			} catch (InterruptedException e) { /* 3C */
				e.printStackTrace();
			} catch (SQLException e) { /* 3D */
				e.printStackTrace();
			}
		}
	}

	/**
	 * Pauser tråden
	 * 
	 * @throws InterruptedException
	 */
	public void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * Fortsætter tråden
	 */
	public void resumeThread() {
		running = true;
	}
}