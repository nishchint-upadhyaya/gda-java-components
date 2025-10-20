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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());
	
	private boolean useAsyncClient = false;

	private MqttClient           mqttClient = null;
	private MqttConnectOptions   connOpts = null;
	private MemoryPersistence    persistence = null;
	private IDataMessageListener dataMsgListener = null;

	private String  clientID = null;
	private String  brokerAddr = null;
	private String  host = ConfigConst.DEFAULT_HOST;
	private String  protocol = ConfigConst.DEFAULT_MQTT_PROTOCOL;
	private int     port = ConfigConst.DEFAULT_MQTT_PORT;
	private int     brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
	
	// params
	
	
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public MqttClientConnector()
	{
		super();

		ConfigUtil configUtil = ConfigUtil.getInstance();
	
		this.host =
			configUtil.getProperty(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		
		this.port =
			configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
		
		this.brokerKeepAlive =
			configUtil.getInteger(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		
		// This next config file boolean property is optional; it can be
		// set within the [Mqtt.GatewayService] and [Cloud.GatewayService]
		// sections of PiotConfig.props. You can use it to create a logical
		// flow within this class to determine whether to use MqttClient
		// or MqttAsyncClient, or simply choose one of the two classes based
		// on your usage needs. Generally speaking, MqttAsyncClient will
		// be necessary when running the GDA as an application, as it will
		// need to handle incoming and outgoing messages using MQTT
		// simultaneously. For GDA-only testing using the test cases
		// specified in this lab module and others, it's generally best -
		// and likely required - to use MqttClient.
		// 
		// IMPORTANT: If you're using an older version of ConfigConst.java,
		// you'll need to add the following line of code to ConfigConst.java:
		// public static final String USE_ASYNC_CLIENT_KEY = "useAsyncClient";
		this.useAsyncClient =
			configUtil.getBoolean(
				ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.USE_ASYNC_CLIENT_KEY);
		
		// NOTE: paho Java client requires a client ID - for now, you
		// can use the generated client ID; for later exercises, you
		// should define your own and load it from the config file
		this.clientID = MqttClient.generateClientId();
		
		// these are specific to the MQTT connection which will be used during connect
		this.persistence = new MemoryPersistence();
		this.connOpts = new MqttConnectOptions();
		
		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		
		// NOTE: If using a random clientID for each new connection,
		// clean session should be 'true'; see MQTT spec for details
		this.connOpts.setCleanSession(false);
		
		// NOTE: Auto-reconnect can be a useful connection recovery feature
		this.connOpts.setAutomaticReconnect(true);
		
		// NOTE: URL does not have a protocol handler for "tcp",
		// so we need to construct the URL manually
		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
	}
	
	
	// public methods
	
	@Override
	public boolean connectClient()
	{
		try {
			if (this.mqttClient == null) {
				this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
			}
			
			if (! this.mqttClient.isConnected()) {
				_Logger.info("MQTT client connecting to broker: " + this.brokerAddr);
				this.mqttClient.connect(this.connOpts);
				return true;
			} else {
				_Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
			}
		} catch (MqttException e) {
			// TODO: handle this exception
			_Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker.", e);
		}
		
		return false;
	}

	@Override
	public boolean disconnectClient()
	{
		try {
			if (this.mqttClient != null) {
				if (this.mqttClient.isConnected()) {
					_Logger.info("Disconnecting MQTT client from broker: " + this.brokerAddr);
					this.mqttClient.disconnect();
					return true;
				} else {
					_Logger.warning("MQTT client not connected to broker: " + this.brokerAddr);
				}
			}
		} catch (Exception e) {
			// TODO: handle this exception
			_Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker: " + this.brokerAddr, e);
		}
		
		return false;
	}

	public boolean isConnected()
	{
		return false;
	}
	
	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		_Logger.info("publishMessage() called with topic: " + topicName + ", message: " + msg + ", QoS: " + qos);
		return false;
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		_Logger.info("subscribeToTopic() called with topic: " + topicName + ", QoS: " + qos);
		return false;
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		return false;
	}

	@Override
	public boolean setConnectionListener(IConnectionListener listener)
	{
		return false;
	}
	
	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null) {
			this.dataMsgListener = listener;
			return true;
		}
		_Logger.info("setDataMessageListener() called with listener: " + listener);
		return false;
	}
	
	// callbacks
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info("MQTT connection successful (is reconnect = " + reconnect + "). Broker: " + serverURI);
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.log(Level.WARNING, "Lost connection to MQTT broker: " + this.brokerAddr, t);
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		// TODO: Logging level may need to be adjusted to see output in log file / console
		_Logger.fine("Delivered MQTT message with ID: " + token.getMessageId());
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage msg) // throws Exception
	{
		// TODO: Logging level may need to be adjusted to reduce output in log file / console
		_Logger.info("MQTT message arrived on topic: '" + topic + "'");
	}

	
	// private methods
	
	/**
	 * Called by the constructor to set the MQTT client parameters to be used for the connection.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initClientParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to load credentials.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initCredentialConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to enable encryption.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initSecureConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
}
