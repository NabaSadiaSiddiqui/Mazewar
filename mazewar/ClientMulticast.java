import java.io.IOException;
import java.util.Iterator;


public class ClientMulticast {

	public ClientMulticast() {
		
	}
	
	public static void mMove(MazewarPacket packetToOthers) {
		System.out.println("Getting an iterator");
		Iterator<ClientState.ClientLocation> others = ClientState.others.iterator();
		System.out.println("Got an iterator");
		System.out.println("Before while loop");
		while(others.hasNext()) {
			System.out.println("Inside while loop");
			ClientState.ClientLocation other = others.next();
			try {
				System.out.println("Sending event to others");
				other.getOut().writeObject(packetToOthers);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
