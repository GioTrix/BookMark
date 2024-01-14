package it.uniroma2.pjdm.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

import it.uniroma2.pjdm.entity.Annotazione;
import it.uniroma2.pjdm.entity.Libro;
import it.uniroma2.pjdm.entity.Utente;

public class BookMarkDAOImpl implements BookMarkDAO {
	private Connection conn;

	public BookMarkDAOImpl(String ip, String port, String dbName, String dbUserName, String dbPwd)
			throws ClassNotFoundException, SQLException {

		// connessione al database
		Class.forName("com.mysql.cj.jdbc.Driver");

		conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + dbName
				+ "?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
				dbUserName, dbPwd);
	}

	public ArrayList<Libro> loadAllBooks() {
		ArrayList<Libro> res = new ArrayList<Libro>();
		String query = "SELECT BookID, Titolo, Autore, Genere, URLCopertina FROM libro order by Rand() asc limit 10";

		try (Statement stmt = conn.createStatement(); ResultSet rset = stmt.executeQuery(query)) {
			while (rset.next()) {
				int idLibro = rset.getInt(1);
				String titolo = rset.getString(2);
				String autore = rset.getString(3);
				String genere = rset.getString(4);
				String url = rset.getString(5);
				res.add(new Libro(idLibro, titolo, autore, genere, url));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public ArrayList<Utente> getClassificaLettori() {
		ArrayList<Utente> res = new ArrayList<Utente>();
		String query = "SELECT U.Username, COUNT(LP.BookID) AS numLibriPreferiti\r\n" + "FROM Utente U\r\n"
				+ "LEFT JOIN Preferito LP ON U.UserID = LP.UserID\r\n" + "GROUP BY U.Username\r\n"
				+ "ORDER BY numLibriPreferiti DESC\r\n" + "LIMIT 10";

		try (Statement stmt = conn.createStatement(); ResultSet rset = stmt.executeQuery(query)) {
			while (rset.next()) {
				String username = rset.getString(1);
				int countBook = rset.getInt(2);
				res.add(new Utente(username, countBook));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public ArrayList<Libro> loadDetailBooks(int idLibro) {
		ArrayList<Libro> res = new ArrayList<>();
		String query = "SELECT Descrizione, AnnoPubblicazione FROM libro WHERE BookID = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idLibro); // Set the parameter value

			try (ResultSet rset = pstmt.executeQuery()) {
				while (rset.next()) {
					String descrizione = rset.getString("Descrizione");
					int annoPubblicazione = rset.getInt("AnnoPubblicazione");
					res.add(new Libro(idLibro, descrizione, annoPubblicazione));

					System.out.println("Descrizione: " + descrizione);
					System.out.println("Anno Pubblicazione: " + annoPubblicazione);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			Enumeration<Driver> enumDrivers = DriverManager.getDrivers();
			while (enumDrivers.hasMoreElements()) {
				Driver driver = enumDrivers.nextElement();
				DriverManager.deregisterDriver(driver);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public ArrayList<Libro> searchBooks(String filter, String value) throws SQLException {
		ArrayList<Libro> res = new ArrayList<Libro>();
		String query = "SELECT BookID, Titolo, Autore, Genere, URLCopertina FROM libro WHERE ";

		// Aggiungi i segnaposto per i parametri in tutti i casi
		switch (filter) {
		case "autore":
			query += "autore LIKE ?";
			break;

		case "titolo":
			query += "titolo LIKE ?";
			break;

		case "genere":
			query += "genere LIKE ?";
			break;

		default:
			// Includi un caso predefinito che non aggiunge alcun segnaposto
			query += "1 = 1"; // Una condizione sempre vera per evitare errori
			break;
		}

		PreparedStatement pstmt = conn.prepareStatement(query);

		// Imposta il parametro indipendentemente dalla condizione
		pstmt.setString(1, "%" + value + "%");

		ResultSet rset = pstmt.executeQuery();

		while (rset.next()) {
			int idLibro = rset.getInt(1);
			String titolo = rset.getString(2);
			String autore = rset.getString(3);
			String genere = rset.getString(4);
			String url = rset.getString(5);

			res.add(new Libro(idLibro, titolo, autore, genere, url));
		}

		rset.close();
		pstmt.close();

		return res;
	}

	@Override
	public boolean addFav(int idUtente, int idLibro) {
		String insertQuery = "INSERT INTO preferito (UserID, BookID) VALUES (?, ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
			pstmt.setInt(1, idUtente);
			pstmt.setInt(2, idLibro);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Libro aggiunto ai preferiti con successo.");
				return true; // Operazione riuscita
			} else {
				System.out.println("Errore durante l'aggiunta del libro ai preferiti.");
			}
		} catch (SQLException e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
		}

		return false; // Operazione fallita
	}

	@Override
	public ArrayList<Libro> getFav(int idUtente) {
		ArrayList<Libro> res = new ArrayList<>();
		String selectQuery = "SELECT l.* FROM preferito p " + "JOIN libro l ON p.BookID = l.BookID "
				+ "WHERE p.UserID = ? " + "ORDER BY l.Titolo ASC";

		try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
			pstmt.setInt(1, idUtente);

			try (ResultSet rset = pstmt.executeQuery()) {
				while (rset.next()) {
					int idLibro = rset.getInt("BookID");
					String titolo = rset.getString("Titolo");
					String autore = rset.getString("Autore");
					String genere = rset.getString("Genere");
					String url = rset.getString("URLCopertina");

					res.add(new Libro(idLibro, titolo, autore, genere, url));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public boolean removeFav(int idUtente, int idLibro) {
		String deleteQuery = "DELETE FROM Preferito WHERE UserID = ? AND BookID = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
			pstmt.setInt(1, idUtente);
			pstmt.setInt(2, idLibro);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Libro rimosso dai preferiti con successo.");
				return true; // Operazione riuscita
			} else {
				System.out.println(
						"Errore durante la rimozione del libro dai preferiti. Il libro potrebbe non essere presente nei preferiti.");
			}
		} catch (SQLException e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			e.printStackTrace();
		}
		return false; // Operazione fallita
	}

	public String initSessione(Integer idUtente) {
		UUID uuid = UUID.randomUUID();
		String insertQuery = "INSERT INTO sessione (token, UserID, created, expiration) VALUES (?, ?, NOW(), NOW()+INTERVAL 1 WEEK)";
		int rowsAffected = 0;

		try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
			// Assegna i valori alle variabili
			pstmt.setString(1, uuid.toString());
			pstmt.setInt(2, idUtente);

			// Esegui la query e ottieni il resultset
			rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Sessione Utente creata con successo.");
				return uuid.toString();
			} else {
				System.out.println("Errore durante la creazione della sessione utente.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Utente login(String email, String password) {
		Utente res = null;
		try {

			// Prepara la query con i template delle variabili
			PreparedStatement statement = this.conn.prepareStatement(
					"SELECT UserID, Username,Email, Password from utente where Email = ? and Password = md5(?)");

			// Assegna i valori alle variabili
			statement.setString(1, email);
			statement.setString(2, password);

			// Esegui la query e ottieni il resultset
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int iduser = rs.getInt(1);
				String username = rs.getString(2);

				int count = 1;
				String uuid = this.initSessione(iduser);
				while (uuid == null && count < 4) {
					uuid = this.initSessione(iduser);
				}

				if (uuid == null) {
					System.out.println("Non è stato possibile generare il token");
					return null;
				}

				res = new Utente(iduser, username, email, password);
				res.setToken(uuid);
				System.out.println("User authenticated successfully");
			} else {
				System.out.println("Invalid email or password!");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public boolean logout(int idUtente, String token) {
		String deleteQuery = "DELETE FROM sessione WHERE UserID=? and token = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
			pstmt.setInt(1, idUtente);
			pstmt.setString(2, token);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Logout avvenuto con successo.");
				return true; // Operazione riuscita
			} else {
				System.out.println("Errore durante durante l'operazione di logout");
			}
		} catch (SQLException e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			e.printStackTrace();
		}
		return false; // Operazione fallita
	}

	@Override
	public Utente checkToken(String token) {
		Utente res = null;
		try {

			// Prepara la query con i template delle variabili
			PreparedStatement statement = this.conn
					.prepareStatement("SELECT UserID from sessione where token = ? and expiration > now()");

			// Assegna i valori alle variabili
			statement.setString(1, token);

			// Esegui la query e ottieni il resultset
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int iduser = rs.getInt(1);
				res = this.getUtente(iduser);
				System.out.println("User authenticated successfully");
			} else {
				System.out.println("Invalid token!");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public int signup(String username, String email, String password) {
		String insertQuery = "INSERT INTO utente (username, email, password) VALUES (?, ?, md5(?))";
		int rowsAffected = 0;

		try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
			// Assegna i valori alle variabili
			pstmt.setString(1, username);
			pstmt.setString(2, email);
			pstmt.setString(3, password);

			// Esegui la query e ottieni il resultset
			rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Utente registrato con successo.");
			} else {
				System.out.println("Errore durante la registrazione.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowsAffected;
	}

	@Override
	public boolean addNote(int idLibro, int idUtente, String testoAnnotazione) {
		String insertQuery = "INSERT INTO annotazione (UserID, BookID, TestoAnnotazione) VALUES (?, ?, ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
			pstmt.setInt(1, idLibro);
			pstmt.setInt(2, idUtente);
			pstmt.setString(3, testoAnnotazione);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Annotazione inserita con successo.");
				return true; // Operazione riuscita
			} else {
				System.out.println("Errore durante l'inserimento dell'annotazione.");
			}
		} catch (SQLException e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
		}

		return false; // Operazione fallita
	}

	@Override
	public ArrayList<Annotazione> getNote(int idUtente, int idLibro) {
		ArrayList<Annotazione> res = new ArrayList<>();
		String selectQuery = "SELECT AnnotationID, TestoAnnotazione FROM annotazione WHERE UserID = ? AND BookID = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
			pstmt.setInt(1, idUtente);
			pstmt.setInt(2, idLibro);

			try (ResultSet rset = pstmt.executeQuery()) {
				while (rset.next()) {
					int idAnnotazione = rset.getInt("AnnotationID");
					String testoAnnotazione = rset.getString("TestoAnnotazione");
					res.add(new Annotazione(idAnnotazione, testoAnnotazione));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public Utente getUtente(int idUtente) {
		Utente res = null;
		try {

			// Prepara la query con i template delle variabili
			PreparedStatement statement = this.conn
					.prepareStatement("SELECT UserID, Username,Email from utente where UserID=?");

			// Assegna i valori alle variabili
			statement.setInt(1, idUtente);

			// Esegui la query e ottieni il resultset
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int iduser = rs.getInt(1);
				String username = rs.getString(2);
				String email = rs.getString(3);

				res = new Utente(iduser, username, email, null);
				System.out.println("User found");
			} else {
				System.out.println("User not found!");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	@Override
	public boolean removeNote(int idAnnotazione) {
		String deleteQuery = "DELETE FROM annotazione WHERE AnnotationID = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
			pstmt.setInt(1, idAnnotazione);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
				System.out.println("Nota rimossa con successo.");
				return true; // Operazione riuscita
			} else {
				System.out.println("Errore durante la rimozione della nota");
			}
		} catch (SQLException e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			e.printStackTrace();
		}
		return false; // Operazione fallita
	}

	@Override
	public int getBookStats(int idUtente) {
		int numeroLibriSalvati = 0;
		String query = "SELECT COUNT(*) AS NumeroLibriSalvati FROM preferito WHERE UserID = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idUtente);

			try (ResultSet resultSet = pstmt.executeQuery()) {
				if (resultSet.next()) {
					numeroLibriSalvati = resultSet.getInt("NumeroLibriSalvati");
					System.out.println("Numero di libri letti: " + numeroLibriSalvati);
				} else {
					System.out.println("Nessun risultato trovato.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return numeroLibriSalvati;
	}

	@Override
	public int getNoteStats(int idUtente) {
		int numeroNoteSalvate = 0;
		String query = "SELECT COUNT(*) AS NumeroNote FROM annotazione WHERE UserID = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idUtente);

			try (ResultSet resultSet = pstmt.executeQuery()) {
				if (resultSet.next()) {
					numeroNoteSalvate = resultSet.getInt("NumeroNote");
					System.out.println("Numero di note salvate: " + numeroNoteSalvate);
				} else {
					System.out.println("Nessun risultato trovato.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return numeroNoteSalvate;
	}

	@Override
	public String getGenreStats(int idUtente) {
		String genere = "";
		String query = "SELECT l.Genere FROM Libro l INNER JOIN Preferito p ON l.BookID = p.BookID WHERE p.UserID = ? "
				+ "GROUP BY l.Genere ORDER BY COUNT(*) DESC LIMIT 1";
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setInt(1, idUtente);

			try (ResultSet resultSet = pstmt.executeQuery()) {
				if (resultSet.next()) {
					genere = resultSet.getString("Genere");
					System.out.println("Genere preferito: " + genere);
				} else {
					System.out.println("Nessun risultato trovato.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return genere;
	}
}
