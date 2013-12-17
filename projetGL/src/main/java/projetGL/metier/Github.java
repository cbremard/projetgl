package projetGL.metier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import progetGL.exceptions.InvalideMethodUrlException;
import progetGL.exceptions.MaxRequestException;
import progetGL.exceptions.OldVersionNotFoundException;
import projetGL.controller.Controller;

public class Github extends Api{
	
//	public static void main( String[] args ){
//		Github git = Github.getInstance();
//		String user, repository, url;
//		user = "cbremard";
//		repository = "projetGL";
//		url = "https://api.github.com/repos/"+user+"/"+repository+"/events";
//		System.out.println("Start !!!");
//		try {
//			git.sendMultiPagesRequest(url);
//		} catch (HttpException e) {
//			System.err.println("Erreur dans la classe maître.");
//			e.printStackTrace();
//		} catch (InvalideMethodUrlException e) {
//			System.err.println("Erreur dans la classe maître.");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.err.println("Erreur dans la classe maître.");
//			e.printStackTrace();
//		} catch (MaxRequestException e) {
//			System.err.println("Erreur dans la classe maître.");
//			e.printStackTrace();
//		}
//		System.out.println("End !!!");
//	}
	
	
	
	
	
	
	
	private static Github uniqueGithub = null;
	private static final int nbOfSavedCommit = 3;
	float coeff;


