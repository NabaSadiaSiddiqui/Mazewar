import java.io.IOException;
import java.util.Iterator;


public class ClientMulticast {

	public ClientMulticast() {
		
	}
	
	public static void mMove(int action) {
		
		ClientState.actions.add(action);
		
		
		while(!ClientState.actions.isEmpty()) {
			action = ClientState.actions.remove();
			
			MazewarPacket packetToOthers = new MazewarPacket();
			packetToOthers.type = MazewarPacket.CLIENT_ACTION;
	    	packetToOthers.player = ClientState.PLAYER_NAME;
			
			switch(action) {
				case MazewarPacket.CLIENT_FORWARD:
		        	packetToOthers.action = action;
					break;
				case MazewarPacket.CLIENT_BACKWARD:
		        	packetToOthers.action = action;
					break;
				case MazewarPacket.CLIENT_LEFT:
		        	packetToOthers.action = action;				
		        	break;
				case MazewarPacket.CLIENT_RIGHT:
		        	packetToOthers.action = action;
		        	break;
				default:
					System.err.println("What action did you just perform?!?!");
					break;
			}
		
			ClientState.tokenLock.lock();
			if(ClientState.HAVE_TOKEN) {
				Iterator<ClientState.ClientLocation> others = ClientState.others.iterator();
				while(others.hasNext()) {
					ClientState.ClientLocation other = others.next();
					try {
						other.getOut().writeObject(packetToOthers);
						Mazewar.consolePrintLn(ClientState.PLAYER_NAME + ": sent action to move to others successfully");
	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				
				String playerName = ClientState.PLAYER_NAME;
				Iterator allClients = Mazewar.maze.getClients();
				Client player = null;
				while(allClients.hasNext()) {
					player = (Client) allClients.next();
					
					if(player.getName().equals(playerName))
						break;
				}
				
				switch(action) {
					case MazewarPacket.CLIENT_FORWARD:
						player.forward();
						break;
					case MazewarPacket.CLIENT_BACKWARD:
						player.backup();
						break;
					case MazewarPacket.CLIENT_LEFT:
						player.turnLeft();
			        	break;
					case MazewarPacket.CLIENT_RIGHT:
						player.turnRight();
			        	break;
			        default:
						System.err.println("What action did you just perform?!?!");
						break;
				}
			} else {
				System.out.println("Do you really wanna be sending this...");
			}
			
			ClientState.tokenLock.unlock();
		}
	}
}
