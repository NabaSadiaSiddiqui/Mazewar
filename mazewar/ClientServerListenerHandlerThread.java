import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

public class ClientServerListenerHandlerThread extends Thread {

	private String TAG = "ClientServerListenerHandlerThread";
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private GUIClient self;
	private Maze maze;
	private int ID_DEFAULT = -1;

	/**
	 * Socket through which communication will be made from other clients
	 */
	private ServerSocket selfSocket = null;

	public ClientServerListenerHandlerThread(Socket socket, GUIClient self,
			Maze maze, ServerSocket selfSocket) {
		super("ClientServerListenerHandlerThread");
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			this.self = self;
			this.maze = maze;
			this.selfSocket = selfSocket;
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
			while (packetFromServer != null) {

				int type = packetFromServer.type;

				switch (type) {
				case MazewarPacket.SERVER_BROADCAST_PLAYERS:
					// Lets start the game
					addRemoteClients(packetFromServer);
					Thread t = new Thread() {
						public void run() {
							while(true) {
								try {
									new ClientListenerHandlerThread(self, selfSocket.accept()).start();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					};
					t.start();
					break;
				case MazewarPacket.SERVER_SET_TOKEN:
					Mazewar.tokenMaster.setHaveToken();
					break;
				default:
					break;
				}

				packetFromServer = (MazewarPacket) in.readObject();
			}

		} catch (IOException e) {
			System.err.println(TAG+"::"+"Line80::ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err
					.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}
	}

	/**
	 * Registers a position (x,y) on the maze with the server as its init
	 * coordinates
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
			while (!gotAck) {
				if (packetFromServer.type == MazewarPacket.SERVER_NACK_JOIN) {

					occupiedCells.add(position);
					position = maze.getEmptyCell(occupiedCells);

					packetToServer = new MazewarPacket();
					packetToServer.type = MazewarPacket.CLIENT_JOIN;
					packetToServer.myPosition = position;

					out.writeObject(packetToServer);

					packetFromServer = (MazewarPacket) in.readObject();

				} else if (packetFromServer.type == MazewarPacket.SERVER_ACK_JOIN) {
					Mazewar.consolePrintLn("Successfully obtained a free spot to play");
					gotAck = true;

				} else {
					System.out
							.println("ERROR: We just got an alien packet. It goes by the code "
									+ String.valueOf(packetFromServer.type));
				}
			}
		} catch (IOException e) {
			System.err.println(TAG+"::"+"Line132::ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err
					.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}
		Point point = position;
		maze.addClientAtPoint(self, point);
	}

	/**
	 * Registers the identity and location (hostname, port number) with the
	 * server
	 */
	private void registerSelf() {
		String name = self.getName();
		Point location = self.getPoint();
		String orientation = self.getOrientation().toString();

		// Make a request to register
		MazewarPacket packetToServer = new MazewarPacket();
		packetToServer.type = MazewarPacket.CLIENT_REGISTER;
		packetToServer.playerInfo = new PlayerMeta(ID_DEFAULT, name,
				location.getX(), location.getY(), orientation,
				self.getHostname(), self.getPort());

		try {
			// Register with server
			out.writeObject(packetToServer);
			// Check registration passed
			MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
			if (packetFromServer.type == MazewarPacket.SERVER_ERROR) {
				System.err
						.println("ERROR: There are already too many players in the game");
				System.exit(1);
			} else if (packetFromServer.type == MazewarPacket.SERVER_OK) {
				Mazewar.consolePrintLn(name + ": registered successfully");
				self.setId(packetFromServer.clientId);
			}
		} catch (IOException e) {
			System.err.println(TAG+"::"+"Line173::ERROR: Could not write to output stream");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err
					.println("ERROR: MazewarPacket class does not exist...uh oh");
			System.exit(1);
		}
	}

	private void addRemoteClients(MazewarPacket packetFromServer) {
		ConcurrentHashMap<String, PlayerMeta> activePlayers = packetFromServer.allPlayers;

		Enumeration<String> clientKeys = activePlayers.keys();

		int nextClientId = self.getId() + 1;
		if (nextClientId >= SharedData.MAX_PLAYERS) {
			nextClientId = 0;
		}

		for (int i = 0; i < SharedData.MAX_PLAYERS; i++) {
			String playerName = clientKeys.nextElement();

			PlayerMeta player = (PlayerMeta) activePlayers.get(playerName);

			if (!self.getName().equals(playerName)) {
				RemoteClient client = new RemoteClient(playerName);
				Point point = new Point(player.getX(), player.getY());
				Direction direction = Direction.strToDir(player
						.getOrientation());
				maze.addClientAtPointWithDirection((Client) client, point,
						direction);

				// Add location of other client to the queue
				if (!isSelf(player.getHostname(), player.getPort())) {
					ClientLocation other = new ClientLocation(player.getHostname(),
							player.getPort(), player.getId(), player.getName());
					
					Mazewar.peers.add(other);
					
					if (player.getId() == nextClientId) {
						Mazewar.next = other;
						System.out.println("Set next client in the ring to "
								+ player.getName());
					}
				}
			}
		}
		
		System.out.println("Added peers...");
		Iterator allPeers = Mazewar.peers.iterator();
		while(allPeers.hasNext()) {
			ClientLocation c = (ClientLocation) allPeers.next();
			System.out.println(c.toString());
		}
	}

	private boolean isSelf(String hostname, int port) {
		return self.getHostname().equals(hostname) && self.getPort() == port;
	}
	
	public void unregisterSelf() {
		String name = self.getName();

		MazewarPacket packetToServer = new MazewarPacket();
		packetToServer.player = self.getName();
		packetToServer.type = MazewarPacket.CLIENT_QUIT;

		try {
			// Tell server you are quiting
			out.writeObject(packetToServer);
		} catch (IOException e) {
			System.err.println(TAG+"::"+"Line244::ERROR: Could not write to output stream");
			System.exit(1);
		}
	}
}
