package projetGL.metier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class NullHostnameVerifier implements HostnameVerifier {
	/**
	 * Host name verifier that does not perform any checks.
	 * An implementation of HostnameVerifier that
	 * returns ok for all, regardless of the certificate
	 */
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}


}

