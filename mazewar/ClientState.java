import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class ClientState {
	
	public static int CURR_TIME = 0;
	
	public static String PLAYER_NAME;
	
	public static Point PLAYER_POINT;
	
	public static boolean isSelf(Client client) {
		return client.getName().equals(PLAYER_NAME);
	}
	
	public static boolean isCurrPosition(Point curr) {
		int xRef = PLAYER_POINT.getX();
		int yRef = PLAYER_POINT.getY();
		
		int xCurr = curr.getX();
		int yCurr = curr.getY();
		
		return xRef == xCurr && yRef == yCurr;
	}
	
	/**
	 * Map of actions performed by the different players in the game
	 * Key is the logical time it was performed at
	 */
	static ConcurrentHashMap<String, SharedData.ActionInfo> actionQueue = new ConcurrentHashMap<String, SharedData.ActionInfo>();

//	public static SharedData.ActionInfo getValidMove() {
//		SharedData.ActionInfo playerMove = null;
//		
//		Iterator allMoves = actionQueue.iterator();
//		while(allMoves.hasNext()) {
//			playerMove = (SharedData.ActionInfo) allMoves.next();
//			
//			if(playerMove.getTime() == CURR_TIME)
//				break;
//		}
//		
//		return playerMove;
//		
//	}
	
	static ConcurrentHashMap<String, Integer> scoreMap = new ConcurrentHashMap<String, Integer>();
}
