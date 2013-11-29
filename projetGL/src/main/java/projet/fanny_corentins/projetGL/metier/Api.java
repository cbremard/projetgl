package projet.fanny_corentins.projetGL.metier;

public abstract class Api extends MethodJunior{
	protected int resquestCounter = 0;
	protected final int maxRequest = 5000;
	
	public int getResquestCounter() {
		return resquestCounter;
	}
	public void setResquestCounter(int resquestCounter) {
		this.resquestCounter = resquestCounter;
	}
	public int getMaxRequest() {
		return maxRequest;
	}
	
	abstract public void getCommit();
}
