package part1;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BellmanFordShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
public class Project2 {
	
	public static String matchFinder(String s, String pattern) {
		try {
			Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            if (matcher.find()) {
                return matcher.group();
            } else {
                return "";
            }
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static List<String> findSysCall(String[] words) {
		String[] sysCalls = new String[]{"read", "write", "recvmsg", "sendmsg", "recvfrom"};
		List wordsList = Arrays.asList(words);
			for(String call: sysCalls) {
				if(wordsList.contains(call)) {
					int ind = Arrays.binarySearch(words, call);
					return Arrays.asList(String.valueOf(ind), call);
			}
		}
			return new ArrayList<>();
	}
	
	public static String fromNanos(long input) {
		Date date = new Date(input);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String myDate = format.format(date);
	    return "2022-12-01 18:56:06";
	}
	
	public static Graph<String, String> backtrack(Graph<String, String> graph, HashMap<String,Integer> visited, String poi, List<String> res, Graph<String, String> g) {
		 visited.put(poi, 1);
	     g.addVertex(poi);
	     List<String> pred = Graphs.predecessorListOf(graph, poi );
	     for( String p : pred){
	        	if (visited.containsKey(p)) {
	            if(visited.get(p)== 0){
	                res.add(p);
	                
	                g = backtrack(graph,visited,p,res,g);
	                g.addEdge(p, poi);
	            } 
	            else {
	            	visited.put(p, 0);
	            }
	            
	        	}
	        }
	        
			 return g;
	}
	
	public static void generateBacktrackGraph(Graph<String, String> btg) {
		Graph<String, String> graph = new DefaultDirectedGraph<>(String.class);
		HashMap<String, Integer> visited = new HashMap<String, Integer>();
		System.out.println("Enter point of Interest: ");
		Scanner scan = new Scanner(System.in);
		String poi = scan.nextLine();
		List<String> path = new ArrayList<String>();
		List<String> predecessor = Graphs.predecessorListOf(btg, poi );
        for (String p : predecessor) {
        	visited.put(p,0);
        }
        Graph<String, String> result = backtrack(btg, visited, poi, path, graph);
        DOTExporter<String, String> graphExporter=new DOTExporter<>();
		graphExporter.setVertexAttributeProvider((v) -> {
	            Map<String, Attribute> map = new HashMap<>();
	            map.put("label", DefaultAttribute.createAttribute(v));
	            return map;
	        });
		graphExporter.setEdgeAttributeProvider((v) -> {
	            Map<String, Attribute> map = new LinkedHashMap<>();
	            map.put("label", DefaultAttribute.createAttribute(v));
	            return map;
	        });
		try {
			graphExporter.exportGraph(result, new FileWriter("/Users/haritha/Desktop/SoftwareSecurity/src/part1/backtrackedGraph.dot"));
			System.out.println("Graph created");
		}
		catch (IOException e){
	  		 System.out.println(e);
	  	 }
	}
	
	public static void generatedGraph(List<HashMap<String, List<String>>> tupleList) {
		Graph<String, String> graph = new DefaultDirectedGraph<>(String.class);
		System.out.println(tupleList.size());
		for(int i=0; i< tupleList.size(); i++) {
			Map<String, List<String>> value = tupleList.get(i);
			String fileId = value.get("subject").get(0);
			graph.addVertex(fileId);
			int size = value.get("object").size();
		
			String execPath = value.get("object").get(size-1);
		
			String latency = value.get("object").get(size-2) == null ? "" : value.get("object").get(size-2);
		
			String timestamp = value.get("object").get(0);
		
			graph.addVertex(execPath);
			String events = "";
			if (!value.get("events").isEmpty()) {
				events = value.get("events").get(0);
			}
			if (events == "read" || events == "sendmsg") {
				graph.addEdge(execPath, fileId, timestamp+latency+execPath);
			}
			else {
				graph.addEdge(fileId, execPath, timestamp+latency+execPath);
			}
		}
		DOTExporter<String, String> graphExporter=new DOTExporter<>();
		graphExporter.setVertexAttributeProvider((v) -> {
	            Map<String, Attribute> map = new HashMap<>();
	            map.put("label", DefaultAttribute.createAttribute(v));
	            return map;
	        });
		graphExporter.setEdgeAttributeProvider((v) -> {
	            Map<String, Attribute> map = new LinkedHashMap<>();
	            map.put("label", DefaultAttribute.createAttribute(v));
	            return map;
	        });
		try {
			graphExporter.exportGraph(graph, new FileWriter("/Users/haritha/Desktop/SoftwareSecurity/src/part1/finalGraphWithEdges.dot"));
			generateBacktrackGraph(graph);
		}
		catch (IOException e){
	  		 System.out.println(e);
	  	 }
	}
	
	
	public static void main(String args[]) {
		File file = new File("filename.txt");
		List<HashMap<String, List<String>>> tripletList = new ArrayList<>();
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = buffer.readLine()) != null) {
				String[] words = line.split(" ");
				List<String> subject = new ArrayList<>();
				List<String> events = new ArrayList<>();
				List<String> object = new ArrayList<>();
				
				String timestamp = words[1];
				String pid = matchFinder(line, "\\([1-9]+[0-9]*\\)");
				pid = pid.substring(1, pid.length()-2);
				subject.add(pid);
				System.out.println(findSysCall(words));
				if (!findSysCall(words).isEmpty()) {
					events.add(findSysCall(words).get(0));
					events.add(findSysCall(words).get(1));
				}
				object.add(timestamp);
				if (matchFinder(line, "fd=\\d\\(<\\w>\\)") != "") {
					object.add(matchFinder(line, "fd=\\d\\(<\\w>\\)"));
				}
				else {
					object.add(matchFinder(line, "res=\\d"));
				}
				
				object.add(matchFinder(line, "latency=\\d"));
				object.add(words[words.length - 1].split("exepath=")[1]); 
				HashMap<String, List<String>> triplet = new HashMap<>();
				triplet.put("subject", subject);
				triplet.put("events", events);
				triplet.put("object", object);
				tripletList.add(triplet);
//				System.out.println(tripletList);
			}
			generatedGraph(tripletList);
			
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}
