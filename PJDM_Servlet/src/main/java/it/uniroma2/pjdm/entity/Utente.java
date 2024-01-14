package it.uniroma2.pjdm.entity;

public class Utente {
	private int idUser;
	private String username;
	private String email;
	private String password;
	private Boolean active;
	private String token;
	int countBook;


	public Utente(String username, int countBook) {
	    this.username = username;
	    this.countBook = countBook;
	}

	public Utente(int idUser, String username, String email, String password, Boolean active) {

		this.idUser = idUser;
		this.username = username;
		this.email = email;
		this.password = password;
		this.active = active;
	}

	public Utente(String username, String email, String password, boolean active) {
		// TODO Auto-generated constructor stub
		this.username = username;
		this.email = email;
		this.password = password;
		this.active = active;
	}

	public Utente(int idUser, String username, String email, String password) {
		// TODO Auto-generated constructor stub
		this.email = email;
		this.username = username;
		this.password = password;
		this.idUser = idUser;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public int getCountBook() {
		return countBook;
	}

	public void setCountBook(int countBook) {
		this.countBook = countBook;
	}

}
