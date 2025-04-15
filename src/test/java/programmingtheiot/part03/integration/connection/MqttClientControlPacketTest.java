package programmingtheiot.part03.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.MqttClientConnector;



public class MqttClientControlPacketTest {
    private static final Logger _Logger = 
        Logger.getLogger(MqttClientControlPacketTest.class.getName());
    
    private MqttClientConnector mqttClient = null;
    
    @Before
    public void setUp() throws Exception {
        this.mqttClient = new MqttClientConnector();
    }
    
    @After
    public void tearDown() throws Exception {
        if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
        }
    }
    
    @Test
    public void testConnectAndDisconnect() {
        // Test CONNECT and CONNACK packets
        assertTrue(this.mqttClient.connectClient());
        
        // Test DISCONNECT packet
        assertTrue(this.mqttClient.disconnectClient());
    }
    
    @Test
    public void testServerPing() throws InterruptedException {
        // Test PINGREQ and PINGRESP packets
        assertTrue(this.mqttClient.connectClient());
        
        // Wait for keep-alive interval to trigger ping
        Thread.sleep(this.mqttClient.getKeepAlive() * 1000 + 2000);
        
        assertTrue(this.mqttClient.disconnectClient());
    }
    
    @Test
    public void testPubSub() {
        // Test all control packets through publish/subscribe flow
        assertTrue(this.mqttClient.connectClient());
        
        int qos1 = 1;
        int qos2 = 2;
        String testPayload = "Test message";
        
        // Test SUBSCRIBE and SUBACK packets (QoS 1)
        assertTrue(this.mqttClient.subscribeToTopic(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos1));
        
        // Test PUBLISH and PUBACK packets (QoS 1)
        assertTrue(this.mqttClient.publishMessage(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, testPayload, qos1));
        
        // Test SUBSCRIBE and SUBACK packets (QoS 2)
        assertTrue(this.mqttClient.subscribeToTopic(
            ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos2));
            
        // Test PUBLISH, PUBREC, PUBREL and PUBCOMP packets (QoS 2)
        assertTrue(this.mqttClient.publishMessage(
            ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, testPayload, qos2));
            
        // Test UNSUBSCRIBE and UNSUBACK packets
        assertTrue(this.mqttClient.unsubscribeFromTopic(
            ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));
        assertTrue(this.mqttClient.unsubscribeFromTopic(
            ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE));
        
        assertTrue(this.mqttClient.disconnectClient());
    }
}