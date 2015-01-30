import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerState {
	
	/**
	 * Queue of all players in the game
	 */
	static BlockingQueue<PlayerMeta> players;
	
	/**
	 * Boolean to indicate if all players in the game have been introduced
	 */
	static boolean PLAYERS_ADDED = false;
	
	/**
	 * Array of sockets, each connected to a client (player)
	 */
	static Socket[] socks = new Socket[SharedData.MAX_PLAYERS];
	
	/**
	 * Array of output streams, each connected to a client (player)
	 */
	static ObjectOutputStream[] outAll = new ObjectOutputStream[SharedData.MAX_PLAYERS];
	
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
	
}
