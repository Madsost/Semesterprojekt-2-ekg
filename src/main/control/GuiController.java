package main.control;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import main.MainApp;
import main.model.DatabaseConn;
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

			initRootLayout();

			showEKGView();

			// Til fejlvisning...
			/*
			 * Alert alert = new Alert(AlertType.ERROR);
			 * alert.setContentText("Hej - der er sket en fejl (ikke");
			 * alert.setTitle("Titel"); alert.setHeaderText("Overskrift tekst");
			 * 
			 * alert.initOwner(primaryStage); alert.showAndWait();
			 */

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
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
}
