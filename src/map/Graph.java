package map;

/**
 * Generates strongly connected graphs. Tries to preserve planarity.
 *
 * https://en.wikipedia.org/wiki/Connectivity_(graph_theory)#Connected_graph
 * https://en.wikipedia.org/wiki/Planarity_testing#Construction_sequence_method
 */
import java.util.Vector;

public class Graph {

	private Vector<Node> nodes, unresolved;
	
	public Graph() {
		nodes = new Vector<Node>();
		unresolved = new Vector<Node>();
	}
	public void clear() {// viewing
		nodes.clear();
		unresolved.clear();
	}
	public Node getNode(Node n, boolean create) {// resolving
		Vector<Node> matches = getNodesMatching(n);
	    if (matches.size() > 0 && true) //TODO: randomize; select one (or none) of matching nodes
			return matches.get(0);
		if (!create)
			return n;
		Node node = new Node(nodes.size());
		nodes.add(node);
		return node;
	}
	private final Vector<Node> getNodesMatching(Node n) {
		assert unresolved.contains(n) : "only unresolved nodes can be matched";
		Vector<Node>	matches = new Vector<Node>(unresolved),
						nodesNextTo_n = getNodesNextTo(n),
						intersection;
		matches.remove(n);
		for (Node m : unresolved) // remove nodes that already share a neighbour with this one
			if (m != n) {
				intersection = getNodesNextTo(m);
				intersection.retainAll(nodesNextTo_n);
				if (!intersection.isEmpty())
					matches.remove(m);
			}
		for (Node m : n.to)
			matches.removeAll(m.to);
		matches.removeAll(n.to);
		matches.remove(nodes.get(0));
		//TODO: remove nodes that would break graph "planarity" (unless too complex to detect)
		return matches;
	}
	private final Vector<Node> getNodesNextTo(Node n) {
		assert unresolved.contains(n) : "listing the nodes next to a resolved node is more complex";
		Vector<Node> v = new Vector<Node>(n.to);
		if (n.to.size() == 1 && !n.to.get(0).to.contains(n))
			for (Node m : nodes)
				if (m.to.contains(n))
					v.add(m);
		return v;
	}
	public Node getNode() {// resolving and viewing
		for (Node node : unresolved)
			if (node.to.isEmpty())
				return node;
		Node node = new Node(nodes.size());
		nodes.add(node);
		unresolved.add(node);
		return node;
	}
	public Node getNode(int index) {
	/*	for (int i = nodes.size(); i <= index; i++) 
			nodes.add(new Node(i));
		return nodes.elementAt(index);
	/*/ for (int i = nodes.size(); i <= index; i++) 
			nodes.add(null);
		Node node = nodes.elementAt(index);
		if (node == null) {
			node = new Node(index);
			nodes.set(index, node);
		}
		return node; //*/
	}
	public Node getNode(String label) {// used by viewer (updating clicked graph)
		return getNode(toInt(label));
	}
	public Node unresolved(Node node) {
		unresolved.add(node);
		return node;
	}
	public Node resolve(Node n) {
		assert unresolved.contains(n) : "node "+n+" has already been resolved"; //TODO: check?
		assert n.to.size() >= 1 || n.hashCode() == 0 : "node "+n+" has no leaving edges";
		assert n.to.size() <= 2 : "node "+n+" has more than two leaving edges";
		/**
		 * number of edges (0-2)
		 * matching nodes (0-n)
		 */

		System.out.print(getNodesMatching(n));
		
		if (n.hashCode() == 0) {

			if (n.to.isEmpty() || true) {//TODO: randomize; resolve without creating a directed loop

				Node node = getNode(n, true);
			    unresolved.remove(node);
			    
			    if (n.to.size() == 1 && !n.to.get(0).to.contains(n)) {// if edge to node 0 is not easy
			    	for (Node m : nodes)
			    		if (m.to.remove(n)) {
			    			assert m != node : "new edge to node 0 was added before removing old one";
			    			m.to.add(node);
			    			break; // since only one node at a time can have an edge to node 0
			    		}
			    }
			    else
			    	for (Node m : n.to)
				        if (m.to.remove(n))
				            m.to.add(node);
			    
			    node.to.add(n);		    
			    node.to.addAll(n.to);
			    
			    n.to.clear();
			    n.to.add(node);
			    
			    return withLinks(node);
			}
			
			Node node = getNode(nodes.size());
			if (false) {//TODO: randomize; create directed loop but leave node 0 out of it
				Node l = getNode(nodes.size());
				unresolved.add(l);
			    if (n.to.size() == 1 && !n.to.get(0).to.contains(n)) {// if edge to node 0 is not easy
			    	for (Node m : nodes)
			    		if (m.to.remove(n)) {
			    			assert m != node : "new edge to node 0 was added before removing old one";
			    			m.to.add(node);
			    			l.to.addAll(n.to);
			    			break; // since only one node at a time can have an edge to node 0
			    		}
			    }
			    else
			    	for (Node m : n.to)
				        if (m.to.remove(n)) {
				            m.to.add(node);
				            l.to.add(m);
				        }
			    
			    node.to.add(n);		    
			    //node.to.addAll(n.to);
			    node.to.add(l);
			    if (true) //TODO: randomize; make edge within loop undirected (else directed)
			    	l.to.add(node);
			    
			    n.to.clear();
			    n.to.add(node);
			    
			    return withLinks(node);
			}
			if (n.to.size() == 1 && n.to.get(0).to.remove(n))
			    n.to.get(0).to.add(node);
			else 
				for (Node m : nodes)
					if (m.to.remove(n)) {
						m.to.add(node);
						if (n.to.remove(m))
							node.to.add(m);
						break;
					}
			node.to.add(n);
			if (false) //TODO: randomize; make edge to node 0 undirected (else directed)
				n.to.add(node);
			return withLinks(node);
		}
		//boolean isDirectedLoop = n.to.size() > 1 || !n.to.get(0).to.contains(n);
		//boolean random = n.to.size() > 1 || !n.to.get(0).to.contains(n);
		if (true) { //TODO: randomize; resolve without creating a directed loop
			Node m = getNode(n, false);
			unresolved.remove(m);
			if (m != n) {
				m.to.addAll(n.to);
				for (Node o : n.to)
					if (o.to.remove(n))
					    o.to.add(m);
				n.to.clear();
			}
			return withLinks(m);
		}
		
		unresolved.remove(n);
		
		Node node = getNode(); //getNode(nodes.size());
		//unresolved.add(node);
		if (n.to.size() == 1)
        	node.to.add(n.to.remove(0));
        else
		    for (Node m : n.to)
			    if (!m.to.contains(n)) {
				    node.to.add(m);
				    n.to.remove(m);
				    break;
			    }
		if (true) //TODO: randomize; have edge to next unresolved node be undirected (else directed)
			node.to.add(n);
		n.to.add(node);
		return n;
	}
	private final static boolean isInDirectedLoop(Node n) {
		return n.to.size() > 1 || !n.to.get(0).to.contains(n);
	}
	private Node withLinks(Node n) {
		int i = nodes.size() == 4 ? 3 : nodes.size() < 9 ? 2 : nodes.size() < 10 ? 1 : 0;
		System.out.print("["+unresolved.size()+"]");
		for (; true && i > 0; i--) {//TODO: randomize; existing link count modulates creation of new ones 
			Node node = getNode(); //getNode(nodes.size());
			node.to.add(n);
			n.to.add(node);
		}
		return n;
	}
	
	public String toString() {
		String s1 = new String(), edges = new String();
		unresolved.sort(null);
		for (Node n : unresolved)
			s1 += ","+n;
		for (Node n : nodes)
			for (Node m : n.to)
				if (m.to.contains(n)) // sort undirected edges by culling duplicates
				    edges += n.hashCode() < m.hashCode() ? ","+n+"-"+m : "";
				else
					edges += ","+n+">"+m;
		return s1.substring(1)+";\n"+(edges.length() > 0 ? edges.substring(1) : ""); // remove initial commas
	}
	public void addEdge(Node node1, Node node2, boolean isDirected) {// used by viewer (opening graphs)
		node1.to.add(node2);
		if (!isDirected)
			node2.to.add(node1);
	}

	private final static int RADIX = 36;
	
	public final static String toString(int i) {
		return Integer.toString(i, RADIX);
	}
	public final static int toInt(String s) {
		return Integer.parseInt(s, RADIX);
	}
}
