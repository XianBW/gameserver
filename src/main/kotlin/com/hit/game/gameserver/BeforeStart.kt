package com.hit.game.gameserver

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.io.File

@Component
@Order(1)
class BeforeStart:ApplicationRunner {

    @Autowired
    private var mJdbcTemplate = JdbcTemplate()

    private val gson = Gson()

    private fun toCorrectFormat(content:String?):String{
        var newChrs = charArrayOf()
        content?.toCharArray()?.forEach {
            if(it == '"'||it == '\'') newChrs += '\\'
            newChrs += it
        }
        return String(newChrs).strip()
    }

    override fun run(args: ApplicationArguments?) {

//        val run = Runtime.getRuntime()
//        run.exec("cmd.exe /k start " + "python craw.py")
//        Thread.sleep(1000*60*60*2)

        mJdbcTemplate.update("delete from send_flag;")

        val filePaths = arrayOf<String>(
                "E:\\gameserver\\src\\main\\resources\\static\\data_2.json",
                "E:\\gameserver\\src\\main\\resources\\static\\data_3.json"
        )
        filePaths.forEach{filePath ->
            val file = File(filePath)

            file.readLines().forEach {
                val game: Game = gson.fromJson(it, Game::class.javaObjectType)

                var hasSame = false
                var gameId = 0
                val gameName = game.name
                mJdbcTemplate.query("select id from game where name = '${toCorrectFormat(gameName)}';") {
                    gameId = it.getInt("id")
                    hasSame = true
                }

                if (!hasSame) {
                    mJdbcTemplate.update("insert into game value(default,'${toCorrectFormat(game.name)}','${toCorrectFormat(game.content)}','${game.image}');")
                    mJdbcTemplate.query("select LAST_INSERT_ID() id;") {
                        gameId = it.getInt("id")
                    }
                }else mJdbcTemplate.update("update game set content = '${toCorrectFormat(game.content)}' where id = $gameId;")

                var hasSamePrice = false
                mJdbcTemplate.query("select * from game_price where game_id = $gameId and price = '${game.price?.replace(",","")}';") {
                    hasSamePrice = true
                }

                if (!hasSamePrice) {
                    mJdbcTemplate.update("update game_price set latest = 0 where game_id = $gameId and site = '${game.site?:"steam"}';")
                    mJdbcTemplate.update("insert into game_price value(default,$gameId,'${game.price?.replace(",","")}','${game.link}','${game.site?:"steam"}',now(),1);")
                }
            }
        }
    }
}