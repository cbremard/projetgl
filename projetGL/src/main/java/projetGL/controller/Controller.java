package projetGL.controller;

import java.util.ArrayList;

import projetGL.metier.Github;
import projetGL.metier.Method;

public class Controller {
	
	private ArrayList<Method> list_method = new ArrayList<Method>();
		
	/**
	 * Initiation step
	 */
	public void init(){
		System.out.print("Initiation step");

		System.out.print(".");System.out.print(".");

		// Initialisation de la méthode Github
		Github method_git = Github.getInstance();
		method_git.setCoeff(1);
		list_method.add(method_git);
		System.out.println("done");
	}


	/**
	 * Lance l'application
	 * @param list_method
	 */
	public float run(){
		float globalScore = 0;
		for (Method method : list_method) {
			method.prepare();
		}

		System.out.println("Scores computation in Controller");
		for (Method method : list_method) {
			System.out.println("New method created with a coef = "+method.getCoeff());
			globalScore += method.getCoeff()*method.calcul_score();
			System.out.println(".");
		}
		System.out.println("done");
		return globalScore;
	}

	

	public static String getGroupId() {
		// TODO Auto-generated method stub
		return "org.nuxeo.ecm.platform";
	}
	
	
	public static String getArtefactId() {
		// TODO Auto-generated method stub
		return "nuxeo-services-parent";
	}

	public static String getNewVersion() {
		// TODO Auto-generated method stub
//		return "4.11";
		return "5.8-SNAPSHOT";
	}


	public static String getOldVersion() {
		// TODO Auto-generated method stub
//		return "3.8.2";
		return "5.4.1-SNAPSHOT";
	}

}