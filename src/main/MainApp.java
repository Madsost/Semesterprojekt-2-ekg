package main;

import main.control.Calculator;
import main.control.EKGSensor;
import main.control.GuiController;
import main.control.Sensor;
import main.control.TestSensor;
import javafx.application.Application;

public class MainApp{
	private static boolean running = false;
	private static Calculator cal = null;
	private static Sensor s = null;

	private static void run() {
		while(running){
			// beregn puls
			
			// vent 
		}
	}
	
	private static void init() {
		cal = new Calculator();
		// if test
		s = new TestSensor();
		// if not test
		s = new EKGSensor();
	}
	
	public static void start(){
		running = true; 
		
		run();
	}
	
	public static void stop(){
		running = false;
	}

	
	public static void main(String[] args) {
		// tråd til GUI
		Thread guiThread = new Thread(){
			public void run(){
				Application.launch(GuiController.class);
			}
		};
		guiThread.start();	
		//init();
		
	}

	public static void pauseSensor() {
		try {
			s.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void cont() {
		s.notify();
	}


}

// yolo Dr. Vobs til tjeneste