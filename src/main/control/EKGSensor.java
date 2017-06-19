package main.control;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * 
 * @author Mads Østergaard
 *
 */
public class EKGSensor implements Sensor {

	private Queue queue = Queue.getInstance();
	private int baudRate = 19200;
	private SerialPort port;
	private String input = "";
	private int toOutputCount = 0;
	private double[] outputBuffer = new double[250];
	private boolean running = false;
	private String split = "!";

	/**
	 * 
	 */
	public EKGSensor() {
		// init();
	}

	/**
	 * 
	 * @param event
	 */
	public void measure(SerialPortEvent event) {
		try {
			if (event.getEventValue() > 0) {

				// put what is on the buffer in a String
				input += port.readString(event.getEventValue());

				// while there is someting in the string we read, we
				// extract
				// values from it
				while (input.contains(split)) {
					// if there are null og nothing instead of a
					// value, remove
					// it
					if (input.substring(0, input.indexOf(split)).contains("null")
							|| input.substring(0, input.indexOf(split)).equals("")) {
						input = input.substring(input.indexOf(split) + 1);
					}

					// read the next value, parse it to an int and
					// put it in the Queue
					if (!(input.substring(0).equals(split))) {
						// System.out.println(toOutputCount);
						outputBuffer[toOutputCount++] = Double.parseDouble(input.substring(0, input.indexOf(split)));
						if (toOutputCount == 250) {
							queue.addToBuffer(outputBuffer);
							outputBuffer = new double[250];
							toOutputCount = 0;
						}
						// removes the value we just saved to
						// the queue so
						// we dont read it again
						input = input.substring(input.indexOf(split) + 1);

					}
				}
			}
		} catch (SerialPortException e) {
			System.out.println("Fik ikke læst fra porten ");
			e.printStackTrace();
		} catch (NumberFormatException ex) {
			return;
		}
	}

	/**
	 * 
	 */
	public void run() {

		try {
			// we listen on the Serial port
			port.addEventListener(new SerialPortEventListener() {

				@Override
				public void serialEvent(SerialPortEvent event) {
					measure(event);
				}

			});
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

		// starter en løkke som vi kan styre...
		while (true) {
			while (!running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void clearLine() {
		try {
			for (int i = 0; i < 100; i++) {
				// hvis der er målinger på vej
				String buffer = "";
				if (port.getInputBufferBytesCount() > 0) {
					buffer += port.readString();
					int pos = -1;
					if ((pos = buffer.lastIndexOf("!")) > -1) {
						buffer = buffer.substring(pos + 1);
					}
				}
			}
		} catch (SerialPortException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * 
	 */
	@Override
	public void init() {
		try {
			// Laver et array af porte og sætter de tilgængelige porte ind
			String[] portArray = SerialPortList.getPortNames();
			// Gemmer navnet på den første port i portlisten i portName
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
			port.setParams(baudRate, 8, 1, 0);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			port.setDTR(true);

			clearLine();

		} catch (SerialPortException ex) {
			System.out.println("Serial Port Exception: " + ex);
		}
	}

	/**
	 * 
	 */
	@Override
	public void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * 
	 */
	@Override
	public void resumeThread() {
		running = true;
	}

	/**
	 * 
	 */
	@Override
	public void stopConn() {
		try {
			port.closePort();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

	}

}