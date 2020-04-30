/*
 * Author: Vallath Nandakumar and the EE 422C instructors.
 * Data: April 20, 2020
 * This starter code assumes that you are using an Observer Design Pattern and the appropriate Java library
 * classes.  Also using Message objects instead of Strings for socket communication.
 * See the starter code for the Chat Program on Canvas.  
 * This code does not compile.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;
import javafx.application.Platform;

public class Server extends Observable {
	
    static Server server;
    ArrayList<Item> items = new ArrayList<Item>();
//	private ArrayList<PrintWriter> clientOutputStreams;


    public static void main (String [] args) {
        server = new Server();
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
//    	clientOutputStreams = new ArrayList<PrintWriter>();
		new Thread( () -> { 
			try {  // Create a server socket 
				@SuppressWarnings("resource")
				ServerSocket serverSocket = new ServerSocket(8000);
				while (true) { 
					Socket socket = serverSocket.accept();
					System.out.println("got a connection");
					new Thread(new ClientHandler(socket)).start();
//					new Thread(() -> {
//						System.out.println("got a connection");
//						new Thread(new ClientHandler(socket)).start();
//					}).start();
				}
			}
			catch(IOException ex) { 
				System.err.println(ex);
			}
		}).start();
    }
	
	class ClientHandler implements Runnable {
		private Socket socket;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				while(true) {
					String message = inputFromClient.readUTF();
					System.out.println("read " + message);
					outputToClient.writeChars("received");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
