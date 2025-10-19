package programmingtheiot.integration.connection;

import static org.junit.Assert.*;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;

/**
 * Integration test for RedisPersistenceAdapter.
 * Verifies connect/disconnect and store/retrieve operations.
 */
public class RedisClientAdapterTest
{
	private static final Logger _Logger =
		Logger.getLogger(RedisClientAdapterTest.class.getName());
	
	private RedisPersistenceAdapter redisClient = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		_Logger.info("==== Starting RedisPersistenceAdapter integration tests ====");
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		_Logger.info("==== Completed RedisPersistenceAdapter integration tests ====");
	}
	
	@Before
	public void setUp() throws Exception
	{
		this.redisClient = new RedisPersistenceAdapter();
	}
	
	@After
	public void tearDown() throws Exception
	{
		if (this.redisClient != null && this.redisClient.isClientConnected())
		{
			this.redisClient.disconnectClient();
		}
	}
	
	@Test
	public void testConnectClient()
	{
		boolean connected = this.redisClient.connectClient();
		assertTrue("Redis client should connect successfully.", connected);
		
		assertTrue("Client should report connected.", this.redisClient.isClientConnected());
		
		this.redisClient.disconnectClient();
	}
	
	@Test
	public void testDisconnectClient()
	{
		this.redisClient.connectClient();
		boolean disconnected = this.redisClient.disconnectClient();
		
		assertTrue("Redis client should disconnect successfully.", disconnected);
		assertFalse("Client should report not connected.", this.redisClient.isClientConnected());
	}
	
	@Test
	public void testStoreDataStringIntActuatorDataArray()
	{
		this.redisClient.connectClient();
		
		ActuatorData ad = new ActuatorData();
		ad.setName("TestActuator");
		ad.setValue(42.0f);
		
		boolean success = this.redisClient.storeData("ActuatorData", 0, ad);
		assertTrue("Actuator data should store successfully.", success);
	}
	
	@Test
	public void testStoreDataStringIntSensorDataArray()
	{
		this.redisClient.connectClient();
		
		SensorData sd = new SensorData();
		sd.setName("TestSensor");
		sd.setValue(21.5f);
		
		boolean success = this.redisClient.storeData("SensorData", 0, sd);
		assertTrue("Sensor data should store successfully.", success);
	}
	
	@Test
	public void testStoreDataStringIntSystemPerformanceDataArray()
	{
		this.redisClient.connectClient();
		
		SystemPerformanceData spd = new SystemPerformanceData();
		spd.setCpuUtilization(0.5f);
		spd.setMemoryUtilization(0.75f);
		
		boolean success = this.redisClient.storeData("SystemPerformanceData", 0, spd);
		assertTrue("System performance data should store successfully.", success);
	}
	
	@Test
	public void testGetActuatorData()
	{
		this.redisClient.connectClient();
		
		_Logger.info("VALUE:" + 
		this.redisClient.getActuatorData("ActuatorData", null, null).length);
		
		assertNotNull("getActuatorData should return a non-null array.",
			this.redisClient.getActuatorData("ActuatorData", null, null));
	}
	
	@Test
	public void testGetSensorData()
	{
		this.redisClient.connectClient();
		
		assertNotNull("getSensorData should return a non-null array.",
			this.redisClient.getSensorData("SensorData", null, null));
	}
}
