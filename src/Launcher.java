import java.awt.Dimension;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;

import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;

public class Launcher implements ViewerListener, Runnable {
	private Graph					graph;
	private ViewerPipe				pipe;
	private boolean					loop = true;
	private int						clicks = 0;
	private HashMap<Node,map.Node>	map = new HashMap<Node,map.Node>();
	
	public Launcher() {
		graph = new SingleGraph("embedded");
		graph.addAttribute("ui.stylesheet", "url('file://"+System.getProperty("user.dir")+"/stylesheet.css')");

		map.Node value = new map.Goal();
		Node key = graph.addNode(value.toString());
		map.put(key, value);
		key.addAttribute("ui.label", value.toString());
		key.addAttribute("ui.class", "goal, unresolved"); // order matters
	}
/*	private final Node add(String id) {
		Node node = graph.addNode(id);
		node.addAttribute("ui.label", id);
		node.addAttribute("ui.class", "unresolved");
		return node;
	}//*/
/*	private final Node add() {
		map.Node value = new map.Node();
		Node key = graph.addNode(value.toString());
		map.put(key, value);
		key.addAttribute("ui.label", value.toString());
		key.addAttribute("ui.class", "unresolved");
		return key;
	}//*/

	
	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		Launcher launcher = new Launcher();
		
		JFrame frame = new JFrame("Wayfarer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(200,200));
		frame.add(launcher.getView());
		frame.pack();
		frame.setVisible(true);
		
		launcher.run();
		System.out.println("Closed?");

	}
	
	private final ViewPanel getView() {
		System.out.println("Initializing Viewer");
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.enableAutoLayout();
		pipe = viewer.newViewerPipe();
		pipe.addSink(graph);
		pipe.addViewerListener(this);
		return viewer.addDefaultView(false);
	}
	
