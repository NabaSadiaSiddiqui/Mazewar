import java.io.ObjectOutputStream;
import java.io.Serializable;
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
	
	/**
	 * Serializing data to store in the queue of actions
	 */
	static class ActionInfo implements Serializable{
		private String name;
		private int action;
		private int time;	// logical time of when the action was performed
		
		public ActionInfo(String name, int action, int time) {
			this.name = name;
			this.action = action;
			this.time = time;
		}
		
		/**
		 * Getter functions
		 */
		public String getPlayerName() {
			return name;
		}
		
		public int getAction() {
			return action;
		}
		
		public int getTime() {
			return time;
		}
		
		/**
		 * Printable output
		 */
		public String toString() {
			return "Player named " + name + " performed action code " + action + " at time " + time; 
		}
	}
}