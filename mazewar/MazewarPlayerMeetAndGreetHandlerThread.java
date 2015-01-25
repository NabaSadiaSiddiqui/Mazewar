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
			
			for(i=0; i<SharedData.MAX_PLAYERS; i++) {				
				Socket socket = SharedData.socks[i];
				
				/* Stream to write back to client */
				ObjectOutputStream toClient = SharedData.outAll[i];
				
				/* Create a packet for meet-and-greet all players */
				MazewarPacket packetToClient = new MazewarPacket();
				
				if(!SharedData.PLAYERS_ADDED) {
					packetToClient.type = MazewarPacket.SERVER_BROADCAST_PLAYERS;
					packetToClient.activeClients = SharedData.players;
					
					toClient.writeObject(packetToClient);
					
					if(i==SharedData.MAX_PLAYERS) {
						SharedData.PLAYERS_ADDED = true;
					}
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
