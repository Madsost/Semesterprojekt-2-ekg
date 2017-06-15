package main.control;

import java.util.ArrayList;

import main.model.DatabaseConn;
import main.util.Filter;

public class Calculator {
	private ArrayList<Double> calcDataset = new ArrayList<>();
	private DatabaseConn dtb = DatabaseConn.getInstance();
	
	private int result = -1;
	private double zcross = 0.0;
	private double threshold = 8000;
	private int fs = 250;
	private int pre = -1;
	private int post = -1;
	private int length = -1;

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
		
		//Folder alt data fra sættet vi tog fra databasen med vores båndpass filter
		for (int data : inputDataset) {
			calcDataset.add(Filter.doFilter(data));
		}
		
		
		// sætter længden på sættet indne vi beregner en puls
		length = calcDataset.size();
		
		// Regner pulsen for det filteret signal
		for(int n = 1; n <= length; n++){
			
			if(calcDataset.get(n-1) <= threshold) pre = 1; else pre = -1;
			if(calcDataset.get(n) <= threshold) post = 1; else post = -1;
			zcross = zcross + Math.abs(pre - post)/2;
			result = (int) Math.round(60 * zcross/((2*length)/fs));
		}
		return result;
	}
}
