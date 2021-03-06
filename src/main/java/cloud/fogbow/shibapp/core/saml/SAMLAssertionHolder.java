package cloud.fogbow.shibapp.core.saml;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cloud.fogbow.shibapp.constants.Messages;
import cloud.fogbow.shibapp.utils.HttpClientWrapper;
import cloud.fogbow.shibapp.utils.HttpResponseWrapper;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.restlet.engine.header.Header;
import org.restlet.util.Series;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class SAMLAssertionHolder {

	private static final Logger LOGGER = Logger.getLogger(SAMLAssertionHolder.class);
	
	public static final String SHIB_ASSERTION_HEADER = "Shib-assertion";
	
	public static final String EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE = "eduPersonPrincipalName";
	public static final String SN_ASSERTION_ATTRIBUTES = "sn";
	public static final String CN_ASSERTION_ATTRIBUTES = "cn";
	public static final String IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES = "identityProvider";
	
	public static void init() throws ConfigurationException {
		DefaultBootstrap.bootstrap();
	}
	
	protected static String getAssertionResponse(String assertionURL, HttpClientWrapper httpClientWrapper) throws Exception {
		HttpResponseWrapper responseWrapper = httpClientWrapper.doGet(assertionURL);
		
		int statusCode = responseWrapper.getStatusLine().getStatusCode();
		String content = responseWrapper.getContent();
		if (statusCode != HttpStatus.SC_OK) {
			String errorMsg = String.format(Messages.Error.UNEXPECTED_STATUS_CODE_D_S, statusCode, content);
			LOGGER.error(errorMsg);
			throw new Exception(errorMsg);
		}
		
		return content;		
	}
	
	public static String getAssertionResponse(String assertionURL) throws Exception {
		return getAssertionResponse(assertionURL, new HttpClientWrapper());
	}
	
	public static Map<String, String> getAssertionAttrs(String assertionResponse) throws Exception {		
		StringReader stringReader = new StringReader(assertionResponse);
		Document document = createDocumentBuilder().parse(new InputSource(stringReader));
		
        Element assertionEl = document.getDocumentElement();
        
        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(assertionEl);
        Assertion assertion = (Assertion) unmarshaller.unmarshall(assertionEl);
		
        Map<String, String> tokenAttrs = new HashMap<String, String>();      
        
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        for (AttributeStatement attributeStatement : attributeStatements) {
        	List<Attribute> attributes = attributeStatement.getAttributes();
        	for (Attribute attribute : attributes) {
				List<XMLObject> attributeValues = attribute.getAttributeValues();
				if (attributeValues.isEmpty()) {
					continue;
				}
				XMLObject attributeValue = attributeValues.iterator().next();
				String value = attributeValue.getDOM().getTextContent().trim();
				tokenAttrs.put(attribute.getFriendlyName(), value);
			}
		}
		
        try {
        	tokenAttrs.put(IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES, getIssuer(assertion));			
		} catch (Exception e) {
			LOGGER.warn(Messages.Warn.UNABLE_TO_RETRIEVE_ISSUER, e);
		}
        
		return tokenAttrs;
	}
	
	protected static String getIssuer(Assertion assertion) throws Exception {		
        Issuer issuer = assertion.getIssuer();
        if (issuer == null) {
			String errorMsg = Messages.Error.INEXISTENT_ISSUER;
			LOGGER.error(errorMsg);
			throw new Exception(errorMsg);
        }
		return issuer.getDOM().getTextContent().trim();
	}
	
	public static String getEduPersonPrincipalName(Map<String, String> assertionAttrs) {
		return assertionAttrs.get(EDU_PERSON_PRINCIPAL_NAME_ASSERTION_ATTRIBUTE); 
	}
	
	public static String getCommonName(Map<String, String> assertionAttrs) {		
		return assertionAttrs.get(CN_ASSERTION_ATTRIBUTES); 
	}	
	
	public String getAssertionUrl(Series<Header> headers) {		
		return headers.getFirstValue(SHIB_ASSERTION_HEADER);
	}

	private static DocumentBuilder createDocumentBuilder() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		return docBuilder;
	}

	public static boolean isPriorityAssertionAttribute(String key) {
		List<String> maisAssertionAttribute = Arrays.asList(new String[] {
				SN_ASSERTION_ATTRIBUTES,
				IDENTITY_PROVIDER_ASSERTION_ATTRIBUTES});
		
		return maisAssertionAttribute.contains(key);
	}
	
}
