package com.kevinlinxp.springcloudapp.reservationservice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.annotation.MessageEndpoint
import org.springframework.integration.annotation.ServiceActivator

@MessageEndpoint
class ReservationMessageProcessor(
        @Autowired val reservationRepository: ReservationRepository
) {

    @ServiceActivator(inputChannel = ReservationChannels.CHANNEL_NAME)
    fun acceptReservation(rn: String) {
        this.reservationRepository.save(Reservation(rn))
    }

}