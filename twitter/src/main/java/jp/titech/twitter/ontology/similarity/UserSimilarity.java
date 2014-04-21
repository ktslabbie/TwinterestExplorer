package jp.titech.twitter.ontology.similarity;

import jp.titech.twitter.data.TwitterUser;

public class UserSimilarity implements Comparable<UserSimilarity> {

	private TwitterUser userA, userB;
	private double similarity = 0.0;
	
	public UserSimilarity(TwitterUser userA, TwitterUser userB, double similarity) {
		this.userA = userA;
		this.userB = userB;
		this.similarity = similarity;
	}

	/**
	 * @return the userA
	 */
	public TwitterUser getUserA() {
		return userA;
	}

	/**
	 * @param userA the userA to set
	 */
	public void setUserA(TwitterUser userA) {
		this.userA = userA;
	}

	/**
	 * @return the userB
	 */
	public TwitterUser getUserB() {
		return userB;
	}

	/**
	 * @param userB the userB to set
	 */
	public void setUserB(TwitterUser userB) {
		this.userB = userB;
	}

	/**
	 * @return the similarity
	 */
	public double getSimilarity() {
		return similarity;
	}

	/**
	 * @param similarity the similarity to set
	 */
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	
	@Override
	public String toString() {
		return userA.getScreenName() + "\t" + userB.getScreenName() + "\t" + similarity;
	}

	public int compareTo(UserSimilarity other) {
		if(this.similarity < other.getSimilarity()) return 1;
		else if (this.similarity > other.getSimilarity()) return -1;
		else return 0;
	}
}
