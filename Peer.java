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
import java.net.InetAddress;
import java.io.File;

// Fiona and Emma 2020
/*
  Running Questions:
*/
/*
  To Do:

*/

public class Peer {

    static XmlRpcClient peer = null;
    
    // connect to another peer specified by port number 
    public static int connect(String host, String myName) {
	// Connect to other peers
	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

	try {
	    config.setServerURL(new URL("http://"+host+":" + 9173));
	    // host may need to be passed through as parameter... TBD 
	    // config.setServerURL(new URL("http://" + host + ":" + port));
	    peer = new XmlRpcClient();
	    peer.setConfig(config);
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
	// see if file exists
	// open file
	// read line by line, send contents
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
		    obj = peer.execute("peer.sayHello", params);
		    System.out.println(obj);
		} else if (first.equals("get")) {
		    Object obj = null;
		    Object[] params = {st.nextToken()};
		    obj = peer.execute("peer.get", params);
		    System.out.println(obj);
		}
	    }

	} catch (Exception e) {
	    System.err.println("JavaServer: " + e);
	    System.out.println("Server exiting.");
	}

    }
}
