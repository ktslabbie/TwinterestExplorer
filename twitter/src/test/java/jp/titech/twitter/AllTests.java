package jp.titech.twitter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TweetTest.class, RedisTest.class, SpotlightTypesTest.class, UserTest.class })
public class AllTests {

}
