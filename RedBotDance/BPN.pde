import java.util.Random;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
                    weight = 0.3*weight;
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
            current[i]=1.0/(1.0 + Math.exp(-sum));

        
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
        double upperLimit=0.0;
        double lowerLimit=0.0;
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