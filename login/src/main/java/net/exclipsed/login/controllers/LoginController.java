package net.exclipsed.login.controllers;

import java.util.Base64;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.*;

import tools.jackson.core.ObjectReadContext.Base;

@RestController
public class LoginController {
    
    // Y0u w1ll n3v3r f1gur3 m3 0ut h4h4 1m t00 3l1t3 0f 4 h4ck3r (hint)
	// Stored credentials: username -> (salt, hash)
	// The salt is random per-user; the hash is SHA-256(salt || password).

	//private static final String STORED_USERNAME = "NotAHackerBTW";
	//private static final String STORED_HASH_B64 = "291h1jB/C7RzwqvO1gVAIy+iilOZhg7A/rk+Nk5Yc/s=";

    private static final String STORED_USERNAME = "ImAHackerBTW";
    private static final String STORED_SALT_B64 = "44MhG4rW8BFORno0JMYbrw==";
	private static final String STORED_HASH_B64 = "3sPe58KF40NQdN+hES/kUn5hWf10HDNGoRYXdCNppys=";

    @GetMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password)
    {
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
            System.out.println("pass: " + computedHash);
        }
        catch(TypeMismatchException tme)
        {
            System.out.println("Type mismatch");
        }
        catch(Exception e)
        {
            System.out.println("An error occurred");
            e.printStackTrace();
        }

		// constant-time comparison to avoid timing attacks
		if (username.equals(STORED_USERNAME) && constantTimeEquals(computedHash, STORED_HASH_B64)) 
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
        byte[] salt = Base64.getDecoder().decode(STORED_SALT_B64);

        PBEKeySpec spec = new PBEKeySpec(
            //310_000 is for readability, java ignores the underscores
            password.toCharArray(), salt, 310_000, 256
        );

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] passwordHash = skf.generateSecret(spec).getEncoded();

        /*
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt);
		md.update(password.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(md.digest());
        */

        // Puts the password hash into base 64
        return Base64.getEncoder().encodeToString(passwordHash);
	}

	private static boolean constantTimeEquals(String a, String b) 
	{
        if(a.length() != b.length())
        {
            System.out.println("Length mismatch");
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
        System.out.println(diff);
		return diff == 0;
	}


	public static String generateHash(String password) throws Exception 
	{
		return hash(password);
	}
}
