package map;

/* https://writing.stackexchange.com/questions/39419/how-to-give-cartography-information-in-a-fantasy-setting-without-being-too-preci */
/* https://rpg.stackexchange.com/questions/120565/how-should-i-go-about-stopping-metagaming-based-on-roll20-showing-unexplored-are */

import java.util.Vector;

public class Node implements Comparable<Node> {

	private final int			hashCode;
	public Vector<Node>			to;
	
	Node(int hashCode) {// "protected" would allow a subclass access even from a different package
		this.hashCode = hashCode;
		to = new Vector<Node>();
	}
	@Override public String toString() {
		return Graph.toString(hashCode);
	}
	@Override public int hashCode() {
		return hashCode;
	}
	@Override public int compareTo(Node node) {
		return this.hashCode-node.hashCode;
	}
}
