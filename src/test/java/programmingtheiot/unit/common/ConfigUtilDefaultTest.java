/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 - 2025 by Andrew D. King
 */ 

package programmingtheiot.unit.common;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.*;

/**
 * This test case class contains very basic unit tests for
 * ConfigUtil. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class ConfigUtilDefaultTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(ConfigUtilDefaultTest.class.getName());
	
	public static final String HOST_VAL    = ConfigConst.DEFAULT_HOST;
	public static final String NOT_SET_VAL = "Not Set";
	public static final int    PORT_VAL    = 1883;
	
	public static final String DEFAULT_USER = "Foo";
	public static final String DEFAULT_AUTH = "Bar";

	
	// member var's
	
	
	// test setup methods
	
	/**
	 * 
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		// nothing to do
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		ConfigUtil.getInstance().reloadConfig();
	}
	
	// test methods
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#getBooleanProperty(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetBooleanProperty()
	{
		String enableLogging =
			ConfigUtil.getInstance().getProperty(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_LOGGING_KEY);
		
		assertTrue(Boolean.parseBoolean(enableLogging));
	}
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#getIntegerProperty(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetIntegerProperty()
	{
		String portStr =
			ConfigUtil.getInstance().getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY);
		
		assertTrue((Integer.parseInt(portStr) == PORT_VAL));

	}
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#loadConfig(java.lang.String)}.
	 */
	@Test
	public void testGetProperty()
	{
		String host =
			ConfigUtil.getInstance().getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY);
		
		String credFile =
			ConfigUtil.getInstance().getProperty(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.CRED_FILE_KEY);
		
		assertTrue(host.equals(String.valueOf(HOST_VAL)));
		assertNotNull(credFile);
	}
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#hasProperty(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHasProperty()
	{
		assertTrue(ConfigUtil.getInstance().hasProperty(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY));
	}
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#hasSection(java.lang.String)}.
	 */
	@Test
	public void testHasSection()
	{
		assertTrue(ConfigUtil.getInstance().hasSection(ConfigConst.MQTT_GATEWAY_SERVICE));
	}
	
	/**
	 * Test method for {@link com.labbenchstudios.edu.connecteddevices.common.ConfigUtil#isConfigDataLoaded()}.
	 */
	@Test
	public void testIsConfigDataLoaded()
	{
		assertTrue(ConfigUtil.getInstance().isConfigDataLoaded());
	}
	
}
