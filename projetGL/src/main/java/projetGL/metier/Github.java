package projetGL.metier;

import java.io.FileNotFoundException;
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
	 * Gestion de l'authentification de l'application auprès de Github
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
	 * Récupération des "nbOfSavedCommit" commits suivant le changment de version pour un utilisateur et un répertoire donné.
	 * Méthode en 3 étapes:
	 *    I. Récupération de tous les numéros de commits liés au répertoire donné
	 *    II. Recherche par dichotomie des commits relatifs au changement de version
	 *    III. Sélection des nbOfSavedCommit commits suivant le changement de version et construction du JSON retourné
	 * @author BREMARD Corentin
	 * @return Un JSON vide si le project en question n'a pas effectuer le changement de version désiré. Sinon, la méthode retourne un Json avec les paramètres "user" (le propriétaire du projet), "repo" (le répertoire du projet), "commitOldVersion" (numéro SHA du project juste avant le changement de version), et "commitAt_ti" (numéros SHA du project après le changement de version).
	 * @throws OldVersionNotFoundException 
	 * @throws NullPointerException
	 * @throws FileNotFoundException 
	 */
	@Override
	protected JSONObject getCommit(String user, String repository) throws OldVersionNotFoundException, NullPointerException, FileNotFoundException {
		JSONObject jsonsResult = new JSONObject();
		String currentVersion="", previousVersion, temporaryStr="";
		ArrayList<String> list_sha = null;
		boolean reverse, finBoucle, oldVersionFound;

		int currentIndexSha, previousIndexSha, indexShaTemp = 0;

		/* I. Récupération de tous les numéros de commits liés au répertoire donné */

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

		/* II. Recherche par dichotomie des commits relatifs au changement de version */
		System.out.println("Recherche de l'ancienne version dans les pom.xml de "+user+"/"+repository);
		String adress_tree=findPathPom(user, repository, list_sha.get(0));
		finBoucle = false;
		oldVersionFound = false;
		currentIndexSha = list_sha.size()-1;
		previousIndexSha = 0;
		previousVersion = Controller.getNewVersion();
		reverse = compareTwoVersion(Controller.getOldVersion(), Controller.getNewVersion())<0;
		while(!finBoucle){
			try {
				indexShaTemp = currentIndexSha;
				System.out.println("Jump from "+ previousIndexSha+" to "+currentIndexSha+"(reverse = "+reverse+")");
				currentVersion = getLibraryVersion(user, repository, list_sha.get(currentIndexSha), adress_tree);
				System.out.println("Version from "+previousVersion+" to "+currentVersion);
				if(!reverse){
					//Cas normal où NewVersion est plus récente que OldVersion
					if(Math.abs(currentIndexSha-previousIndexSha)<1){
						//Fin de l'algorithme
						finBoucle = true;
						if(currentIndexSha>0 && compareTwoVersion(Controller.getOldVersion(),currentVersion)==0 && compareTwoVersion(currentVersion,getLibraryVersion(user, repository, list_sha.get(currentIndexSha-1), adress_tree))!=0){
							//Succès, le changement de version a été trouvé.
							oldVersionFound = true;
						}
					}else if(compareTwoVersion(Controller.getOldVersion(),currentVersion)>0){
						//currentVersion plus récente que la version recherchée
						currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
						finBoucle = currentIndexSha>=list_sha.size();
					}else if (compareTwoVersion(Controller.getOldVersion(),currentVersion)<0){
						//currentVersion plus anciennne que la version recherchée
						currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
						finBoucle = currentIndexSha<0;
					}else{
						//currentVersion égale à la version recherchée
						if(currentIndexSha<0){
							//Fin de l'algorithme avec échec. Le changement de version n'a pas été trouvé.
							finBoucle = true;
						}else if(compareTwoVersion(Controller.getOldVersion(),getLibraryVersion(user, repository, list_sha.get(currentIndexSha-1), adress_tree))!=0){
							//Fin de l'algorithme avec succès. Le changement de version a été trouvé.
							finBoucle = true;
							oldVersionFound = true;
						}else{
							//cas où l'on doit remonter pour trouver LE commit ayant fait le changement de version
							//eg. NewVersion
							//    OldVersion
							//    OldVersion <- current position
							currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
							finBoucle = currentIndexSha<0;
						}
					}
				}else{
					//Cas anormal où NewVersion est plus ancienne que OldVersion
					if(Math.abs(currentIndexSha-previousIndexSha)<1){
						//Fin de l'algorithme
						finBoucle = true;
						if(currentIndexSha>0 && compareTwoVersion(Controller.getOldVersion(),currentVersion)==0 && compareTwoVersion(currentVersion,getLibraryVersion(user, repository, list_sha.get(currentIndexSha-1), adress_tree))!=0){
							//Succès, le changement de version a été trouvé.
							oldVersionFound = true;
						}
					}else if(compareTwoVersion(Controller.getOldVersion(),currentVersion)>0){
						//currentVersion plus récente que la version recherchée
						currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
						finBoucle = currentIndexSha<0;
					}else if (compareTwoVersion(Controller.getOldVersion(),currentVersion)<0){
						//currentVersion plus anciennne que la version recherchée
						currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
						finBoucle = currentIndexSha>=list_sha.size();
					}else{
						//currentVersion égale à la version recherchée
						if(currentIndexSha<0){
							//Fin de l'algorithme avec échec. Le changement de version n'a pas été trouvé.
							finBoucle = true;
						}else if(compareTwoVersion(Controller.getOldVersion(),getLibraryVersion(user, repository, list_sha.get(currentIndexSha-1), adress_tree))!=0){
							//Fin de l'algorithme avec succès. Le changement de version a été trouvé.
							finBoucle = true;
							oldVersionFound = true;
						}else{
							//cas où l'on doit remonter pour trouver LE commit ayant fait le changement de version
							//eg. NewVersion
							//    OldVersion
							//    OldVersion <- current position
							currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
							finBoucle = currentIndexSha<0;
						}
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				if(currentIndexSha>previousIndexSha){
					currentIndexSha -= Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					finBoucle=currentIndexSha<0;
				}
				else if(currentIndexSha<previousIndexSha){
					currentIndexSha += Math.ceil(Math.abs(currentIndexSha-previousIndexSha)/2);
					finBoucle=currentIndexSha>=list_sha.size();
				}
				else{
					finBoucle=true;
				}
			}
			previousIndexSha = indexShaTemp;
			previousVersion = currentVersion;
		}

		/* III. Sélection des nbOfSavedCommit commits suivant le changement de version et construction du JSON retourné */
		if(oldVersionFound){
			temporaryStr = "{\"user\":\""+user+"\"";
			temporaryStr += ",\"repo\":\""+repository+"\"";
			temporaryStr += ",\"commitOldVersion\":\""+list_sha.get(currentIndexSha)+"\"";
			for (int j = 1; j <= nbOfAnalysedCommits; j++) {
				temporaryStr += ",\"commitAt_t"+j+"\":\"";
				if(currentIndexSha-j>=0){
					temporaryStr += list_sha.get(currentIndexSha-j);
				}
				temporaryStr += "\"";
			}
			temporaryStr += "}";
			try {
				jsonsResult = new JSONObject(temporaryStr);
			} catch (JSONException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}else{
			throw new OldVersionNotFoundException(user+"'s repository ("+repository+") doesn't use the old version.");
		}
		return jsonsResult;
	}

	/**
	 * Compare 2 versions d'une librairie
	 * @param previousVersion
	 * @param currentVersion
	 * @return -1 si la version de gauche est la nouvelle, 1 si c'est celle de droite et 0 si c'est la même version
	 */
	protected int compareTwoVersion(String previousVersion, String currentVersion) {
		int result = 0;
		String previousFiguresStr[], currentFiguresStr[];

		previousVersion=previousVersion.replaceAll("-SNAPSHOT", "");
		previousVersion=previousVersion.replaceAll("-DEPRECATED", "");
		currentVersion=currentVersion.replaceAll("-SNAPSHOT", "");
		currentVersion=currentVersion.replaceAll("-DEPRECATED", "");

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
			System.err.println("Invalid version number in "+previousVersion+" or "+currentVersion);
		}
		return result;
	}

	/**
	 * Recherche le numéro de la version utlisée pour un commit donné
	 * @param user : le propriétaire du projet
	 * @param repository : le nom du projet Github
	 * @param sha : le numéro du commit
	 * @param pathToPOM : le chemin de l'emplacement du pom.xml dans le projet
	 * @return la version de la libraire utilisée
	 * @throws HttpException
	 * @throws IOException
	 * @throws InvalideMethodUrlException
	 * @throws MaxRequestException
	 * @throws FileNotFoundException
	 */
	protected String getLibraryVersion(String user, String repository, String sha, String pathToPOM) throws HttpException, IOException, InvalideMethodUrlException, MaxRequestException, FileNotFoundException {
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
		start = resultVersion.indexOf(prefixe);
		if(start<0){throw new FileNotFoundException("Library not found in "+user+"/"+repository+"(sha:"+sha+")");}
		start += prefixe.length();
		end = resultVersion.substring(start).indexOf("</version>");
		if(end<0){throw new FileNotFoundException("Library not found in "+user+"/"+repository+"(sha:"+sha+")");}
		end += start;
		resultVersion = resultVersion.substring(start, end);
		return resultVersion;
	}

	/**
	 * Renvoie pour un projet donné, l'emplacement du pom.xml dans le projet (à la racine ou dans le dossier du projet)
	 * @param user : le propriétaire du projet
	 * @param repo : le nom du projet sous Github
	 * @param sha : le numéro du commit
	 * @return le chemin de l'emplacement du pom.xml dans le projet
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
	 * Retourne l'utilisateur présent dans une URL Github
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
	 * Retourne le répertoire présent dans une URL Github pour un utilisateur donné
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
	 * @author fanny
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
			try {
				size = project.getInt("size");
			} catch (NumberFormatException e) {
				System.err.println("Erreur lors de la recherche de la taille du projet : " + e.getMessage());
				e.printStackTrace();
			}
		} catch (InvalideMethodUrlException e) {
			System.err.println("Erreur lors de la recherche de la taille du projet : " + e.getMessage());
			e.printStackTrace();
		} catch (MaxRequestException e) {
			System.err.println("Erreur lors de la recherche de la taille du projet : " + e.getMessage());
			e.printStackTrace();
		}catch (JSONException e) {
			System.err.println("Erreur lors de la recherche de la taille du projet : " + e.getMessage());
			e.printStackTrace();
		}

		return size;
	}

	/**
	 * Les requêtes github qui retournent plusieurs objets sont limitées par défaut, à 30 objets par réponse.
	 * Cette méthode permet de récupérer l'ensemble des résultats.
	 * Elle surcharge en quelque sorte la méthode mère sendRequest(String request)
	 * @author BREMARD corentin
	 * @param request : l'url générale qui permet d'afficher la première page de résultats
	 * @throws MaxRequestException 
	 * @throws IOException 
	 * @throws InvalideMethodUrlException 
	 * @throws HttpException 
	 * @return result : la liste des numéros SHA des commits du projet
	 */
	protected ArrayList<String> sendMultiPagesRequest(String request) throws HttpException, InvalideMethodUrlException, IOException, MaxRequestException{
		ArrayList<String> finalResult = new ArrayList<String>();
		String UriNextPage, linkResponse;
		GetMethod gmethod;
		JSONArray jsonTemp;

		System.out.println("-------------------- Dans sendMultipageRequest");
		UriNextPage = request;
		int nbPageCollected = 0;
		while(UriNextPage != null && UriNextPage.length()>0 && nbPageCollected<100){
			gmethod = sendRequest(UriNextPage);

			try {
				jsonTemp = new JSONArray(gmethod.getResponseBodyAsString());
				if(jsonTemp.length()>0){
					for (int i = 0; i < jsonTemp.length(); i++) {
						finalResult.add(jsonTemp.getJSONObject(i).getString("sha"));
					}
					try{
						linkResponse = gmethod.getResponseHeader("Link").getValue();
					}catch(NullPointerException e){
						linkResponse = "";
					}
					if (linkResponse.contains(">; rel=\"next\"")) {
						UriNextPage = linkResponse.subSequence(linkResponse.indexOf("<")+1, linkResponse.indexOf(">; rel=\"next\"")).toString();
					} else {
						UriNextPage = "";
					}
				}else{
					UriNextPage = "";
				}
				nbPageCollected++;
			} catch (JSONException e) {
				System.out.println(e.getMessage());
			}

		}
		System.out.println("-------------------- Fin sendMultipageRequest");

		return finalResult;
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
	 *    IV. Récupération des commits relatifs au changement de version
	 *    V. Récupération de la taille des commits et construction du score final
	 */
	public void compute() {
		state = new StateRunning();

		/* I. Initialisation des variables */
		String request, endURL, user, repo;
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<GithubProject> projects = new ArrayList<GithubProject>();
		GithubProject project;
		JSONObject sha_commits = new JSONObject();
		float sum_score_comments=0;
		TextMining tMining;
		ArrayList<Pair_String> users_repos = new ArrayList<Pair_String>();
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

		/* II. Récupération des utilisateurs et répertoires via une recherche Google */
		urls = gs.getUrlResult(request,endURL);


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

		/* IV. Récupération des commits relatifs au changement de version */
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



		// Intégration de l'analyse des commentaires associés aux commits
		if (projects.size()>0) {
			tMining = new TextMining();
			for (GithubProject proj : projects) {
				tMining.indexComments(proj.getComments(), proj.getUser(), proj.getRepo());
			}
			projects = tMining.analyseComments(projects);
		}

		// Calcul du score total de la méthode Github
		// Division du score total par le nombre de projets trouvés
		if(projects.size()>0){
			for (GithubProject proj : projects) {
				sum_score_comments+=proj.getScore_comments();
				Github.getInstance().setScore(Github.getInstance().getScore() + proj.getScorePond());
			}
			Github.getInstance().setScore(Github.getInstance().getScore()/sum_score_comments);
		}
	}

}