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
import org.json.JSONObject;

import it.uniroma2.pjdm.dao.BookMarkDAOImpl;
import it.uniroma2.pjdm.entity.Annotazione;

/**
 * Servlet implementation class AddNoteServlet
 */
public class AddNoteServlet extends HttpServlet {
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    PrintWriter out = response.getWriter();
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");

	    try {
	        // Ottieni l'id dell'utente e dell'annotazione dalla richiesta
	        int idUtente = Integer.parseInt(request.getParameter("idUser"));
	        int idLibro = Integer.parseInt(request.getParameter("idBook"));

	        // Ottieni la lista delle annotazioni per l'utente specificato
	        System.out.println("ID Utente: " + idUtente + ", ID Libro: " + idLibro);
	        ArrayList<Annotazione> annotazioni = dao.getNote(idUtente, idLibro);

	        // Crea il JSON da inviare come risposta
	        if (annotazioni.isEmpty()) {
	            System.out.println("Non ci sono annotazioni per il libro con ID: " + idLibro);
	            response.setStatus(HttpServletResponse.SC_OK);
	            out.write("[]");
	            out.flush();
	        } else {
	            response.setStatus(HttpServletResponse.SC_OK);
	            JSONArray getNoteJson = new JSONArray();

	            for (Annotazione annotazione : annotazioni) {
	                JSONObject annotazioneJson = new JSONObject();
	                annotazioneJson.put("idAnnotazione", annotazione.getIdAnnotazione());
	                annotazioneJson.put("testoAnnotazione", annotazione.getTestoAnnotazione());
	                getNoteJson.put(annotazioneJson);
	            }

	            out.write(getNoteJson.toString());
	            out.flush();
	        }
	    } catch (NumberFormatException e) {
	        System.out.println("Parametri non validi: " + e.getMessage());
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Bad Request
	    } catch (Exception e) {
	        System.out.println("Errore durante l'operazione: " + e.getMessage());
	        e.printStackTrace(); // Stampa lo stack trace per identificare l'errore
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Internal Server Error
	    }
	}




	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idUtente = Integer.parseInt(request.getParameter("idUser"));
            int idLibro = Integer.parseInt(request.getParameter("idBook"));
            String testoAnnotazione = request.getParameter("testoAnnotazione");

            // Chiama il metodo per l'aggiunta ai preferiti
            boolean success = dao.addNote(idUtente, idLibro,testoAnnotazione);

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
			int idNote = Integer.parseInt(request.getParameter("idNote"));

			// Chiama il metodo per la rimozione dalle note
			boolean success = dao.removeNote(idNote);

			if (success) {
				System.out.println("Nota rimossa con successo");
				response.setStatus(HttpServletResponse.SC_OK); // Successo
			} else {
				System.out.println("Errore durante la rimozione dalle note");
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
