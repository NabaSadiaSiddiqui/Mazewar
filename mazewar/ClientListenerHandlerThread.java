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
								ClientMulticast.sendAck();
								break;
							case MazewarPacket.CLIENT_BACKWARD:
								Mazewar.consolePrintLn("Action: backward");
								player.backup();
								ClientMulticast.sendAck();
								break;
							case MazewarPacket.CLIENT_LEFT:
								Mazewar.consolePrintLn("Action: left");
								player.turnLeft();
								ClientMulticast.sendAck();
								break;
							case MazewarPacket.CLIENT_RIGHT:
								Mazewar.consolePrintLn("Action: right");
								player.turnRight();
								break;
							case MazewarPacket.CLIENT_FIRE:
								Mazewar.consolePrintLn("Action: fire");
								player.fire();
								ClientMulticast.sendAck();
								break;
							case MazewarPacket.CLIENT_RESPAWN:
								Mazewar.consolePrintLn("Action: respawn");
								PlayerMeta pInfo = packetFromClient.playerInfo;
								String name = pInfo.getName();
								Point p = new Point(pInfo.getX(), pInfo.getY());
								Direction d = Direction.strToDir(pInfo.getOrientation());
								player.respawn(name, p, d);
								ClientMulticast.sendAck();
								break;
							case MazewarPacket.CLIENT_QUIT:
								Mazewar.consolePrintLn("Action: quit");
								Mazewar.removePlayer(playerName);
								cleanupPeerInfo(playerName);
								break;
							default:
								Mazewar.consolePrint("Action: unknown");
								break;
						}
						break;
					case MazewarPacket.CLIENT_ACK:
						System.out.println("Got an ack code");
						ClientState.nAcks++;
						if(ClientState.nAcks == (SharedData.MAX_PLAYERS-1)) {
							ClientState.nAcks = 0;
							TokenMaster.unsetNeedToken();
							TokenMaster.passToken();
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
	
	private static void cleanupPeerInfo(String name) {
		Iterator others = ClientState.others.iterator();
		ClientState.ClientLocation _this = null;
		while(others.hasNext()) {
			ClientState.ClientLocation peer = (ClientState.ClientLocation) others.next();
			if(peer.getName().equals(name)) {
				_this = peer;
				break;
			}
		}
		if(_this.getId() == ClientState.nextClient.getId()) {
			reassignNextClient(_this.getId());
		}
		ClientState.others.remove(_this);
	}
	
	private static void reassignNextClient(int prevClientId) {
		int nextClientId = prevClientId + 1;
		if(nextClientId >= SharedData.MAX_PLAYERS) {
			nextClientId = 0;
		}
		
		if(nextClientId == ClientState.PLAYER_ID) {
			ClientState.nextClient = null;
			return;
		}
		
		Iterator<ClientState.ClientLocation> others = ClientState.others.iterator();
		
		while(others.hasNext()) {
			ClientState.ClientLocation other = (ClientState.ClientLocation) others.next();
			if(other.getId() == nextClientId) {
				ClientState.nextClient = other;
				System.out.println("Set next client in the ring");
				break;
			}
		}
	}
}
