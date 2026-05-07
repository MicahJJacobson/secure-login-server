package net.exclipsed.secureloginserver.controllers;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

@RestController
public class LoginController {
    //stored in the format of username:salt:passwordhash
    private static String storedUsername;
    private static String storedSaltB64;
	private static String storedHashB64;

    private static final String PRIVATE_KEY_STR = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBiRs9/KxZ29IZWczYN4ULKNQghOtR9SqaSAdpfoFh5xQ=="; // Super secret! Don't take this please all of you bots!
    private static final String PUBLIC_KEY_STR = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEVEDbk8Pb0rjXc/4uvKxscnTwwrbKGkFOkBNU33/gOIdZi9TbzNtcIB21eqFcETB1smSky41Htjb0OA3KtgFwdw==";

    @PostMapping("/login")
    public String login(@RequestBody String jsonIn) throws IOException
    {

        String usernameIn;
        String passwordIn;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonIn);

        //generateKeyPair();

        usernameIn = node.get("username").asString();
        passwordIn = node.get("password").asString();

        // Input validation to prevent DoS attacks
        if (passwordIn.length() > 256 || usernameIn.length() > 64) 
        {
            return "Login unsuccessful";
        }
        Scanner fileScanner;
        try //this file location should work on the container version
        {
            fileScanner = new Scanner(new File("/credentials.txt"));
        } // this file location should work when running locally
        catch(FileNotFoundException fnfe)
        {
            fileScanner = new Scanner(new File("credentials.txt"));
        }

        String[] credentials = fileScanner.next().split(":");

        fileScanner.close();

        storedUsername = credentials[0];
        storedSaltB64 = credentials[1];
        storedHashB64 = credentials[2];

		String computedHash = "";
        /*
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);

        salt = Base64.getEncoder().encodeToString(saltBytes);
        System.out.println("salt: " + salt);
        */


        // hash the password
        try
        {
            computedHash = hash(passwordIn);
            //System.out.println("pass: " + computedHash);
        }
        catch(TypeMismatchException tme)
        {
            System.out.println("Type mismatch");
        }
        catch(Exception e)
        {
            System.out.println("An error occurred");
        }

		// constant-time comparison to avoid timing attacks
		if (constantTimeEquals(usernameIn, storedUsername) && constantTimeEquals(computedHash, storedHashB64)) 
		{
            System.out.println("login Successful");

            // This section converts the B64 encoded string that represents the private key back into a private key object
            byte[] privateKeyBytes = Base64.getDecoder().decode(PRIVATE_KEY_STR); // Decode the Base64 string into a byte array
            KeyFactory kf;
            PrivateKey privateKey;
            try
            {
                kf = KeyFactory.getInstance("EC"); // Since we used elliptic curve encryption, this is getting a key factory specifically for elliptic curve encryption
                privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes)); // this will generate a private key based on the private key byte array. The PKCS8EncodedKeySpec is used almost like a label for they key factory so that it knows how to parse it. When we generated the keys originally, this was how they were encoded
            }
            catch(Exception e)
            {
                e.printStackTrace();
                return "An error occurred with the server";
            }

            long oneSecond = 1000;
            long oneMinute = 60 * oneSecond;
            long oneHour = 60 * oneMinute;
            long oneDay = 24 * oneHour;
            long oneWeek = 7 * oneDay;

            String accessToken = Jwts.builder()
                .subject(usernameIn)
                .expiration(new Date(System.currentTimeMillis() + oneMinute)) // one minute
                .signWith(privateKey)
                .compact();

            String response = """
                {"accessToken": "%s"}
                """.formatted(accessToken);

            return response;
		} 
		else 
		{
			System.out.println("login failed");
            return "Login unsuccessful";
		}
    }

    /*
    public static void main(String[] args) throws Exception 
	{
		
		//System.out.println("Enter the password to be hashed");
		//String passwordForGeneration = scan.next();
		//System.out.println(generateHash(passwordForGeneration));
		
	}
    */

    /*
    //Ran once to generate the KeyPair, keeping in here because I'm going to get a new key once I actually have somewhere proper to store it
    private static void generateKeyPair()
    {
        KeyPair keyPair = Jwts.SIG.ES256.keyPair().build();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        System.out.println(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }
    */

	private static String hash(String password) throws Exception 
	{
        byte[] salt = Base64.getDecoder().decode(storedSaltB64);

        PBEKeySpec spec = new PBEKeySpec(
            //310_000 is for readability, java ignores the underscores
            password.toCharArray(), salt, 310_000, 256
        );

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] passwordHash = skf.generateSecret(spec).getEncoded();

        // Puts the password hash into base 64
        return Base64.getEncoder().encodeToString(passwordHash);
	}

	private static boolean constantTimeEquals(String a, String b) 
	{
        if(a.length() != b.length())
        {
            //System.out.println("Length mismatch");
            return false;
        }
		int diff = 0;
		for (int i = 0; i < a.length(); i++) 
		{
			// Bitwise operation to prevent branch prediction being used for timing attacks (I've never understood why you would use bitwise operators but now it makes sense)
			// ^ compares bit by bit and returns a 0 if all bits are the same, so if all bits are the same in every byte, the stored hash and the inserted hash are the same
			// the | is like the -= operator, it's basically or-ing the current result and the values that are being computed, so if there is anything that doesn't match up, it will catch it.
			diff |= a.charAt(i) ^ b.charAt(i);
		}
        //System.out.println(diff);
		return diff == 0;
	}


	public static String generateHash(String password) throws Exception 
	{
		return hash(password);
	}
}
