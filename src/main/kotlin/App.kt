package org.example.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["infrastructure.adapter.persistence.jpa.repository"])
@EntityScan(basePackages = ["infrastructure.adapter.persistence.jpa.entity"])
@ComponentScan(basePackages = [
    "infrastructure",
    "application",
    "infrastructure",
    "shared",
    "org.example.example",
    "domain"
])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
