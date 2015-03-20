import java.io.*;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

/**
 * Listens to incoming requests from other clients
 */
public class ClientListenerHandlerThread extends Thread {
	private BlockingQueue<ClientLocation> peers;
	private GUIClient gui;
	private ClientLocation next;
	private Lock tokenLock;
	private TokenMaster tokenMaster;
	private int nAcks = 0;
	
	/**
	 * Socket through which communication will be made from other clients
	 */
	private ObjectInputStream selfIn = null;
	
	public ClientListenerHandlerThread(BlockingQueue<ClientLocation> peers, GUIClient gui, ClientLocation nextClient, Lock tokenLock, ObjectInputStream selfIn, TokenMaster tokenMaster) {
		super("ClientListenerHandlerThread");
		System.out.println("Created thread to listen to incoming actions from other players in the game");
		this.peers = peers;
		this.gui = gui;
		this.next = nextClient;
		this.tokenLock = tokenLock;
		this.tokenMaster = tokenMaster;
		this.selfIn = selfIn;
	}
	
	public void run() {
		try {
			MazewarPacket packetFromClient = (MazewarPacket) selfIn.readObject();

			while(packetFromClient != null) {
				int type = packetFromClient.type;
				
				switch(type) {
					case MazewarPacket.CLIENT_TOKEN_EXCHANGE:
						System.out.println("Got token");
						tokenMaster.setHaveToken();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(tokenMaster.haveToken() && !tokenMaster.needToken()) { // dont need it BUT have it
							tokenMaster.passToken(this.next);
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
								new ClientMulticast(peers, gui, tokenLock).sendAck();
								break;
							case MazewarPacket.CLIENT_BACKWARD:
								Mazewar.consolePrintLn("Action: backward");
								player.backup();
								new ClientMulticast(peers, gui, tokenLock).sendAck();
								break;
							case MazewarPacket.CLIENT_LEFT:
								Mazewar.consolePrintLn("Action: left");
								player.turnLeft();
								new ClientMulticast(peers, gui, tokenLock).sendAck();
								break;
							case MazewarPacket.CLIENT_RIGHT:
								Mazewar.consolePrintLn("Action: right");
								player.turnRight();
								new ClientMulticast(peers, gui, tokenLock).sendAck();
								break;
							case MazewarPacket.CLIENT_FIRE:
								Mazewar.consolePrintLn("Action: fire");
								player.fire();
								new ClientMulticast(peers, gui, tokenLock).sendAck();
								break;
							case MazewarPacket.CLIENT_RESPAWN:
								Mazewar.consolePrintLn("Action: respawn");
								PlayerMeta pInfo = packetFromClient.playerInfo;
								String name = pInfo.getName();
								Point p = new Point(pInfo.getX(), pInfo.getY());
								Direction d = Direction.strToDir(pInfo.getOrientation());
								player.respawn(name, p, d);
								new ClientMulticast(peers, gui, tokenLock).sendAck();
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
						//ClientMulticast.sendAck();
						break;
					case MazewarPacket.CLIENT_ACK:
						System.out.println("Got an ack code");
						nAcks++;
						if(nAcks == (SharedData.MAX_PLAYERS-1)) {
							nAcks = 0;
							tokenMaster.unsetNeedToken();
							tokenMaster.passToken(this.next);
						}
						break;
					default:
						System.out.println("Got some mysterious token");
						break;
				}
				
				packetFromClient = (MazewarPacket) selfIn.readObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void cleanupPeerInfo(String name) {
		Iterator others = peers.iterator();
		ClientLocation _this = null;
		while(others.hasNext()) {
			ClientLocation peer = (ClientLocation) others.next();
			if(peer.getName().equals(name)) {
				_this = peer;
				break;
			}
		}
		if(_this.getId() == this.next.getId()) {
			reassignNextClient(_this.getId());
		}
		peers.remove(_this);
	}
	
	private void reassignNextClient(int prevClientId) {
		int nextClientId = prevClientId + 1;
		if(nextClientId >= SharedData.MAX_PLAYERS) {
			nextClientId = 0;
		}
		
		Iterator<ClientLocation> others = peers.iterator();
		
		while(others.hasNext()) {
			ClientLocation other = (ClientLocation) others.next();
			if(other.getId() == nextClientId) {
				this.next = other;
				System.out.println("Set next client in the ring");
				break;
			}
		}
	}
}
