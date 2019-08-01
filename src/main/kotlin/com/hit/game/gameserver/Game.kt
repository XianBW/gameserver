package com.hit.game.gameserver

class Game(Name:String?=null,
           Price:String?=null,
           Prices:Array<String>?=null,
           Content:String?=null,
           Image:String?=null,
           Comments:Array<String>?=null,
           Link:String?=null,
           Site:String?=null
) {
    var name = Name
    var price = Price
    var prices = Prices
    var content = Content
    var image = Image
    var comments = Comments
    var link = Link
    var site = Site
}