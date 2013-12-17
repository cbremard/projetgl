package projetGL.metier;

import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.text.Element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GoogleSearch extends MethodJunior{
	private static GoogleSearch uniqueGoogle = null;
	
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
		System.out.println(NbResult);
		
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
			nbPages = (long) Math.ceil(0.1*getNbResult(doc));
			System.out.println(nbPages + " pages");
			nbPages = Math.min(100, nbPages); // On se limite à 100 pages
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
			System.err.println("Erreur au niveau des requêtes Google");
			e.printStackTrace();
		}
		/*System.out.println("urls");
		for (String url : urls){
			System.out.println(url);
		}*/
		return urls;
	}

	@Override
	public float getScore() {
		// TODO Auto-generated method stub
		// return GoogleSearch.getInstance().getNbResult("theREquest");
		return 0;
	}

	public float compute() {
		// TODO Auto-generated method stub
		return 0;
	}

}
