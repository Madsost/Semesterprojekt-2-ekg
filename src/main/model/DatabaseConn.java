package main.model;

import java.awt.event.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sun.javafx.scene.layout.region.Margins;

import main.control.Observed;
import main.control.Queue;

public class DatabaseConn extends Thread implements Observed {

	private List<ActionListener> lyttere = new ArrayList<ActionListener>();

	private static DatabaseConn instance;
	private Queue q = Queue.getInstance();
	private boolean running = false;
	private Connection conn = null;
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private int activeExamination = 0;

	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/62453_datb?useSSL=false";

	private final String USER = "java";
	private final String PASS = "1234";

	private int oldID = 0;
	private int newID = 0;

	/**
	 * EKG = 1, PULS = 2.
	 */
	private DatabaseConn() {
		try {
			Class.forName(JDBC_DRIVER).newInstance();
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			System.out.println("Forbindelse oprettet til databasen!");

			// newExamination();

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public static DatabaseConn getInstance() {
		if (instance == null)
			instance = new DatabaseConn();
		return instance;
	}

	// put the raw data from the sensor in the database
	/**
	 * 
	 * @param data
	 */
	public synchronized void addData(ArrayList<Integer> data) {
		System.out.println("Tilføj data til database: " + this.getClass().getName());
		try {
			String sql = "SELECT LAST_INSERT_ID() FROM Måling WHERE TYPE=1";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next())
				oldID = rset.getInt(1);
			sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES";
			for (int input : data) {
				sql += "(" + input + ",1," + activeExamination + "),";
			}
			int length = sql.length();
			sql = sql.substring(0, length - 1);
			// System.out.println(sql);
			sql += ";";
			// System.out.println(sql);
			pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			conn.commit();

			sql = "SELECT LAST_INSERT_ID() FROM Måling";
			stmt = conn.createStatement();
			ResultSet rset2 = stmt.executeQuery(sql);
			if (rset2.next())
				newID = rset2.getInt(1);
			notification("EKG");
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * Gemmer en puls i databasen. </br>
	 * 
	 * @param pulse
	 */
	public synchronized void addPulse(int pulse) {
		System.out.println("Gemmer puls ... ");
		try {
			String sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES(?,?,?);";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pulse);
			pstmt.setInt(2, 2);
			pstmt.setInt(3, activeExamination);
			pstmt.execute();
			conn.commit();

			// notification("Pulse");
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

	}

	/**
	 * 
	 * @param length
	 * @return
	 */
	public synchronized ArrayList<Integer> getData(int length) {
		// fetch "length" numbers of measurement from the
		// database and put it in an ArrayList
		System.out.println("Henter ... ");

		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			String sql = "SELECT værdi FROM måling WHERE type=1 AND Undersøgelse_idUndersøgelse = " + activeExamination
					+ " AND idMåling < " + newID + " ORDER BY idMåling ASC LIMIT " + length + ";";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				list.add(rset.getInt(1));
			}
			return list;
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public synchronized void stopExamination() {
		try {
			String sql = "UPDATE Undersøgelse SET slut = now() WHERE idUndersøgelse =" + activeExamination + ";";
			pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			conn.commit();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	/**
	 * 
	 * @return
	 */
	public synchronized int getPulse() {
		int pulse = 0;
		int pulseID = 0;
		try {
			// Hent nyeste puls
			String sql = "SELECT LAST_INSERT_ID() FROM måling WHERE type = 2;";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next())
				pulseID = rset.getInt(1);
			// System.out.println("Puls id: " + pulseID);
			// get the pulse from the database
			sql = "SELECT Værdi FROM måling WHERE idMåling = " + pulseID + ";";
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset.next())
				pulse = rset.getInt(1);
			// System.out.println("Puls fra dtb: " + pulse);
			return pulse;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public void attachListener(ActionListener l) {
		lyttere.add(l);

	}

	@Override
	public void detachListener(ActionListener l) {
		lyttere.remove(l);
	}

	@Override
	public void notification(String string) {
		ActionEvent event = new ActionEvent(this, 0, string);
		for (Iterator<ActionListener> i = lyttere.iterator(); i.hasNext();) {
			ActionListener l = (ActionListener) i.next();
			l.actionPerformed(event);
		}
	}

	@Override
	public void run() {
		System.out.println("Start databasetråd: " + this.getClass().getName());
		newExamination();
		running = true;
		try {
			while (true) {
				while (!running) {

					Thread.sleep(200);

				}
				// System.out.println("Forsøger at hente fra buffer ... ");
				ArrayList<Integer> toDatabase = q.getBuffer();
				if (toDatabase.size() > 0 && toDatabase != null)
					this.addData(toDatabase);
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void pauseThread() throws InterruptedException {
		running = false;
	}

	public synchronized void resumeThread() {
		running = true;
	}

	public synchronized void setRunning(boolean value) {
		this.running = value;
	}

	public synchronized void newExamination() {
		try {
			System.out.println("Opretter undersøgelse ... ");
			// hent den aktuelle undersøgelsesID og gem i activeExamination
			String sql = "INSERT INTO Undersøgelse (Start, Slut) VALUES (now(), now());";
			pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			conn.commit();
			System.out.println("Undersøgelse oprettet!");

			System.out.println("Henter undersøgelsesID ...");
			sql = "SELECT LAST_INSERT_ID() FROM Undersøgelse";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next())
				activeExamination = rset.getInt(1);
			System.out.println("UndersøgelsesID hentet!");
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

	}

	public synchronized ArrayList<Integer> getDataToGraph() {
		// System.out.println("Henter til graf: " + this.getClass().getName());
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			String sql = "SELECT værdi FROM måling WHERE type=1 AND Undersøgelse_idUndersøgelse = " + activeExamination
					+ " AND idMåling > " + oldID + " AND idMåling <= " + newID + " ORDER BY idMåling ASC";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				list.add(rset.getInt(1));
			}
			return list;
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return null;
		}
	}

	public synchronized boolean isAppRunning() {
		boolean isRunning = false;
		try {
			String sql = "SELECT * FROM tilstand WHERE idTilstand = 1;";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next()) {
				isRunning = (rset.getInt(1) == 1);
			}
			return isRunning;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized boolean isExamRunning() {
		boolean isRunning = false;
		try {
			String sql = "SELECT idTilstand FROM tilstand WHERE idTilstand = 2;";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next()) {
				isRunning = (rset.getInt(1) == 2);
			}

			return isRunning;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized void setExaminationRunning(boolean value) {
		boolean examRunning = value;
		try {
			if (examRunning) {
				String sql = "INSERT INTO tilstand VALUES (2);";
				pstmt = conn.prepareStatement(sql);
				pstmt.execute();
				conn.commit();
			} else {
				String sql = "DELETE FROM tilstand WHERE idTilstand = 2";
				pstmt = conn.prepareStatement(sql);
				pstmt.execute();
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void setAppRunning(boolean value) {
		boolean appRunning = value;
		try {
			if (appRunning) {
				String sql = "INSERT INTO tilstand VALUES (1);";
				pstmt = conn.prepareStatement(sql);
				pstmt.execute();
				conn.commit();
			} else {
				String sql = "DELETE FROM tilstand WHERE idTilstand = 1";
				pstmt = conn.prepareStatement(sql);
				pstmt.execute();
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized String getStartTime() {
		try {
			String sql = "SELECT DATE_FORMAT(Start,'%H:%i:%s') FROM undersøgelse WHERE idUndersøgelse = "
					+ activeExamination + ";";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			String time = "";
			if (rset.next()) {
				time = rset.getString(1);
			}
			System.out.println(time);
			return time;

		} catch (SQLException e) {

			return null;
		}
	}

	public ArrayList<Integer> getDataToHistory(String startTime) {
		LocalDate localDate = LocalDate.now();
		String toSQL = localDate.toString() + " " + startTime;
		ArrayList<Integer> output = new ArrayList<>();
		try {
			String sql = "SELECT * FROM Måling WHERE TIMESTAMP >= '" + toSQL + "' AND Undersøgelse_idUndersøgelse = "
					+ activeExamination + ";";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			while (rset.next()) {
				output.add(rset.getInt(2));
			}
			return output;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * static void main(String[] args) { DatabaseConn dtb =
	 * DatabaseConn.getInstance(); dtb.getDataToHistory(dtb.getStartTime()); }
	 */

}
