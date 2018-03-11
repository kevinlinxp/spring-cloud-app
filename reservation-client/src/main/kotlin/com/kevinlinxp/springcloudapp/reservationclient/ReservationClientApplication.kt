package com.kevinlinxp.springcloudapp.reservationclient

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.Resources
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
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
class ReservationApiGatewayRestController(@Autowired @LoadBalanced val restTemplate: RestTemplate) {

    @RequestMapping("/names")
    fun getReservationNames(): List<String> {

        val ptr = object : ParameterizedTypeReference<Resources<Reservation>>() {}

        val responseEntity = this.restTemplate.exchange(
                "http://reservation-service/reservations",
                HttpMethod.GET, null, ptr)

        val responseBody = responseEntity.body ?: return emptyList()

        return responseBody.content
                .map { it.reservationName }
                .filterNotNull()
                .toList()
    }

}

data class Reservation(var id: Long?, var reservationName: String?) {
    constructor() : this(null, null)
}