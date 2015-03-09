package jp.titech.twitter.web;

import com.codahale.metrics.health.HealthCheck;

public class APIHealthCheck extends HealthCheck {
    
	/**
	 * TODO: skipping health checking for now. Always return true.
	 */
    public APIHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        /*final String saying = String.format(template, "TEST");
        if (!saying.contains("TEST")) {
            return Result.unhealthy("template doesn't include a name");
        }*/
        return Result.healthy();
    }
}