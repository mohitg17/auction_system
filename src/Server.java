/*
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * Mohit Gupta
 * mg58629
 * 16295
 * Spring 2020
 */
import java.io.BufferedReader;
import org.json.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class Server extends Observable {
	
    ArrayList<Item> items = new ArrayList<Item>();

    public static void main (String [] args) {
        Server server = new Server();
        server.populateItems();
        try {
			server.setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void populateItems() {
    	File f = new File("src/items.txt");
    	Scanner sc = null;
    	try {
			sc = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	while(sc.hasNextLine()) {
    		items.add(new Item(sc.nextLine().split(",")));
    	}
    }
    
    public void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(8000);
		while (true) { 
			Socket socket = serverSocket.accept();
			System.out.println("got a connection");
			ClientHandler handler = new ClientHandler(this, socket);
			handler.displayItems(items);
			this.addObserver(handler);
			new Thread(handler).start();
		}
    }
    
    public void processRequest (String message) {
    	String output = "invalid input\n";
    	String[] inputs = message.split(",");
    	if(inputs.length == 2) {
    		for(Item i : items) {
    			if(i.getName().equals(inputs[0])) {
    				Item selected = i;
    				if(i.getCurrentBid() < Integer.parseInt(inputs[1])) {
	    				i.setCurrentBid(Integer.parseInt(inputs[1]));
	    				output = "Bid for " + i.getName() + " was processed. Current bid is now " + i.getCurrentBid() + "\n";
    				}
    				else {
    					output = "invalid bid amount. Current bid for item is higher than your bid";
    				}
    			}
    		}
    	}
		for(Item j : items) {
			output += j.toString();
		}
    	try {
    		this.setChanged();
    		this.notifyObservers(output);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
	class ClientHandler implements Runnable, Observer {
		private Server server;
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		
		public ClientHandler(Server server, Socket socket) {
			this.server = server;
			this.socket = socket;
			try {
				reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				writer = new PrintWriter(this.socket.getOutputStream()); 
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		public void displayItems(ArrayList<Item> list) {
			String output = "";
			for(Item i : list) {
				output += i;
			}
			writer.print(output);
			writer.flush();
		}
		
		private void notifyClient(String message) {
			System.out.println("send message " + message);
			writer.println(message);
			writer.flush();
		}
		
		@Override
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					server.processRequest(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void update(Observable o, Object arg) {
			this.notifyClient((String) arg);
		}
	}
}
