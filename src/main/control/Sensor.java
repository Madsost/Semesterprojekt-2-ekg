package main.control;

public interface Sensor extends Runnable {

	public void init();

	public void run();

	public void pauseThread() throws InterruptedException;

	public void resumeThread();
	
	public void stopConn();

}
