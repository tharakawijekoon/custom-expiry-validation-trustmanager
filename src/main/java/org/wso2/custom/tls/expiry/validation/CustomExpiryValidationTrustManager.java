package org.wso2.custom.tls.expiry.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;


public class CustomExpiryValidationTrustManager implements X509TrustManager {
    private static Log log = LogFactory.getLog(CustomExpiryValidationTrustManager.class);

    private X509TrustManager trustManager;
    private static CustomExpiryValidationTrustManager instance;

    //Configuration Options
    private static final ServerConfiguration config = ServerConfiguration.getInstance();
    private static final String TRUST_STORE_LOCATION = config.getFirstProperty("Security.TrustStore.Location");
    private static final String TRUST_STORE_TYPE = config.getFirstProperty("Security.TrustStore.Type");

    public CustomExpiryValidationTrustManager() throws Exception {

        setupTrustManager();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        trustManager.checkClientTrusted(x509Certificates, s);

        for (X509Certificate cert : x509Certificates) {

            try {
                cert.checkValidity();
            } catch (CertificateExpiredException e) {
                log.error( "CertificateExpiredException :" + e.getMessage());
                throw e;
            } catch (CertificateNotYetValidException exp) {
                log.error ("CertificateNotYetValidException :" + exp.getMessage());
                throw exp;
            }
        }
    }

    /**
     * Checks the validity of passed x509Certificate certificate chain
     *
     * @param x509Certificates
     * @param s
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            trustManager.checkServerTrusted(x509Certificates, s);

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        return trustManager.getAcceptedIssuers();
    }


    /**
     * This method loads the TrustManager by reading the carbon server's default trust store file
     *
     * @throws Exception
     */
    private void setupTrustManager() throws Exception {

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore clientTrustStore = null;
        try (InputStream trustStoreInputStream = new FileInputStream(TRUST_STORE_LOCATION)) {

            clientTrustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
            clientTrustStore.load(trustStoreInputStream, null);
            trustManagerFactory.init(clientTrustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            for (TrustManager t : trustManagers) {
                if (t instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) t;
                    return;
                }
            }
            throw new IdentityException("No X509TrustManager in TrustManagerFactory");
        }
    }
}
