package main.model;

import java.awt.event.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

			newExamination();

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
		try {
			String sql = "SELECT LAST_INSERT_ID() FROM Måling WHERE TYPE=1";
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
			if (rset.next())
				oldID = rset.getInt(1);
			for (int input : data) {
				sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES(?,?,"
						+ activeExamination + ")";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, input);
				pstmt.setInt(2, 1);
				pstmt.execute();
				conn.commit();
			}
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
		try {
			String sql = "INSERT INTO måling (værdi, type) VALUES(?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pulse);
			pstmt.setInt(2, 2);
			pstmt.execute();
			conn.commit();
			notification("Pulse");
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

		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			String sql = "SELECT værdi FROM måling WHERE type=1 ORDER BY idMåling ASC";
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

		// get the pulse from the database

		return pulse;
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
		running = true;
		while (running) {
			ArrayList<Integer> toDatabase = q.getBuffer();
			if (toDatabase.size() > 0 && toDatabase != null)
				this.addData(toDatabase);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}

	// TIL TEST:
	public static void main(String[] args) throws InterruptedException {
		DatabaseConn dtb = DatabaseConn.getInstance();
		/*
		 * ArrayList<Integer> data = new ArrayList<>(); for(int i = 0; i<10000;
		 * i++){ data.add(i*10); } dtb.addData(data);
		 */
		ArrayList<Integer> out = new ArrayList<>();
		out = dtb.getData(2);
		for (int tal : out) {
			System.out.println(tal);
		}
		Thread.sleep(2000);
		dtb.stopExamination();
	}

	public void newExamination() {
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

	public ArrayList<Integer> getDataToGraph() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			String sql = "SELECT værdi FROM måling WHERE type=1 AND idMåling >= "+oldID+" AND idMåling <= "+newID+" ORDER BY idMåling ASC";
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

}
