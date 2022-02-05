
city=1
printf "Case %d : Test number (negative)\n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

city=''
printf "Case %d : Test no arg (negative)\n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI
printf "\n"

city=This_City_Never_Exist_In_Real_World
printf "Case %d : Test no such city\n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

city=SeAtTlE
printf "Case %d : Test city case sensitive \n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

city=Seattle
printf "Case %d : Test one arg without \" \" \n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

city="Saigon"
printf "Case %d : Test one arg within \" \" \n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

city="Truth or Consequences"
printf "Case %d : Test multiple words in one arg \n" "$((count++))"
printf "City name: %s\n" "$city"
java WeatherRestAPI $city
printf "\n"

cityToken1=Truth
cityToken2=or
cityToken3=Consequences
printf "Case %d : Test multiple words in multiple args \n" "$((count++))"
printf "City name: %s %s %s\n" "$cityToken1" "$cityToken2" "$cityToken3" 
java WeatherRestAPI $cityToken1 $cityToken2 $cityToken3 
printf "\n"
