package it.uniroma2.pjdm.dao;

import java.sql.SQLException;
import java.util.ArrayList;

import it.uniroma2.pjdm.entity.Annotazione;
import it.uniroma2.pjdm.entity.Libro;
import it.uniroma2.pjdm.entity.Utente;

public interface BookMarkDAO {
	public ArrayList<Libro> loadAllBooks();

	public ArrayList<Libro> loadDetailBooks(int idLibro);

	public ArrayList<Libro> searchBooks(String filter, String value) throws SQLException;

	public ArrayList<Libro> getFav(int idUtente);

	public ArrayList<Utente> getClassificaLettori();

	public void closeConnection();

	public boolean addFav(int idUtente, int idLibro);

	public boolean addNote(int idLibro, int idUtente, String testoAnnotazione);

	public boolean removeNote(int idAnnotazione);

	public ArrayList<Annotazione> getNote(int idUtente, int idLibro);

	public boolean removeFav(int idUtente, int idLibro);

	public Utente login(String email, String password);

	public int signup(String username, String email, String password);

	public Utente checkToken(String token);

	public Utente getUtente(int idUtente);

	public int getBookStats(int idUtente);

	public int getNoteStats(int idUtente);

	public boolean logout(int idUtente, String token);

	public String getGenreStats(int idUtente);
}
