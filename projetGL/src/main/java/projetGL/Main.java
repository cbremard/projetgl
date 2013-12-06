package projetGL;

import java.util.ArrayList;

import projetGL.metier.Github;
import projetGL.metier.MethodJunior;
import projetGL.metier.StateReady;
import projetGL.metier.Method;

/**
 * 
 * @author fanny and Corentin
 *
 */
public class Main {
    public static void main( String[] args ){
    	System.out.print("Initiation step");
    	int globalScore = 0; System.out.print(".");
    	ArrayList<Method> methodes = new ArrayList(); System.out.print(".");
    	Github method1 = new Github();
    	method1.prepare();
    	methodes.add(method1); System.out.println("done");
    	
    	System.out.print("Scores computation");
    	for (Method method : methodes) {
    		globalScore += method.getCoeff()*method.getScore();System.out.print(".");
		}
    	System.out.println("done");
    	System.out.println("Final score = " + globalScore);
    }
}
