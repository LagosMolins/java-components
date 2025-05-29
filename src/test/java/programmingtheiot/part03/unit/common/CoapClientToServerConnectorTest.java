package programmingtheiot.gda.connection;

import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SensorData;

public class CoapClientToServerConnectorTest
{
	private CoapClientConnector coapClient;

	private static final boolean USE_DEFAULT_RESOURCES = true;
	private static final int DEFAULT_TIMEOUT = 5000; // 5 seconds timeout

	@Before
	public void setUp() throws Exception
	{
		this.coapClient = new CoapClientConnector();
	}

	@Test
	public void testSystemPerformancePutMessage()
	{
		SystemPerformanceData spData = new SystemPerformanceData();

		// Opcional: asignar valores específicos
		spData.setCpuUtil(0.42f);
		spData.setDiskUtil(0.33f);
		spData.setMemUtil(0.77f);

		String jsonData = DataUtil.getInstance().systemPerformanceDataToJson(spData);

		this.coapClient.sendPutRequest(
			ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE,
			null,
			USE_DEFAULT_RESOURCES,
			jsonData,
			DEFAULT_TIMEOUT
		);
	}

	@Test
	public void testSensorDataPutMessage()
	{
		SensorData sensorData = new SensorData();

		// Opcional: asignar valores específicos
		sensorData.setName("TestSensor");
		sensorData.setValue(22.5f);
		sensorData.setTypeID(1); // Tipo 1 = temperatura

		String jsonData = DataUtil.getInstance().sensorDataToJson(sensorData);

		this.coapClient.sendPutRequest(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
			null,
			USE_DEFAULT_RESOURCES,
			jsonData,
			DEFAULT_TIMEOUT
		);
	}
}