	@Override public void viewClosed(String viewName) {
		System.out.println("Closing "+viewName);
		loop = false;
	}
	@Override public void buttonPushed(String id) {
	}
	private Node add(map.Node value) {
		String id = value.toString();
		Node key = graph.getNode(id);		
		if (key == null) {
			System.out.println("ADDing node "+id);
			key = graph.addNode(id);
			map.put(key, value);
			key.addAttribute("ui.label", value.toString());
			key.addAttribute("ui.class", "unresolved");
			for (map.Node n : value.to) {
				if (!map.values().contains(n)) {
					id = n.toString();
					System.out.println("Adding node "+id);
					Node node = graph.addNode(id);
					map.put(node, n);
					node.addAttribute("ui.label", id);
					node.addAttribute("ui.class", "unresolved");
				}
				if (n.to.contains(value)) {
					System.out.println("+"+value+n);
					graph.addEdge(value+"-"+n, value.toString(), n.toString());
				} else {
					if (value.to.size() > 1) {
						System.out.println("~"+n+value.to.get(1)+" +"+value+n+" "+value.to);
						// if there is an undirected edge, replace it with a directed one
						if (graph.removeEdge((Edge)graph.getNode(n.toString()).getEdgeBetween(value.to.get(1).toString())) != null)
							graph.addEdge(n+"-"+value.to.get(1), n.toString(), value.to.get(1).toString(), true);
					}
					graph.addEdge(value+"-"+n, value.toString(), n.toString(), true);
				}
			}
		}
		else
			System.out.println(key+" EXISTS");
		return key;
	}
	private Node node(map.Node value) {
		String id = value.toString();
		Node key = graph.getNode(id);
		if (key == null) {
			key = graph.addNode(id);
			map.put(key, value);
			key.addAttribute("ui.label", value.toString());
			key.addAttribute("ui.class", "unresolved");
		}
		return key;
	}
	@Override public void buttonReleased(String id) {
		if (clicks++ > 0) {// i.e. doubleclick
			Node node = graph.getNode(id);
			String classes = node.getAttribute("ui.class");
			if (classes != null) {
				Vector<String[]> v = new Vector<String[]>();
				System.out.print("Resolving node "+id);
				map.Node resolved = map.get(node).resolve();
				System.out.println(resolved.toString().equals(id) ? "" : " as "+resolved);
				node(resolved).removeAttribute("ui.class");
                for (Edge e : node.getEachLeavingEdge()) {
                	if (e == null)
                		System.out.println("WTF");
                	map.Node n = map.get(e.getOpposite(node));
                	if (!resolved.toString().equals(id)) {
                		System.out.print(id+"!>"+n+", ");
                		graph.removeEdge(e);
                		continue;
                	}
                	if (n != null && !resolved.to.contains(n)) {// && n.to.contains(resolved)) {
						System.out.print(resolved+"!>"+n); //.get(0).to.get(0).to);
						//v.add(new String[] { e.getId() });
						graph.removeEdge(e);
						if (resolved.to.lastElement().to.contains(n)) {//(n.to.contains(resolved)) {
							if (!e.isDirected()) {
								System.out.print(", "+n+"->"+node);
								//v.add(new String[] { n.toString(), resolved.toString() });
								graph.addEdge(n+"-"+resolved, n.toString(), resolved.toString(), true);
							}
							n = resolved.to.lastElement();
							System.out.print(", "+n+"->"+n.to.get(0));
							//v.add(new String[] { node(n).toString(), n.to.get(0).toString() } );
							graph.addEdge(node(n)+"-"+n.to.get(0), n.toString(), n.to.get(0).toString(), true);
						}
						else if (!e.isDirected()) {
							System.out.print(", "+n+"->"+resolved);
							//v.add(new String[] { n.toString(), resolved.toString() });
							graph.addEdge(n+"-"+resolved, n.toString(), resolved.toString(), true);
						}
						System.out.println();
					}
                }
            /*	for (String[] edge : v)
                	if (edge.length == 2)
                		graph.addEdge(edge[0]+"-"+edge[1], edge[0], edge[1], true);
                	else
                		graph.removeEdge(edge[0]); //*/
                node = node(resolved);
				for (map.Node n : resolved.to)
					if (!node.hasEdgeBetween(n.toString())) {
						System.out.println(resolved+"--"+n);
						graph.addEdge(resolved+"-"+node(n), resolved.toString(), n.toString());	
				    }
				if (true)
					return;
				
				
				for (Edge e : node.getEachLeavingEdge())
					if (e.isDirected()) {
						System.out.println("* "+resolved.to);
						graph.removeEdge(e);
						//add(resolved.to.get(0));
						//graph.addEdge(node+"-"+resolved.to.get(0), node.toString(), resolved.to.get(0).toString());
						//System.out.println("? "+node+" "+resolved+" "+resolved.to);
					}
					else
						System.out.println(">>"+e.getOpposite(node)+" "+resolved.to);
				if (!resolved.toString().equals(id)) {
					System.out.println(id+" -> "+resolved);
					for (Edge edge : node.getEachEdge()) // detach Goal from prior connections
						graph.removeEdge(edge);
					add(resolved).removeAttribute("ui.class");
					return;
				}
			/*	else
					for (Edge edge : node.getEachEdge())
						graph.removeEdge(edge); //*/
				node.removeAttribute("ui.class");
				for (map.Node n : resolved.to) {
					add(n);
				}
				
			/*	System.out.println("resolving node "+id);
				if (classes.matches("(^|.*[, ])goal($|[, ].*)")) {
					Node m = add();
					m.changeAttribute("ui.class", "goal, unresolved");
					graph.addEdge(id+n.toString(), id, m.toString(), true);	
				}//*/
				//System.out.println(map);
			}
		}
	}
	@Override public void run() {
        do {
        	clicks = 0;
            pipe.pump();
            try { Thread.sleep(300); } catch (Exception e) {}
        } while (loop);
		
	}
}