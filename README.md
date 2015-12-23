#RedBotDance - BPN Neural Network to learn music patterns and make a robot dance!

The code in this repository allows you control a RedBot (Sparkfun robot) using an Android application. This app allows you train and use a Back Propagation Neural Network able to hear sound patterns and move accordingly.


##RedBotDance-Motors
The solution in this folder is intended to be uploaded into the RedBot's chip using the Arduino IDE. It uses the library RedBot.h designed to work with this robot and control the two motors.

```c++
#include <RedBot.h>
RedBotMotors motors;
```

It reads characters from serial port 9600 where a bluetooth module is supposed to be connnected

For more information on how to get the hardware and working with Arduino, please see the [Dancing RedBot instructable](http://www.instructables.com/id/Dancing-RedBot/?ALLSTEPS).



