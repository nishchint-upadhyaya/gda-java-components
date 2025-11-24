package programmingtheiot.gda.connection;

import org.json.JSONObject;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

public class UbidotsDataMapper
{
    public static String toVariableJson(String varName, double value, long tsMillis)
    {
        // {"varName": {"value": 25.3, "timestamp": 1700000000000}}
        JSONObject root = new JSONObject();
        JSONObject v = new JSONObject();
        v.put("value", value);
        v.put("timestamp", tsMillis);
        root.put(varName, v);
        return root.toString();
    }

    public static String toSensorPayload(SensorData sd, String varName)
    {
        return toVariableJson(varName, sd.getValue(), sd.getTimeStampMillis());
    }

    public static String toSystemPayload(SystemPerformanceData spd, String varName)
    {
        // choose one metric or build a multi-variable JSON (e.g., cpu, mem, disk)
        JSONObject root = new JSONObject();
        root.put("cpu", new JSONObject().put("value", spd.getCpuUtilization()).put("timestamp", spd.getTimeStamp()));
        root.put("mem", new JSONObject().put("value", spd.getMemoryUtilization()).put("timestamp", spd.getTimeStamp()));
        root.put("disk", new JSONObject().put("value", spd.getDiskUtilization()).put("timestamp", spd.getTimeStamp()));
        return root.toString();
    }
}