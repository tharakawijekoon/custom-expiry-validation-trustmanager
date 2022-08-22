# custom-expiry-validation-trustmanager

The Identity Server depends on the tomcat container to validate the client cert which in turn depends on the JDK default implementation, therefore if you have a certificate in the trust store javax.net.ssl default TrustManagers will trust it regardless of expiry of the certificate.

This custom trust manager implementation validates the certificate expiry of the client certificates and prevents users with expired certificates accessing the server.

## Build
Execute the following command to build the project  in the directory where the pom file is located to build the project.

```
mvn clean install
```

## Deploy
1. After successfully building the project, the resulting jar file can be retrieved from the target directory. Copy the resulting jar to the <IS_HOME>/repository/components/lib/ directory.

2. Add the following configuration to the tomcat connector to use the custom trust manager for the client certificate expiry validation this should be added to the <IS_HOME>/repository/conf/tomcat/catalina-server.xml file.

```
trustManagerClassName="org.wso2.custom.tls.expiry.validation.CustomExpiryValidationTrustManager"
```
The connector configuration should look as follows, after adding the above the property to the connector, 

```
<Connector protocol="org.apache.coyote.http11.Http11NioProtocol"
                   port="9443"
                   bindOnInit="false"
                   sslProtocol="TLS"
                   sslEnabledProtocols="TLSv1,TLSv1.1,TLSv1.2"
                   maxHttpHeaderSize="8192"
                   acceptorThreadCount="2"
                   maxThreads="250"
                   minSpareThreads="50"
                   disableUploadTimeout="false"
                   enableLookups="false"
                   connectionUploadTimeout="120000"
                   maxKeepAliveRequests="200"
                   acceptCount="200"
                   server="WSO2 Carbon Server"
                   clientAuth="want"
                   compression="on"
                   scheme="https"
                   secure="true"
                   SSLEnabled="true"
                   compressionMinSize="2048"
                   noCompressionUserAgents="gozilla, traviata"
                   compressableMimeType="text/html,text/javascript,application/x-javascript,application/javascript,application/xml,text/css,application/xslt+xml,text/xsl,image/gif,image/jpg,image/jpeg"
                   keystoreFile="${carbon.home}/repository/resources/security/wso2carbon.jks"
                   keystorePass="wso2carbon"
                   trustManagerClassName="org.wso2.custom.tls.expiry.validation.CustomExpiryValidationTrustManager"
                   URIEncoding="UTF-8"/>
                   
```
IS-5.9.0 upwards, the <IS_HOME>/repository/conf/deployment.toml file is being used to configure the server hence the following configurations can be added in the deployment.toml file to configure the trustmanager.

```
[transport.https.sslHostConfig.properties]
....
....
trustManagerClassName = "org.wso2.custom.tls.expiry.validation.CustomExpiryValidationTrustManager"
```
4. Restart the server.
