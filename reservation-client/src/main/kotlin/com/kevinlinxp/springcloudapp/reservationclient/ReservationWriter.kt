package com.kevinlinxp.springcloudapp.reservationclient

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway

@MessagingGateway
interface ReservationWriter {
    @Gateway(requestChannel = ReservationChannels.CHANNEL_NAME)
    fun write(rn: String)
}

