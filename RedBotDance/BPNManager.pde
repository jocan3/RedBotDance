import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.lang.Thread;
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
        

}   
    