# Secure Login Server Using SpringBoot

We were required to make a login server in my security class using Java. I wanted to take it further, so I decided to find a way to self host a server so that the credentials and operations wouldn't be performed on the client's machine (and also it sounded really fun).

With this setup, the server can control any rate limiting that is desired. I did not set up rate limiting in my implementation, but it would be very feasible with this setup.

The source code for the server is contained in the `src/main/java/net/exclipsed/secureloginserver/controllers` directory.

The source code for the client is contained in the `client` directory. If the server is still up on my local hardware, this is the only thing you need to download in order to make a login attempt. It will prompt you for a username and a password, separated by a space.

## <a id="security-controls-implemented">Security Controls Implemented</a>
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

![](/images/SecureLoginServer_Request_Pipeline_Flow.drawio.png)

+ Client
    + Client will take in a username and password
    + Builds an HTTP GET request using the username and password as parameters
    + Sends the request
    + Wait for response and output message body
+ CloudFlare DNS
    + Resolves the nginx VPS[^VPS] server IP based on the defined A rule and the requested URL
+ Nginx Reverse Proxy
    + Routes the request to the virtual machine that runs all docker containers and the exposed port on that machine for this service
    + Prevents exposure of internal network structure
+ Wireguard VPN Tunnel to Firewall
    + Due to CGNAT[^CGNAT] limitations, my homelab has no public IPv4 address
    + A wireguard connection between the VPS running nginx and my firewall is used to securely route all traffic to homelab environment
+ Docker Container
    + Packets are routed to the Docker container's open port \(8080 by default\)
+ Java Based HTTP Server
    + SpringBoot was used to create a Java based HTTP server
    + All logic and calculations are performed on this server
        + See the [security controls implemented](#security-controls-implemented) for more information
    + Sends a response back to the client indicating whether or not their login attempt was successful

## How was Docker Set Up?

Surprisingly, docker was the biggest headache in this project. I have worked with docker many times before, however my previous experience was limited to images already on Docker Hub, and I only ever made minor changes to those images.

This project forced me to build Dockerfiles to create images from scratch, which was far more time consuming to learn about than I could have expected.

This project also forced me to learn how to build docker compose files. This didn't take as long as learning how to build a Dockerfile, but it still did take a decent amount of time.

In order to improve refactorability, portability, and allowance for fast redeployment, I had to learn how to set up container deployments based on a Github repository. This led to multiple complications, and was overall a much more confusing process than setting up a regular container, but is also incredibly useful for production-level environments.

In this implementation, when a container is redeployed it will pull the github repository and rebuild the docker image based on the Dockerfile in the repository. After this, it will reference the docker compose file stored in the same repository, which will indicate how the container should be spun up.
 
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

## What I learned
### Software hardening
There are many avenues that can be used to circumvent security measures, so it's important to consider as many as you possibly can

I learned how to implement
+ Hashing
+ Salting
+ Constant Time Execution
+ Branch Prediction Mitigation
+ Input Validation

### HTTP Servers
While I've worked with and hosted HTTP servers in the past, I have only had minimal experience working with the creation of HTTP requests and have had no experience creating an actual server using code

I learned
+ How an HTTP GET request is formatted
+ How to send parameters using an HTTP Request
+ Java http library
+ SpringBoot API in Java
    + Used for building and running HTTP servers using Java
+ Security of GET vs POST requests

### Docker
I have some experience working with Docker previously, but I had never built a docker image from scratch, nor had I ever created a docker compose file

I learned
+ How to write a Dockerfile
+ How to write a Docker Compose file
+ How to set up a docker stack in Portainer[^Portainer] using a Github repository

[^Portainer]: Portainer is a web based GUI for Docker that is hosted using a docker container. This is similar in concept to Docker Desktop for windows, however, the features and implementation are far different.
[^VPS]: Virtual Private Server. This is a virtualized server that is hosted in the cloud that can be fully controlled by the user.
[^CGNAT]: Carrier-Grade NAT. This is where the ISP places a second layer of NAT between your router and the internet, meaning that multiple routers (and therefore different households) are under the same public IPv4 address. In order to be accessed by the open internet, people on CGNAT need to find workarounds, such as VPN tunneling from a VPS.
