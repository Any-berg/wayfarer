package map;

public class Goal extends Node {

	@Override public Node resolve() {
		Node node = new Node();
		node.to.add(this);
		node.to.addAll(this.to);
		for (Node n : this.to) {
			n.to.remove(this);
			n.to.add(node);
		}
		this.to.clear();
		this.to.add(node);
		//System.out.println(this+":"+this.to+" "+node+":"+node.to+" "+node.to.get(0)+":"+node.to.get(0).to);
		return node.withLinks();
	}
}
