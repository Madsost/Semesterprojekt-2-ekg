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
	private int xSeriesData = 0;
	private XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private int latestPulse = 0;
	private GuiController main = null;
	private boolean running = false;
	private boolean appRunning = false;
	private boolean graphShown = true;

	@FXML
	private Label pulseLabel;
	@FXML
	private AnchorPane graphPane;
	@FXML
	private final NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
	@FXML
	private final NumberAxis yAxis = new NumberAxis();
	@FXML
	private LineChart<Number, Number> graph;
	@FXML
	private Button startStopButton;
	@FXML
	private CheckBox showGraph;

	public EKGViewController() {
		dtb = DatabaseConn.getInstance();
		dtb.attachListener(this);
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

		graph = new LineChart<Number, Number>(xAxis, yAxis) {
			// Override to remove symbols on each data point
			@Override
			protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
			}
		};

		graph.setAnimated(false);
		graph.setHorizontalGridLinesVisible(true);

		series.setName("EKG dataserie");

		graph.getData().addAll(series);
	}

	// -- Timeline gets called in the JavaFX Main thread
	private void prepareTimeline() {
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				addDataToSeries();
			}
		}.start();
	}

	private void addDataToSeries() {
		for (int i = 0; i < 20; i++) { // -- add 20 numbers to the plot+
			if (dataQ.isEmpty())
				break;
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

	private void handleStart() {
		dtb.attachListener(this);
		prepareTimeline();
	}

	private void handleStop() {
		dtb.detachListener(this);

	}

	@FXML
	private void handleStartStop() {
		if (!running) {
			running = true;
			if (appRunning) {
				// main.cont();
				// dtb.newExamination();
				System.out.println("appRunning sand");
			}
			if (!appRunning) {
				// main.begin();
				appRunning = true;
				System.out.println("appRunning falsk");
			}
			handleStart();
			startStopButton.setText("Afslut undersøgelse");
		} else {
			running = false;
			// dtb.stopExamination();
			// graphController.stop();
			handleStop();
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
			break;
		case "EKG":
			addToDataQ();
			break;
		default:
			break;
		}

	}

	private void addToDataQ() {
		ArrayList<Integer> toDataQ = dtb.getDataToGraph();
		for (int k : toDataQ) {
			dataQ.add(k);
		}
	}

	private void handleNewPulse() {
		latestPulse = dtb.getPulse();
		pulseLabel.setText("" + latestPulse);
	}

}
