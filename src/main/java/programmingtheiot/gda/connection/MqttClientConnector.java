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

import programmingtheiot.gda.connection.IConnectionListener;


public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended {
    private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());
    
    private boolean useAsyncClient = false;
    private MqttClient mqttClient = null;
    private MqttConnectOptions connOpts = null;
    private MemoryPersistence persistence = null;
    private IDataMessageListener dataMsgListener = null;
    
    private String clientID = null;
    private String brokerAddr = null;
    private String host = ConfigConst.DEFAULT_HOST;
    private String protocol = ConfigConst.DEFAULT_MQTT_PROTOCOL;
    private int port = ConfigConst.DEFAULT_MQTT_PORT;
    private int brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
    
    public MqttClientConnector()
    {
        super();
        
        ConfigUtil configUtil = ConfigUtil.getInstance();
        
        this.host = configUtil.getProperty(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.HOST_KEY, 
            ConfigConst.DEFAULT_HOST);
        
        this.port = configUtil.getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.PORT_KEY, 
            ConfigConst.DEFAULT_MQTT_PORT);
        
        this.brokerKeepAlive = configUtil.getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.KEEP_ALIVE_KEY, 
            ConfigConst.DEFAULT_KEEP_ALIVE);
        
        this.useAsyncClient = configUtil.getBoolean(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.USE_ASYNC_CLIENT_KEY);
        
        this.clientID = MqttClient.generateClientId();
        this.persistence = new MemoryPersistence();
        this.connOpts = new MqttConnectOptions();
        
        this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
        this.connOpts.setCleanSession(false);
        this.connOpts.setAutomaticReconnect(true);
        
        this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;
    }

    private boolean isConnected() {
        return this.mqttClient != null && this.mqttClient.isConnected();
    }
    
    @Override
    public boolean connectClient()
    {
        try {
            if (this.mqttClient == null) {
                this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
                this.mqttClient.setCallback(this);
            }

            if (!this.mqttClient.isConnected()) {
                _Logger.info("MQTT client connecting to broker: " + this.brokerAddr);
                this.mqttClient.connect(this.connOpts);
                return true;
            } else {
                _Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
            }
        } catch (MqttException e) {
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
            _Logger.log(Level.SEVERE, "Failed to disconnect MQTT client from broker: " + this.brokerAddr, e);
        }
        return false;
    }

    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
        if (topicName == null || msg == null || qos < 0 || qos > 2) {
            _Logger.warning("Invalid parameters for publishMessage.");
            return false;
        }

        if (!isConnected()) {
            _Logger.warning("MQTT client not connected. Cannot publish.");
            return false;
        }
        try {
            String topic = topicName.getResourceName();
            MqttMessage mqttMsg = new MqttMessage(msg.getBytes());
            mqttMsg.setQos(qos);
            mqttMsg.setRetained(false);

            this.mqttClient.publish(topic, mqttMsg);
            _Logger.info("Published message to topic: " + topic + " [QoS: " + qos + "]");
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message to MQTT broker.", e);
            return false;
        }
    }

    @Override
    public boolean subscribeToTopic(ResourceNameEnum topicName, int qos) {
        if (topicName == null || qos < 0 || qos > 2) {
            _Logger.warning("Invalid parameters for subscribeToTopic.");
            return false;
        }

        if (!isConnected()) {
            _Logger.warning("MQTT client not connected. Cannot subscribe.");
            return false;
        }

        try {
            String topic = topicName.getResourceName();
            this.mqttClient.subscribe(topic, qos);
            _Logger.info("Subscribed to topic: " + topic + " [QoS: " + qos + "]");
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to subscribe to topic.", e);
            return false;
        }
    }



    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum topicName) {
        if (topicName == null) {
            _Logger.warning("Topic name is null.");
            return false;
        }

        if (!isConnected()) {
            _Logger.warning("MQTT client not connected. Cannot unsubscribe.");
            return false;
        }

        try {
            String topic = topicName.getResourceName();
            this.mqttClient.unsubscribe(topic);
            _Logger.info("Unsubscribed from topic: " + topic);
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to unsubscribe from topic.", e);
            return false;
        }
    }

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
    public void messageArrived(String topic, MqttMessage message)
    {
        // TODO: Logging level may need to be adjusted to reduce output in log file / console
        _Logger.info("MQTT message arrived on topic: '" + topic + "'");
    }

    @Override
    public boolean setConnectionListener(IConnectionListener listener)
    {
        _Logger.warning("Method setConnectionListener() not implemented yet");
        return false;
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            return true;
        }
        return false;
    }
    public int getKeepAlive() {
        return this.brokerKeepAlive;
    }
}
