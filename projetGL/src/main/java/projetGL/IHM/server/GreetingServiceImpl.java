package projetGL.IHM.server;

import java.util.ArrayList;

import projetGL.IHM.client.GreetingService;
import projetGL.IHM.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		return null;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	public ArrayList<String> greetServer(ArrayList<String> input)
			throws IllegalArgumentException {
		
		if (!FieldVerifier.isValidName(input.get(0))) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"groupId must be at least 2 characters long");
		}
		
		// Escape data from the client to avoid cross-site script vulnerabilities.
		input.set(0, escapeHtml(input.get(0)));
		input.set(1, escapeHtml(input.get(1)));
		input.set(2, escapeHtml(input.get(2)));
		input.set(3, escapeHtml(input.get(3)));
		
		ArrayList<String> retour = new ArrayList<String>();
		retour.add("Le résultat du calcul est : ");
		// TODO lancement et retour calcul
		return retour ;
	}
}
