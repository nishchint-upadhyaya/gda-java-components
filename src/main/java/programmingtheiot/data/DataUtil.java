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

package programmingtheiot.data;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DataUtil
{
	// static
	
	private static final Logger   _Logger   =
		Logger.getLogger(DataUtil.class.getName());
	
	private static final DataUtil _Instance = new DataUtil();

	/**
	 * Returns the Singleton instance of this class.
	 * 
	 * @return ConfigUtil
	 */
	public static final DataUtil getInstance()
	{
		return _Instance;
	}
	
	
	// private var's
	
	
	// constructors
	
	/**
	 * Default (private).
	 * 
	 */
	private DataUtil()
	{
		super();
	}
	
	
	// public methods
	
	public String actuatorDataToJson(ActuatorData data)
	{
		String jsonData = null;
		
		if (data != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(data);
		}
		
		return jsonData;
	}
	
	public String actuatorDataToTimeAndValueJson(ActuatorData data)
	{
		String jsonData = null;
		
		if (data != null) {
			Gson gson = new Gson();
			TimeAndValuePayloadData tvData = new TimeAndValuePayloadData(data);
			jsonData = gson.toJson(tvData);
		}

		return jsonData;
	}
	
	public String sensorDataToJson(SensorData sensorData)
	{
		String jsonData = null;
		
		if (sensorData != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(sensorData);
		}
		
		return jsonData;
	}
	
	public String sensorDataToTimeAndValueJson(SensorData data)
	{
		String jsonData = null;
		
		if (data != null) {
			Gson gson = new Gson();
			TimeAndValuePayloadData tvData = new TimeAndValuePayloadData(data);
			jsonData = gson.toJson(tvData);
		}

		return jsonData;
	}
	
	public String systemPerformanceDataToJson(SystemPerformanceData sysPerfData)
	{
		String jsonData = null;
		
		if (sysPerfData != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(sysPerfData);
		}
		
		return jsonData;
	}
	
	public String systemStateDataToJson(SystemStateData sysStateData)
	{
		return null;
	}
	
	public ActuatorData jsonToActuatorData(String jsonData)
	{
		ActuatorData data = null;
		
		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, ActuatorData.class);
		}
		
		return data;
	}
	
	public SensorData jsonToSensorData(String jsonData)
	{
		SensorData data = null;
		
		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SensorData.class);
		}
		
		return data;
	}
	
	public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
	{
		SystemPerformanceData data = null;
		
		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SystemPerformanceData.class);
		}
		
		return data;
	}
	
	public SystemStateData jsonToSystemStateData(String jsonData)
	{
		return null;
	}
	
}
