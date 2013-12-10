package projetGL.metier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class NullHostnameVerifier implements HostnameVerifier {
	/**
	 * Host name verifier that does not perform nay checks.
	 */
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}


}

