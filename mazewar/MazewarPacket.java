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
	public static final int CLIENT_MOVE = 101;
	public static final int CLIENT_EXIT = 102;
	public static final int CLIENT_DIE = 103;

	public static final int CLIENT_JOIN = 104;
	
	public static final int SERVER_OK = 105;
	public static final int SERVER_BROADCAST_PLAYERS = 106;
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
	public BlockingQueue activeClients;
	
	/**
	 * Information about a single client
	 */
	public PlayerMeta playerInfo;
}
