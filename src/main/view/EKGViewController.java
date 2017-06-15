package main.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import main.control.Calculator;
import main.control.GuiController;
import main.model.DatabaseConn;

public class EKGViewController implements ActionListener {

	private DatabaseConn dtb = null;
	private Calculator cal = null;
	private GuiController main = null;

	private static final int MAX_DATA_POINTS = 750;
	private LineChart<Number, Number> lineChart = null;
	private NumberAxis xAxis = null;
	private NumberAxis yAxis = null;
	private int xSeriesData = 0;
	private int latestPulse = 0;
	private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<>();
	private XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private ArrayList<Integer> toDataQ = new ArrayList<>();

	private boolean running = false;
	private boolean appRunning = false;
	private boolean graphShown = true;
	private int ekgCounter = 0;

	private Thread adderThread = null;
	private Thread calculatorThread = null;
	private Adder adder = null;

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

	public EKGViewController() {
		dtb = DatabaseConn.getInstance();
		cal = new Calculator();
		xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
		yAxis = new NumberAxis();
		dtb.attachListener(this);
	}

	@FXML
	public void initialize() {
		pulseLabel.setText("--");
		showGraph.setSelected(true);

		Image icon = new Image("file:resources/Images/cardiogram.png");
		pulseIcon.setGraphic(new ImageView(icon));

		xAxis.setForceZeroInRange(false);
		xAxis.setAutoRanging(false);
		xAxis.setTickLabelsVisible(false);
		xAxis.setTickMarkVisible(false);
		xAxis.setMinorTickVisible(false);

		lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
			// overskrives, for at fjerne symbol på hver måling
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};

		lineChart.setAnimated(false);
		lineChart.setHorizontalGridLinesVisible(true);
		lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);

		series.setName("EKG dataserie");

		lineChart.getData().addAll(series);

		lineChart.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
		graphPane.getChildren().add(lineChart);
		graphPane.setRightAnchor(lineChart, 0.0);
		graphPane.setLeftAnchor(lineChart, 0.0);

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

	private void addDataToSeries() {
		for (int i = 0; i < 10; i++) {
			if (dataQ.isEmpty()) {
				break;
			}
			series.getData().add(new XYChart.Data<>(xSeriesData++, dataQ.remove()));
		}

		// fjerner data for at sikre, at vi ikke når over MAX_DATA_POINTS
		if (series.getData().size() > MAX_DATA_POINTS) {
			series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
		}
		// opdater
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
			if (appRunning) {
				adder.resumeThread();
				dtb.resumeThread();
				dtb.setExaminationRunning(true);
				cal.resumeThread();
			}
			if (!appRunning) {
				adder = new Adder();
				adderThread = new Thread(adder);
				adderThread.setDaemon(true);
				adderThread.start();

				prepareTimeline();

				appRunning = true;

				// start databasetråden
				dtb.start();
				dtb.setExaminationRunning(true);

				// start en calculator-tråd
				calculatorThread = new Thread(cal);
				calculatorThread.setDaemon(true);
				calculatorThread.start();
			}
			startStopButton.setText("Afslut undersøgelse");
		} else {
			running = false;
			try {
				adder.pauseThread();
				startStopButton.setText("Start undersøgelse");
				dtb.setExaminationRunning(false);
				dtb.stopExamination();
				dtb.pauseThread();
				cal.pauseThread();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
			// System.out.println("Puls - testudskrift" +
			// this.getClass().getName());
			break;
		case "EKG":
			// System.out.println("EKG - testudskrift: " +
			// this.getClass().getName());
			toDataQ.addAll(dtb.getDataToGraph());
			break;
		default:
			break;
		}
	}
	
	@FXML
	public void handleShowHistory(){
		main.showHistoryView();
	}

	public void updatePulse(int newPulse) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				pulseLabel.setText("" + newPulse);
			}
		});

	}

	private void handleNewPulse() {
		latestPulse = dtb.getPulse();
		// System.out.println("test" + latestPulse);
		updatePulse(latestPulse);
	}

	/**
	 * Hjælpeklasse, der sikrer at data bliver sendt til grafen med den rigtige
	 * hastighed.
	 * 
	 * @author Mads Østergaard
	 *
	 */
	private class Adder implements Runnable {
		private boolean running = false;

		@Override
		public void run() {
			running = true;
			try {
				while (true) {
					while (!running) {
						Thread.sleep(200);
					}
					if (toDataQ.size() > 0 && ekgCounter < toDataQ.size()) {
						dataQ.add(toDataQ.get(ekgCounter++));
					}
					Thread.sleep(4);
				}

			} catch (InterruptedException ex) {
				System.out.println("afbrudt");
				return;
			}

		}

		public void pauseThread() throws InterruptedException {
			running = false;
		}

		public void resumeThread() {
			running = true;
		}

	}

}
