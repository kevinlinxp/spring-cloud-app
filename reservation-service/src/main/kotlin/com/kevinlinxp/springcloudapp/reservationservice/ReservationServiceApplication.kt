package com.kevinlinxp.springcloudapp.reservationservice

import brave.sampler.Sampler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter
import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@EnableBinding(Sink::class)
@SpringBootApplication
@EnableDiscoveryClient
class ReservationServiceApplication {

    @Bean
    fun runner(rr: ReservationRepository) = CommandLineRunner {
        "Dr. Rod,Dr. Syer,ALL THE COMMUNITY,Josh,Kevin".split(",")
                .forEach { rr.save(Reservation(it)) }
        rr.findAll().forEach { println(it) }
    }

    @Bean
    fun getSampler(): Sampler {
        return Sampler.ALWAYS_SAMPLE
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args)
}

@MessageEndpoint
class MessageReservationReceiver(
        @Autowired val reservationRepository: ReservationRepository
) {

    @ServiceActivator(inputChannel = Sink.INPUT)
    fun acceptReservation(rn: String) {
        this.reservationRepository.save(Reservation(rn))
    }

}

@RefreshScope
@RestController
class MessageRestController {

    @Value("\${message}")
    private var message = ""

    @RequestMapping("/message")
    fun message(): String {
        return this.message
    }

}


@Configuration
class ExposeIdRepositoryRestConfigurerAdapter : RepositoryRestConfigurerAdapter() {
    override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration?) {
        config?.exposeIdsFor(Reservation::class.java)
    }
}

@RepositoryRestResource
interface ReservationRepository : JpaRepository<Reservation, Long> {

    @RestResource(path = "by-name")
    fun findByReservationName(@Param("rn") rn: String): Collection<Reservation>
}

@Entity
data class Reservation constructor(
        @Id @GeneratedValue var id: Long?,
        var reservationName: String?
) {
    // This (ugly) secondary constructor is needed by JPA.
    constructor() : this(null, null)

    constructor(reservationName: String) : this(null, reservationName)
}
