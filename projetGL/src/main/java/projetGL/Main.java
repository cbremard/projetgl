package projetGL;

import java.util.ArrayList;

import projetGL.metier.GithubReady;
import projetGL.metier.Method;

/**
 * 
 * @author fanny and Corentin
 *
 */
public class Main {
    public static void main( String[] args ){
    	System.out.print("Initiation step");
    	int globalScore = 0;System.out.print(".");
    	ArrayList<Method> methodes = new ArrayList();System.out.print(".");
    	methodes.add(new GithubReady());System.out.println("done");
    	
    	System.out.print("Scores computation");
    	for (Method method : methodes) {
    		globalScore += method.getCoeff()*method.getScore();System.out.print(".");
		}
    	System.out.println("done");
    	System.out.println("Final score = " + globalScore);
    }
}
