package jp.titech.twitter.network;

public class NetworkController {
	
	private static NetworkController 	networkController;
	
	private NetworkController() {}

	/**
	 * Retrieve the NetworkController singleton instance.
	 * 
	 * @return the network controller singleton
	 */
	public static NetworkController getInstance(){
		if(networkController == null){ networkController = new NetworkController(); }
		return networkController;
	}
}
