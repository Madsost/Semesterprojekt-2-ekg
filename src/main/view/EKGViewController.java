package main.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
	private int latestPulse = 0;
	private GuiController main = null;
	private GraphController graphController = null;
	private boolean running = false;
	private boolean appRunning = false;
	private boolean graphShown = true;

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
		dtb.attachListener(this);
	}

	@FXML
	public void initialize() {

		pulseLabel.setText("--");

		showGraph.setSelected(true);

		// init grafen og sæt den i anchorpane
		initGraph();
	}

	private void initGraph() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/GraphView.FXML"));

			graphPane.getChildren().add((AnchorPane) loader.load());

			graphController = loader.getController();

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

	}

	public void setGuiController(GuiController main) {
		this.main = main;
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
			// graphController.start();
			startStopButton.setText("Afslut undersøgelse");
		} else {
			running = false;
			// dtb.stopExamination();
			// graphController.stop();
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
	public void actionPerformed(ActionEvent arg0) {
		String eventCommand = arg0.getActionCommand();
		if (eventCommand.equals("Pulse"))
			handleNewPulse();
	}

	private void handleNewPulse() {
		latestPulse = dtb.getPulse();
		pulseLabel.setText("" + latestPulse);
	}

}
