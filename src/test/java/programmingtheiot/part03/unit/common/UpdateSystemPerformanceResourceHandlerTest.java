package programmingtheiot.gda.connection.handlers;

import static org.mockito.Mockito.*;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.Before;
import org.junit.Test;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.data.SystemPerformanceData;

public class UpdateSystemPerformanceResourceHandlerTest {
    private UpdateSystemPerformanceResourceHandler handler;
    private CoapExchange mockExchange;
    private IDataMessageListener mockListener;

    @Before
    public void setUp() {
        handler = new UpdateSystemPerformanceResourceHandler("test");
        mockListener = mock(IDataMessageListener.class);
        mockExchange = mock(CoapExchange.class);
        
        handler.setDataMessageListener(mockListener);
    }

    @Test
    public void testHandlePUTWithValidData() {
        // Simula un payload JSON válido
        String jsonData = "{\"cpuUtil\": 50.0, \"memUtil\": 30.0}";
        when(mockExchange.getRequestPayload()).thenReturn(jsonData.getBytes());

        handler.handlePUT(mockExchange);

        // Verifica que se llamó al listener
        verify(mockListener).handleSystemPerformanceMessage(any(), any(SystemPerformanceData.class));
        verify(mockExchange).respond(eq(ResponseCode.CHANGED));
    }

    @Test
    public void testHandlePUTWithInvalidData() {
        // Simula un payload JSON inválido
        when(mockExchange.getRequestPayload()).thenReturn("invalid".getBytes());

        handler.handlePUT(mockExchange);

        verify(mockExchange).respond(eq(ResponseCode.BAD_REQUEST));
    }
}