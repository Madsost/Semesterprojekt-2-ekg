package main.control;

import java.awt.event.ActionListener;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.MainApp;
import main.view.EKGViewController;
import main.view.RootLayoutController;

public class GuiController extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;

	public GuiController() {
		// skal der ske noget her?
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;

			this.primaryStage.setTitle("GUI");

			initRootLayout();

			showEKGView();

			// tilføj en eventhandler så alt bliver afsluttet når vinduet
			// lukker.
			/*
			 * this.primaryStage.addEventHandler(new WindowEvent, new
			 * EventHandler<WindowEvent>(){
			 * 
			 * @Override public void handle(WindowEvent arg0) { // TODO
			 * Auto-generated method stub
			 * 
			 * }
			 * 
			 * });
			 */

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
