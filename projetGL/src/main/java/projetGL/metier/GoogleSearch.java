package projetGL.metier;

import java.io.IOException;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Evaluator.IsEmpty;

public class GoogleSearch extends MethodJunior{
	private static GoogleSearch uniqueGoogle = null;
	
	public static GoogleSearch getInstance(){
		if (uniqueGoogle==null){
			uniqueGoogle = new GoogleSearch();
		}
		return uniqueGoogle;
	}
	
	public int getNbResult(String request){
		//TODO by CorentinB
		return 0;
	}
	
	public Stack<String> getUrlResult(String request){
		Stack<String> urls = new Stack();
		Document doc = null;
		System.out.println(request);
		try {
			doc = Jsoup.connect(request).userAgent("Firefox").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (doc != null){
			urls.add(doc.toString());
		}
		//TODO by CorentinB
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
