import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inline class to describe a client on the maze
 */
class PlayerMeta implements Serializable {
	private int id, port, posX, posY;
	private String name, hostname, orientation;
	
	/**
	 * Constructor
	 */
	public PlayerMeta(int id, String name, int posX, int posY, String orientation, String hostname, int port) {
		this.id = id;
		this.name = name;
		this.posX = posX;
		this.posY = posY;
		this.orientation = orientation;
		this.hostname = hostname;
		this.port = port;
	}
	
	/**
	 * Printable output
	 */
	public String toString() {
		return " Remote client is " + name + ", at " + port + ":" + hostname;
	}
	
	/**
	 * Getter functions
	 */
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getX() {
		return posX;
	}
	
	public int getY() {
		return posY;
	}
	
	public String getOrientation() {
		return orientation;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
}

public class MazewarPacket implements Serializable {

	/**
	 * Constants
	 */
	public static final int CLIENT_NULL = 0;
	// Client request to join the game
	public static final int CLIENT_JOIN = 101;
	// Client request to register with server
	public static final int CLIENT_REGISTER = 102;
	// Client request to make a move
	public static final int CLIENT_ACTION = 103;
	// Client request to move forward
	public static final int CLIENT_FORWARD = 104;
	// Client request to move backward
	public static final int CLIENT_BACKWARD = 105;
	// Client request to turn left
	public static final int CLIENT_LEFT = 106;
	// Client request to turn right
	public static final int CLIENT_RIGHT = 107;
	// Client request to fire
	public static final int CLIENT_FIRE = 108;
	// Client request to respawn
	public static final int CLIENT_RESPAWN = 109;
	// Client request to quit game
	public static final int CLIENT_QUIT = 110;
	// Client assigning the next player the token
	public static final int CLIENT_TOKEN_EXCHANGE = 111;
	// Client ack code to player that sent out an action
	public static final int CLIENT_ACK = 112;
	
	// Server ack code to player wanting to join the game
	public static final int SERVER_ACK_JOIN = 201;
	// Server nack code to player wanting to join the game
	public static final int SERVER_NACK_JOIN = 202;
	// Server code everything went okay on our side
	public static final int SERVER_OK = 203;
	// Server response to clients with all players in the game
	public static final int SERVER_BROADCAST_PLAYERS = 204;
	// Server notification
	public static final int SERVER_BROADCAST_MOVE = 205;
	// Server code to assign token
	public static final int SERVER_SET_TOKEN = 206;
	// Server code that something is amiss...check error code
	public static final int SERVER_ERROR = 207;
	
	
	/**
	 * Error codes
	 */
	public static final int ERROR_MAX_PLAYER_CAPACITY_REACHED = -101;
	
	/**
	 * Message header
	 */
	public int type = MazewarPacket.CLIENT_NULL;
		
	/**
	 * Report errors
	 */
	public int error_code;
	
	
	/**
	 * Information about a single client
	 */
	public PlayerMeta playerInfo;
	
	/**
	 * The coordinates at which player wants to start the game from
	 */
	public Point myPosition;
	
	/**
	 * Player name who generated the action
	 */
	public String player;
	
	/**
	 * Action performed by player
	 */
	public int action;
	
	/**
	 * State to indicate if this player has the token
	 */
	public static boolean have_token = false;
	
	/**
	 * Hash map of all players in the game
	 */
	public ConcurrentHashMap<String, PlayerMeta> allPlayers = new ConcurrentHashMap<String, PlayerMeta>();
}
