package projetGL.metier;

import static org.junit.Assert.*;

import org.junit.Test;

public class PairComparatorTest {

	/**
	 * Test de compare() avec un deux paires égales
	 */
	@Test
	public void testCompare() {
		String repo1 = "repo1";
		String repo2 = "repo1";
		String user1 = "user1";
		String user2 = "user1";
		
		Pair_String pair1 = new Pair_String(user1, repo1);
		Pair_String pair2 = new Pair_String(user2, repo2);
		
		PairComparator pair_comp = new PairComparator();
		assertEquals(0,pair_comp.compare(pair1, pair2));
	}
	
	/**
	 * Test de compare() avec un deux paires différentes
	 */
	@Test
	public void testCompare_2() {
		String repo1 = "repo1";
		String repo2 = "repo2";
		String user1 = "user1";
		String user2 = "user2";
		
		Pair_String pair1 = new Pair_String(user1, repo1);
		Pair_String pair2 = new Pair_String(user2, repo2);
		
		PairComparator pair_comp = new PairComparator();
		assertFalse(pair_comp.compare(pair1, pair2)==0);
	}

}
