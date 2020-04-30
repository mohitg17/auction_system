
public class Item {

	private String name;
	private String description;
	private int minCost;
	private int endTime;
	
	public Item() {
		
	}
	
	public Item(String[] s) {
		name = s[0];
		description = s[1];
		minCost = Integer.parseInt(s[2]);
		endTime = Integer.parseInt(s[3]);
	}
	
}
