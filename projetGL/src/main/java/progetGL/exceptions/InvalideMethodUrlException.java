package progetGL.exceptions;

public class InvalideMethodUrlException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;

	public InvalideMethodUrlException(String message) {
		this.message=message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
