package projetGL.metier;

import junit.framework.TestCase;

import org.junit.Test;

public class GithubProjectTest extends TestCase {

	/**
	 * Test de getScorePond avec un projet de taille supérieure à 0 :
	 * Le résultat renvoyé est-il le bon ?
	 */
	@Test
	public void testgetScorePond() {
		GithubProject git = new GithubProject();
		git.setModified_lines(5);
		git.setOctet_size(1000);
		git.setScore_comments(2);
		float resu =  (((float)((20399/1844)*5)/1000)*2);

		assertEquals(resu,git.getScorePond());
	}
	
	/**
	 * Autre Test de getScorePond avec un projet de taille nulle :
	 * Le résultat renvoyé est-il nul ?
	 */
	@Test
	public void testgetScorePond_2() {
		GithubProject git = new GithubProject();
		git.setModified_lines(5);
		git.setOctet_size(0);
		git.setScore_comments(2);
		float resu = 0;
		
		assertEquals(resu,git.getScorePond());
	}
}
