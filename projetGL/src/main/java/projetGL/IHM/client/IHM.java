package projetGL.IHM.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class IHM implements EntryPoint {
	
	
	private VerticalPanel mainPanel = new VerticalPanel() ;
	
	private TextBox groupField = new TextBox();
	private TextBox artifactField = new TextBox();
	private TextBox fromVersionField = new TextBox();
	private TextBox toVersionField = new TextBox();
	private Button sendButton = new Button("Calculer");
	private DialogBox dialogBox = new DialogBox();
	private Button closeButton = new Button("Fermer");
	private Label groupLabel = new Label();
	private HTML serverResponseLabel = new HTML();
	

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		groupField.setText("groupId");
		artifactField.setText("artifactId");
		fromVersionField.setText("fromVersion");
		toVersionField.setText("toVersion");

		// Add the fields and Button to the mainPanel
		// Use RootPanel.get() to get the entire body element
		mainPanel.add(groupField);
		mainPanel.add(artifactField);
		mainPanel.add(fromVersionField);
		mainPanel.add(toVersionField);
		mainPanel.add(sendButton);
		
		RootPanel.get().add(mainPanel);

		// Focus the cursor on the name field when the app loads
		groupField.setFocus(true);
		groupField.selectAll();
		
		// Listen for mouse events on the Calculer button.
	    sendButton.addClickHandler(new ClickHandler() {
	      public void onClick(ClickEvent event) {
	        calculer();
	      }
	    });


		// Create the popup dialog box
		dialogBox.setText("Résultat du calcul");
		dialogBox.setAnimationEnabled(true);
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Calcul pour le groupId :</b>"));
		dialogVPanel.add(groupLabel);
		dialogVPanel.add(new HTML("<br><b>Résultat :</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});
		

/*
			
			/**
			 * Send the name from the nameField to the server and wait for a response.
			 *
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = groupField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
										.setText("Remote Procedure Call - Failure");
								serverResponseLabel
										.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {
								dialogBox.setText("Remote Procedure Call");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
							}
						});
			}
		}*/

	}
	

	private void calculer() {
		final String symbol = groupField.getText().toUpperCase().trim();
		groupField.setFocus(true);

	    // groupId must be between 1 and 10 chars that are numbers, letters, or dots.
	    if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
	      Window.alert("'" + symbol + "' n'est pas un groupId valable.");
	      groupField.selectAll();
	      return;
	    }
	    
	    

	    sendButton.setEnabled(false);
	    ArrayList<String> params = new ArrayList<String>();
	    params.add(groupField.getText());
	    params.add(artifactField.getText());
	    params.add(fromVersionField.getText());
	    params.add(toVersionField.getText());
		groupLabel.setText(groupField.getText());
		serverResponseLabel.setText("");
		
		final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
		
		greetingService.greetServer(params,
				new AsyncCallback<ArrayList<String>>() {
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox
								.setText("Résultat du calcul - Erreur");
						serverResponseLabel
								.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML("SERVER_ERROR");
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(ArrayList<String> result) {
						dialogBox.setText("Résultat du calcul");
						serverResponseLabel
								.removeStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(result.get(0));
						dialogBox.center();
						closeButton.setFocus(true);
					}
				});
		
	}

	
}