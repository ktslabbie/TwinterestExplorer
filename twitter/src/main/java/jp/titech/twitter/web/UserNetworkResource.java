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
import jp.titech.twitter.db.TweetBaseUtil;
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
    public TwitterNetworkJSON getNetwork(@QueryParam("name") Optional<String> name) {
    	DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph;
    	TwitterUser seedUser = TweetBaseUtil.getTwitterUserWithScreenName(name.or(defaultName));
    	NetworkBuilder networkBuilder = new NetworkBuilder(seedUser, 100);
		networkBuilder.build();
		twitterUserGraph = networkBuilder.getGraph();
		//twitterUserGraph.vertexSet().remove(seedUser);
        
		return new TwitterNetworkJSON(twitterUserGraph);
    }
}