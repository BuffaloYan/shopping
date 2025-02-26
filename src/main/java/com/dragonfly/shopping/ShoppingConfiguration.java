package com.dragonfly.shopping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class ShoppingConfiguration {
//    @Bean
//    WebClient http3WebClient(WebClient.Builder builder) {
//        HttpClient client =
//                HttpClient.create()
//                        // Configure HTTP/3 protocol
//                        .protocol(HttpProtocol.HTTP3)
//                        // Configure HTTP/3 settings
//                        .http3Settings(spec -> spec.idleTimeout(Duration.ofSeconds(5))
//                                .maxData(10_000_000)
//                                .maxStreamDataBidirectionalLocal(1_000_000));
//
//        return builder.clientConnector(new ReactorClientHttpConnector(client)).build();
//    }

}
