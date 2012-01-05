package kiwanami.web.cont;


public interface IGlobalService {

	public void set(String key,Object obj);
	public Object get(String key);
	public String[] keys();

	public void addShutdownTask(String key,Runnable task);

}
