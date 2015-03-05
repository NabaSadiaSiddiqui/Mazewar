import java.io.*;
import java.net.*;

/**
 * Listens to incoming requests from other clients
 */
public class ClientListenerHandlerThread extends Thread {
	
	public ClientListenerHandlerThread() {
		super("ClientListenerHandlerThread");
		System.out.println("Created thread to listen to incoming actions from other players in the game");
	}
	
	public void run() {
		
	}

}
