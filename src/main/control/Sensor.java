package main.control;

/**
 * Definerer grænsefladen til sensorerne.
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
 *
 */
public interface Sensor extends Runnable {

	public void init();

	public void run();

	public void pauseThread() throws InterruptedException;

	public void resumeThread();
	
	public void stopConn();
}
