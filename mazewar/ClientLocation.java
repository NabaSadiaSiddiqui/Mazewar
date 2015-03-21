import java.io.*;
import java.net.*;

public class ClientLocation {
	// Name of the player
	private String name;
	// Unique id of the client, assigned by the server
	private int id;

	/**
	 * Socket for communication with the client
	 */
	private static Socket socket = null;
	/**
	 * Data structures to read/write to/from out/in stream
	 */
	private static ObjectOutputStream out = null;
	private static ObjectInputStream in = null;

	public ClientLocation(String hostname, int port, int id, String name) {
		this.id = id;
		this.name = name;

		try {
			socket = new Socket(hostname, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			System.out.println("ClientLocation::Created output stream");
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR: Coudn't get I/O for the connection");
			e.printStackTrace();
		}
	}

	public ObjectOutputStream getOut() {
		if (!socket.isConnected()) {
			System.err.println("ClientLocation::socket is closed");
		}

		if (out == null) {
			System.err.println("Output stream is null");
		}

		return out;
	}

	public ObjectInputStream getIn() {
		if (in == null) {
			try {
				InputStream iStream = socket.getInputStream();
				while (iStream == null) {
					iStream = socket.getInputStream();
				}
				in = new ObjectInputStream(iStream);
				System.out.println("ClientLocation::got streams to read from");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!socket.isConnected()) {
			System.err.println("ClientLocation::socket is closed");
		}

		return in;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
