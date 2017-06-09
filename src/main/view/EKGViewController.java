package main.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.model.DatabaseConn;

public class EKGViewController implements ActionListener {
	
	private DatabaseConn dtb = null;
	private int latestPulse = 0;
	
	@FXML
	private Label pulseLabel;
	
	public EKGViewController(){
		dtb = DatabaseConn.getInstance();
		dtb.attachListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String eventCommand = arg0.getActionCommand();
		if(eventCommand.equals("Pulse")) handleNewPulse();
	}
	
	private void handleNewPulse(){
		latestPulse = dtb.getPulse();
		pulseLabel.setText(""+latestPulse);
	}

}
