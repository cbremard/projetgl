package projetGL.exceptions;

public class OldVersionNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;
	public OldVersionNotFoundException(String message) {
		this.message=message;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
