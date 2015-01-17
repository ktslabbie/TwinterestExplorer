package jp.titech.twitter.web;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.network.NetworkBuilder;

@Path("/api/get-user-network")
@Produces(MediaType.APPLICATION_JSON)
public class UserNetworkResource {

	private final String defaultName;

	public UserNetworkResource(String defaultName) {
		this.defaultName = defaultName;
	}

	@GET
	@Timed
	public TwitterNetworkJSON getNetwork(@QueryParam("name") Optional<String> name, @QueryParam("userCount") Optional<Integer> userCount) {
		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph;
		NetworkBuilder networkBuilder;
		TwitterConnector connector = new TwitterConnector();

		TwitterUser seedUser = connector.getTwitterUserWithScreenName(name.or(defaultName));
		if(seedUser.isProtected()) return null; // This should not be possible.
		
		networkBuilder = new NetworkBuilder(seedUser, userCount.or(99), connector);
		networkBuilder.build();
		twitterUserGraph = networkBuilder.getGraph();

		return new TwitterNetworkJSON(twitterUserGraph);
	}
}