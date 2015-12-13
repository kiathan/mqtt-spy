/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.spy.security;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.util.io.pem.PemReader;

import pl.baczkowicz.spy.utils.FileUtils;

/**
 * Utility class for handling SSL/TLS connections.
 */
public class SecureSocketUtils
{
	private final static String ALGORITHM = "RSA";
	
	/**
	 * Loads a PEM file from the specified location.
	 * 
	 * @param file Location of the file to load
	 * 
	 * @return Content of the PEM file
	 * 
	 * @throws IOException Thrown when cannot read the file
	 */
    public static byte[] loadPemFile(final String file) throws IOException 
    {
        final PemReader pemReader = new PemReader(new FileReader(file));
        final byte[] content = pemReader.readPemObject().getContent();
        pemReader.close();
        return content;        
    }
    
	/**
	 * Loads a key file from the specified location.
	 * 
	 * @param file Location of the file to load
	 * 
	 * @return Content of the key file
	 * 
	 * @throws IOException Thrown when cannot read the file
	 */
    public static byte[] loadBinaryFile(final String file) throws IOException 
    {
        final FileInputStream inputStream = new FileInputStream(file);
        final byte[] data = new byte[inputStream.available()];
        inputStream.read(data);
        inputStream.close();
        return data;
    }

    /**
     * Loads a private key from the specified location.
     */
    public static PrivateKey loadPrivateKeyFromPemFile(final String keyFile) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException 
    {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadPemFile(keyFile));
        final PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(privateKeySpec);
        return privateKey;
    }

    /**
     * Loads a private key from the specified location.
     */
    public static PrivateKey loadPrivateKeyFromBinaryFile(final String keyFile) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException 
    {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadBinaryFile(keyFile));
        final PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(privateKeySpec);
        return privateKey;
    }
    
    /**
     * Loads an X509 certificate from the given location.
     */
    public static X509Certificate loadX509Certificate(final String certificateFile) throws IOException, CertificateException 
    {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final InputStream inputStream = FileUtils.loadFileByName(certificateFile);
        final X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
        inputStream.close();
        return certificate;
    }
	
    /**
     * Creates a trust manager factory.
     */
	public static TrustManagerFactory getTrustManagerFactory(final String serverCertificateFile) 
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		// Load CA certificate
		final X509Certificate caCertificate = (X509Certificate) loadX509Certificate(serverCertificateFile);
		
		// CA certificate is used to authenticate server
		final KeyStore keyStore = getKeyStoreInstance();
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca-certificate", caCertificate);
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		
		return tmf;
	}
	
    /**
     * Creates a trust manager factory.
     */
	public static TrustManagerFactory getTrustManagerFactory(final String keyStoreFile, final String keyStorePassword) 
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		// Load key store
		final KeyStore keyStore = loadKeystore(keyStoreFile, keyStorePassword);
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		
		return tmf;
	}
	
	/**
	 * Creates a key manager factory using a key store.
	 */
	public static KeyManagerFactory getKeyManagerFactory(final String keyStoreFile, final String keyStorePassword, final String clientKeyPassword) 
			throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException
	{
		// Load key store
		final KeyStore keyStore = loadKeystore(keyStoreFile, keyStorePassword);			
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, clientKeyPassword.toCharArray());
		
		return kmf;
	}
	
	/**
	 * Creates a key manager factory.
	 */
	public static KeyManagerFactory getKeyManagerFactory(
			final String clientCertificateFile, final String clientKeyFile, final String clientKeyPassword, final boolean pemFormat) 
			throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException
	{
		// Load client certificate
		final X509Certificate clientCertificate = loadX509Certificate(clientCertificateFile);			

		// Load private client key
		final PrivateKey privateKey = pemFormat ? loadPrivateKeyFromPemFile(clientKeyFile) : loadPrivateKeyFromBinaryFile(clientKeyFile);

		// Client key and certificate are sent to server
		final KeyStore keyStore = getKeyStoreInstance();
		keyStore.load(null, null);
		keyStore.setCertificateEntry("certificate", clientCertificate);
		keyStore.setKeyEntry("private-key", privateKey, clientKeyPassword.toCharArray(), new Certificate[] { clientCertificate });
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, clientKeyPassword.toCharArray());
		
		return kmf;
	}
	
	/**
	 * Loads a key store from the specified location and using the given password.
	 */
	public static KeyStore loadKeystore(final String keyStoreFile, final String keyStorePassword)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		final FileInputStream inputStream = new FileInputStream(keyStoreFile);
		
		KeyStore keyStore;
		
		try
		{
			// TODO: add support for other key store types
			keyStore = getKeyStoreInstance();
			keyStore.load(inputStream, keyStorePassword.toCharArray());
		}
		finally
		{
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
		
		return keyStore;
	}
		
	public static KeyStore getKeyStoreInstance() throws KeyStoreException
	{
		return KeyStore.getInstance(KeyStore.getDefaultType());
	}
	
	public static KeyStore getKeyStoreInstance(final String type) throws KeyStoreException
	{			
		return KeyStore.getInstance(type);
	}
	
	public static KeyStore getKeyStoreInstance(final String type, final Provider provider) throws KeyStoreException
	{			
		return KeyStore.getInstance(type, provider);
	}
}
