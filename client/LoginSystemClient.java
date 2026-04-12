import java.util.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import java.net.*;

public class LoginSystemClient
{
    // Y0u w1ll n3v3r f1gur3 m3 0ut h4h4 1m t00 3l1t3 0f 4 h4ck3r (hint)
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("Please enter a username and password");

        String inputUsername = scan.next();
        String inputPassword = scan.next();

        // Creates a new http client
        HttpClient httpClient = HttpClient.newHttpClient();
        // Builds an http request using the inputted username and password 
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("http://secureloginserver.exclipsed.net/login?username=" + inputUsername + "&password=" + inputPassword))
            .build();
        // Sends the http request and stores the http response given by the server
        HttpResponse<String> httpResponse  = httpClient.send(httpRequest, BodyHandlers.ofString());
        // Prints the status code given by the application, this will indicate what happened with the request
        // 200-level request was successful
        // 400-level something was wrong with the request, such as incorrect syntax
        // 500-level something was wrong with the server. If you get this type of status code, you can re-run the exact same request to try again
        System.out.println("Status Code: " + httpResponse.statusCode());
        System.out.println("Response Headers: " + httpResponse.headers());
        // This prints whether or not the 
        System.out.println("Response Body: " + httpResponse.body());

        scan.close();
    }
}
