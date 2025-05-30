import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.californium.core.coap.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

public class GetActuatorCommandResourceHandlerTest {
    private GetActuatorCommandResourceHandler handler;
    private CoapExchange mockExchange;
    private ActuatorData testData;

    @BeforeEach
    public void setUp() {
        handler = new GetActuatorCommandResourceHandler("testResource");
        mockExchange = mock(CoapExchange.class);
        testData = new ActuatorData();
        testData.setValue(50.0);
    }

    @Test
    public void testOnActuatorDataUpdate() {
        // Inicializar actuatorData para evitar NPE
        handler.onActuatorDataUpdate(new ActuatorData());

        assertTrue(handler.onActuatorDataUpdate(testData));
        // Verificar que los datos se actualizaron (podrías añadir getters para comprobarlo)
    }

    @Test
    public void testHandleGETWithNoData() {
        handler.handleGET(mockExchange);
        verify(mockExchange).accept();
        verify(mockExchange).respond(ResponseCode.SERVICE_UNAVAILABLE, anyString());
    }

    @Test
    public void testHandleGETWithData() {
        handler.onActuatorDataUpdate(testData);  // Simular datos previos
        handler.handleGET(mockExchange);

        verify(mockExchange).accept();
        verify(mockExchange).respond(eq(ResponseCode.CONTENT), anyString());
    }
}