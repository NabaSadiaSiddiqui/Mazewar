import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerState {
	
	/**
	 * Queue of all players in the game
	 */
	//static BlockingQueue<PlayerMeta> players = new ArrayBlockingQueue<PlayerMeta>(SharedData.MAX_PLAYERS);
	
	/**
	 * Boolean to indicate if all players in the game have been introduced
	 */
	static boolean PLAYERS_ADDED = false;
	
	/**
	 * Array of output streams, each connected to a client (player)
	 */
	static ConcurrentHashMap<String, ObjectOutputStream> outAll = new ConcurrentHashMap<String, ObjectOutputStream>();

	/**
	 * Array of points occupied by player(s)
	 */
	static BlockingQueue<Point> occupiedCells = new ArrayBlockingQueue<Point>(SharedData.MAX_PLAYERS);	
	
	/**
	 * Queue of actions to broadcast by server
	 * Use LinkedBlockingQueue instead of, say ArrayBlockingQueue, because we do not need to specify a capacity!
	 */
	static BlockingQueue<SharedData.ActionInfo> actionQueue = new LinkedBlockingQueue<SharedData.ActionInfo>();
	
	/**
	 * Logical time of the server
	 */
	static int time;
	
	/**
	 * Variable to indicate the state of the game (in-progress vs. ended)
	 */
	static boolean playing = true;
	
	/**
	 * Init information about each client (as they join the game)
	 */
	static class PlayerDetails implements Serializable{
		private int port, x, y;
		private String hostname;
		private String orientation;
		
		public PlayerDetails(int port, String hostname, int x, int y, String orientation) {
			this.port = port;
			this.hostname = hostname;
			this.x = x;
			this.y = y;
			this.orientation = orientation;
		}
		
		public int getPort() {
			return port;
		}
		
		public String getHostname() {
			return hostname;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
		
		public String getOrientation() {
			return orientation;
		}
	}
	
	/**
	 * Hashmap to store all players in the game
	 */
	static ConcurrentHashMap<String, PlayerDetails> allPlayers = new ConcurrentHashMap<String, PlayerDetails>();

	
}
