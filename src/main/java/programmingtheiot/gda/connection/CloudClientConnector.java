package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Cloud client bridge used by the GDA.
 * Publishes telemetry and subscribes to downlink/commands via MQTT.
 * Supports optional Ubidots "time and value" JSON payloads.
 */
public class CloudClientConnector implements ICloudClient, IConnectionListener
{
	private static final Logger _Logger = Logger.getLogger(CloudClientConnector.class.getName());

	// --- Configurable state
	private String baseTopic    = "";
	private String deviceLabel  = "gda-1";
	private String sensorVar    = "sensor";
	private String sysPerfVar   = "system";
	private int    qosLevel     = 1;
	private boolean useTimeAndValuePayload = false;  // NEW

	private MqttClientConnector mqttClient = null;
	private IDataMessageListener dataMsgListener = null;

	// --- Constructors
	public CloudClientConnector()
	{
		ConfigUtil cfg = ConfigUtil.getInstance();

		this.baseTopic   = cfg.getProperty(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, ConfigConst.BASE_TOPIC_KEY, "/v1.6/devices");
		this.deviceLabel = cfg.getProperty(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, "deviceLabel", "gda-1");
		this.qosLevel    = cfg.getInteger(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY, 1);

		this.sensorVar   = cfg.getProperty(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, "sensorVar",  "sensor");
		this.sysPerfVar  = cfg.getProperty(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, "systemVar", "system");

		// NEW: toggle for time-and-value style payloads
		this.useTimeAndValuePayload =
			    cfg.getBoolean(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE, "useTimeAndValuePayload");


		if (this.baseTopic.endsWith("/")) {
			this.baseTopic = this.baseTopic.substring(0, this.baseTopic.length() - 1);
		}
	}

	@Override
	public boolean connectClient()
	{
		if (this.mqttClient == null) {
			this.mqttClient = new MqttClientConnector(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE);
			this.mqttClient.setConnectionListener(this);
		}
		return this.mqttClient.connectClient();
	}

