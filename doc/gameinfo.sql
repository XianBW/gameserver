create database gameinfo;
use gameinfo

create table game(
    id integer auto_increment,
    name varchar(500),
    content text,
    image varchar(500),
    primary key(id)
);

create table game_price(
    id integer auto_increment,
    game_id integer,
    price varchar(100),
    link varchar(500),
    site varchar(20),
    last_modified datetime,
    primary key(id),
    foreign key(game_id) references game(id) on delete cascade
);
