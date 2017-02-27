package edu.pantek.util.mulithread;

public class test {
	    /*全局协调函数*/
        public static int Incre=0;
	    public static void setIncre(int incre){	    	
	    	Incre=incre;
	    }
	    public static int getIncre(){
		   return Incre;
	    }
	    /*多线程执行部分*/
	    public static void excute(String name,int increment){
	     System.out.println(name);
	    	for(int i=0;i<1;i++){
	    	  if(getIncre()==0){
	    		  setIncre(increment);
	    		  //System.out.println(getIncre());
	    	  }	    	
	    	  int cre= getIncre();	    	   
	    	  //System.out.println(cre);	        
	    	  setIncre(cre+increment);	    	
	    	  System.out.println(getIncre());
	    	}
	    }
	    
	    public static void main(String[] args){
	    	excute("test",100);
	    }

}


