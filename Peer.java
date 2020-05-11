import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.sql.*;
import java.util.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;



// Fiona and Emma 2020
/*
  Running Questions:
  - Does the host need to be the same amongst all peers? 
*/
/*
  To Do:
  - each peer should have vector of its connectiosn to other peers  
*/

public class Peer {

    // global variables for stateful server   
    //    public XmlRpcClient client; 
    //public XmlRpcClientConfigImpl config;

    public Peer(int num, int port) {
	// Create connection to the database
	//Class.forName("org.sqlite.JDBC");
	//c = DriverManager.getConnection("jdbc:sqlite:bookstore.db");
	//c.setAutoCommit(false);
	try {
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    XmlRpcServer xmlRpcServer;
	
	    // Set up the webserver 
	    WebServer server = new WebServer(port);
	    xmlRpcServer = server.getXmlRpcServer();
	    phm.addHandler("peer" + num, Peer.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();
	    System.out.println("Started successfully.");
	} catch (Exception e) {
	    System.err.println("JavaServer: " + e);
	    System.out.println("Server exiting.");
	}
    }

    // connect this peer to peer specified by port number 
    public int connect(int port) {
	// Connect to other peers
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	XmlRpcClient client=null;
	//config = new XmlRpcClientConfigImpl();
	//client = null;
	try {
	    config.setServerURL(new URL("http://lohani.cs.williams.edu:" + port));
	    // host may need to be passed through as parameter... TBD 
	    // config.setServerURL(new URL("http://" + host + ":" + port));
	    client = new XmlRpcClient();
	    client.setConfig(config);
	} catch (Exception e) {	   
	    System.out.println("Problem connecting to server!");
	    return 0; 
	}      
	return 1; 
    }

    // Send string input directly- may want to make node name instead of port, TBD (could do a hash table with key- node name, value- port) 
    public int receive(String peerName, String fileName) {
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	XmlRpcClient client = null;
	try {
	    config.setServerURL(new URL("http://lohani.cs.williams.edu:" + 9174));
	    // host may need to be passed through as parameter... TBD
	    // config.setServerURL(new URL("http://" + host + ":" + port));
	    client = new XmlRpcClient();
	    client.setConfig(config);
	} catch (Exception e) {
	    System.out.println("Problem connecting to server!");
	    return 0;
	}

	try {
	    Vector<String> params = new Vector<String>();
	    params.addElement(fileName);
	    //	    String[] input = new String[1];
	    //input[0] = fileName; 
	    System.out.println("Before execute: " + peerName);
	    Object[] result = (Object[])client.execute(peerName+".sendFile", params.toArray());
	    String output = (String) result[0];
	    System.out.println(output); 	    
	    return 1; 
	} catch (Exception e) {
	    System.out.println("Receive error: " + e);
	}	
	return 0;
    }

   
    public String[] sendFile(String fileName) {	
	System.out.println("In send");
	// see if file exists
	// open file
	// read line by line, send contents
	String[] result = new String[1];
	result[0] = "Hello!";
	return result; 
    }
    
    // Set up server and interface to allow operations from stdin and clients 
    public static void main (String [] args) {
	try {
	    Peer peer1 = new Peer(1, 9173);
	    Peer peer2 = new Peer(2, 9174);

	    //peer1.connect(9174);
	    //System.out.println("Successful connection of 1 to 2");

	    peer1.receive("peer2", "Hello!");
	    System.out.println("Receive complete.");

	    /*
	    // Reads user commmands from standard in- reprompts if not valid command
	    Scanner in = new Scanner(System.in);
	    while(in.hasNext()) {
		String s = in.nextLine();
		String[] params = s.split(" ");
	    }
	    */
	} catch (Exception exception) {
	    System.err.println("JavaServer: " + exception);
	    System.out.println("Server exiting."); 
	}
    }
}
