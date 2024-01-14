package it.uniroma2.pjdm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import org.json.JSONObject;

import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GetGenreStats
 */
public class GetGenreStats extends HttpServlet {
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		try {
			int idUtente = Integer.parseInt(request.getParameter("idUser"));

			String genere = dao.getGenreStats(idUtente);

			// Controlla il risultato e scrive l'output
			if (genere.isEmpty()) {
				System.out.println("Nessun risultato");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Not Found
			} else {
				response.setStatus(HttpServletResponse.SC_OK); // OK
				JSONObject genreStatsJson = new JSONObject();
				genreStatsJson.put("genere", genere);
				out.print(genreStatsJson.toString());
				out.flush();
			}
		} catch (NumberFormatException e) {
			System.out.println("Parametro idBook non valido: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Bad Request
		} catch (Exception e) {
			System.out.println("Errore durante l'operazione: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}