import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.XmlRpcException;
import java.sql.*;
import java.util.*;

// Fiona and Emma 2020
/*
  This server interacts between clients and the bookstore database. It is responsible for handling the client operations of buy, search, and lookup, as well as server specific operations that include log, restock, and update. 
*/

public class Peer {

    // global variables for stateful server
    static HashMap<Integer, Integer> purchaselog;
    static Connection c = null;

    public Peer(int port) {
	
    }
     
    // Set up server and interface to allow operations from stdin and clients 
    public static void main (String [] args) {
	try {
	    // Create connection to the database
	    //Class.forName("org.sqlite.JDBC");
	    //c = DriverManager.getConnection("jdbc:sqlite:bookstore.db");
	    //c.setAutoCommit(false);
	    
	    PropertyHandlerMapping phm = new PropertyHandlerMapping();
	    XmlRpcServer xmlRpcServer;
      
	    // Set up the webserver 
	    WebServer server = new WebServer(9173);
	    xmlRpcServer = server.getXmlRpcServer();
	    phm.addHandler("ourserver", Server.class);
	    xmlRpcServer.setHandlerMapping(phm);
	    server.start();

	    // Populate log hashmap
	    purchaselog = new HashMap<Integer, Integer>(); 
	    purchaselog.put(53477,0);
	    purchaselog.put(53573,0);
	    purchaselog.put(12365,0);
	    purchaselog.put(12498,0);    
	    
	    System.out.println("Started successfully.");
	    System.out.println("Accepting requests (commands = log, restock, update id price, exit)");

	    // Reads user commmands from standard in- reprompts if not valid command
	    Scanner in = new Scanner(System.in);
	    while(in.hasNext()) {
		String s = in.nextLine();
		String[] params = s.split(" ");

		// Execute if existing command, reprompt if not
		if(params[0].equals("log")) {
		    log();
		} else if (params[0].equals("restock")) {
		    restock(); 
		} else if (params[0].equals("update")) {
		    try {
			int id = Integer.parseInt(params[1]);
			float price = Float.parseFloat(params[2]);
			update(id, price);
		    } catch (Exception e) {
			System.out.println("Invalid update parameters. Please try again.");
		    }
		} else if (params[0].equals("exit")) {
		    c.close();
		    System.out.println("Goodbye.");
		    System.exit(0);
		} else {
		    System.out.println("Please enter a valid command (log, restock, update id price, exit)."); 
		}
	    }
	    
	} catch (Exception exception) {
	    System.err.println("JavaServer: " + exception);
	    System.out.println("Server exiting."); 
	}
    }
}
