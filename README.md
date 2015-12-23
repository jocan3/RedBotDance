#RedBotDance - BPN Neural Network to learn music patterns and control a dancing robot!

The code in this repository allows you to control a RedBot (Sparkfun robot) using an Android application. This app is able to train and use a Back Propagation Neural Network that can hear sound patterns and move accordingly.


##RedBotDance-Motors
The solution in this folder is intended to be uploaded into the RedBot's chip using the Arduino IDE. It uses the library RedBot.h designed to work with this robot and control the two motors.

```c++
#include <RedBot.h>
RedBotMotors motors;
```

It reads characters from serial port 9600 representing commands received through a bluetooth module. These commands will make the RedBot to move in four different ways: up, down, left and right. In order to read the from the serial port it needs to be initialized, checked for availability and read the value into a char.

```c++
Serial.begin(9600);
```

```c++
Serial.available()
```
```c++
inChar = (char)Serial.read();
```


For more information on how to get the hardware and working with Arduino, please see the [Dancing RedBot instructable](http://www.instructables.com/id/Dancing-RedBot/?ALLSTEPS).


##RedBotDance (Android-Processing app)

In the RedBotDance folder you can see the code of the Android application built using Processing IDE and the [Android mode](http://madrid.verkstad.cc/en/course-literature/processing-android-mode/). The files include the main program, a Back Propagation Network and a class to manage the BPN (linking the main program and the neural network).

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

The main methods of the BPN are the ones to propagate a Layer (calculating the result of each unit as the sum of all the input links multiplied by the respective weights and to adjust weights when the network is being trained based on a pre-calculated error. Note that this model of BPN is generic and no problem-specific.

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

The BPNManager handles the BPN specifically for this project in terms of sound inputs, returning one single output and training the network to recognize sound intensity patterns. The move method calls the BPN feed forward process to determine if the sound pattern received can be considered to perform a move and.

```java
    public double move(double [] input){
        neuralNetwork.setInputs(input);
        neuralNetwork.propagateForward();
        double [] output = neuralNetwork.getOutputs();        
        return output[0];
    }
```

The train method forces the BPN to adjust the weights in order to move/not move for the specific pattern depending on the parameter values. it first sets the input pattern, the expected output and call the feed forward method of the BPN. Then it calls the back propagation method to compute the errors from the output layer backwards through all the weights. Finally it calls the adjust weights method.

```java
    public void train(double [] input, boolean result){
                neuralNetwork.setInputs(input);
                double r = result? 0.9 : 0.1;
                double [] output = {r};
                neuralNetwork.setExpectedOutput(output);
                neuralNetwork.propagateForward();
                neuralNetwork.computeOutputError();
                if (neuralNetwork.getError()){
                    neuralNetwork.propagateBackward();
                    neuralNetwork.adjustWeights();
                }
    }
```

The main program is located in the RedBotDance.pde file and uses the Processing methods setup() and draw() to control the execution flow. The graphical part is designed using the [ControlP5 library](http://www.sojamo.de/libraries/controlP5/) with four toggle buttons that activate the different states of the application. This is created in the initUI() function. 

To use the phone hardware capabilities we used the [Ketai Library](http://ketai.org/) to handle the comunication of the program with the bluetoth and the microphone.

```java
    import ketai.net.bluetooth.*;
    import ketai.ui.*;
    import ketai.net.*;
    import ketai.sensors.*;
```

```java
    bt = new KetaiBluetooth(this);
```

```java
    mic = new KetaiAudioInput(this);
```

Each toggle button triggers the respective method to activate some features and deactivate others. So when none of the toggle buttons is active  the manual control can be used to drive the RedBot using directional buttons to set a direction value to send and the mousepressed property.

```java
    if (mousePressed){
        byte[] data = {(byte)valueToSend};
        bt.broadcast(data);
      }
```

If the MIC button is activated the KetaiAudioInput object starts getting values in a data array. These values are plotted in the screen using the line() function.

```java
    for (int i = 0; i < data.length; i++)
      {
        if(i != data.length-1)
          line(baseX+i, baseY+map(data[i], -32768, 32767,displayHeight,0), baseX+i+1, baseY+map(data[i+1], -32768, 32767,displayHeight,0));
      }
```

There is a function to calculate peaks and valleys based on the array received by the audio device. For performance we included global variables to calculate the sum of the peaks and valleys absolute value and create an array of real values in case we need it for the neural network The idea is to go through the data array only one time per cycle given that the draw() fuction loops infinitely during the execution.

```java
 int [] findPeaksAndValleys(short [] array){
    int [] indexes = new int[array.length];
    int numPeaks = 0;
    sumPeaks = 0;
    BPNInput = new double[DEFAULT_NUM_UNITS_INPUT_LAYER];
    inputsStr = "";
    short maxValue = 0;
    
    for (int i = 0; i < array.length; ++i){
      
      if (i < BPNInput.length) BPNInput[i] = (double)Math.abs(array[i])/32767; //normalize to value between 0 and 1
      
      if ((i < array.length-1)&&(i > 0)){
        if ((array[i-1] < array[i] && array[i] > array[i+1]) || (array[i-1] > array[i] && array[i] < array[i+1])){
          sumPeaks += Math.abs(array[i]);
          indexes[numPeaks] = i;
          ++numPeaks;
        } 
      }
    }
    int [] result = new int[numPeaks];
    for (int i = 0; i < result.length; ++i) result[i] = indexes[i];
    return result;
  }
```

The Dance toggle button executes a function to send a random move to the robot if the sum calculated in findPeaksAndValleys() exceeds a predefined intensity threshold.

```java
    if (dance){
        if (data != null){
          findPeaksAndValleys(data);
          info = "Dancing. " +". Thresh: " + INTESITY_THRESHOLD + ". Current: " + sumPeaks;
          
           if (sumPeaks > INTESITY_THRESHOLD){
            byte[] result = {getRandomElement(moves)};
            bt.broadcast(result);
          }
        }
     }
```

If the BPN is activated then the code executes the useBrain logic that simply sends the audio input received to the BPNManager and broadcasts a random command to the RedBot via Bluetooth depending on the output obtained from the neural network and the tolerance defined.

```java
 if (useBrain){
      
      if (data != null){
       
        findPeaksAndValleys(data);
        double output = brainManager.move(BPNInput);
        
        double lowerBound = 0.9-brain.getTolerance();
        double upperBound = 0.9+brain.getTolerance();
        
        boolean dance = (output > lowerBound && output < upperBound); 
        if (dance){
          byte[] result = {getRandomElement(moves)};
          bt.broadcast(result);
        }
        
         info = "Using neural network. Sum peaks: "+sumPeaks+". Value returned: " + output;
      }
  }
```
The training option works similar to the previous one with the difference that the BPNManager train function is called each time the BPN returns a non-expected output: If the peak/valleys sum exceed the intensity threshold and the BPN returns false or if the peak/valleys sum is below the intensity threshold and the BPN returns true.

```java
   if (training){   
         if (data != null){
       
        findPeaksAndValleys(data);
        double output = brainManager.move(BPNInput);
        
        double lowerBound = 0.9-brain.getTolerance();
        double upperBound = 0.9+brain.getTolerance();
        
        boolean dance = (output > lowerBound && output < upperBound); 
        if (dance){
          byte[] result = {getRandomElement(moves)};
          bt.broadcast(result);
          
          if (sumPeaks < INTESITY_THRESHOLD){
            brainManager.train(BPNInput, false);
          }
          
        }else{
          if (sumPeaks > INTESITY_THRESHOLD){
            brainManager.train(BPNInput, true);
          }
          
        }
        
         info = "Training neural network. Sum peaks: "+sumPeaks+". Value returned: " + output;
      }
        
   }
```

You can download the apk of the Android application [here](https://drive.google.com/file/d/0By8PJeonJB8DU0Z0WUgwdDJmdG8/view?usp=sharing).




