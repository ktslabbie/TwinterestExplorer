/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		1 nov. 2012
 */
package jp.titech.twitter.ontology.dbpedia;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Kristian Slabbekoorn
 * 
 * Singleton client class for Redis.
 */
public class RedisClient {

	private static RedisClient redisClient;
	private JedisPool pool;
	private JsonFactory f;
	
	/**
	 * Constructor.
	 */
	public RedisClient() {
		String[] uriParts = Vars.REDIS_URL.split(":");
		pool = new JedisPool(new JedisPoolConfig(), uriParts[0], Integer.parseInt(uriParts[1]));
		f = new JsonFactory();
	}

	/**
	 * @param uri The key URI
	 * @param type The type of class to get as a string (yago, category, dbpedia, freebase, schema)
	 * 
	 * @return
	 */
	public List<String> query(String uri, String type) {
		List<String> typeList = new ArrayList<String>();
		
		// Jedis implements Closable. Hence, the jedis instance will be auto-closed after the last statement.
		try (Jedis jedis = pool.getResource()) {
			//Log.getLogger().info("Querying Redis with: " + uri);
			String jsonList = jedis.hget(uri, type);
			//Log.getLogger().info("Obtained from Redis: " + jsonList);
			if(jsonList != null) {
				JsonParser jp = f.createParser(jsonList.substring(1, jsonList.length()-1));
				jp.nextToken();
	
				while(jp.nextToken() != JsonToken.END_ARRAY) {
					typeList.add(jp.getText());
				}
			}
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.getLogger().error("Nullpointer!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return typeList;
	}
	
	public static RedisClient getInstance(){
		if(redisClient == null) redisClient = new RedisClient();
		return redisClient;
	}
}
