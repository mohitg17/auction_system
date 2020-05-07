
public class Item {

	private String name;
	private String description;
	private int minCost;
	private int currentBid;
	private int endTime;
	
	public Item() {
		
	}
	
	public Item(String[] s) {
		name = s[0];
		description = s[1];
		minCost = Integer.parseInt(s[2]);
		currentBid = minCost;
		endTime = Integer.parseInt(s[3]);
	}
	
	public int getCurrentBid() {
		return currentBid;
	}

	public void setCurrentBid(int currentBid) {
		this.currentBid = currentBid;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Item Name: " + name + "\nItem Description: " + description + "\nMin Cost: " + minCost + "\nCurrent Bid: " + currentBid + "\nEnd Time: " + endTime + " seconds\n\n";
	}
	
}
