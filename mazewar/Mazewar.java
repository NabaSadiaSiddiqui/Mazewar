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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

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
		 * Socket through which communication will be made from other clients
		 */
		public static ServerSocket selfSocket = null;
		
		/**
		 * State to indicate if socket is accepting connections
		 */
		public static boolean isOpen = false;

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
                
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_QUIT;
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to quit to server successfully");
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	//return false;
        	
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

                
                // Create the GUIClient and connect it to the KeyListener queue
                guiClient = new GUIClient(name);
                this.addKeyListener(guiClient);
                
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
                
                // Lets set up and register with the server
                new ClientServerListenerHandlerThread(socket, guiClient, maze).start();
        }

        
        /**
         * Entry point for the game.  
         * @param args Command-line arguments.
         */
        public static void main(String args[]) {
        	
        	String hostnameLookupServer = "localhost";
			int portLookupServer = 8080;
						
			try {
				if(args.length == 4) {
					hostnameLookupServer = args[0];
					portLookupServer = Integer.parseInt(args[1]);
					
					ClientState.hostname = args[2];
					ClientState.port = Integer.parseInt(args[3]);
					
					selfSocket = new ServerSocket(ClientState.port);
				} else {
					System.err.println("ERROR: Invalid arguments!");
					System.exit(-1);
				}
			} catch (IOException e) {
				System.err.println("ERROR: Client could not listen on port!");
				System.exit(-1);
			}

            /* Create the GUI */
            new Mazewar(hostnameLookupServer, portLookupServer);
        }
        
        private static void initNetwork(String hostname, int port) {
        	try {
    			socket = new Socket(hostname, port);
    		} catch(UnknownHostException e) {
    			System.err.println("ERROR: Don't know where to connect!");
    			System.exit(1);
    		} catch(IOException e) {
    			System.err.println("ERROR: Coudn't get I/O for the connection");
    			System.exit(1);
    		}
        }
        
        public static void addRemoteClients(Client self, Maze maze, MazewarPacket packetFromServer) {
			//BlockingQueue<PlayerMeta> activePlayers = packetFromServer.activeClients;

        	ConcurrentHashMap<String, ServerState.PlayerDetails> activePlayers = packetFromServer.allPlayers;
			
			Enumeration<String> clientKeys = activePlayers.keys();
						
			for(int i=0; i<SharedData.MAX_PLAYERS; i++) {
				//PlayerMeta player = (PlayerMeta)activePlayers.remove();
				String playerName = clientKeys.nextElement();
				ServerState.PlayerDetails player = (ServerState.PlayerDetails) activePlayers.get(playerName);
				if(!self.getName().equals(playerName)) {
					RemoteClient client = new RemoteClient(playerName);
					Point point = new Point(player.getX(), player.getY());
					Direction direction = Direction.strToDir(player.getOrientation());
					maze.addClientAtPointWithDirection((Client) client, point, direction);
					
					// Add location of other client to the queue
					
					if(!ClientState.isSelfLocation(player.getHostname(), player.getPort())) {
						ClientState.others.add(new ClientState.ClientLocation(player.getHostname(), player.getPort()));
						System.out.println("Non-blocking");
					}
				}
			}
        }
        
        public static boolean forward() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_FORWARD;
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to move forward to server successfully");
			} catch (IOException e) {
				System.err.println("4");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean backup() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_BACKWARD;
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to move backward to server successfully");
			} catch (IOException e) {
				System.err.println("5");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean turnLeft() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_LEFT;
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to turn left to server successfully");
			} catch (IOException e) {
				System.err.println("6");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean turnRight() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_RIGHT;
        	
        	/*try {
				out.writeObject(packetToServer);			
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to turn right to server successfully");
			} catch (IOException e) {
				System.err.println("7");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean fire() {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_FIRE;
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to fire to server successfully");
			} catch (IOException e) {
				System.err.println("8");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean respawn(Point point, Direction d) {
        	/* Send action to server */
        	MazewarPacket packetToServer = new MazewarPacket();
        	packetToServer.type = MazewarPacket.CLIENT_ACTION;
        	packetToServer.player = ClientState.PLAYER_NAME;
        	packetToServer.action = MazewarPacket.CLIENT_RESPAWN;
        	
        	//packetToServer.playerInfo = new PlayerMeta(ClientState.PLAYER_NAME, point.getX(), point.getY(), d.toString());
        	
        	
        	/*try {
				out.writeObject(packetToServer);
				consolePrintLn(ClientState.PLAYER_NAME + ": sent action to respawn to server successfully");
			} catch (IOException e) {
				System.err.println("9");
				System.err.println("ERROR: Could not write to output stream");
				System.exit(1);
			}*/
        	
        	return false;
        }
        
        public static boolean removePlayer(String name) {
        	Iterator allClients = Mazewar.maze.getClients();
        	Client target = null;
        	while(allClients.hasNext()) {
        		Client client = (Client) allClients.next();
        		if(client.getName().equals(name)) {
        			target = client;
        			break;
        		}
        	}
        	Mazewar.maze.removeClient(target);
        	
        	return true;
        }
        
        private void checkAction(MazewarPacket packetFromServer) {
			//SharedData.ActionInfo playerMove = packetFromServer.move;
			SharedData.ActionInfo playerMove = ClientState.actionQueue.get(String.valueOf(ClientState.CURR_TIME));
        	if(playerMove == null) {
        		return;
        	}
			ClientState.CURR_TIME++;
			String playerName = playerMove.getPlayerName();
			int playerAction = playerMove.getAction();
			//int playerTime = playerMove.getTime();
						
			Iterator allClients = Mazewar.maze.getClients();
			Client player = null;
			while(allClients.hasNext()) {
				player = (Client) allClients.next();
				
				if(player.getName().equals(playerName))
					break;
			}
			
			
			switch(playerAction) {
				case MazewarPacket.CLIENT_FORWARD:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: forward");
					player.forward();
					break;
				case MazewarPacket.CLIENT_BACKWARD:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: backward");
					player.backup();
					break;
				case MazewarPacket.CLIENT_LEFT:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: left");
					player.turnLeft();
					break;
				case MazewarPacket.CLIENT_RIGHT:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: right");
					player.turnRight();
					break;
				case MazewarPacket.CLIENT_FIRE:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: fire");
					player.fire();
					break;
				case MazewarPacket.CLIENT_RESPAWN:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: respawn");
					PlayerMeta addPlayer = playerMove.getPlayerMeta();
					consolePrintLn("Add player " + addPlayer.name + " at x, y positions of " + addPlayer.posX + " " + addPlayer.posY);	
					player.respawn(addPlayer.name, new Point(addPlayer.posX, addPlayer.posY), Direction.strToDir(addPlayer.orientation));
					break;
				case MazewarPacket.CLIENT_QUIT:
					consolePrintLn("Received packet: " + playerMove.toString());
					consolePrintLn("Action: quit");
					removePlayer(playerName);
					//quit();
					break;
				default:
					consolePrintLn("Should we have gotten here?");
					break;
			}
		
        }
        
        private void enqueueAction(MazewarPacket packetFromServer) {
        	SharedData.ActionInfo playerMove = packetFromServer.move;
			String playerName = playerMove.getPlayerName();
			int playerAction = playerMove.getAction();
			int playerTime = playerMove.getTime();
						
			ClientState.actionQueue.put(String.valueOf(playerTime), playerMove);				
        }
}
