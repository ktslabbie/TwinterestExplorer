/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.matching.spotlight;

public abstract class SpotlightFunction {

	private String matches;
	protected String log;
	
	public String getMatches(){
		return (this.matches == null) ? "" : this.matches;
	}
	
	protected void setMatches(String tMatches){
		this.matches = tMatches;
	}
	
	public String getLog(){
		return log;
	}
}
