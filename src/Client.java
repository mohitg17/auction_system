/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * Mohit Gupta
 * mg58629
 * 16295
 * Spring 2020
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.*;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application { 
	// I/O streams 
	private TextArea incoming;
	private TextField outgoing;
	private TextField username;
	private TextField password;
	private BufferedReader reader;
	private PrintWriter writer;
	private Button submit;
	private Button send;
	private Button history;
	private Button quit;
	private ComboBox dropdown;
		
	@Override
	public void start(Stage primaryStage) {
		Text text1 = new Text("Username");   
		Text text2 = new Text("Password"); 
		username = new TextField();         
		password = new TextField();  

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
			String usr = username.getText();
			String pw = password.getText();
			Stage stage = (Stage) submit.getScene().getWindow();
		    stage.close();
		    
			// outgoing
			BorderPane paneForTextField = new BorderPane(); 
			paneForTextField.setPadding(new Insets(10, 10, 10, 10)); 
			paneForTextField.setStyle("-fx-border-color: green"); 
			paneForTextField.setLeft(new Label("Enter a bid: ")); 
			outgoing = new TextField("Bid Amount");
			outgoing.setAlignment(Pos.CENTER_RIGHT);
//			outgoing.setAlignment(Pos.CENTER);
			paneForTextField.setCenter(outgoing);

			// control panel
			GridPane paneForControls = new GridPane();
		    ColumnConstraints column1 = new ColumnConstraints();
		    column1.setPercentWidth(30);
		    ColumnConstraints column2 = new ColumnConstraints();
		    column2.setPercentWidth(30);
		    ColumnConstraints column3 = new ColumnConstraints();
		    column3.setPercentWidth(20);
		    ColumnConstraints column4 = new ColumnConstraints();
		    column4.setPercentWidth(20);
		    paneForControls.getColumnConstraints().addAll(column1, column2, column3, column4);
			paneForControls.setMinSize(600, 50);
			paneForControls.setPadding(new Insets(10, 10, 10, 10));
			paneForControls.setVgap(5); 
			paneForControls.setHgap(5);
			paneForControls.setAlignment(Pos.CENTER_LEFT);
			
			// incoming
			BorderPane mainPane = new BorderPane();
			incoming = new TextArea(); 
			incoming.setPrefSize(600, 600);
			mainPane.setCenter(new ScrollPane(incoming)); 
			mainPane.setTop(paneForTextField);
			
			// items list and text field
			dropdown = new ComboBox();
			paneForControls.add(dropdown, 0, 0);
			paneForControls.add(outgoing, 1, 0);
			
			// send and history button
			send = new Button("Send");
			send.setAlignment(Pos.CENTER);
			paneForControls.add(send, 2, 0);
			history = new Button("History");
			history.setAlignment(Pos.CENTER);
			paneForControls.add(history, 3, 0);
			
			// quit button
			quit = new Button("Quit");
			quit.setAlignment(Pos.CENTER_RIGHT);
			mainPane.setBottom(quit);
			

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
			
			send.setOnAction(r -> {
				if(!dropdown.getValue().equals("items")) {
					Message message = new Message((String)dropdown.getValue(), outgoing.getText());
					GsonBuilder builder = new GsonBuilder();
					Gson gson = builder.create();
					writer.println(gson.toJson(message));
					writer.flush();
					outgoing.setText("Bid Amount");
					outgoing.requestFocus();
				}
			});
			
			quit.setOnAction(f -> {
				secondStage.close();
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
		Gson gson = new Gson();
		Message msg = gson.fromJson(message, Message.class);
		switch(msg.getType()) {
		case "items":
			String[] items = msg.getMessage().split("\n");
			for(int i = 0; i < items.length; i++) {
				dropdown.getItems().add(items[i]);
			}
			break;
		case "update":
			String tmp = msg.getMessage();
			Platform.runLater(() -> {
				incoming.appendText(tmp + "\n");
			});
			break;
		}
	}
	
	class Reader implements Runnable {
		@Override
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
//					processRequest(message);
					String tmp = message;
					Platform.runLater(() -> {
						incoming.appendText(tmp + "\n");
					});
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
