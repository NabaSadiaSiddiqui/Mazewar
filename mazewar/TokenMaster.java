import java.io.*;
import java.util.concurrent.locks.Lock;

public class TokenMaster {
	// private ClientState.ClientLocation next;
	private Lock tokenLock;
	// State indicates if player has the token
	private boolean HAVE_TOKEN = false;
	// State indicates if player needs token to enter a critical section
	private boolean NEED_TOKEN = false;

	public TokenMaster(Lock tokenLock) {
		this.tokenLock = tokenLock;
	}

	public void passToken(ClientLocation next) {
		System.out.println("Pass token to next client");
		if (next == null) {
			return;
		}
		try {
			tokenLock.lock();
			HAVE_TOKEN = false;
			MazewarPacket packetToNext = new MazewarPacket();
			packetToNext.type = MazewarPacket.CLIENT_TOKEN_EXCHANGE;
			System.out.println(next.getName());
			next.getOut().writeObject(packetToNext);
			tokenLock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean needToken() {
		boolean need;
		tokenLock.lock();
		need = NEED_TOKEN;
		tokenLock.unlock();
		return need;
	}

	public boolean haveToken() {
		boolean have;
		tokenLock.lock();
		have = HAVE_TOKEN;
		tokenLock.unlock();
		return have;
	}

	public void setNeedToken() {
		tokenLock.lock();
		NEED_TOKEN = true;
		tokenLock.unlock();
	}

	public void unsetNeedToken() {
		tokenLock.lock();
		NEED_TOKEN = false;
		tokenLock.unlock();
	}

	public void setHaveToken() {
		tokenLock.lock();
		HAVE_TOKEN = true;
		tokenLock.unlock();
	}
}
