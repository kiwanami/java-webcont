package sample;


public class LockObject {

	private Object lock = new Object();

	public void _wait(long time) {
		synchronized(lock) {
			try {
				lock.wait(time);
			} catch (InterruptedException e) {
			}
		}
	}

	public void _notifyAll() {
		synchronized(lock) {
			lock.notifyAll();
		}
	}

}
