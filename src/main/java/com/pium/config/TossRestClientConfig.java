package com.pium.config;

import com.pium.adapter.outbound.auth.toss.TossAuthProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

/**
 * 토스 API 호출용 RestClient 설정
 */
@Configuration
public class TossRestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    @Qualifier("tossRestClient")
    public RestClient tossRestClient(
            RestClient.Builder restClientBuilder,
            TossAuthProperties tossAuthProperties
    ) {
        if (!hasMtlsFiles(tossAuthProperties)) {
            return restClientBuilder.build();
        }

        try {
            SSLContext sslContext = createSslContext(
                    Path.of(tossAuthProperties.mtlsCertPath()),
                    Path.of(tossAuthProperties.mtlsKeyPath())
            );

            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(CONNECT_TIMEOUT)
                    .build();

            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(READ_TIMEOUT);

            return restClientBuilder
                    .requestFactory(requestFactory)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Toss mTLS RestClient", e);
        }
    }

    private boolean hasMtlsFiles(TossAuthProperties tossAuthProperties) {
        return StringUtils.hasText(tossAuthProperties.mtlsCertPath())
                && StringUtils.hasText(tossAuthProperties.mtlsKeyPath())
                && Files.exists(Path.of(tossAuthProperties.mtlsCertPath()))
                && Files.exists(Path.of(tossAuthProperties.mtlsKeyPath()));
    }

    private SSLContext createSslContext(Path certPath, Path keyPath) throws Exception {
        X509Certificate certificate = loadCertificate(certPath);
        PrivateKey privateKey = loadPrivateKey(keyPath);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("toss-client-cert", certificate);
        keyStore.setKeyEntry("toss-client-key", privateKey, new char[0], new X509Certificate[]{certificate});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, new char[0]);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private X509Certificate loadCertificate(Path certPath) throws Exception {
        String content = Files.readString(certPath, StandardCharsets.UTF_8)
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");

        byte[] certificateBytes = Base64.getDecoder().decode(content);
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certificateBytes));
    }

    private PrivateKey loadPrivateKey(Path keyPath) throws Exception {
        String content = Files.readString(keyPath, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        try {
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception ignored) {
            return KeyFactory.getInstance("EC").generatePrivate(keySpec);
        }
    }
}
