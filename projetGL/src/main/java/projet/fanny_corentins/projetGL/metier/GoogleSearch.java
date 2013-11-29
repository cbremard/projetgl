package projet.fanny_corentins.projetGL.metier;

import java.util.ArrayList;

public class GoogleSearch extends MethodJunior{
	private static GoogleSearch uniqueGoogle = null;
	
	public static GoogleSearch getInstance(){
		if (uniqueGoogle==null){
			uniqueGoogle = new GoogleSearch();
		}
		return uniqueGoogle;
	}
	
	public int getNbResult(String request){
		//TODO by CorentinB
		return 0;
	}
	
	public ArrayList<String> getUrlResult(String request){
		ArrayList<String> urls = new ArrayList();
		//TODO by CorentinB
		return urls;
	}

	@Override
	public float getScore() {
		// TODO Auto-generated method stub
		// return GoogleSearch.getInstance().getNbResult("theREquest");
		return 0;
	}

}
