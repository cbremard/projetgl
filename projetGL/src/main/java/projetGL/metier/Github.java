package projetGL.metier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import projetGL.controller.Controller;
import projetGL.exceptions.IdentificationFailledException;
import projetGL.exceptions.InvalideMethodUrlException;
import projetGL.exceptions.MaxRequestException;
import projetGL.exceptions.OldVersionNotFoundException;

public class Github extends Api{
	private static Github uniqueGithub = null;
	private static final int nbOfAnalysedCommits = 3; // Nombre de commits à analyser pour trouver le nombre de lignes modifiées suite à un changement de version
	private float coeff;
	private float score;


	/**
	 * Constructeur privé de la classe Github pour obliger l'usage de la méthode getInstance
	 * @author BREMARD Corentin
	 */
	private Github() {
		super();
		this.score = 0;
	}

	/**
	 * Par respect du pattern singleton, getInstance assure l'usage d'une unique instance de la classe Github
	 * @author BREMARD Corentin
	 * @return uniqueGithub: une unique instance Github
	 */
	public static Github getInstance(){
		if (uniqueGithub==null){
			uniqueGithub = new Github();
		}
		return uniqueGithub;
	}

	/**
	 * @return le score de l'instance Github
	 */
	@Override
	public float getScore() {
		return Github.getInstance().score;
	}

	/**
	 * Méthode set pour modifier la valeur Score de l'instance Github
	 * @param _score
	 */
	public void setScore(float _score){
		Github.getInstance().score = _score;
	}

	/**
	 * calcul_score() lance un appel à compute() pour le calcul du score.
	 * Les différents cas d'utilisation sont gérés via le paramètre state.
	 * @author BREMARD Corentin
	 * @return result: le score généré par la méthode
	 */
	public float calcul_score() {
		try {
			state.compute(this);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("The score will have a value of 0.");
			e.printStackTrace();
		}
		if(GoogleSearch.getInstance().getScore()>0){
			state = new StateSuccess();
		}else{
			state = new StateFailure();
		}
		return GoogleSearch.getInstance().getScore();
	}

