import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientServerListenerHandlerThread extends Thread {
	
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private Client self;
	private Maze maze;
	
	public ClientServerListenerHandlerThread(Socket socket, Client self, Maze maze) {
		super("ClientServerListenerHandlerThread");
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			this.self = self;
			this.maze = maze;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		System.out.println("Registering a point on the maze with the server");
		registerPosition();
		
		System.out.println("Registering self with the server");
		registerSelf();
    	
    	// Get remote clients on the maze   	
    	try {			
			// Get other players in the game
    		MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
    		while(packetFromServer != null) {
    			
    			int type = packetFromServer.type;
    			
    			switch(type) {
	    			case MazewarPacket.SERVER_BROADCAST_PLAYERS:
	    				Thread thread = new Thread() {
	    						public void run() {
	    							try {
	    								System.out.println("Socket is not connected");
										Mazewar.selfConn = Mazewar.selfSocket.accept();
										System.out.println("isInputShutdown is " + Mazewar.selfConn.isInputShutdown());
										Mazewar.selfIn = new ObjectInputStream(Mazewar.selfConn.getInputStream());
										System.out.println("isInputShutdown is " + Mazewar.selfConn.isInputShutdown());
					    				// Lets start the game
					                    new ClientListenerHandlerThread().start();
	    							} catch (IOException e) {
										e.printStackTrace();
									}
	    						}};
	    				thread.start();
	    				Mazewar.addRemoteClients(self, maze, packetFromServer);
	    				break;
    				default:
    					break;
    			}
				
    			packetFromServer = (MazewarPacket) in.readObject();
    		}
			
		} catch (IOException e) {
			System.err.println("ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}   
	}
	
	/**
	 * Registers a position (x,y) on the maze with the server as its init coordinates
	 */
	private void registerPosition() {
		// First get a position you can start at
    	ArrayList<Point> occupiedCells = new ArrayList<Point>();
    	// Get an available cell
    	Point position = maze.getEmptyCell(occupiedCells);
    	/* Make a request to server with available position */
    	MazewarPacket packetToServer = new MazewarPacket(); 
    	packetToServer.type = MazewarPacket.CLIENT_JOIN;
    	packetToServer.myPosition = position;
    	try {
    		out.writeObject(packetToServer);
    		// Ensure that the position is available for occupancy
    		boolean gotAck = false;
    		MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
    		while(!gotAck) {
    			if(packetFromServer.type == MazewarPacket.SERVER_NACK_JOIN) {
    	        	
    				occupiedCells.add(position);
    				position = maze.getEmptyCell(occupiedCells);
    				
    				packetToServer = new MazewarPacket();
    				packetToServer.type = MazewarPacket.CLIENT_JOIN;
    	        	packetToServer.myPosition = position;
    	        	
    	        	out.writeObject(packetToServer);
    	        	
    	        	packetFromServer = (MazewarPacket) in.readObject();
    			
    			} else if(packetFromServer.type == MazewarPacket.SERVER_ACK_JOIN) {
    				Mazewar.consolePrintLn("Successfully obtained a free spot to play");
					gotAck = true;
				
    			} else {
					System.out.println("ERROR: We just got an alien packet. It goes by the code " + String.valueOf(packetFromServer.type));
				}
    		}
    	} catch (IOException e) {
			System.err.println("ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}
    	Point point = position;
        maze.addClientAtPoint(self, point);
	}
	
	/**
	 * Registers the identity and location (hostname, port number) with the server
	 */
	private void registerSelf() {
    	String name = self.getName();
    	Point location = self.getPoint();
    	String orientation = self.getOrientation().toString();
    	
    	// Make a request to register
    	MazewarPacket packetToServer = new MazewarPacket();
    	packetToServer.type = MazewarPacket.CLIENT_REGISTER;
    	packetToServer.playerInfo = new PlayerMeta(name, location.getX(), location.getY(), orientation, ClientState.hostname, ClientState.port);
    	
    	try {
    		// Register with server
			out.writeObject(packetToServer);
        	// Check registration passed 
			MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
			if(packetFromServer.type == MazewarPacket.SERVER_ERROR) {
				System.err.println("ERROR: There are already too many players in the game");
				System.exit(1);
			}
			// Save self state
			ClientState.PLAYER_NAME = name;
			ClientState.PLAYER_POINT = location;
			Mazewar.consolePrintLn(name + ": registered successfully");
		} catch (IOException e) {
			System.err.println("ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}   
	}
}
