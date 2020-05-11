/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * Mohit Gupta
 * mg58629
 * 16295
 * Spring 2020
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Client extends Application { 
	// I/O streams 
	private TextArea incoming = new TextArea();
	private TextField outgoing;
	private TextField username;
	private TextField password;
	private BufferedReader reader;
	private PrintWriter writer;
	private Button submit = new Button("Submit");
	private Button send;
	private Button history;
	private Button view;
	private Button search;
	private Button quit;
	private ComboBox dropdown = new ComboBox();
	private boolean login = false;
	GsonBuilder builder = new GsonBuilder();
	Gson gson = builder.create();
		
	@Override
	public void start(Stage primaryStage) {
		try {
			setUpNetworking();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Text text1 = new Text("Username");   
		Text text2 = new Text("Password"); 
		username = new TextField();         
		password = new TextField();  

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
		    if(!username.getText().equals("")) {
				Message msg = new Message("credentials", username.getText(), password.getText());
			    writer.println(gson.toJson(msg));
			    writer.flush();
			    try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		    
		    if(login) {
		    	Stage stage = (Stage) submit.getScene().getWindow();
		    	stage.close();
		    
				// outgoing
				BorderPane paneForTextField = new BorderPane(); 
				paneForTextField.setPadding(new Insets(10, 10, 10, 10)); 
				paneForTextField.setStyle("-fx-border-color: green"); 
				paneForTextField.setLeft(new Label("Enter a bid: ")); 
				outgoing = new TextField();
				outgoing.setAlignment(Pos.CENTER);
				paneForTextField.setCenter(outgoing);
	
				// control panel
				GridPane paneForControls = new GridPane();
			    ColumnConstraints column1 = new ColumnConstraints();
			    column1.setPercentWidth(20);
			    ColumnConstraints column2 = new ColumnConstraints();
			    column2.setPercentWidth(20);
			    ColumnConstraints column3 = new ColumnConstraints();
			    column3.setPercentWidth(15);
			    ColumnConstraints column4 = new ColumnConstraints();
			    column4.setPercentWidth(15);
			    ColumnConstraints column5 = new ColumnConstraints();
			    column5.setPercentWidth(15);
			    ColumnConstraints column6 = new ColumnConstraints();
			    column5.setPercentWidth(15);
			    paneForControls.getColumnConstraints().addAll(column1, column2, column3, column4, column5, column6);
				paneForControls.setMinSize(600, 50);
				paneForControls.setPadding(new Insets(10, 10, 10, 10));
				paneForControls.setVgap(5); 
				paneForControls.setHgap(5);
				paneForControls.setAlignment(Pos.CENTER);
				
				// incoming
				BorderPane mainPane = new BorderPane();
				incoming.setPrefSize(700, 700);
				mainPane.setCenter(new ScrollPane(incoming)); 
				mainPane.setTop(paneForControls);
				
				// items list and text field
				paneForControls.add(dropdown, 0, 0);
				paneForControls.add(outgoing, 1, 0);
				GridPane.setHalignment(dropdown, HPos.CENTER);
				GridPane.setHalignment(outgoing, HPos.CENTER);
				
				//buttons
				send = new Button("Send");
				send.setAlignment(Pos.CENTER);
				send.setMinWidth(75);
				paneForControls.add(send, 2, 0);
				history = new Button("History");
				history.setMinWidth(75);
				history.setAlignment(Pos.CENTER);
				paneForControls.add(history, 4, 0);
				view = new Button("View");
				send.setAlignment(Pos.CENTER);
				send.setMinWidth(50);
				paneForControls.add(view, 5, 0);
				search = new Button("Search");
				search.setAlignment(Pos.CENTER);
				search.setMinWidth(50);
				paneForControls.add(search,  3, 0);
				GridPane.setHalignment(send, HPos.CENTER);
				GridPane.setHalignment(history, HPos.CENTER);
				GridPane.setHalignment(view, HPos.CENTER);
				GridPane.setHalignment(search, HPos.CENTER);
				
				// quit button
				quit = new Button("Quit");
				quit.setMinSize(50, 25);
				quit.setAlignment(Pos.CENTER);
				mainPane.setBottom(quit);
				mainPane.setAlignment(quit, Pos.CENTER);
				
				Stage secondStage = new Stage();
				Scene scene2 = new Scene(mainPane, 700, 700); 
				secondStage.setTitle("Client"); // Set the stage title 
				secondStage.setScene(scene2); // Place the scene in the stage 
				secondStage.show(); // Display the stage   
		        
				outgoing.setOnAction(e -> {
					send.fire();
				});
				
				send.setOnAction(r -> {
					if(!(dropdown.getValue() == null)) {
						Message message = new Message("bid", (String)dropdown.getValue(), Integer.parseInt(outgoing.getText()));
						writer.println(gson.toJson(message));
						writer.flush();
						outgoing.setText("");
						outgoing.requestFocus();
					}
					else {
						Message message = new Message("refresh", "");
						writer.println(gson.toJson(message));
						writer.flush();
					}
					click();
				});
				
				history.setOnAction(z -> {
					Message message = new Message("history", "");
					writer.println(gson.toJson(message));
					writer.flush();
					click();
				});
				
				view.setOnAction( p -> {
					Message message = new Message("refresh", "");
					writer.println(gson.toJson(message));
					writer.flush();
					click();
				});
				
				search.setOnAction(w -> {
					Message message = new Message("search", (String)dropdown.getValue());
					writer.println(gson.toJson(message));
					writer.flush();
					click();
				});
				
				quit.setOnAction(f -> {
					secondStage.close();
					click();
				});
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
		Message msg = gson.fromJson(message, Message.class);
		String tmp = "";
		switch(msg.type) {
		case "items":
			for(int i = 0; i < msg.items.size(); i++) {
				dropdown.getItems().add(msg.items.get(i).getName());
			}
			break;
		case "update":
			if(!msg.message.equals("")) {
				tmp = msg.message + "\n\n";
			}
			for(int i = 0; i < msg.items.size(); i++) {
				tmp += msg.items.get(i).toString();
			}
			break;
		case "notification":
			tmp = msg.message;
			break;
		case "verification":
			if(msg.message.equals("success")) {
				login = true;
			}
		}
		if(!msg.type.equals("items") && !msg.type.equals("verification")) {
			String out = tmp;
			Platform.runLater(() -> {
				incoming.clear();
				incoming.appendText(out + "\n");
			});
		}
	}
	
	private void click() {
        Media media = new Media(new File("click.m4a").toURI().toString());  
        MediaPlayer mediaPlayer = new MediaPlayer(media);  
        mediaPlayer.play();
	}
	
	class Reader implements Runnable {
		@Override
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					processRequest(message);
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
