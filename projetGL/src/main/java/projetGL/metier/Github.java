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
	
	public float compute() {
		/* Initiation des variables */
		String request, endURL, temp;
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<String> users = new ArrayList<String>();
		ArrayList<String> repos = new ArrayList<String>();
		int index, score=0;
		GoogleSearch gs = new GoogleSearch();
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
		urls = gs.getUrlResult(request,endURL);
		for (String url : urls) {
				temp = getUser(url);
				users.add(temp);
				repos.add(getRepo(url,temp));
		}
		/* Suppression des doublons */
//for (int i = 0; i < users.size(); i++) {System.out.println("User = "+users.get(index)+" and repository = "+repos.get(index));}
		index=1;
		while(index < users.size()) {
			if(users.get(index-1)==users.get(index) && repos.get(index-1)==repos.get(index)){
				users.remove(index);
				repos.remove(index);
			}else{
				index++;
			}
		}
//System.out.println("Now:");
//for (int i = 0; i < users.size(); i++) {System.out.println("User = "+users.get(index)+" and repository = "+repos.get(index));}
		
		
		/* Récupération des commits */
		
		
		return score;
	}

//	https://api.github.com/repos/user/repository/events
	
}

/*
private void test(){
	System.out.println("Start");
	String path = "C:/Users/pORTABLE/Desktop/json.txt";
//	Etape d'initialisation
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
//	Modification à apporter avant d'envoyer la requète
	method.addRequestHeader("Accept", "application/vnd.github.preview");
	try {
		System.out.println("request sending");
//		Envoi de la requète
		int statusCode = client.executeMethod(method);
		System.out.println("request recieved");
//		Test si tout s'est bien passé
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + method.getStatusText()
					+ "\n" + "Try later." + "\n");
			System.out.println(url);
			System.out.println(method.getResponseBodyAsString());
		} else {
			System.out.println("Request to string");
//			Transformation du résultat en JSON
			System.out.println("Request to JSon");
			reponse = method.getResponseBodyAsString();
			json = new JSONObject(reponse);
//			Traitements diverses du JSON
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
*/
/*
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
*/
