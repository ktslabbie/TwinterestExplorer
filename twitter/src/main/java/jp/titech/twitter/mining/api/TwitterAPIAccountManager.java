package jp.titech.twitter.mining.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

public class TwitterAPIAccountManager {
	
	private static TwitterAPIAccountManager twitterAccountManager; // TwitterAccountManager is a singleton class
	private List<TwitterAPIAccount> twitterAPIAccounts;
	private int poolSize = 0;

	private TwitterAPIAccountManager() {
		initTwitterAccounts();
	}
	
	void initTwitterAccounts() {
		Log.getLogger().info("Initializing Twitter API accounts.");
		twitterAPIAccounts = new ArrayList<TwitterAPIAccount>();

		try {
			Scanner scanner = new Scanner(new FileReader(new File(Vars.API_ACCOUNTS_FILE)));
			String next;
			int index = 0;
			
			while(scanner.hasNext()) {
				if(!(next = scanner.next()).isEmpty()) {
					String[] parts = next.split(",");
					twitterAPIAccounts.add(new TwitterAPIAccount(parts[0], parts[1], parts[2], parts[3], index));
					index++;
					poolSize++;
				}
			}

			scanner.close();

		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}  catch(Exception e) {
			Log.getLogger().error("Something went wrong loading API accounts!");
			e.printStackTrace();
		}
	}
	
	public TwitterAPIAccount getTwitterAccount(int index) {
		if(index < 0) return getRandomAccount();
		else {
			Log.getLogger().info("Using API account given provided index: " + this.twitterAPIAccounts.get(index % this.poolSize));
			return this.twitterAPIAccounts.get(index % this.poolSize);
		}
	}
	
	public int getAccountPoolSize() {
		return this.poolSize;
	}
	
	public TwitterAPIAccount getRandomAccount() {
		int index = (int) Math.floor( Math.random()*this.poolSize );
		Log.getLogger().info("Using random API account: " + twitterAPIAccounts.get(index));
		return twitterAPIAccounts.get(index);
	}

	/**
	 * Get the singleton instance of the database object.
	 * @return TweetBase the database
	 */
	public static TwitterAPIAccountManager getInstance(){
		if(twitterAccountManager == null){ twitterAccountManager = new TwitterAPIAccountManager(); }
		return twitterAccountManager;
	}

	/**
	 * Return an account that is not yet rate-limited, or null if none available.
	 * Round-robin through the account list starting from currentIndex.
	 * 
	 * @return Available API account or null
	 */
	public TwitterAPIAccount getNextAccount(int currentIndex) {
		int total = this.poolSize;
		
		while(total-- >= 0) {
			int i = currentIndex % this.poolSize;
			if(twitterAPIAccounts.get(i).getSecondsUntilReset() <= 0) {
				Log.getLogger().info("Using API account: " + twitterAPIAccounts.get(i));
				return twitterAPIAccounts.get(i);
			}
			currentIndex++;
		}
		
		Log.getLogger().info("No accounts available. Time to wait...");
		return null;
	}
}
