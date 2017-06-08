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

		// z-cross algoritme (Li tan side 369)
		// MATLAB kode:
		/*
		 * zcross = 0.0; threshold=0.8; for n= 2:length(y2) pre_sign = -1;
		 * cur_sign = -1; if 
		 * y2(n-1)>threshold pre_sign = 1; 
		 * end 
		 * if
		 * y2(n)>threshold cur_sign = 1; 
		 * end 
		 * zcross = zcross + abs(cur_sign-pre_sign)/2; 
		 * end 
		 * 
		 * rate = 60/(length(y2)/300)*(zcross/2)
		 */

		return result;
	}
}
