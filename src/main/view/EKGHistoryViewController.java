package main.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.control.Calculator;
import main.control.GuiController;
import main.model.DatabaseConn;
import main.util.Filter;

/**
 * Håndterer historik-visning.
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
 *
 */
public class EKGHistoryViewController {
	private Stage dialogStage = null;
	private DatabaseConn dtb = DatabaseConn.getInstance();

	@FXML
	private Button getData;
	@FXML
	private Button forwardsButton;
	@FXML
	private Button backwardsButton;
	@FXML
	private Button exit;
	@FXML
	private Button plus;
	@FXML
	private Button minus;
	@FXML
	private Label pulseLabel;
	@FXML
	private Label pulseIcon;
	@FXML
	private LineChart<Number, Number> graph;
	@FXML
	private NumberAxis xAxis;
	@FXML
	private NumberAxis yAxis;
	@FXML
	private TextField inputField;

	private final int MAX_DATA_POINTS = 1000;
	private XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private int counter = 0;
	private int zoomCounter = 0;
	private Calculator cal = new Calculator();
	private ArrayList<Double> toSeries = null;

	/**
	 * Kaldes automatisk af loaderen.
	 */
	@FXML
	public void initialize() {
		try {
			inputField.setText(dtb.getStartTime());
			Image icon = new Image("file:resources/Images/cardiogram.png");
			pulseIcon.setGraphic(new ImageView(icon));

			graph.getXAxis().setAutoRanging(false);

			xAxis.setUpperBound(MAX_DATA_POINTS);
			xAxis.setLowerBound(0.0);

			int seconds = MAX_DATA_POINTS / 250;
			series.setName(seconds + " sekunders målinger");

			graph.getData().addAll(series);
			graph.setAnimated(false);
			graph.setCreateSymbols(false);
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
	private void handleUpdate() {
		try {
			if (isValidInput()) {
				toSeries = new ArrayList<>();
				ArrayList<Double> temp = dtb.getDataToHistory(inputField.getText());

				// udjævn serie
				for (double i : temp) {
					toSeries.add(Filter.doSmooth(i));
				}

				series.getData().clear();
				for (int i = 0; i < toSeries.size(); i++) {
					series.getData().add(new XYChart.Data<>(i, toSeries.get(i)));
				}
				getPulse();
			}
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			// e.printStackTrace();

			alert.showAndWait();
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean isValidInput() {
		// giver 24-timers mønster i formen tt:mm:ss
		String timeMatcher = "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";
		Pattern pattern = Pattern.compile(timeMatcher);
		Matcher matcher = pattern.matcher(inputField.getCharacters());

		String errorMessage = "";
		if (inputField.getText().length() == 0 || inputField.getText() == null) {
			errorMessage += "Feltet er tomt!\n";
		} else if (!matcher.matches()) {
			errorMessage += "Forkert format! Brug tt:mm:ss\n";
		}
		if (errorMessage.length() == 0) {
			return true;
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(dialogStage);
			alert.setTitle("Fejl i input");
			alert.setHeaderText("Ret venligst fejlene i felterne.");
			alert.setContentText(errorMessage);

			alert.showAndWait();
			return false;
		}
	}

	/**
	 * Håndterer luk-knap
	 */
	@FXML
	private void handleExit() {
		dialogStage.close();
	}

	/**
	 * Håndterer > knap
	 */
	@FXML
	private void handleGraphChangeRight() {
		counter = 100;
		xAxis.setLowerBound(xAxis.getLowerBound() + counter);
		xAxis.setUpperBound(xAxis.getUpperBound() + counter);
	}

	/**
	 * Håndterer < knap
	 */
	@FXML
	private void handleGraphChangeLeft() {
		counter = 100;
		xAxis.setLowerBound(xAxis.getLowerBound() - counter);
		xAxis.setUpperBound(xAxis.getUpperBound() - counter);
	}

	/**
	 * Giver klassen adgang til dens dialogStage.
	 * 
	 * @param dialogStage
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	/**
	 * Håndterer + knap
	 */
	@FXML
	private void handlePlus() {
		zoomCounter = 100;
		xAxis.setLowerBound(xAxis.getLowerBound() + zoomCounter);
		xAxis.setUpperBound(xAxis.getUpperBound() - zoomCounter);
	}

	/**
	 * Håndtere - knap
	 */
	@FXML
	private void handleMinus() {
		zoomCounter = -100;
		xAxis.setLowerBound(xAxis.getLowerBound() + zoomCounter);
		xAxis.setUpperBound(xAxis.getUpperBound() - zoomCounter);
	}

	/**
	 * Beregner pulsen på baggrund af det aktuelle datasæt og opdaterer
	 * puls-label med det.
	 */
	private void getPulse() {
		pulseLabel.setText("" + cal.calculatePulse(toSeries));
	}

}
