package projetGL.metier;

import junit.framework.TestCase;

import org.junit.Test;

import projetGL.exceptions.InvalideMethodUrlException;


public class GithubTest extends TestCase{

	public void testGetInstance(){
		/** Given */
		Github g1 = Github.getInstance();
		Github g2 = Github.getInstance();
		/** When */
		g1.setCoeff(1);
		g2.setCoeff(2);
		g1.setResquestCounter(1);
		g2.setResquestCounter(2);
		/** Then */
		assertEquals(g1.getCoeff(),g2.getCoeff());
		assertEquals(g1.getResquestCounter(),g2.getResquestCounter());
	}

	public void testGetUser1(){
		/** Given */
		String url, user;
		Github github = Github.getInstance();
		/** When */
		url = "/search?q=%22fr.ensai%22+%22test8%22+site:github.com&client=ubuntu&hs=RgF&channel=fs&ie=UTF-8&oe=UTF-8&prmd=ivns&source=lnt&tbs=li:1&sa=X&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CB4QpwUoAQ";
		url = "/url?q=https://github.com/barais/ensai2013/blob/master/pom.xml&sa=U&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CCMQFjAA&usg=AFQjCNFH-lrldP3fFbGW7n4CGbKprsPa3w";
		user = "barais";
		/** Then */
		try {
			assertEquals(user, github.getUser(url));
		} catch (InvalideMethodUrlException e) {
			assertTrue(false);
		}
	}

	public void testGetUser2(){
		/** Given */
		String falseUrl;
		Github github = Github.getInstance();
		/** When */
		falseUrl = "/search?q=%22fr.ensai%22+%22test8%22+site:github.com&client=ubuntu&hs=RgF&channel=fs&ie=UTF-8&oe=UTF-8&prmd=ivns&source=lnt&tbs=li:1&sa=X&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CB4QpwUoAQ";
		/** Then */
		try {
			github.getUser(falseUrl);
			assertTrue(false);
		} catch (InvalideMethodUrlException e) {
			assertTrue(true);
		}
	}

	public void testGetRepo1() {
		/** Given */
		String url, repo;
		Github github = Github.getInstance();
		/** When */
		url="http://webcache.googleusercontent.com/search?q=cache:rh5KfLNbjWcJ:https://github.com/javagems/junit/blob/master/pom.xml+&cd=26&hl=en&ct=clnk&gl=fr&client=ubuntu";
		repo = "junit";
		/** Then */
		try {
			assertEquals(repo, github.getRepo(url, "javagems"));
		} catch (InvalideMethodUrlException e) {
			assertTrue(false);
		}
	}

	public void testGetRepo2() {
		/** Given */
		String falseUrl;
		Github github = Github.getInstance();
		/** When */
		falseUrl="http://webcache.googleusercontent.com/search?q=cache:5hbmDxUUIG0J:https://github.fr/WhisperSystems/maven/blob/master/gcm-server/releases/junit/junit/3.8.1/junit-3.8.1.pom+&cd=2&hl=en&ct=clnk&gl=fr&client=ubuntu";
		/** Then */
		try {
			github.getRepo(falseUrl, "WhisperSystems");
			assertTrue(false);
		} catch (InvalideMethodUrlException e) {
			assertTrue(true);
		}
	}
	
	/**
	 * Test de la méthode compareTwoVersions avec :
	 *  - Deux versions identiques
	 *  - La version de gauche plus récente
	 *  - La version de droite plus récente
	 */
	@Test
	public void testCompareTwoVersions(){
		Github github = Github.getInstance();
		assertEquals("Comparaison de deux versions identiques OK", 0, github.compareTwoVersions("3.2.1", "3.2.1"));
		assertEquals("Comparaison de deux versions différentes (gauche) OK", -1, github.compareTwoVersions("5.2", "3.2.1"));
		assertEquals("Comparaison de deux versions différentes (droite) OK", 1, github.compareTwoVersions("1", "1.1.1"));
	}
}