package processing.test.redbotdance;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import android.content.Intent; 
import android.os.Bundle; 
import ketai.net.bluetooth.*; 
import ketai.ui.*; 
import ketai.net.*; 
import ketai.sensors.*; 
import java.util.Random; 
import java.util.Calendar; 
import java.util.GregorianCalendar; 
import java.io.File; 
import java.util.StringTokenizer; 
import java.util.ArrayList; 
import java.lang.Thread; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class RedBotDance extends PApplet {

/* A simple example to controll RedBot using an Android device as Remote control 
 @author: Jose Andres Mena
 @author: Oscar Rodriguez
 */

//required for BT enabling on startup








final int DEFAULT_NUM_UNITS_INPUT_LAYER = 1024;
final double DEFAULT_LEARNING_FACTOR = 0.5f;
final int DEFAULT_NUM_HIDDEN_LAYERS = 1;
final int DEFAULT_NUM_UNITS_HIDDEN_LAYERS = 512;
final int DEFAULT_UNITS_OUTPUT_LAYER = 1;
final double DEFAULT_TOLERANCE = 0.3f;

final int INTESITY_THRESHOLD = 400000;

ControlP5 cp5;

int vScreenWidth;
int vScreenHeight;
int wSize;
int hSize;

KetaiBluetooth bt;
KetaiAudioInput mic;
short[] data;
double[] BPNInput;
int sumPeaks;
String inputsStr;
String realStr;

BPN brain;
BPNManager brainManager;

boolean useBrain = false;
boolean dance = false;
boolean training = false;

boolean trainTap = false;

boolean isConfiguring = true;
boolean connected = false;
String info = "";
String connectionInfo = "";
KetaiList klist;
ArrayList devicesDiscovered = new ArrayList();
char valueToSend = 'u';

byte [] moves = {'u','d','l','r'};
int  slowIndex = 0;
int  mediumIndex = 0;
int fastIndex = 0;
int maxPeak =0;

int wait = 0;
int time = 0;

//********************************************************************
// The following code is required to enable bluetooth at startup.
//********************************************************************

public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  bt = new KetaiBluetooth(this);
}

public void onActivityResult(int requestCode, int resultCode, Intent data) {
  bt.onActivityResult(requestCode, resultCode, data);
}

public void setup() {
  
  vScreenWidth = displayWidth;
  vScreenHeight = displayHeight;
  
  textSize(34);
  
  mic = new KetaiAudioInput(this);
  //start listening for BT connections
  bt.start();
  //at app start select device\u2026
  orientation(LANDSCAPE);
  background(0); 
  isConfiguring = true;
  
  brain = new BPN(0, DEFAULT_LEARNING_FACTOR, DEFAULT_NUM_HIDDEN_LAYERS, DEFAULT_NUM_UNITS_INPUT_LAYER, DEFAULT_UNITS_OUTPUT_LAYER, DEFAULT_NUM_UNITS_HIDDEN_LAYERS, DEFAULT_TOLERANCE);
  brainManager = new BPNManager(brain);
  
  initUI();
  
}

