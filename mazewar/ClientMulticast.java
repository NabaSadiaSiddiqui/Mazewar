import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

public class ClientMulticast {
	private BlockingQueue<ClientLocation> peers;
	private GUIClient self;
	// Lock to manage access to the token
	private Lock tokenLock;

	public ClientMulticast(BlockingQueue<ClientLocation> peers, GUIClient self,
			Lock tokenLock) {
		this.peers = peers;
		this.self = self;
		this.tokenLock = tokenLock;
	}

	public void mCast(int action, PlayerMeta newPosition,
			TokenMaster tokenMaster) {
		tokenMaster.setNeedToken();

		MazewarPacket packetToOthers = new MazewarPacket();
		packetToOthers.type = MazewarPacket.CLIENT_ACTION;
		packetToOthers.player = Mazewar.guiClient.getName();

		switch (action) {
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
		case MazewarPacket.CLIENT_FIRE:
			packetToOthers.action = action;
			break;
		case MazewarPacket.CLIENT_RESPAWN:
			packetToOthers.action = action;
			packetToOthers.playerInfo = newPosition;
			break;
		case MazewarPacket.CLIENT_QUIT:
			packetToOthers.action = action;
			packetToOthers.have_token = true;
			break;
		default:
			System.err.println("What action did you just perform?!?!");
			break;
		}

		if (action == MazewarPacket.CLIENT_RESPAWN) {
			Iterator<ClientLocation> others = peers.iterator();
			while (others.hasNext()) {
				ClientLocation other = others.next();
				try {
					other.getOut().writeObject(packetToOthers);
					Mazewar.consolePrintLn(self.getName()
							+ ": sent action to respawn to others successfully");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			String playerName = self.getName();
			Iterator allClients = Mazewar.maze.getClients();
			Client player = null;
			while (allClients.hasNext()) {
				player = (Client) allClients.next();

				if (player.getName().equals(playerName))
					break;
			}

			switch (action) {
			case MazewarPacket.CLIENT_RESPAWN:
				System.out.println("ClientMulticast: Respawn self");
				String name = newPosition.getName();
				Point p = new Point(newPosition.getX(), newPosition.getY());
				Direction d = Direction.strToDir(newPosition.getOrientation());
				player.respawn(name, p, d);
				break;
			default:
				System.err.println("What action did you just perform?!?!");
				break;
			}
		} else if (action == MazewarPacket.CLIENT_QUIT) {
			Iterator<ClientLocation> others = peers.iterator();
			while (others.hasNext()) {
				ClientLocation other = others.next();
				try {
					other.getOut().writeObject(packetToOthers);
					Mazewar.consolePrintLn(self.getName()
							+ ": sent action to respawn to others successfully");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Mazewar.quit();
		} else {
			tokenLock.lock();
			if (tokenMaster.haveToken()) {
				Iterator<ClientLocation> others = peers.iterator();
				System.out.println("ClientMulticast::before while loop");
				while (others.hasNext()) {
					System.out.println("ClientMulticast::inside while loop");
					ClientLocation other = others.next();
					try {
						other.getOut().writeObject(packetToOthers);
						Mazewar.consolePrintLn(self.getName()
								+ ": sent action to " + other.getName());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("ClientMulticast::end of while loop");

				// Now make the move on our end as well
				String playerName = self.getName();
				Iterator allClients = Mazewar.maze.getClients();
				Client player = null;
				while (allClients.hasNext()) {
					player = (Client) allClients.next();

					if (player.getName().equals(playerName))
						break;
				}

				switch (action) {
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
				case MazewarPacket.CLIENT_FIRE:
					player.fire();
					break;
				case MazewarPacket.CLIENT_RESPAWN:
					String name = newPosition.getName();
					Point p = new Point(newPosition.getX(), newPosition.getY());
					Direction d = Direction.strToDir(newPosition
							.getOrientation());
					player.respawn(name, p, d);
					break;
				default:
					System.err.println("What action did you just perform?!?!");
					break;
				}
			} else {
				System.out.println("Do you really wanna be sending this...");
			}
			tokenLock.unlock();
		}
	}

	public void sendAck() {
		MazewarPacket packetToOthers = new MazewarPacket();
		packetToOthers.type = MazewarPacket.CLIENT_ACK;
		packetToOthers.player = Mazewar.guiClient.getName();

		Iterator<ClientLocation> others = peers.iterator();
		while (others.hasNext()) {
			ClientLocation other = others.next();
			try {
				other.getOut().writeObject(packetToOthers);
				Mazewar.consolePrintLn(Mazewar.guiClient.getName()
						+ ": sent ack to others");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
