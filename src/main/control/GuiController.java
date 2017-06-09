package main.control;

import java.awt.event.ActionListener;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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

			this.primaryStage.setTitle("GUI-Mockup");

			initRootLayout();

			showEKGView();

		} catch (Exception e) {
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
		}
	}

	public void pause() {
		MainApp.pauseSensor();
	}
	
	public void begin(){
		MainApp.start();
	}
	
	public void cont(){
		MainApp.cont();
	}

}
