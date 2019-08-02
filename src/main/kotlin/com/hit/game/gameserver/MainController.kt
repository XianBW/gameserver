package com.hit.game.gameserver

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import com.hit.game.gameserver.Game as Game

@Component
@Order(2)
@Controller
@RequestMapping("/")
class MainController {

    private fun toCorrectFormat(content:String?):String{
        var newChrs = charArrayOf()
        content?.toCharArray()?.forEach {
            if(it == '"'||it == '\'') newChrs += '\\'
            newChrs += it
        }
        return String(newChrs).strip()
    }

    private fun upperFirstChar(s:String):String{
        var chars = s.toCharArray()
        chars[0] = chars[0].toUpperCase()
        return String(chars)
    }

    @Autowired
    private var mJdbcTemplate = JdbcTemplate()
    private val gson = Gson()

    @ResponseBody
    @GetMapping("/test")
    fun testServer():String{
        return "OK"
    }

    @ResponseBody
    @GetMapping("/gamelist")
    fun returnList():String {
        var games = arrayOf<Game>()
        mJdbcTemplate.query("select name,image,id from game;"){it0 ->
            var minPrice = "Unknown"
            val gameId = it0.getInt("id")
            mJdbcTemplate.query("select price from game_price where game_id = $gameId and latest = 1;"){
                val price = it.getString("price")
                if(minPrice.length>price.length||minPrice.length==price.length&&minPrice>price) minPrice = price
            }
            games += Game(Name = it0.getString("name"),
                        Image = it0.getString("image"),
                        Price = minPrice)
        }
        return gson.toJson(games)
    }

    @ResponseBody
    @PostMapping("/gamelist")
    fun searchList(@RequestBody Name:String?):String {
        if(Name == null) return returnList()
        var games = arrayOf<Game>()
        mJdbcTemplate.query("select name,image,id from game where name like '%${toCorrectFormat(Name)}%' or content like '%${toCorrectFormat(Name)}%';"){it0 ->
            var minPrice = "Unknown"
            val gameId = it0.getInt("id")
            mJdbcTemplate.query("select price from game_price where game_id = $gameId and latest = 1;"){
                val price = it.getString("price")
                if(minPrice.length>price.length||minPrice.length==price.length&&minPrice>price) minPrice = price
            }

            games += Game(Name = it0.getString("name"),
                        Image = it0.getString("image"),
                        Price = minPrice)
        }

        return gson.toJson(games)
    }

    @ResponseBody
    @PostMapping("/game")
    fun returnGame(@RequestBody gameN:Game):String {
        var game = Game()
        mJdbcTemplate.query("select * from game where name = '${toCorrectFormat(gameN.name)}';"){ it0 ->
            val gameId = it0.getInt("id")
            var prices = arrayOf<String>()
            var gameLink = ""
            var minPrice = "Unknown"
            mJdbcTemplate.query("select price,link,site from game_price where game_id = $gameId and latest = 1;"){
                val price = it.getString("price")
                if(minPrice.length>price.length||minPrice.length==price.length&&minPrice>price) {
                    minPrice = price
                    gameLink = it.getString("link")
                }
                prices += "Price of ${upperFirstChar(it.getString("site"))}: ￥$price"
            }
            game = Game(Name = it0.getString("name"),
                        Content = it0.getString("content"),
                        Image = it0.getString("image"),
                        Link = gameLink,
                        Prices = prices)
        }
        return gson.toJson(game)
    }

    @ResponseBody
    @PostMapping("/sale")
    fun returnOnSaleGame(@RequestBody androidId:String):String {
        var has = false
        mJdbcTemplate.query("select flag from send_flag where id = '$androidId';"){ has = true }
        if(!has) mJdbcTemplate.update("insert into send_flag value('$androidId',0);")
        mJdbcTemplate.query("select flag from send_flag where id = '$androidId';"){ has = it.getBoolean("flag") }

        if(has) return ""
        mJdbcTemplate.update("update send_flag set flag = 1 where id = '$androidId';")

        var game = Game()
        mJdbcTemplate.query("select name,image,game.id id from game,game_price where game.id = game_price.game_id and last_modified >= all(select last_modified from game_price);"){it0 ->
            var minPrice = "Unknown"
            val gameId = it0.getInt("id")
            mJdbcTemplate.query("select price from game_price where game_id = $gameId and latest = 1;"){
                val price = it.getString("price")
                if(minPrice.length>price.length||minPrice.length==price.length&&minPrice>price) minPrice = price
            }

            game = Game(Name = it0.getString("name"),
                        Image = it0.getString("image"),
                        Price = minPrice)
        }

        return gson.toJson(game)
    }
}