	@Override
	public boolean disconnectClient()
	{
		if (this.mqttClient != null && this.mqttClient.isConnected()) {
			return this.mqttClient.disconnectClient();
		}
		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return (listener != null);
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
	{
		if (resource == null || data == null) return false;

		String payload = this.useTimeAndValuePayload
			? DataUtil.getInstance().sensorDataToTimeAndValueJson(data)
			: DataUtil.getInstance().sensorDataToJson(data);

		String topic = buildUbidotsTopic(resource, data.getName());
		return publishMessageToCloud(topic, payload);
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
	{
		if (resource == null || data == null) return false;

		// Send CPU utilization
		SensorData cpu = new SensorData();
		cpu.updateData(data);
		cpu.setName(ConfigConst.CPU_UTIL_NAME);
		cpu.setValue(data.getCpuUtilization());
		boolean cpuOk = sendEdgeDataToCloud(resource, cpu);

		if (!cpuOk) _Logger.warning("Failed to send CPU utilization to cloud.");

		// Send Memory utilization
		SensorData mem = new SensorData();
		mem.updateData(data);
		mem.setName(ConfigConst.MEM_UTIL_NAME);
		mem.setValue(data.getMemoryUtilization());
		boolean memOk = sendEdgeDataToCloud(resource, mem);

		if (!memOk) _Logger.warning("Failed to send memory utilization to cloud.");

		return cpuOk && memOk;
	}

	@Override
	public boolean subscribeToCloudEvents(ResourceNameEnum resource)
	{
		if (this.mqttClient == null || !this.mqttClient.isConnected()) {
			_Logger.warning("No MQTT connection – cannot subscribe.");
			return false;
		}

		String topic = buildUbidotsTopic(resource, null);
		this.mqttClient.subscribeToTopic(topic, this.qosLevel);
		return true;
	}

	@Override
	public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
	{
		if (this.mqttClient == null || !this.mqttClient.isConnected()) {
			_Logger.warning("No MQTT connection – cannot unsubscribe.");
			return false;
		}

		String topic = buildUbidotsTopic(resource, null);
		this.mqttClient.unsubscribeFromTopic(topic);
		return true;
	}

	// --- IConnectionListener ---
	@Override
	public void onConnect()
	{
		_Logger.info("Cloud connected: provisioning topics and downlink subscriptions...");

		LedEnablementMessageListener ledListener = new LedEnablementMessageListener(this.dataMsgListener);

		ActuatorData ad = new ActuatorData();
		ad.setAsResponse();
		ad.setName(ConfigConst.LED_ACTUATOR_NAME);
		ad.setValue(-1.0f);

		String ledTopic = buildUbidotsTopic(ledListener.getResource(), ad.getName());
		String adJson = this.useTimeAndValuePayload
			? DataUtil.getInstance().actuatorDataToTimeAndValueJson(ad)
			: DataUtil.getInstance().actuatorDataToJson(ad);

		this.publishMessageToCloud(ledTopic, adJson);
		this.mqttClient.subscribeToTopic(ledTopic, this.qosLevel, ledListener);
	}

	@Override
	public void onDisconnect()
	{
		_Logger.info("MQTT client disconnected.");
	}

	// --- Helper methods ---
	private String buildUbidotsTopic(ResourceNameEnum resource, String itemName)
	{
		String variable;
		if (itemName != null && !itemName.isBlank()) {
			variable = itemName;
		} else {
			switch (resource) {
				case CDA_SENSOR_MSG_RESOURCE:
					variable = this.sensorVar;
					break;
				case CDA_SYSTEM_PERF_MSG_RESOURCE:
					variable = this.sysPerfVar;
					break;
				default:
					variable = resource.getResourceType();
			}
		}
		return normalizeJoin(this.baseTopic, this.deviceLabel, variable).toLowerCase();
	}

	private boolean publishMessageToCloud(String topicName, String payload)
	{
		try {
			this.mqttClient.publishMessage(topicName, payload.getBytes(), this.qosLevel);
			return true;
		} catch (Exception e) {
			_Logger.warning("Failed to publish to CSP: " + topicName);
			return false;
		}
	}

	private static String normalizeJoin(String... parts)
	{
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (p == null || p.isEmpty()) continue;
			if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') sb.append('/');
			if (p.startsWith("/")) p = p.substring(1);
			if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
			sb.append(p);
		}
		return "/" + sb.toString();
	}

	// --- Downlink listener (unchanged) ---
	private class LedEnablementMessageListener implements IMqttMessageListener
	{
		private final IDataMessageListener dataMsgListener;
		private final ResourceNameEnum resource = ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE;
		private final int typeID   = ConfigConst.LED_ACTUATOR_TYPE;
		private final String itemName = ConfigConst.LED_ACTUATOR_NAME;

		LedEnablementMessageListener(IDataMessageListener dataMsgListener)
		{
			this.dataMsgListener = dataMsgListener;
		}

		public ResourceNameEnum getResource()
		{
			return this.resource;
		}

		@Override
		public void messageArrived(String topic, MqttMessage message)
		{
			try {
				String jsonData = new String(message.getPayload());
				ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(jsonData);
				actuatorData.setLocationID(ConfigConst.CONSTRAINED_DEVICE);
				actuatorData.setTypeID(this.typeID);
				actuatorData.setName(this.itemName);

				int val = (int) actuatorData.getValue();
				switch (val) {
					case ConfigConst.ON_COMMAND:
						_Logger.info("Received LED enablement message [ON].");
						actuatorData.setStateData("LED switching ON");
						break;
					case ConfigConst.OFF_COMMAND:
						_Logger.info("Received LED enablement message [OFF].");
						actuatorData.setStateData("LED switching OFF");
						break;
					default:
						return;
				}
				if (this.dataMsgListener != null) {
					jsonData = DataUtil.getInstance().actuatorDataToJson(actuatorData);
					this.dataMsgListener.handleIncomingMessage(
						ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, jsonData);
				}
			} catch (Exception e) {
				_Logger.warning("Failed to convert message payload to ActuatorData.");
			}
		}
	}
}