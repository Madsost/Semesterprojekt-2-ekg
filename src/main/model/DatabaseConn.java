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

/**
 * 
 * @author Mads Østergaard
 *
 */
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

	/**
	 * 
	 * @return instansen af databaseforbindelsen
	 */
	public static DatabaseConn getInstance() {
		if (instance == null)
			instance = new DatabaseConn();
		return instance;
	}

	 
	/**
	 * put the raw data from the sensor in the database
	 * @param data
	 */
	public synchronized void addData(ArrayList<Double> data) {
		// System.out.println("Tilføj data til database: " +
		// this.getClass().getName());
		try {
			oldID = newID;

			String sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES";
			for (double input : data) {
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
			stmt.closeOnCompletion();
			ResultSet rset2 = stmt.executeQuery(sql);
			if (rset2.next())
				newID = rset2.getInt(1);

			rset2.close();

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
	public synchronized void addPulse(int pulse) throws SQLException {
		// System.out.println("Gemmer puls ... ");
		String sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES(?,?,?);";
		pstmt = conn.prepareStatement(sql);
		pstmt.setInt(1, pulse);
		pstmt.setInt(2, 2);
		pstmt.setInt(3, activeExamination);
		pstmt.execute();
		conn.commit();

		notification("Pulse");

	}

	/**
	 * 
	 * @param length
	 * @return
	 */
	public synchronized ArrayList<Double> getData(int length) throws SQLException {
		// fetch "length" numbers of measurement from the
		// database and put it in an ArrayList
		// System.out.println("Henter ... ");

		ArrayList<Double> list = new ArrayList<>();
		String sql = "SELECT værdi FROM måling WHERE type=1 AND Undersøgelse_idUndersøgelse = " + activeExamination
				+ " AND idMåling < " + newID + " ORDER BY idMåling ASC LIMIT " + length + ";";
		stmt = conn.createStatement();
		stmt.closeOnCompletion();
		ResultSet rset = stmt.executeQuery(sql);
		while (rset.next()) {
			list.add(rset.getDouble(1));
		}
		rset.close();
		return list;
	}

	/**
	 * 
	 */
	public synchronized void stopExamination() throws SQLException {
		String sql = "UPDATE Undersøgelse SET slut = now() WHERE idUndersøgelse =" + activeExamination + ";";
		pstmt = conn.prepareStatement(sql);
		pstmt.execute();
		conn.commit();
	}

	/**
	 * 
	 * @return
	 */
	public synchronized int getPulse() throws SQLException {
		int pulse = 0;
		int pulseID = 0;
		// Hent nyeste puls
		String sql = "SELECT LAST_INSERT_ID() FROM måling WHERE type = 2;";
		stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		if (rset.next())
			pulseID = rset.getInt(1);

		// get the pulse from the database
		sql = "SELECT Værdi FROM måling WHERE idMåling = " + pulseID + ";";
		stmt = conn.createStatement();
		rset = stmt.executeQuery(sql);
		if (rset.next())
			pulse = rset.getInt(1);
		rset.close();
		// stmt.close();
		return pulse;
	}

	/**
	 * 
	 */
	@Override
	public void attachListener(ActionListener l) {
		lyttere.add(l);

	}

	/**
	 * 
	 */
	@Override
	public void detachListener(ActionListener l) {
		lyttere.remove(l);
	}

	/**
	 * 
	 */
	@Override
	public synchronized void notification(String string) {
		ActionEvent event = new ActionEvent(this, 0, string);
		for (Iterator<ActionListener> i = lyttere.iterator(); i.hasNext();) {
			ActionListener l = (ActionListener) i.next();
			l.actionPerformed(event);
		}
	}

	/**
	 * 
	 */
	@Override
	public  void run() {
		System.out.println("Start databasetråd: " + this.getClass().getName());
		try {
			newExamination();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		running = true;
		try {
			while (true) {
				while (!running) {

					Thread.sleep(200);

				}
				// System.out.println("Forsøger at hente fra buffer ... ");
				ArrayList<Double> toDatabase = q.getBuffer();
				if (toDatabase.size() > 0 && toDatabase != null)
					this.addData(toDatabase);
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * 
	 */
	public synchronized void resumeThread() {
		running = true;
	}

	/**
	 * 
	 * @param value
	 */
	public synchronized void setRunning(boolean value) {
		this.running = value;
	}

	/**
	 * 
	 * @throws SQLException
	 */
	public synchronized void newExamination() throws SQLException {
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
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public synchronized ArrayList<Double> getDataToGraph() throws SQLException {
//		long t1 = System.currentTimeMillis();
//		System.out.println("Henter til graf!");
		// System.out.println("Henter til graf: " + this.getClass().getName());
		ArrayList<Double> list = new ArrayList<>();
		String sql = "SELECT værdi FROM måling WHERE type=1 AND Undersøgelse_idUndersøgelse = " + activeExamination
				+ " AND idMåling > " + oldID + " AND idMåling <= " + newID + " ORDER BY idMåling ASC";
		stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		stmt.closeOnCompletion();
		while (rset.next()) {
			list.add(rset.getDouble(1));
		}
		rset.close();
//		long t2 = System.currentTimeMillis();
//		System.out.println("Returnerer: "+(t2-t1));
		return list;
	}

	/**
	 * Metoden undersøger om der er en undersøgelse igang ved at hente en række
	 * i tilstandstabellen. </br>
	 * Hvis rækken findes er der en undersøgelse i gang, hvis ikke er der ikke.
	 * 
	 * @return sand hvis undersøgelsen er i gang, falsk hvis den ikke er.
	 */
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

	/**
	 * Metoden undersøger om der er en undersøgelse igang ved at hente en række
	 * i tilstandstabellen. </br>
	 * Hvis rækken findes er der en undersøgelse i gang, hvis ikke er der ikke.
	 * 
	 * @return sand hvis undersøgelsen er i gang, falsk hvis den ikke er.
	 */
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

	/**
	 * Indsætter eller fjerner en række i tilstandstabellen.
	 * 
	 * @param value
	 *            en booleansk værdi. Hvis sand indsættes en række i
	 *            tilstandstabellen, hvis falsk fjernes den.
	 */
	public synchronized void setExaminationRunning(boolean value) throws SQLException {
		boolean examRunning = value;
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
	}

	/**
	 * Indsætter eller fjerner en række i tilstandstabellen.
	 * 
	 * @param value
	 *            en booleansk værdi. Hvis sand indsættes en række i
	 *            tilstandstabellen, hvis falsk fjernes den.
	 */
	public synchronized void setAppRunning(boolean value) throws SQLException {
		boolean appRunning = value;

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
	}

	/**
	 * Henter starttidspunktet for den nuværende undersøgelse
	 * 
	 * @return starttidspunktet som en streng - format: tt:mm:ss
	 */
	public synchronized String getStartTime() throws SQLException {

		String sql = "SELECT DATE_FORMAT(Start,'%H:%i:%s') FROM undersøgelse WHERE idUndersøgelse = "
				+ activeExamination + ";";
		stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		String time = "";
		if (rset.next()) {
			time = rset.getString(1);
		}
		// System.out.println(time);
		return time;

	}

	/**
	 * Henter data fra databasen fra et starttidspunkt og frem - i samme
	 * undersøgelse som den kørende.</br>
	 * 
	 * Metoden er begrænset til at hente 5000 målinger.
	 * 
	 * @param startTime
	 *            starttidspunktet, hvorfra der skal hentes målinger. Format:
	 *            tt:mm:ss
	 * @return en liste med data fra databasen
	 */
	public synchronized ArrayList<Double> getDataToHistory(String startTime) throws SQLException {
		LocalDate localDate = LocalDate.now();
		String toSQL = localDate.toString() + " " + startTime;
		ArrayList<Double> output = new ArrayList<>();
		String sql = "SELECT Værdi FROM Måling WHERE TIMESTAMP >= '" + toSQL + "' AND Undersøgelse_idUndersøgelse = "
				+ activeExamination + " LIMIT 5000;";
		stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		while (rset.next()) {
			output.add(rset.getDouble(1));
		}
		return output;
	}

	/**
	 * 
	 */
	public synchronized void stopConn() {
		try {
			conn.close();
			stmt.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
