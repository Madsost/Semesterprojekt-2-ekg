package main.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import main.control.Calculator;
import main.control.GuiController;
import main.model.DatabaseConn;
import main.util.Filter;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class EKGViewController implements ActionListener {

	private DatabaseConn dtb = null;
	private Calculator cal = null;
	private GuiController main = null;

	private static final int MAX_DATA_POINTS = 750;
	private LineChart<Number, Number> lineChart = null;
	private NumberAxis xAxis = null;
	private NumberAxis yAxis = null;
	private int xSeriesData = 0;
	private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<>();
	private XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private ArrayList<Double> temp = null;

	private boolean running = false;
	private boolean appRunning = false;
	private boolean graphShown = true;

	private Thread calculatorThread = null;
	// privSate Adder adder = null;
	// private Thread adderThread = null;

	// private long t2, t1;

	@FXML
	private Label pulseLabel;
	@FXML
	private Label pulseIcon;
	@FXML
	private AnchorPane graphPane;
	@FXML
	private Button startStopButton;
	@FXML
	private CheckBox showGraph;
	@FXML
	private Button showHistory;

	/**
	 * 
	 */
	public EKGViewController() {
		dtb = DatabaseConn.getInstance();
		cal = new Calculator();
		xAxis = new NumberAxis(0, MAX_DATA_POINTS, 50);
		yAxis = new NumberAxis();
		dtb.attachListener(this);

		// adder = new Adder();

	}

	/**
	 * 
	 */
	@FXML
	public void initialize() {
		pulseLabel.setText("--");
		showGraph.setSelected(true);

		// -- opret icon
		Image icon = new Image("file:resources/Images/cardiogram.png");
		pulseIcon.setGraphic(new ImageView(icon));

		// -- opsætning af x-akse
		xAxis.setForceZeroInRange(false);
		xAxis.setAutoRanging(false);
		xAxis.setTickLabelsVisible(false);
		xAxis.setTickMarkVisible(true);
		xAxis.setMinorTickCount(10);
		xAxis.setMinorTickVisible(true);
		xAxis.setLowerBound(0.0);
		xAxis.setUpperBound(MAX_DATA_POINTS);

		// -- oprettelse af grafen
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setVerticalGridLinesVisible(true);

		// -- opsætning af graf
		lineChart.setCreateSymbols(false);
		lineChart.setAnimated(false);
		lineChart.setHorizontalGridLinesVisible(true);
		lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

		series.setName("EKG dataserie");

		// -- opsætning af y-akse
		yAxis.setAutoRanging(true);
		yAxis.setMinorTickCount(100);
		yAxis.setTickMarkVisible(true);
		yAxis.setMinorTickVisible(false);
		yAxis.setTickLabelsVisible(true);

		// -- indsæt grafen i graphPane (et anchor-pane)
		lineChart.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
		graphPane.getChildren().add(lineChart);

		// -- lad grafen følge med graphPane
		graphPane.setRightAnchor(lineChart, 0.0);
		graphPane.setLeftAnchor(lineChart, 0.0);
		graphPane.setBottomAnchor(lineChart, 0.0);
		graphPane.setTopAnchor(lineChart, 0.0);

		// -- tilføj dataserien til grafen
		lineChart.getData().addAll(series);
	}

	/**
	 * Tidslinjen bliver kaldt i hovedtråden. Opdaterer grafen med 60Hz.
	 */
	private void prepareTimeline() {
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				addDataToSeries();
			}
		}.start();
	}

	
	/**
	 * 
	 */
	private void addDataToSeries() {
		int count = (dataQ.size() > 500) ? 3 : 2;
		count = 4;
		for (int i = 0; i < count; i++) {

			if (dataQ.isEmpty()) {
				return;
			}
			series.getData().add(new XYChart.Data<>(xSeriesData++, dataQ.remove()));
		}

		// -- fjerner data for at sikre, at vi ikke når over MAX_DATA_POINTS
		if (series.getData().size() > MAX_DATA_POINTS) {
			series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
		}

		// -- opdater
		xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
		xAxis.setUpperBound(xSeriesData - 1);

	}

	/**
	 * 
	 * @param main
	 */
	public void setGuiController(GuiController main) {
		this.main = main;
	}

	/**
	 * 
	 */
	@FXML
	private void handleStartStop() {
		try {
			if (!running) {
				running = true;
				if (appRunning) {

					dtb.newExamination();
					dtb.setExaminationRunning(true);
					dtb.resumeThread();

					series.getData().clear();
					xAxis.setLowerBound(0.0);
					xAxis.setUpperBound(MAX_DATA_POINTS);

					// fortsæt beregner-tråd
					cal.resumeThread();
					// adder.resumeThread();
				}
				if (!appRunning) {

					appRunning = true;

					// start databasetråden
					dtb.start();
					dtb.setExaminationRunning(true);

					// start en calculator-tråd
					calculatorThread = new Thread(cal);
					calculatorThread.setDaemon(true);
					calculatorThread.start();

					/*
					 * adderThread = new Thread(adder);
					 * adderThread.setDaemon(true); adderThread.start();
					 */

					prepareTimeline();
				}
				startStopButton.setText("Afslut undersøgelse");
			} else {
				running = false;

				startStopButton.setText("Start undersøgelse");
				dtb.setExaminationRunning(false);
				dtb.stopExamination();
				dtb.pauseThread();
				cal.pauseThread();
				// adder.pauseThread();
			}
		} catch (InterruptedException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.showAndWait();
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.showAndWait();
		}
	}

	/**
	 * 
	 */
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

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			String eventCommand = event.getActionCommand();
			switch (eventCommand) {
			case "Pulse":
				handleNewPulse();
				break;
			case "EKG":
				temp = dtb.getDataToGraph();
				for (double i : temp) {
					i = Filter.doSmooth(i);
					dataQ.add(i);
				}

				break;
			default:
				break;
			}
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.showAndWait();
		}

	}

	/**
	 * 
	 */
	@FXML
	public void handleShowHistory() {
		main.showHistoryView();
	}

	/**
	 * 
	 * @param newPulse
	 */
	public void updatePulse(int newPulse) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				pulseLabel.setText("" + newPulse);
			}
		});

	}

	/**
	 * 
	 */
	private void handleNewPulse() {
		try {
			updatePulse(dtb.getPulse());
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.showAndWait();
		}
	}

}
