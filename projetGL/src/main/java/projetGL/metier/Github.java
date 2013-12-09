package projetGL.metier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import projetGL.controller.Controller;

public class Github extends Api{
	private static Github uniqueGithub = null;
	float coeff;

	public Github() {
		super();
	}
	
	public static Github getInstance(){
		if (uniqueGithub==null){
			uniqueGithub = new Github();
		}
		return uniqueGithub;
	}
	
	@Override
	public float getScore() {
		float result = -1;
		try {
			result = state.compute(this);
		} catch (Exception e) {
			e.getMessage();
			System.err.println("The score will have a value of -1.");
		}
		return result;
	}

	@Override
	public void getCommit() {
		//TODO
	}
	

	private String getUser(String url) {
		int begin, end;
		String refText="https://github.com/";
		begin = url.indexOf(refText) + refText.length();
		end = begin + url.substring(begin).indexOf("/");
		return url.substring(begin, end);
	}
	private String getRepo(String url, String user) {
		int begin, end;
		String refText="https://github.com/" + user + "/";
		begin = url.indexOf(refText) + refText.length();
		end = begin + url.substring(begin).indexOf("/");
		return url.substring(begin, end);
	}

	private void test(){
		System.out.println("Start");
		String path = "C:/Users/pORTABLE/Desktop/json.txt";
		/* Etape d'initialisation */
		String librarie = "org.swinglabs";// Mot clé pour recherche
		// String url = "https://api.github.com/search/code?q="+librarie+"+in:file+extension:gemspec+-repo:"+librarie+"/"+librarie+".rb&sort=indexed";
		// String url =  "https://api.github.com/search/repositories?q=tetris+language:assembly&sort=stars&order=desc";
		// String url = "https://api.github.com/search/code?q="+librarie+"+language:java";
		String url = "https://api.github.com/search/code?q=" + librarie + "+in:file+language:xml+@barais/ensai2013";
		// String url =
		// "https://api.github.com/search/issue?q="+librarie+"+in:body+language:xml";
		String reponse = "";
		JSONObject json;
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);
		/* Modification à apporter avant d'envoyer la requète */
		method.addRequestHeader("Accept", "application/vnd.github.preview");
		try {
			System.out.println("request sending");
			/* Envoi de la requète */
			int statusCode = client.executeMethod(method);
			System.out.println("request recieved");
			/* Test si tout s'est bien passé */
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + method.getStatusText()
						+ "\n" + "Try later." + "\n");
				System.out.println(url);
				System.out.println(method.getResponseBodyAsString());
			} else {
				System.out.println("Request to string");
				/* Transformation du résultat en JSON */
				System.out.println("Request to JSon");
				reponse = method.getResponseBodyAsString();
				json = new JSONObject(reponse);
				/* Traitements diverses du JSON */
				System.out.println(reponse + "\n");
				JSONArray jsonArray = json.getJSONArray("items");
				System.out.println("Nombre de Gems annoncé: "
						+ json.getInt("total_count"));
				System.out.println("Nombre de Gems récupéré: "
						+ jsonArray.length() + "\n");
				JSONObject gem;
				writeText("Résultat complet : \n" + json.toString(),
						path, true);
				writeText("\n" + "Résultats individuels :",
						path, false);
				for (int i = 0; i < jsonArray.length(); i++) {
					gem = jsonArray.getJSONObject(i);
					// System.out.println("Gem " + i + ": " + gem.toString());
					writeText("Gem " + i + ": \n" + gem.toString(),
							path, false);
				}
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		System.out.println("End");
	}
    
    private static void writeText(String text, String path, boolean overwrite){
		BufferedWriter bw;
		FileWriter fw;
		try {
			fw = new FileWriter(path, !overwrite);
			bw  = new BufferedWriter(fw);
			bw.write(text + "\n");
			bw.close(); 
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
    
    
    
    
    
    
	public float compute() {
		/* Initiation des variables */
		String request, linkHref, endURL, temp;
		ArrayList<String> users = new ArrayList<String>();
		ArrayList<String> repos = new ArrayList<String>();
		int score=0, nbPages=1;
		Document doc;
		Elements links;
//		request = "https//www.google.fr/search?client=ubuntu" + "&channel=fs" + "&q=eric+pidoux" + "&ie=utf-8" + "&oe=utf-8" + "&gws_rd=cr" + "&ei=_GlqUsniL4OEhQerl4CQDQ#channel=fs" + "&q=%22"+Controller.getLibrairie()+"%22+%22"+Controller.getNewVersion()+"%22+site:github.com";
		// TODO Change next line
		request = "https://www.google.fr/search?client=ubuntu"
				+ "&channel=fs"
				+ "&q=%22"+Controller.getLibrairie()+"%22+%22"+Controller.getNewVersion()+"%22+site:github.com"
				+ "&ie=utf-8"
				+ "&oe=utf-8"
				+ "&gws_rd=cr"
				+ "&ei=UwyeUva_KuvY7AaK04CICg";
		endURL = "pom.xml";
		
		/* Récupération des résultats d'une recherche Google */
//		urls = gs.getUrlResult(request);
		try {
			doc = Jsoup.connect(request).userAgent("Firefox").get();
			nbPages= (int) (0.1*toInt(doc.getElementById("resultStats").text()));
			writeText(doc.toString(), "/home/corentin/Bureau/googleResultFromJsoup.text", true);
			/* Filtrages des résultats obtenus : Récupération de tout les <a href="..."> </a>, Sélection des lien contenant "pom.xml" et Extraction des users et repositories*/
			links = doc.getElementsByTag("a");
			for (Element link : links) {
				linkHref = link.attr("href");
				writeText(linkHref, "/home/corentin/Bureau/googleResultFromJsoup.text", false);
				if(linkHref.contains(endURL)){
					temp = getUser(linkHref);
					users.add(temp);
					repos.add(getRepo(linkHref,temp));
				}
			}
			writeText("Résultats :", "/home/corentin/Bureau/googleResultFromJsoup.text", false);
			for (int i = 0; i < users.size(); i++) {
				writeText("User = "+users.get(i)+" and repository = "+repos.get(i), "/home/corentin/Bureau/googleResultFromJsoup2.text", false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*
		 for (Iterator<String> iterator = urls.iterator(); iterator.hasNext();) {

			temp = iterator.next();
			if(temp.substring(temp.length()-endURL.length()) == endURL){
				users.add(getUser(temp));
				repos.add(getRepo(temp));
System.out.println(temp);
System.out.println("  "+getUser(temp)+"      "+getRepo(temp));
			}
			
		}*/
		
		
		/* Récupération des commits */
		
		// TODO finish this procedure
		return score;
	}

	private int toInt(String text) {
		System.out.println(text);
		
		return 0;
	}


	
}
