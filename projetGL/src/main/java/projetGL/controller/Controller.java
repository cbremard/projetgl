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
		Github method_git = new Github();
		method_git.setCoeff(1);
		list_method.add(method_git);
		System.out.println("done");
	}


	/**
	 * Lance l'application
	 * @param list_method
	 */
	public void run(){
		int globalScore = 0;
		// Si github est cochée dans l'IHM
		list_method.get(0).prepare();

		System.out.println("Scores computation in Controller");
		for (Method method : list_method) {
			System.out.println(method.getCoeff());
			globalScore += method.getCoeff()*method.getScore();System.out.print(".");
		}
		System.out.println("done");
		System.out.println("Final score = " + globalScore);
	}

	public static String getLibrairie() {
		// TODO Auto-generated method stub
		return "fr.ensai";
	}

	public static String getNewVersion() {
		// TODO Auto-generated method stub
		return "test8";
	}

}