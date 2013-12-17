package projetGL.metier;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import progetGL.exceptions.InvalideMethodUrlException;
import progetGL.exceptions.MaxRequestException;
import progetGL.exceptions.OldVersionNotFoundException;

public abstract class Api extends MethodJunior{
	protected int resquestCounter = 0;
	protected final int maxRequest = 5000;
	
	public int getResquestCounter() {
		return resquestCounter;
	}
	public void setResquestCounter(int resquestCounter) {
		this.resquestCounter = resquestCounter;
	}
	public int getMaxRequest() {
		return maxRequest;
	}
	
	abstract protected JSONObject getCommit(String user, String repository) throws OldVersionNotFoundException;
	
	/**
	 * Gères les requêtes lancées sur les APIs de Github et incrément le compteur de requête.
	 * @author BREMARD Corentin
	 * @param multiPages 
	 * @param url: l'url à soumettre
	 * @return le Json renvoyé par les API de Github si tout ce passe bien. Lève une exception sinon.
	 * @throws InvalideMethodUrlException 
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws MaxRequestException 
	 */
	protected GetMethod sendRequest(String request) throws InvalideMethodUrlException, HttpException, IOException, MaxRequestException{
		int statusCode;
		HttpClient client = new HttpClient();
		GetMethod gmethod;
		if(resquestCounter>=maxRequest){
			throw new MaxRequestException("You reached the maximum of request on Github's API");
		}else{
			gmethod = new GetMethod(request);
			statusCode = client.executeMethod(gmethod);
			resquestCounter++;
			if (statusCode != HttpStatus.SC_OK) {
				System.err.print("Unexpected result with URL "+request+" : ");
				throw new InvalideMethodUrlException(gmethod.getStatusText());
			}
		}
		return gmethod;
	}
}
