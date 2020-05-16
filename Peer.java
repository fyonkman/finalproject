import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.util.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.InetAddress;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream; 
import java.io.BufferedInputStream;

// Fiona and Emma 2020
/*
  Running Questions:
*/
/*
  To Do:
  - how to piece together spilt file
  - how to send pieces accross the various peers 
*/

public class Peer {

    // make port number a final int global var? 
    
    static HashMap<String, XmlRpcClient> peerList;

    /*
      Creates a peer connection to 'host', adds connection to PeerList  
     */
    public static int connect(String host, String myName) {

	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	try {
	    // connect to host on port 9173
	    config.setServerURL(new URL("http://"+host+":" + 9173));
	    XmlRpcClient peer = new XmlRpcClient();
	    peer.setConfig(config);
	    peerList.put(host, peer);
	    System.out.println(myName + "is successfully connected to " + host);
	    
	} catch (Exception e) {   
	    System.out.println("Problem connecting to " + host);
	    return 0; 
	}      
	return 1;
    }

    /*
      Test method: prints out and returns hello
     */
    public String sayHello(String peer) {
	try {
	    System.out.println("saying hello to "+peer);
	    return "hello "+peer+"!";
	} catch (Exception e) {
	    System.out.println("Problem connecting to server!");
	}
	return "";
    }    

    /*
    // locate file, read contents of file, return it
    public String get(String fileName) {
	System.out.println("In send");
	String result = "";
	try{
	    File file = new File("./test.txt");
	    if(file.exists()){
		Scanner sc = new Scanner(file);

		while (sc.hasNextLine()){
		    result = result + sc.nextLine();
		}
	    } else {
		System.out.println("File not found.");
	    }
	} catch (Exception e) {
	    System.out.println("Problem with get method.");
	}
	return result;
    }
    */
    
    /*
      Splits input of file into numNodes, sends to intermediary nodes via passFile method
     */
    public String splitFile(String fileName, int numNodes) {
	try {
	    File file = new File("./" + fileName);

	    // get size of file and determine size of each file chunk
	    long size = file.length();
	    int bytesPerSplit = ((int)size)/numNodes;
	    byte[] buffer = new byte[bytesPerSplit];

	    // create buffered input stream based on file, split and send contents by chunk sizes
	    try (FileInputStream fis = new FileInputStream(file);
		 BufferedInputStream bis = new BufferedInputStream(fis)) {

		int nodesSent = 0;
		// bytes read in 
		int bytesAmount = 0;

		while ((bytesAmount = bis.read(buffer)) > 0) {		    		   
		    String output = new String(buffer);

		    //*** NEED TO HANDLE CASE WHEN SIZE OF FILE IS EXACTLY DIVISIBLE SO NO REMAINDER
		    // Add to the if statement: & ((bytesAmount = bis.read(buffer)) != 0) and remove first line after if statement?
		    // if last node, tack on remainder of bytes from file
		    if(nodesSent == numNodes-1) {
			bytesAmount = bis.read(buffer);
			String end = new String(buffer);
			end = end.substring(0, bytesAmount);
			output = output + end;
			System.out.println(output);
			break;
		    }		    

		    // Here I think we go ahead and send by calling pass file :) 
		    System.out.println(output); 
		    nodesSent++;
		}
	    }	
	} catch (Exception e) {
	    System.out.println("Problem splitting file.");
	}
	return "splitFile complete!";
    }

    /*
      Intermediary method that passes input through middle node to the destination node
     */
    public String passFile(String splitFile, String destinationPeer) {
	try{
	    System.out.println("beginning of pass on file");
	    Object obj = null; 
	    Object[] params = {splitFile};
	    // get destination peer from peerList 
	    XmlRpcClient destPeer = peerList.get(destinationPeer);
	    // pass input to final node as parameter 
	    obj = destPeer.execute("peer.receive", params);
	    System.out.println("passed on file");
	} catch (Exception e) {
	    System.out.println("Problem passing the file.");
	    System.err.println("PassFile: " + e);
	}
	return "passFile complete!"; 
    }

    /*
      Receives input, places into hashmap, to be reasssembled at the end 
     */
    public String receive(String splitFile) {
	System.out.println(splitFile);
	// add pieces to hashmap of final file pieces
	// if size of hashmap is equal to total number of pieces, print out final output and done!
	return "Receive done!"; 
    }
    
    /*
      Creates server for the peer, handles command line input of.... (NEED TO FILL IN BASED ON FINAL COMMANDS WE CHOOSE) 
     */
    public static void main (String [] args) {
	//could read in peer hostnames if desired
	//int num = Integer.valueOf(args[0]).intValue();
	//int port = Integer.valueOf(args[1]).intValue();
	try {
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    XmlRpcServer xmlRpcServer;

	    // Set up the webserver
	    WebServer server = new WebServer(9173);
	    xmlRpcServer = server.getXmlRpcServer();
	    phm.addHandler("peer", Peer.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();
	    System.out.println("Peer started successfully on port 9173 on "+InetAddress.getLocalHost().getHostName());

	    //initialize peer hashmap
	    peerList = new HashMap<>();

	    Scanner in = new Scanner(System.in);
	    while(true) {
		String s = in.nextLine();
		StringTokenizer st = new StringTokenizer(s);
		String first = st.nextToken();
		if (first.equals("connect")) {
		    connect(st.nextToken(), InetAddress.getLocalHost().getHostName());
		} else if (first.equals("hello")) {
		    Object obj = null;
		    Object[] params = {"DEONI"};
		    //obj = peer.execute("peer.sayHello", params);
		    System.out.println(obj);
		} else if (first.equals("get")) {
		    Object obj = null;
		    Object[] params = {st.nextToken()};
		    //obj = peer.execute("peer.get", params);
		    System.out.println(obj);
		} else if (first.equals("split")) {
		    // command line args: split fileName peerWithFile
		    Object obj = null;
		    Object[] params = {st.nextToken(), 3};
		    String cPeer = st.nextToken();
		    System.out.println("cpeer is " + cPeer);
		    XmlRpcClient connectedPeer = peerList.get(cPeer);
		    obj = connectedPeer.execute("peer.splitFile", params);
		    System.out.println(obj);
		} else if (first.equals("pass")) {
		    // command line args: pass peerWithFile
		    //Object[] params = {"test text", InetAddress.getLocalHost().getHostName()};
		    Object obj = null; 
		    Object[] params = {"test text", "limia"};
		    String cPeer = st.nextToken();
		    //System.out.println(cPeer);
		    XmlRpcClient conPeer = peerList.get(cPeer);
		    obj = conPeer.execute("peer.passFile", params);
		    System.out.println("pass on file complete");
		}
	    }

	} catch (Exception e) {
	    System.err.println("JavaServer: " + e);
	    System.out.println("Server exiting.");
	}

    }
}
