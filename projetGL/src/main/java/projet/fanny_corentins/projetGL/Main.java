package projet.fanny_corentins.projetGL;

import java.util.ArrayList;

import projet.fanny_corentins.projetGL.metier.GithubReady;
import projet.fanny_corentins.projetGL.metier.Method;

/**
 * 
 * @author fanny and Corentin
 *
 */
public class Main {
    public static void main( String[] args ){
    	int globalScore = 0;
    	ArrayList<Method> methodes = new ArrayList();
    	methodes.add(new GithubReady());
    	
    	for (Method method : methodes) {
    		globalScore += method.getCoeff()*method.getScore();
		}
    	System.out.println(globalScore);
    }
}
