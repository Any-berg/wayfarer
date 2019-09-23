import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;

/**
 * Generates strongly connected graphs. Tries to preserve planarity.
 * 
 * Current issues:
 * - generated graphs should not be nonplanar (not sure if they can be that, or merely "inconvenient")
 * 
 *
 */

public class Launcher implements ViewerListener, ActionListener, Runnable {
	
	private Graph					graph;
	private map.Graph				g;
	private ViewerPipe				pipe;
	private boolean					loop = true;
	private int						clicks = 0;
	private HashMap<Node,map.Node>	index = new HashMap<Node,map.Node>();
	
	public Launcher() {
		graph = new SingleGraph("embedded");
		g = new map.Graph();
		initialize();
	}
	private map.Node initialize() {
		graph.addAttribute("ui.stylesheet", "url('file://"+System.getProperty("user.dir")+"/stylesheet.css')");
		map.Node value = g.getNode();
		Node key = graph.addNode(value.toString());
		index.put(key, value);
		key.addAttribute("ui.label", value.toString());
		key.addAttribute("ui.class", "goal, unresolved"); // order matters
		return value;
	}

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		Launcher launcher = new Launcher();
		
		JFrame frame = new JFrame("Wayfarer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(200,200));
		frame.add(launcher.getView());
		frame.setJMenuBar(launcher.getMenuBar());
		frame.pack();
		frame.setVisible(true);
		
		launcher.run();
		System.out.println("Closed?");

	}
	private final JMenuBar getMenuBar() {
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem menuItem = new JMenuItem("New");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Open");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("Save");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menubar.add(menu);
		return menubar;
	}
	private final ViewPanel getView() {
		System.out.println("Initializing viewer");
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
	private Node node(map.Node value) {
		String id = value.toString();
		Node key = graph.getNode(id);
		if (key == null) {
			key = graph.addNode(id);
			index.put(key, value);
			key.addAttribute("ui.label", value.toString());
			key.addAttribute("ui.class", (value.hashCode() == 0 ? "goal,":"")+"unresolved");
		}
		return key;
	}
	@Override public void buttonReleased(String id) {
		if (clicks++ > 0) {
			Node node = graph.getNode(id);
			String classes = node.getAttribute("ui.class");
			if (classes != null) {
				System.out.print("Resolving node "+id);
				map.Node clicked = g.getNode(id), resolved = g.resolve(clicked);
				node(resolved).removeAttribute("ui.class");
				if (clicked == resolved) {
					System.out.println();
					for (Edge e : node.getEachLeavingEdge()) //TODO: test with
						if (e.isDirected())
							System.out.println("REMOVED "+graph.removeEdge(e));
						else if (!resolved.to.contains(g.getNode(e.getSourceNode().toString()))) {
							System.out.print("MODIFIED "+graph.removeEdge(e));
							System.out.println(" INTO "+graph.addEdge(e.getId(), e.getSourceNode().toString(), e.getTargetNode().toString(), true));
						}
				}
				else {
					System.out.println(" as "+resolved);
					for (Edge e : node.getEachLeavingEdge())
						if (e.isDirected())
							System.out.println("REMOVED "+graph.removeEdge(e));
					for (Edge e : node.getEachEdge()) {
						System.out.println("REMOVED "+graph.removeEdge(e));
						map.Node n = g.getNode(e.getOpposite(node).toString());
						if (n.to.contains(resolved))
							System.out.println("ADDED "+graph.addEdge(n+"-"+resolved, node(n), node(resolved), !resolved.to.contains(n))); //TODO: validate if always directed?
					}
				}
				for (map.Node n : resolved.to)
					if (!node(resolved).hasEdgeToward(n.toString())) {
						System.out.print("ADDED "+graph.addEdge(resolved+"-"+n, node(resolved), node(n), !n.to.contains(resolved)));
					    for (map.Node m : n.to)
					    	if (!node(n).hasEdgeToward(m.toString()))
					    	    System.out.print(" AND "+graph.addEdge(n+"-"+m, node(n), node(m), !m.to.contains(n)));
					    System.out.println();
					}
			}
			System.out.println(g);
		}
	}
	
	@Override public void run() {
		do {
        	clicks = 0;
            pipe.pump();
            try { Thread.sleep(400); } catch (Exception e) {}
        } while (loop);
	}
	
	@Override public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("New")) {
			System.out.println("Resetting graph");
			index.clear();
			graph.clear();
			g.clear();
			initialize();
		}
		else if (cmd.equals("Save")) {
			System.out.println(g);
		}
		else if (cmd.equals("Open")) {
			System.out.println("Resetting graph");
			index.clear();
			graph.clear();
			g.clear();
			open("test");
		}
	}
    private void open(String filename) {// no assumptions about sortedness 
    	HashMap<Integer,Node> index = new HashMap<Integer,Node>();
    	
    	String content;
    	Pattern p;
		Matcher m;
		try {
    		int offset = 0;
    		content = new String(Files.readAllBytes(Paths.get(filename)));
    		p = Pattern.compile("\\G(\\d+)(,\\s*)?");
    		m = p.matcher(content);
    	    while (m.find()) {
    			int i = map.Graph.toInt(m.group(1));
    			System.out.println("Creating unresolved node "+i);
    			Node node = addNode(i, true);
    			index.put(i, node);
     		    offset = m.end();
     		}
    		if (content.charAt(offset++) != ';')
    			throw new IOException("unexpected '"+content.charAt(offset-1)+"'");
    		content = content.substring(offset);
    		p = Pattern.compile("\\G\\s*(\\d+) *([->]) *(\\d+),?");
    		m = p.matcher(content);
    		offset = 0;
    		while (m.find()) {
    			int i = map.Graph.toInt(m.group(1));
    			Node from = index.get(i), to;
    			if (from == null) {
    				System.out.println("Creating resolved node "+i);
    				index.put(i, from = addNode(i, false));
    			}
    			i = map.Graph.toInt(m.group(3));
    			to = index.get(i);
    			if (to == null) {
       				System.out.println("Creating resolved node "+i);
       				index.put(i, to = addNode(i, false));
    			}
    			boolean isDirected = m.group(2).equals(">");
    			graph.addEdge(from.toString()+"-"+to.toString(), from, to, isDirected);
    			g.addEdge(g.getNode(from.toString()), g.getNode(to.toString()), isDirected);
    			System.out.println("Adding edge from node "+m.group(1)+" to node "+m.group(3));
    			if (!isDirected)
    				System.out.println("Adding edge from node "+m.group(3)+" to node "+m.group(1));
    		    offset = m.end();
    		}
    		if (content.length() != offset)
    			throw new IOException("unexpected '"+content.substring(offset)+"'");
    	} catch (IOException e) { e.printStackTrace(); }
		
    	graph.addAttribute("ui.stylesheet", "url('file://"+System.getProperty("user.dir")+"/stylesheet.css')");
    	System.out.println(g);
    }
    
    private Node addNode(Integer i, boolean isUnresolved) {
    	//map.Node n = new map.Node(i);
    	map.Node n = g.getNode(i);
    	Node node = graph.addNode(n.toString());
    	index.put(node, n);
		node.addAttribute("ui.label", i.toString()); //TODO:
		if (isUnresolved) {
		    node.addAttribute("ui.class", (i == 0 ? "goal,":"")+"unresolved");
		    g.unresolved(n);
		}
    	return node;
    }//*/
}
