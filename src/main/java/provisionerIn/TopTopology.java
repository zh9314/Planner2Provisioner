package provisionerIn;

import java.util.ArrayList;


public class TopTopology {

	
	/**
	 * This field can be <br/>
	 * 1. the url of the ssh key file  <br/>
	 * 2. the absolute path of the file on the local machine <br/>
	 * 3. the file name of the file. By default, this file will be at the same 
	 * folder of the description files. <br/>
	 * Examples: url@http://www.mydomain.com/pathToFile/myId_dsa <br/>
	 * file@/home/id_dsa (the file path is absolute path)<br/>
	 * name@id_rsa.pub (just fileName) <br/>
	 * null <br/>
	 * This is not case sensitive.
	 * The file must exist. Otherwise, there will be a warning log message for this.
	 * And you can load these information manually later on.  <br/>
	 * All the "script" field is designed like this.
	 */
	public String publicKeyPath;
	
	
	/*
	 * The user name defined by the user.
	 * This is corresponding to the ssh key.
	 */
	public String userName;
	
	
	public ArrayList<SubTopologyInfo> topologies;
	
	public ArrayList<TopConnection> connections;
	

	
}