public void initUI(){
  
  cp5 = new ControlP5(this);
  
  wSize = (int)(vScreenWidth/6);
  hSize = (int)(vScreenHeight/4);
  
  int middle = vScreenWidth/2;
 

  cp5.addButton("logo")
    .setPosition(3*wSize+wSize/2,0.5f)
    .setImage(loadImage("logo1.png"))
    .updateSize()
  ;    

  
  cp5.addButton("UP")
    .setPosition(4*wSize,0.5f*hSize + hSize/2 + hSize/2)
    .setImage(loadImage("up.png"))
    .updateSize()
    .activateBy(ControlP5.PRESSED)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(34);
  ;    
  
  cp5.addButton("LEFT")
    .setPosition(3*wSize+wSize/2,1.5f*hSize + hSize/2)
    .setImage(loadImage("left.png"))
    .updateSize()
    .activateBy(ControlP5.PRESSED)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(34);
  ;    
  
  cp5.addButton("DOWN")
    .setPosition(4*wSize,2.5f*hSize-hSize/2 + hSize/2)
    .setImage(loadImage("down.png"))
    .updateSize()
    //.setSize(wSize,hSize)
    .activateBy(ControlP5.PRESSED)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(34);;
  ;    
  
  cp5.addButton("RIGHT")
    .setPosition(5*wSize-wSize/2,1.5f*hSize + hSize/2)
    .setImage(loadImage("right.png"))
    .updateSize()
    //.setSize(wSize,hSize)
        .activateBy(ControlP5.PRESSED)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(34);
  ;     
  
  wSize = (int)(vScreenWidth/12);
  hSize = (int)(vScreenHeight/8);
  
  cp5.addButton("START_STOP")
    .setPosition(0.5f*wSize,(1*hSize))
    .setImages(loadImage("refresh.png"), loadImage("refresh.png"), loadImage("refresh.png"))
    .updateSize()
    //.setSize(wSize,hSize)
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(17);
  ;
  
  cp5.addTextlabel("label_net")
  .setText("BPN")
  .setPosition(2.5f*wSize,0.7f*hSize)
  .setColorValue(0x00000)
  .setFont(createFont("Georgia",26));
  
  cp5.addButton("NEURAL_NETWORK")
    .setPosition(2.5f*wSize,1*hSize)
    .setImage(loadImage("toggle_off.png"))
    .updateSize()
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(17);
  ;  
  
  
  cp5.addTextlabel("label_train")
  .setText("Train")
  .setPosition(2.5f*wSize,2.2f*hSize)
  .setColorValue(0x00000)
  .setFont(createFont("Georgia",26));
  
  
  cp5.addButton("TRAIN")
    .setPosition(2.5f*wSize,2.5f*hSize)
    .setImage(loadImage("toggle_off.png"))
    .updateSize()
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(17);
  ;
  
  
  cp5.addTextlabel("label_dance")
  .setText("Dance")
  .setPosition(2.5f*wSize,3.7f*hSize)
  .setColorValue(0x00000)
  .setFont(createFont("Georgia",26));
  
  
  cp5.addButton("DANCE")
    .setPosition(2.5f*wSize,4*hSize)
    .setImage(loadImage("toggle_off.png"))
    .updateSize()
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(17);
  ;
  
  cp5.addTextlabel("label_mic")
  .setText("Mic")
  .setPosition(2.5f*wSize,5.2f*hSize)
  .setColorValue(0x00000)
  .setFont(createFont("Georgia",26));
  
  cp5.addButton("MIC")
    .setPosition(2.5f*wSize,5.5f*hSize)
    .setImage(loadImage("toggle_off.png"))
    .updateSize()
    .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setSize(17);
  ;  
}

public void UP(){
  if (isConfiguring || !connected)
    return;
  info = "UP pressed";
  valueToSend = 'u';
}

public void DOWN(){
  if (isConfiguring || !connected)
    return;
  info = "DOWN pressed";
  valueToSend = 'd';
}

public void LEFT(){
  if (isConfiguring || !connected)
    return;
  info = "LEFT pressed";
  valueToSend = 'l';
}

public void RIGHT(){
  if (isConfiguring || !connected)
    return;
  info = "RIGHT pressed";
  valueToSend = 'r';
}

public void START_STOP(){
  if (isConfiguring)
    return;
  if (connected == true){
    bt.stop();
    connectionInfo = "Connection stopped";
    connected = false;
  }
  else{
    connectionInfo = "Connection started";
    cp5 = null;
    bt.start();
    isConfiguring=true;
    connected = false;
    useBrain = false;  
    training = false;
    dance = false;  
    if (mic.isActive()){
      mic.stop();
    }
    
    initUI();     
  }
}

public void MIC(){
  if (mic.isActive()){
    cp5.get(Button.class,"MIC").setImage(loadImage("toggle_off.png"));
    mic.stop(); 
  } else{
    cp5.get(Button.class,"MIC").setImage(loadImage("toggle_on.png"));
    mic.start();
  }
}

