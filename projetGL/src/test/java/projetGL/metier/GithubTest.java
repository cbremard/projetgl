package projetGL.metier;

import junit.framework.Assert;
import junit.framework.TestCase;
import projetGL.exceptions.InvalideMethodUrlException;


public class GithubTest extends TestCase{
	Github github = Github.getInstance();

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
		Assert.assertEquals(g1.getCoeff(),g2.getCoeff());
		Assert.assertEquals(g1.getResquestCounter(),g2.getResquestCounter());
	}

	public void testGetUser1(){
		/** Given */
		String url, user;
		/** When */
		url = "/search?q=%22fr.ensai%22+%22test8%22+site:github.com&client=ubuntu&hs=RgF&channel=fs&ie=UTF-8&oe=UTF-8&prmd=ivns&source=lnt&tbs=li:1&sa=X&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CB4QpwUoAQ";
		url = "/url?q=https://github.com/barais/ensai2013/blob/master/pom.xml&sa=U&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CCMQFjAA&usg=AFQjCNFH-lrldP3fFbGW7n4CGbKprsPa3w";
		user = "barais";
		/** Then */
		try {
			Assert.assertEquals(user, github.getUser(url));
		} catch (InvalideMethodUrlException e) {
			Assert.assertTrue(false);
		}
	}

	public void testGetUser2(){
		/** Given */
		String falseUrl;
		/** When */
		falseUrl = "/search?q=%22fr.ensai%22+%22test8%22+site:github.com&client=ubuntu&hs=RgF&channel=fs&ie=UTF-8&oe=UTF-8&prmd=ivns&source=lnt&tbs=li:1&sa=X&ei=VLKhUu20KcWM7Qb3hIGwBA&ved=0CB4QpwUoAQ";
		/** Then */
		try {
			github.getUser(falseUrl);
			Assert.assertTrue(false);
		} catch (InvalideMethodUrlException e) {
			Assert.assertTrue(true);
		}
	}

	public void testGetRepo1() {
		/** Given */
		String url, repo;
		/** When */
		url="http://webcache.googleusercontent.com/search?q=cache:rh5KfLNbjWcJ:https://github.com/javagems/junit/blob/master/pom.xml+&cd=26&hl=en&ct=clnk&gl=fr&client=ubuntu";
		repo = "junit";
		/** Then */
		try {
			Assert.assertEquals(repo, github.getRepo(url, "javagems"));
		} catch (InvalideMethodUrlException e) {
			Assert.assertTrue(false);
		}
	}

	public void testGetRepo2() {
		/** Given */
		String falseUrl;
		/** When */
		falseUrl="http://webcache.googleusercontent.com/search?q=cache:5hbmDxUUIG0J:https://github.fr/WhisperSystems/maven/blob/master/gcm-server/releases/junit/junit/3.8.1/junit-3.8.1.pom+&cd=2&hl=en&ct=clnk&gl=fr&client=ubuntu";
		/** Then */
		try {
			github.getRepo(falseUrl, "WhisperSystems");
			Assert.assertTrue(false);
		} catch (InvalideMethodUrlException e) {
			Assert.assertTrue(true);
		}
	}

}
