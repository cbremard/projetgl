package projetGL.metier;

import java.util.Comparator;

public class PairComparator implements Comparator<Pair>{
	/* cf. http://www.javabeat.net/sorting-custom-types-in-java/ */

	public int compare(Pair pair1, Pair pair2) {
		int result;
		result = pair1.getLeft().compareTo(pair2.getLeft());
		if( result == 0 ){
			result = pair1.getRight().compareTo(pair2.getRight());
		}
		return result;
	}
}
