import java.util.HashMap;


public class ClientState {

	public static String PLAYER_NAME;
	
	public static Point PLAYER_POINT;
	
	public static HashMap<String, Client> playersInGame = new HashMap<String, Client>();

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
}
