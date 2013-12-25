package projetGL.metier;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import projetGL.exceptions.InvalideMethodUrlException;
import projetGL.exceptions.MaxRequestException;

public class TextMining {


	public static JSONArray init(){
		JSONObject commitInformation  = new JSONObject();
		JSONArray commits = new JSONArray();
		Github G = Github.getInstance();

		try {
			commitInformation = new JSONObject(G.sendRequest("https://api.github.com/repos/cbremard/projetGL/compare/c06de99d199947e130a5fc92f41c5385f912959a...120bdae5ed4e5e662e0e16639df867bf180c2d09").getResponseBodyAsString());
			commits = commitInformation.getJSONArray("commits");
			System.out.println(commits.toString());
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalideMethodUrlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MaxRequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//System.out.println(commitInformation.toString());

		// Save commits messages for other methodes
		// Affichage requête

		return commits;
	}


	@SuppressWarnings("resource")
	public static void analyseComment(/*JSONArray commits*/){

		// TODO A changer par le paramètre plus tard
		JSONArray commits = init();

		String message = "";
		Document doc = new Document();
		Directory fsDirectory = null;
		
		try {
			/* Création d'une instance d'analyseur, qui sera utilisée pour découper les données d'entrée */
			Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_30);
			//Création d'un nouvel index
			boolean create = true;
			//Création d'une stratégie de suppression
			IndexDeletionPolicy deletionPolicy = new KeepOnlyLastCommitDeletionPolicy(); 
			IndexWriter indexWriter = null;

			//Création de l'instance de Directory où les fichiers d'index seront stockés
			 fsDirectory =  FSDirectory.open(new File("src/resources/dir_lucene"));

			indexWriter = new IndexWriter(fsDirectory,standardAnalyzer,create,deletionPolicy, IndexWriter.MaxFieldLength.UNLIMITED);


			// Parcours du tableau des commits
			for (int l = 0; l < commits.length(); l++) {

				try {
					message = commits.getJSONObject(l).getJSONObject("commit").getString("message");
					System.out.println(message);

					Field messagefield = new Field("message",message,Field.Store.NO,Field.Index.ANALYZED);

					// Ajout des champs à un Document Lucene
					doc.add(messagefield);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// AJout des documents à l'indexWriter
			indexWriter.addDocument(doc);

			indexWriter.close();
			
			/* Recherche des courriers contenant le modèle 'arch*' dans le champ sujet*/
			// Query query = new WildcardQuery(new Term("message","annu?"));
			/* Recherche des courriers contenant des mots similaires
				   à 'admnistrtor' dans le champ sujet. */
			Query query_simil = new FuzzyQuery(new Term("message", "annulation"));


			IndexSearcher indexSearcher = new IndexSearcher(fsDirectory);

			/* Le premier paramètre est la requête à exécuter,
				   le second est le nombre de résultats qui doivent être ramenés */
			TopDocs topDocs = indexSearcher.search(query_simil,1);	
			System.out.println("Total hits "+topDocs.totalHits);

			// Récupère le tableau de références vers les documents
			ScoreDoc[] scoreDocArray = topDocs.scoreDocs;	
			for(ScoreDoc scoredoc: scoreDocArray){
				// Retourne le document et affiche les détails
				Document docu = indexSearcher.doc(scoredoc.doc);
				
				// TODO retourne un nullPOinterException
				System.out.println("Message: "+docu.getField("message").toString());
			}
			

		} catch (CorruptIndexException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (LockObtainFailedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	
	}
}
