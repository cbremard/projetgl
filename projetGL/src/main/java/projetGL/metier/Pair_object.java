package projetGL.metier;

public class Pair_object {

	  private Object left;
	  private Object right;

	  public Pair_object(Object left, Object right) {
	    this.left = left;
	    this.right = right;
	  }

	  public Object getLeft() { return left; }
	  public Object getRight() { return right; }

	  @Override
	  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

	  @Override
	  public boolean equals(Object o) {
	    if (o == null) return false;
	    if (!(o instanceof Pair_object)) return false;
	    Pair_object pairo = (Pair_object) o;
	    return this.left.equals(pairo.getLeft()) &&
	           this.right.equals(pairo.getRight());
	  }

	}