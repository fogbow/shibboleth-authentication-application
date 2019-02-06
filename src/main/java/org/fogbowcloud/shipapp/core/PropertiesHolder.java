package org.fogbowcloud.shipapp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.PropertyException;

public class PropertiesHolder {

	protected static final int DEFAULT_HTTP_PORT = 8000;	
	
	public static final String FOGBOW_GUI_URL_CONF = "fogbow_gui_url";
	public static final String AS_PUBLIC_KEY_PATH_CONF = "as_public_key_path";
	public static final String SHIP_PRIVATE_KEY_PATH_CONF = "ship_private_key_path";
	private static final String SHIB_HTTP_PORT_CONF = "shib_http_port";
	public static final String SERVICE_PROVIDER_MACHINE_IP_CONF = "service_provider_machine_ip";
	
	public static Properties properties;
	
	public static void init(String propertiePath) throws IOException, PropertyException {
		properties = new Properties();
		File file = new File(propertiePath);
		FileInputStream fileInputStream = new FileInputStream(file);
		properties.load(fileInputStream);
		
		checkProperties(properties);
	}
	
	// TODO remove this. This is used only for tests because we had problem with Power Mockito
	public static void setProperties(Properties properties) {
		PropertiesHolder.properties = properties;
	}

	protected static void checkProperties(Properties properties) throws PropertyException {
		if (getShibPrivateKey() == null || getShibPrivateKey().isEmpty()) {
			throw new PropertyException("Ship App private key not especified in the properties.");
		}
		
		if (getAsPublicKey() == null || getAsPublicKey().isEmpty()) {
			throw new PropertyException("AS public key not especified in the properties.");
		}
		
		if (getDashboardUrl() == null || getDashboardUrl().isEmpty()) {
			throw new PropertyException("Dashboard AS url not especified in the properties.");
		}
		
		if (getShibIp() == null || getShibIp().isEmpty()) {
			throw new PropertyException("Shib machine ip not especified in the properties.");
		}
	}

	public static int getShipHttpPort() {
		String httpPortStr = properties.getProperty(SHIB_HTTP_PORT_CONF);
		int port = httpPortStr == null ? DEFAULT_HTTP_PORT : Integer.parseInt(httpPortStr);
		return port;
	}
	
	public static String getDashboardUrl() {
		return properties.getProperty(FOGBOW_GUI_URL_CONF);
	}
	
	public static String getAsPublicKey() {
		return properties.getProperty(AS_PUBLIC_KEY_PATH_CONF);
	}
	
	public static String getShibPrivateKey() {
		return properties.getProperty(SHIP_PRIVATE_KEY_PATH_CONF);
	}
	
	public static String getShibIp() {
		return properties.getProperty(SERVICE_PROVIDER_MACHINE_IP_CONF);
	}	
	
}
