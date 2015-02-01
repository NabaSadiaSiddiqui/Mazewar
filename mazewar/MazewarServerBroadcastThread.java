import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;


public class MazewarServerBroadcastThread extends Thread {
		
	public MazewarServerBroadcastThread() {
		super("MazewarPlayerMeetAndGreetHandlerThread");
		System.out.println("Created new thread to introduce players to each other");
	}
	
	public void run() {
		while(ServerState.playing) {
			try {
				int i;
				
				while(ServerState.actionQueue.isEmpty()) {	// wait for an action to be received by a player
					MazewarServerBroadcastThread.sleep(1000);
				}
				
				// Action to send to all players
				SharedData.ActionInfo action = ServerState.actionQueue.take();
				
				// Stream to write back to client
				ObjectOutputStream toClient;
				
				// broadcast moves to players
				for(i=0; i<SharedData.MAX_PLAYERS; i++) {				
					toClient = ServerState.outAll[i];
					
					// Create a packet to send actions in the queue 
					MazewarPacket packetToClient = new MazewarPacket();
					packetToClient.type = MazewarPacket.SERVER_BROADCAST_MOVE;
					
					packetToClient.move = action;
					
					PlayerMeta addPlayer = action.getPlayerMeta();
					if(addPlayer!=null) {
						packetToClient.playerInfo = addPlayer;
					}
	
					toClient.writeObject(packetToClient);
				}
			} catch(IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
