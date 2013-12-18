package projetGL.metier;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import progetGL.exceptions.IdentificationFailledException;
import progetGL.exceptions.InvalideMethodUrlException;
import progetGL.exceptions.MaxRequestException;
import progetGL.exceptions.OldVersionNotFoundException;

public abstract class Api extends MethodJunior{
	protected int resquestCounter;
	protected int maxRequest;
	protected int accountIndex;
	protected ArrayList<String> accesTokens;
	
	public Api() {
		super();
		accesTokens = new ArrayList<String>();
		accesTokens.add("f3893d75651ca47a2a7fa1b53b6176caf88b49e3"); // cbremard's accces
		accesTokens.add("5a90a9a5cd81ee0280f3fbda6897e733384288ad"); // Fanny's accces
		resquestCounter = -1;
		maxRequest = -1;
		accountIndex = 0;
	}
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
	abstract protected boolean authentification(int accountIndex) throws IdentificationFailledException;
	
	/**
	 * Gères les requêtes lancées sur les APIs de Github en 3 étapes.
	 *    I. Gestion des authentifications afin obtenir un plus grand nombre de requête
	 *    II. Exécution de la requête et incrémentation du compteur de requête
	 *    III. Vérifications/synchronisation des compteurs de l'application avec les réponses de la requête
	 * @author BREMARD Corentin
	 * @param multiPages 
	 * @param url: l'url à soumettre
	 * @return le Json renvoyé par les API de Github si tout ce passe bien. Lève une exception sinon.
	 * @throws InvalideMethodUrlException 
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws MaxRequestException 
	 * @throws IdentificationFailledException 
	 */
	protected GetMethod sendRequest(String request) throws InvalideMethodUrlException, HttpException, IOException, MaxRequestException{
		int statusCode, maxRequestExpected, resquestCounterExpected;
		HttpClient client = new HttpClient();
		GetMethod gmethod;
		boolean authentifactionSuccessed, isApiRequest;
		String authentifiedRequest, refText;
		/* I. Gestion des authentifications afin obtenir un plus grand nombre de requête */
		authentifactionSuccessed = true;
		if((maxRequest-resquestCounter)<=0){
			authentifactionSuccessed = false;
			while(!authentifactionSuccessed && accountIndex<accesTokens.size()){
				try {
					authentifactionSuccessed = authentification(accountIndex);
				} catch (IdentificationFailledException e) {
					System.err.println("Fail to connect at the account number "+accountIndex);
				}
				accountIndex++;
			}
		}
		if(!authentifactionSuccessed){
			throw new MaxRequestException("You reached the maximum of request on Github's API, or the authentification step failled");
		}
		/* II. Exécution de la requête et incrémentation du compteur de requête */
		refText = "https://api.github.com/";
		isApiRequest = (request.substring(0,refText.length()).equals(refText));
		if(isApiRequest){
			authentifiedRequest = request+"?access_token="+accesTokens.get(accountIndex);
			resquestCounter++;
		}else{
			authentifiedRequest = request;
		}
		gmethod = new GetMethod(authentifiedRequest);
		statusCode = client.executeMethod(gmethod);
		if (statusCode != HttpStatus.SC_OK) {
			throw new InvalideMethodUrlException(gmethod.getStatusText());
		}
		/*III. Vérifications/synchronisation des compteurs de l'application avec les réponses de la requête */
		if(isApiRequest){
			maxRequestExpected = Integer.parseInt(gmethod.getResponseHeader("X-RateLimit-Limit").getValue());
			if(maxRequest != maxRequestExpected){
				//System.err.println("The limitation of request is equals to "+maxRequest+" whereas it should be equals to "+maxRequestExpected+" a correction will be done.");
				maxRequest = maxRequestExpected;
			}
			resquestCounterExpected = maxRequest - Integer.parseInt(gmethod.getResponseHeader("X-RateLimit-Remaining").getValue());
			if(resquestCounter != resquestCounterExpected){
				//System.err.println("The request counter is equals to "+resquestCounter+" whereas it should be equals to "+resquestCounterExpected+" a correction will be done.");
				resquestCounter = resquestCounterExpected;
			}
			System.out.println("Github resources state : "+resquestCounter+"/"+maxRequest);
		}
		return gmethod;
	}
}
