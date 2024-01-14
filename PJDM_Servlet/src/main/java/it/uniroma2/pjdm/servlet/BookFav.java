package it.uniroma2.pjdm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;

import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import it.uniroma2.pjdm.entity.Libro;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BookFav
 */
public class BookFav extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookMarkDAOImpl dao;

	public void init() throws ServletException {
		String ip = getServletContext().getInitParameter("ip");
		String port = getServletContext().getInitParameter("port");
		String dbName = getServletContext().getInitParameter("dbName");
		String userName = getServletContext().getInitParameter("userName");
		String password = getServletContext().getInitParameter("password");

		System.out.print("BookMark. Opening DB connection...");

		try {
			dao = new BookMarkDAOImpl(ip, port, dbName, userName, password);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("DONE.");
	}

	public void destroy() {
		System.out.print("BookMark. Closing DB connection...");
		dao.closeConnection();
		System.out.println("DONE.");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		try {
			// Ottieni l'id dell'utente dalla richiesta
			int idUtente = Integer.parseInt(request.getParameter("idUser"));

			// Ottieni la lista dei libri preferiti per l'utente specificato
			ArrayList<Libro> preferiti = dao.getFav(idUtente);

			// Crea il JSON da inviare come risposta
			if (preferiti.isEmpty()) {
				System.out.println("Non ci sono preferiti per l'utente con ID: " + idUtente);
				response.setStatus(HttpServletResponse.SC_OK);
				out.write("[]");
				out.flush();
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				JSONArray getFavJson = new JSONArray(preferiti);
				out.print(getFavJson);
				out.flush();
			}
		} catch (NumberFormatException e) {
			System.out.println("Parametri non validi: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Bad Request
		} catch (Exception e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int idUtente = Integer.parseInt(request.getParameter("idUser"));
			int idLibro = Integer.parseInt(request.getParameter("idBook"));

			// Chiama il metodo per l'aggiunta ai preferiti
			boolean success = dao.addFav(idUtente, idLibro);

			if (success) {
				System.out.println("Libro aggiunto ai preferiti con successo");
				response.setStatus(HttpServletResponse.SC_OK); // Successo
			} else {
				System.out.println("Errore durante l'aggiunta ai preferiti");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
			}
		} catch (NumberFormatException e) {
			System.out.println("Parametri non validi: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Bad Request
		} catch (Exception e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int idUtente = Integer.parseInt(request.getParameter("idUser"));
			int idLibro = Integer.parseInt(request.getParameter("idBook"));

			// Chiama il metodo per la rimozione dai preferiti
			boolean success = dao.removeFav(idUtente, idLibro);

			if (success) {
				System.out.println("Libro rimosso dai preferiti con successo");
				response.setStatus(HttpServletResponse.SC_OK); // Successo
			} else {
				System.out.println("Errore durante la rimozione dai preferiti");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
			}
		} catch (NumberFormatException e) {
			System.out.println("Parametri non validi: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Bad Request
		} catch (Exception e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
		}
	}
}
