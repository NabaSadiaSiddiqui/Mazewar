import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerState {
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
	static BlockingQueue<Point> occupiedCells = new ArrayBlockingQueue<Point>(
			SharedData.MAX_PLAYERS);

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
	static class PlayerDetails implements Serializable {
		private int port, x, y;
		private String hostname;
		private String orientation;

		public PlayerDetails(int id, int port, String hostname, int x, int y,
				String orientation) {
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
	static ConcurrentHashMap<String, PlayerMeta> allPlayers = new ConcurrentHashMap<String, PlayerMeta>();

	/**
	 * A uniformly increasing integer which is assigned to a client
	 */
	static int clientId = 0;

}
