package com.kevinlinxp.springcloudapp.reservationclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.hateoas.Resources
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping

@Service
@FeignClient("reservation-service")
interface ReservationReader {

    @RequestMapping("/reservations")
    fun getReservations(): Resources<Reservation>
}