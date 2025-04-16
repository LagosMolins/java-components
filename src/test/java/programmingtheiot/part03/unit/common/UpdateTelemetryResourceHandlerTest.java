
package programmingtheiot.gda.connection.handlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.Before;
import org.junit.Test;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.data.SensorData;

public class UpdateTelemetryResourceHandlerTest {
    private UpdateTelemetryResourceHandler handler;
    private CoapExchange mockExchange;
    private IDataMessageListener mockListener;

    @Before
    public void setUp() {
        handler = new UpdateTelemetryResourceHandler("test");
        mockListener = mock(IDataMessageListener.class);
        mockExchange = mock(CoapExchange.class);
        
        handler.setDataMessageListener(mockListener);
    }

    @Test
    public void testHandlePUTWithValidSensorData() {
        // Configura un payload JSON válido
        String jsonData = "{\"name\": \"TempSensor\", \"value\": 25.5, \"type\": \"temperature\"}";
        when(mockExchange.getRequestPayload()).thenReturn(jsonData.getBytes());

        handler.handlePUT(mockExchange);

        // Verifica que:
        // 1. Se llamó al listener con los datos correctos
        verify(mockListener).handleSensorMessage(any(), any(SensorData.class));
        // 2. Se respondió con CHANGED (2.04)
        verify(mockExchange).respond(eq(ResponseCode.CHANGED));
    }

    @Test
    public void testHandlePUTWithInvalidJson() {
        // Payload malformado
        when(mockExchange.getRequestPayload()).thenReturn("invalid_json".getBytes());

        handler.handlePUT(mockExchange);

        // Debe responder con BAD_REQUEST (4.00)
        verify(mockExchange).respond(eq(ResponseCode.BAD_REQUEST));
    }

    @Test
    public void testHandlePUTWithMissingListener() {
        // Simula que no hay listener configurado
        handler.setDataMessageListener(null);
        String jsonData = "{\"name\": \"TempSensor\", \"value\": 25.5}";
        when(mockExchange.getRequestPayload()).thenReturn(jsonData.getBytes());

        handler.handlePUT(mockExchange);

        // Debe responder con SERVICE_UNAVAILABLE (5.03)
        verify(mockExchange).respond(eq(ResponseCode.SERVICE_UNAVAILABLE));
    }

    @Test
    public void testHandleGET() {
        handler.handleGET(mockExchange);
        
        // GET debe responder con METHOD_NOT_ALLOWED (4.05) o CONTENT (2.05) si implementas caché
        verify(mockExchange).respond(eq(ResponseCode.METHOD_NOT_ALLOWED), anyString());
    }

    @Test
    public void testHandlePOST() {
        handler.handlePOST(mockExchange);
        verify(mockExchange).respond(eq(ResponseCode.METHOD_NOT_ALLOWED), anyString());
    }

    @Test
    public void testHandleDELETE() {
        handler.handleDELETE(mockExchange);
        verify(mockExchange).respond(eq(ResponseCode.METHOD_NOT_ALLOWED), anyString());
    }
}



