package main.control;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.MainApp;
import main.model.DatabaseConn;
import main.view.EKGHistoryViewController;
import main.view.EKGViewController;
import main.view.RootLayoutController;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class GuiController extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private DatabaseConn dtb = DatabaseConn.getInstance();

	/**
	 * Konstruktør. Kaldes automatisk når processen starter
	 */
	public GuiController() {
	}

	/**
	 * 
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;

			this.primaryStage.setTitle("EKG Målesystem");

			this.primaryStage.getIcons().add(new Image("file:resources/images/cardiogram2.png"));

			initRootLayout();

			showEKGView();

		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.initOwner(primaryStage);
			alert.showAndWait();

			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * 
	 */
	public void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.FXML"));

			rootLayout = (BorderPane) loader.load();
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			RootLayoutController controller = loader.getController();
			controller.setGuiController(this);

			primaryStage.show();

		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.initOwner(primaryStage);
			alert.showAndWait();

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void showEKGView() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/EKGView.FXML"));

			AnchorPane ekgOverview = (AnchorPane) loader.load();

			rootLayout.setCenter(ekgOverview);

			EKGViewController controller = loader.getController();
			controller.setGuiController(this);

		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.initOwner(primaryStage);
			alert.showAndWait();

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void showHistoryView() {
		try {
			// indhent loaderen til FXML filen og opret et nyt stage til pop up
			// vinduet.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ShowEKGHistoryView.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// Opret et stage til oversigten
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Hent tidligere målinger");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			
			dialogStage.getIcons().add(new Image("file:resources/images/cardiogram2.png"));

			// Giv controlleren kontrol over vinduet.
			EKGHistoryViewController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setGuiController(this);

			dialogStage.show();
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.initOwner(primaryStage);
			alert.showAndWait();

			e.printStackTrace();
		}
	}

	/**
	 * Kaldes som det sidste når vinduet lukkes.
	 */
	@Override
	public void stop() {
		try {
			dtb.setAppRunning(false);
			dtb.setExaminationRunning(false);
			dtb.stopExamination();
			System.out.println("Vinduet lukkes...");
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fejl i " + this.getClass().getSimpleName());
			alert.setHeaderText("Der skete en fejl! Se detaljerne nedenfor.");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());

			alert.showAndWait();
		}
	}
}
