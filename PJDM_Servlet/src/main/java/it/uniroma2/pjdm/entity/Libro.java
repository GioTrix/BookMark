package it.uniroma2.pjdm.entity;

public class Libro {
	private int idLibro;
	private String titolo;
	private String autore;
	private String genere;
	private String descrizione;
	private String url;
	private int annoPubblicazione;

	public Libro(int idLibro, String titolo, String autore, String genere, String url, int annoPubblicazione) {
		this.idLibro = idLibro;
		this.titolo = titolo;
		this.autore = autore;
		this.genere = genere;
		this.url = url;
		this.annoPubblicazione = annoPubblicazione;
	}

	public Libro(int idLibro, String titolo, String autore, String genere, String url) {
		this.idLibro = idLibro;
		this.titolo = titolo;
		this.autore = autore;
		this.genere = genere;
		this.url = url;
	}

	public Libro(int idLibro, String descrizione, int annoPubblicazione) {
		this.idLibro = idLibro;
		this.descrizione = descrizione;
		this.annoPubblicazione = annoPubblicazione;

	}

	public Libro(int idLibro, String titolo, String autore, String url) {
		this.idLibro = idLibro;
		this.titolo = titolo;
		this.autore = autore;
		this.url = url;
	}

	public int getIdLibro() {
		return idLibro;
	}

	public void setIdLibro(int idLibro) {
		this.idLibro = idLibro;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public String getAutore() {
		return autore;
	}

	public void setAutore(String autore) {
		this.autore = autore;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getGenere() {
		return genere;
	}

	public void setGenere(String genere) {
		this.genere = genere;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public int getAnnoPubblicazione() {
		return annoPubblicazione;
	}

	public void setAnnoPubblicazione(int annoPubblicazione) {
		this.annoPubblicazione = annoPubblicazione;
	}
}
