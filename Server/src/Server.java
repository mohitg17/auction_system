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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import com.google.gson.*;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class Server extends Observable {
	
    ArrayList<Item> items = new ArrayList<Item>();
    ArrayList<String> last_bidder = new ArrayList<String>();
    ArrayList<ArrayList<String>> bids = new ArrayList<ArrayList<String>>();
    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> passwords = new ArrayList<String>();
    String previous_message = "";
    String history = "";
    boolean display_history = false;
	GsonBuilder builder = new GsonBuilder();
	Gson gson = builder.create();
	StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    static int client_number = 0;
    static boolean over = false;
    Object lock = new Object();
    Object lock2 = new Object();
    long time = System.currentTimeMillis();
    int max_time = 0;

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
    	for(Item i : items) {
    		if(i.getRemainingTime() > max_time) {
    			max_time = i.getRemainingTime();
    		}
    	}
    	try {
	        File file = new File("history.txt");
	        Scanner scan = new Scanner(file);
	        while (scan.hasNextLine()) {
	          history += scan.nextLine() + "\n";
	        }
	        scan.close();
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    public void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(8000);
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					refresh();
				}
			}
		}).start();
		while (true) { 
			Socket socket = serverSocket.accept();
			System.out.println("got a connection");
			ClientHandler handler = new ClientHandler(this, socket);
			bids.add(new ArrayList<String>());
			this.addObserver(handler);
			new Thread(handler).start();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				  @Override
				  public void run() {
				    endAuction();
				  }
				}, max_time*1000);
		}
    }
    
    public void processRequest (ClientHandler client, Message message) {
    	String output = "invalid input\n";
    	Message out = new Message("notification", "invalid input");
    	if(message.type.equals("bid")) {
    		for(Item i : items) {
    			if(i.getName().equals(message.bid_item)) {
    				if(!(i.getRemainingTime() <= 0)) {
    					if(i.getCurrentBid() < message.amount) {
    						if(message.amount == i.getBuyNow()) {
    							output = client.name + " purchased " + i.getName() + " for $" + message.amount;
        						bids.get(Integer.parseInt(client.name.substring(client.name.length()-1))).add("purchased " + i.getName() + " for $" + message.amount);
    							i.setCurrentBid(-1);
    						}
    						else {
    							i.setCurrentBid(message.amount);
    							output = "Bid for " + i.getName() + " was processed. Current bid is now " + i.getCurrentBid();
        						bids.get(Integer.parseInt(client.name.substring(client.name.length()-1))).add("Bid $" + message.amount + " on " + i.getName());
    						}
    						last_bidder.set(items.indexOf(i), client.name);
    					}
    					else {
    						output = "invalid bid amount. Current bid for item is higher than your bid";
    					}
    				}
    				else {
    					output = "Auction for " + message.bid_item + " is over.";
    				}
    			}
    		}
    		out = new Message("update", output, items);
    		previous_message = output;
        	try {
        		this.setChanged();
        		this.notifyObservers(gson.toJson(out));
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
    	}
    	else if(message.type.equals("history")) {
    		output = "";
    		for(int i = 0; i < bids.size(); i++) {
    			output += "Client" + i + " bids:\n";
    			for(int j = 0; j < bids.get(i).size(); j++) {
    				output += bids.get(i).get(j) + "\n";
    			}
    			output += "\n";
    		}
    		output += "\n" + history;
    		display_history = true;
    		out = new Message("notification", output);
    		client.writer.println(gson.toJson(out));
    		client.writer.flush();
    	}
    	else if(message.type.equals("credentials")) {
    		String status = "";
    		if(usernames.contains(message.username)) {
    			if(passwordEncryptor.checkPassword(message.password, passwords.get(usernames.indexOf(message.username)))) {
	    			int client_num = usernames.indexOf(message.username);
	    			client.name = "Client" + client_num;
	    			bids.remove(bids.size()-1);
	    			client_number--;
	    			status = "success";
    			}
    			else {
    				status = "failed";
    			}
    		}
    		else {
    			usernames.add(message.username);
    			String encryptedPassword = passwordEncryptor.encryptPassword(message.password);
    			passwords.add(encryptedPassword);
    			status = "success";
    		}
    		out = new Message("verification", status);
    		client.writer.println(gson.toJson(out));
    		client.writer.flush();
			client.sendItemList();
			client.displayItems(items);
    	}
    	else if(message.type.equals("refresh")) {
    		display_history = false;
    		Message msg = new Message("update", previous_message, items);
    		client.writer.println(gson.toJson(msg));
    		client.writer.flush();
    	}
    	else if(message.type.equals("search")) {
    		output = message.message + " Bids:\n";
    		for(int i = 0; i < bids.size(); i++) {
    			for(int j = 0; j < bids.get(i).size(); j++) {
    				if(bids.get(i).get(j).contains(message.message)) {
    					output += "Client" + i + " " + bids.get(i).get(j) + "\n";
    				}
    			}
    			output += "\n";
        		display_history = true;
        		out = new Message("notification", output);
        		client.writer.println(gson.toJson(out));
        		client.writer.flush();
    		}
    	}
    	else {
    		client.writer.println(gson.toJson(out));
    		client.writer.flush();
    	}
    }
    
    public void refresh() {
    	synchronized(lock) {
    		if(System.currentTimeMillis() - time >= 1000) {
    			time = System.currentTimeMillis();
    			for(Item i : items) {
    				if(i.getRemainingTime() > 0) {
    					i.setRemainingTime(i.getRemainingTime()-1);
    				}
    			}
    			if(!display_history && !over) {
	    			Message msg = new Message("update", previous_message, items);
	        		this.setChanged();
	        		this.notifyObservers(gson.toJson(msg));
    			}
    		}
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
    	Message msg = new Message("notification", output, items);
    	try {
    		this.setChanged();
    		this.notifyObservers(gson.toJson(msg));
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	over = true;
        try {
            File f = new File("history.txt");
            if (f.createNewFile()) {
              System.out.println("File created: " + f.getName());
            } else {
              System.out.println("File already exists.");
            }
            FileWriter writer = new FileWriter("history.txt", true);
    		output = "*****AUCTION HISTORY*****\n\n" + output + "\n";
    		for(int i = 0; i < bids.size(); i++) {
    			output += "Client" + i + " bids:\n";
    			for(int j = 0; j < bids.get(i).size(); j++) {
    				output += bids.get(i).get(j) + "\n";
    			}
    			output += "\n";
    		}
    		writer.write(output);
            writer.close();
         } 
        catch (IOException e) {
        	  e.printStackTrace();
         }
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
			Message msg = new Message("update", "", list);
			writer.println(gson.toJson(msg));
			writer.flush();
		}
		
		public void sendItemList() {
			String output = "";
			Message message = new Message("items", "", items);
			writer.println(gson.toJson(message));
			writer.flush();
		}
		
		private void notifyClient(String message) {
//			System.out.println("send message " + message);
			writer.println(message);
			writer.flush();
		}
		
		@Override
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					synchronized(lock) {
						//	System.out.println("read " + message);
						Message msg = gson.fromJson(message, Message.class);
						server.processRequest(this, msg);
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void update(Observable o, Object arg) {
			this.notifyClient((String) arg);
		}
	}
}
