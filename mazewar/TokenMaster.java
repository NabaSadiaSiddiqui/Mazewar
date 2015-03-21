import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenMaster {
	public static Lock tokenLock;
	// State indicates if player has the token
	private boolean HAVE_TOKEN = false;
	// State indicates if player needs token to enter a critical section
	private boolean NEED_TOKEN = false;

	public TokenMaster() {
		tokenLock = new ReentrantLock();
	}

	public void passToken(ClientLocation next) {
		if (next == null) {
			return;
		}

		try {
			tokenLock.lock();
			System.out.println("Pass token to " + next.getName());
			HAVE_TOKEN = false;
			MazewarPacket packetToNext = new MazewarPacket();
			packetToNext.type = MazewarPacket.CLIENT_TOKEN_EXCHANGE;
			next.getOut().writeObject(packetToNext);
			next.getOut().flush();
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
	
	public void acquireLock() {
		tokenLock.lock();
	}
	
	public void releaseLock() {
		tokenLock.unlock();
	}
}
