package main.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import main.MainApp;
import main.control.GuiController;
import main.model.DatabaseConn;

public class EKGViewController implements ActionListener {

	private DatabaseConn dtb = null;
	private static final int MAX_DATA_POINTS = 900;
	private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<>();
	private XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private LineChart lineChart = null;
	private NumberAxis xAxis = null;
	private NumberAxis yAxis = null;
	private int xSeriesData = 0;
	private int latestPulse = 0;
	private GuiController main = null;
	private boolean running = false;
	private boolean appRunning = false;
	private boolean graphShown = true;
	ArrayList<Integer> toDataQ = new ArrayList<>();
	private int ekgCounter = 0;
	private Thread adderThread = null;

	@FXML
	private Label pulseLabel;
	@FXML
	private AnchorPane graphPane;
	@FXML
	private Button startStopButton;
	@FXML
	private CheckBox showGraph;

	public EKGViewController() {
		dtb = DatabaseConn.getInstance();
		xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
		yAxis = new NumberAxis();
	}

	@FXML
	public void initialize() {
		pulseLabel.setText("--");
		showGraph.setSelected(true);

		xAxis.setForceZeroInRange(false);
		xAxis.setAutoRanging(false);
		xAxis.setTickLabelsVisible(false);
		xAxis.setTickMarkVisible(false);
		xAxis.setMinorTickVisible(false);

		lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
			// Override to remove symbols on each data point
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};

		lineChart.setAnimated(false);
		lineChart.setHorizontalGridLinesVisible(true);
		lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

		series.setName("EKG dataserie");

		lineChart.getData().addAll(series);

		/*
		 * dtb.attachListener(this);
		 * 
		 * Adder adder = new Adder(); Thread t1 = new Thread(adder);
		 * t1.setDaemon(true); t1.start();
		 * 
		 * prepareTimeline();
		 */

		lineChart.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
		graphPane.getChildren().add(lineChart);
		graphPane.setRightAnchor(lineChart, 0.0);
		graphPane.setLeftAnchor(lineChart, 0.0);
	}

	private class Adder implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					if (toDataQ.size() > 0 && ekgCounter < toDataQ.size()) {
						dataQ.add(toDataQ.get(ekgCounter++));
					}
					Thread.sleep(4);
				}
			} catch (InterruptedException ex) {
				return;
			}

		}

	}

	// -- Timeline gets called in the JavaFX Main thread
	private void prepareTimeline() {
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				// System.out.println("animation timer");
				addDataToSeries();
			}
		}.start();
	}

	private void addDataToSeries() {
		for (int i = 0; i < 20; i++) { // -- add numbers to the plot+
			if (dataQ.isEmpty()) {
				break;
			}
			// System.out.println("Dataq ikke tom");
			series.getData().add(new XYChart.Data<>(xSeriesData++, dataQ.remove()));
		}

		// remove points to keep us at no more than MAX_DATA_POINTS
		if (series.getData().size() > MAX_DATA_POINTS) {
			series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
		}
		// update
		xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
		xAxis.setUpperBound(xSeriesData - 1);
	}

	public void setGuiController(GuiController main) {
		this.main = main;
	}

	@FXML
	private void handleStartStop() {
		if (!running) {
			running = true;
			dtb.attachListener(this);
			if (appRunning) {
				// main.cont();
				adderThread.notify();
				dtb.newExamination();
				System.out.println("appRunning sand");
			}
			if (!appRunning) {
				Adder adder = new Adder();
				adderThread = new Thread(adder);
				adderThread.setDaemon(true);
				adderThread.start();

				prepareTimeline();

				// main.begin();
				appRunning = true;
				System.out.println("appRunning var falsk");
			}
			startStopButton.setText("Afslut undersøgelse");
		} else {
			running = false;
			dtb.stopExamination();
			dtb.detachListener(this);
			try {
				adderThread.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			startStopButton.setText("Start undersøgelse");
			// main.pause();
		}
	}

	@FXML
	private void handleCheckBox() {
		if (graphShown) {
			graphShown = false;
			graphPane.setVisible(graphShown);
		} else {
			graphShown = true;
			graphPane.setVisible(graphShown);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String eventCommand = event.getActionCommand();
		switch (eventCommand) {
		case "Pulse":
			handleNewPulse();
			System.out.println("Puls - testudskrift" + this.getClass().getName());
			break;
		case "EKG":
			System.out.println("EKG - testudskrift: " + this.getClass().getName());
			toDataQ.addAll(dtb.getDataToGraph());
			break;
		default:
			break;
		}
	}

	private void handleNewPulse() {
		latestPulse = dtb.getPulse();
		pulseLabel.setText("" + latestPulse);
	}

}
