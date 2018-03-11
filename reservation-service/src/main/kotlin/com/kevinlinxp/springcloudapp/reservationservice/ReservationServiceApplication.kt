package com.kevinlinxp.springcloudapp.reservationservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
class ReservationServiceApplication {

    @Bean
    internal fun runner(rr: ReservationRepository) = CommandLineRunner {
        "Dr. Rod,Dr. Syer,ALL THE COMMUNITY,Josh,Kevin".split(",")
                .forEach { rr.save(Reservation(it)) }
        rr.findAll().forEach { println(it) }
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args)
}

@RefreshScope
@RestController
internal class MessageRestController {

    @Value("\${message}")
    private var message = ""

    @RequestMapping("/message")
    fun message(): String {
        return this.message
    }

}

@RepositoryRestResource
internal interface ReservationRepository : JpaRepository<Reservation, Long> {

    @RestResource(path = "by-name")
    fun findByReservationName(@Param("rn") rn: String): Collection<Reservation>
}

@Entity
internal data class Reservation constructor(
        var reservationName: String?
) {

    @Id
    @GeneratedValue
    var id: Long? = null

    constructor() : this(null)
}