package programmingtheiot.integration.connection;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.junit.Test;

// JUnit 5 is optional; these annotations will be ignored if not present.
import static org.junit.Assert.*;

/**
 * UbidotsIntegrationIT
 *
 * A minimal, classroom-friendly integration test for Ubidots.
 * - HTTP test uses only Java 11+ stdlib (always runs).
 * - MQTT test runs only if:
 *     1) RUN_MQTT=1
 *     2) Eclipse Paho jar is on the classpath.
 *
 * You can run this either:
 *   - As a JUnit test (if JUnit 5 is in your project), OR
 *   - From the main() method as a standalone smoke test.
 */
public class UbidotsIntegrationIT {

    // ===== Student-configurable via environment variables =====
    private static final String TOKEN  = envOr("UBI_TOKEN", "BBUS-tEah5Lv9cCQel7UL4MZGlDLCDUKABz");            // required for HTTP
    private static final String DEVICE = envOr("UBI_DEVICE", "machine-a");    // default device label
    private static final String URL    = "https://industrial.api.ubidots.com/api/v1.6/devices/" + DEVICE;

    //private static final boolean RUN_MQTT   = "1".equals(envOr("RUN_MQTT", "0"));
    private static final boolean RUN_MQTT   = true;
    private static final String  MQTT_BROKER= envOr("MQTT_BROKER", "ssl://industrial.api.ubidots.com:8883");

    // ===== Standalone runner =====
    public static void main(String[] args) throws Exception {
        System.out.println(">>> UbidotsIntegrationIT (standalone) starting…");
        System.out.println("Device: " + DEVICE);
        if (TOKEN == null || TOKEN.isBlank()) {
            System.err.println("ERROR: UBI_TOKEN is not set. HTTP test cannot run.");
            System.exit(2);
        }

        boolean httpOk = httpPostSmokeTest();
        System.out.println("HTTP POST -> " + (httpOk ? "OK" : "FAILED"));

        boolean mqttOk = false;
        if (RUN_MQTT) {
            mqttOk = tryMqttPublish();
            System.out.println("MQTT PUBLISH -> " + (mqttOk ? "OK" : "SKIPPED/FAILED"));
        } else {
            System.out.println("MQTT PUBLISH -> SKIPPED (set RUN_MQTT=1 to enable)");
        }

        // Exit non-zero if mandatory (HTTP) failed
        if (!httpOk) System.exit(1);
        System.out.println(">>> UbidotsIntegrationIT complete.");
    }

    // ===== JUnit-compatible tests (optional) =====
    @Test
    public void httpPostShouldSucceed() throws Exception {
        assertNotNull(TOKEN, "UBI_TOKEN must be set for HTTP test");
        assertTrue(httpPostSmokeTest());
    }

    @Test
    public void mqttPublishShouldSucceedIfEnabledAndAvailable() throws Exception {
        if (!RUN_MQTT) {
            System.out.println("MQTT disabled via RUN_MQTT != 1 — skipping test.");
            return;
        }
        if (!isPahoAvailable()) {
            System.out.println("Paho MQTT jar not found on classpath — skipping MQTT test.");
            return;
        }
        assertTrue(tryMqttPublish());
    }

    // ===== Implementation: HTTP (always available) =====
    private static boolean httpPostSmokeTest() throws Exception {
        String payload = "{\"temperature\": " + (20 + (int)(Math.random()*15)) + ", \"humidity\": 42.0}";
        System.out.println("POST " + URL);
        System.out.println("Payload: " + payload);

        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP Status: " + resp.statusCode());
        System.out.println("HTTP Body  : " + truncate(resp.body(), 300));

        // Ubidots returns 2xx on success
        return resp.statusCode() >= 200 && resp.statusCode() < 300;
    }

    // ===== Implementation: MQTT (optional) =====
    private static boolean tryMqttPublish() {
        if (!isPahoAvailable()) {
            System.out.println("Paho MQTT library not found — skipping MQTT test.");
            return false;
        }

        try {
            // Reflective calls so the class compiles without the Paho jar present.
            Class<?> mqttClientCls = Class.forName("org.eclipse.paho.client.mqttv3.MqttClient");
            Class<?> mqttOptsCls   = Class.forName("org.eclipse.paho.client.mqttv3.MqttConnectOptions");
            Class<?> mqttMsgCls    = Class.forName("org.eclipse.paho.client.mqttv3.MqttMessage");

            String clientId = "gda-" + UUID.randomUUID();
            String topic    = "/v1.6/devices/" + DEVICE;
            String payload  = "{\"vibration\": " + String.format("%.3f", Math.random()/10.0) + "}";

            Object opts = mqttOptsCls.getConstructor().newInstance();
            mqttOptsCls.getMethod("setUserName", String.class).invoke(opts, TOKEN);
            mqttOptsCls.getMethod("setPassword", char[].class).invoke(opts, new char[0]);
            mqttOptsCls.getMethod("setCleanSession", boolean.class).invoke(opts, true);
            mqttOptsCls.getMethod("setAutomaticReconnect", boolean.class).invoke(opts, true);

            Object client = mqttClientCls
                    .getConstructor(String.class, String.class)
                    .newInstance(MQTT_BROKER, clientId);

            mqttClientCls.getMethod("connect", mqttOptsCls).invoke(client, opts);

            Object msg = mqttMsgCls
                    .getConstructor(byte[].class)
                    .newInstance(payload.getBytes(StandardCharsets.UTF_8));
            mqttMsgCls.getMethod("setQos", int.class).invoke(msg, 1);

            System.out.println("MQTT publish -> " + topic + " : " + payload);
            mqttClientCls.getMethod("publish", String.class, mqttMsgCls)
                    .invoke(client, topic, msg);

            mqttClientCls.getMethod("disconnect").invoke(client);
            mqttClientCls.getMethod("close").invoke(client);

            return true;
        } catch (ClassNotFoundException cnf) {
            System.out.println("Paho MQTT classes not found. Add org.eclipse.paho.client.mqttv3 to classpath.");
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    // ===== Helpers =====
    private static String envOr(String name, String defVal) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? defVal : v.trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return (s.length() <= max) ? s : s.substring(0, max) + "…";
    }

    private static boolean isPahoAvailable() {
        try {
            Class.forName("org.eclipse.paho.client.mqttv3.MqttClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}