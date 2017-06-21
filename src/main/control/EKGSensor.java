package main.control;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import main.MainApp;

/**
 * Ansvarlig for kommunikation med den serielle forbindelse til Arduinoen.
 * 
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
 *
 */
public class EKGSensor implements Sensor {

	private Queue queue = Queue.getInstance();
	private SerialPort port;
	private String input = "";
	private int toOutputCount = 0;
	private double[] outputBuffer = new double[250];
	private boolean running = false;

	/**
	 * Kaldes af <code>Thread.start()</code> i MainApp. Læser input fra porten
	 * og gemmer i <code>outputBuffer</code>. Parser strengen fra porten til double og kalder
	 * <code>addToBuffer()</code> i <code>Queue</code> når der er 250 målinger.
	 */
	public void run() {
		// starter en løkke som vi kan styre...
		running = true;
		while (true) {
			try {
				while (!running) {
					Thread.sleep(100);
				}
				input += port.readString();
				int pos = -1;
				while ((pos = input.indexOf("!")) > -1) {
					outputBuffer[toOutputCount++] = Double.parseDouble(input.substring(0, pos));
					if (toOutputCount == 250) {
						queue.addToBuffer(outputBuffer);
						outputBuffer = new double[250];
						toOutputCount = 0;
					}

					input = input.substring(pos + 1);
				}
				Thread.sleep(100);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SerialPortException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Kaldes i <code>init()</code> for at rense forbindelsen.
	 */
	private void clearLine() {
		try {
			for (int i = 0; i < 100; i++) {
				if (port.getInputBufferBytesCount() > 0) {
					port.readString();
				}
			}
		} catch (SerialPortException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * Opsætter den serielle forbindelse og renser de første målinger.
	 */
	@Override
	public void init() {
		try {
			// Laver et array af porte og sætter de tilgængelige porte ind
			String[] portArray = SerialPortList.getPortNames();
			// Gemmer navnet på den første port i portlisten i portName
			if (portArray.length == 0) {
				System.out.println(
						"Der var ikke tilsluttet nogle enheder til programmet!\nPrøv igen. Programmet lukker.");
				MainApp.stopApp();
			}
			String portName = portArray[0];
			// laver en instans af SerialPort (jssc), port, med argumentet
			// portName, som var dne første port i listen af porte
			port = new SerialPort(portName);
			// porten åbnes
			port.openPort();

			// standard parametre der skal sætes ved åbning af port
			// - 9600 er standard baud rate, som sættes til at være det samme
			// som arduinoen kører
			// - 8 er antallet af dataBits
			// - 1 er stop bits
			// - 0 er paritetstypen
			port.setParams(SerialPort.BAUDRATE_19200, 8, 1, 0);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			port.setDTR(true);

			clearLine();

		} catch (SerialPortException ex) {
			System.out.println("Serial Port Exception: " + ex);
		}
	}

	/**
	 * Pauser tråden
	 */
	@Override
	public void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * Fortsætter tråden
	 */
	@Override
	public void resumeThread() {
		running = true;
	}

	/**
	 * Afslutter forbindelsen
	 */
	@Override
	public void stopConn() {
		try {
			if (port != null) {
				port.closePort();
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

	}

}