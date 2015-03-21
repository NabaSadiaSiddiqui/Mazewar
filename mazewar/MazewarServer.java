import java.io.IOException;
import java.net.ServerSocket;

public class MazewarServer {

	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = null;
		boolean listening = true;

		try {
			if (args.length == 1) {
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port!");
			System.exit(-1);
		}

		while (listening) {
			try {
				new MazewarServerHandlerThread(serverSocket.accept()).start();
			} catch (IOException e) {
				System.err.println("ERROR: Could not listen on a connection");
				System.exit(-1);
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("ERROR: Could not close port!");
			System.exit(-1);
		}
	}

}
