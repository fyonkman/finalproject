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
  This class is a peer for the implementation of our peer-to-peer system. The goal of this system is to allow a peer to request a file from another peer. This file gets split and the parts are sent to intermediary peers in the system in order to maximize speed of transfer and ultimately minimize bandwidth usage between the destination peer and the peer with the file. 
*/

public class Peer {

    // port number 
    private static final int port = 9173; 
    // list of active connections to other peers
    static HashMap<String, XmlRpcClient> peerList;
    // key determines number ordering for file pieces to put back together 
    static HashMap<Integer, String> filePieces; 

    /*
      Creates a peer connection to 'host', adds connection to PeerList  
     */
    public static int connect(String host, String myName) {

	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	try {
	    // connect to host on port 9173
	    config.setServerURL(new URL("http://"+host+":" + port));
	    XmlRpcClient peer = new XmlRpcClient();
	    peer.setConfig(config);
	    peerList.put(host, peer);
	    System.out.println(myName + " is successfully connected to " + host);
	    
	} catch (Exception e) {   
	    System.out.println("Problem connecting to " + host);
	    return 0; 
	}      
	return 1;
    }

    /*
      Splits input of file into numNodes, sends to intermediary nodes via passFile method
     */
    public String splitFile(String fileName, int numNodes, String destPeer) {
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

		// array of peers that can be used as intermediaries 
		Object[] intermPeers = peerList.values().toArray(); 
		
		while ((bytesAmount = bis.read(buffer)) > 0) {		    		   
		    String output = new String(buffer);
		    // if last node, tack on remainder of bytes from file
		    if(nodesSent == numNodes-1) {
			bytesAmount = bis.read(buffer);
			// tack on remainder of bytes 
			if(bytesAmount > 0) {
			    String end = new String(buffer);
			    end = end.substring(0, bytesAmount);
			    output = output + end;
			}			
		    }		    
		    Object obj = null;
		    Object[] params = {output, nodesSent+1, destPeer, numNodes};
		    // get intermediary peer from peer list, assume number of connections >= numNodes/file pieces
		    XmlRpcClient intermPeer = (XmlRpcClient)intermPeers[nodesSent];

		    // pass file piece to middle node as parameter
		    obj = intermPeer.execute("peer.passFile", params);
		    // break on last node
		    if(nodesSent == numNodes-1) {break;}
		    nodesSent++;
		}
	    }
	    	
	} catch (Exception e) {
	    System.out.println("Problem splitting file.");
	}
	return "SplitFile complete!";
    }

    /*
      Intermediary method that passes input through middle node to the destination node
     */
    public String passFile(String splitFile, int locationNum, String destinationPeer, int numNodes) {
	try{
	    System.out.println("Piece being passed is: " + splitFile);
	    System.out.println("Location num: " + locationNum + "\nDestination peer: " + destinationPeer + "\nNumNodes: " + numNodes);
	    
	    Object obj = null; 
	    Object[] params = {splitFile, locationNum, numNodes};
	    // get destination peer from peerList 
	    XmlRpcClient destPeer = peerList.get(destinationPeer);
	    // pass input to final node as parameter 
	    obj = destPeer.execute("peer.receive", params);
	    System.out.println("Passed on file.");
	} catch (Exception e) {
	    System.out.println("Problem passing the file.");
	    System.err.println("PassFile: " + e);
	}
	return "PassFile complete!"; 
    }

    /*
      Receives input, places into hashmap, to be reasssembled at the end 
     */
    public String receive(String splitFile, int locationNum, int totalPieces) {
	filePieces.put(locationNum, splitFile);

	// once last piece is added, print out final output of file
	if(filePieces.size() == totalPieces) {
	    String finalFile = "";
	    for(int i = 1; i <= totalPieces; i++) {
		finalFile = finalFile + filePieces.get(i);
	    }
	    System.out.print("The final file is:\n" + finalFile);
	    // Here we should reset the hashmap of file pieces so can handle next file request... 
	}

	return "Receive done!"; 
    }
    
    /*
      Creates server for the peer, handles command line input for 'split fileName numNodes peerWithFile' and 'connect nodeName' 
     */
    public static void main (String [] args) {
	try {
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    XmlRpcServer xmlRpcServer;

	    // Set up the webserver
	    WebServer server = new WebServer(port);
	    xmlRpcServer = server.getXmlRpcServer();
	    phm.addHandler("peer", Peer.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();
	    System.out.println("Peer started successfully on port 9173 on "+InetAddress.getLocalHost().getHostName());

	    //initialize peer hashmap
	    peerList = new HashMap<>();

	    // initialize file pieces hashmap
	    filePieces = new HashMap<>();

	    Scanner in = new Scanner(System.in);
	    while(true) {
		String s = in.nextLine();
		StringTokenizer st = new StringTokenizer(s);
		String first = st.nextToken();
		if (first.equals("connect")) {
		    connect(st.nextToken(), InetAddress.getLocalHost().getHostName());               
		} else if (first.equals("split")) {
		    // command line args needed: split fileName numNodes peerWithFile
		    Object obj = null;
		    String fileName = st.nextToken();
		    int numNodes = Integer.parseInt(st.nextToken());
		    String myName = InetAddress.getLocalHost().getHostName();
		    // isolate lab machine name
		    myName = myName.substring(0, myName.indexOf(".")); 

		    // execute splitFile call on node that has the file (cPeer)
		    Object[] params = {fileName, numNodes, myName};
		    String cPeer = st.nextToken();
		    XmlRpcClient connectedPeer = peerList.get(cPeer);
		    obj = connectedPeer.execute("peer.splitFile", params);
		    System.out.println(obj);
		} else {
		    System.out.println("Please type 'split fileName numNodes peerWithFile' or 'connect nodeName'.");
		}
	    }

	} catch (Exception e) {
	    System.err.println("JavaServer: " + e);
	    System.out.println("Server exiting.");
	}

    }
}
