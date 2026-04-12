package net.exclipsed.secureloginserver.controllers;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.*;

@RestController
public class LoginController {
    //stored in the format of username:salt:passwordhash
    private static String storedUsername;
    private static String storedSaltB64;
	private static String storedHashB64;

    @GetMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) throws IOException
    {
        // Input validation to prevent DoS attacks
        if (password.length() > 256 || username.length() > 64) 
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
            computedHash = hash(password);
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
		if (constantTimeEquals(username, storedUsername) && constantTimeEquals(computedHash, storedHashB64)) 
		{
			System.out.println("login Successful");
            return "Login successful!";
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
