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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application { 
	// I/O streams 
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;

	@Override
	public void start(Stage primaryStage) { 
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

		Scene scene = new Scene(mainPane, 600, 600); 
		primaryStage.setTitle("Client"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 

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
