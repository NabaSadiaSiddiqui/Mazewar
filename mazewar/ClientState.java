import java.util.HashMap;


public class ClientState {

	// Client moves correspond to values in MazewarPacket
	/*public static int ACTION_FORWARD = 1;
	public static int ACTION_BACKWARD = 2;
	public static int ACTION_LEFT = 3;
	public static int ACTION_RIGHT = 4;
	public static int ACTION_FIRE = 5;
	public static int ACTION_QUIT = 6;*/
	
	//public static int PLAYER_ACTION;
	public static String PLAYER_NAME;
	
	public static HashMap<String, Client> playersInGame = new HashMap<String, Client>();
}
