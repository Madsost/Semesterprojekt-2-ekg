package main.control;

import java.util.ArrayList;

import main.model.DatabaseConn;
import main.util.Filter;

public class Calculator {
	private ArrayList<Double> calcDataset = new ArrayList<>();
	private DatabaseConn dtb = DatabaseConn.getInstance();

	public Calculator() {
		dtb.run();
		dtb.setDaemon(true);
		dtb.setName("Database tråd");
	}

	public boolean validateData() {
		return false;
	}

	public int calculatePulse() {
		ArrayList<Integer> inputDataset = null; // DatabaseConn.getData(1000);
		int result = 0;
		
		for (int data : inputDataset) {
			calcDataset.add(Filter.doFilter(data));
		}

		// z-cross algoritme (Li tan side 369)
		// MATLAB kode:
		
		double zcross = 0.0;
		double threshold = 8000;
		int fs = 250;
		int pre = -1;
		int post = -1;
		int length = calcDataset.size();
		
		
		for(int n = 1; n <= length; n++){
			
			if(calcDataset.get(n-1) <= threshold) pre = 1; else pre = -1;
			if(calcDataset.get(n) <= threshold) post = 1; else post = -1;
			zcross = zcross + Math.abs(pre - post)/2;
			result = (int) Math.round(60 * zcross/((2*length)/fs));
		}
		return result;
	}
}
