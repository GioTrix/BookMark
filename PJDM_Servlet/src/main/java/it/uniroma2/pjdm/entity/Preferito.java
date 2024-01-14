package it.uniroma2.pjdm.entity;

public class Preferito {	
	private int idPreferito;
	private int idUtente;
	private int idLibro;
	
	public Preferito(int idPreferito, int idUtente, int idLibro) {
		super();
		this.idPreferito = idPreferito;
		this.idUtente = idUtente;
		this.idLibro = idLibro;
	}
	
	public int getIdPreferito() {
		return idPreferito;
	}
	public void setIdPreferito(int idPreferito) {
		this.idPreferito = idPreferito;
	}
	public int getIdUtente() {
		return idUtente;
	}
	public void setIdUtente(int idUtente) {
		this.idUtente = idUtente;
	}
	public int getIdLibro() {
		return idLibro;
	}
	public void setIdLibro(int idLibro) {
		this.idLibro = idLibro;
	}
}
