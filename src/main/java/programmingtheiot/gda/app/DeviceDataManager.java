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

package programmingtheiot.gda.app;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DeviceDataManager implements IDataMessageListener
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManager.class.getName());
	
	// private var's
	
	private boolean enableMqttClient = true;
	private boolean enableCoapServer = false;
	private boolean enableCloudClient = false;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = false;
	private boolean enableSystemPerf = false;
	
	private IActuatorDataListener actuatorDataListener = null;
	private IPubSubClient mqttClient = null;
	private ICloudClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private IRequestResponseClient smtpClient = null;
	private CoapServerGateway coapServer = null;
	private SystemPerformanceManager sysPerfMgr = null;
	
	private RedisPersistenceAdapter redisClient;
	
	private ActuatorData   latestHumidifierActuatorData = null;
	private ActuatorData   latestHumidifierActuatorResponse = null;
	private SensorData     latestHumiditySensorData = null;
	private OffsetDateTime latestHumiditySensorTimeStamp = null;

	private boolean handleHumidityChangeOnDevice = false; // optional
	private int     lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

	// TODO: Load these from PiotConfig.props
	private long    humidityMaxTimePastThreshold = 300; // seconds
	private float   nominalHumiditySetting   = 40.0f;
	private float   triggerHumidifierFloor   = 30.0f;
	private float   triggerHumidifierCeiling = 50.0f;

	// constructors
	
	public DeviceDataManager()
	{
		super();
		
		ConfigUtil configUtil = ConfigUtil.getInstance();
		
		this.enableMqttClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
		
		this.enableCoapServer =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
		
		this.enableCloudClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
		
		this.enablePersistenceClient =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
		
		// parse config rules for local actuation events

		// TODO: add these to ConfigConst
		this.handleHumidityChangeOnDevice =
			configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

		this.humidityMaxTimePastThreshold =
			configUtil.getInteger(
				ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

		this.nominalHumiditySetting =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

		this.triggerHumidifierFloor =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

		this.triggerHumidifierCeiling =
			configUtil.getFloat(
				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

		// TODO: basic validation for timing - add other validators for remaining values
		if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {
			this.humidityMaxTimePastThreshold = 300;
		}	
		initManager();
	}
	
	public DeviceDataManager(
		boolean enableMqttClient,
		boolean enableCoapClient,
		boolean enableCloudClient,
		boolean enableSmtpClient,
		boolean enablePersistenceClient)
	{
		super();
		
		initManager();
	}
	
	// public methods
	
	@Override
	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	{
		if (data != null) {
			// NOTE: Feel free to update this log message for debugging and monitoring
			_Logger.log(
				Level.FINE,
				"Actuator request received: {0}. Message: {1}",
				new Object[] {resourceName.getResourceName(), Integer.valueOf((data.getCommand()))});
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}
			
			// TODO: retrieve this from config file
			int qos = ConfigConst.DEFAULT_QOS;
			
			// TODO: you may want to implement some analysis logic here or
			// in a separate method to determine how best to handle incoming
			// ActuatorData before calling this.sendActuatorCommandtoCda()
			
			// Recall that this private method was implement in Lab Module 10
			// See PIOT-GDA-10-003 for details
			this.sendActuatorCommandtoCda(resourceName, data);
			
			return true;
		} else {
			return false;
		}
	}
	
	public void setActuatorDataListener(String name, IActuatorDataListener listener)
	{
		if (listener != null) {
			// for now, just ignore 'name' - if you need more than one listener,
			// you can use 'name' to create a map of listener instances
			this.actuatorDataListener = listener;
		}
	}
	
	
	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		if (data != null) {
			_Logger.info("Handling actuator response: " + data.getName());
			
			// this next call is optional for now
			//this.handleIncomingDataAnalysis(resourceName, data);
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}
			
			if (this.redisClient.connectClient()) {
			    this.redisClient.storeData(resourceName.getDeviceName(), 0, data);
			}
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	{
		if (resourceName != null && msg != null) {
			try {
				if (resourceName == ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE) {
					_Logger.info("Handling incoming ActuatorData message: " + msg);
					
					// NOTE: it may seem wasteful to convert to ActuatorData and back while
					// the JSON data is already available; however, this provides a validation
					// scheme to ensure the data is actually an 'ActuatorData' instance
					// prior to sending off to the CDA
					ActuatorData ad = DataUtil.getInstance().jsonToActuatorData(msg);
					String jsonData = DataUtil.getInstance().actuatorDataToJson(ad);
					
					if (this.mqttClient != null) {
						// TODO: retrieve the QoS level from the configuration file
						_Logger.fine("Publishing data to MQTT broker: " + jsonData);
						return this.mqttClient.publishMessage(resourceName, jsonData, 0);
					}
					
					// TODO: If the GDA is hosting a CoAP server (or a CoAP client that
					// will connect to the CDA's CoAP server), you can add that logic here
					// in place of the MQTT client or in addition
					
				} else {
					_Logger.warning("Failed to parse incoming message. Unknown type: " + msg);
					
					return false;
				}
			} catch (Exception e) {
				_Logger.log(Level.WARNING, "Failed to process incoming message for resource: " + resourceName, e);
			}
		} else {
			_Logger.warning("Incoming message has no data. Ignoring for resource: " + resourceName);
		}
		
		return false;
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if (data != null) {
			_Logger.info(">>> handleSensorMessage CALLED - Resource: " + resourceName + ", Name: " + data.getName() + ", Value: " + data.getValue());
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}
			
			String jsonData = DataUtil.getInstance().sensorDataToJson(data);
			
			_Logger.fine("JSON [SensorData] -> " + jsonData);
			
			// TODO: retrieve this from config file
			int qos = ConfigConst.DEFAULT_QOS;
			
			// NOTE: Your code may not have a persistenceClient reference or
			// a enablePersistenceClient boolean
			if (this.enablePersistenceClient && this.persistenceClient != null) {
				this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
			}
			
			this.handleIncomingDataAnalysis(resourceName, data);
			_Logger.info(">>> About to call handleUpstreamTransmission for value: " + data.getValue());
			
			this.handleUpstreamTransmission(resourceName, jsonData, qos);
			_Logger.info(">>> Finished handleUpstreamTransmission for value: " + data.getValue());
			
			return true;
		} else {
			return false;
		}
	}

	private void handleUpstreamTransmission(ResourceNameEnum resource, String jsonData, int qos)
	{
		// TODO: feel free to change the logging levels for debugging and monitoring
		_Logger.info("Sending JSON data to cloud service: " + resource);
		
		if (this.cloudClient != null) {
			SensorData data = DataUtil.getInstance().jsonToSensorData(jsonData);
			_Logger.info("Converted JSON data to SensorData instance for upstream transmission." + jsonData);
			
			
			if (this.cloudClient.sendEdgeDataToCloud(resource, data)) {
				_Logger.info("Sent JSON data upstream to CSP.");
			}
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data)
	{
		// check either resource or SensorData for type
		if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
			handleHumiditySensorAnalysis(resource, data);
		}
	}

	private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data)
	{
		//
		// NOTE: INCOMPLETE and VERY BASIC CODE SAMPLE. Not intended to provide a solution.
		//
		
		_Logger.fine("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());
		
		boolean isLow  = data.getValue() < this.triggerHumidifierFloor;
		boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;
		
		if (isLow || isHigh) {
			_Logger.fine("Humidity data from CDA exceeds nominal range.");
			
			if (this.latestHumiditySensorData == null) {
				// set properties then exit - nothing more to do until the next sample
				this.latestHumiditySensorData = data;
				this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);
				
				_Logger.fine(
					"Starting humidity nominal exception timer. Waiting for seconds: " +
					this.humidityMaxTimePastThreshold);
				
				return;
			} else {
				OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);
				
				long diffSeconds =
					ChronoUnit.SECONDS.between(
						this.latestHumiditySensorTimeStamp, curHumiditySensorTimeStamp);
				
				_Logger.fine("Checking Humidity value exception time delta: " + diffSeconds);
				
				if (diffSeconds >= this.humidityMaxTimePastThreshold) {
					ActuatorData ad = new ActuatorData();
					ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
					ad.setLocationID(data.getLocationID());
					ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
					ad.setValue(this.nominalHumiditySetting);
					
					if (isLow) {
						ad.setCommand(ConfigConst.ON_COMMAND);
					} else if (isHigh) {
						ad.setCommand(ConfigConst.OFF_COMMAND);
					}
					
					_Logger.info(
						"Humidity exceptional value reached. Sending actuation event to CDA: " +
						ad);
					
					this.lastKnownHumidifierCommand = ad.getCommand();
					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);
					
					// set ActuatorData and reset SensorData (and timestamp)
					this.latestHumidifierActuatorData = ad;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				}
			}
		} else if (this.lastKnownHumidifierCommand == ConfigConst.ON_COMMAND) {
			// check if we need to turn off the humidifier
			if (this.latestHumidifierActuatorData != null) {
				// check the value - if the humidifier is on, but not yet at nominal, keep it on
				if (this.latestHumidifierActuatorData.getValue() >= this.nominalHumiditySetting) {
					this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);
					
					_Logger.info(
						"Humidity nominal value reached. Sending OFF actuation event to CDA: " +
						this.latestHumidifierActuatorData);
					
					sendActuatorCommandtoCda(
						ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, this.latestHumidifierActuatorData);
					
					// reset ActuatorData and SensorData (and timestamp)
					this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();
					this.latestHumidifierActuatorData = null;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				} else {
					_Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");
				}
			} else {
				// shouldn't happen, unless some other logic
				// nullifies the class-scoped ActuatorData instance
				_Logger.warning(
					"ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
			}
		}
	}

	private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data)
	{
		// NOTE: This is how an ActuatorData command will get passed to the CDA
		// when the GDA is providing the CoAP server and hosting the appropriate
		// ActuatorData resource. It will typically be used when the OBSERVE
		// client (the CDA, assuming the GDA is the server and CDA is the client)
		// has sent an OBSERVE GET request to the ActuatorData resource.
		if (this.actuatorDataListener != null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}
		
		// NOTE: This is how an ActuatorData command will get passed to the CDA
		// when using MQTT to communicate between the GDA and CDA
		if (this.enableMqttClient && this.mqttClient != null) {
			String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
			
			if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {
				_Logger.info(
					"Published ActuatorData command from GDA to CDA: " + data.getCommand());
			} else {
				_Logger.warning(
					"Failed to publish ActuatorData command from GDA to CDA: " + data.getCommand());
			}
		}
	}
		
	private OffsetDateTime getDateTimeFromData(BaseIotData data)
	{
		OffsetDateTime odt = null;
		
		try {
			odt = OffsetDateTime.parse(data.getTimeStamp());
		} catch (Exception e) {
			_Logger.warning(
				"Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");
			
			// TODO: this won't be accurate, but should be reasonably close, as the CDA will
			// most likely have recently sent the data to the GDA
			odt = OffsetDateTime.now();
		}
		
		return odt;
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());
			
			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}
			
			// TODO: retrieve this from config file
			int qos = ConfigConst.DEFAULT_QOS;
			
			// NOTE: You may want to persist your SystemPerformanceData here
			
			// NOTE: You may want to also analyze the SystemPerformanceData here
			
			this.handleUpstreamTransmission(resourceName, data.toString(), qos);
			
			return true;
		} else {
			return false;
		}
	}
	
	public void startManager()
	{
		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Successfully connected MQTT client to broker.");
				
				// NOTE: Subscriptions are now handled in MqttClientConnector.connectComplete()
				// as per Lab Module 10. This ensures subscriptions are properly restored
				// on reconnect.

			} else {
				_Logger.severe("Failed to connect MQTT client to broker.");
				
				// TODO: take appropriate action
			}
		}
		
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}
		
		if (this.redisClient != null) {
			this.redisClient.connectClient();
		}
		
		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.startServer()) {
				_Logger.info("CoAP server started.");
			} else {
				_Logger.severe("Failed to start CoAP server. Check log file for details.");
			}
		}
		if (this.enableCloudClient && this.cloudClient != null) {
			if (this.cloudClient.connectClient()) {
				_Logger.info("Successfully connected to Ubidots cloud.");
			} else {
				_Logger.severe("Failed to connect to Ubidots cloud.");
			}
		}
	}
	
	public void stopManager()
	{
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.stopManager();
		}
		
		if (this.redisClient != null) {
			this.redisClient.disconnectClient();
		}
		
		if (this.mqttClient != null) {
			// add necessary un-subscribes
			
			// TODO: check the return value for each and take appropriate action
			
			// NOTE: The unsubscribeFromTopic() method calls below should match with
			// the subscribeToTopic() method calls from startManager(). Also, the
			// unsubscribe logic below can be moved to MqttClientConnector's
			// disconnectClient() call PRIOR to actually disconnecting from
			// the MQTT broker.
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
			
			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Successfully disconnected MQTT client from broker.");
			} else {
				_Logger.severe("Failed to disconnect MQTT client from broker.");
				
				// TODO: take appropriate action
			}
		}
		
		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.stopServer()) {
				_Logger.info("CoAP server stopped.");
			} else {
				_Logger.severe("Failed to stop CoAP server. Check log file for details.");
			}
		}
		if (this.enableCloudClient && this.cloudClient != null) {
			if (this.cloudClient.disconnectClient()) {
				_Logger.info("Successfully disconnected from Ubidots cloud.");
			} else {
				_Logger.severe("Failed to disconnect from Ubidots cloud.");
			}
		}
	}

	
	// private methods
	
	/**
	 * Initializes the enabled connections. This will NOT start them, but only create the
	 * instances that will be used in the {@link #startManager() and #stopManager()) methods.
	 * 
	 */
	
	// THIS LOOKS TO BE OLD NAME!!!
	// THE LAB05 INSTRUCTIONS CALL THIS initManager()
	private void initConnections()
	{
	}
	
	private void initManager()
	{
		ConfigUtil configUtil = ConfigUtil.getInstance();
		
		this.enableSystemPerf =
			configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,  ConfigConst.ENABLE_SYSTEM_PERF_KEY);
		
		if (this.enableSystemPerf) {
			this.sysPerfMgr = new SystemPerformanceManager();
			this.sysPerfMgr.setDataMessageListener(this);
		}
		
		if (this.redisClient == null) {
			this.redisClient = new RedisPersistenceAdapter();
		}
		
		// NOTE: This is new - creating the MQTT client connector instance
		if (this.enableMqttClient) {
			this.mqttClient = new MqttClientConnector();
			
			// NOTE: The next line isn't technically needed until Lab Module 10
			this.mqttClient.setDataMessageListener(this);
		}
		
		if (this.enableCoapServer) {
			// TODO: implement this in Lab Module 8
		}
		
		if (this.enableCloudClient) {
			this.cloudClient = new CloudClientConnector();
			this.cloudClient.setDataMessageListener(this);
		}
		
		if (this.enablePersistenceClient) {
			// TODO: implement this as an optional exercise in Lab Module 5
		}
		
		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
		}
		/*
		if (ConfigConst.DEFAULT_CLOUD_PROVIDER == ConfigConst.CLOUD_PROVIDER_MQTT) {
		    this.cloudClient = new MqttCloudClientConnector();
		} else if (ConfigConst.DEFAULT_CLOUD_PROVIDER == ConfigConst.CLOUD_PROVIDER_COAP) {
		    this.cloudClient = new CoapCloudClientConnector();
		} else if (ConfigConst.DEFAULT_CLOUD_PROVIDER == ConfigConst.CLOUD_PROVIDER_UBIDOTS) {
		    this.cloudClient = new UbidotsCloudClient();
		}*/
	}
	
	private void handleIncomingDataAnalysis(ResourceNameEnum resource, ActuatorData data)
	{
		_Logger.info("Analyzing incoming actuator data: " + data.getName());
		
		if (data.isResponseFlagEnabled()) {
			// TODO: implement this
		} else {
			if (this.actuatorDataListener != null) {
				this.actuatorDataListener.onActuatorDataUpdate(data);
			}
		}
	}
}