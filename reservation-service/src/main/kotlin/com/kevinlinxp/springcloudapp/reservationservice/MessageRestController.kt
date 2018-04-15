package com.kevinlinxp.springcloudapp.reservationservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
