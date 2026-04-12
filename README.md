# Secure Login Server Using SpringBoot

We were required to make a login server in my security class using Java. I wanted to take it further, so I decided to find a way to self host a server so that the credentials wouldn't be on the client's machine.

With this setup, the server can control any rate limiting that is desired. I did not set up rate limiting, but it would be possible with this setup.

The source code for this project is contained in the `/src directory`, inside of multiple nested directories.

## Security Controls Implemented
+ Hashing
 + Used PBKDF2 as the hashing algorithm in order to slow down brute force attacks if the attacker were to ever gain access to the hashes
