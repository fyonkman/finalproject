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

public class Server {

    // global variables for stateful server
    static HashMap<Integer, Integer> purchaselog;
    static Connection c = null;

    // Client operation- buy book with the same id # if stock > 0, decrementing stock after purchase 
    public Integer[] buy(int id) {

	Statement stmt = null;
	ResultSet rset;
	String query;
	int success = 0;
	try{
	    // Check if ID exists and stock > 0
	    stmt = c.createStatement();
	    query = "SELECT * FROM BOOKS WHERE (ID = " + id + " AND STOCK > 0);";
	    rset = stmt.executeQuery(query);

	    if (rset != null) {
		// Decrement stock of book to be purchased
		stmt = c.createStatement();
		query = "UPDATE BOOKS SET STOCK = STOCK - 1 WHERE ID = " + id + ";";
		stmt.executeUpdate(query);

		// Update log of book purchased		
		purchaselog.put(id, purchaselog.get(id)+1);	
		success = 1;	    
	    }
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}

	// return 1 if success, 0 otherwise 
	Integer[] array = new Integer[1];
	array[0] = new Integer(success);
	return array; 
    }

    // Client search operation, returns all books that match the specified topic in the database 
    public Vector<String> search(String topic) {
	
	Statement stmt = null;
	String query;
	ResultSet rset;
	Vector<String> vec = new Vector<String>(); 

	try{
	    // Get books that match the topic
	    stmt = c.createStatement();
	    query = "SELECT * FROM BOOKS WHERE TOPIC = '" + topic + "';";
	    rset = stmt.executeQuery(query);
	    
	    ResultSetMetaData rsmd = rset.getMetaData();
	    int colNum = rsmd.getColumnCount();

	    // Create string of info for each book, add to vector to be returned 
	    while(rset.next()) {
		String entry = "";
		for(int i = 1; i <= colNum; i++) {
		    if(i>1) {
			entry = entry + ", ";
		    }
		    String colVal = rset.getString(i);
		    entry = entry + colVal + " "+ rsmd.getColumnName(i);
		}
		vec.add(entry);
	    }
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}
	return vec;
    }

    // Client lookup operation, returns details of the book matching the id
    public Vector<String> lookup(int id) {
	
	Statement stmt = null;
	String query;
	ResultSet rset;
	Vector<String> vec = new Vector<String>();

	try{
	    // Select the books in the database that match the id
	    stmt = c.createStatement();
	    query = "SELECT * FROM BOOKS WHERE ID = " + id + ";";
	    rset = stmt.executeQuery(query);

	    ResultSetMetaData rsmd = rset.getMetaData();
	    int colNum = rsmd.getColumnCount();

	    // Create string of info for book, add to vector to be returned 
	    while(rset.next()) {
		String entry = "";
		for(int i = 1; i <= colNum; i++) {
		    if(i>1) {
			entry = entry + ", ";
		    }
		    String colVal = rset.getString(i);
		    entry = entry + colVal + " "+ rsmd.getColumnName(i);
		}
		vec.add(entry);
	    }
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}
	return vec;
    }
    
    // Helper method for debugging that prints out table in database
    public static void printTable() {
	Statement stmt = null;
	String query;
	ResultSet rset;

	try{
	    stmt = c.createStatement();
	    query = "SELECT * FROM BOOKS ORDER BY ID";
	    rset = stmt.executeQuery(query);
	    ResultSetMetaData rsmd = rset.getMetaData();
	    int colNum = rsmd.getColumnCount();

	    while(rset.next()) {
		for(int i = 1; i <= colNum; i++) {
		    if(i>1) System.out.print(", ");
		    String colVal = rset.getString(i);
		    System.out.print(colVal + " "+ rsmd.getColumnName(i));
		}
		System.out.println(""); 
	    }
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}
    }
    
    // Server operation, prints out current running log of book purchases (ID, # bought) 
    public static void log() {
	purchaselog.entrySet().forEach(entry->{
		System.out.println(entry.getKey() + " " + entry.getValue());
	    });
	
    }

    // Server operation, adds 10 to the stock for each book
    public static void restock() {
	
	Statement stmt = null;
	String query;

	try{
	    stmt = c.createStatement();
	    query = "UPDATE BOOKS SET STOCK = STOCK + 10;";	    
	    stmt.executeUpdate(query);
	    printTable(); 
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}
    }

    // Server operation, update a given book's price 
    public static void update (int ID, float price) {

	Statement stmt = null;
	String query;

	try{
	    stmt = c.createStatement();
	    query = "UPDATE BOOKS SET PRICE = " + price + " WHERE ID = " + ID + ";";	    
	    stmt.executeUpdate(query);
	    printTable();
	} catch (Exception e) {
	    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	}
    }

    // Set up server and interface to allow operations from stdin and clients 
    public static void main (String [] args) {
	try {
	    // Create connection to the database
	    Class.forName("org.sqlite.JDBC");
	    c = DriverManager.getConnection("jdbc:sqlite:bookstore.db");
	    c.setAutoCommit(false);
	    
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
