package ru.privetdruk.restorder.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    @Value("${webclient.connection.timeout.ms:1000}")
    private int connectionTimeout;
    @Value("${webclient.read.timeout.ms:10000}")
    private int readTimeout;
    @Value("${webclient.max.in.memory.size.byte:10485760}")
    private int maxInMemorySize;
    @Value("${webclient.wiretap.format:TEXTUAL}")
    private AdvancedByteBufFormat wiretapFormat;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                    configurer.defaultCodecs().maxInMemorySize(maxInMemorySize);
                })
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                        .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)))
                        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, wiretapFormat)
                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
                .build();
    }
}
