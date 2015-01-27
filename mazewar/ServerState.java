import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Serializing data to store in the queue of actions
 * @author siddi224
 *
 */
class ActionInfo {
	private String name;
	private int action;
	private int time;	// logical time of when the action was performed
	
	public ActionInfo(String name, int action, int time) {
		this.name = name;
		this.action = action;
		this.time = time;
	}
}

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
	static BlockingQueue<ActionInfo> actionQueue = new LinkedBlockingQueue<ActionInfo>();
	
	/**
	 * Logical time of the server
	 */
	static int time;
	
}
