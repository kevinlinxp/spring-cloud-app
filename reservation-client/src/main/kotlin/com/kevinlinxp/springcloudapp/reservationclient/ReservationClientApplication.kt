package com.kevinlinxp.springcloudapp.reservationclient

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.Resources
import org.springframework.http.HttpMethod
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableDiscoveryClient
/**
 * This is an "edge-service" - a service that talks to the other services.
 * There are a couple of different kinds of edge-services
 * <ul>
 *     <li>Micro-proxy. For example, the "ZuulProxy" used below.</li>
 *     <li>API-gateway {@link ReservationApiGatewayRestController}</li>
 * </ul>
 */
@EnableZuulProxy
@EnableBinding(Source::class)
class ReservationClientApplication {

    @LoadBalanced
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationClientApplication>(*args)
}

@RestController
@RequestMapping("/reservations") // This is the client-side (edge-service) mapping
class ReservationApiGatewayRestController(
        @Autowired @LoadBalanced val restTemplate: RestTemplate,
        @Autowired @Output(Source.OUTPUT) val messageChannel: MessageChannel
) {

    @RequestMapping("/names")
    fun getReservationNames(): List<String> {

        val ptr = object : ParameterizedTypeReference<Resources<Reservation>>() {}

        val responseEntity = this.restTemplate.exchange(
                "http://reservation-service/reservations",
                HttpMethod.GET, null, ptr)

        val responseBody = responseEntity.body ?: return emptyList()

        return responseBody.content
                .mapNotNull { it.reservationName }
                .toList()
    }


    @RequestMapping(method = [RequestMethod.POST])
    fun write(@RequestBody r: Reservation) {
        val reservationName = r.reservationName ?: return
        this.messageChannel.send(MessageBuilder.withPayload(reservationName).build())
    }

}

data class Reservation(var id: Long?, var reservationName: String?) {
    constructor() : this(null, null)
}