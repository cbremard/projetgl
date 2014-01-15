package projetGL.metier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import projetGL.controller.Controller;
import projetGL.exceptions.IdentificationFailledException;
import projetGL.exceptions.InvalideMethodUrlException;
import projetGL.exceptions.MaxRequestException;
import projetGL.exceptions.OldVersionNotFoundException;

public class Github extends Api{



	public static void main( String[] args ) throws OldVersionNotFoundException, HttpException, IOException, InvalideMethodUrlException, MaxRequestException{
		Github git = Github.getInstance();
		git.getCommit("cbremard", "projetGL");
//		System.out.println(git.compareTwoVersion("3.8.1", "4.11"));
//		System.out.println(git.compareTwoVersion("4.11", "4.11"));
//		System.out.println(git.compareTwoVersion("4.11", "3.8.1"));
	}


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
		if(Github.getInstance().getScore()>0){
			state = new StateSuccess();
		}else{
			state = new StateFailure();
		}
		return Github.getInstance().getScore();
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
	 * @throws NullPointerException
	 * @throws FileNotFoundException 
	 */
	@Override
	protected JSONObject getCommit(String user, String repository) throws OldVersionNotFoundException, NullPointerException, FileNotFoundException {
		JSONObject jsonsResult = new JSONObject();
		JSONObject jsonTemp2 = new JSONObject();
		JSONArray jsonTemp3 = new JSONArray();
		String currentVersion, previousVersion, temporaryStr = "";
		ArrayList<String> list_sha = null;
		boolean reverse, finBoucle, haveBeforeParam, oldVersionFound = false;
		JSONObject jobj;
		String pom;
		int currentIndexSha, previousIndexSha, indexShaTemp;

		/* I. Récupération de tous les évènements liés au répertoire donné */

		try {
			list_sha = sendMultiPagesRequest("https://api.github.com/repos/"+user+"/"+repository+"/commits");
		} catch (HttpException e) {
			System.err.println("HttpException : "+e);
		} catch (InvalideMethodUrlException e) {
			System.err.println("Unexpected result with "+user+"'s repository ("+repository+") : "+e);
		} catch (IOException e) {
			System.err.println("IOException : "+e);
		} catch (MaxRequestException e) {
			System.err.println("MaxRequestException : "+e);
		}

		/* II. Sélection des nbOfSavedCommit commits suivant le changement de version */
		System.out.println("Recherche de l'ancienne version dans les pom.xml");
		//		indexSha = 0;
		String adress_tree=findPathPom(user, repository, list_sha.get(0));

		//TODO Corentin: penser à prendre en compte le cas où newVersion<oldVersion
		finBoucle = false;
		currentIndexSha = list_sha.size()-1;
		previousIndexSha = 0;
		previousVersion = Controller.getNewVersion();
		reverse = compareTwoVersion(Controller.getOldVersion(), Controller.getNewVersion())<0;
		while(!finBoucle){
			try {
System.out.println("Jump from "+ previousIndexSha+" to "+currentIndexSha+"(reverse = "+reverse+")");
				currentVersion = getLibraryVersion(user, repository, list_sha.get(currentIndexSha), adress_tree);
System.out.println("Version from "+previousVersion+" to "+currentVersion);
				indexShaTemp = currentIndexSha;
				if(compareTwoVersion(previousVersion,currentVersion)>0){
					if(!reverse){
						finBoucle=true;
					}else{
						currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					}
				}else if (compareTwoVersion(previousVersion,currentVersion)<0){
					if(!reverse){
						currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					}else{
						finBoucle=true;
					}
				}else{
					if(currentIndexSha>previousIndexSha){
						currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					}else if(currentIndexSha<previousIndexSha){
						currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					}else{
						finBoucle=true;
					}
				}
				previousIndexSha = indexShaTemp;
				previousVersion = currentVersion;
			} catch (HttpException e) {
				System.err.print("HttpException with URL "+temporaryStr +" : "+e.getMessage());
				if(currentIndexSha>previousIndexSha){currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else if(currentIndexSha<previousIndexSha){currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else{finBoucle=true;}
			} catch (IOException e) {
				System.err.println("IO with URL "+temporaryStr +" : "+e);
				if(currentIndexSha>previousIndexSha){currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else if(currentIndexSha<previousIndexSha){currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else{finBoucle=true;}
			} catch (InvalideMethodUrlException e) {
				System.err.println("UnexpectException with URL "+temporaryStr +" : "+e.getMessage());
				if(currentIndexSha>previousIndexSha){currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else if(currentIndexSha<previousIndexSha){currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else{finBoucle=true;}
			} catch (MaxRequestException e) {
				System.err.println(e);
				if(currentIndexSha>previousIndexSha){currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else if(currentIndexSha<previousIndexSha){currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);}
				else{finBoucle=true;}
			}
		}
		return null;

//		for (String sha : list_sha) {
//			try {
//				currentIndexSha++;
//				temporaryStr ="https://raw2.github.com/"
//						+ user+"/"
//						+ repository+"/"
//						+ sha;
//
//				temporaryStr += adress_tree + "/pom.xml";
//
//				temporaryStr = sendRequest(temporaryStr).getResponseBodyAsString();
//				temporaryStr.replaceAll(" ", "");
//
//				if(StringUtils.containsIgnoreCase(temporaryStr, "<version>"+Controller.getOldVersion()+"</version>")
//						&& StringUtils.containsIgnoreCase(temporaryStr, "<artifactId>"+Controller.getArtefactId()+"</artifactId>") 
//						&& StringUtils.containsIgnoreCase(temporaryStr, "<groupId>" +Controller.getGroupId()+"</groupId>")
//						){
//					oldVersionFound = true;
//					temporaryStr = "{\"user\":\""+user+"\"";
//					temporaryStr += ",\"repo\":\""+repository+"\"";
//					temporaryStr += ",\"commitOldVersion\":\""+sha+"\"";
//					for (int j = 1; j <= nbOfAnalysedCommits; j++) {
//						temporaryStr += ",\"commitAt_t"+j+"\":\"";
//						if(currentIndexSha-j>=0){
//							temporaryStr += list_sha.get(currentIndexSha-j);
//						}
//						temporaryStr += "\"";
//					}
//					temporaryStr += "}";
//					jsonsResult = new JSONObject(temporaryStr);
//					break;
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				System.err.print("Unexpected result with URL "+temporaryStr +" : ");
//				System.err.println(e.getMessage());
//			}
//		}	
//
//
//		// Si aucune des versions du projet ne contient la "OldVersion" de la librairie recherchée
//		if(!oldVersionFound){
//			throw new OldVersionNotFoundException(user+"'s repository ("+repository+") doesn't use the old version.");
//		}
//		return jsonsResult;
	}

	/**
	 * Compare 2 version of a library
	 * @param previousVersion
	 * @param currentVersion
	 * @return -1 if left version is the newer, 1 if it's the right version and 0 if it's the same version
	 */
	protected int compareTwoVersion(String previousVersion, String currentVersion) {
		int result = 0;
		String previousFiguresStr[], currentFiguresStr[];
		try{
			if(!previousVersion.equals(currentVersion)){
			previousFiguresStr = previousVersion.split("\\.");
			currentFiguresStr = currentVersion.split("\\.");
			for(int i=0; i<Math.min(previousFiguresStr.length, currentFiguresStr.length);i++){
				if(Integer.parseInt(previousFiguresStr[i])<Integer.parseInt(currentFiguresStr[i])){
					result=1;
					break;
				}else if(Integer.parseInt(previousFiguresStr[i])>Integer.parseInt(currentFiguresStr[i])){
					result=-1;
					break;
				}else if(i==(Math.min(previousFiguresStr.length, currentFiguresStr.length)-1)){
					if(previousFiguresStr.length<currentFiguresStr.length){
						result=1;
						break;
					}else if(previousFiguresStr.length>currentFiguresStr.length){
						result=-1;
						break;
					}
				}
			}
		}
		}catch(NumberFormatException e){
			System.err.println("Invalid version number");
		}
		return result;
	}

	/**
	 * Recherche le numéro de la version utlisé pour un commit donné
	 * @param user
	 * @param repository
	 * @param sha
	 * @param pathToPOM
	 * @return la version de la libraire utilisé
	 * @throws HttpException
	 * @throws IOException
	 * @throws InvalideMethodUrlException
	 * @throws MaxRequestException
	 */
	protected String getLibraryVersion(String user, String repository, String sha, String pathToPOM) throws HttpException, IOException, InvalideMethodUrlException, MaxRequestException {
		String request, prefixe, resultVersion;
		int start, end;
		request = "https://raw2.github.com/"
				+ user+"/"
				+ repository+"/"
				+ sha
				+ pathToPOM
				+ "/pom.xml";

		prefixe = "<groupId>"+Controller.getGroupId()+"</groupId>"
				+"<artifactId>"+Controller.getArtefactId()+"</artifactId>"
				+"<version>";
			resultVersion = sendRequest(request).getResponseBodyAsString();
			resultVersion = resultVersion.replaceAll("\\s","");
			start = resultVersion.indexOf(prefixe) + prefixe.length();
			end = start + resultVersion.substring(start).indexOf("</version>");
			resultVersion = resultVersion.substring(start, end);
		return resultVersion;
	}

	/**
	 * 
	 * @param user
	 * @param repo
	 * @param sha
	 * @return
	 * @throws FileNotFoundException 
	 */
	protected String findPathPom(String user, String repo, String sha) throws FileNotFoundException{
		String requete, response, result="";
		JSONArray jsontree;
		boolean pomFound = false;
		requete ="https://api.github.com/repos/"
				+ user+"/"
				+ repo+"/"
				+ "git/trees/"
				+ sha;
		try {
			response = sendRequest(requete).getResponseBodyAsString();
			if (response.contains("\"pom.xml\"")) {
				result = "";
				pomFound = true;
			} else {
				jsontree = (new JSONObject(response)).getJSONArray("tree");
				for (int i = 0; i < jsontree.length(); i++) {
					if(jsontree.getJSONObject(i).getString("path").equals(repo)){
						result = "/" + repo;
						pomFound = true;
					}
				}
			}
		} catch (JSONException e) {
			System.err.println("Erreur lors de la recherche du pom.xml dans "+user+"/"+repo+" : "+e);
		} catch (HttpException e) {
			System.err.println("Erreur lors de la recherche du pom.xml dans "+user+"/"+repo+" : "+e);
		} catch (IOException e) {
			System.err.println("Erreur lors de la recherche du pom.xml dans "+user+"/"+repo+" : "+e);
		} catch (InvalideMethodUrlException e) {
			System.err.println("Erreur lors de la recherche du pom.xml dans "+user+"/"+repo+" : "+e);
		} catch (MaxRequestException e) {
			System.err.println("Erreur lors de la recherche du pom.xml dans "+user+"/"+repo+" : "+e);
		}
		if(!pomFound){
			throw new FileNotFoundException("pom.xml not found in "+user+"/"+repo);
		}
		return result;
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
	protected int getProjectSize(String user, String repo) throws IOException, HttpException{
		int size=0;
		JSONObject project;
		try {
			project = new JSONObject(sendRequest("https://api.github.com/repos/"+user+"/"+repo).getResponseBodyAsString());
			//System.out.println(project.toString());
			try {
				size = project.getInt("size");
				//System.out.println("Size : " + size);
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
	protected ArrayList<String> sendMultiPagesRequest(String request) throws HttpException, InvalideMethodUrlException, IOException, MaxRequestException{
		ArrayList<String> finalResult = new ArrayList<String>();
		String UriNextPage, linkResponse;
		GetMethod gmethod;
		JSONArray jsonTemp;

		System.out.println("-------------------- Dans sendMultipageRequest");
		UriNextPage = request;
		while(UriNextPage != null && UriNextPage.length()>0){
			gmethod = sendRequest(UriNextPage);
			System.out.println("UriNextPage premier : " + UriNextPage);
			try {
				jsonTemp = new JSONArray(gmethod.getResponseBodyAsString());
				if(jsonTemp.length()>0){
					for (int i = 0; i < jsonTemp.length(); i++) {
						finalResult.add(jsonTemp.getJSONObject(i).getString("sha"));
					}
					linkResponse = gmethod.getResponseHeader("Link").getValue();
					if (linkResponse.contains(">; rel=\"next\"")) {
						UriNextPage = linkResponse.subSequence(linkResponse.indexOf("<")+1, linkResponse.indexOf(">; rel=\"next\"")).toString();
					} else {
						UriNextPage = "";
					}
					System.out.println("UriNextPage : " + UriNextPage);
				}else{
					UriNextPage = "";
				}
			} catch (JSONException e) {
				System.out.println(e.getMessage());
			}

		}
		//		while(UriNextPage != null && UriNextPage.length()>0){
		//			gmethod = sendRequest(UriNextPage);
		//			System.out.println("UriNextPage premier : " + UriNextPage);
		//			bodyResponse = gmethod.getResponseBodyAsString();
		//			//System.out.println(bodyResponse);
		//			if(bodyResponse.length()>3){
		//				finalResult += bodyResponse.substring(1, bodyResponse.length()-1)+",";
		//				linkResponse = gmethod.getResponseHeader("Link").getValue();
		//				//System.out.println("linkResponse : " + linkResponse);
		//				if (linkResponse.contains(">; rel=\"next\"")) {
		//					UriNextPage = linkResponse.subSequence(linkResponse.indexOf("<")+1, linkResponse.indexOf(">; rel=\"next\"")).toString();
		//				} else {
		//					UriNextPage = "";
		//				}
		//				System.out.println("UriNextPage : " + UriNextPage);
		//			}else{
		//				UriNextPage = "";
		//			}
		//		}
		System.out.println("-------------------- Fin sendMultipageRequest");

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
		JSONObject sha_commits = new JSONObject();

		TextMining tMining;
		ArrayList<Pair_String> users_repos = new ArrayList<Pair_String>();
		JSONArray commit_infos = new JSONArray();
		JSONObject details_commit  = new JSONObject();
		int index;
		Pair_String user_repo;
		GoogleSearch gs = GoogleSearch.getInstance();


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

		// TODO Change next line
		/* II. Récupération des utilisateurs et répertoires via une recherche Google */
		urls = gs.getUrlResult(request,endURL);
		//				urls.add("/url?q=https://github.com/excilys-blemale/projet-test-jenkins/blob/master/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CCgQFjAB&usg=AFQjCNHq7BRpDuU7pkkMpz7RvOziAEX08w");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:c3D7xLADygcJ:https://github.com/excilys-blemale/projet-test-jenkins/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CCsQIDAB&usg=AFQjCNHLUJ5QLu95jT6aLpAjvxUlb6d2og");
		//				urls.add("/url?q=https://github.com/jbourcie/projet-musee/blob/master/aapweb/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CC0QFjAC&usg=AFQjCNHoZLLKtuutPbal6KX0OmmomYRapw");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:5ww8N8D1D70J:https://github.com/jbourcie/projet-musee/blob/master/aapweb/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CDAQIDAC&usg=AFQjCNGz1yEMfNLwDRE9d2QubTP_b8KZIA");
		//				urls.add("/url?q=https://github.com/aktos/projet/blob/master/projet/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CEgQFjAH&usg=AFQjCNGnM5q2CdPHq_fVxYnjnzqVOKMrVQ");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:sGYrjHJKYRMJ:https://github.com/aktos/projet/blob/master/projet/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CEsQIDAH&usg=AFQjCNERbBtY2d8DLD2dP1jW8Ad0DebFtA");
		//				urls.add("/url?q=https://github.com/abois/mywebapp/blob/master/pom.xml&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CE0QFjAI&usg=AFQjCNG36i9_EqydOK2MFujYNgUYt4AWHw");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:znwfzAr9m54J:https://github.com/abois/mywebapp/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=Sl-xUoLZKceV7Aa80IDQCA&ved=0CFAQIDAI&usg=AFQjCNGBwmSq6amYHhcK74f1vQvkpMNuYg");
		//				urls.add("/url?q=https://github.com/divarvel/TA-Melog/blob/master/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CCMQFjAAOAo&usg=AFQjCNGUekbiGWAz3Bhwxyqc7tdNMkl_IA");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:ugN72Njg4MQJ:https://github.com/divarvel/TA-Melog/blob/master/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CCYQIDAAOAo&usg=AFQjCNHtKmCY_ucXv7CmYR5pY6Imp3K1Iw");
		//				urls.add("/url?q=https://github.com/Pasquet/projet-15min/blob/master/projet-15-webapp/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CC0QFjACOAo&usg=AFQjCNHrDPt4KhDzJuhonhxbRJO5U_8KeA");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:0AY_sfQOD80J:https://github.com/Pasquet/projet-15min/blob/master/projet-15-webapp/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CDAQIDACOAo&usg=AFQjCNEdjBVZRuKmTxSbN2nu9StvTHF3Ag");
		//				urls.add("/url?q=https://github.com/trich/tiw5-2011-tp2/blob/master/projet/modele/pom.xml&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CEYQFjAHOAo&usg=AFQjCNF1O6vxYk-DcMKGbWUfBsBGVCclvQ");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:xl6BQbLFxDUJ:https://github.com/trich/tiw5-2011-tp2/blob/master/projet/modele/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUqn_GMmL7Aa6j4GIAQ&ved=0CEkQIDAHOAo&usg=AFQjCNEM9MkLqryx2FyfdrV4WZz3mI8-7w");
		//				urls.add("/url?q=https://github.com/sunye/AlmaGTD/blob/master/GTDClientKrom/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCMQFjAAOBQ&usg=AFQjCNE6_iokobDiiL84vHLkIcu-E4H18A");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:TGtHVX_GFuEJ:https://github.com/sunye/AlmaGTD/blob/master/GTDClientKrom/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCYQIDAAOBQ&usg=AFQjCNFh2jb13Fyiru4K4K92SWvAb_Mlrg");
		//				urls.add("/url?q=https://github.com/sunye/AlmaGTD/blob/master/GTDWebClient/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCgQFjABOBQ&usg=AFQjCNFsF5k9N8SoNuJfWPSx0r1ygk_fTg");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:eKNE2-pnRXAJ:https://github.com/sunye/AlmaGTD/blob/master/GTDWebClient/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CCsQIDABOBQ&usg=AFQjCNEP6HnVdEwd-LsaKNbS0gUI5Pkfdw");
		//				urls.add("/url?q=https://github.com/trich/tiw5-2011-tp2/blob/master/projet/web-interface/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CC0QFjACOBQ&usg=AFQjCNE6iikCLQk5gMbtFSNDZxmo6pXRkQ");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:7OnQeUY7rgMJ:https://github.com/trich/tiw5-2011-tp2/blob/master/projet/web-interface/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDAQIDACOBQ&usg=AFQjCNE0zBKxtr2VEOGL57-Hhkru2ICcpA");
		//				urls.add("/url?q=https://github.com/ChristelleLacan/QuizZer/blob/master/QuizZer/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDgQFjAEOBQ&usg=AFQjCNFijrGvKrNW-MV9ON9HZk6s78lGiQ");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:aJufrzJh028J:https://github.com/ChristelleLacan/QuizZer/blob/master/QuizZer/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CDsQIDAEOBQ&usg=AFQjCNHI8LgYMZS4PNVBW5OfsCMZs_wzkQ");
		//				urls.add("/url?q=https://github.com/pthurotte/testDevCloud/blob/master/appSuiviExploit-webapp/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CEgQFjAHOBQ&usg=AFQjCNEfNr0fhMxzCqMBN2H5yC-IAVU7xA");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:D2_mfqBYiqwJ:https://github.com/pthurotte/testDevCloud/blob/master/appSuiviExploit-webapp/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CEsQIDAHOBQ&usg=AFQjCNFnGuSOd9rgqxyNjG26RTlKMGlHUw");
		//				urls.add("/url?q=https://github.com/Pasquet/projet-15min/blob/master/projet15-functional-tests/pom.xml&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CE0QFjAIOBQ&usg=AFQjCNFNDRAKdBX-GzMOiXiQ-l4Xc8rZkg");
		//				urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:qOoRxkVJQogJ:https://github.com/Pasquet/projet-15min/blob/master/projet15-functional-tests/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CFAQIDAIOBQ&usg=AFQjCNH5vIOzRUGWCVdOwaI9rkVaInDnZA");
		urls.add("/url?q=https://webcache.googleusercontent.com/search%3Fclient%3Dubuntu%26channel%3Dfs%26q%3Dcache:qOoRxkVJQogJ:https://github.com/cbremard/projetGL/blob/master/projet15-functional-tests/pom.xml%252B%2522projet%2522%2B%25223.8.1%2522%2Bsite:github.com%26oe%3Dutf-8%26gws_rd%3Dcr%26hl%3Dfr%26ct%3Dclnk&sa=U&ei=S1-xUtedKLHT7Aa81YHwDg&ved=0CFAQIDAIOBQ&usg=AFQjCNH5vIOzRUGWCVdOwaI9rkVaInDnZA");


		System.out.println("----------------------------------- URLs");
		/* Récupération des couples user-repo */
		for (String url : urls) {
			System.out.println(url);
			try {
				user = getUser(url);
				repo = getRepo(url,user);
				user_repo = new Pair_String(user, repo);
				users_repos.add(user_repo);	// Liste des couples user-repository pour obtenir chaque projet
			} catch (InvalideMethodUrlException e) {
				System.err.println(e.getMessage());
			}
		}
		System.out.println("------------------------------------ END URLs");

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

		System.out.println("Nombre de couples repo-user trouvés : " + users_repos.size());

		/* IV. Récupération des commits */
		// Boucle sur chaque couple user-repository
		Pair_String pair_user_repo;
		while(users_repos.size()>0 && projects.size()<100){
			pair_user_repo = users_repos.remove(0);
			try {
				sha_commits = getCommit(pair_user_repo.getLeft(), pair_user_repo.getRight());
				project = new GithubProject(pair_user_repo.getLeft(), pair_user_repo.getRight(), sha_commits);
				projects.add(project);
			} catch (OldVersionNotFoundException e) {
				System.err.println(e.getMessage());
			} catch (NullPointerException e) {
				System.err.println("NullPointerException : "+e.getMessage());
			} catch (FileNotFoundException e) {
				System.err.println(e.getMessage());
			}
		}


		/* V. Récupération de la taille des commits et construction du score final */
		// Boucle sur chaque projet
		for (GithubProject proj : projects) {
			try {
				// Boucle sur chacun des 3 (=nbOfAnalysedCommits) commits que l'on veut analyser
				for (int k = 1; k <= nbOfAnalysedCommits; k++) {
					details_commit = new JSONObject(sendRequest("https://api.github.com/repos/"+
							proj.getUser()+"/"+proj.getRepo()+"/commits/"
							+ proj.getSha_commits().getString("commitAt_t"+k)+""
							).getResponseBodyAsString());

					// Sauvegarde des commentaires associés à chaque commit
					proj.setComments(details_commit.getJSONObject("commit").getString("message"));

					// Sauvegarde du nombre de lignes modifiées
					proj.setModified_lines(details_commit.getJSONObject("stats").getInt("total"));
				}

				// Récupération de la taille du projet
				proj.setOctet_size(getProjectSize(proj.getUser(),proj.getRepo()));


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

		//		for (GithubProject proj : projects) {
		//			try {
		//				// Boucle sur chacun des 3 (=nbOfAnalysedCommits) commits que l'on veut analyser
		//				for (int k = -1; k < nbOfAnalysedCommits-1; k++) {
		//					compare_commits = new JSONObject(sendRequest("https://api.github.com/repos/"+
		//							proj.getUser()+"/"+proj.getRepo()+"/compare/"
		//							+proj.getDetail_commits().getString("commitAt_t"+k)+"..."
		//							+proj.getDetail_commits().getString("commitAt_t"+(k+1))+"").getResponseBodyAsString());
		//
		//					commit_infos = compare_commits.getJSONArray("commits");
		//
		//					System.out.println("https://api.github.com/repos/"+
		//							proj.getUser()+"/"+proj.getRepo()+"/compare/"
		//							+proj.getDetail_commits().getString("commitAt_t"+k)+"..."
		//							+proj.getDetail_commits().getString("commitAt_t"+(k+1))+"");
		//
		//					// Sauvegarde des commentaires associés à chaque commit
		//					for (int l = 0; l < commit_infos.length(); l++) {
		//						proj.setComments(commit_infos.getJSONObject(l).getJSONObject("commit").getString("message"));
		//					}
		//
		//					commit_infos = compare_commits.getJSONArray("files");
		//					// Other loop because sometime, you have more than one commit between two given SHA
		//					for (int l = 0; l < commit_infos.length(); l++) {
		//						proj.setModified_lines(commit_infos.getJSONObject(l).getInt("changes"));
		//					}
		//				}
		//
		//				proj.setOctet_size(GetProjectSize(proj.getUser(),proj.getRepo()));
		//				
		//				
		//			} catch (JSONException e) {
		//				System.err.println(e.getMessage()+" ("+e+")");
		//			} catch (HttpException e) {
		//				System.err.println(e.getMessage());
		//			} catch (IOException e) {
		//				System.err.println("Connection faillure : "+e);
		//			} catch (InvalideMethodUrlException e) {
		//				System.err.println(e.getMessage());
		//			} catch (MaxRequestException e) {
		//				System.err.println(e.getMessage());
		//			}
		//		}

		// Intégration de l'analyse des commentaires associés aux commits
		if (projects.size()>0) {
			tMining = new TextMining();
			for (GithubProject proj : projects) {
				//System.out.println("projet : " + proj.getUser() + " comment : "+ proj.getComments());
				tMining.indexComments(proj.getComments(), proj.getUser(), proj.getRepo());
			}
			projects = tMining.analyseComments(projects);
		}

		// Calcul du score total de la méthode Github
		// Division du score total par le nombre de projets trouvés
		float sum_score_comments=0;
		if(projects.size()>0){
			for (GithubProject proj : projects) {
				sum_score_comments+=proj.getScore_comments();
				Github.getInstance().setScore(Github.getInstance().getScore() + proj.getScorePond());
			}
			Github.getInstance().setScore(Github.getInstance().getScore()/sum_score_comments);
		}
	}

}