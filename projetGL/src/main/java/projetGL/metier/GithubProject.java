package projetGL.metier;

import org.json.JSONObject;

/**
 * Un objet GithubProject est un projet sous Github ayant un nom de répertoire (repo) et un propriétaire (user)
 * @author fanny
 *
 */
public class GithubProject {

	private String user;
	private String repo;
	private String comments;
	private float score_comments;
	private JSONObject sha_commits;
	private int modified_lines;
	private int octet_size;
	private static final float lineWeight = 20399/1844; //= Taille en Bits du projet (cf. GitHub) divisée par le nombre total de lignes au 09/01/213
	
	/**
	 * Constructeur sans paramètres
	 */
	public GithubProject() {
		super();
		comments = "";
		modified_lines = 0;
		octet_size = 0;
		score_comments = 0;
	}
	
	/**
	 * Constructeur avec paramètres
	 * @param user : le nom du propriétaire du projet sur Github
	 * @param repo: le nom du répertoire du projet
	 * @param detail_commits : Détail des commits effectués pour le projet (JSON)
	 */
	public GithubProject(String user, String repo, JSONObject detail_commits) {
		super();
		this.user = user;
		this.repo = repo;
		this.sha_commits = detail_commits;
	}

	/**
	 * Calcul du pourcentage de lignes modifiées dans le projet
	 * @return le pourcentage de lignes modifiées ou 0 si le projet est de taille nulle
	 */
	public float getScore() {
		// divide Modified_lines by the project's size in order to have the percentage of modified lines
		if(octet_size >0){
			return lineWeight*modified_lines/octet_size;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Calcul du score pondéré du projet
	 * @return le score total du projet, pondéré par le score sur les commentaires des commits
	 */
	public float getScorePond() {
			return score_comments*getScore();
	}
	
	
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getRepo() {
		return repo;
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}
	
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		if (this.comments == null) {
			this.comments = comments;
		} else {
			this.comments += " ## " + comments;
		}
		
	}

	public JSONObject getSha_commits() {
		return sha_commits;
	}
	public void setSha_commits(JSONObject detail_commits) {
		this.sha_commits = detail_commits;
	}

	public int getModified_lines() {
		return modified_lines;
	}
	public void setModified_lines(int modified_lines) {
		this.modified_lines += modified_lines;
	}

	public int getOctet_size() {
		return octet_size;
	}
	public void setOctet_size(int lines_total) {
		this.octet_size = lines_total;
	}

	public float getScore_comments() {
		return score_comments;
	}
	public void setScore_comments(float score_comments) {
		this.score_comments = score_comments;
	}
	
	
}
