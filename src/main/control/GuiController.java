package main.control;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import main.MainApp;
import main.model.DatabaseConn;
import main.view.EKGHistoryViewController;
import main.view.EKGViewController;
import main.view.RootLayoutController;

public class GuiController extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private DatabaseConn dtb = DatabaseConn.getInstance();

	public GuiController() {
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;

			this.primaryStage.setTitle("GUI");
			
			this.primaryStage.getIcons().add(new Image("file:resources/images/cardiogram2.png"));

			initRootLayout();

			showEKGView();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

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

			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					dtb.setAppRunning(false);
					dtb.setExaminationRunning(false);
					dtb.stopExamination();
					System.out.println("Vinduet lukkes...");
				}
			});

		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Der er sket en fejl: " + e.getMessage());
			alert.setTitle("Fejl!");
			alert.setHeaderText("Fejl i 'initialisering'");

			alert.initOwner(primaryStage);
			alert.showAndWait();
		}
	}

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
			alert.setContentText("Der er sket en fejl: " + e.getMessage());
			alert.setTitle("Fejl!");
			alert.setHeaderText("Fejl i 'vis oversigt'");

			alert.initOwner(primaryStage);
			alert.showAndWait();
			e.printStackTrace();
		}
	}

	public void showHistoryView() {
		try {
			// load the FMXL file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ShowEKGHistoryView.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			// create the dialog stage
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Hent tidligere målinger");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);

			// set the job into the controller
			EKGHistoryViewController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setGuiController(this);

			dialogStage.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Der er sket en fejl: " + e.getLocalizedMessage());
			alert.setTitle("Fejl!");
			alert.setHeaderText("Fejl i 'vis historik'");

			alert.initOwner(primaryStage);
			alert.showAndWait();
		}
	}
}
