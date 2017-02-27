package edu.pantek.deeplearning.word2vec;

public class Configuration {

	   int CBow=0;
	   int Window=5;

	   int Mini_Count=5; 
	   int Num_Threads=1;
	   int Min_Reduce=1;
       
	   int Layer1_Size=200;
       
	   boolean hs=true;
	   int negative=0;
       /**
        * CBow=1:CBOW Model, CBow=0:Skip-Gram Model
        * @param cb
        */
       public void setCBOW(int cb){
    	   this.CBow=cb;
       }
       /**
        * Window
        * @param win
        */
       public void setWindow(int win){
    	   this.Window=win;
       }
       /**
        * MiniCount
        * @param count
        */
       public void setMiniCount(int count){
    	   this.Mini_Count=count;
       }
       /**
        * Threads
        * @param nu
        */
       public void setThreads(int nu){
    	   this.Num_Threads=nu;
       }
       /**
        * MiniReduce
        */
       public void setReduce(int mini){
    	   this.Min_Reduce=mini;
       }
       /**
        * LayerSize
        */
       public void setLayerSize(int size){
           this.Layer1_Size=size;	   
       }
       /**
        * hs
        * @param h
        */
       public void seths(boolean h){
    	   this.hs=h;
       }
       /**
        * @param neg  0 or 1
        */
       public void setNegtive(int neg){
    	   this.negative=neg;
       }
}
