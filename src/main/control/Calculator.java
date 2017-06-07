package main.control;

import java.util.ArrayList;

import main.model.Measurement;
import main.util.Filter;

public class Calculator {
	private ArrayList<Double> calcDataset = new ArrayList<>();
	private Measurement activeMeasurement;

	public void setActiveMeasurement(Measurement m) {
		this.activeMeasurement = m;
	}

	public boolean validateData() {
		return false;
	}

	public int calculatePulse() {
		ArrayList<Double> inputDataset = activeMeasurement.getDataset();
		int result = 0;
		for (double data : inputDataset) {
			calcDataset.add(Filter.doFilter(data));
		}

		// z-cross algoritme (Li tan side ??)

		return result;
	}
}
