package com.kevinlinxp.springcloudapp.reservationclient

import brave.sampler.Sampler
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.Resources
import org.springframework.http.HttpMethod
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.messaging.MessageChannel
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

/**
 * This is an "edge-service" - a service that talks to the other services.
 * There are a couple of different kinds of edge-services
 * <ul>
 *     <li>Micro-proxy. For example, the "ZuulProxy" used below.</li>
 *     <li>API-gateway {@link ReservationApiGatewayRestController}.</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableZuulProxy // This is our micro-proxy
@EnableFeignClients
//@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL]) // see: https://dzone.com/articles/accessing-a-spring-data-rest-api-with-feign
@EnableBinding(value = [ReservationChannels::class]) // Besides rest-call, messaging is used by our API-gateway to talk with the other micro-services. Spring Cloud Stream is going to see the channel name in the interface. And will create the corresponding channel that connects to the broker.
@IntegrationComponentScan
class ReservationClientApplication {

    @LoadBalanced
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun getSampler(): Sampler {
        return Sampler.ALWAYS_SAMPLE
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationClientApplication>(*args)
}

@RestController
@RequestMapping("/reservations") // This is the client-side (edge-service) mapping
class ReservationApiGateway(
        @Autowired @LoadBalanced private val restTemplate: RestTemplate,
        // @Autowired @Output(Source.OUTPUT) private val messageChannel: MessageChannel, // Replaced by ReservationWriter supported by Spring Cloud Stream
        @Autowired private val reservationService: ReservationReader, // Leverage FeignClients which is higher-level than RestTemplate
        @Autowired private val reservationWriter: ReservationWriter
) {

    companion object {
        const val SERVICE_URL = "http://reservation-service"
    }

    fun getReservationNamesFallback(): List<String> {
        return emptyList()
    }

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    @RequestMapping("/names")
    fun getReservationNames(): List<String> {
        val ptr = object : ParameterizedTypeReference<Resources<Reservation>>() {}
        val responseEntity = this.restTemplate.exchange(
                "$SERVICE_URL/reservations",
                HttpMethod.GET, null, ptr)
        val responseBody = responseEntity.body ?: return emptyList()
        return responseBody.content
                .mapNotNull { it.reservationName }
                .toList()
    }

    @RequestMapping("")
    fun getReservations(): List<Reservation> {
        // Leverage FeignClients so we don't need to use RestTemplate.
        return this.reservationService.getReservations()
                .content.toList()
    }

    @RequestMapping(method = [RequestMethod.POST])
    fun write(@RequestBody r: Reservation) {
        val reservationName = r.reservationName ?: return
        // this.messageChannel.send(MessageBuilder.withPayload(reservationName).build()) // Low-level way. Replaced by ReservationWriter supported by Spring Cloud Stream
        this.reservationWriter.write(reservationName)
    }
}

interface ReservationChannels {

    companion object {
        const val CHANNEL_NAME = "output"
    }

    @Output(CHANNEL_NAME)
    fun output(): MessageChannel
}

