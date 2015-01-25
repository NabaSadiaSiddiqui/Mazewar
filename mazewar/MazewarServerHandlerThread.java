import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;


public class MazewarServerHandlerThread extends Thread {
	private Socket socket = null;
	
	public MazewarServerHandlerThread(Socket socket) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new thread to handle a client");
	}
	
	public void run() {
		boolean gotDiePacket = false;
		
		try {
			/* Stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazewarPacket packetFromClient;
		
			/* Stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
				
			while((packetFromClient = (MazewarPacket) fromClient.readObject()) != null) {
				/* Create a packet to send reply back to client */
				MazewarPacket packetToClient = new MazewarPacket();
				packetToClient.type = MazewarPacket.SERVER_OK;
				
				/* process registration */
				if(packetFromClient.type == MazewarPacket.CLIENT_JOIN) {
					PlayerMeta player = packetFromClient.playerInfo;
					
					if(SharedData.players.size() < SharedData.MAX_PLAYERS) { // Add player to the queue if possible
						SharedData.players.add(player);
						SharedData.socks[SharedData.CURR_PLAYERS_COUNT] = socket;
						SharedData.outAll[SharedData.CURR_PLAYERS_COUNT] = toClient;
						SharedData.CURR_PLAYERS_COUNT++;
						
					} else { // Report error
						packetToClient.type = MazewarPacket.SERVER_ERROR;
						packetToClient.error_code = MazewarPacket.ERROR_MAX_PLAYER_CAPACITY_REACHED;
					}
				}
				toClient.writeObject(packetToClient);
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
			e.printStackTrace();
		}
	}

}
