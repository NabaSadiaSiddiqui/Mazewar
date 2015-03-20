import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientState {
	
	//public static String hostname = "localhost";
	
	//public static int port;
	
	public static int ID_DEFAULT = -1;
	
	public static int CURR_TIME = 0;
	
	// State to indicate how many ACK codes it has received
	//public static int nAcks = 0;
	
	/**
	 * Thread responsible for passing down the token
	 */
	//public static Thread tokenMaster;
		
	/**
	 * Queue of actions performed by the player
	 */
	//static BlockingQueue<Integer> actions = new LinkedBlockingQueue<Integer>();
	
	
	static ConcurrentHashMap<String, Integer> scoreMap = new ConcurrentHashMap<String, Integer>();

	/**
	 * Queue of all other players in the game and their location
	 */
	//static BlockingQueue<ClientLocation> others = new ArrayBlockingQueue<ClientLocation>(SharedData.MAX_PLAYERS-1);

	static class ClientLocation {
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
	
	public static boolean isSelf(Client client) {
		return client.getName().equals(Mazewar.guiClient.getName());
	}
	
	/*public static boolean isSelfLocation(String hostname, int port) {
		return hostname.equals(ClientState.hostname) && port == ClientState.port;
	}*/
	
	/*public static boolean isCurrPosition(Point curr) {
		int xRef = Mazewar.guiClient.getPoint().getX();
		int yRef = Mazewar.guiClient.getPoint().getY();
		
		int xCurr = curr.getX();
		int yCurr = curr.getY();
		
		return xRef == xCurr && yRef == yCurr;
	}*/
	
}
