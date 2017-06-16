package main.control;

import java.awt.event.ActionListener;

/**
 * 
 * @author Mads Østergaard
 *
 */
public interface Observed {
	public void attachListener(ActionListener l);
	
	public void detachListener(ActionListener l);
	
	public void notification(String string);	
}
