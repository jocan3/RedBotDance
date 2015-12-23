#RedBotDance - BPN Neural Network to learn music patterns and make a robot dance!

The code in this repository allows you control a RedBot (Sparkfun robot) using an Android application. This app allows you train and use a Back Propagation Neural Network able to hear sound patterns and move accordingly.


##RedBotDance-Motors
The solution in this folder is intended to be uploaded into the RedBot's chip using the Arduino IDE. It uses the library RedBot.h designed to work with this robot and control the two motors.

```c++
#include <RedBot.h>
RedBotMotors motors;
```

It reads characters from serial port 9600 representing commands received through a bluetooth module. These commands will make the RedBot to move in four different ways: up, down, left and right.

```c++
Serial.begin(9600);
```
```c++
if (Serial.available()) {
    inChar = (char)Serial.read();
    if (inChar == 'u') {
      motors.drive(-127);
      delay(50);
      motors.brake();
    }
    if (inChar == 'd') {
      motors.drive(127);
      delay(50);
      motors.brake();
    }
    if (inChar == 'l') {
      motors.leftDrive(-255);
      delay(50);
      motors.brake();
    }
    if (inChar == 'r') {
      motors.rightDrive(-255);
      delay(50);
      motors.brake();
    }
  }
```

For more information on how to get the hardware and working with Arduino, please see the [Dancing RedBot instructable](http://www.instructables.com/id/Dancing-RedBot/?ALLSTEPS).


##RedBotDance (Android-Processing app)

In the RedBotDance folder you can see the code of the Android application built using Processing IDE and the Android mode. The files include the main program, a Back Propagation Network and a class to manage the BPN (linking the main program and the neural network).

The BPN class implements a Back Propagation Network with the attributes required by the model and a subclass Layer with its corresponding methods. All these values are initialized and the Layers created with random weights in the constructor of the BPN.

```java
  public BPN(double alpha, double eta, int numHiddenLayers, int numInputUnits, int numOutputUnits, int numUnitsHiddenLayer, double tol){
        outputVector=new double [numOutputUnits];
        
        this.error=false;
        this.tolerance=tol;
        this.alpha=alpha;
        this.eta=eta;
        this.numHiddenLayers=numHiddenLayers;
        this.numInputUnits=numInputUnits+1;
        this.numOutputUnits=numOutputUnits;
        this.numUnitsHiddenLayer=numUnitsHiddenLayer+1;
        this.errorsAverage = 0;
        
        layers=new Layer[numHiddenLayers+2];
        layers[0]= inputUnits = new Layer(this.numInputUnits,1);
               
        for(int i=1;i<numHiddenLayers+1;++i){
            layers[i]=new Layer(this.numUnitsHiddenLayer,layers[i-1].getSize());
        }
        
        layers[numHiddenLayers+1]= outputUnits= new Layer(this.numOutputUnits,this.numUnitsHiddenLayer);
        expectedOutput = new double [this.numOutputUnits];
        
        //wights generated randomly each time a BPN is created
        Calendar c = new GregorianCalendar();
        Random random = new Random(c.get(Calendar.SECOND));
        int sign= random.nextInt(2);
        
        double weight = 0;
        //starts from 1 because 1st Layer does not have weights
        for(int k=1;k<numHiddenLayers+2;++k){
            for(int i=0; i<layers[k].getSize();++i){
                for(int j=0; j<layers[k-1].getSize();++j){
                    sign= random.nextInt(2);
                    weight=random.nextDouble();                    
                    weight = 0.3*weight;
                    if(sign==1){
                    weight=-1*weight;
                    }
                    layers[k].getDelta()[i][j]=layers[k].getWeights()[i][j]=weight;
                }
            }
        }
    }
```

The main methods of the BPN are the ones to propagate a Layer (calculating the result of each unit as the sum of all the input links multiplied by the respective weights and to adjust weights when the network is being trained based on a pre-calculated error.

```java
 public void propagateLayer(Layer lower, Layer upper){
        double [] inputs;
        double [] current;
        double [][] connections;
        double sum;
        
        inputs=lower.getOutputs();
        current=upper.getOutputs();
        connections=upper.getWeights();
        
        for(int i=0; i < upper.getSize();++i){
            sum=0;
            for(int j=0; j<lower.getSize();++j){
                sum=sum+ inputs[j] * connections[i][j];
            }
            current[i]=1.0/(1.0 + Math.exp(-sum));
        }
    }
```

```java
    public void adjustWeights(){
        Layer current;
        double [] inputs;
        double [] unites;
        double [][] weights;
        double [][] delta;
        double [] error;
        
        for(int i= 1; i<layers.length;++i){

            current = layers[i];
            unites = layers[i].getOutputs();
            inputs = layers[i-1].getOutputs();
            weights=current.getWeights();
            delta=current.getDelta();
            error=layers[i].getErrors();
            for(int j=0;j<unites.length;++j){

                for(int k=0; k<layers[i-1].getSize();++k){

                    double weightsBefore = weights[j][k]; 
                    weights[j][k]=weights[j][k]+(inputs[k]*eta*error[j]);// + (alpha*delta[j][k]);
                    delta[j][k]= weights[j][k]-weightsBefore;
                }
            }
        }
    }
```





