/**
 * @author pan
 * MulitThreads Super Class, Main Function --MulitExcute(id)
 */
package edu.pantek.util.mulithread;

public class MulitThreads extends Thread{

	private int threadID;
	@SuppressWarnings("unused")
	private static int threadNum;
	private ThreadObject to;
	
	public void run() {
         to.ThreadsModel(threadID);
         
	}
    public MulitThreads(int id,ThreadObject ob){                                 	
    	this.threadID=id;
    	this.to=ob;
    	//System.out.println(this.threadName);
    }
    public int getID(){
    	return this.threadID;
    }
    
   public static  void MulitExcute(int num,ThreadObject t){
	    threadNum=num;
	    for (int i=0 ;i<num;i++){
	        Thread mulit =new MulitThreads(i,t);
	        mulit.start();
	   }
   }
   public static void main(String[] args){
	     //MulitExcute(10);
	   }
   }
