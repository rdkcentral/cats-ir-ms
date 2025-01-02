# IR Microservice

The IR Microservice provides the ability to make IR commands via HTTP requests for devices under test. To achieve this, the IR Microservice interfaces with programmable IR (Infrared) blasters deployed to the rack. A device under test is directly connected to individual ports on the IR blaster, allowing for commands to enable remote control operations for individual devices under test. Custom slot mapping is supported to ensure the microservice is aware of all devices under test connected to each IR blaster deployed to the rack as well as the individual port assignments for each device.


## Development Setup
Build using ``` mvn clean install ```

Run using ``` java -jar target/ir-ms.jar ```

### Running Locally
```
mvn spring-boot:run
```



## Building
Build the project using mvn clean install. Copy the built jar file into the corresponding directory structure as required by the Dockerfile.
```
docker build -t="/ir-ms" .
```



## Deploying
The ir-ms.yml file is the configuration file, it must contain the RedRatHub host and port number as well as at least one IR hardware device host such as IRNetBoxes or GlobalCache IP2IR (iTach). IR Hardware devices are specified in blocks of one or more devices as shown below:

```
redRatHubHost: 10.21.55.230
redRatHubPort: 40000

irDevices:
  - type: irNetBox
    host: 192.168.100.31
    port: 8080
    maxPorts: 16
  - type: itach
    host: 192.168.100.35
    port: 4998
    maxPorts: 3
```

In this example, the redRatHubHost specifies the IP address of the RedRatHub and the redRatHubPort specifies the port number. There is one IRNetBox device and one ITach connected. The IRNetBox has 16 ports and the ITach has 3 ports. The IRNetBox is connected to the host at 192.168.100.31 and the ITach is connected to the host at 192.168.100.35.


## Custom Slot Mapping
IR-ms offers the capability to customize any slot's device and outlet reference. This allows for flexibility in slot capability for non traditional rack deployments. For instance, say you have a IR device and 16 slots on your rack. If you want device 3 to have IR capability but it is not necessary to map it to slot 2, you could create a slot mapping that allows for this with the following JSON:
```
{
      "slots": {
          "1": "1:1",
          "3": "1:2",
          "4": "1:3",
          "5": "1:4",
          "6": "1:5",
          "7": "1:6",
          "8": "1:7",
          "9": "1:8"
      }   
}
```
This would be stored as mappings.json in the /irms/ms directory by default.



## NGINX Configuration
NGINX is used to support a unified path for communication to the rack microservices as well as communication between the rack microservices. NGINX configuration for ir-ms can be found at [ir-ms.conf](conf/ir-ms.conf). This configuration file is used to route requests to the IR microservice.



## Supported IR Device Hardware
The supported types are listed below:

| Hardware Type  | Hardware Type Identifiers          | Connection Protocol  | Documentation                                                      |
|----------------|------------------------------------|---|--------------------------------------------------------------------|
| RedRat IRNetBox | irnetboxpro3 / redrat3             | Telnet  | [RedRat Docs](https://rrhub.redrat.co.uk/docs)                     |
| Global Cache IP2IR | gc100 / gc100-12 / gc100-6 / itach |  Telnet | [iTach Docs](https://www.globalcache.com/files/docs/API-iTach.pdf) |

For more information, have a look at the type definitions in [IRHardwareEnum.java](src/main/java/com/comcast/cats/ir/IRHardwareEnum.java).


## Access the Swagger Documentation
The Swagger Documentation for the IR Microservice can be accessed at https://localhost:9090/ir/swagger-ui.html when running locally. Default swagger path is /ir/swagger-ui.html.



## IR Health Check
```
GET http://localhost:9090/ir/health 
```
