# Mazewar  

This is a **distributed**, multi-player (upto 4) implementation of Mazewar. Refer to the design document for more details.  
  
Team Members:  
* Naba Sadia Siddiqui (998072945)  
* Sameer Patel (998352526)  
  
Steps to run the game:  
1. Run 'Make'  
2. Start the server as `./server portNumber`  
3. Start the clients as `./run server_hostName server_portNumber client_hostName client_portNumber`
  
The game makes the following assumptions:  
1. All player names are unique  
2. All players enter the correct hostName and portNumber they are listening on  
3. Lookup server is always running  

**Known issues:**  
1. Before everyone can start playing, the first player to join the game has to first move forward and then again move backward to original position