	/**
	 * Récupération des nbOfSavedCommit commits suivant le changment de version pour un utilisateur et un répertoire donné.
	 * Méthode en 3 étapes:
	 *    I. Récupération de tous les événnements liés au répertoire donné
	 *    II. Sélection des commits uniquement
	 *    III. Sélection des nbOfSavedCommit commits suivant le changement de version
	 * @author BREMARD Corentin
	 * @return Un JSON vide si le project en question n'a pas effectuer le changement de version désiré. Sinon, la méthode retourne un Json avec les paramètres "user" (le propriétaire du projet), "repo" (le répertoire du projet), "commitAt_t-1" (numéro SHA du project juste avant le changement de version), "commitAt_t0" (numéro SHA du project lors du changement de version) et les "comitAt_ti" (les numéros SHA du project aux instants t+i suivant le changement dez version.
	 * @throws OldVersionNotFoundException 
	 */
	@Override
	protected JSONObject getCommit(String user, String repository) throws OldVersionNotFoundException {
		JSONObject jsonsResult = new JSONObject();
		JSONArray jsonTemp1 = new JSONArray(); // Tableau de Json
		JSONArray jsonTemp2 = new JSONArray();
		String temporaryStr = "";
		boolean haveBeforeParam, oldVersionFound = false;

		/* I. Récupération de tous les évènements liés au répertoire donné */
		try {
			temporaryStr = sendMultiPagesRequest("https://api.github.com/repos/"+user+"/"+repository+"/events");
			jsonTemp1 = new JSONArray(temporaryStr);
		} catch (JSONException e) {
			System.err.println("JSONArray parsing error : " + temporaryStr);
		} catch (Exception e) {
			System.err.println("Unexpected result with "+user+"'s repository ("+repository+") : " + e.getMessage());
		}

		/* II. Sélection des commits uniquement */
		for (int i = 0; i < jsonTemp1.length(); i++) {
			haveBeforeParam = false;
			try {
				if(jsonTemp1.getJSONObject(i).getString("type").equals("PushEvent")){
					temporaryStr = "{";
					temporaryStr += "\"head\":\"" + jsonTemp1.getJSONObject(i).getJSONObject("payload").getString("head") +"\",";
					temporaryStr += "\"before\":\"" + jsonTemp1.getJSONObject(i).getJSONObject("payload").getString("before") +"\"}";
					haveBeforeParam = true;
					jsonTemp2.put(new JSONObject(temporaryStr));
				}
			} catch (JSONException e) {
				if(haveBeforeParam){
					System.err.println("One error appened when trying to parse a String to JSON : "+e);
				}
			}
		}
		//The commit jsonTemp2.getJSONObject(i) is after the commit jsonTemp2.getJSONObject(i+1) 
		//So jsonTemp2.getJSONObject(i).getString("before") == jsonTemp2.getJSONObject(i+1).getString("head") 

		/* III. Sélection des nbOfSavedCommit commits suivant le changement de version */
		//Start at 1 because the newer commit (index==0) have the new librarie
		for (int i = 0+1; i < jsonTemp2.length(); i++) {
			try {
				// Adresse à requêter pour trouver le pom.xml de la version cherchée
				temporaryStr ="https://raw.github.com/"
						+ user+"/"
						+ repository+"/"
						+ jsonTemp2.getJSONObject(i).getString("head")+"/"
						+ repository+"/pom.xml";
				temporaryStr = sendRequest(temporaryStr).getResponseBodyAsString();
				temporaryStr.replaceAll(" ", "");
				if(temporaryStr.contains("<version>"+Controller.getOldVersion()+"</version>")
						&& temporaryStr.contains("<artifactId>"+Controller.getArtefactId()+"</artifactId>") 
						&& temporaryStr.contains("<groupId>" +Controller.getGroupId()+"</groupId>")
						){
					oldVersionFound = true;
					temporaryStr = "{\"commitAt_t"+-1+"\":\""+jsonTemp2.getJSONObject(i).getString("before")+"\"";
					temporaryStr += ",\"commitAt_t"+0+"\":\""+jsonTemp2.getJSONObject(i).getString("head")+"\"";
					for (int j = 1; j < nbOfAnalysedCommits; j++) {
						temporaryStr += ",\"commitAt_t"+j+"\":\"";
						if(i-j>=0){
							temporaryStr += jsonTemp2.getJSONObject(i-j).getString("head");
						}
						temporaryStr += "\"";
					}
					temporaryStr += ",\"user\":\""+user+"\"";
					temporaryStr += ",\"repo\":\""+repository+"\"";
					temporaryStr += "}";
					jsonsResult = new JSONObject(temporaryStr);
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err.print("Unexpected result with URL "+temporaryStr +" : ");
				System.err.println(e.getMessage());
			}
		}

		// Si aucune des versions du projet ne contient la "OldVersion" de la librairie recherchée
		if(!oldVersionFound){
			throw new OldVersionNotFoundException(user+"'s repository ("+repository+") don't use the old version.");
		}
		return jsonsResult;
	}

	/**
	 * Retourne l'utilisateur présent dans une URL github
	 * @author BREMARD Corentin
	 * @param url: l'URL à analyser
	 * @return Le pseudo de l'utilisateur recherché si l'URL est correcte. Lève une erreur sinon.
	 * @throws InvalideMethodUrlException 
	 */
	protected String getUser(String url) throws InvalideMethodUrlException {
		int begin, end;
		String refText="https://github.com/";
		begin = url.indexOf(refText);
		if(begin<0){
			throw new InvalideMethodUrlException("Impossible to extract user from "+url);
		}else{
			begin += refText.length();
			end = url.substring(begin).indexOf("/");
			if (end<0){
				throw new InvalideMethodUrlException("Impossible to extract user from "+url);
			}else{
				end += begin;
			}
		}
		return url.substring(begin, end);
	}

	/**
	 * Retourne le répertoire présent dans une URL github pour un utilisateur donné
	 * @author BREMARD Corentin
	 * @param url: l'URL à analyser
	 * @param user : le propriétaire du repository recherché
	 * @return Le nom du répertoire recherché si l'URL est correcte. Lève une erreur sinon.
	 * @throws InvalideMethodUrlException 
	 */
	protected String getRepo(String url, String user) throws InvalideMethodUrlException {
		int begin, end;
		String refText="https://github.com/" + user + "/";
		begin = url.indexOf(refText);
		if(begin<0){
			throw new InvalideMethodUrlException("Impossible to extract "+user+"'s repository from "+url);
		}else{    			
			begin += refText.length();
			end = url.substring(begin).indexOf("/");
			if(end<0){
				throw new InvalideMethodUrlException("Impossible to extract "+user+"'s repository from "+url);
			}else{
				end += begin;
			}
		}
		return url.substring(begin, end);
	}

	/**
	 * Permet d'obtenir la taille en octets d'un répertoire pour un utilisateur.
	 * @author BREMARD Corentin
	 * @param user: le propriétaire du projet
	 * @param repo: le répertoire du projet
	 * @return la taille en octets du projet si tout se passe bien. Lève une exception sinon.
	 * @throws IOException
	 * @throws HttpException
	 */
	protected int GetProjectSize(String user, String repo) throws IOException, HttpException{
		int size=0;
		//		URL url;
		//		URLConnection connection = null;
		JSONObject project;
		try {
			project = new JSONObject(sendRequest("https://api.github.com/repos/"+user+"/"+repo).getResponseBodyAsString());
			System.out.println(project.toString());
			try {
				size = project.getInt("size"); // TODO : À CORRIGER !!! ÇA FONCTIONNE PAS :-(
				System.out.println("Size : " + size);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		} catch (InvalideMethodUrlException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (MaxRequestException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}catch (JSONException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		//			try {
		//			url = new URL("https://github.com/"+user+
		//					"/"+repo+"/archive/master.zip");
		//			connection = url.openConnection();
		//			size = connection.getContentLength();
		//			if (size < 0){
		//				throw new HttpException("Impossible to determine projet size for "+user+"'s repository.");
		//			}
		//		} catch (IOException e) {
		//			throw e;
		//		}
		//		finally{
		//			if(connection != null){
		//				connection.getInputStream().close();
		//			}
		//		}
		return size;
	}

	/**
	 * Les requêtes github qui retournent plusieurs objects sont limitées par défaut, à 30 objets par réponse.
	 * Cette méthode permet de récupérer l'ensemble des résultats.
	 * Elle surcharge la méthode mère sendRequest(String request)
	 * @author BREMARD corentin
	 * @param request : l'url générale qui permet d'afficher la première page de résultats
	 * @throws MaxRequestException 
	 * @throws IOException 
	 * @throws InvalideMethodUrlException 
	 * @throws HttpException 
	 * @return result : un JSONArray sous format string
	 */
	protected String sendMultiPagesRequest(String request) throws HttpException, InvalideMethodUrlException, IOException, MaxRequestException{
		String finalResult, UriNextPage, linkResponse, bodyResponse;
		GetMethod gmethod;

		finalResult = "[";
		UriNextPage = request;
		while(UriNextPage != null && UriNextPage.length()>0){
			gmethod = sendRequest(UriNextPage);
			bodyResponse = gmethod.getResponseBodyAsString();
			if(bodyResponse.length()>3){
				finalResult += bodyResponse.substring(1, bodyResponse.length()-1)+",";
				linkResponse = gmethod.getResponseHeader("Link").getValue();
				UriNextPage = linkResponse.subSequence(linkResponse.indexOf("<")+1, linkResponse.indexOf(">")).toString();
			}else{
				UriNextPage = "";
			}
		}
		finalResult = finalResult.substring(0, finalResult.length()-1) + "]";
		return finalResult;
	}

	/**
	 * Gestion de l'authentification de l'application auprès de github
	 * @param accountIndex : l'index du compte à utiliser. Si un compte est épuisé, l'idée est de basculer sur le second.
	 * @author BREMARD Corentin
	 * @return succes : vaut true si tout s'est bien passé
	 * @throws IdentificationFailledException
	 */
	@Override
	protected boolean authentification(int accountIndex) throws IdentificationFailledException {
		boolean succes = false;
		String query = "https://api.github.com/user?access_token=";
		HttpClient client = new HttpClient();
		GetMethod gmethod;
		int statusCode;
		if(accountIndex>=0 && accountIndex< accesTokens.size()){
			try {
				gmethod = new GetMethod(query + accesTokens.get(accountIndex));
				statusCode = client.executeMethod(gmethod);
				if (statusCode != HttpStatus.SC_OK) {
					throw new IdentificationFailledException("Connection failled, try later");
				}else{
					maxRequest = Integer.parseInt(gmethod.getResponseHeader("X-RateLimit-Limit").getValue());
					resquestCounter = maxRequest-Integer.parseInt(gmethod.getResponseHeader("X-RateLimit-Remaining").getValue());
					succes = true;
				}
			} catch (HttpException e) {
				throw new IdentificationFailledException("Cannot acces to webservice : " + e);
			} catch (IOException e) {
				throw new IdentificationFailledException("Transaction failled : " + e);
			} catch (NumberFormatException e){
				throw new IdentificationFailledException("Unexpected result : " + e);
			}
		}
		return succes;
	}

	/**
	 * Méthode maître de la classe Github. C'est elle qui calcule le score demandé.
	 * Cette méthode est censée être appellée uniquement par la classe stateReady.
	 * La moindre erreur est gérée de façon à continuer le calcul sur les données restantes.
	 * Si aucun calcul n'a abouti, la méthode retourne un score de 0.
	 * L'exécution se fait en 5 étapes:
	 *    I. Initialisation des variables
	 *    II. Récupération des utilisateurs et répertoires via une recherche Google
	 *    III. Suppression des couples user/repo en double
	 *    IV. Récupération des commits
	 *    V. Récupération de la taille des commits et construction du score final
	 * @author BREMARD Corentin
	 */
	public void compute() {
		state = new StateRunning();

		/* I. Initialisation des variables */
		String request, endURL, user, repo;
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<GithubProject> projects = new ArrayList<GithubProject>();
		GithubProject project;
		JSONObject detail_commit = new JSONObject();

		TextMining tMining;
		ArrayList<Pair_String> users_repos = new ArrayList<Pair_String>();
		JSONArray commit_infos = new JSONArray();
		JSONObject compare_commits  = new JSONObject();
		int index;
		Pair_String user_repo;
		float lineWeight;
		GoogleSearch gs = GoogleSearch.getInstance();

		lineWeight = 5609931/2176; // TODO : what's this ?
		// TODO Change next line
		request = "https://www.google.fr/search?client=ubuntu"
				+ "&channel=fs"
				+ "&q=%22<groupId>" +Controller.getGroupId()+"</groupId>"
				+ "%22+%22<artifactId>"+Controller.getArtefactId()+"</artifactId>"
				+ "%22+%22<version>"+Controller.getNewVersion()+"</version>"
				+ "%22+site:github.com"
				+ "&ie=utf-8"
				+ "&oe=utf-8"
				+ "&gws_rd=cr"
				+ "&ei=UwyeUva_KuvY7AaK04CICg";
		endURL = "pom.xml";

		/* II. Récupération des utilisateurs et répertoires via une recherche Google */
		//		urls = gs.getUrlResult(request,endURL);
		//		urls.add("/url?q=https://github.com/excilys-blemale/projet-test-jenkins/blob/master/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CCgQFjAB&usg=AFQjCNHq7BRpDuU7pkkMpz7RvOziAEX08w");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:c3D7xLADygcJ:https://github.com/excilys-blemale/projet-test-jenkins/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CCsQIDAB&usg=AFQjCNHLUJ5QLu95jT6aLpAjvxUlb6d2og");
		//		urls.add("/url?q=https://github.com/jbourcie/projet-musee/blob/master/aapweb/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CC0QFjAC&usg=AFQjCNHoZLLKtuutPbal6KX0OmmomYRapw");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:5ww8N8D1D70J:https://github.com/jbourcie/projet-musee/blob/master/aapweb/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CDAQIDAC&usg=AFQjCNGz1yEMfNLwDRE9d2QubTP_b8KZIA");
		//		urls.add("/url?q=https://github.com/aktos/projet/blob/master/projet/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CEgQFjAH&usg=AFQjCNGnM5q2CdPHq_fVxYnjnzqVOKMrVQ");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:sGYrjHJKYRMJ:https://github.com/aktos/projet/blob/master/projet/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CEsQIDAH&usg=AFQjCNERbBtY2d8DLD2dP1jW8Ad0DebFtA");
		//		urls.add("/url?q=https://github.com/abois/mywebapp/blob/master/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CE0QFjAI&usg=AFQjCNG36i9_EqydOK2MFujYNgUYt4AWHw");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:znwfzAr9m54J:https://github.com/abois/mywebapp/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CFAQIDAI&usg=AFQjCNGBwmSq6amYHhcK74f1vQvkpMNuYg");
		//		urls.add("/url?q=https://github.com/divarvel/TA-Melog/blob/master/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CCMQFjAAOAo&usg=AFQjCNGUekbiGWAz3Bhwxyqc7tdNMkl_IA");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:ugN72Njg4MQJ:https://github.com/divarvel/TA-Melog/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CCYQIDAAOAo&usg=AFQjCNHtKmCY_ucXv7CmYR5pY6Imp3K1Iw");
		//		urls.add("/url?q=https://github.com/Pasquet/projet-15min/blob/master/projet-15-webapp/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CC0QFjACOAo&usg=AFQjCNHrDPt4KhDzJuhonhxbRJO5U_8KeA");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:0AY_sfQOD80J:https://github.com/Pasquet/projet-15min/blob/master/projet-15-webapp/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CDAQIDACOAo&usg=AFQjCNEdjBVZRuKmTxSbN2nu9StvTHF3Ag");
		//		urls.add("/url?q=https://github.com/trich/tiw5-2011-tp2/blob/master/projet/modele/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CEYQFjAHOAo&usg=AFQjCNF1O6vxYk-DcMKGbWUfBsBGVCclvQ");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:xl6BQbLFxDUJ:https://github.com/trich/tiw5-2011-tp2/blob/master/projet/modele/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CEkQIDAHOAo&usg=AFQjCNEM9MkLqryx2FyfdrV4WZz3mI8-7w");
		//		urls.add("/url?q=https://github.com/sunye/AlmaGTD/blob/master/GTDClientKrom/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCMQFjAAOBQ&usg=AFQjCNE6_iokobDiiL84vHLkIcu-E4H18A");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:TGtHVX_GFuEJ:https://github.com/sunye/AlmaGTD/blob/master/GTDClientKrom/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCYQIDAAOBQ&usg=AFQjCNFh2jb13Fyiru4K4K92SWvAb_Mlrg");
		//		urls.add("/url?q=https://github.com/sunye/AlmaGTD/blob/master/GTDWebClient/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCgQFjABOBQ&usg=AFQjCNFsF5k9N8SoNuJfWPSx0r1ygk_fTg");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:eKNE2-pnRXAJ:https://github.com/sunye/AlmaGTD/blob/master/GTDWebClient/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCsQIDABOBQ&usg=AFQjCNEP6HnVdEwd-LsaKNbS0gUI5Pkfdw");
		//		urls.add("/url?q=https://github.com/trich/tiw5-2011-tp2/blob/master/projet/web-interface/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CC0QFjACOBQ&usg=AFQjCNE6iikCLQk5gMbtFSNDZxmo6pXRkQ");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:7OnQeUY7rgMJ:https://github.com/trich/tiw5-2011-tp2/blob/master/projet/web-interface/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDAQIDACOBQ&usg=AFQjCNE0zBKxtr2VEOGL57-Hhkru2ICcpA");
		//		urls.add("/url?q=https://github.com/ChristelleLacan/QuizZer/blob/master/QuizZer/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDgQFjAEOBQ&usg=AFQjCNFijrGvKrNW-MV9ON9HZk6s78lGiQ");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:aJufrzJh028J:https://github.com/ChristelleLacan/QuizZer/blob/master/QuizZer/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDsQIDAEOBQ&usg=AFQjCNHI8LgYMZS4PNVBW5OfsCMZs_wzkQ");
		//		urls.add("/url?q=https://github.com/pthurotte/testDevCloud/blob/master/appSuiviExploit-webapp/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CEgQFjAHOBQ&usg=AFQjCNEfNr0fhMxzCqMBN2H5yC-IAVU7xA");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:D2_mfqBYiqwJ:https://github.com/pthurotte/testDevCloud/blob/master/appSuiviExploit-webapp/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CEsQIDAHOBQ&usg=AFQjCNFnGuSOd9rgqxyNjG26RTlKMGlHUw");
		//		urls.add("/url?q=https://github.com/Pasquet/projet-15min/blob/master/projet15-functional-tests/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CE0QFjAIOBQ&usg=AFQjCNFNDRAKdBX-GzMOiXiQ-l4Xc8rZkg");
		//		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:qOoRxkVJQogJ:https://github.com/Pasquet/projet-15min/blob/master/projet15-functional-tests/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CFAQIDAIOBQ&usg=AFQjCNH5vIOzRUGWCVdOwaI9rkVaInDnZA");
		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:qOoRxkVJQogJ:https://github.com/cbremard/projetGL/blob/master/projet15-functional-tests/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CFAQIDAIOBQ&usg=AFQjCNH5vIOzRUGWCVdOwaI9rkVaInDnZA");

		/* Récupération des couples user-repo */
		for (String url : urls) {
			try {
				user = getUser(url);
				repo = getRepo(url,user);
				user_repo = new Pair_String(user, repo);
				users_repos.add(user_repo);	// Liste des couples user-repository pour obtenir chaque projet
			} catch (InvalideMethodUrlException e) {
				System.err.println(e.getMessage());
			}
		}


		/* III. Suppression des couples user-repo en double */
		Collections.sort(users_repos, new PairComparator());
		index=1;
		while(index < users_repos.size()) {
			if(users_repos.get(index-1).equals(users_repos.get(index))){
				users_repos.remove(index);
			}else{
				index++;
			}
		}	


		/* IV. Récupération des commits */
		// Boucle sur chaque couple user-repository
		for (Pair_String pair_user_repo : users_repos) {
			try {
				detail_commit = getCommit(pair_user_repo.getLeft(), pair_user_repo.getRight());
				project = new GithubProject(pair_user_repo.getLeft(), pair_user_repo.getRight(), detail_commit);
				projects.add(project);
			} catch (OldVersionNotFoundException e) {
				System.err.println(e.getMessage());
			}
		}


		/* V. Récupération de la taille des commits et construction du score final */
		// Boucle sur chaque projet
		for (GithubProject proj : projects) {
			try {
				// Boucle sur chacun des 3 (=nbOfAnalysedCommits) commits que l'on veut analyser
				for (int k = -1; k < nbOfAnalysedCommits-1; k++) {
					compare_commits = new JSONObject(sendRequest("https://api.github.com/repos/"+
							proj.getUser()+"/"+proj.getRepo()+"/compare/"
							+proj.getDetail_commits().getString("commitAt_t"+k)+"..."
							+proj.getDetail_commits().getString("commitAt_t"+(k+1))+"").getResponseBodyAsString());
					// Affichage requête
					System.out.println("https://api.github.com/repos/"+
							proj.getUser()+"/"+proj.getRepo()+"/compare/"
							+proj.getDetail_commits().getString("commitAt_t"+k)+"..."
							+proj.getDetail_commits().getString("commitAt_t"+(k+1))+"");

					commit_infos = compare_commits.getJSONArray("commits");


					// Sauvegarde des commentaires associés à chaque commit
					for (int l = 0; l < commit_infos.length(); l++) {
						proj.setComments(commit_infos.getJSONObject(l).getJSONObject("commit").getString("message"));
					}

					commit_infos = compare_commits.getJSONArray("files");
					// Other loop because sometime, you have more than one commit between two given SHA
					for (int l = 0; l < commit_infos.length(); l++) {
						proj.setModified_lines(commit_infos.getJSONObject(l).getInt("changes"));
					}
				}

				// divide Modified_lines by the project's size in order to have the percentage of modified lines
				try{
					proj.setOctet_size(GetProjectSize(proj.getUser(),proj.getRepo()));
				} catch (Exception e) {
					// Une seconde fois car souvent la première plante ;)
					proj.setOctet_size(GetProjectSize(proj.getUser(),proj.getRepo()));
				}
				if(proj.getOctet_size() >0){
					// In average, a line is 35 octets (it's the case for this document)
					proj.setScore(lineWeight*proj.getModified_lines()/proj.getOctet_size());
				}
			} catch (JSONException e) {
				System.err.println(e.getMessage()+" ("+e+")");
			} catch (HttpException e) {
				System.err.println(e.getMessage());
			} catch (IOException e) {
				System.err.println("Connection faillure : "+e);
			} catch (InvalideMethodUrlException e) {
				System.err.println(e.getMessage());
			} catch (MaxRequestException e) {
				System.err.println(e.getMessage());
			}
		}

		// Intégration de l'analyse des commentaires associés aux commits
		if (projects.size()>0) {
			tMining = new TextMining();
			for (GithubProject proj : projects) {
				tMining.indexComments(proj.getComments(), proj.getUser(), proj.getRepo());
			}
			projects = tMining.analyseComments(projects);
			// TODO : intégrer le score de TextMining au score total (+1 pour éviter les score*0,... ?)
		}

		// Calcul du score total de la méthode Github
		// Division du score total par le nombre de projets trouvés
		if(projects.size()>0){
			for (GithubProject proj : projects) {
				Github.getInstance().setScore(Github.getInstance().getScore() + proj.getScore());
			}
			Github.getInstance().setScore(Github.getInstance().getScore()/projects.size());
		}
	}

}