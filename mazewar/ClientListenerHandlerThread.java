import java.io.*;
import java.net.*;
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
		Iterator<ClientState.ClientLocation> others = ClientState.others.iterator();
		boolean listening = true;
		while(listening) {
			while(others.hasNext()) {
				ClientState.ClientLocation other = others.next();
				try {
					MazewarPacket packetFromClient = (MazewarPacket) Mazewar.selfIn.readObject();
	
					if(packetFromClient != null) {
						System.out.println("Its not null");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			others = ClientState.others.iterator();
		}
	}

}
