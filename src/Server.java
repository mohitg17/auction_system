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
import java.util.Timer;
import java.util.TimerTask;

public class Server extends Observable {
	
    ArrayList<Item> items = new ArrayList<Item>();
    ArrayList<String> last_bidder = new ArrayList<String>();
    ArrayList<ArrayList<String>> bids = new ArrayList<ArrayList<String>>();
    static int client_number = 1;
    static boolean over = false;

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
    	for(int i = 0; i < items.size(); i++) {
    		last_bidder.add(null);
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
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				  @Override
				  public void run() {
				    endAuction();
				  }
				}, 30000);
		}
    }
    
    public void processRequest (ClientHandler client, String message) {
    	String output = "invalid input\n";
    	String[] inputs = message.split(",");
    	if(inputs.length == 2) {
    		for(Item i : items) {
    			if(i.getName().equals(inputs[0])) {
    				Item selected = i;
    				if(i.getCurrentBid() < Integer.parseInt(inputs[1])) {
	    				i.setCurrentBid(Integer.parseInt(inputs[1]));
	    				output = "Bid for " + i.getName() + " was processed. Current bid is now " + i.getCurrentBid() + "\n";
	    				last_bidder.set(items.indexOf(i), client.name);
	    				bids.get(Integer.parseInt(client.name.substring(client.name.length()-1))).add("Bid " + i.getCurrentBid() + " on: " + i.getName());
    				}
    				else {
    					output = "invalid bid amount. Current bid for item is higher than your bid\n";
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
    
    public void endAuction() {
    	String output = "The auction has ended. Winners are listed below:\n";
    	for(Item i : items) {
    		if(last_bidder.get(items.indexOf(i)) == null) {
    			output += i.getName() + ": there were no bids for this item\n";
    		}
    		else {
    			output += i.getName() + ": " + last_bidder.get(items.indexOf(i)) + " won this item\n";
    		}
    	}
    	try {
    		this.setChanged();
    		this.notifyObservers(output);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	over = true;
    }
    
	class ClientHandler implements Runnable, Observer {
		private Server server;
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;
		private String name;
		
		public ClientHandler(Server server, Socket socket) {
			name = "Client" + client_number;
			client_number++;
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
					if(!over) {
						System.out.println("read " + message);
						server.processRequest(this, message);
					}
					else {
						server.processRequest(this, "This item is no longer accepting bids");
					}
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
