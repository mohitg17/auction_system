/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * Mohit Gupta
 * mg58629
 * 16295
 * Spring 2020
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application { 
	// I/O streams 
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	private Button submit;
		
	@Override
	public void start(Stage primaryStage) {
		Text text1 = new Text("Username");   
		Text text2 = new Text("Password"); 
		TextField username = new TextField();         
		TextField password = new TextField();  

		Button submit = new Button("Submit");

		GridPane gridpane = new GridPane();  
		gridpane.setMinSize(400, 200);
		gridpane.setPadding(new Insets(10, 10, 10, 10)); 
		gridpane.setVgap(5); 
		gridpane.setHgap(5);
		gridpane.setAlignment(Pos.CENTER); 

		gridpane.add(text1, 0, 0); 
		gridpane.add(username, 1, 0); 
		gridpane.add(text2, 0, 1);       
		gridpane.add(password, 1, 1); 
		gridpane.add(submit, 0, 2);
	      
		Scene scene = new Scene(gridpane); 
		primaryStage.setTitle("Login"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage
		
		submit.setOnAction(d -> {
			Stage stage = (Stage) submit.getScene().getWindow();
		    stage.close();
			// outgoing
			BorderPane paneForTextField = new BorderPane(); 
			paneForTextField.setPadding(new Insets(10, 10, 10, 10)); 
			paneForTextField.setStyle("-fx-border-color: green"); 
			paneForTextField.setLeft(new Label("Enter a bid: ")); 
			outgoing = new TextField(); 
			outgoing.setAlignment(Pos.BOTTOM_RIGHT); 
			paneForTextField.setCenter(outgoing); 

			// incoming
			BorderPane mainPane = new BorderPane();
			incoming = new TextArea(); 
			incoming.setPrefSize( 600, 600);
			mainPane.setCenter(new ScrollPane(incoming)); 
			mainPane.setTop(paneForTextField); 
			
			ComboBox dropdown = new ComboBox();
			dropdown.getItems().add("Choice 1");
			dropdown.getItems().add("Choice 2");
			dropdown.getItems().add("Choice 3");

			Stage secondStage = new Stage();
			Scene scene2 = new Scene(mainPane, 600, 600); 
			secondStage.setTitle("Client"); // Set the stage title 
			secondStage.setScene(scene2); // Place the scene in the stage 
			secondStage.show(); // Display the stage 

			outgoing.setOnAction(e -> {
				writer.println(outgoing.getText());
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			});

			try {
				setUpNetworking();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}
	
	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("localhost", 8000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new Reader());
		readerThread.start();
	}
	
	private void processRequest(String message) {
		String[] arr = message.split("\n", 2);
		switch(arr[0]) {
		case "items":
			break;
		}
	}
	
	class Reader implements Runnable {
		@Override
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					incoming.appendText(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
