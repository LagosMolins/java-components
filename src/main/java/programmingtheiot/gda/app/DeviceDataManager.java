/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

 package programmingtheiot.gda.app;

 import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
 import programmingtheiot.gda.connection.CoapServerGateway;
 import programmingtheiot.gda.connection.IPersistenceClient;
 import programmingtheiot.gda.connection.IPubSubClient;
 import programmingtheiot.gda.connection.IRequestResponseClient;
 import programmingtheiot.gda.system.SystemPerformanceManager;
 
 import programmingtheiot.gda.connection.MqttClientConnector;
 
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
	 private IPubSubClient cloudClient = null;
	 private IPersistenceClient persistenceClient = null;
	 private IRequestResponseClient smtpClient = null;
	 private CoapServerGateway coapServer = null;
	 private SystemPerformanceManager sysPerfMgr = null;


	private ActuatorData latestHumidifierActuatorData =null;
	private ActuatorData latestHumidifierActuatorResponse =null;
	private SensorData latestHumiditySensorData =null;
	private OffsetDateTime latestHumiditySensorTimeStamp =null;

	private boolean handleHumidityChangeOnDevice =false;// optional
	private int lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

	// TODO: Load these from PiotConfig.props
	private long humidityMaxTimePastThreshold =300;// seconds
	private float nominalHumiditySetting   =40.0f;
	private float triggerHumidifierFloor   =30.0f;
	private float triggerHumidifierCeiling =50.0f;



 
	 // constructors
	 
	 public DeviceDataManager()
	 {
		 super();
 
		 ConfigUtil configUtil = ConfigUtil.getInstance();
 
		 this.enableMqttClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
 
		 this.enableCoapServer = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
 
		 this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
 
		 this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
 
		 this.handleHumidityChangeOnDevice = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,"handleHumidityChangeOnDevice");
 
		 this.humidityMaxTimePastThreshold = configUtil.getInteger(ConfigConst.GATEWAY_DEVICE,"humidityMaxTimePastThreshold");
 
		 this.nominalHumiditySetting = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE,"nominalHumiditySetting");
 
		 this.triggerHumidifierFloor = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE,"triggerHumidifierFloor");
 
		 this.triggerHumidifierCeiling = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE,"triggerHumidifierCeiling");
 
		 if (this.humidityMaxTimePastThreshold <10 ||this.humidityMaxTimePastThreshold >7200) {
			 this.humidityMaxTimePastThreshold =300;
		 }
 
		 initManager();
		 initConnections();
	 }
	 
	 
	 public DeviceDataManager(
		 boolean enableMqttClient,
		 boolean enableCoapClient,
		 boolean enableCloudClient,
		 boolean enableSmtpClient,
		 boolean enablePersistenceClient)
	 {
		 super();
		 
		 this.enableMqttClient = enableMqttClient;
		 this.enableCoapServer = enableCoapClient;
		 this.enableCloudClient = enableCloudClient;
		 this.enableSmtpClient = enableSmtpClient;
		 this.enablePersistenceClient = enablePersistenceClient;
		 
		 initManager();
	 }
	 
	 // public methods
	 
	 @Override
	 public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	 {
		 if (data != null) {
			 _Logger.info("Handling actuator response: " + data.getName());
	 
			 this.handleIncomingDataAnalysis(resourceName, data);
	 
			 if (data.hasError()) {
				 _Logger.warning("Error flag set for ActuatorData instance.");
			 }
	 
			 return true;
		 } else {
			 return false;
		 }
	 }
 
	 @Override
	 public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	 {
		 return false;
	 }
 
	 @Override
	 public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	 {
		 if (msg != null) {
			 _Logger.info("Handling incoming message from " + resourceName.getResourceName() + ": " + msg);
			 
			 // Aquí puedes añadir lógica específica para cada tipo de mensaje
			 switch (resourceName) {
				 case CDA_SENSOR_MSG_RESOURCE:
					 // Procesar mensaje de sensor
					 break;
				 case CDA_ACTUATOR_RESPONSE_RESOURCE:
					 // Procesar respuesta de actuador
					 break;
				 case GDA_MGMT_STATUS_MSG_RESOURCE:
					 // Procesar mensaje de estado
					 break;
				 case CDA_SYSTEM_PERF_MSG_RESOURCE:
					 // Procesar datos de rendimiento
					 break;
				 default:
					 _Logger.warning("Unknown resource: " + resourceName);
			 }
			 
			 return true;
		 }
		 return false;
	 }
 
	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if (data !=null) {
			_Logger.fine("Handling sensor message: " +data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}

			String jsonData =DataUtil.getInstance().sensorDataToJson(data);

			_Logger.fine("JSON [SensorData] -> " +jsonData);

			// TODO: retrieve this from config file
			int qos =ConfigConst.DEFAULT_QOS;

			if (this.enablePersistenceClient &&this.persistenceClient !=null) {
				this.persistenceClient.storeData(resourceName.getResourceName(),qos,data);
			}

			this.handleIncomingDataAnalysis(resourceName,data);

			this.handleUpstreamTransmission(resourceName,jsonData,qos);

			return true;
		} else {
			return false;
		}
	}
	
	private void handleUpstreamTransmission(ResourceNameEnum resource,String jsonData,int qos)
	{
		// NOTE: This will be implemented in Part 04
		_Logger.info("TODO: Send JSON data to cloud service: " +resource);
		if (this.cloudClient != null) {
			//if (this.cloudClient.sendEdgeDataToCloud(resource, jsonData)) {
			_Logger.fine("Sent JSON data upstream to CSP.");
			//}
		}
	}
	
 
	 @Override
	 public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	 {
		 if (data != null) {
			 _Logger.info("Handling system performance message: " + data.getName());
	 
			 if (data.hasError()) {
				 _Logger.warning("Error flag set for SystemPerformanceData instance.");
			 }
	 
			 return true;
		 } else {
			 return false;
		 }
	 }
	 
	 public void setActuatorDataListener(String name, IActuatorDataListener listener)
	 {
		 if (listener != null) {
			 this.actuatorDataListener = listener;
			 _Logger.info("Actuator data listener set for name: " + name);
		 }
	 }
	 
	 public void startManager()
	{
		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Successfully connected MQTT client to broker.");
	
				// add necessary subscriptions
	
				//int qos = ConfigConst.DEFAULT_QOS;
	
	
				// IMPORTANT NOTE: The 'subscribeToTopic()' method calls shown
				// below will be moved to MqttClientConnector.connectComplete()
				// in Lab Module 10. For now, they can remain here.
				//this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
				//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
				//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
				//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
			} else {
				_Logger.severe("Failed to connect MQTT client to broker.");
	
			}
		}
	
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}

		// ADD in the following - DO NOT remove the other logic you've already included here
		// as part of previous lab modules.

		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.startServer()) {
				_Logger.info("CoAP server started.");
			} else {
				_Logger.severe("Failed to start CoAP server. Check log file for details.");
			}
		}

		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}

		if (this.cloudClient != null) {
			this.cloudClient.connectClient();
		}
	}
	
	 
	 public void stopManager()
	 {
		 if (this.sysPerfMgr != null) {
			 this.sysPerfMgr.stopManager();
		 }
		 
		 if (this.mqttClient != null) {
			 this.mqttClient.unsubscribeFromTopic(
				 ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			 this.mqttClient.unsubscribeFromTopic(
				 ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			 this.mqttClient.unsubscribeFromTopic(
				 ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			 this.mqttClient.unsubscribeFromTopic(
				 ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
 
			 if (this.mqttClient.disconnectClient()) {
				 _Logger.info("Successfully disconnected MQTT client from broker.");
			 } else {
				 _Logger.severe("Failed to disconnect MQTT client from broker.");
			 }
		 }
 
		 if (this.enableCoapServer && this.coapServer != null) {
			 if (this.coapServer.stopServer()) {
				 _Logger.info("CoAP server stopped successfully.");
			 } else {
				 _Logger.warning("Failed to stop CoAP server.");
			 }
		 }
	 }
 
	 // private methods
	 
	 private void initManager()
	 {
		 ConfigUtil configUtil = ConfigUtil.getInstance();
	 
		 this.enableSystemPerf =
			 configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);
	 
		 if (this.enableSystemPerf) {
			 this.sysPerfMgr = new SystemPerformanceManager();
			 this.sysPerfMgr.setDataMessageListener(this);
		 }
	 
		 if (this.enableMqttClient) {
			 this.mqttClient = new MqttClientConnector();
			 this.mqttClient.setDataMessageListener(this);
		 }
	 
		 if (this.enableCoapServer) {
			 this.coapServer = new CoapServerGateway(this);
			 _Logger.info("CoAP server initialized.");
		 }
	 
		 if (this.enableCloudClient) {
			 // TODO: implement this in Lab Module 10
		 }
	 
		 if (this.enablePersistenceClient) {
			 // TODO: implement this as an optional exercise in Lab Module 5
		 }
	 }
 
	 private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data)
	 {
		 _Logger.info("Analyzing incoming actuator data: " + data.getName());
 
		 if (data.isResponseFlagEnabled()) {
			 // TODO: implement this for response handling
		 } else {
			 if (this.actuatorDataListener != null) {
				 this.actuatorDataListener.onActuatorDataUpdate(data);
			 }
		 }
	 }
 
	 private void handleIncomingDataAnalysis(ResourceNameEnum resource,SensorData data)
	{
		if (data.getTypeID() ==ConfigConst.HUMIDITY_SENSOR_TYPE) {
			handleHumiditySensorAnalysis(resource,data);
		}
	}

    private void handleHumiditySensorAnalysis(ResourceNameEnum resource,SensorData data)
	{

		_Logger.fine("Analyzing humidity data from CDA: " +data.getLocationID() +". Value: " +data.getValue());

		boolean isLow  =data.getValue() <this.triggerHumidifierFloor;
		boolean isHigh =data.getValue() >this.triggerHumidifierCeiling;

		if (isLow ||isHigh) {
			_Logger.fine("Humidity data from CDA exceeds nominal range.");

			if (this.latestHumiditySensorData ==null) {
				this.latestHumiditySensorData =data;
				this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);

				_Logger.fine("Starting humidity nominal exception timer. Waiting for seconds: " +
				this.humidityMaxTimePastThreshold);

				return;
			} else {
				OffsetDateTime curHumiditySensorTimeStamp =getDateTimeFromData(data);

				long diffSeconds = ChronoUnit.SECONDS.between(this.latestHumiditySensorTimeStamp,curHumiditySensorTimeStamp);

				_Logger.fine("Checking Humidity value exception time delta: " +diffSeconds);

				if (diffSeconds >=this.humidityMaxTimePastThreshold) {
					ActuatorData ad =new ActuatorData();
					ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
					ad.setLocationID(data.getLocationID());
					ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
					ad.setValue(this.nominalHumiditySetting);

					if (isLow) {
						ad.setCommand(ConfigConst.ON_COMMAND);
					} else if (isHigh) {
						ad.setCommand(ConfigConst.OFF_COMMAND);
					}

					_Logger.info("Humidity exceptional value reached. Sending actuation event to CDA: " + ad);

					this.lastKnownHumidifierCommand =ad.getCommand();
					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,ad);

					this.latestHumidifierActuatorData =ad;
					this.latestHumiditySensorData =null;
					this.latestHumiditySensorTimeStamp =null;
				}
			}
		} else if (this.lastKnownHumidifierCommand ==ConfigConst.ON_COMMAND) {
			if (this.latestHumidifierActuatorData !=null) {
				if (this.latestHumidifierActuatorData.getValue() >=this.nominalHumiditySetting) {
					this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);

					_Logger.info("Humidity nominal value reached. Sending OFF actuation event to CDA: " +
					this.latestHumidifierActuatorData);

					sendActuatorCommandtoCda(
					ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,this.latestHumidifierActuatorData);

					this.lastKnownHumidifierCommand =this.latestHumidifierActuatorData.getCommand();
					this.latestHumidifierActuatorData =null;
					this.latestHumiditySensorData =null;
					this.latestHumiditySensorTimeStamp =null;
				}else {
					_Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");
				}
			}else {
				_Logger.warning("ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
			}
		}
	}

	private void sendActuatorCommandtoCda(ResourceNameEnum resource,ActuatorData data)
	{
	
		if (this.actuatorDataListener !=null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}

		
		if (this.enableMqttClient &&this.mqttClient !=null) {
			String jsonData =DataUtil.getInstance().actuatorDataToJson(data);

			if (this.mqttClient.publishMessage(resource,jsonData,ConfigConst.DEFAULT_QOS)) {
				_Logger.info("Published ActuatorData command from GDA to CDA: " +data.getCommand());
			} else {
				_Logger.warning("Failed to publish ActuatorData command from GDA to CDA: " +data.getCommand());
			}
		}
	}

	private OffsetDateTime getDateTimeFromData(BaseIotData data)
	{
		OffsetDateTime odt = null;

		try {
			odt =OffsetDateTime.parse(data.getTimeStamp());
		}catch (Exception e) {
			_Logger.warning("Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");

		// TODO: this won't be accurate, but should be reasonably close, as the CDA will
		// most likely have recently sent the data to the GDA
			odt =OffsetDateTime.now();
		}

		return odt;
	}


 
	 public void setDataMessageListener(IDataMessageListener listener)
	 {
		 if (listener != null) {
			 this.dataMsgListener = listener;
		 }
	 }
 }

