package projetGL.metier;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class TextMiningTest {

	@Test
	public void testAnalyseComments() {
		TextMining tm = new TextMining();
		ArrayList<GithubProject> projects = new ArrayList<GithubProject>();
		GithubProject proj = new GithubProject();
		proj.setComments("annualtion du test sur le recherche google (Echec, la page n'a pas du tout été trouvé par google) Question: l'utilisation de google permet t'el de récupérer une liste exhaustive des pom.xml utilisant une version donnée? ## annualtion du test sur le recherche google (Echec, la page n'a pas du tout été trouvé par google) Question: l'utilisation de google permet t'el de récupérer une liste exhaustive des pom.xml utilisant une version donnée? ## Création de la classe Ihm");
		proj.setRepo("projetGL");
		proj.setUser("cbremard");
		projects.add(proj);
		for (GithubProject project : projects) {
			tm.indexComments(project.getComments(), project.getUser(), project.getRepo());
		}
		assertTrue((float)(1.0474639)== tm.analyseComments(projects).get(0).getScore_comments());
	}

}
