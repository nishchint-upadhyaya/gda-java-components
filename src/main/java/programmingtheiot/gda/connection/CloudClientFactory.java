package programmingtheiot.gda.connection;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;

public class CloudClientFactory
{
    private static final CloudClientFactory INSTANCE = new CloudClientFactory();
    public static CloudClientFactory getInstance() { return INSTANCE; }
    private CloudClientFactory() { }

    public ICloudClient createCloudClient()
    {
        String section = ConfigConst.CLOUD_GATEWAY_SERVICE; // [Cloud.GatewayService]
        String name = ConfigUtil.getInstance().getProperty(section, ConfigConst.CLOUD_SERVICE_NAME_KEY, "Ubidots");

        switch (name.trim().toLowerCase()) {
            case "ubidots":
                return new UbidotsCloudClient();
            // case "aws": return new AwsIotCoreCloudClient();
            default:
                throw new IllegalArgumentException("Unsupported cloudServiceName: " + name);
        }
    }
    
    
}