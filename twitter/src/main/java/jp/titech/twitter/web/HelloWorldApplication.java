package jp.titech.twitter.web;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.db.TweetBase;

import org.apache.log4j.PropertyConfigurator;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
	public static void main(String[] args) throws Exception {
		new HelloWorldApplication().run(args);
	}

	@Override
	public String getName() {
		return "hello-world";
	}

	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		TweetBase.getInstance();
	}

	@Override
	public void run(HelloWorldConfiguration configuration, Environment environment) {
		final HelloWorldResource resource = new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName());
		final TwitterUserResource twitterUserResource = new TwitterUserResource(configuration.getDefaultName());
		final UserTweetsResource userTweetsResource = new UserTweetsResource();
		final UserOntologyResource userOntologyResource = new UserOntologyResource(configuration.getDefaultName());
		final UserNetworkResource userNetworkResource = new UserNetworkResource(configuration.getDefaultName());
		final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());
		
		environment.jersey().register(resource);
		environment.jersey().register(twitterUserResource);
		environment.jersey().register(userTweetsResource);
		environment.jersey().register(userOntologyResource);
		environment.jersey().register(userNetworkResource);
		environment.healthChecks().register("template", healthCheck);
	}
}