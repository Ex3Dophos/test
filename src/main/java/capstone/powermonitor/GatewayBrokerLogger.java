package capstone.powermonitor;

import java.util.Map;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class GatewayBrokerLogger implements ConfigurableComponent, MqttCallback {	
	private static final Logger s_logger = LoggerFactory.getLogger(GatewayBrokerLogger.class);
	
	// Cloud Application identifier
	private static final String APP_ID = "GatewayBrokerLogger";

	// Publishing Property Names
	private static final String   MQTT_TOPIC_PROP_NAME   = "logging.mqttTopic";
		
	private MqttClient 					mqttClient;
	
	private Map<String, Object>         m_properties;
	private String 						broker;
	
	
	// ----------------------------------------------------------------
	//
	//   Dependencies
	//
	// ----------------------------------------------------------------
	
	public GatewayBrokerLogger() 
	{
		super();
	}
		
	// ----------------------------------------------------------------
	//
	//   Activation APIs
	//
	// ----------------------------------------------------------------

	protected void activate(ComponentContext componentContext, Map<String,Object> properties) 
	{
		s_logger.info("Activating " + APP_ID + "...");
		
		m_properties = properties;
		for (String s : properties.keySet()) {
			s_logger.info("Activate - "+s+": "+properties.get(s));
		}
		
		String topic = (String) m_properties.get(MQTT_TOPIC_PROP_NAME);
		broker = "tcp://127.0.0.1:1883";
		
		// get the mqtt client for this application
		s_logger.info("Connecting MqttClient for {}...", APP_ID);
		try {
	        mqttClient = new MqttClient(broker, APP_ID);
	        mqttClient.connect();
	        mqttClient.setCallback(this);
	        
			s_logger.info("subscribe mqtt client to: " + topic);
	        mqttClient.subscribe(topic);
	    } catch (MqttException e) {
	        e.printStackTrace();
	    }
		s_logger.info("Activating " + APP_ID + " ... Done.");
	}
	
	
	protected void deactivate(ComponentContext componentContext) 
	{
		s_logger.debug("Deactivating " + APP_ID + "...");
		
		// Releasing the CloudApplicationClient
		s_logger.info("Releasing MqttClient for {}...", APP_ID);
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}

		s_logger.debug("Deactivating " + APP_ID + "... Done.");
	}	
	
	
	public void updated(Map<String,Object> properties)
	{
		s_logger.info("Updated " + APP_ID + "...");

		try {
			s_logger.info("Unsubscribe mqtt client ");
			mqttClient.unsubscribe("#");
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		// store the properties received
		m_properties = properties;
		for (String s : properties.keySet()) {
			s_logger.info("Update - "+s+": "+properties.get(s));
		}
		
		String topic = (String) m_properties.get(MQTT_TOPIC_PROP_NAME);

		try {
			s_logger.info("subscribe mqtt client to: " + topic);
			mqttClient.subscribe(topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
		
		
		s_logger.info("Updated " + APP_ID + "... Done.");
	}
	
	
	
	// ----------------------------------------------------------------
	//
	//   MQTT Paho Application Callback Methods
	//
	// ----------------------------------------------------------------
	
	@Override
	public void connectionLost(Throwable cause) {}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		s_logger.info("Recieved MQTT -- Topic: "+ topic +" Message: " + message);   
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {}
	
	
	// ----------------------------------------------------------------
	//
	//   Private Methods
	//
	// ----------------------------------------------------------------
	
}