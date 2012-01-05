package kiwanami.web.cont;

public interface IBranchService {

	public void set(String key,Object obj);
	public Object get(String key);
	public String[] keys();

}
