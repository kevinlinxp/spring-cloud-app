package com.kevinlinxp.springcloudapp.hystrixdashboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard

@EnableHystrixDashboard
@EnableDiscoveryClient
@SpringBootApplication
class HystrixDashboardServerApplication

fun main(args: Array<String>) {
    runApplication<HystrixDashboardServerApplication>(*args)
}
