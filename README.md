# Secure Login Server Using SpringBoot

We were required to make a login server in my security class using Java. I wanted to take it further, so I decided to find a way to self host a server so that the credentials wouldn't be on the client's machine.

With this setup, the server can control any rate limiting that is desired. I did not set up rate limiting, but it would be possible with this setup.

The source code for the server is contained in the `src/main/java/net/exclipsed/secureloginserver/controllers` directory.

The source code for the client is container in the `client` directory. If the server is still being hosted, this is the only thing you need to download in order to make a login attempt. It will prompt you for a username and a password, separated by a space.

## Security Controls Implemented
+ Hashing
    + Used PBKDF2 as the hashing algorithm in order to slow down brute force attacks if the attacker were to ever gain access to the hashes
+ Salting
    + Salting is implemented into this algorithm to prevent the use of rainbow tables if the hash were to ever be accessed
    + Likely not necessary for the implemented credentials, but could prove useful if the user wanted to create their own password that could be susceptible to rainbow tables
+ Constant Time Execution
    + Implemented an algorithm which compares the hashes in constant time, no matter the placement of the letter\(s\) that are off
+ Branch Prediction Mitigation
    + Modern CPUs use branch prediction to improve execution speed, which will fetch instructions from system memory and place then inside of the CPU cache based on which code it believes will run next. This can be used by threat actors to perform a timing attack
    + To prevent timing attacks using branch prediction, every character in the stored hash and the computed hash are compared using bitwise operations. This ensures that there is no program branching occurring at all
 
## Possible Improvements
+ Nonce Implementation
    + The current version of the program is susceptible to replay attacks.
    + The implementation of nonces using tick or something else would be useful
+ HTTPS
    + Currently, I do not have SSL certificates set up, so these requests are going over HTTP which means that if I get the right password, that password has been passed over the internet unencrypted, which is bad for obvious reasons.
