package it.uniroma2.pjdm.entity;

import java.util.Date;

public class Annotazione {
	private int idAnnotazione;
	private int userID;
	private int bookID;
	private String testoAnnotazione;
	private Date dataCreazione;

	public Annotazione(int idAnnotazione, int userID, int bookID, String testoAnnotazione, Date dataCreazione) {
		this.idAnnotazione = idAnnotazione;
		this.userID = userID;
		this.bookID = bookID;
		this.testoAnnotazione = testoAnnotazione;
		this.dataCreazione = dataCreazione;
	}
	
	public Annotazione(int idAnnotazione, String testoAnnotazione) {
		this.idAnnotazione = idAnnotazione;
		this.testoAnnotazione = testoAnnotazione;
	}

	public Annotazione(String testoAnnotazione) {
		this.testoAnnotazione = testoAnnotazione;

	}

	public int getIdAnnotazione() {
		return idAnnotazione;
	}

	public void setIdAnnotazione(int idAnnotazione) {
		this.idAnnotazione = idAnnotazione;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getBookID() {
		return bookID;
	}

	public void setBookID(int bookID) {
		this.bookID = bookID;
	}

	public String getTestoAnnotazione() {
		return testoAnnotazione;
	}

	public void setTestoAnnotazione(String testoAnnotazione) {
		this.testoAnnotazione = testoAnnotazione;
	}

	public Date getDataCreazione() {
		return dataCreazione;
	}

	public void setDataCreazione(Date dataCreazione) {
		this.dataCreazione = dataCreazione;
	}
}
