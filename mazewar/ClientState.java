import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ClientState {
	
	public static String hostname = "localhost";
	public static int port;
	
	public static int CURR_TIME = 0;
	
	public static String PLAYER_NAME;
	
	public static Point PLAYER_POINT;
	
	public static boolean isSelf(Client client) {
		return client.getName().equals(PLAYER_NAME);
	}
	
	public static boolean isSelfLocation(String hostname, int port) {
		return hostname.equals(ClientState.hostname) && port == ClientState.port;
	}
	
	public static boolean isCurrPosition(Point curr) {
		int xRef = PLAYER_POINT.getX();
		int yRef = PLAYER_POINT.getY();
		
		int xCurr = curr.getX();
		int yCurr = curr.getY();
		
		return xRef == xCurr && yRef == yCurr;
	}
	
	/**
	 * Map of actions performed by the different players in the game
	 * Key is the logical time it was performed at
	 */
	static ConcurrentHashMap<String, SharedData.ActionInfo> actionQueue = new ConcurrentHashMap<String, SharedData.ActionInfo>();
	
	static ConcurrentHashMap<String, Integer> scoreMap = new ConcurrentHashMap<String, Integer>();

	/**
	 * Queue of all other players in the game and their location
	 */
	static BlockingQueue<ClientLocation> others = new ArrayBlockingQueue<ClientLocation>(SharedData.MAX_PLAYERS-1);

	static class ClientLocation {
		private static String hostname;
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
		
		public ClientLocation(String hostname, int port) {
			this.hostname = hostname;
			this.port = port;
			
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
				System.out.println("Its closed");
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
				System.out.println("Its closed");
			}
			
			return in;
		}
	}
	
}
