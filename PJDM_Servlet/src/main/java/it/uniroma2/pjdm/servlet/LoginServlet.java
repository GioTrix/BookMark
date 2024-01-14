package it.uniroma2.pjdm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import org.json.JSONObject;

import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import it.uniroma2.pjdm.entity.Utente;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoginUtente
 */
public class LoginServlet extends HttpServlet {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */

	private BookMarkDAOImpl dao;

	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

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
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Utente utente_res = null;

		String email = request.getParameter("email");
		String password = request.getParameter("password");

		try {
			utente_res = dao.login(email, password);
		} catch (Exception err) {
			err.printStackTrace();
		}

		if (utente_res == null) {
			response.setStatus(200);
			JSONObject res = new JSONObject();
			res.put("error", "User not found");
			out.print(res.toString());
			out.flush();
		} else {
			response.setStatus(200);
			JSONObject res = new JSONObject();
			res.put("idUser", utente_res.getIdUser());
			res.put("username", utente_res.getUsername());
			res.put("email", utente_res.getEmail());
			res.put("token", utente_res.getToken());

			out.print(res.toString());
			out.flush();
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int idUtente = Integer.parseInt(request.getParameter("idUser"));
			String token = request.getParameter("token");

			// Chiama il metodo per la rimozione dai preferiti
			boolean success = dao.logout(idUtente,token);

			if (success) {
				System.out.println("Logout avvenuto con successo");
				response.setStatus(HttpServletResponse.SC_OK); // Successo
			} else {
				System.out.println("Errore durante l'operazione di logout");
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
