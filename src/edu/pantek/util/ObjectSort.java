package edu.pantek.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectSort {
	       /**
	        * 冒泡排序,结果由大到小.
	        * */
	       public static long[] sortArray(long[] array){
	    	   //两个辅助空间
	    	   int count=0;
	    	   long exc;
	    	   while(true){
	    		   count=0;
	    		   for(int i=0;i<array.length-1;i++){
	    			    if(array[i]<array[i+1]){
	    				    exc=array[i];
	    				    array[i]=array[i+1];
	    				    array[i+1]=exc;
	    				    count++;
	    			    }  
	    	      }
	              if(count==0)	 break;	    		    
	    	   }	    	   
	    	   return array;
	       }
	       /**
	        * 三个辅助空间,改变原有List值顺序
	        * @param str
	        * @param beg
	        */
	       public static void sortList(List<Struct> str,int beg){
	    	 //三个辅助空间
	    	   int count=0;
	    	   long exc;
	    	   String word;
	    	   while(true){
	    		   count=0;
	    		   for(int i=beg;i<str.size()-1;i++){
	    			    if(str.get(i).cn<str.get(i+1).cn){
	    				    exc=str.get(i).cn;
	    				    word=str.get(i).word;
	    				    str.get(i).cn=str.get(i+1).cn;
	    				    str.get(i).word=str.get(i+1).word;
	    				    str.get(i+1).cn=exc;
	    				    str.get(i+1).word=word;
	    				    count++;
	    			    }  
	    	      }
	              if(count==0)	 break;	 
	             // System.out.println(count);
	    	   }	    	   
	       }
	       /**
	        * 快排排序
	        */
	        public static void quickSort(long[] array,int beg,int end,boolean desc){
	        	int start=beg;int stop=end;
	        	long pos=array[beg];	        	
	            while(beg<end){
	            	if(desc){
	        		     while(end>beg&&array[end]<=pos) end--;
	                     if(beg<end)      array[beg]=array[end];
	                     while(beg<end&&array[beg]>=pos)  beg++;
	                     if(beg<end)  array[end]=array[beg];	                
	        	    }else{
	        	    	 while(end>beg&&array[end]>=pos) end--;
		                 if(beg<end)      array[beg]=array[end];
		                 while(beg<end&&array[beg]<=pos)  beg++;
		                 if(beg<end)  array[end]=array[beg];	       
	        	    }
	        	   array[beg]=pos;
	        	  if(beg-start>1)   quickSort(array,start,beg-1,desc);
	        	  if(stop-end>1)   quickSort(array,end+1,stop,desc);
	           }
	      }
	        /**
	         * 降序快排函数
	         * @param str List
	         * @param beg 开始指针
	         * @param end 结束指针
	         */	    
	     public static void quicksort(List<Struct> str,int beg,int end){
	    	 int start=beg;
	    	 int stop=end;
	    	 //递归时将切分数据几何为多个子集合,所以注意此处为每个子集beg;
	    	 String word=str.get(beg).word;
	    	 long pivot= str.get(beg).cn;
	    	 while(beg<end){
	    		 while(end>beg&&str.get(end).cn<=pivot) end--;
	    		 if(beg<end){
	    			 str.get(beg).cn=str.get(end).cn;
	    			 str.get(beg).word=str.get(end).word;
	    		 }
	    		 while(beg<end&&str.get(beg).cn>=pivot) beg++;
	    		 if(beg<end){
	    			 str.get(end).cn=str.get(beg).cn;
	    			 str.get(end).word=str.get(beg).word;
	    		 }
	    		 str.get(beg).cn=pivot;
	    		 str.get(beg).word=word;
	    		 if(beg-start>1)   quicksort(str,start,beg-1);
	    		 if(stop-end>1)   quicksort(str,beg+1,stop);
	    		// System.out.println(cn);
	    	 }
	     }
	     /**
	      * 堆排序
	      * 
	      */
	     public static void heapsort(){
	    	 
	     }
	     /**
	      * Collections Sort 排序
	      * @param str
	      * @return
	      */
	     public static void CSort(List<Struct> str){
	    	// ArrayList<Struct> list=new ArrayList<Struct>(str);
	    	 Collections.sort(str,new Comparator<Struct>(){
	    		 public int compare(Struct str1,Struct str2){	    			 
	    			 return (int) (str2.cn-str1.cn);
	    		 }
	    	 });	 
	    	 //return str;
	     }
	     public static Map<String, Integer> MapSortByValue(Map<String,Integer> map){
	     	ArrayList<Map.Entry<String, Integer>> list=new ArrayList<Map.Entry<String,Integer>>(map.entrySet());
	    	Collections.sort(list,new Comparator<Map.Entry<String,Integer>>(){
	    		public int compare(Entry<String,Integer> arg0,Entry<String,Integer> arg1){
	    			return arg1.getValue()-arg0.getValue();
	    		}
	    	});
	    	Map<String,Integer> result=new LinkedHashMap<String,Integer>();
	    	for(int i=0;i<list.size();i++){
	    		result.put(list.get(i).getKey(), list.get(i).getValue());
	    	}
	    	//释放内存
	    	list.clear();
	    	list=null;
	    	//System.out.println(result.toString());
	    	return result; 	
	     }
}
