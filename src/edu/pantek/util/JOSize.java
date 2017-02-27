package edu.pantek.util;

import java.util.ArrayList;
import java.util.List;


public class JOSize{
	 
	public double  SizeOf_List(List<String> list){
		double size=-1;
		for(int i=0;i<list.size();i++){
			if(i==0)
				size+=1;
			size+= list.get(i).length();			
		}
	   if(size!=-1)
		   size=size/512;
	   
		return size;
	}
	public static void main(String[] args){
		
		List<String> list =new ArrayList<String>();
		
		JOSize js=new JOSize();
		System.out.println(js.SizeOf_List(list)+" KB");
		
		
		
		
	}
}