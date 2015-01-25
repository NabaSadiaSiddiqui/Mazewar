import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class SharedData {
	
	static int MAX_PLAYERS = 4;
	
	/**
	 * Variable to hold current number of players in the game
	 * CURR_PLAYERS_COUNT <= MAX_PLAYERS
	 */
	static int CURR_PLAYERS_COUNT = 0;
	
	static BlockingQueue players;
	
	/**
	 * Boolean to indicate if all players in the game have been introduced
	 */
	static boolean PLAYERS_ADDED = false;
	
	/**
	 * Array of sockets, each connected to a client (player)
	 */
	static Socket[] socks = new Socket[MAX_PLAYERS];

}