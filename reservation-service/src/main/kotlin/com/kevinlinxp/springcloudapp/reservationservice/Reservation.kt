package com.kevinlinxp.springcloudapp.reservationservice

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Reservation constructor(
        @Id @GeneratedValue var id: Long?,
        var reservationName: String?
) {
    // This (ugly) secondary constructor is needed by JPA.
    constructor() : this(null, null)

    constructor(reservationName: String) : this(null, reservationName)
}