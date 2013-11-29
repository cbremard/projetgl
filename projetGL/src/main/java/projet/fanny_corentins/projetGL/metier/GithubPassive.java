package projet.fanny_corentins.projetGL.metier;

public class GithubPassive extends Api implements GithubState{

	@Override
	public void getCommit() {}

	@Override
	public float getScore() {
		return -1;
	}
}
