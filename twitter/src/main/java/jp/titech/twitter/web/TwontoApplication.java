package jp.titech.twitter.web;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.db.TweetBase;

import org.apache.log4j.PropertyConfigurator;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TwontoApplication extends Application<TwontoConfiguration> {
	public static void main(String[] args) throws Exception {
		new TwontoApplication().run(args);
	}

	@Override
	public void initialize(Bootstrap<TwontoConfiguration> bootstrap) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		TweetBase.getInstance();
	}

	@Override
	public void run(TwontoConfiguration configuration, Environment environment) {
		final TwitterUserResource twitterUserResource = new TwitterUserResource(configuration.getDefaultName());
		final UserTweetsResource userTweetsResource = new UserTweetsResource();
		final UserOntologyResource userOntologyResource = new UserOntologyResource(configuration.getDefaultName());
		final UserNetworkResource userNetworkResource = new UserNetworkResource(configuration.getDefaultName());
		final APIHealthCheck healthCheck = new APIHealthCheck();
		    
		environment.jersey().register(twitterUserResource);
		environment.jersey().register(userTweetsResource);
		environment.jersey().register(userOntologyResource);
		environment.jersey().register(userNetworkResource);
		environment.healthChecks().register("template", healthCheck);
	}
}