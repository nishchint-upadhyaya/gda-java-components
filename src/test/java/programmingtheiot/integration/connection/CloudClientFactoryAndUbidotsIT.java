package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assume;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CloudClientFactory;
import programmingtheiot.gda.connection.ICloudClient;
import programmingtheiot.gda.connection.UbidotsCloudClient;

/**
 * Combined tests:
 *  - Unit: verifies CloudClientFactory returns Ubidots client from config.
 *  - Optional Integration: publishes mock data to Ubidots (only runs when enabled via system prop).
 *
 * Enable the integration test with:
 *   mvn -DRUN_UBIDOTS_IT=true -Dtest=CloudClientFactoryAndUbidotsIT test
 */
public class CloudClientFactoryAndUbidotsIT {

    private static final String RUN_IT_FLAG = "RUN_UBIDOTS_IT";

    private CloudClientConnector cloud; // only used in the integration test
    private final IDataMessageListener stubListener = new NoopDataListener();

    /* ---------- Unit test: Factory ---------- */

    @Test
    public void factoryReturnsUbidots_fromConfig() {
        // Ensure ConfigUtil is initialized (reads ./config/PiotConfig.props)
        ConfigUtil.getInstance();

        ICloudClient client = CloudClientFactory.getInstance().createCloudClient();
        assertNotNull("Factory returned null client", client);
        assertTrue("Factory should return UbidotsCloudClient when cloudServiceName=Ubidots",
                client instanceof UbidotsCloudClient);
    }

    /* ---------- Optional Integration test: Publish to Ubidots ---------- */

    @Before
    public void setUp() {
        // Only set up the live client if integration test is enabled
        boolean runIT = Boolean.parseBoolean(System.getProperty(RUN_IT_FLAG, "false"));
        Assume.assumeTrue("Skipping live Ubidots test (set -DRUN_UBIDOTS_IT=true to enable).", runIT);

        cloud = new CloudClientConnector();
        cloud.setDataMessageListener(stubListener);
        assertTrue("Failed to connect to cloud broker. Check PiotConfig.props, token, and network.",
                cloud.connectClient());
    }

    @After
    public void tearDown() {
        if (cloud != null) {
            cloud.disconnectClient();
        }
    }

    @Test
    public void publishMockDataToUbidots_whenEnabled() throws Exception {
        // Gate on the flag so this entire test is skipped unless explicitly enabled
        //boolean runIT = Boolean.parseBoolean(System.getProperty(RUN_UBIDOTS_IT, "false"));
        //Assume.assumeTrue("Skipping live Ubidots publish test (set -DRUN_UBIDOTS_IT=true).", runIT);

        // --- Mock SensorData examples ---
        SensorData temp = new SensorData();
        temp.setName("temperature"); // becomes Ubidots variable when useTimeAndValuePayload=true
        temp.setValue(23.7f);
        //temp.setTimeStamp(System.currentTimeMillis());
        assertTrue("Failed to send SensorData (temperature).",
                cloud.sendEdgeDataToCloud(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, temp));

        SensorData humidity = new SensorData();
        humidity.setName("humidity");
        humidity.setValue(48.2f);
        //humidity.setTimeStamp(System.currentTimeMillis());
        assertTrue("Failed to send SensorData (humidity).",
                cloud.sendEdgeDataToCloud(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, humidity));

        // --- Mock SystemPerformanceData (split into cpu/mem SensorData internally) ---
        SystemPerformanceData spd = new SystemPerformanceData();
        spd.setCpuUtilization(0.42f);    // 42%
        spd.setMemoryUtilization(0.33f); // 33%
        spd.setDiskUtilization(0.51f);   // (not currently sent, but fine)
        //spd.setTimeStamp(System.currentTimeMillis());
        assertTrue("Failed to send SystemPerformanceData.",
                cloud.sendEdgeDataToCloud(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, spd));

        // Give async publish a brief window
        Thread.sleep(1200);
    }

    /* ---------- Minimal listener to avoid NPEs ---------- */
    private static class NoopDataListener implements IDataMessageListener {
        @Override public boolean handleActuatorCommandResponse(ResourceNameEnum r, programmingtheiot.data.ActuatorData d){ return true; }
        @Override public boolean handleActuatorCommandRequest(ResourceNameEnum r, programmingtheiot.data.ActuatorData d){ return true; }
        @Override public boolean handleSensorMessage(ResourceNameEnum r, programmingtheiot.data.SensorData d){ return true; }
        @Override public boolean handleSystemPerformanceMessage(ResourceNameEnum r, programmingtheiot.data.SystemPerformanceData d){ return true; }
        @Override public boolean handleIncomingMessage(ResourceNameEnum r, String msg){ return true; }
		@Override
		public void setActuatorDataListener(String name, IActuatorDataListener listener) {
			// TODO Auto-generated method stub
			
		}
    }
}