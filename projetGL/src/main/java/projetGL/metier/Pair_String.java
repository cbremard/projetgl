package projetGL.metier;


public class Pair_String{
	private String left;
	private String right;
	public Pair_String(String left, String right) {
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
	public boolean equals(Pair_String pair){
	   return (left.equals(pair.getLeft()) && right.equals(pair.getRight()));
	}
}
