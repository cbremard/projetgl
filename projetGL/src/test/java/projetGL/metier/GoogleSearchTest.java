package projetGL.metier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/* Au moins une méthde de test dans chaque classe de test
L'assertion: le résultat de l'expression booléenne indique un succès ou une erreur
Dans paramètres assert : valeur attendue toujours avant valeur courante
On peut aussi tester le renvoi d'exception (sans mettre l'assert et en exécutant la méthode à tester)

Méthode SetUp() : appelée avant chaque méthode de test ! Donc à mettre que si tous les tests ont besoin
de quelque chose de commun (ex : création d'une instance d'un même type)
Méthode TearDown() : pas obligatoire, mais permet de libérer une connexion à la BDD par exemple

Méthode de test : ne renvoie aucun résultat, n'a pas de paramètre, est public

 */
public class GoogleSearchTest extends TestCase{
	
	/**
	 * Test de getNbResult : 
	 * 	- Est ce que le nombre de résultats renvoyés par la requête google est le bon ?
	 */
	public void testgetNbResult() {
		GoogleSearch gs = GoogleSearch.getInstance();
		Document doc;
		try {
			InputStream file = new FileInputStream("src/resources/test_docHtml.html");
			doc = Jsoup.parse(file, "UTF-8", "src/resources/test_docHtml.html");
			assertEquals(400,gs.getNbResult(doc));
			
		} catch (FileNotFoundException e) {
			assertTrue("Le fichier de test n'existe pas",false);
		} catch (IOException e) {
			assertTrue("Le parse de test de Jsoup ne fonctionne pas", false);
		}
	}


	/* TODO : Est ce qu'il faut la tester ?
	 * Sachant que même le test va se connecter à internet pour lancer la requête
	public void testgetUrlResult() {
		GoogleSearch gs = GoogleSearch.getInstance();
		
	}
	*/
	// Méthode pour tester le renvoi d'exceptions
	//	public void testSommer() throws Exception {
	//		MaClasse2 mc = new MaClasse2(1,1);
	//
	//		// cas de test 1
	//		assertEquals(2,mc.sommer());
	//
	//		// cas de test 2
	//		try {
	//			mc.setA(0);
	//			mc.setB(0);
	//			mc.sommer();    
	//			// Méthode fail() si l'exception n'est pas attrapée
	//			fail("Une exception de type IllegalStateException aurait du etre levee");
	//		} catch (IllegalStateException ise) {
	//		}
	//
	//	}
}
