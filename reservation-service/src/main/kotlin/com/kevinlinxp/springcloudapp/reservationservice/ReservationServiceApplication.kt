package com.kevinlinxp.springcloudapp.reservationservice

import brave.sampler.Sampler
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter
import org.springframework.messaging.SubscribableChannel

@SpringBootApplication
@EnableDiscoveryClient
@EnableBinding(value = [ReservationChannels::class])
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

    @Configuration
    class ExposeIdRepositoryRestConfigurerAdapter : RepositoryRestConfigurerAdapter() {
        override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration?) {
            config?.exposeIdsFor(Reservation::class.java)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationServiceApplication>(*args)
}

interface ReservationChannels {

    companion object {
        const val CHANNEL_NAME = "input"
    }

    @Input(CHANNEL_NAME)
    fun input(): SubscribableChannel
}