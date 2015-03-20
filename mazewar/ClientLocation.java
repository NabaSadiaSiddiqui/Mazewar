import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientLocation {
	private static String hostname, name;
	private static int port;
	
	/**
	 * Socket for communication with the client
	 */
	private static Socket socket = null;
	/**
	 * Data structures to read/write to/from out/in stream
	 */
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;
	
	// Unique id of the client, assigned by the server
	private static int id;
	
	public ClientLocation(String hostname, int port, int id, String name) {
		this.hostname = hostname;
		this.port = port;
		this.id = id;
		this.name = name;
		
		try {
			socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: Coudn't get I/O for the connection");
			e.printStackTrace();
		}
	}
	
	public ObjectOutputStream getOut() {
		if(out == null) {
			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("Got streams to write to");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!socket.isConnected()) {
			System.out.println("Socket is closed");
		}
		
		return out;
	}
	
	public ObjectInputStream getIn() {
		if(in == null) {
			try {
				InputStream iStream = socket.getInputStream();
				while(iStream == null) {
					iStream = socket.getInputStream();
				}
				in = new ObjectInputStream(iStream);
				System.out.println("Got streams to read from");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!socket.isConnected()) {
			System.out.println("Socket is closed");
		}
		
		return in;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
}

