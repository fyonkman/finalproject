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
  - figure out how to split file
  - how to piece together spilt file
  - how to send pieces accross the various peers 
*/

public class Peer {

    //static XmlRpcClient peer = null;
    static HashMap<String, XmlRpcClient> peerList;
    
    // connect to another peer specified by port number 
    public static int connect(String host, String myName) {
	// Connect to other peers
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

	try {
	    config.setServerURL(new URL("http://"+host+":" + 9173));
	    // host may need to be passed through as parameter... TBD 
	    // config.setServerURL(new URL("http://" + host + ":" + port));
	    XmlRpcClient peer = new XmlRpcClient();
	    peer.setConfig(config);
	    peerList.put(host, peer);
	    System.out.println("connected");
	    
	} catch (Exception e) {   
	    System.out.println("Problem connecting to server!");
	    return 0; 
	}      
	return 1;
    }

    // server method: respond to sayHello request from another peer
    public String sayHello(String peer) {
	try {
	    System.out.println("saying hello to "+peer);
	    return "hello "+peer+"!";
	} catch (Exception e) {
	    System.out.println("Problem connecting to server!");
	}
	return null;
    }    

    // locate file, read contents of file, return it
    //in the future, returning a String[] may be useful for when we split file
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
    
    // split input of file into numNodes
    // still need list of open nodes we can send to... can hard code for testing purposes for now 
    public String splitFile(String fileName, int numNodes) {
	try {
	    File file = new File("./" + fileName);
	    //byte[] fileContent = Files.readAllBytes(file.toPath());	    
	    // get size of file
	    long size = file.length();
	    int bytesPerSplit = ((int)size)/numNodes;

	    byte[] buffer = new byte[bytesPerSplit];

	    try (FileInputStream fis = new FileInputStream(file);
		 BufferedInputStream bis = new BufferedInputStream(fis)) {

		int nodesSent = 0;
		int bytesAmount = 0;
		while ((bytesAmount = bis.read(buffer)) > 0) {		    
		    String output = new String(buffer); 
		    // send output
		    if(nodesSent == numNodes-1) {
			bytesAmount = bis.read(buffer);
			String end = new String(buffer);
			end = end.substring(0, bytesAmount);
			output = output + end;
			System.out.println(output);
			break;
		    }		    
		    System.out.println(output); 
		    nodesSent++;
		}
	    }	
	} catch (Exception e) {
	    System.out.println("Problem splitting file.");
	}
	return "splitFile complete!";
    }

    //pass file to destination peer
     public void passOnFile(String splitFile, String destinationPeer) {
	 try{
	     System.out.println("beginning of pass on file");
	     Object[] params = {splitFile};
	     //peer should be destination peer
	     peerList.get(destinationPeer).execute("peer.receive", params);
	     System.out.println("passed on file");
	 } catch (Exception e) {
	     System.out.println("Problem passing the file.");
	 }
     }

    public void receive(String splitFile) {
	System.out.println(splitFile);
    }
    
    // Create peers, connect them, send messages....
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
		    Object obj = null;
		    Object[] params = {st.nextToken(), 3};
		    String cPeer = st.nextToken();
		    System.out.println("cpeer is " + cPeer);
		    XmlRpcClient connectedPeer = peerList.get(cPeer);
		    obj = connectedPeer.execute("peer.splitFile", params);
		    System.out.println(obj);
		} else if (first.equals("pass")) {
		    //Object[] params = {"test text", InetAddress.getLocalHost().getHostName()};
		    Object[] params = {"test text", "senepol"};
		    String cpeer = st.nextToken();
		    System.out.println(cpeer);
		    XmlRpcClient conPeer = peerList.get(cpeer);
		    conPeer.execute("peer.passOnFile", params);
		    System.out.println("pass on file complete");
		}
	    }

	} catch (Exception e) {
	    System.err.println("JavaServer: " + e);
	    System.out.println("Server exiting.");
	}

    }
}