public void NEURAL_NETWORK(){
  if (isConfiguring)
    return;
  if (useBrain){
    cp5.get(Button.class,"NEURAL_NETWORK").setImage(loadImage("toggle_off.png"));
    useBrain = false;
  }
  else{
    cp5.get(Button.class,"NEURAL_NETWORK").setImage(loadImage("toggle_on.png"));
    cp5.get(Button.class,"TRAIN").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"DANCE").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"MIC").setImage(loadImage("toggle_on.png"));
    useBrain = true;  
    training = false;
    dance = false;  
    if (!mic.isActive()){
      mic.start();
    }
  }
}

public void TRAIN(){
  if (isConfiguring)
    return;
  if (training){
    cp5.get(Button.class,"TRAIN").setImage(loadImage("toggle_off.png"));
    training = false;
  }
  else{
    cp5.get(Button.class,"NEURAL_NETWORK").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"TRAIN").setImage(loadImage("toggle_on.png"));
    cp5.get(Button.class,"DANCE").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"MIC").setImage(loadImage("toggle_on.png"));
    training = true;  
    dance = false;
    useBrain = false;
    if (!mic.isActive()){
      mic.start();
    }
  }
}

public void activateDance(int w){
  if (dance){
    dance = false;
    cp5.get(Button.class,"DANCE").setImage(loadImage("toggle_off.png"));
  }
  else{
    cp5.get(Button.class,"NEURAL_NETWORK").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"TRAIN").setImage(loadImage("toggle_off.png"));
    cp5.get(Button.class,"DANCE").setImage(loadImage("toggle_on.png"));
    cp5.get(Button.class,"MIC").setImage(loadImage("toggle_on.png"));
    useBrain = false;  
    dance= true;
    time = millis();
    wait = w;
    if (!mic.isActive()){
      mic.start();
    }
  }
}


public void DANCE(){
  if (isConfiguring)
    return;
  activateDance(10);
}



