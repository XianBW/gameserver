package com.hit.game.gameserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GameserverApplication

fun main(args: Array<String>) {
    runApplication<GameserverApplication>(*args)
}
