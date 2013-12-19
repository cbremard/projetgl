package projetGL.metier;

import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import projetGL.controller.Controller;

public class GoogleSearch extends MethodJunior{
	private static GoogleSearch uniqueGoogle = null;
	
	private GoogleSearch(){
		super();
	}
	
	
	public static GoogleSearch getInstance(){
		if (uniqueGoogle==null){
			uniqueGoogle = new GoogleSearch();
		}
		return uniqueGoogle;
	}
	
	
	
	
	/**
	 * Permet, grâce à l'analyse de la page HTML renvoyée pour une requête Google,
	 * de trouver le nombre de résultats pour cette même requête.
	 * @param doc : le document HTML à analyser.
	 * @return le nombre de résultats renvoyés pour une requête Google définie
	 */
	public int getNbResult(Document doc){
		String text;
		int NbResult=0;
		text = doc.getElementById("resultStats").text(); // le document doit contenir un ID "resultStats"
		//System.out.println(text);
		
		// Spliter la chaîne de caractères obtenue pour en extraire le nombre de résultats
		String[] words;
		String nombre = "";
		words = text.split("");
		for (String a : words){
			if (a.matches("[0-9]")){
				nombre += a;
			}
		}
		
		NbResult = Integer.parseInt(nombre);
		System.out.println("Nombre de résultats poour la requête Google : " + NbResult);
		
		return NbResult;
	}
	
	
	
	/**
	 * Méthode qui interroge Google via des requêtes pour récupérer les URLs
	 * contenant le mot clé donné en paramètre
	 * @param request : requête Google
	 * @param keyword : mot clé qui détermine quelles urls nous intéressent
	 * @return
	 */
	public ArrayList<String> getUrlResult(String request, String keyword){
		ArrayList<String> urls = new ArrayList<String>();
		String linkHref;
		long nbPages;
		Document doc;
		Elements links;
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			doc = Jsoup.connect(request).userAgent("Firefox").get();
			nbPages = (long) Math.ceil(0.1*getNbResult(doc)); // On récupère le nombre de résultats et on veut le nombre de pages (10 résultats par page)
			System.out.println(nbPages + " pages");
			nbPages = Math.min(100, nbPages); // On se limite à 100 pages (Google n'affiche rien après le 1000ième résultat)
			for (long i = 0; i < nbPages; i++) {
				if(i>0){
					doc = Jsoup.connect(request+"&start="+10*i).userAgent("Firefox").get();
				}
				links = doc.getElementsByTag("a");
				for (org.jsoup.nodes.Element link : links) {
					linkHref = link.attr("href");
					if(linkHref.contains(keyword)){
						urls.add(linkHref);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Erreur au niveau des requêtes Google : " + e);
		} catch (NullPointerException e){
			System.err.println("Echec de la connexion");
		}
		/*System.out.println("urls");
		for (String url : urls){
			System.out.println(url);
		}*/
		return urls;
	}

	
	@Override
	public float getScore() {
		return GoogleSearch.getInstance().score;
	}

	public void setScore(float _score){
		GoogleSearch.getInstance().score = _score;
	}
	
	
	
	
	/**
	 * calcul_score() lance un appel à compute() pour le calcul du score.
	 * Les différents cas d'utilisation sont gérés via le paramètre state.
	 * @author fanny
	 * @return result: le score généré par la méthode
	 */
	public float calcul_score() {
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
	 * Exécute le calcul de score pour la méthode "GoogleSearch" si cette dernière est cochée par l'utilisateur
	 * 
	 * @author fanny
	 * @return TODO
	 */
	public float compute() {
		state = new StateRunning();
		float _score = 0;
		float _scoreInit=0;
		String request = "https://www.google.fr/search?client=ubuntu"
				+ "&channel=fs"
				+ "&q=%22"+Controller.getGroupId()
				+ "%22+%22"+Controller.getArtefactId()
				+ "%22+%22"+Controller.getNewVersion()+ "%22"
				+ "&ie=utf-8"
				+ "&oe=utf-8"
				+ "&gws_rd=cr"
				+ "&ei=UwyeUva_KuvY7AaK04CICg";
		Document doc;
		
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			doc = Jsoup.connect(request).userAgent("Firefox").get();
			_scoreInit = GoogleSearch.getInstance().getNbResult(doc);
		} catch (IOException e){
			System.err.println("Erreur au niveau de la requête Google : " + e);
		}
		// TODO calcul du score réel
		_score = _scoreInit;
		
		GoogleSearch.getInstance().setScore(_score);
		
		// TODO Quoi à retourner ?
		return 0;
	}

}
