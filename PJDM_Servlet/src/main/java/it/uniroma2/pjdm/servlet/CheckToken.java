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
 * Servlet implementation class CheckToken
 */
public class CheckToken extends HttpServlet {
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

	public CheckToken() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
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

		String token = request.getParameter("token");

		try {
			utente_res = dao.checkToken(token);
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
			res.put("token", token);

			out.print(res.toString());
			out.flush();
		}
	}

}
