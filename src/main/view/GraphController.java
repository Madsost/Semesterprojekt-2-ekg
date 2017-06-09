package main.view;

import java.util.Timer;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.util.*;
import main.model.DatabaseConn;

public class GraphController {
	private DatabaseConn dtb = DatabaseConn.getInstance();
	private Timer timer = null;
	private ArrayList<Integer> dataToGraph = null;

	@FXML
	private final NumberAxis xAxis = new NumberAxis();
	@FXML
	private final NumberAxis yAxis = new NumberAxis();
	@FXML
	private LineChart<Number, Number> graph;

	public GraphController() {
		dataToGraph = new ArrayList<>();
	}

	public void start() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				dataToGraph = dtb.getData(900);
			}

		}, 2000, 200);
	}

	public void stop() {
		timer.cancel();
	}

}
