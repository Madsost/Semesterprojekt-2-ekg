package main.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import main.control.GuiController;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class RootLayoutController {
	private GuiController main = null;

	/**
	 * 
	 * @param main
	 */
	public void setGuiController(GuiController main) {
		this.main = main;
	}

	/**
	 * Åbner en "om"-dialog
	 */
	@FXML
	private void handleAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("EKG - applikation");
		alert.setHeaderText("Om");
		alert.setContentText(
				"Udviklere: Mads Østergaard, Emma Lundgaard Christensen, Morten Guldhammer Vorborg" + "\nGruppe 1.");

		alert.showAndWait();
	}

	/**
	 * Afslutter programmet ved at kalde <code>stop()</code> i
	 * <code>GuiController</code>.
	 */
	@FXML
	private void handleExit() {
		main.stop();
	}

	/**
	 * Åbner historikken.
	 */
	@FXML
	private void handleHistory() {
		main.showHistoryView();
	}

}
