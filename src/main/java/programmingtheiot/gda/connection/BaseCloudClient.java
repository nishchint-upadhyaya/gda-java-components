package programmingtheiot.gda.connection;

import org.eclipse.paho.client.mqttv3.*;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.DataUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Generic MQTT-based base client that implements Andy's ICloudClient.
 * Subclasses (e.g., UbidotsCloudClient) can override topic shaping and auth.
 */
public abstract class BaseCloudClient implements ICloudClient, MqttCallbackExtended
{
    protected final String sectionName; // e.g., ConfigConst.CLOUD_GATEWAY_SERVICE_UBIDOTS

    // MQTT / config
    protected String host;
    protected int securePort;
    protected int keepAlive;
    protected int defaultQos;
    protected boolean enableAuth;
    protected boolean enableCrypt;
    protected String clientId;
    protected String baseTopic;     // e.g., "/v1.6/devices"

    protected String deviceLabel;   // optional convenience (can also be part of baseTopic)

    protected MqttClient mqttClient;
    protected MqttConnectOptions connOpts;

    protected IDataMessageListener dataMsgListener;

    protected BaseCloudClient(String sectionName)
    {
        this.sectionName = sectionName;
        initFromConfig();
        initMqtt();
    }

    /* -------------------------- ICloudClient -------------------------- */

    @Override
    public boolean connectClient()
    {
        try {
            if (mqttClient != null && !mqttClient.isConnected()) {
                mqttClient.connect(connOpts);
            }
            return isClientConnected();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean disconnectClient()
    {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
    {
        if (!isClientConnected() || data == null) return false;

        try {
            String json = DataUtil.getInstance().sensorDataToJson(data);
            String topic = buildPublishTopic(resource, data);
            return publish(topic, defaultQos, json.getBytes(StandardCharsets.UTF_8), false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
    {
        if (!isClientConnected() || data == null) return false;

        try {
            String json = DataUtil.getInstance().systemPerformanceDataToJson(data);
            String topic = buildPublishTopic(resource, data);
            return publish(topic, defaultQos, json.getBytes(StandardCharsets.UTF_8), false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean subscribeToCloudEvents(ResourceNameEnum resource)
    {
        if (!isClientConnected()) return false;

        try {
            String topic = buildSubscribeTopic(resource);
            // Per-topic listener that routes by resource type:
            IMqttMessageListener listener = (rtopic, msg) -> handleInboundMessage(resource, rtopic, msg);
            mqttClient.subscribe(topic, defaultQos, listener);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
    {
        if (!isClientConnected()) return false;
        try {
            String topic = buildSubscribeTopic(resource);
            mqttClient.unsubscribe(topic);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        this.dataMsgListener = listener;
        return (listener != null);
    }

    /* -------------------------- MqttCallbackExtended -------------------------- */

    @Override
    public void connectComplete(boolean reconnect, String serverURI)
    {
        // Subclasses may resubscribe or perform post-connect actions here.
    }

    @Override
    public void connectionLost(Throwable cause) { /* optional logging */ }

    @Override
    public void messageArrived(String topic, MqttMessage message) { /* only used if wildcard subs w/o explicit listener */ }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { /* no-op */ }

    /* -------------------------- Helpers / Hooks -------------------------- */

    public boolean isClientConnected()
    {
        return mqttClient != null && mqttClient.isConnected();
    }

    protected void initFromConfig()
    {
        ConfigUtil cfg = ConfigUtil.getInstance();

        this.host        = cfg.getProperty(sectionName, ConfigConst.HOST_KEY, "localhost");
        this.securePort  = cfg.getInteger(sectionName, ConfigConst.SECURE_PORT_KEY, 8883);
        this.keepAlive   = cfg.getInteger(sectionName, ConfigConst.KEEP_ALIVE_KEY, 60);
        this.defaultQos  = cfg.getInteger(sectionName, ConfigConst.DEFAULT_QOS_KEY, 1);
        this.enableAuth  = cfg.getBoolean(sectionName, ConfigConst.ENABLE_AUTH_KEY);
        this.enableCrypt = cfg.getBoolean(sectionName, ConfigConst.ENABLE_CRYPT_KEY);
        this.baseTopic   = cfg.getProperty(sectionName, ConfigConst.BASE_TOPIC_KEY, "");
        this.deviceLabel = cfg.getProperty(sectionName, ConfigConst.DEVICE_ID_PROP, "gda-1");

        this.clientId = cfg.getProperty(sectionName, ConfigConst.DEVICE_ID_PROP,
            "GDA-" + UUID.randomUUID());
    }

    protected void initMqtt()
    {
        try {
            String scheme = enableCrypt ? "ssl" : "tcp";
            String brokerUri = scheme + "://" + host + ":" + securePort;

            this.mqttClient = new MqttClient(brokerUri, clientId, null);
            this.mqttClient.setCallback(this);

            this.connOpts = new MqttConnectOptions();
            this.connOpts.setCleanSession(true);
            this.connOpts.setKeepAliveInterval(keepAlive);

            // Auth/TLS specifics are set by subclass via configureAuthTls(...)
            configureAuthTls(connOpts);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }

    protected boolean publish(String topic, int qos, byte[] payload, boolean retained)
    {
        try {
            MqttMessage m = new MqttMessage(payload);
            m.setQos(qos);
            m.setRetained(retained);
            mqttClient.publish(topic, m);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Default topic builders — subclasses can override for provider-specific needs.
     */
    protected String buildPublishTopic(ResourceNameEnum resource, Object dataObj)
    {
        // Default: {baseTopic}/{deviceLabel}/{resourceName}
        return join(baseTopic, deviceLabel, resource.getResourceName());
    }

    protected String buildSubscribeTopic(ResourceNameEnum resource)
    {
        // Default: subscribe to the same path used for publishes (or override)
        return join(baseTopic, deviceLabel, resource.getResourceName());
    }

    /**
     * Default inbound handler — attempts SensorData then SystemPerformanceData.
     * Subclasses can override for provider-specific payloads.
     */
    protected void handleInboundMessage(ResourceNameEnum resource, String topic, MqttMessage msg)
    {
        if (dataMsgListener == null || msg == null) return;

        String payload = new String(msg.getPayload(), StandardCharsets.UTF_8);

        try {
            switch (resource) {
                case CDA_SENSOR_MSG_RESOURCE: {
                    SensorData sd = DataUtil.getInstance().jsonToSensorData(payload);
                    dataMsgListener.handleSensorMessage(resource, sd);
                    break;
                }
                case CDA_SYSTEM_PERF_MSG_RESOURCE: {
                    SystemPerformanceData sp = DataUtil.getInstance().jsonToSystemPerformanceData(payload);
                    dataMsgListener.handleSystemPerformanceMessage(resource, sp);
                    break;
                }
                // Add other resources as needed
                default:
                    // Unknown resource; ignore or log
                    break;
            }
        } catch (Exception e) {
            // swallow or log parse errors
        }
    }

    /**
     * Let subclasses wire token/password and TLS keystores as needed.
     */
    protected void configureAuthTls(MqttConnectOptions opts) throws Exception
    {
        // no-op in base
    }

    protected static String join(String... parts)
    {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') sb.append('/');
            if (p.startsWith("/")) p = p.substring(1);
            if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
            sb.append(p);
        }
        return sb.toString();
    }
}