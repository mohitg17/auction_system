
public class Item {

	private String name;
	private String description;
	private int minCost;
	private int currentBid;
	private int buyNow;
	private int endTime;
	
	public Item() {
		
	}
	
	public Item(String[] s) {
		name = s[0];
		description = s[1];
		minCost = Integer.parseInt(s[2]);
		currentBid = minCost;
		buyNow = minCost*20;
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getBuyNow() {
		return buyNow;
	}

	public void setBuyNow(int buyNow) {
		this.buyNow = buyNow;
	}

	@Override
	public String toString() {
		return "Item Name: " + name + "\nItem Description: " + description + "\nMin Cost: " + minCost + 
				"\nCurrent Bid: " + currentBid + "\nBuy now: " + buyNow + "\nEnd Time: " + endTime + " seconds\n\n";
	}
	
}
