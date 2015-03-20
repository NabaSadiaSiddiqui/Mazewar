import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class ClientServerListenerHandlerThread extends Thread {
	
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private GUIClient self;
	private Maze maze;
	private BlockingQueue<ClientLocation> peers;
	private ClientLocation next;
	private Lock tokenLock;
	private TokenMaster tokenMaster;
	private int ID_DEFAULT = -1;
	
	/**
	 * Socket through which communication will be made from other clients
	 */
	private ServerSocket selfSocket = null;
	private Socket selfConn = null;
	private ObjectInputStream selfIn = null;
	
	public ClientServerListenerHandlerThread(Socket socket, GUIClient self, Maze maze, BlockingQueue<ClientLocation> peers, ClientLocation nextClient, Lock tokenLock, ServerSocket selfSocket, Socket selfConn, ObjectInputStream selfIn, TokenMaster tokenMaster) {
		super("ClientServerListenerHandlerThread");
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			this.self = self;
			this.maze = maze;
			this.peers = peers;
			this.next = nextClient;
			this.tokenLock = tokenLock;
			this.tokenMaster = tokenMaster;
			this.selfSocket = selfSocket;
			this.selfConn = selfConn;
			this.selfIn = selfIn;
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
	    				System.out.println("Got a packet to broadcast players");
	    				Thread thread = new Thread() {
	    						public void run() {
	    							try {
										selfConn = selfSocket.accept();
										selfIn = new ObjectInputStream(selfConn.getInputStream());
					    				// Lets start the game
										new ClientListenerHandlerThread(peers, self, next, tokenLock, selfIn, tokenMaster).start();
	    							} catch (IOException e) {
										e.printStackTrace();
									}
	    						}};
	    				thread.start();
						
	    				addRemoteClients(packetFromServer);
	    				break;
	    			case MazewarPacket.SERVER_SET_TOKEN:
	    				tokenMaster.setHaveToken();
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
    	packetToServer.playerInfo = new PlayerMeta(ID_DEFAULT, name, location.getX(), location.getY(), orientation, self.getHostname(), self.getPort());
    	
    	try {
    		// Register with server
			out.writeObject(packetToServer);
        	// Check registration passed 
			MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
			if(packetFromServer.type == MazewarPacket.SERVER_ERROR) {
				System.err.println("ERROR: There are already too many players in the game");
				System.exit(1);
			} else if(packetFromServer.type == MazewarPacket.SERVER_OK) {
				Mazewar.consolePrintLn(name + ": registered successfully");
				self.setId(packetFromServer.clientId);
			}			
		} catch (IOException e) {
			System.err.println("ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}   
	}
	
	private void addRemoteClients(MazewarPacket packetFromServer) {
    	ConcurrentHashMap<String, PlayerMeta> activePlayers = packetFromServer.allPlayers;
		
		Enumeration<String> clientKeys = activePlayers.keys();
		
		int nextClientId = self.getId() + 1;
		if(nextClientId >= SharedData.MAX_PLAYERS) {
			nextClientId = 0;
		}
		
		System.out.println("Self client id is " + self.getId());
		System.out.println("Next client id is " + nextClientId);
		
		
		for(int i=0; i<SharedData.MAX_PLAYERS; i++) {
			String playerName = clientKeys.nextElement();

			final PlayerMeta player = (PlayerMeta) activePlayers.get(playerName);
			
			if(!self.getName().equals(playerName)) {
				RemoteClient client = new RemoteClient(playerName);
				Point point = new Point(player.getX(), player.getY());
				Direction direction = Direction.strToDir(player.getOrientation());
				maze.addClientAtPointWithDirection((Client) client, point, direction);
				
				// Add location of other client to the queue
				if(!isSelf(player.getHostname(), player.getPort())) {
					ClientLocation other = new ClientLocation(player.getHostname(), player.getPort(), player.getId(), player.getName());
					boolean added = peers.add(other);
					
					if(added) {
						System.out.println("Successfully added " + player.getId());
					} else {
						System.out.println("Error adding " + player.getId());
					}
					
					if(player.getId()==nextClientId) {
						this.next = other;
						System.out.println("Set next client in the ring to " + player.getName());
					}
				}
			}
		}
	}
	
	private boolean isSelf(String hostname, int port) {
		return self.getHostname().equals(hostname) && self.getPort() == port;
	}
}
