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
  
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * The entry point and glue code for the game.  It also contains some helpful
 * global utility methods.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {
		
		/**
		 * Socket for communication with the server
		 */
		static Socket socket = null;
		/**
		 * Data structures to read/write to/from out/in streams
		 */
		static ObjectOutputStream out = null;
		static ObjectInputStream in = null;
	

        /**
         * The default width of the {@link Maze}.
         */
        private final int mazeWidth = 20;

        /**
         * The default height of the {@link Maze}.
         */
        private final int mazeHeight = 10;

        /**
         * The default random seed for the {@link Maze}.
         * All implementations of the same protocol must use 
         * the same seed value, or your mazes will be different.
         */
        private final int mazeSeed = 42;

        /**
         * The {@link Maze} that the game uses.
         */
        private Maze maze = null;

        /**
         * The {@link GUIClient} for the game.
         */
        private GUIClient guiClient = null;

        /**
         * The panel that displays the {@link Maze}.
         */
        private OverheadMazePanel overheadPanel = null;

        /**
         * The table the displays the scores.
         */
        private JTable scoreTable = null;
        
        /** 
         * Create the textpane statically so that we can 
         * write to it globally using
         * the static consolePrint methods  
         */
        private static final JTextPane console = new JTextPane();
      
        /** 
         * Write a message to the console followed by a newline.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrintLn(String msg) {
                console.setText(console.getText()+msg+"\n");
        }
        
        /** 
         * Write a message to the console.
         * @param msg The {@link String} to print.
         */ 
        public static synchronized void consolePrint(String msg) {
                console.setText(console.getText()+msg);
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
                // Put any network clean-up code you might have here.
                // (inform other implementations on the network that you have 
                //  left, etc.)
                

                System.exit(0);
        }
       
        /** 
         * The place where all the pieces are put together. 
         */
        public Mazewar(String hostname, int port) {
                super("ECE419 Mazewar");
                consolePrintLn("ECE419 Mazewar started!");
                
                // Create the maze
                maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
                assert(maze != null);
                
                // Have the ScoreTableModel listen to the maze to find
                // out how to adjust scores.
                ScoreTableModel scoreModel = new ScoreTableModel();
                assert(scoreModel != null);
                maze.addMazeListener(scoreModel);
                
                // Throw up a dialog to get the GUIClient name.
                String name = JOptionPane.showInputDialog("Enter your name");
                if((name == null) || (name.length() == 0)) {
                  Mazewar.quit();
                }
                
                // You may want to put your network initialization code somewhere in
                // here.
                //TODO: put network initialization code here
                initNetwork(hostname, port);
                // Get a position that the client can occupy and register it with server
                Point point = registerPositionWithServer(maze);

                
                // Create the GUIClient and connect it to the KeyListener queue
                guiClient = new GUIClient(name);
                maze.addClientAtPoint(guiClient, point);
                this.addKeyListener(guiClient);
                
                //TODO: register client with server
                registerSelfWithServer(guiClient, maze);
                
                // Use braces to force constructors not to be called at the beginning of the
                // constructor.
                /*{
                        maze.addClient(new RobotClient("Norby"));
                        maze.addClient(new RobotClient("Robbie"));
                        maze.addClient(new RobotClient("Clango"));
                        maze.addClient(new RobotClient("Marvin"));
                }*/

                
                // Create the panel that will display the maze.
                overheadPanel = new OverheadMazePanel(maze, guiClient);
                assert(overheadPanel != null);
                maze.addMazeListener(overheadPanel);
                
                // Don't allow editing the console from the GUI
                console.setEditable(false);
                console.setFocusable(false);
                console.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
               
                // Allow the console to scroll by putting it in a scrollpane
                JScrollPane consoleScrollPane = new JScrollPane(console);
                assert(consoleScrollPane != null);
                consoleScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Console"));
                
                // Create the score table
                scoreTable = new JTable(scoreModel);
                assert(scoreTable != null);
                scoreTable.setFocusable(false);
                scoreTable.setRowSelectionAllowed(false);

                // Allow the score table to scroll too.
                JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
                assert(scoreScrollPane != null);
                scoreScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scores"));
                
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
                
                //TODO: get remote clients from the server and add them to the maze
                addRemoteClients(guiClient, maze);
                
                //Listen for server broadcasts
                attachBroadcastListener();
        }

        
        /**
         * Entry point for the game.  
         * @param args Command-line arguments.
         */
        public static void main(String args[]) {
        	
        	String hostname = "localhost";
			int port = 8080;
			
			if(args.length == 2) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}

            /* Create the GUI */
            new Mazewar(hostname, port);
        }
        
        private static void initNetwork(String hostname, int port) {
        	try {
    			socket = new Socket(hostname, port);
    			out = new ObjectOutputStream(socket.getOutputStream());
    			in = new ObjectInputStream(socket.getInputStream());
    		} catch(UnknownHostException e) {
    			System.err.println("ERROR: Don't know where to connect!");
    			System.exit(1);
    		} catch(IOException e) {
    			System.err.println("ERROR: Coudn't get I/O for the connection");
    			System.exit(1);
    		}
        }
        
        /**
         * Registers the position of the client with the server and returns it
         * @param maze
         * @return {@link Point} which the client can occupy on the maze
         */
        private static Point registerPositionWithServer(Maze maze) {        	
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
        				consolePrintLn("Successfully obtained a free spot to play");
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
        	
        	return position;
        }
        
        private static void registerSelfWithServer(Client client, Maze maze) {
        	        	
        	String name = client.getName();
        	Point location = client.getPoint();
        	String orientation = client.getOrientation().toString();
        	
        	/* Make a request to register */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_REGISTER;
        	packetToServer.playerInfo = new PlayerMeta(name, location.getX(), location.getY(), orientation);
        	
        	try {
				out.writeObject(packetToServer);
				
	        	// Check registration passed 
				MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
				if(packetFromServer.type == MazewarPacket.SERVER_ERROR) {
					System.err.println("ERROR: There are already too many players in the game");
					System.exit(1);
				}
				
				// Save self.name
				ClientState.PLAYER_NAME = name;
				consolePrintLn(name + ": registered successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			} catch (ClassNotFoundException e) {
				System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
				System.exit(1);
			}
        }
        
        private static void addRemoteClients(Client self, Maze maze) {      	
        	try {				
	        	/* Get remote clients */
				MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
				while(packetFromServer.type != MazewarPacket.SERVER_BROADCAST_PLAYERS) { // Wait to get the players
					packetFromServer = (MazewarPacket) in.readObject();
				}
				BlockingQueue<PlayerMeta> activePlayers = packetFromServer.activeClients;
				
				for(int i=0; i<SharedData.MAX_PLAYERS; i++) {
					PlayerMeta player = (PlayerMeta)activePlayers.remove();
					String playerName = player.name;
					if(!self.getName().equals(playerName)) {
						RemoteClient client = new RemoteClient(playerName);
						Point point = new Point(player.posX, player.posY);
						Direction direction = Direction.strToDir(player.orientation);
						maze.addClientAtPointWithDirection((Client) client, point, direction);
					}
				}	
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			} catch (ClassNotFoundException e) {
				System.err.println("ERROR: MazewarPacket class does not exist...uh oh");
				System.exit(1);
			}
        }
        
        public static boolean forward() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_FORWARD;
        	
        	try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to move forward to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}
        	
        	return false;
        }
        
        public static boolean backup() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_BACKWARD;
        	
        	try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to move backward to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}
        	
        	return false;
        }
        
        public static boolean turnLeft() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_LEFT;
        	
        	try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to turn left to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}
        	
        	return false;
        }
        
        public static boolean turnRight() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_RIGHT;
        	
        	try {
				out.writeObject(packetToServer);			
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to turn right to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}
        	
        	return false;
        }
        
        public static boolean fire() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_FIRE;
        	
        	try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to fire to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}
        	
        	return false;
        }
        
        private void attachBroadcastListener() {
        	try {				
	        	/* Get moves */
        		MazewarPacket packetFromServer = (MazewarPacket) in.readObject();
        		while(packetFromServer != null) {
    				while(packetFromServer.type != MazewarPacket.SERVER_BROADCAST_MOVE) { // Wait to get the player actions
    					packetFromServer = (MazewarPacket) in.readObject();
    				}
    				SharedData.ActionInfo playerMove = packetFromServer.move;
    				String playerName = playerMove.getPlayerName();
    				int playerAction = playerMove.getAction();
    				int playerTime = playerMove.getTime();
    				
    				switch(playerAction) {
    					case MazewarPacket.CLIENT_FORWARD:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: forward");
    						break;
    					case MazewarPacket.CLIENT_BACKWARD:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: backward");
    						break;
    					case MazewarPacket.CLIENT_LEFT:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: left");
    						break;
    					case MazewarPacket.CLIENT_RIGHT:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: right");
    						break;
    					case MazewarPacket.CLIENT_FIRE:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: fire");
    						break;
    					case MazewarPacket.CLIENT_QUIT:
    						consolePrintLn("Received packet: " + playerMove.toString());
    						consolePrintLn("Action: quit");
    						break;
        				default:
        					consolePrintLn("Should we have gotten here?");
        					break;
    				}
    				
    				//consolePrintLn("Received packet: " + playerMove.toString());
    				
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
}
