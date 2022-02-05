#!/bin/sh

javac WeatherRestAPI.java
let count=1

printf "Case %d : Test number (negative)\n" "$((count++))"
java WeatherRestAPI -1
printf "\n"

printf "Case %d : Test number (negative)\n" "$((count++))"
java WeatherRestAPI 1
printf "\n"

printf "Case %d: Test Invalid Input (spacing)\n" "$((count++))"
java WeatherRestAPI Sai Gon
printf "\n"

printf "Case %d : Test no arg (negative)\n" "$((count++))"
java WeatherRestAPI
printf "\n"