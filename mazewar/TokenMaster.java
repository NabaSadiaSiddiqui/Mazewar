import java.io.*;

public class TokenMaster extends Thread {
	public TokenMaster() {
		super("TokenMaster");
	}
	
	public void run() {
		while(true) {
			if(canReleaseToken()) {
				ClientState.tokenLock.lock();
				ClientState.HAVE_TOKEN = false;
				MazewarPacket packetToNext = new MazewarPacket();
				packetToNext.type = MazewarPacket.CLIENT_TOKEN_EXCHANGE;
				try {
					ClientState.nextClient.getOut().writeObject(packetToNext);
				} catch (IOException e) {
					e.printStackTrace();
				}
				ClientState.tokenLock.unlock();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean canReleaseToken() {
		return ClientState.HAVE_TOKEN;
	}
}
