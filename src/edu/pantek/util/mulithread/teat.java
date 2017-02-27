package edu.pantek.util.mulithread;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import edu.pantek.util.mulithread.MulitThreads;

public class teat extends ThreadObject{
         int num=4;
	     
	     public void ThreadsModel(int id){
	    	    //if(id==0)
	    	      Date d1=new Date();
	    	       //System.out.println("Thread:"+ (id+1)+"  Say Hello!");
	    	       RandomAccessFile ran=null;     
	    	       Long FileSize = null;
	    		   try {
	    					 ran=new RandomAccessFile("./word2vec_res/train_files/text8","r");
	    					 FileSize = new Long(ran.length()/num*(id+1)-1);
	    					 ran.seek(ran.length()/num*id);
	    				} catch (Exception e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}finally{
	    					try {
								ran.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	    				}
	    		  @SuppressWarnings("unused")
				  String  word;
	    		  Long ls;
	    		  while(true){
	    			  try {
	    				 ls=new Long(ran.getFilePointer());
						if(ls>=FileSize)  break;
						else {
							word=ReadOne(ran,FileSize);
			     	    	// if(!word.equals(""))
			     	    	      //System.out.println("Thread:"+ (id+1)+" print:   "+word);
							}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	    		  }
	    		  num-=1;
	    		  
	    		  Date d2=new Date();
		          System.out.println((d2.getTime()-d1.getTime())/1000);
		          if(num==0) System.out.println("Over");
	     }
	     
	     public String ReadOneWord(RandomAccessFile ran){
	    	 String word="";
	    	 String temp="";
	    	 byte[] b=new byte[1];
	    	 while(true){
	    		 try {
					 ran.read(b,0, 1);
					 temp=new String(b,"gbk");
					 temp=temp.trim();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	    		
	    		 if(temp.equals("")||temp.equals(null)) {
	    			 break;
	    		 }
	    		 //if( word.equals("\t")|| word.equals("\n")|| word.equals("\r")) continue; 
	    		 else { 
	    			        word+=temp;
	    	     }
	    	 }
	    	 return word;
	     }
	     
	     public String ReadOne(RandomAccessFile ran,Long l)  {    	   
	    	    String word="";
	    	    int i=0;
	    	    byte temp=' ';
	    	    byte[] tmp=new byte[50];
	    	    while(true){	    	    
	    	    	try {
	    	    		if(ran.getFilePointer()>=l)  break;
						temp=ran.readByte();
					    //System.out.println((int)temp);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    	    	//System.out.println((int)temp);
	    	        if((int)temp==32) break;
	    	        if(i>=50) break;
	    	        else{
		    	        	tmp[i]=temp;		    	        	
		    	        	i++;
	    	        }
	    	    }
				try {
					word = new String(tmp,"utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	  
               
	    	    return word;
	     }
	     public void mulit(int num,teat t){
	    	 MulitThreads.MulitExcute(num,t);
	    	 
	     }
	     
	   public void Save(){
		   while(true){
			   if(num==0) break;
		   }
		   System.out.println("over");
	   } 
	     
	   public static void main(String[] args){
		   //teat t=new teat();
		  // System.out.println(Runtime.getRuntime().availableProcessors());
		  // t.mulit(4, t);
		   /* RandomAccessFile ran=null;     
		   
		   String word="";
		   try {	   th.start();
	   MulitThreads mulit1 =new MulitThreads(1,t);
	   Thread th1=new Thread(mulit1);
	   th1.start();
	   MulitThreads mulit2 =new MulitThreads(2,t);
	   Thread th2=new Thread(mulit2);
	   th2.start();
	   MulitThreads mulit3 =new MulitThreads(3,t);
	   Thread th3=new Thread(mulit3);
	   th3.start();
					 ran=new RandomAccessFile("./word2vec_res/train_files/text8","r");
					 ran.seek(0);
					 word=t.ReadOne(ran,ran.length());
					 word=t.ReadOne(ran,ran.length());
					 word=t.ReadOne(ran,ran.length());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		         
		         
		     //    for(int i=0;i<10;i++){
		        	  
		        	 if(!word.equals(""))
		        	      System.out.println(word);
		        	    //  System.out.println(t.ReadOneWord(ran));
		        	    //  System.out.println(t.ReadOneWord(ran));
		        	    //  System.out.println(t.ReadOneWord(ran));
		        	     // System.out.println(t.ReadOneWord(ran));
		        	      //int i=1;
		                //  String word="";
		        	  //    for(int i=0;i<16;i++){
		        	 //   	  word=t.ReadOne(ran,1);
		        	 //   	  if(!word.equals(""))
		        	 //   	  System.out.println(word);
		        	    	 // i++;
		        	   //   }
		         //}
		        	*/ 
		          
		         //  MulitThreads.MulitExcute(2,t);
		         /*
		   List<String> list=new ArrayList<String>();
		   list.add("test");
		  // list.add("test1");
		   FileOutputStream fw=null;
		   ObjectOutputStream out=null;		   
		try {
			fw = new   FileOutputStream("./test.dat");
			out=new ObjectOutputStream(fw);
			out.writeObject(list);
			out.close();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   try {
			FileInputStream file=new FileInputStream("./test.dat");
			ObjectInputStream obreader=new ObjectInputStream(file);
			Object l=obreader.readObject();
			System.out.println((List)l);
			obreader.close();
			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		        */
		   //Configuration conf=new Configuration();
		   //System.out.println(conf.Window);
	}
}
