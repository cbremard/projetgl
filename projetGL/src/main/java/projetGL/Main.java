package projetGL;

import javax.net.ssl.HttpsURLConnection;

import projetGL.controller.Controller;


/**
 * 
 * @author fanny and Corentins
 *
 */
public class Main {
    public static void main( String[] args ){
    	
    	Controller c = new Controller();
    	c.init();
    	c.run();
    	
    }
}
