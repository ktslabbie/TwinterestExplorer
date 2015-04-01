package jp.titech.twitter.web;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.api.TwitterAPIAccountManager;
import jp.titech.twitter.ontology.dbpedia.RedisClient;
import jp.titech.twitter.util.Util;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.servlets.CrossOriginFilter;

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
		Util.loadStopwords();
	}

	private void configureCORS(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://ktslabbie.github.io");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
		filter.setInitParameter("allowCredentials", "true");
	}

	@Override
	public void run(TwontoConfiguration configuration, Environment environment) {
		final SimpleUserResource simpleUserResource = new SimpleUserResource(configuration.getDefaultName());
		final UserResource userResource = new UserResource(configuration.getDefaultName());
		final UserFollowersListResource userFollowersListResource = new UserFollowersListResource(configuration.getDefaultName());
		final KeywordUserListResource keywordUserListResource = new KeywordUserListResource(configuration.getDefaultKeyword());
		final APIHealthCheck healthCheck = new APIHealthCheck();

		configureCORS(environment);
		
		environment.jersey().register(simpleUserResource);
		environment.jersey().register(userResource);
		environment.jersey().register(userFollowersListResource);
		environment.jersey().register(keywordUserListResource);
		environment.healthChecks().register("template", healthCheck);
	}
}
