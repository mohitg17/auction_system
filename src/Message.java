import java.util.ArrayList;

public class Message {
	String type;
	String message;
	String bid_item;
	int amount;
	ArrayList<Item> items;
	
	public Message(String type, String message, ArrayList<Item> items) {
		this.type = type;
		this.message = message;
		this.items = items;
	}
	
	public Message(String type, String bid_item, int amount) {
		this.type = type;
		this.bid_item = bid_item;
		this.amount = amount;
	}
	
	public Message(String type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public Message() {
		
	}
	
}
