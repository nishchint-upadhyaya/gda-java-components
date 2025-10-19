/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */

package programmingtheiot.gda.connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class RedisPersistenceAdapter implements IPersistenceClient
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(RedisPersistenceAdapter.class.getName());
	
	// private var's
	private String host = null;
    private int port = 0;
    private Jedis redisClient = null;
    private boolean isConnected = false;
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public RedisPersistenceAdapter()
	{
		super();
		
		initConfig();
	}
	
	
	// public methods
	
	// public methods
	
	/**
	 *
	 */
	@Override
	public boolean connectClient()
	{
		if (isConnected) {
            _Logger.warning("Redis client already connected.");
            return true;
        }
        try {
			ConfigUtil config = ConfigUtil.getInstance();
        	String password = config.getProperty(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.PASSWORD_KEY, "");

            this.redisClient = new Jedis(this.host, this.port);

			// Authenticate with password
			if (password != null && !password.isEmpty()) {
				this.redisClient.auth("default", password); // Redis Cloud uses 'default' user by default
				_Logger.info("Authenticated with Redis Cloud using user 'default'.");
			} else {
				_Logger.warning("No password provided for Redis authentication.");
			}
			
            this.isConnected = true;
            _Logger.info("Connected to Redis at " + host + ":" + port);
        } catch (Exception e) {
            _Logger.severe("Failed to connect to Redis: " + e.getMessage());
            this.isConnected = false;
        }
        return this.isConnected;
	}

	/**
	 *
	 */
	@Override
	public boolean disconnectClient()
	{
		if (!isConnected) {
            _Logger.warning("Redis client already disconnected.");
            return true;
        }
        try {
            this.redisClient.close();
            this.isConnected = false;
            _Logger.info("Disconnected from Redis.");
        } catch (Exception e) {
            _Logger.severe("Failed to disconnect from Redis: " + e.getMessage());
        }
        return !this.isConnected;
	}
	
	 public boolean isClientConnected() {
	        return this.isConnected;
	 }
	 

	/**
	 *
	 */
	@Override
	public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate)
	{
		return new ActuatorData[0];
	}

	/**
	 *
	 */
	@Override
	public SensorData[] getSensorData(String topic, Date startDate, Date endDate)
	{
		return new SensorData[0];
	}

	/**
	 *
	 */
	@Override
	public void registerDataStorageListener(Class cType, IPersistenceListener listener, String... topics)
	{
	}

	/**
	 *
	 */
	@Override
	public boolean storeData(String topic, int qos, ActuatorData... data)
	{
		if (!isClientConnected() || data == null) return false;
	    	var du = DataUtil.getInstance();
	    	for (ActuatorData d : data) {
	    		String json = du.actuatorDataToJson(d);   // <-- not d.toString()
	    		this.redisClient.rpush(topic, json);
	    	}
	    return true;
	}

	/**
	 *
	 */
	@Override
	public boolean storeData(String topic, int qos, SensorData... data)
	{
		if (!isClientConnected() || data == null) return false;
    	var du = DataUtil.getInstance();
    	for (SensorData d : data) {
    		String json = du.sensorDataToJson(d);  
    		this.redisClient.rpush(topic, json);
    	}
    	return true;
	}

	/**
	 *
	 */
	@Override
	public boolean storeData(String topic, int qos, SystemPerformanceData... data)
	{
		if (!isClientConnected() || data == null) return false;
    	var du = DataUtil.getInstance();
    	for (SystemPerformanceData d : data) {
    		String json = du.systemPerformanceDataToJson(d);  
    		this.redisClient.rpush(topic, json);
    	}
    	return true;
	}
	
	// private methods
	
	/**
	 * 
	 */
	private void initConfig()
	{
		ConfigUtil config = ConfigUtil.getInstance();
    	this.host = config.getProperty(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.HOST_KEY, "localhost");
    	this.port = config.getInteger(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.PORT_KEY, 6379);

		_Logger.info("Loaded configuration: host=" + this.host + ", port=" + this.port);

	}
	

}