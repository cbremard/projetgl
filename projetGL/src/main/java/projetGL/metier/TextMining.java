package projetGL.metier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

import projetGL.controller.Controller;


// TODO : mettre un coeff, en fonction de si les commentaires des commits parlent de la version ou non
public class TextMining {

	private Directory fsDirectory;
	private Document doc;
	private Analyzer standardAnalyzer;
	private IndexDeletionPolicy deletionPolicy;
	private IndexWriter indexWriter;
	private boolean create;
	private BooleanQuery query;
	private IndexSearcher indexSearcher;
	private TopDocs topDocs;

	
	/**
	 * Constructeur de TextMining avec initialisation des attributs de Lucene (pour la recherche de commentaires pertinents)
	 */
	public TextMining() {
		super();
		try {
			//Création de l'instance de Directory où les fichiers d'index seront stockés
			fsDirectory =  FSDirectory.open(new File("src/resources/dir_lucene"));
			/* Création d'une instance d'analyseur, qui sera utilisée pour découper les données d'entrée */
			standardAnalyzer = new StandardAnalyzer(Version.LUCENE_30);
			//Création d'un nouvel index
			create = true;
			//Création d'une stratégie de suppression
			// TODO à voir s'il faut laisser comme ça
			deletionPolicy = new KeepOnlyLastCommitDeletionPolicy(); 
			indexWriter = null;
			doc = new Document();

			try {
				indexWriter = new IndexWriter(fsDirectory,standardAnalyzer,create,deletionPolicy, IndexWriter.MaxFieldLength.UNLIMITED);
			} catch (CorruptIndexException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			} catch (LockObtainFailedException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}


	/**
	 * Création de la requête à utiliser pour chercher dans l'index
	 */
	public void queriesCreation(){

		Query queryF;
		query = new BooleanQuery();
		queryF = new FuzzyQuery(new Term("comment", "version"));
		/* QueryF: Recherche des commentaires contenant des mots similaires à 'version' dans le champ 'message'. */
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", "library"));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", "librairie"));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", "dependence"));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", Controller.getArtefactId()));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", Controller.getGroupId()));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", Controller.getNewVersion()));
		query.add(queryF, Occur.SHOULD);
		queryF = new FuzzyQuery(new Term("comment", (String) Controller.getOldVersion()));
		query.add(queryF, Occur.SHOULD);

	}


	/**
	 * Ajoute les commentaires des commits (les champs) au document Lucene
	 * @param comment : les commentaires associés aux commits analysés
	 * @param user : le propriétaire du projet
	 * @param repo : le nom du projet
	 */
	public void indexComments(String comment, String user, String repo){
		// Ajout des champs à un Document Lucene
		// TODO : nouveau document pour chaque projet ?
		doc = new Document();
		doc.add(new Field("comment", comment, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("user", user, Field.Store.NO, Field.Index.NOT_ANALYZED));
		doc.add(new Field("repo", repo, Field.Store.NO, Field.Index.NOT_ANALYZED));
		
		try {
			// Ajout des documents à l'index
			indexWriter.addDocument(doc);
		} catch (CorruptIndexException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Analyse des commentaires des commits
	 * @param projects : la liste des projets Github à scorer
	 * @return la liste des projets Github mis à jour avec les scores calculés grâce au TextMining
	 */
	public 	ArrayList<GithubProject> analyseComments(ArrayList<GithubProject> projects){

		ArrayList<GithubProject> list_resu = new ArrayList<GithubProject>();
		
		// Création de la requête
		queriesCreation();

		// TODO : à voir si l'analyse se fait sur des documents différents ou si c'est sur les champs
		
		try {
			// Fermeture de l'écriture dans l'index
			indexWriter.close();
			// Initialisation de la recherche dans l'index
			indexSearcher = new IndexSearcher(fsDirectory);
			
			
			// Recherche dans l'index grâce à la requête
			/* Le premier paramètre est la requête à exécuter,
			   le second est le nombre de résultats qui doivent être ramenés */
			topDocs = indexSearcher.search(query,projects.size());
			System.out.println("Total hits "+topDocs.totalHits);
			
			
			// Récupère le tableau de références vers les documents
			ScoreDoc[] scoreDocArray = topDocs.scoreDocs;	
			
			
			GithubProject proj ;
			int docId;
			for(ScoreDoc scoredoc: scoreDocArray){
				proj = new GithubProject();
				proj.setScore_comments(scoredoc.score);
				System.out.println("Score : " + scoredoc.score);
				// Retourne le document et affiche les détails
				docId = scoredoc.doc;
				Document d = indexSearcher.doc(docId);
				proj.setUser(d.getField("user").stringValue());
				proj.setRepo(d.getField("repo").stringValue());
				list_resu.add(proj);
			}
			
			indexSearcher.close();
			
			list_resu = recordScores(list_resu, projects);
			
			// TODO : voir si ça fonctionne
			indexWriter.deleteAll();
			
		} catch (CorruptIndexException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return list_resu;
	}

	
	/**
	 * Méthode qui associe à chaque projet le score trouvé par la méthode de TextMining sur les commentaires
	 * @param list_doc : la liste des scores associés aux projets pertinents pour la requête
	 * @param projects : la liste des projets Github à scorer
	 * @return la liste des projets mis à jour avec le score de TextMining sur les commentaires
	 */
	public ArrayList<GithubProject> recordScores(ArrayList<GithubProject> list_doc, ArrayList<GithubProject> projects){
		
		for (GithubProject doc : list_doc) {
			for (GithubProject proj : projects) {
				if ((proj.getUser() == doc.getUser()) && (proj.getRepo() == doc.getRepo())) {
					proj.setScore_comments(doc.getScore_comments());
					break;
				}
			}
		}
		return projects;
	}



}
