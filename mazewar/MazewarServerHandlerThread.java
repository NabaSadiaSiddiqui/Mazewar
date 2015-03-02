import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class MazewarServerHandlerThread extends Thread {
	private Socket socket = null;
	
	public MazewarServerHandlerThread(Socket socket) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new thread to handle a client");
	}
	
	public void run() {
		boolean gotServerError = false;
		boolean gotQuitPacket = false;
		
		try {
			/* Stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazewarPacket packetFromClient;
		
			/* Stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
				
			while(!gotQuitPacket && (packetFromClient = (MazewarPacket) fromClient.readObject()) != null) {
				/* Create a packet to send reply back to client */
				MazewarPacket packetToClient = new MazewarPacket();
				packetToClient.type = MazewarPacket.SERVER_OK;
				
				/* process client request to get a mutually exclusive position on maze */
				if(packetFromClient.type == MazewarPacket.CLIENT_JOIN) {
					Point position = packetFromClient.myPosition;
					int x = position.getX();
					int y = position.getY();

					boolean cellBusy = false;
					
					Iterator allPoints = ServerState.occupiedCells.iterator();
					while(allPoints.hasNext() && !cellBusy) {
						Point ref = (Point) allPoints.next();
						int xRef = ref.getX();
						int yRef = ref.getY();
						
						if(xRef == x && yRef == y) {
							packetToClient.type = MazewarPacket.SERVER_NACK_JOIN;
							cellBusy = true;
						}
					}
					
					if(!cellBusy) {
						ServerState.occupiedCells.add(position);
						packetToClient.type = MazewarPacket.SERVER_ACK_JOIN;
					}
					toClient.writeObject(packetToClient);
					continue;
				}
				
				
				/* process registration */
				if(packetFromClient.type == MazewarPacket.CLIENT_REGISTER) {
					PlayerMeta player = packetFromClient.playerInfo;
					
					if(ServerState.allPlayers.size() < SharedData.MAX_PLAYERS) { // Add player to hashmap

						System.out.println("Inside if loop");
						ServerState.PlayerDetails detail = new ServerState.PlayerDetails(player.port, player.hostname, player.posX, player.posY, player.orientation);
						ServerState.allPlayers.put(player.name, detail);
						System.out.println("Size is " + ServerState.allPlayers.size());
						ServerState.outAll.put(player.name, toClient);
						
						SharedData.CURR_PLAYERS_COUNT++;

					}
					
					/*if(ServerState.players.size() < SharedData.MAX_PLAYERS) { // Add player to the queue if possible
						ServerState.players.add(player);
						ServerState.outAll.put(player.name, toClient);
						
						SharedData.CURR_PLAYERS_COUNT++;
						
					}*/ else { // Report error
						gotServerError = true;
						packetToClient.type = MazewarPacket.SERVER_ERROR;
						packetToClient.error_code = MazewarPacket.ERROR_MAX_PLAYER_CAPACITY_REACHED;
					}
					toClient.writeObject(packetToClient);
					
					if(SharedData.MAX_PLAYERS == SharedData.CURR_PLAYERS_COUNT) {
						introducePlayers();
					}
					continue;
				}
				
				/* enqueue player's action */
				/*if(packetFromClient.type == MazewarPacket.CLIENT_ACTION) {
					String playerName = packetFromClient.player;
					int playerAction = packetFromClient.action;
					int seq = ServerState.time;
					
					SharedData.ActionInfo action;
					
					if(playerAction == MazewarPacket.CLIENT_RESPAWN) {
						action = new SharedData.ActionInfo(playerName, playerAction, seq, packetFromClient.playerInfo);
					} else {
						action = new SharedData.ActionInfo(playerName, playerAction, seq);
					
						if(playerAction == MazewarPacket.CLIENT_QUIT) {
							// Remove player from BlockingQueue<PlayerMeta> players
							Iterator players = ServerState.players.iterator();
							PlayerMeta target = null;
							Point point = null;
							while(players.hasNext()) {
								PlayerMeta tmp = (PlayerMeta) players.next();
								if(tmp.name.equals(playerName)) {
									target = tmp;
									point = new Point(tmp.posX, tmp.posY);
									break;
								}
							}
							ServerState.players.remove(target);
							
							// Remove output stream from outAll
							ServerState.outAll.remove(playerName);
							
							// Remove point from occupiedCells
							ServerState.occupiedCells.remove(point);
							
							// Decrement CURR_PLAYER_COUNT
							SharedData.CURR_PLAYERS_COUNT--;
							
							gotQuitPacket = true;
						}
					}
					
					ServerState.actionQueue.add(action);
					
					// increment logical time
					ServerState.time++;
					
					continue;
				}*/
				
			}
			
			/**
			 * Cleanup when client exits
			 */
			fromClient.close();
			toClient.close();
			socket.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			
			if(gotServerError) {
				System.out.println("A player had to exit because of overpopulation");
			} else {
				e.printStackTrace();
			}
		}
	}
	
	private void introducePlayers() {
		Enumeration<String> oStreamsKeys = ServerState.outAll.keys();
		
		// broadcast all players
		for(int i=0; i<SharedData.MAX_PLAYERS; i++) {
			
			if(SharedData.MAX_PLAYERS != SharedData.CURR_PLAYERS_COUNT) {
				break;
			}
			
			// Stream to write back to client 
			ObjectOutputStream toClient = ServerState.outAll.get(oStreamsKeys.nextElement());
		
			// Create a packet for meet-and-greet all players 
			MazewarPacket packetToClient = new MazewarPacket();
			
			if(!ServerState.PLAYERS_ADDED) {
				packetToClient.type = MazewarPacket.SERVER_BROADCAST_PLAYERS;
				packetToClient.allPlayers = ServerState.allPlayers;

				try {
					toClient.writeObject(packetToClient);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(i==SharedData.MAX_PLAYERS) {
					ServerState.PLAYERS_ADDED = true;
				}
			}
		}
	}

}
