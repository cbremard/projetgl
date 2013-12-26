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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import projetGL.controller.Controller;
import projetGL.exceptions.InvalideMethodUrlException;
import projetGL.exceptions.MaxRequestException;


// TODO : mettre un coeff, en fonction de si les commentaires des commits parlent de la version ou non
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
			System.err.println("Problème accès requête HTTP");
			e.printStackTrace();
		} catch (JSONException e) {
			System.err.println("Erreur de JSON");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Erreur IOexception");
			e.printStackTrace();
		} catch (InvalideMethodUrlException e) {
			System.err.println("Méthode URL invalide");
			e.printStackTrace();
		} catch (MaxRequestException e) {
			System.err.println("Nombre de requêtes maximal atteint");
			e.printStackTrace();
		}

		//System.out.println(commitInformation.toString());

		return commits;
	}


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

					// Ajout des champs à un Document Lucene
					doc.add(new Field("message", message, Field.Store.YES, Field.Index.ANALYZED));

				} catch (JSONException e) {
					System.err.println("Erreur de parsage JSON");
					e.printStackTrace();
				}
			}

			// AJout des documents à l'indexWriter
			indexWriter.addDocument(doc);

			indexWriter.close();

			/* Query1 : Recherche des commentaires contenant des mots similaires
				   à 'version' dans le champ 'message'. */
			Query query1 = new FuzzyQuery(new Term("message", "version"));
			Query query2 = new FuzzyQuery(new Term("message", "library"));
			Query query3 = new FuzzyQuery(new Term("message", "librairie"));
			Query query4 = new FuzzyQuery(new Term("message", "dependence"));
			Query query5 = new FuzzyQuery(new Term("message", Controller.getArtefactId()));
			Query query6 = new FuzzyQuery(new Term("message", Controller.getGroupId()));
			Query query7 = new FuzzyQuery(new Term("message", Controller.getNewVersion()));
			Query query8 = new FuzzyQuery(new Term("message", (String) Controller.getOldVersion()));

			BooleanQuery query = new BooleanQuery();
			query.add(query1, Occur.SHOULD);
			query.add(query2, Occur.SHOULD);
			query.add(query3, Occur.SHOULD);
			query.add(query4, Occur.SHOULD);
			query.add(query5, Occur.SHOULD);
			query.add(query6, Occur.SHOULD);
			query.add(query7, Occur.SHOULD);
			query.add(query8, Occur.SHOULD);

			IndexSearcher indexSearcher = new IndexSearcher(fsDirectory);


			
			/* Le premier paramètre est la requête à exécuter,
				   le second est le nombre de résultats qui doivent être ramenés */
			TopDocs topDocs = indexSearcher.search(query,10);	
			System.out.println("Total hits "+topDocs.totalHits);

			// Récupère le tableau de références vers les documents
			ScoreDoc[] scoreDocArray = topDocs.scoreDocs;	
			int i = 0;
			for(ScoreDoc scoredoc: scoreDocArray){
				i++;
				System.out.println("Score : " + scoredoc.score);
				// Retourne le document et affiche les détails
				int docId = scoredoc.doc;
			    Document d = indexSearcher.doc(docId);
			    System.out.println((i) + ". " + d.getField("message").stringValue());
			}

			indexSearcher.close();
			// TODO à voir !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			//indexWriter.deleteAll();
			
			
		} catch (CorruptIndexException e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		} catch (LockObtainFailedException e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		}
		
	}
}
