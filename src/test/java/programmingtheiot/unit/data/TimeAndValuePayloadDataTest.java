/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 - 2025 by Andrew D. King
 */ 

package programmingtheiot.unit.data;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.TimeAndValuePayloadData;

/**
 * This test case class contains very basic unit tests for
 * SensorData. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class TimeAndValuePayloadDataTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(TimeAndValuePayloadDataTest.class.getName());
	
	public static final String DEFAULT_NAME = "TimeAndValueDataFooBar";
	
	public static final float TEST_VAL_A = 22.5f;
	public static final float TEST_VAL_B = 39.4f;
	
	// member var's
	
	
	// test setup methods
	
	@Before
	public void setUp() throws Exception
	{
	}
	
	@After
	public void tearDown() throws Exception
	{
	}
	
	
	// test methods
	
	@Test
	public void testDefaultValues()
	{
		TimeAndValuePayloadData tvd = new TimeAndValuePayloadData();
		
		assertTrue(tvd.getValue() == ConfigConst.DEFAULT_VAL);
		assertTrue(tvd.getTimeStampMillis() == 0L);
	}
	
	@Test
	public void testActuatorDataUpdates()
	{
		ActuatorData ad = createTestActuatorData();
		TimeAndValuePayloadData tvd = new TimeAndValuePayloadData(ad);

		assertTrue(ad.getValue() == tvd.getValue());
		assertTrue(ad.getTimeStampMillis() == tvd.getTimeStampMillis());
	}
	
	@Test
	public void testSensorDataUpdates()
	{
		SensorData ssd = createTestSensorData();
		TimeAndValuePayloadData tvd = new TimeAndValuePayloadData(ssd);

		assertTrue(ssd.getValue() == tvd.getValue());
		assertTrue(ssd.getTimeStampMillis() == tvd.getTimeStampMillis());
	}
	
	
	// private
	
	private ActuatorData createTestActuatorData()
	{
		ActuatorData ad = new ActuatorData();
		ad.setName(DEFAULT_NAME);
		ad.setValue(TEST_VAL_A);
		ad.setCommand(ConfigConst.OFF_COMMAND);
		
		return ad;
	}
	
	private SensorData createTestSensorData()
	{
		SensorData ssd = new SensorData();
		ssd.setName(DEFAULT_NAME);
		ssd.setValue(TEST_VAL_B);
		
		return ssd;
	}
	
}
