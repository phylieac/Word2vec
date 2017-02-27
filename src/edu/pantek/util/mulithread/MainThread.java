package edu.pantek.util.mulithread;

class MainThread implements Runnable {

	private String ThreadName;
	private int ThreadNum;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println(ThreadName);
		System.out.println("Thread's Num : "+ThreadNum);
       // MulitThread.MulitExcute(ThreadNum);
		
	}
    public MainThread(String name,int num){
    	this.ThreadName=name+ " Thread State: running";
    	this.ThreadNum=num;
    }
    public static void main(String[] args){
    	MainThread test=new MainThread("test",5);
    	Thread th=new Thread(test);
    	th.start();
    }
	  
}
