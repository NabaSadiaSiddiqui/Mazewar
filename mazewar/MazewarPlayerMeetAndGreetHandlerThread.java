import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;


public class MazewarPlayerMeetAndGreetHandlerThread extends Thread {
		
	public MazewarPlayerMeetAndGreetHandlerThread() {
		super("MazewarPlayerMeetAndGreetHandlerThread");
		System.out.println("Created new thread to introduce players to each other");
	}
	
	public void run() {
		try {
			int i;
			
			while(SharedData.MAX_PLAYERS != SharedData.CURR_PLAYERS_COUNT) {	// wait for all players to join the game
				MazewarPlayerMeetAndGreetHandlerThread.sleep(1000);
			}
			
			// broadcast all players
			for(i=0; i<SharedData.MAX_PLAYERS; i++) {				
				Socket socket = ServerState.socks[i];
				
				/* Stream to write back to client */
				ObjectOutputStream toClient = ServerState.outAll[i];
				
				/* Create a packet for meet-and-greet all players */
				MazewarPacket packetToClient = new MazewarPacket();
				
				if(!ServerState.PLAYERS_ADDED) {
					packetToClient.type = MazewarPacket.SERVER_BROADCAST_PLAYERS;
					packetToClient.activeClients = ServerState.players;
					
					toClient.writeObject(packetToClient);
					
					if(i==SharedData.MAX_PLAYERS) {
						ServerState.PLAYERS_ADDED = true;
					}
				}
			}
			
			while(true) {
				while(ServerState.actionQueue.isEmpty()) {	// wait for an action to be received by a player
					MazewarPlayerMeetAndGreetHandlerThread.sleep(1000);
				}
				
				SharedData.ActionInfo action = ServerState.actionQueue.take();
								
				// broadcast moves to players
				for(i=0; i<SharedData.MAX_PLAYERS; i++) {				
					Socket socket = ServerState.socks[i];
					
					// Stream to write back to client 
					ObjectOutputStream toClient = ServerState.outAll[i];
					
					// Create a packet to send actions in the queue 
					MazewarPacket packetToClient = new MazewarPacket();
					packetToClient.type = MazewarPacket.SERVER_BROADCAST_MOVE;
					
					
					//TODO: send all information to all players
					packetToClient.move = action;
					//packetToClient.player = action.getPlayerName() + String.valueOf(action.getTime());
	
					toClient.writeObject(packetToClient);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Check all boards to see that remote clients have been added");
		}
		
	}

}
