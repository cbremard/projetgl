package projet.fanny_corentins.projetGL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class App 
{
    public static void main( String[] args )
    {
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
}
