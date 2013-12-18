package projetGL.metier;


public class Pair{
	private String left;
	private String right;
	public Pair(String left, String right) {
		super();
		this.left = left;
		this.right = right;
	}
	public String getLeft() {
		return left;
	}
	public void setLeft(String left) {
		this.left = left;
	}
	public String getRight() {
		return right;
	}
	public void setRight(String right) {
		this.right = right;
	}
	public boolean equals(Pair pair){
	   return (left.equals(pair.getLeft()) && right.equals(pair.getRight()));
	}
}
