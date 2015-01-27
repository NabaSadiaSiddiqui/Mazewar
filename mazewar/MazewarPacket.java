import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

/**
 * Inline class to describe a client on the maze
 * @author siddi224
 */
class PlayerMeta implements Serializable {
	public String name;
	public int posX;
	public int posY;
	public String orientation;
	
	/**
	 * Constructor
	 */
	public PlayerMeta(String name, int posX, int posY, String orientation) {
		this.name = name;
		this.posX = posX;
		this.posY = posY;
		this.orientation = orientation;
	}
	
	/**
	 * Printable output
	 */
	public String toString() {
		return " Remote client is " + name + ", at x-position of " + posX + " and y-position of " + posY + ", facing " + orientation;
	}
}

public class MazewarPacket implements Serializable{

	/**
	 * Constants
	 */
	public static final int CLIENT_NULL = 0;
	// Client request to join the game
	public static final int CLIENT_JOIN = 101;
	// Client request to register with server
	public static final int CLIENT_REGISTER = 102;
	// Server ack code to player wanting to join the game
	public static final int SERVER_ACK_JOIN = 103;
	// Server nack code to player wanting to join the game
	public static final int SERVER_NACK_JOIN = 104;
	// Server code everything went okay on our side
	public static final int SERVER_OK = 105;
	// Server response to clients with all players in the game
	public static final int SERVER_BROADCAST_PLAYERS = 106;
	// Server code that something is amiss...check error code
	public static final int SERVER_ERROR = 107;
	
	
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
	
	/*
	 * An array of ALL players currently in the game
	 */
	public BlockingQueue<PlayerMeta> activeClients;
	
	/**
	 * Information about a single client
	 */
	public PlayerMeta playerInfo;
	
	/**
	 * The coordinates at which player wants to start the game from
	 */
	public Point myPosition;
}
