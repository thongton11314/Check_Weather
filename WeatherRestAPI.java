
/**
 * Author: Thong Ton
 * Date: 10/19/2021
 * Description: The application will implement two different APIs to show 
 *              the city's weather and cities near by
 */

// Library needed
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

// External library, not inbuild java library
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * WeatherRestAPI Class
 * @implNote This class implement two different APIs, which first for getting city's weather, second for cities near by
 * @implSpec If getting the first API fail, which getting city, then there is no more data to be used for getting cities near by.
 *           Which mean that not calling second API
 * @see https://openweathermap.org/api
 * @see https://rapidapi.com/wirefreethought/api/geodb-cities/
 */
public class WeatherRestAPI {

    private final static int MAX_RETRY = 3;
    private final static int MAX_TIME_OUT = 3;
    private final static int LONGTIDU = 0;
    private final static int LATIDU = 1;
    private static String CITY;


    /**
     * run the  program
     * @param args city 
     */
    public static void main(String[] args) {
        try {

            // Main process
            CITY = getInput(args);

            double radius = 10; // Max Radius
            int limit = 10;     // Max city near by

            // Convert to JSON OpenWeatherMap
            JSONObject jsonOpenWeatherMap = apiOpenWeatherMap(CITY);

            // Display the weather condition
            if (jsonOpenWeatherMap != null) {
                openweathermapDisplayData(jsonOpenWeatherMap);
            }

            // No more information to get location, end program
            else {
                return;
            }

            // Get longtitude and latitude for city API
            String[] location = getLongtitudeAndLatitude(jsonOpenWeatherMap);

            // Get API city near by response
            JSONObject jsonCityNearBy = apiByCityByLocation(location[LONGTIDU], location[LATIDU], radius, limit);

            // Display the cities near by
            if (jsonCityNearBy != null) {
                cityNearByDisplayData(jsonCityNearBy);
            }

            // No more information to get cities, end program
            else {
                return;
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    /**
     * This function check format string
     * @param args input data
     * @return a city in URL format
     */
    public static String getInput(String[] args) {

        // Early exit, if number or wrong input format
        if (args.length == 0) {
            System.out.println("Error input: Need to provide city");
            System.exit(0);
        }

        if (args.length == 1) {

            // nagative or number
            if (args[0].matches("[-+]?\\d+")) {
                System.out.println("Error input: Need to provide city");
                System.exit(0);
            }

            // Check if space between world
            if (args[0].matches(".*\\s+.*")) {
                args[0] = args[0].replaceAll("\\s+", "%20");
            }
            return args[0].toLowerCase();
        }

        // More than 1 args
        String city = "";
        for(int i = 0; i < args.length; i++){
            if (i == args.length - 1) {
                city += args[i];
            }
            else {
                city += args[i] + "%20";
            }
        }
        return city.toLowerCase();
    }

    /**
     * Get the weather information by getting API
     * @param city
     * @return JSONObject that contain the city's weather data
     * @throws Exception
     * @see https://openweathermap.org/api
     */
    private static JSONObject apiOpenWeatherMap(String city)
            throws Exception {
        String key = "398e27a58a97b09125e90cd9e17ac109";
        String curURI = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s"
                        , city
                        , key);

        // Step 1: Get the URL. Need to have {city} and {api_key} to get access API
        URI uri = new URI(curURI);

        // Step 2: Create a client side
        HttpClient client = HttpClient.newHttpClient();

        // Step 3: Create request, set timeout as well
        HttpRequest request = HttpRequest.newBuilder()
                                .uri(uri)
                                .timeout(Duration.ofSeconds(MAX_TIME_OUT))
                                .build();

        // Step 4: Get the Website response
        HttpResponse<String> response;
        
        // Step 5: Set up back off (retries)
        int count = 0;
        int time = 1;
        do {
            response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();

            // Check first API, status here, handle back off here
            if (code == 429 || code >= 500) {
                setBackOff(time++, "OpenWeather");
            }
            else if (code >= 200 && code < 300) {
                break;
            }
            else {
                throw new Exception(new String("Error city" + ": City not found" + "\n"));
            }
            count++;
        } while (count < MAX_RETRY);

        // If retrired fail
        if (count == MAX_RETRY) {
            System.out.println("Many retried, ran out of limit rate. Terminate accessing OpenWeather API");
            return null;
        }
        return new JSONObject(response.body());
    }

    /**
     * Get the cities near by
     * @param longtitude location's longtitude
     * @param latitude location's latitude
     * @param radius the radius to get the all the cities
     * @param limit the limit of city near by
     * @return JSONObject that contain all the cities near by data
     * @throws Exception
     * @see https://rapidapi.com/wirefreethought/api/geodb-cities/
     */
    private static JSONObject apiByCityByLocation(String longtitude, String latitude, double radius, int limit)
            throws Exception {

        String key = "d88b573580mshcb379e98d934495p182896jsn73bf36296945";

        // Get URI
        String curURI = String.format("https://wft-geo-db.p.rapidapi.com/v1/geo/locations/" 
                                        + "%s%s"
                                        + "/nearbyCities?"
                                        + "radius=%f"
                                        + "&limit=%d",
                        latitude, longtitude, radius, limit);

        // Step 2: Create a client side
        HttpClient client = HttpClient.newHttpClient();

        // Set up request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(curURI))
                .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .header("x-rapidapi-key", key)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(MAX_TIME_OUT))
                .build();

        // Get response
        HttpResponse<String> response;

        // Set up back off (retries)
        int count = 0;
        int time = 1;
        do {
            response = client.send(request, BodyHandlers.ofString());
            int code = response.statusCode();

            // Check first API, status here, handle back off here
            if (code == 429 || code >= 500) {
                setBackOff(time++, "OpenWeather");
            }
            else if (code >= 200 && code < 300) {
                break;
            }
            else {
                throw new Exception(new String("Error " + code + ": City not found" + "\n"));
            }
            count++;
        } while (count < MAX_RETRY);
        return new JSONObject(response.body());
    }

    /**
     * This function run the back off retry
     * @param time time to be exponential
     * @param apiName the web API name
     * @throws InterruptedException
     */
    private static void setBackOff(int time, String apiName) throws InterruptedException {
        System.out.println("Warning: Fail getting data from " + apiName + ". Back off trigger"
                            + ". Retry " + (time * 2) + " second");
        TimeUnit.SECONDS.sleep((time) * 2);
    }

    /**
     * Get the JSONObject specific in weather then display data into screen
     * @param jsonFromAPI weather JSON
     */
    private static void openweathermapDisplayData(JSONObject jsonFromAPI) {
        System.out.println("Weather information");

        // {"sys"}->{""}
        String country = ((JSONObject) jsonFromAPI.get("sys")).get("country").toString();
        System.out.println(" - Country:\t" + country);

        // {"name"}
        String city = jsonFromAPI.get("name").toString();
        System.out.println(" - City:\t" + city);

        // {"weather"}->[{""}]
        String weatherMain = ((JSONObject) ((JSONArray) jsonFromAPI.get("weather")).get(0)).get("main").toString();
        String weatherMainDescription = ((JSONObject) ((JSONArray) jsonFromAPI.get("weather")).get(0))
                .get("description").toString();
        System.out.println(" - Weather:");
        System.out.println("\t- Main:\t\t" + weatherMain);
        System.out.println("\t- Description:\t" + weatherMainDescription);

        // {"main"}->{""}
        String temperature = ((JSONObject) jsonFromAPI.get("main")).get("temp").toString();
        String tempFeelLike = ((JSONObject) jsonFromAPI.get("main")).get("feels_like").toString();
        String tempMin = ((JSONObject) jsonFromAPI.get("main")).get("temp_min").toString();
        String tempMax = ((JSONObject) jsonFromAPI.get("main")).get("temp_max").toString();
        String pressure = ((JSONObject) jsonFromAPI.get("main")).get("pressure").toString();
        String huminity = ((JSONObject) jsonFromAPI.get("main")).get("humidity").toString();

        // Convert Kevlin to F
        // ((k - 273.15) * 1.8) + 32;
        int temperatureVal = (int) (((Double.parseDouble(temperature) - 273.15) * 1.8) + 32);
        int tempFeelLikeVal = (int) (((Double.parseDouble(tempFeelLike) - 273.15) * 1.8) + 32);
        int tempMinVal = (int) (((Double.parseDouble(tempMin) - 273.15) * 1.8) + 32);
        int tempMaxVal = (int) (((Double.parseDouble(tempMax) - 273.15) * 1.8) + 32);
        System.out.println(" - Temperature:\t");
        System.out.println("\t- Main:\t\t" + temperatureVal + " (F)");
        System.out.println("\t- Feel like:\t" + tempFeelLikeVal + " (F)");
        System.out.println("\t- Min:\t\t" + tempMinVal + " (F)");
        System.out.println("\t- Max:\t\t" + tempMaxVal + " (F)");
        System.out.println("\t- Pressure:\t" + pressure);
        System.out.println("\t- Huminity:\t" + huminity);

        // "wind"->{""}
        String speed = ((JSONObject) jsonFromAPI.get("wind")).get("speed").toString();
        String degree = ((JSONObject) jsonFromAPI.get("wind")).get("deg").toString();
        System.out.println(" - Wind:");
        System.out.println("\t- Speed:\t" + speed + (" meter/sec"));
        System.out.println("\t- Degree:\t" + degree);
    }

    /**
     * Get the location's longtitude and latitude by city
     * @param jsonFromAPI weather JSON
     * @return The longtitude and latitude of city
     */
    private static String[] getLongtitudeAndLatitude(JSONObject jsonFromAPI) {

        // "coord"->"lon" or "lat"
        String longtidu = ((JSONObject) jsonFromAPI.get("coord")).get("lon").toString();
        String latidu = ((JSONObject) jsonFromAPI.get("coord")).get("lat").toString();
        char plus = '+';
        char minus = '-';
        if (!(longtidu.charAt(0) == minus) && !(longtidu.charAt(0) == plus)) {
            longtidu = '+' + longtidu;
        }
        if (!(latidu.charAt(0) == minus) && !(latidu.charAt(0) == plus)) {
            latidu = '+' + latidu;
        }
        String[] temp = { longtidu, latidu };
        return temp;
    }

    /**
     * Display all the cities near by the current city
     * @param citiesNearBy list of city near by
     */
    private static void cityNearByDisplayData(JSONObject citiesNearBy) {
        System.out.println("Near by locations");
        JSONArray listOfCities = ((JSONArray) citiesNearBy.get("data"));
        for (int i = 0; i < listOfCities.length(); i++) {
            JSONObject citiesObject = (JSONObject) listOfCities.get(i);
            String cityName = citiesObject.get("city").toString();
            String distance = citiesObject.get("distance").toString();
            if (!cityName.toLowerCase().equals(CITY.toLowerCase())) {
                System.out.println(" - " + cityName);
                System.out.println("\t- Distance:\t" + distance + " (miles)");
            }
        }
    }
}