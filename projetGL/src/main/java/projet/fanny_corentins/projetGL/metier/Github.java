package projet.fanny_corentins.projetGL.metier;

public class Github extends Api{
	private static Github uniqueGithub = null;
	public static GithubState gstate;

	public Github(float coefficient) {
		super();
		this.coefficient=coefficient;
	}
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
	public void getCommit() {
		gstate.getCommit();
	}
	@Override
	public float getScore() {
		return gstate.getScore();
	}
}
