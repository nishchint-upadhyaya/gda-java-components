package programmingtheiot.gda.connection;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Ubidots-specific implementation.
 * Typical pattern: baseTopic="/v1.6/devices", publish to "/v1.6/devices/{deviceLabel}/{variable}"
 * We map ResourceNameEnum -> variable names via config (or fallback to enum names).
 */
public class UbidotsCloudClient extends BaseCloudClient
{
    // Optional: per-resource variable names (override enum names if desired)
    private final String sensorVar;
    private final String sysPerfVar;

    public UbidotsCloudClient()
    {
        super(ConfigConst.UBIDOTS_CLOUD_GATEWAY_SERVICE);

        ConfigUtil cfg = ConfigUtil.getInstance();
        this.sensorVar  = cfg.getProperty(sectionName, ConfigConst.UBI_SENSOR_VAR_KEY,  "sensor");
        this.sysPerfVar = cfg.getProperty(sectionName, ConfigConst.UBI_SYSPERF_VAR_KEY, "system");
    }

    @Override
    protected void configureAuthTls(MqttConnectOptions opts) throws Exception
    {
        // Ubidots MQTT typically uses token as "username" and empty password (or vice-versa).
        // Store it in cred file and load via ConfigUtil.getCredential(...).
        /*String token = ConfigUtil.getInstance().getCredential(sectionName, "token", null);
        if (token != null && !token.isEmpty()) {
            opts.setUserName(token);
            opts.setPassword(new char[0]);
        }*/
    	// hardcode for now:
    	
    	 opts.setUserName("BBUS-tEah5Lv9cCQel7UL4MZGlDLCDUKABz");
         opts.setPassword(new char[0]);
         
        // TLS: often handled by the broker with default CA; if you have custom certs, wire SSLSocketFactory here.
    }

    @Override
    protected String buildPublishTopic(ResourceNameEnum resource, Object dataObj)
    {
        // Map resource -> Ubidots variable topic
        String variable;
        switch (resource) {
            case CDA_SENSOR_MSG_RESOURCE:
                variable = sensorVar;
                break;
            case CDA_SYSTEM_PERF_MSG_RESOURCE:
                variable = sysPerfVar;
                break;
            default:
                variable = resource.getResourceName();
        }
        // /v1.6/devices/{deviceLabel}/{variable}
        return join(baseTopic, deviceLabel, variable);
    }

    @Override
    protected String buildSubscribeTopic(ResourceNameEnum resource)
    {
        // If you consume downlink commands, put that topic here (often different path in Ubidots).
        // Default: same path as publish target (harmless if unused).
        return buildPublishTopic(resource, null);
    }
}