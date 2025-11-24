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

import com.google.gson.Gson;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DataUtil
{
	// static
	
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
	
	public String sensorDataToJson(SensorData sensorData)
	{
		String jsonData = null;

		if (sensorData != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(sensorData);
		}

		return jsonData;
	}

	/**
	 * Generates the Ubidots variable payload expected when publishing to
	 * /v1.6/devices/{device}/{variable} topics.
	 * Example: {"value":42.0,"timestamp":1700000000000,"context":{"lat":0.0,"lng":0.0}}
	 */
	public String sensorDataToValueJson(SensorData data)
	{
		if (data == null) return "{}";

		try {
			org.json.JSONObject payload = new org.json.JSONObject();
			payload.put("value", data.getValue());

			// Uncomment the following lines to include timestamp in the payload
			// long ts = data.getTimeStampMillis();
			// if (ts > 0) {
			// 	payload.put("timestamp", ts);
			// }

			// Add context with location data if available
			if (data.getLatitude() != 0.0 || data.getLongitude() != 0.0) {
				org.json.JSONObject context = new org.json.JSONObject();
				context.put("lat", data.getLatitude());
				context.put("lng", data.getLongitude());
				payload.put("context", context);
			}

			return payload.toString();
		} catch (Exception e) {
			return "{}";
		}
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
		String jsonData = null;

		if (sysStateData != null) {
			Gson gson = new Gson();
			jsonData = gson.toJson(sysStateData);
		}

		return jsonData;
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
		SystemStateData data = null;

		if (jsonData != null && jsonData.trim().length() > 0) {
			Gson gson = new Gson();
			data = gson.fromJson(jsonData, SystemStateData.class);
		}

		return data;
	}
	
	/**
	 * Converts an ActuatorData instance into Ubidots "time and value" JSON format:
	 * {
	 *   "ActuatorName": { "value": 1.0, "timestamp": 1700000000000 }
	 * }
	 *
	 * @param data The ActuatorData instance.
	 * @return A JSON string with name, value, and timestamp.
	 */
	public String actuatorDataToTimeAndValueJson(ActuatorData data)
	{
	    if (data == null) {
	        return "{}";
	    }

	    try {
	        org.json.JSONObject root = new org.json.JSONObject();
	        org.json.JSONObject payload = new org.json.JSONObject();

	        payload.put("value", data.getValue());
	        payload.put("timestamp", data.getTimeStampMillis());

	        // Use actuator name (e.g., "LedActuator") as the key
	        String varName = (data.getName() != null && !data.getName().isEmpty())
	                ? data.getName()
	                : "actuator";

	        root.put(varName, payload);
	        return root.toString();
	    } catch (Exception e) {
	        // In case of JSON errors, fall back to minimal structure
	        return "{}";
	    }
	}
	
	// ===== Add to DataUtil.java =====

	/**
	 * Ubidots-style "time and value" JSON for a single SensorData variable.
	 * {
	 *   "temperature": { "value": 23.7, "timestamp": 1700000000000 }
	 * }
	 */
	public String sensorDataToTimeAndValueJson(SensorData data)
	{
		if (data == null) return "{}";

		try {
			org.json.JSONObject root = new org.json.JSONObject();
			org.json.JSONObject payload = new org.json.JSONObject();

			payload.put("value", data.getValue());
			payload.put("timestamp", data.getTimeStampMillis());

			String varName = (data.getName() != null && !data.getName().isEmpty())
					? data.getName()
					: "sensor";

			root.put(varName, payload);
			return root.toString();
		} catch (Exception e) {
			return "{}";
		}
	}

	/**
	 * Ubidots-style payload for SystemPerformanceData as multiple variables
	 * (cpu, mem, and optionally disk). Produces:
	 * {
	 *   "cpu": {"value": 0.42, "timestamp": ...},
	 *   "mem": {"value": 0.33, "timestamp": ...},
	 *   "disk": {"value": 0.51, "timestamp": ...}
	 * }
	 */
	public String systemPerformanceDataToTimeAndValueJson(SystemPerformanceData data)
	{
		if (data == null) return "{}";

		try {
			org.json.JSONObject root = new org.json.JSONObject();
			long ts = data.getTimeStampMillis();

			org.json.JSONObject cpu = new org.json.JSONObject();
			cpu.put("value", data.getCpuUtilization());
			cpu.put("timestamp", ts);
			root.put("cpu", cpu);

			org.json.JSONObject mem = new org.json.JSONObject();
			mem.put("value", data.getMemoryUtilization());
			mem.put("timestamp", ts);
			root.put("mem", mem);

			// Optional: include disk if your pipeline expects it
			org.json.JSONObject disk = new org.json.JSONObject();
			disk.put("value", data.getDiskUtilization());
			disk.put("timestamp", ts);
			root.put("disk", disk);

			return root.toString();
		} catch (Exception e) {
			return "{}";
		}
	}	
}