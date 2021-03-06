package cloud.fogbow.shibapp.core;

import java.net.URISyntaxException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.shibapp.constants.Messages;
import cloud.fogbow.shibapp.core.models.ShibToken;
import cloud.fogbow.shibapp.core.saml.SAMLAssertionHolder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

public class ShibController {

	private static final Logger LOGGER = Logger.getLogger(ShibController.class);
	
	protected static final String UTF_8 = "UTF-8";
	
	protected static final String KEY_URL_PARAMETER = "key";
	protected static final String KEY_SIGNATURE_URL_PARAMETER = "keySignature";
	protected static final String TOKEN_URL_PARAMETER = "token";
	
	protected static final String DEFAULT_DOMAIN_ASSERTION_URL = "localhost";
	public static final String SHIB_AS_TOKEN_STRING_SEPARATOR = "!#!";

	public String createToken(String assertionUrl) throws Exception {
		String assertionResponse = getAssertionResponse(assertionUrl);		
		Map<String, String> assertionAttrs = getAssertionAttr(assertionResponse);		
		String eduPersonPrincipalName = SAMLAssertionHolder.getEduPersonPrincipalName(assertionAttrs);
		String commonName = SAMLAssertionHolder.getCommonName(assertionAttrs);		
		String secret = createSecret();			
		String assertionUrlNormalized = normalizeAssertionUrl(assertionUrl);		
		
		ShibToken shibToken = new ShibToken(secret, assertionUrlNormalized, eduPersonPrincipalName, commonName, assertionAttrs);
		return shibToken.generateTokenStr();
	}

	// TODO understand better about this assertion url because this one will be used by AS
	// TODO adding shib ip when the assertion url is a localhost domain
	protected String normalizeAssertionUrl(String assertionUrl) {
		String shibIp = PropertiesHolder.getShibIp();
		String assertionUrlNormalized = assertionUrl.replace(DEFAULT_DOMAIN_ASSERTION_URL, shibIp);
		return assertionUrlNormalized;
	}

	protected Map<String, String> getAssertionAttr(String assertionResponse) throws Exception {
		if (assertionResponse == null) {
			String errorMsg = Messages.Error.NULL_RESPONSE;
			LOGGER.error(errorMsg);
			throw new Exception(errorMsg);
		}
		
		Map<String, String> assertionAttrs = SAMLAssertionHolder.getAssertionAttrs(assertionResponse);
		return assertionAttrs;
	}

	protected String getAssertionResponse(String assertionUrl) throws Exception {
		String assertionResponse = null;
		try {
			assertionResponse = SAMLAssertionHolder.getAssertionResponse(assertionUrl);
		} catch (Exception e) {
			String errorMsg = Messages.Error.UNABLE_TO_GET_ASSERTIONS;
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
		return assertionResponse;
	}
	
	// The secret is also the creation time
	protected String createSecret() throws Exception {		
		return String.valueOf(System.currentTimeMillis());
	}
	
	public String encrypRSAKey(String key) throws Exception {
		try {
			String asPublicKeyPath = PropertiesHolder.getAsPublicKey();
			RSAPublicKey publicKey = getPublicKey(asPublicKeyPath);
			return CryptoUtil.encrypt(key, publicKey);
		} catch (Exception e) {
			String errorMsg = Messages.Error.UNABLE_TO_ENCRYPT_MESSAGE_WITH_RSA;
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}		
	}
	
	public String encrypAESAsToken(String asToken, String aesKey) throws Exception {
		try {
			return CryptoUtil.encryptAES(aesKey.getBytes(UTF_8), asToken);
		} catch (Exception e) {
			String errorMsg = Messages.Error.UNABLE_TO_ENCRYPT_MESSAGE_WITH_AES;
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}
	}
	
	public String createAESkey() {
		return CryptoUtil.generateAESKey();
	}
	
	public String signKey(String key) throws Exception {
		try {
			String shibPrivateKeyPath = PropertiesHolder.getShibPrivateKey();
			RSAPrivateKey privateKey = getPrivateKey(shibPrivateKeyPath);
			return CryptoUtil.sign(privateKey, key);
		} catch (Exception e) {
			String errorMsg = Messages.Error.UNABLE_TO_SIGN_MESSAGE;
			LOGGER.error(errorMsg, e);
			throw new Exception(errorMsg);
		}		
	}	

	public String createTargetUrl(String asTokenEncrypted, String keyEncrypted, String keySigned) throws URISyntaxException {
		String urlDashboard = PropertiesHolder.getDashboardUrl();
		URIBuilder uriBuilder = new URIBuilder(urlDashboard);
		uriBuilder.addParameter(TOKEN_URL_PARAMETER, asTokenEncrypted);
		uriBuilder.addParameter(KEY_URL_PARAMETER, keyEncrypted);
		uriBuilder.addParameter(KEY_SIGNATURE_URL_PARAMETER, keySigned);
		return uriBuilder.toString();
	}

	protected RSAPublicKey getPublicKey(String publicKeyPath) {
		try {
			return CryptoUtil.getPublicKey(publicKeyPath.trim());
		} catch (Exception e) {
			LOGGER.error(Messages.Error.UNABLE_TO_GET_PUBLIC_KEY, e);
		}
		return null;
	}

	protected RSAPrivateKey getPrivateKey(String privateKeyPath) {	
		try {
			return CryptoUtil.getPrivateKey(privateKeyPath.trim());
		} catch (Exception e) {
			LOGGER.error(Messages.Error.UNABLE_TO_GET_PRIVATE_KEY, e);
		}
		return null;
	}
	
}
