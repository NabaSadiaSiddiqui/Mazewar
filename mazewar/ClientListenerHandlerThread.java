import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

/**
 * Listens to incoming requests from other clients
 */
public class ClientListenerHandlerThread extends Thread {
	private int nAcks = 0;

	/**
	 * Socket through which communication will be made from other clients
	 */
	private ObjectInputStream selfIn = null;
	private Socket selfConn = null;

	public ClientListenerHandlerThread(GUIClient gui, Socket selfConn) {
		super("ClientListenerHandlerThread");
		System.out.println("Created thread to listen to incoming actions from other players in the game");
		this.selfConn = selfConn;
	}

	public void run() {
		boolean gotQuitPacket = false;

		try {
			System.out.println("ClientListenerHandlerThread is running");
			selfIn = new ObjectInputStream(selfConn.getInputStream());
			MazewarPacket packetFromClient = (MazewarPacket) selfIn.readObject();

			while (!gotQuitPacket && 
					(packetFromClient = (MazewarPacket) selfIn.readObject()) 
					!= null) {
				
				int type = packetFromClient.type;

				switch (type) {
					case MazewarPacket.CLIENT_TOKEN_EXCHANGE:
						System.out.println("Got token");
						Mazewar.tokenMaster.setHaveToken();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (Mazewar.tokenMaster.haveToken() && !Mazewar.tokenMaster.needToken()) { // dont need it BUT have it
							Mazewar.tokenMaster.passToken(Mazewar.next);
						}
						break;
					case MazewarPacket.CLIENT_ACTION:
						System.out.println("Got an action");
						int action = packetFromClient.action;
						String playerName = packetFromClient.player;
						Iterator allClients = Mazewar.maze.getClients();
						Client player = null;
						while (allClients.hasNext()) {
							player = (Client) allClients.next();
	
							if (player.getName().equals(playerName))
								break;
						}
	
						switch (action) {
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
								Direction d = Direction
										.strToDir(pInfo.getOrientation());
								player.respawn(name, p, d);
								break;
							case MazewarPacket.CLIENT_QUIT:
								Mazewar.consolePrintLn("Action: quit");
								Mazewar.removePlayer(playerName);
								cleanupPeerInfo(playerName);
								gotQuitPacket = true;
								break;
							default:
								Mazewar.consolePrint("Action: unknown");
								break;
						}
						ClientMulticast.sendAck();
						break;
					case MazewarPacket.CLIENT_ACK:
						if(!Mazewar.tokenMaster.haveToken())
							break;
						System.out.println("Got ACK code");
						nAcks++;
						if (nAcks == (SharedData.MAX_PLAYERS - 1)) {
							nAcks = 0;
							Mazewar.tokenMaster.unsetNeedToken();
							Mazewar.tokenMaster.passToken(Mazewar.next);
						}
						break;
					default:
						System.out.println("Got some mysterious token");
						break;
				}
			}
			
			/**
			 * Cleanup when client exits
			 */
			selfIn.close();
			selfConn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void cleanupPeerInfo(String name) {
		Iterator others = Mazewar.peers.iterator();
		ClientLocation _this = null;
		while (others.hasNext()) {
			ClientLocation peer = (ClientLocation) others.next();
			if (peer.getName().equals(name)) {
				_this = peer;
				break;
			}
		}
		if (_this.getId() == Mazewar.next.getId()) {
			System.out.println("ClientListenerHandlerThread::need to reaasign next client");
			reassignNextClient(_this.getId());
		}
		Mazewar.peers.remove(_this);
	}

	private void reassignNextClient(int prevClientId) {
		int nextClientId = prevClientId + 1;
		if (nextClientId >= SharedData.MAX_PLAYERS) {
			nextClientId = 0;
		}

		Iterator<ClientLocation> others = Mazewar.peers.iterator();

		while (others.hasNext()) {
			ClientLocation other = (ClientLocation) others.next();
			if (other.getId() == nextClientId) {
				Mazewar.next = other;
				System.out.println("Set next client in the ring to " + other.getName());
				Mazewar.tokenMaster.setHaveToken();
				break;
			}
		}
	}
}
