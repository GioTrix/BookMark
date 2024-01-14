package it.uniroma2.pjdm.servlet;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SignupServlet
 */
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private BookMarkDAOImpl dao;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SignupServlet() {
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
		System.out.println("SignupServlet. Invocato metodo doPost");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject resJsonObject = new JSONObject();

		if (request.getParameter("username") == null || request.getParameter("email") == null
				|| request.getParameter("password") == null) {
			response.setStatus(400);
			try {
				resJsonObject.put("result", "Campo non compilato");
			} catch (JSONException e) {
				response.setStatus(533);
				e.printStackTrace();
			}
			response.getWriter().append(resJsonObject.toString());
			return;
		}

		String password = null;
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		password = request.getParameter("password");
		// Boolean active = true;
		try {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			response.setStatus(400);

		}

		int res = -1;

		res = dao.signup(username, email, password);

		if (res == 1) {
			response.setStatus(200);
			try {
				resJsonObject.put("result", "Registrazione avvenuta con successo");
				response.getWriter().append(resJsonObject.toString());
			} catch (JSONException e) {
				response.setStatus(533);
				e.printStackTrace();
			}

		} else {
			response.setStatus(404);
			try {
				resJsonObject.put("result", "Operazione non riuscita");
			} catch (JSONException e) {
				response.setStatus(533);
				e.printStackTrace();
			}
			response.getWriter().append(resJsonObject.toString());
		}

	}
}
