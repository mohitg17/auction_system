/*
 * Author: Vallath Nandakumar and EE 422C instructors
 * Date: April 20, 2020
 * This starter code is from the MultiThreadChat example from the lecture, and is on Canvas. 
 * It doesn't compile.
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
	DataOutputStream toServer = null; 
	DataInputStream fromServer = null;

	@Override
	public void start(Stage primaryStage) { 
		BorderPane paneForTextField = new BorderPane(); 
		paneForTextField.setPadding(new Insets(5, 5, 5, 5)); 
		paneForTextField.setStyle("-fx-border-color: green"); 
		paneForTextField.setLeft(new Label("Enter a radius: ")); 

		TextField tf = new TextField(); 
		tf.setAlignment(Pos.BOTTOM_RIGHT); 
		paneForTextField.setCenter(tf); 

		BorderPane mainPane = new BorderPane(); 
		// Text area to display contents 
		TextArea ta = new TextArea(); 
		mainPane.setCenter(new ScrollPane(ta)); 
		mainPane.setTop(paneForTextField); 


		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 450, 200); 
		primaryStage.setTitle("Client"); // Set the stage title 
		primaryStage.setScene(scene); // Place the scene in the stage 
		primaryStage.show(); // Display the stage 

		tf.setOnAction(e -> { 
			try { 
				// Get the radius from the text field 
				String message = tf.getText().trim(); 

				// Send the radius to the server 
				toServer.writeChars(message); 
				toServer.flush(); 

//				// Get area from the server 
//				String response = fromServer.readUTF(); 
//
//				// Display to the text area 
//				ta.appendText("Response is " + response + "\n");

			} 
			catch (IOException ex) { 
				System.err.println(ex); 
			} 
		}); 

		try { 
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 8000); 
			fromServer = new DataInputStream(socket.getInputStream()); 
			toServer = new DataOutputStream(socket.getOutputStream()); 
		} 
		catch (IOException ex) { 
			ta.appendText(ex.toString() + '\n');
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
