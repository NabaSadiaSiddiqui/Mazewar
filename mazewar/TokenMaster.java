import java.io.*;

public class TokenMaster extends Thread {
	public TokenMaster() {
		super("TokenMaster");
	}
	
	public void run() {
		//while(true) {	
		System.out.println("Passing token to next player in the ring");
			if(!TokenMaster.needToken() && TokenMaster.haveToken()) { // dont need it BUT have it
				TokenMaster.passToken();
			} /*else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
		//}
	}
	
	public static void passToken() {
		try {
			ClientState.tokenLock.lock();
			ClientState.HAVE_TOKEN = false;
			MazewarPacket packetToNext = new MazewarPacket();
			packetToNext.type = MazewarPacket.CLIENT_TOKEN_EXCHANGE;
			ClientState.nextClient.getOut().writeObject(packetToNext);
			ClientState.tokenLock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean needToken() {
		boolean need;
		ClientState.tokenLock.lock();
		need = ClientState.NEED_TOKEN;
		ClientState.tokenLock.unlock();
		return need;
	}
	
	public static boolean haveToken() {
		boolean have;
		ClientState.tokenLock.lock();
		have = ClientState.HAVE_TOKEN;
		ClientState.tokenLock.unlock();
		return have;
	}
	
	public static void setNeedToken() {
		ClientState.tokenLock.lock();
		ClientState.NEED_TOKEN = true;
		ClientState.tokenLock.unlock();
	}
	
	public static void unsetNeedToken() {
		ClientState.tokenLock.lock();
		ClientState.NEED_TOKEN = false;
		ClientState.tokenLock.unlock();
	}
	
	public static void setHaveToken() {
		ClientState.tokenLock.lock();
		ClientState.HAVE_TOKEN = true;
		ClientState.tokenLock.unlock();
	}
}
