package projetGL.metier;

public abstract class MethodJunior implements Method{
	protected float score;
	protected float coefficient;
	public State state;
	
	public MethodJunior() {
		super();
		this.state = new StatePassive();
	}
	
	public float getCoeff() {
		return coefficient;
	}
	public void setCoeff(float coefficient) {
		this.coefficient = coefficient;
	}
	public void prepare() {
		this.state = new StateReady();
	}
	
	abstract public float getScore();

}
