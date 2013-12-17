package projetGL.IHM.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	
	void greetServer(ArrayList<String> input, AsyncCallback<ArrayList<String>> callback)
			throws IllegalArgumentException;
}
