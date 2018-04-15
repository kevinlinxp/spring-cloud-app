package com.kevinlinxp.springcloudapp.reservationclient

data class Reservation(var id: Long?, var reservationName: String?) {
    // This (ugly) secondary constructor is needed by Jackson.
    constructor() : this(null, null)
}