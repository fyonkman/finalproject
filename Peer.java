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

public class Peer {

    // global variables for stateful server
    static HashMap<Integer, Integer> purchaselog;
    static Connection c = null;

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
	    phm.addHandler("peer" + Integer.toString(num), Peer.class);
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

    
    // Set up server and interface to allow operations from stdin and clients 
    public static void main (String [] args) {
	try {
	    Peer peer1 = new Peer(1, 9173);
	    Peer peer2 = new Peer(2, 9174);

	    peer1.connect(9174);
	    System.out.println("Successful connection of 1 to 2");
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
