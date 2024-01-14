package it.uniroma2.pjdm.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import org.json.JSONArray;
import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import it.uniroma2.pjdm.entity.Libro;

public class SearchBooks extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BookMarkDAOImpl dao;

    public SearchBooks() {
        super();
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    
    	
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String filter = request.getParameter("filter"); // Otteniamo il filtro dal parametro della richiesta
        String value = request.getParameter("value"); // Otteniamo il valore dal parametro della richiesta

        try {
            ArrayList<Libro> searchResult = dao.searchBooks(filter, value);

            if (searchResult.isEmpty()) {
                System.out.println("Nessun libro trovato.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                JSONArray searchResultJson = new JSONArray(searchResult);
                out.print(searchResultJson);
                out.flush();
            }
        } catch (SQLException err) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            System.out.println("Errore: " + err.getMessage());
            out.println("Errore: " + err.getMessage());
        }
    }
}
