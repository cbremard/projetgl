package projetGL.metier;

public abstract class MethodJunior implements Method{
	protected float score;
	protected float coefficient;
	
	public float getCoeff() {
		return coefficient;
	}
	public void setCoeff(float coefficient) {
		this.coefficient = coefficient;
	}
	abstract public float getScore();
}
