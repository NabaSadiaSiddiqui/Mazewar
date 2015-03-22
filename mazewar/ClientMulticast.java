import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

public class ClientMulticast {

	public ClientMulticast() {
	}

	public void mCast(int action, PlayerMeta newPosition) {
		Mazewar.tokenMaster.setNeedToken();

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
			Iterator<ClientLocation> others = Mazewar.peers.iterator();
			while (others.hasNext()) {
				ClientLocation other = others.next();
				try {
					other.getOut().writeObject(packetToOthers);
					Mazewar.consolePrintLn("Sent action to respawn to "
							+ other.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			String playerName = Mazewar.guiClient.getName();
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
		} else {
			Mazewar.tokenMaster.acquireLock();
			if (Mazewar.tokenMaster.haveToken()) {
				Iterator<ClientLocation> others = Mazewar.peers.iterator();

				while (others.hasNext()) {
					ClientLocation other = others.next();
					try {
						other.getOut().writeObject(packetToOthers);
						Mazewar.consolePrintLn("Sent action to " + other.toString());
					} catch (InvalidClassException e) {
						e.printStackTrace();
					} catch (NotSerializableException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// Now make the move on our end as well
				String playerName = Mazewar.guiClient.getName();
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
				case MazewarPacket.CLIENT_QUIT:
					Mazewar.quit();
					break;
				default:
					System.err.println("What action did you just perform?!?!");
					break;
				}
			} else {
				System.out.println("Do you really wanna be sending this...");
			}
			Mazewar.tokenMaster.releaseLock();
		}
	}

	public void sendAck() {
		MazewarPacket packetToOthers = new MazewarPacket();
		packetToOthers.type = MazewarPacket.CLIENT_ACK;
		packetToOthers.player = Mazewar.guiClient.getName();

		Iterator<ClientLocation> others = Mazewar.peers.iterator();
		while (others.hasNext()) {
			ClientLocation other = others.next();
			try {
				other.getOut().writeObject(packetToOthers);
				other.getOut().flush();
				Mazewar.consolePrintLn(Mazewar.guiClient.getName()
						+ ": sent ack to others");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
