create database schuldenapp;

use schuldenapp;

create table nutzer(
IDnutzer int Not Null auto_increment,
nutzername varchar(10) not null default '',
passwort varchar(20) not null default '',
primary key (IDnutzer)
);
