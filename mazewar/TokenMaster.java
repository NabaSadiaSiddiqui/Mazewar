import java.io.*;

public class TokenMaster extends Thread {
	public TokenMaster() {
		super("TokenMaster");
	}
	
	public void run() {
		while(true) {
			try {
				if(canReleaseToken()) {
					ClientState.tokenLock.lock();
					ClientState.HAVE_TOKEN = false;
					MazewarPacket packetToNext = new MazewarPacket();
					packetToNext.type = MazewarPacket.CLIENT_TOKEN_EXCHANGE;
					ClientState.nextClient.getOut().writeObject(packetToNext);
					ClientState.tokenLock.unlock();
					Thread.sleep(100);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean canReleaseToken() {
		boolean res = false;
		ClientState.tokenLock.lock();
		res = ClientState.HAVE_TOKEN;
		ClientState.tokenLock.unlock();
		return res;
	}
}
