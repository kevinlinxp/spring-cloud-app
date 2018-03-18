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
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.messaging.Source
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.Resources
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.http.HttpMethod
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
/**
 * This is an "edge-service" - a service that talks to the other services.
 * There are a couple of different kinds of edge-services
 * <ul>
 *     <li>Micro-proxy. For example, the "ZuulProxy" used below.</li>
 *     <li>API-gateway {@link ReservationApiGatewayRestController}.</li>
 * </ul>
 */
@EnableZuulProxy // This is our micro-proxy
@EnableBinding(Source::class) // Besides rest-call, messaging is used by our API-gateway to talk with the other micro-services.
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
class ReservationApiGatewayRestController(
        @Autowired @LoadBalanced private val restTemplate: RestTemplate,
        @Autowired @Output(Source.OUTPUT) private val messageChannel: MessageChannel,
        @Autowired private val reservationService: ReservationService
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

    @RequestMapping(method = [RequestMethod.POST])
    fun write(@RequestBody r: Reservation) {
        val reservationName = r.reservationName ?: return
        this.messageChannel.send(MessageBuilder.withPayload(reservationName).build())
    }

    /*
     * Leverage FeignClients so we don't need to use RestTemplate.
     */
    @RequestMapping("")
    fun getReservations(): List<Reservation> {
        return this.reservationService.getReservations()
    }
}

@Service
@EnableFeignClients
// see: https://dzone.com/articles/accessing-a-spring-data-rest-api-with-feign
@EnableHypermediaSupport(type = [EnableHypermediaSupport.HypermediaType.HAL])
class ReservationService(
        @Autowired private val nameFeignClients: ReservationFeignClient
) {

    fun getReservations(): List<Reservation> {
        return nameFeignClients.getReservations().content.toList()
    }

    @FeignClient("reservation-service")
    interface ReservationFeignClient {

        @RequestMapping("/reservations")
        fun getReservations(): Resources<Reservation>
    }
}

data class Reservation(var id: Long?, var reservationName: String?) {
    // This (ugly) secondary constructor is needed by Jackson.
    constructor() : this(null, null)
}