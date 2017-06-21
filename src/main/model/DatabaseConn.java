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
 * Database-klasse. Håndterer kommunikation med MySQL-databasen.
 * 
 * @author Mads Østergaard, Emma Lundgaard og Morten Vorborg.
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
	 * Privat konstruktør. Kaldes af <code>DatabaseConn.getInstance()</code>
	 */
	private DatabaseConn() {
		try {
			Class.forName(JDBC_DRIVER).newInstance();
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			conn.setAutoCommit(false);
			System.out.println("Forbindelse oprettet til databasen!");

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	/**
	 * Opretter en instans af klassen hvis den ikke er oprettet.
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
	 * 
	 * @param data
	 *            arrayListe med input data til databasen.
	 */
	public synchronized void addData(ArrayList<Double> data) {
		try {
			oldID = newID;

			String sql = "INSERT INTO måling (værdi, type, Undersøgelse_idUndersøgelse) VALUES";
			for (double input : data) {
				sql += "(" + input + ",1," + activeExamination + "),";
			}
			int length = sql.length();
			sql = sql.substring(0, length - 1);
			sql += ";";
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
	 *            den puls der skal gemmes i databasen.
	 */
	public synchronized void addPulse(int pulse) throws SQLException {
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
	 * Kaldes af Calculator til pulsberegning.
	 * 
	 * @param length
	 *            antal målinger der skal hentes
	 * @return målingerne fra databasen som arrayList
	 */
	public synchronized ArrayList<Double> getData(int length) throws SQLException {

		ArrayList<Double> list = new ArrayList<>();
		String sql = "SELECT værdi FROM måling WHERE type=1 AND Undersøgelse_idUndersøgelse = " + activeExamination
				+ " AND idMåling < " + newID + " ORDER BY idMåling desc LIMIT " + length + ";";
		stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(sql);
		while (rset.next()) {
			list.add(rset.getDouble(1));
		}
		return list;
	}

	/**
	 * Kaldes når undersøgelsen er slut. Sætter sluttidspunktet til det
	 * nuværende tidspunkt.
	 */
	public synchronized void stopExamination() throws SQLException {
		String sql = "UPDATE Undersøgelse SET slut = now() WHERE idUndersøgelse =" + activeExamination + ";";
		pstmt = conn.prepareStatement(sql);
		pstmt.execute();
		conn.commit();
	}

	/**
	 * Kaldes når grænsefladen skal opdaterer pulsen.
	 * 
	 * @return den seneste puls
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
		return pulse;
	}

	/**
	 * Tilføj lytter på klassen
	 * 
	 * @param l
	 *            lytterobjekt, der implementerer ActionListener
	 */
	@Override
	public void attachListener(ActionListener l) {
		lyttere.add(l);

	}

	/**
	 * Fjern lytter på klassen
	 * 
	 * @param l
	 *            lytterobjekt, der implementerer ActionListener
	 */
	@Override
	public void detachListener(ActionListener l) {
		lyttere.remove(l);
	}

	/**
	 * Giver alle lyttere en notifikation (ActionEvent)
	 * 
	 * @param string
	 *            eventbeskeden til lytterne
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
	 * Kaldes af <code>start()</code> når tråden sættes i gang.
	 */
	@Override
	public void run() {
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
	 * Pauser tråden
	 * 
	 * @throws InterruptedException
	 */
	public void pauseThread() throws InterruptedException {
		running = false;
	}

	/**
	 * Fortsætter tråden
	 */
	public void resumeThread() {
		running = true;
	}

	/**
	 * Opretter en ny undersøgelse. Kaldes i <code>run()</code>
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
	 * Kaldes i EKGViewController til opdatering af graf når den modtager
	 * notifikation om nye målinger.
	 * 
	 * @return en liste med de seneste målinger
	 * @throws SQLException
	 */
	public synchronized ArrayList<Double> getDataToGraph() throws SQLException {
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
	 * Afslutter forbindelsen til databasen.
	 */
	public synchronized void stopConn() {
		try {
			if (stmt != null)
				stmt.close();
			if (pstmt != null)
				pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
