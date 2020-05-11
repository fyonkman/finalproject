import java.util.*;
import java.net.URL;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

// Fiona and Emma 2020
/*
  This is a java implementation of a client that can interact with the bookstore database through the server. It can use the buy, search, lookup, and exit operations. 
 */
public class JClient {
 
    // Helper method to handle buy command; if test == 1, server side purchase successful  
    public static void buy(String[] param, XmlRpcClient client) {
	try {
	    int id = Integer.parseInt(param[1]);
	    Vector<Integer> params = new Vector<Integer>();
	    params.addElement(new Integer(id));
	    Object[] result = (Object[])client.execute("ourserver.buy", params.toArray());
	    int test = ((Integer) result[0]).intValue();
	    if(test == 1) {
		System.out.println("Bought book " + id);
	    } else {
		System.out.println("Failed Purchase: Book not found or out of stock.");
	    }
	} catch (Exception e) {
	    System.out.println("Invalid parameters. Please try again.");
	    System.err.println("Buy err: " + e);
	}
    }

    // Search command
    public static void search(String[] param, XmlRpcClient client) {
	try {
	    String topic = param[1] + " " + param[2];
	    Vector<String> params = new Vector<String>();
	    params.addElement(topic);
	    Object[] result = (Object[])client.execute("ourserver.search", params.toArray());
	    if(result.length > 0) {
		for(int i = 0; i < result.length; i++) {
		    System.out.println(result[i]);
		}
	    } else {
		System.out.println("No books found on that topic.");
	    }
	} catch (Exception e) {
	    System.err.println("Search err: " + e);
	}
    }

    // Lookup command
    public static void lookup(String[] param, XmlRpcClient client) {
	try {
	    int id = Integer.parseInt(param[1]);
	    Vector<Integer> params = new Vector<Integer>();
	    params.addElement(new Integer(id));
	    Object[] result = (Object[])client.execute("ourserver.lookup", params.toArray());

	    if(result.length > 0) {
		for(int i = 0; i < result.length; i++) {
		    System.out.println(result[i]);
		}
	    } else {
		System.out.println("No books matching that ID.");
	    }

	} catch (Exception e) {
	    System.out.println("Invalid parameters. Please try again.");
	    System.err.println("Lookup err: " + e);
	} 
    }
    
    // Connect client to server, parse input from standard in and execute commands accordingly 
    public static void main (String [] args) {
	try {
	    // Connect to server
	    String host = (args.length < 1) ? null : args[0];
	    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    XmlRpcClient client=null;
	    try {
		config.setServerURL(new URL("http://lohani.cs.williams.edu:" + 9173));
		// config.setServerURL(new URL("http://" + host + ":" + 9173));
		client = new XmlRpcClient();
		client.setConfig(config);
	    } catch (Exception e) {
		System.out.println("Problem connecting to server!");
		System.exit(0); 
	    }

	    System.out.println("Accepting requests (Commands = buy id, search topic, lookup id, exit)");
	    
	    //String param[] = new String[3];
	    
	    //param[0] = "search";
	    //param[1] = "college";
	    //param[2] = "life";
	    //for(int i = 0; i < 500; i++) {
		//System.out.println(i); 
		//search(param, client);
		//}
	    
	    String param[] = new String[2];
	    param[0] = "buy";
	    param[1] = "12498";
	    //for(int i = 0; i < 500; i++) {
	    //	System.out.println(i); 
		buy(param, client); 
		//}
	    
	    System.exit(0); 
	    
	    /*
	    // Read commands from standard in
	    Scanner in = new Scanner(System.in); 
	    while(in.hasNext()) {
		String s = in.nextLine();
		String param[] = s.split(" ");
		
		// Begin execution if command is valid, reprompt otherwise 
		if(param[0].equals("buy")) {
		    buy(param, client); 		    
		} else if (param[0].equals("search")) {
		    search(param, client);		   		    
		} else if (param[0].equals("lookup")) {
		    lookup(param, client); 		  
		} else if (param[0].equals("exit")) {
		    System.out.println("Goodbye.");
		    System.exit(0);
		} else {
		    System.out.println("Please enter a valid command (buy id, search topic, lookup id, exit).");
		}					             
	    }
	    */
	} catch (Exception e) {
	    System.err.println("Client: " + e); 
	}

    }
}
    
