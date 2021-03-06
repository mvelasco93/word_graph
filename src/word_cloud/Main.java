package word_cloud;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDOT;


public class Main{
	public static void main(String[] args) throws IOException{
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		String styleSheet =
				"node {" +
						"   fill-color: blue;" +
						"	size-mode: dyn-size;"+
						"	size: 1px;"+
						"	text-alignment: center;"+
						"	text-size: 8px;"+
						//"	text-mode: hidden;"+
						"}" +

			    "edge {"+
			    "	shape: line;"+
			    "	fill-color: #222;"+
			    "}";


		URL data = Main.class.getResource("bd_cleaned_first.csv");
		Graph tweets = new SingleGraph("Word Cloud");
		String filePath = "Venezuela_cleaned_words.dot";
		tweets.addAttribute("ui.stylesheet", styleSheet);
		tweets.addAttribute("ui.quality");
		tweets.addAttribute("ui.antialias");
		tweets.addAttribute("ui.screenshoot", "graph.png");
		tweets.addAttribute("layout.quality",5);


		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(data.getPath()));
			while ((line = br.readLine()) != null) {
				//Use comma as separator
				String[] tweet = line.split(cvsSplitBy);
				//Detects language for each tweet
				//words[4] hashtags[6]
				StringTokenizer tokens=new StringTokenizer(tweet[4], " ");
				String prev = "";
				while(tokens.hasMoreTokens()){
					String str=tokens.nextToken();	
					//Ignore if word refers to city location
					if (!str.isEmpty()){
						//If node doesn't exist
						if (tweets.getNode(str)== null){
							tweets.addNode(str).addAttribute("weight", 1);
							tweets.getNode(str).addAttribute("cons", 0);

							if(!prev.equals("")){
								int j = Integer.parseInt(tweets.getNode(str).getAttribute("cons").toString())+1;
								int k = Integer.parseInt(tweets.getNode(prev).getAttribute("cons").toString())+1;
								//Add edge to connect on tweet
								String edge_name = prev+str;
								tweets.addEdge(edge_name, prev, str);
								tweets.getEdge(edge_name).setAttribute("weight", 1);
								tweets.getNode(str).setAttribute("cons", j);
								tweets.getNode(prev).setAttribute("cons", k);
								prev = str;
							}
							prev = str;
						}
						//If node exists, modifies weight
						else{
							int i = tweets.getNode(str).getAttribute("weight");
							tweets.getNode(str).setAttribute("weight", i+1);
							//If node doesn't have prev node
							if(!prev.equals("")){
								String edge_name = prev+str;
								String edge_name2 = str+prev;
								//Find if edge doesn't exists
								if((tweets.getEdge(edge_name) == null) && (tweets.getEdge(edge_name2) == null)){
									int j = Integer.parseInt(tweets.getNode(str).getAttribute("cons").toString())+1;
									int k = Integer.parseInt(tweets.getNode(prev).getAttribute("cons").toString())+1;
									tweets.getNode(str).setAttribute("cons", j);
									tweets.getNode(prev).setAttribute("cons", k);
									tweets.addEdge(edge_name, prev, str);
									tweets.getEdge(edge_name).setAttribute("weight", 1);
								}else{
									//Add weight of edge if exists
									if(tweets.getEdge(edge_name) != null){
										int l = Integer.parseInt(tweets.getEdge(edge_name).getAttribute("weight").toString())+1;
										tweets.getEdge(edge_name).setAttribute("weight", l);
									}else{
										int m = Integer.parseInt(tweets.getEdge(edge_name2).getAttribute("weight").toString())+1;
										tweets.getEdge(edge_name2).setAttribute("weight", m);
									}
								}
							}
						}
					}
				}
			} 
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		for (Node n: tweets){
			n.addAttribute("ui.label", n.getId());
			double size = Integer.parseInt(n.getAttribute("weight").toString());
			size = size*0.5 +1;
			n.addAttribute("ui.size", size );

		}
		//display
		tweets.display();

		//save
		FileSinkDOT fs = new FileSinkDOT();
		fs.writeAll(tweets, filePath);

	}



}
