package it.uniroma2.pjdm.exception;

public class UtenteInesistenteException extends Exception{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 8959940163483946086L;

	public UtenteInesistenteException() {
		super("L'ID selezionato non appartiene ad alcun utente");
	}

}