	/**
	 * Constructeur privé de la classe Github pour obliger l'usage de la méthode getInstance
	 * @author BREMARD Corentin
	 */
	private Github() {
		super();
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
	 * getScore() lance un appel pour le calcul du score.
	 * Cette méthode doit être appellé pour le calcul du score.
	 * Les différents cas d'utilisation sont gérés via le paramètre state.
	 * @author BREMARD Corentin
	 * @return result: le score généré par la méthode
	 */
	@Override
	public float getScore() {
		float result = 0;
		try {
			result = state.compute(this);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("The score will have a value of 0.");
		}
		if(score>0){
			state = new StateSuccess();
		}else{
			state = new StateFailure();
		}
		return result;
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
		JSONArray jsonTemp1 = new JSONArray();
		JSONArray jsonTemp2 = new JSONArray();
		String temporaryStr = "";
		boolean oldVersionFound = false;
		/* I. Récupération de tous les événnements liés au répertoire donné */
		try {
			temporaryStr = sendMultiPagesRequest("https://api.github.com/repos/"+user+"/"+repository+"/events");
			jsonTemp1 = new JSONArray(temporaryStr);
		} catch (JSONException e) {
			System.err.println("JSONArray parsing error : " + temporaryStr);
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Unexpected result with "+user+"'s repository ("+repository+") :"+e.getMessage());
		}
		/* II. Sélection des commits uniquement */
		for (int i = 0; i < jsonTemp1.length(); i++) {
			try {
				if(jsonTemp1.getJSONObject(i).getString("type").equals("PushEvent")){
					temporaryStr = "{";
					temporaryStr += "\"head\":\"" + jsonTemp1.getJSONObject(i).getJSONObject("payload").getString("head") +"\",";
					temporaryStr += "\"before\":\"" + jsonTemp1.getJSONObject(i).getJSONObject("payload").getString("before") +"\"}";
					jsonTemp2.put(new JSONObject(temporaryStr));
				}
			} catch (JSONException e) {
				System.err.println("One error appened during creation of jsonTemp2.");
				e.printStackTrace();
			}
		}
		//The commit jsonTemp2.getJSONObject(i) is after the commit jsonTemp2.getJSONObject(i+1) 
		//So jsonTemp2.getJSONObject(i).getString("before") == jsonTemp2.getJSONObject(i+1).getString("head") 

		/* III. Sélection des nbOfSavedCommit commits suivant le changement de version */
		//Start at 1 because the newer commit (index==0) have the new librarie
		for (int i = 0+1; i < jsonTemp2.length(); i++) {
			try {
				temporaryStr ="https://raw.github.com/"
						+ user+"/"
						+ repository+"/"
						+ jsonTemp2.getJSONObject(i).getString("head")+"/"
						+ repository+"/pom.xml";
				temporaryStr = sendRequest(temporaryStr).getResponseBodyAsString();
				temporaryStr.replaceAll(" ", "");
				if(temporaryStr.contains(Controller.getOldVersion()) && temporaryStr.contains(Controller.getLibrairie())){
					oldVersionFound = true;
					temporaryStr = "{\"commitAt_t"+-1+"\":\""+jsonTemp2.getJSONObject(i).getString("before")+"\"";
					temporaryStr += ",\"commitAt_t"+0+"\":\""+jsonTemp2.getJSONObject(i).getString("head")+"\"";
					for (int j = 1; j < nbOfSavedCommit; j++) {
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
		if(!oldVersionFound){
			throw new OldVersionNotFoundException(user+"'s repository ("+repository+") don't use the old version.");
		}
		return jsonsResult;
	}

	/**
	 * Retourne l'utilisateur présent dans une URL github
	 * @author BREMARD Corentin
	 * @param url: l'URL à analyser
	 * @return Le speudo de l'utilisateur recherché si l'URL est correcte. Lève une erreur sinon.
	 * @throws InvalideMethodUrlException 
	 */
	private String getUser(String url) throws InvalideMethodUrlException {
		//TODO test bad urls
		int begin, end;
		String refText="https://github.com/";
		begin = url.indexOf(refText);
		if(begin<0){
			throw new InvalideMethodUrlException("Impossible to extract an user from "+url);
		}else{
			begin += refText.length();
			end = url.substring(begin).indexOf("/");
			if (end<0){
				throw new InvalideMethodUrlException("Impossible to extract an user from "+url);
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
	 * @return Le nom du répertoire de l'utilisateur recherché si l'URL est correcte. Lève une erreur sinon.
	 * @throws InvalideMethodUrlException 
	 */
	private String getRepo(String url, String user) throws InvalideMethodUrlException {
		int begin, end;
		String refText="https://github.com/" + user + "/";
		begin = url.indexOf(refText);
		if(begin<0){
			throw new InvalideMethodUrlException("Impossible to extract the "+user+"'s repository from "+url);
		}else{    			
			begin += refText.length();
			end = url.substring(begin).indexOf("/");
			if(end<0){
				throw new InvalideMethodUrlException("Impossible to extract the "+user+"'s repository from "+url);
			}else{
				end += begin;
			}
		}
		return url.substring(begin, end);
	}
	
	/**
	 * Permet d'obtenir la taille en octets d'un répertoire d'un utilisateur.
	 * @author BREMARD Corentin
	 * @param user: le propriétaire du projet
	 * @param repo: le répertoire du projet
	 * @return la taille en octets du projet si tout se passe bien. Lève une exception sinon.
	 * @throws Exception
	 */
	private int GetProjectSize(String user, String repo) throws Exception{
		int size=0;
		URL url;
		URLConnection connection = null;
		try {
			url = new URL("https://github.com/"+user+
					"/"+repo+"/archive/master.zip");
			connection = url.openConnection();
			size = connection.getContentLength();
			if (size < 0){
				throw new Exception("Could not determine projet size for "+user+"'s repository.");
			}
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}finally{
			if(connection != null){
				connection.getInputStream().close();
			}
		}
		return size;
	}
	
	/**
	 * Les requêtes github qui retourne plusieurs objects sont limités à 30 objets par réponses par défaut. Cette méthode permet de récupérer l'ensemble des résultats.
	 * Cette méthode surcharge la méthode mère sendRequest(String request)
	 * @author BREMARD corentin
	 * @param request : l'url générale qui permet d'affiche la première page de résultats
	 * @throws MaxRequestException 
	 * @throws IOException 
	 * @throws InvalideMethodUrlException 
	 * @throws HttpException 
	 * @return result : un JSONArray sous format string
	 */
	private String sendMultiPagesRequest(String request) throws HttpException, InvalideMethodUrlException, IOException, MaxRequestException{
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
	 * Méthode maître de la classe Github. C'est elle qui calculer le score demandé.
	 * Cette méthode est sensé être appellée uniquement par la classe stateReady.
	 * La moindre erreur est gérer de façon à continuer le calculs sur les données restantes. Si aucun calcul n'a aboutie, la méthode retourne un score de 0.
	 * l'éxécution se fait en 5 étapes:
	 *    I. Initiation des variables
	 *    II. Récupération des utilisateurs et répertoires via une recherche Google
	 *    III. Suppression des couples user/repo en double
	 *    IV. Récupération des commits
	 *    V. Récupération de la taille des commits et construction du score final
	 * @author BREMARD Corentin
	 * @return le score du modèle
	 */
	public float compute() {
		/* I. Initiation des variables */
		String request, endURL, temp;
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<String> users = new ArrayList<String>();
		ArrayList<String> repos = new ArrayList<String>();
		JSONArray commits = new JSONArray();
		JSONArray informations = new JSONArray();
		JSONObject commitInformation  = new JSONObject();
		JSONObject commit  = new JSONObject();
		int index, scoreTemp, projectSize;
		float score;
		GoogleSearch gs = new GoogleSearch();

		// TODO Change next line
		request = "https://www.google.fr/search?client=ubuntu"
				+ "&channel=fs"
				+ "&q=%22"+Controller.getLibrairie()+"%22+%22"+Controller.getNewVersion()+"%22+site:github.com"
				+ "&ie=utf-8"
				+ "&oe=utf-8"
				+ "&gws_rd=cr"
				+ "&ei=UwyeUva_KuvY7AaK04CICg";
		endURL = "pom.xml";

		/* II. Récupération des utilisateurs et répertoires via une recherche Google */
		//		urls = gs.getUrlResult(request,endURL);
		//		for (String url : urls) {
		//				temp = getUser(url);
		//				users.add(temp);
		//				repos.add(getRepo(url,temp));
		//		}

		users.add("cbremard");
		repos.add("projetGL");

		/* III. Suppression des couples user/repo en double */
		index=1;
		while(index < users.size()) {
			if(users.get(index-1)==users.get(index) && repos.get(index-1)==repos.get(index)){
				users.remove(index);
				repos.remove(index);
			}else{
				index++;
			}
		}	

		/* IV. Récupération des commits */
		for (int i = 0; i < users.size(); i++) {
			try {
				commits.put(getCommit(users.get(i), repos.get(i)));
			} catch (OldVersionNotFoundException e) {
				System.err.println(e.getMessage());
			}
		}
		
		/* V. Récupération de la taille des commits et construction du score final */
		score=0;
		// Loop on all projets found
		for (int j = 0; j < commits.length(); j++) {
			try {
				commit = commits.getJSONObject(j);
				scoreTemp = 0;
				// Loop on each commit of the given project
				for (int k = -1; k < nbOfSavedCommit-1; k++) {
					commitInformation = new JSONObject(sendRequest("https://api.github.com/repos/"+
							commit.getString("user")+
							"/"+commit.getString("repo")+
							"/compare/"+commit.getString("commitAt_t"+k)+
							"..."+commit.getString("commitAt_t"+(k+1))+"").getResponseBodyAsString());
					informations = commitInformation.getJSONArray("files");
					// Other loop because sometime, you have more than one commit between two given SHA
					for (int l = 0; l < informations.length(); l++) {
						scoreTemp += informations.getJSONObject(l).getInt("changes");
					}
				}
				// divide scoreTemp by the project's size in order to have the percentage of number of modified lines	
				projectSize = GetProjectSize(commit.getString("user"),commit.getString("repo"));
				if(projectSize >0){
					// In average, a line is 35 octets (it's the case for this document)
					score += (float) 35*scoreTemp/projectSize;
				}
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		// And divide the final score by the number of projects found in order to have the mean.
		if(commits.length()>0){
			score = (float) score /commits.length();
		}else{
			score = 0;
		}
		return score;
	}
}