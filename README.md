# Secure Login Server Using SpringBoot

We were required to make a login server in my security class using Java. I wanted to take it further, so I decided to find a way to self host a server so that the credentials wouldn't be on the client's machine.

With this setup, the server can control any rate limiting that is desired. I did not set up rate limiting, but it would be possible with this setup.

The source code for the server is contained in the `src/main/java/net/exclipsed/secureloginserver/controllers` directory.

The source code for the client is contained in the `client` directory. If the server is still up on my local hardware, this is the only thing you need to download in order to make a login attempt. It will prompt you for a username and a password, separated by a space.

## <a id="my-anchor">security-controls-implemented</a>
+ Hashing
    + Used PBKDF2 as the hashing algorithm in order to slow down brute force attacks if the attacker were to ever gain access to the hashes.
+ Salting
    + Salting is implemented into this algorithm to prevent the use of rainbow tables if the hash were to ever be accessed
    + Likely not necessary for the implemented credentials, but could prove useful if the user wanted to create their own password that could be susceptible to rainbow tables.
+ Constant Time Execution
    + Implemented an algorithm which compares the hashes in constant time, no matter the placement of the letter\(s\) that are off.
+ Branch Prediction Mitigation
    + Modern CPUs use branch prediction to improve execution speed, which will fetch instructions from system memory and place then inside of the CPU cache based on which code it believes will run next. This can be used by threat actors to perform a timing attack.
    + To prevent timing attacks using branch prediction, every character in the stored hash and the computed hash are compared using bitwise operations. This ensures that there is no program branching occurring at all.
+ Input Validation
    + Ensures that the username and password entered are less than 64 and 256 characters long respectively.
    + Without this, DoS attacks would be possible if the user entered a 10MB password for example, which would need to be iterated over 310,000 times.
 
## How does it work?

Client -> Cloudflare DNS -> Nginx Reverse Proxy -> Wireguard VPN Tunnel to Firewall -> Docker Container -> Java Based HTTP Server

+ Client
    + Client will take in a username and password
    + Builds an HTTP GET request using the username and password as parameters
    + Sends the request
    + Wait for response and output message body
+ CloudFlare DNS
    + Resolves the nginx VPS server IP based on the defined A rule and the requested URL
+ Nginx Reverse Proxy
    + Routes the request to the virtual machine that runs all docker containers and the exposed port on that machine for this service
    + Prevents exposure of internal network structure
+ Wireguard VPN Tunnel to Firewall
    + Due to CGNAT limitations, my homelab has no public IPv4 address
    + A wireguard connection between the VPS running nginx and my firewall is used to securely route all traffic to homelab environment
+ Docker Container
    + Packets are routed to the Docker container's open port \(8080 by default\)
+ Java Based HTTP Server
    + SpringBoot was used to create a Java based HTTP server
    + All logic and calculations are performed on this server
        + See [security controls implemented](#security-controls-implemented) for more information
    + Sends a response back to the client indicating whether or not their login attempt was successful
 
## Possible Improvements
+ Nonce Implementation
    + The current version of the program is susceptible to replay attacks.
    + The implementation of nonces using tick or something else would be useful.
+ HTTPS
    + Currently, I do not have SSL certificates set up, so these requests are going over HTTP which means that if I get the right password, that password has been passed over the internet unencrypted, which is bad for obvious reasons.
+ Rate Limiting
    + Currently there is no rate limiting in place for this server other than the speed of the hashing algorithm.
    + Possible rate limiting solutions
        + Account Lockout
        + Exponetial Delay
        + IP Lockout
+ Post Request
    + Current version of the code uses a GET request, which is logged by many systems.
    + Implementation of a POST request and a request body would prevent the username and password from being logged.
+ Postgre Database
    + Use of a Postgre database would remove the requirement for the credentials to be stored in the container of the server.
+ Multi-user Capabilities
    + The server can only currently check for one specific username and password. It would be cool to add the ability to check for multiple users with different usernames and passwords.
