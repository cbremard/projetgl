package projetGL.metier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import progetGL.exceptions.InvalideMethodUrlException;
import progetGL.exceptions.MaxRequestException;
import progetGL.exceptions.OldVersionNotFoundException;
import projetGL.controller.Controller;


public class GithubTest {
//	String url = "https://www.google.fr/search?client=ubuntu&channel=fs&q=java+url+getContent-Length&ie=utf-8&oe=utf-8&gws_rd=cr&ei=a4mtUterMIi20QX84ID4Bw#channel=fs&q=entamer";
//	String url = "https://github.com/cbremard/projetgl/tree/master/projetGL";
//	try {
//		String user = getInstance().getUser(url);
//		System.out.println(user);
//		System.out.println(getInstance().getRepo(url, user));
//	} catch (InvalideGithubUrlException e) {
//		e.printStackTrace();
//	}


	public static Github getInstanceTest(){
		//TODO
		return null;
	}

	public float getScoreTest() {
		//TODO
		return 0;
	}


	protected JSONObject getCommitTest(String user, String repository) throws OldVersionNotFoundException {
		//TODO
		return null;
	}

	private String getUserTest(String url) throws InvalideMethodUrlException {
		//TODO
		return null;
	}

	private String getRepoTest(String url, String user) throws InvalideMethodUrlException {
		//TODO
		return null;
	}
	
	private int GetProjectSizeTest(String user, String repo) throws Exception{
		//TODO
		return 0;
	}
	
	private String sendMultiPagesRequestTest(String request) throws HttpException, InvalideMethodUrlException, IOException, MaxRequestException{
		//TODO
		return null;
	}

	public float computeTest() {
		//TODO
		return 0;
	}

}
