package programmingtheiot.part03.integration.connection;

import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.CoapServerGateway;

/**
 * Integration tests for CoapServerGateway.
 */
public class CoapServerGatewayTest
{
    // Constants
    private static final Logger _Logger = 
        Logger.getLogger(CoapServerGatewayTest.class.getName());
    private static final int DEFAULT_TIMEOUT = 120000; // 2 minutes in ms
    private static final String COAP_URL = 
        ConfigConst.DEFAULT_COAP_PROTOCOL + "://" + 
        ConfigConst.DEFAULT_HOST + ":" + 
        ConfigConst.DEFAULT_COAP_PORT;
    
    // Member variables
    private CoapServerGateway coapServerGateway;
    private IDataMessageListener dataMsgListener;
    
    @Before
    public void setUp() throws Exception
    {
        this.dataMsgListener = new DefaultDataMessageListener();
        this.coapServerGateway = new CoapServerGateway(this.dataMsgListener);
    }
    
    @After
    public void tearDown() throws Exception
    {
        if (this.coapServerGateway != null) {
            this.coapServerGateway.stopServer();
        }
    }
    
    @Test
    public void testRunSimpleCoapServerGatewayIntegration()
    {
        try {
            // Start the server
            this.coapServerGateway.startServer();
            _Logger.info("CoAP server started. Running for " + 
                         (DEFAULT_TIMEOUT/1000) + " seconds...");
            
            // Create client and discover resources
            CoapClient client = new CoapClient(COAP_URL);
            Set<WebLink> resources = client.discover();
            
            // Log discovered resources
            if (resources != null) {
                _Logger.info("Discovered CoAP resources:");
                for (WebLink resource : resources) {
                    _Logger.info(" --> " + resource.getURI() + 
                                " | Attributes: " + resource.getAttributes());
                }
            }
            
            // Test specific resources
            testResource(client, ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
            testResource(client, ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
            testResource(client, ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
            
            // Keep server running for manual testing
            _Logger.info("Server will run for " + (DEFAULT_TIMEOUT/60000) + 
                        " minutes for manual testing...");
            Thread.sleep(DEFAULT_TIMEOUT);
            
        } catch (Exception e) {
            _Logger.severe("Test failed: " + e.getMessage());
        } finally {
            _Logger.info("Test completed.");
        }
    }
    
    private void testResource(CoapClient client, ResourceNameEnum resource)
    {
        try {
            String resourcePath = COAP_URL + "/" + resource.getResourceName();
            _Logger.info("Testing resource: " + resourcePath);
            
            client.setURI(resourcePath);
            String response = client.get().getResponseText();
            
            _Logger.info("Response from " + resource + ": " + response);
        } catch (Exception e) {
            _Logger.warning("Error testing resource " + resource + ": " + e.getMessage());
        }
    }
}