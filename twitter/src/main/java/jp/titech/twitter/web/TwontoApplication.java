package jp.titech.twitter.web;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.api.TwitterAPIAccountManager;
import jp.titech.twitter.ontology.dbpedia.RedisClient;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

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
		RedisClient.getInstance();
		TwitterAPIAccountManager.getInstance();
		Util.loadStopwords(Vars.STOPWORDS_FILE);
	}

	@Override
	public void run(TwontoConfiguration configuration, Environment environment) {
		final SimpleUserResource simpleUserResource = new SimpleUserResource(configuration.getDefaultName());
		final UserResource userResource = new UserResource(configuration.getDefaultName());
		final UserListResource userListResource = new UserListResource();
		final APIHealthCheck healthCheck = new APIHealthCheck();
		
		environment.jersey().register(simpleUserResource);
		environment.jersey().register(userResource);
		environment.jersey().register(userListResource);
		environment.healthChecks().register("template", healthCheck);
	}
}