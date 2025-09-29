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
		return false;
	}

	/**
	 *
	 */
	@Override
	public boolean disconnectClient()
	{
		return false;
	}

	/**
	 *
	 */
	@Override
	public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate)
	{
		return null;
	}

	/**
	 *
	 */
	@Override
	public SensorData[] getSensorData(String topic, Date startDate, Date endDate)
	{
		return null;
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
		return false;
	}

	/**
	 *
	 */
	@Override
	public boolean storeData(String topic, int qos, SensorData... data)
	{
		return false;
	}

	/**
	 *
	 */
	@Override
	public boolean storeData(String topic, int qos, SystemPerformanceData... data)
	{
		return false;
	}
	
	
	// private methods
	
	/**
	 * 
	 */
	private void initConfig()
	{
	}

}
