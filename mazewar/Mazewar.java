/*
Copyright (C) 2004 Geoffrey Alan Washburn
   
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
   
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
   
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * The entry point and glue code for the game. It also contains some helpful
 * global utility methods.
 * 
 * @author Geoffrey Washburn &lt;<a
 *         href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {

	/**
	 * Socket for communication with the server
	 */
	static Socket socket = null;

	/**
	 * Socket through which communication will be made from other clients
	 */
	private static ServerSocket selfSocket = null;

	/**
	 * Queue of all other players in the game and their location
	 */
	public static BlockingQueue<ClientLocation> peers = new ArrayBlockingQueue<ClientLocation>(SharedData.MAX_PLAYERS - 1);
	// The player next to the self player in the ring
	private static ClientLocation nextClient;
	// Token master to manage token
	public static TokenMaster tokenMaster;

	/**
	 * The default width of the {@link Maze}.
	 */
	private final int mazeWidth = 20;

	/**
	 * The default height of the {@link Maze}.
	 */
	private final int mazeHeight = 10;

	/**
	 * The default random seed for the {@link Maze}. All implementations of the
	 * same protocol must use the same seed value, or your mazes will be
	 * different.
	 */
	private final int mazeSeed = 42;

	/**
	 * The {@link Maze} that the game uses.
	 */
	public static Maze maze = null;

	/**
	 * The {@link GUIClient} for the game.
	 */
	public static GUIClient guiClient = null;

	/**
	 * The panel that displays the {@link Maze}.
	 */
	private OverheadMazePanel overheadPanel = null;

	/**
	 * The table the displays the scores.
	 */
	private JTable scoreTable = null;

	/**
	 * Create the textpane statically so that we can write to it globally using
	 * the static consolePrint methods
	 */
	private static final JTextPane console = new JTextPane();

	/**
	 * Write a message to the console followed by a newline.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrintLn(String msg) {
		console.setText(console.getText() + msg + "\n");
	}

	/**
	 * Write a message to the console.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrint(String msg) {
		console.setText(console.getText() + msg);
	}

	/**
	 * Clear the console.
	 */
	public static synchronized void clearConsole() {
		console.setText("");
	}

	/**
	 * Static method for performing cleanup before exiting the game.
	 */
	public static void quit() {
		System.exit(0);
	}

	/**
	 * The place where all the pieces are put together.
	 */
	public Mazewar(String hostname, int port, String selfHostname, int selfPort) {
		super("ECE419 Mazewar");
		consolePrintLn("ECE419 Mazewar started!");

		// Create the maze
		maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
		assert (maze != null);

		// Have the ScoreTableModel listen to the maze to find
		// out how to adjust scores.
		ScoreTableModel scoreModel = new ScoreTableModel();
		assert (scoreModel != null);
		maze.addMazeListener(scoreModel);

		// Throw up a dialog to get the GUIClient name.
		String name = JOptionPane.showInputDialog("Enter your name");
		if ((name == null) || (name.length() == 0)) {
			Mazewar.quit();
		}

		// You may want to put your network initialization code somewhere in
		// here.
		// TODO: put network initialization code here
		initNetwork(hostname, port);

		// Create the GUIClient and connect it to the KeyListener queue
		guiClient = new GUIClient(name);
		this.addKeyListener(guiClient);
		guiClient.setHostname(selfHostname);
		guiClient.setPort(selfPort);

		// Lets set up and register with the server
		new ClientServerListenerHandlerThread(socket, guiClient, maze,
				nextClient, selfSocket).start();

		// Create the panel that will display the maze.
		overheadPanel = new OverheadMazePanel(maze, guiClient);
		assert (overheadPanel != null);
		maze.addMazeListener(overheadPanel);

		// Don't allow editing the console from the GUI
		console.setEditable(false);
		console.setFocusable(false);
		console.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder()));

		// Allow the console to scroll by putting it in a scrollpane
		JScrollPane consoleScrollPane = new JScrollPane(console);
		assert (consoleScrollPane != null);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Console"));

		// Create the score table
		scoreTable = new JTable(scoreModel);
		assert (scoreTable != null);
		scoreTable.setFocusable(false);
		scoreTable.setRowSelectionAllowed(false);

		// Allow the score table to scroll too.
		JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
		assert (scoreScrollPane != null);
		scoreScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Scores"));

		// Create the layout manager
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(layout);

		// Define the constraints on the components.
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 3.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(overheadPanel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		c.weighty = 1.0;
		layout.setConstraints(consoleScrollPane, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		layout.setConstraints(scoreScrollPane, c);

		// Add the components
		getContentPane().add(overheadPanel);
		getContentPane().add(consoleScrollPane);
		getContentPane().add(scoreScrollPane);

		// Pack everything neatly.
		pack();

		// Let the magic begin.
		setVisible(true);
		overheadPanel.repaint();
		this.requestFocusInWindow();
	}

	/**
	 * Entry point for the game.
	 * 
	 * @param args
	 *            Command-line arguments.
	 */
	public static void main(String args[]) {

		String hostnameLookupServer = "localhost";
		int portLookupServer = 8080;
		String hostname = null;
		int port = -1;

		tokenMaster = new TokenMaster();

		try {
			if (args.length == 4) {
				hostnameLookupServer = args[0];
				portLookupServer = Integer.parseInt(args[1]);

				hostname = args[2];
				port = Integer.parseInt(args[3]);
				selfSocket = new ServerSocket(port);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("ERROR: Client could not listen on port!");
			System.exit(-1);
		}

		/* Create the GUI */
		new Mazewar(hostnameLookupServer, portLookupServer, hostname, port);
	}

	private void initNetwork(String hostname, int port) {
		try {
			socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Coudn't get I/O for the connection");
			System.exit(1);
		}
	}

	public static void respawn(Point point, Direction d) {
		ClientServerListenerHandlerThread.clientThread.respawn(point, d);
	}

	public static boolean removePlayer(String name) {
		Iterator allClients = Mazewar.maze.getClients();
		Client target = null;
		while (allClients.hasNext()) {
			Client client = (Client) allClients.next();
			if (client.getName().equals(name)) {
				target = client;
				break;
			}
		}
		Mazewar.maze.removeClient(target);

		return true;
	}

	public static void mQuit() {
		ClientServerListenerHandlerThread.clientThread.quit();
	}

	public static void mForward() {
		ClientServerListenerHandlerThread.clientThread.forward();
	}

	public static void mBackup() {
		ClientServerListenerHandlerThread.clientThread.backup();
	}

	public static void mFire() {
		ClientServerListenerHandlerThread.clientThread.fire();
	}

	public static void mLeft() {
		ClientServerListenerHandlerThread.clientThread.left();
	}

	public static void mRight() {
		ClientServerListenerHandlerThread.clientThread.right();
	}
}
