import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author siddi224
 * Data shared between client and server
 */
public class SharedData {
	
	static int MAX_PLAYERS = 2;
	
	/**
	 * Variable to hold current number of players in the game
	 * CURR_PLAYERS_COUNT <= MAX_PLAYERS
	 */
	static int CURR_PLAYERS_COUNT = 0;
}