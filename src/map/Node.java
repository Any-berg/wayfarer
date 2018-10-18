package map;

/* https://writing.stackexchange.com/questions/39419/how-to-give-cartography-information-in-a-fantasy-setting-without-being-too-preci */

import java.util.Vector;

public class Node {

	private static int		counter = 0;
	
	private final int		hashCode;
	public Vector<Node>		to;

	protected Node() {
		hashCode = counter++;
		assert hashCode < 62 : "higher node count is not supported yet";
		to = new Vector<Node>();
	}
	@Override public int hashCode() {
		return hashCode;
	}
	@Override public String toString() {// TODO: implement modulo-62 conversion
		return ""+(char)(hashCode+(
			hashCode < 10 ? 48-0 :			// 0-9
			hashCode < 36 ? 65-10 :			// A-Z
			hashCode < 62 ? 97-36 : -21));	// a-z
	}
	public Node resolve() {
		if (false) //TODO: randomize (undirected or loop)
			return this.withLinks();
		Node node = new Node();
		//System.out.println(this+":"+this.to+" "+node);
		node.to.add(this.to.remove(0));
		node.to.add(this);
		if (false) //TODO: randomize
			this.to.clear(); // make it a directed edge
		this.to.add(node);
		//System.out.println(this+":"+this.to+" "+node+":"+node.to+" "+node.to.get(0)+":"+node.to.get(0).to+" "+node.to.get(0).to.get(1)+":"+node.to.get(0).to.get(1).to);
		return this;
	}
	protected final Node withLinks() {
		for (int i = 4-this.to.size(); true && i > 0; i--) {// existing link count modulates creation of new ones 
			Node node = new Node();
			node.to.add(this);
			this.to.add(node);
			break; // for debugging
		}
		return this;
	}
}
