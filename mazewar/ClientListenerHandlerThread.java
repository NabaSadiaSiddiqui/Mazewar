import java.io.*;
import java.util.Iterator;

/**
 * Listens to incoming requests from other clients
 */
public class ClientListenerHandlerThread extends Thread {
	
	public ClientListenerHandlerThread() {
		super("ClientListenerHandlerThread");
		System.out.println("Created thread to listen to incoming actions from other players in the game");
	}
	
	public void run() {
		try {
			MazewarPacket packetFromClient = (MazewarPacket) Mazewar.selfIn.readObject();

			while(packetFromClient != null) {
				int type = packetFromClient.type;
				
				switch(type) {
					case MazewarPacket.CLIENT_TOKEN_EXCHANGE:
						TokenMaster.setHaveToken();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(TokenMaster.haveToken() && !TokenMaster.needToken()) { // dont need it BUT have it
							TokenMaster.passToken();
						}
						break;
					case MazewarPacket.CLIENT_ACTION:
						
						int action = packetFromClient.action;
						String playerName = packetFromClient.player;
						Iterator allClients = Mazewar.maze.getClients();
						Client player = null;
						while(allClients.hasNext()) {
							player = (Client) allClients.next();
							
							if(player.getName().equals(playerName))
								break;
						}
						
						switch(action) {
							case MazewarPacket.CLIENT_FORWARD:
								Mazewar.consolePrintLn("Action: forward");
								player.forward();
								break;
							case MazewarPacket.CLIENT_BACKWARD:
								Mazewar.consolePrintLn("Action: backward");
								player.backup();
								break;
							case MazewarPacket.CLIENT_LEFT:
								Mazewar.consolePrintLn("Action: left");
								player.turnLeft();
								break;
							case MazewarPacket.CLIENT_RIGHT:
								Mazewar.consolePrintLn("Action: right");
								player.turnRight();
								break;
							case MazewarPacket.CLIENT_FIRE:
								Mazewar.consolePrintLn("Action: fire");
								player.fire();
								break;
							case MazewarPacket.CLIENT_RESPAWN:
								Mazewar.consolePrintLn("Action: respawn");
								PlayerMeta pInfo = packetFromClient.playerInfo;
								String name = pInfo.getName();
								Point p = new Point(pInfo.getX(), pInfo.getY());
								Direction d = Direction.strToDir(pInfo.getOrientation());
								player.respawn(name, p, d);
								break;
							default:
								Mazewar.consolePrint("Action: unknown");
								break;
						}
						break;
					default:
						System.out.println("Got some mysterious token");
						break;
				}
				
				packetFromClient = (MazewarPacket) Mazewar.selfIn.readObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