public void draw() {

  if (isConfiguring)
  {
    background(0);
    klist = new KetaiList(this, bt.getPairedDeviceNames());
    isConfiguring = false;
  }
  else
  {
    fill(0);
    background(255);
    text("Action: " + info,15 ,vScreenHeight - 50);    
    if (useBrain){
      
      if (data != null){
       
        findPeaksAndValleys(data);
        double output = brainManager.move(BPNInput);
        
        double lowerBound = 0.9f-brain.getTolerance();
        double upperBound = 0.9f+brain.getTolerance();
        
        boolean dance = (output > lowerBound && output < upperBound); 
        if (dance){
          byte[] result = {getRandomElement(moves)};
          bt.broadcast(result);
        }
        
         info = "Using neural network. Sum peaks: "+sumPeaks+". Value returned: " + output;
      }
      }else if (training){   
         if (data != null){
       
        findPeaksAndValleys(data);
        double output = brainManager.move(BPNInput);
        
        double lowerBound = 0.9f-brain.getTolerance();
        double upperBound = 0.9f+brain.getTolerance();
        
        boolean dance = (output > lowerBound && output < upperBound); 
        if (dance){
          byte[] result = {getRandomElement(moves)};
          bt.broadcast(result);
          
          if (sumPeaks < INTESITY_THRESHOLD){
            brainManager.train(BPNInput, false);
           // System.out.println("Train negative");
          }
          
        }else{
          if (sumPeaks > INTESITY_THRESHOLD){
            brainManager.train(BPNInput, true);
            //System.out.println("Train positive");
          }
          
        }
        
         info = "Training neural network. Sum peaks: "+sumPeaks+". Value returned: " + output;
      }
        
      }else if (dance){
     
        if (data != null){
          findPeaksAndValleys(data);
          info = "Dancing. " +". Thresh: " + INTESITY_THRESHOLD + ". Current: " + sumPeaks;
          
           if (sumPeaks > INTESITY_THRESHOLD){
            byte[] result = {getRandomElement(moves)};
            bt.broadcast(result);
          }
        }
 
      }else if (mousePressed){
        byte[] data = {(byte)valueToSend};
        bt.broadcast(data);
      }else{
        info = "";
      }
    }
    
    if (data != null)
    {     
      stroke(204, 102, 0);
      int baseX = (int)(2.5f*wSize);
      int baseY = height/2;//(int)((5*hSize)+(hSize/2)); 
      for (int i = 0; i < data.length; i++)
      {
        if(i != data.length-1)
          line(baseX+i, baseY+map(data[i], -32768, 32767,displayHeight,0), baseX+i+1, baseY+map(data[i+1], -32768, 32767,displayHeight,0));
      }
    } 
  }

  
  
  public byte[] getRandomInputs(int numInputs){
    byte[] result = new byte[numInputs];
    for (int i = 0; i < numInputs; ++i){
      result[i] = (byte)(((int)random(2) == 1) ? '1' : '0');
    }
    return result;
  }
  
  public short[] getRandomInputsShort(int numInputs){
    short[] result = new short[numInputs];
    for (int i = 0; i < numInputs; ++i){
      result[i] = (short)random(16000);
    }
    return result;
  }
  
  public byte getRandomElement(byte[] list){
    return list[(int)random(list.length)];
  }
  
  public void onKetaiListSelection(KetaiList klist) {
    String selection = klist.getSelection();
    connectionInfo = "Connecting to " + selection + "." + str(bt.connectToDeviceByName(selection));
    //dispose of list for now
    connected = true;
    klist = null;
  }
  
  public int [] findPeaksAndValleys(short [] array){
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
  
  public void onAudioEvent(short[] _data)
  {
    trainTap = mousePressed;
    data= _data;
  }
  
//Call back method to manage data received
  public void onBluetoothDataEvent(String who, byte[] data) {
    if (isConfiguring)
      return;
  }
  




/**
 * Implements a Back Propagation Network, its data structures and methods inherent to the model
 * @author Andres Mena
 * @version 2012
 
 */
public class BPN
{
    private Layer inputUnits;
    private Layer outputUnits;
    private Layer[] layers;
    private double alpha;
    private double eta;
    private int numHiddenLayers;
    private int numInputUnits;
    private int numOutputUnits;
    private int numUnitsHiddenLayer;
    private double [] expectedOutput;
    private double tolerance;
    private boolean error;
    private double [] outputVector;
    private double errorsAverage;
    
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
                    weight = 0.3f*weight;
                    if(sign==1){
                    weight=-1*weight;
                    }
                    layers[k].getDelta()[i][j]=layers[k].getWeights()[i][j]=weight;
                }
            }
        }
    }

    public void setAlpha(double value){
            alpha=value;
    }
    
    public void setEta(double value){
        eta=value;
    }
    
    public void setNumHiddenLayers(int value){
        numHiddenLayers=value;
    }
    
    public boolean getError(){
        return error;
    }
    
    public double getTolerance(){
        return tolerance;
    }
    
    public double getErrorsAverage(){
        return errorsAverage;
    }
    
    
    public int getNumInputUnits(){
        return numInputUnits-1;
    }
    
    public int getNumOutputUnits(){
        return numOutputUnits;
    }
    
    public Layer getUnputUnits(){
        return inputUnits;
    }
    
    public Layer getOutputUnits(){
        return outputUnits;
    }
    
    public Layer getLayer(int index){
        return layers[index];
    } 
    
    public double getAlpha(){
        return alpha;
    }
    
    public double getEta(){
        return eta;
    }
    
    public int getNumHiddenLayers(){
        return numHiddenLayers;
    }

    public void setExpectedOutput(double[] value){
        for(int i=0;i<numOutputUnits;++i){
            expectedOutput[i]=value[i];
        }
    }
    
   public void setInputs(double[] inputs){
        inputUnits.setOutputs(0,1);
        for(int i=1;i<numInputUnits;++i){
            inputUnits.setOutputs(i,inputs[i-1]);
        }    
    }
    
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
            current[i]=1.0f/(1.0f + Math.exp(-sum));

        
        }
        
        
        
        
    }
    
    public void propagateForward(){
        Layer upper;
        Layer lower;
        
        for(int i=0;i<layers.length-1;++i){
            layers[i].setOutputs(0,1);
            lower=layers[i];
            upper=layers[i+1];
            propagateLayer(lower,upper);            
        }
    }
    
   public double[] getOutputs(){
        for(int i=0;i<numOutputUnits;++i){
            outputVector[i]=outputUnits.getOutputs(i);
        }
        return outputVector;
   }
    
   public void computeOutputError(){
        double [] errors = outputUnits.getErrors();
        double [] outputs = outputUnits.getOutputs();
        double upperLimit=0.0f;
        double lowerLimit=0.0f;
        error=false;
        errorsAverage = 0;
        for(int i =0; i< numOutputUnits; ++i){
            upperLimit=expectedOutput[i]+tolerance;
            lowerLimit=expectedOutput[i]-tolerance;
            
            if(outputs[i]>upperLimit || outputs[i]<lowerLimit){
                error=true;
            }
            errors[i]= outputs[i]*(1-outputs[i])*(expectedOutput[i]-outputs[i]);
            

            double errorAbs = expectedOutput[i]-outputs[i];

            errorAbs = (errorAbs >= 0) ? errorAbs : (-1*errorAbs);
            errorsAverage+= errorAbs;
            
        }
        
        errorsAverage = errorsAverage/numOutputUnits;
    }
    
    public void backPropagationError(Layer lower, Layer upper){
        double [] sender = upper.getErrors(); 
        double [] receiver = lower.getErrors();
        double [][] connections=upper.getWeights();
        double unit;
        for(int i=0; i<lower.getSize();++i){
            receiver[i]=0;
            for(int j=0; j<upper.getSize();++j){

                
                receiver[i] = receiver[i]+ sender[j]*connections[j][i];
            }

            unit = lower.getOutputs(i);
            receiver[i]= receiver[i]* unit * (1-unit);

            
        }
    }
    
    
    public void propagateBackward(){
        Layer upper;
        Layer lower;
        
        for(int i=layers.length-1;i>1;--i){
            lower=layers[i-1];
            upper=layers[i];
            backPropagationError(lower,upper);
        }
    }
    
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

    /**
     * Subclass that implements a Layer of the BPN
     * @author Andres Mena
     * @version 2012
     
     */
      public class Layer{
          private double [] outputs;
          private double [] errors;
          private double [][] weights;
          private double [][] last_delta;//no sabemos
          private int size;
          private int numInputs;
          
          public Layer(int t, int numI){
                numInputs=numI;
                size=t;
                outputs=new double[t];
                errors=new double[t];
                weights=new double [t][numI];
                last_delta = new double [t][numI];
            }
          
          public int getSize(){
            return size;  
          }
            
         public int getNumInputs(){
            return numInputs;  
         }
        
          public void setOutputs(int index, double value){
              outputs [index]=value;
          }
    
          public double getOutputs(int index){
            return outputs[index];
          }
          
           public double [] getOutputs(){
            return outputs;
          }
          
           public double [][] getWeights(){
            return weights;
          }
          
           public double [][] getDelta(){
            return last_delta;
          }
          
            public double [] getErrors(){
            return errors;
          }
        
        }
}




/**
 * Manages a neural network able to recognize patterns based on songs strenght
 */
public class BPNManager
{

    private BPN neuralNetwork;
    

    public BPNManager(BPN n){
        neuralNetwork = n;
    }
    
    
    public void setRedNeuronal(BPN n){
        this.neuralNetwork = n;
    }
    
       
    
    public double move(double [] input){
        neuralNetwork.setInputs(input);
        neuralNetwork.propagateForward();
        double [] output = neuralNetwork.getOutputs();        
        return output[0];
    }
    
    public void train(double [] input, boolean result){
                neuralNetwork.setInputs(input);
                double r = result? 0.9f : 0.1f;
                double [] output = {r};
                neuralNetwork.setExpectedOutput(output);
                neuralNetwork.propagateForward();
                neuralNetwork.computeOutputError();
                if (neuralNetwork.getError()){
                    neuralNetwork.propagateBackward();
                    neuralNetwork.adjustWeights();
                }
    }
        

}   
    
  public void settings() {  size(displayWidth, displayHeight); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "RedBotDance" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
