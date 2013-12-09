package projetGL.metier;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleSearch extends MethodJunior{
	private static GoogleSearch uniqueGoogle = null;
	
	public static GoogleSearch getInstance(){
		if (uniqueGoogle==null){
			uniqueGoogle = new GoogleSearch();
		}
		return uniqueGoogle;
	}
	
	public int getNbResult(Document doc){
		String text;
		int NbResult=0;
		text = doc.getElementById("resultStats").text();
		//TODO Completer cette méthode pour récvupérer le nombre de résultats présent dans text qui peut prendre les valeurs "Environ 1 987 000 résultats" ou "56 r&amp;sultats" ou etc.
		return NbResult;
	}
	
	public ArrayList<String> getUrlResult(String request){
		ArrayList<String> urls = new ArrayList<String>();
		double nbPages;
		Document doc;
		Elements links;
		try {
			doc = Jsoup.connect(request).userAgent("Firefox").get();
			nbPages = Math.ceil(0.1*getNbResult(doc));
			for (double i = 0; i < nbPages; i++) {
				if(i>0){
					doc = Jsoup.connect(request="&start="+10*i).userAgent("Firefox").get();
				}
				links = doc.getElementsByTag("a");
				for (Element link : links) {
					urls.add(link.attr("href"));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
