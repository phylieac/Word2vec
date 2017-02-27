package edu.pantek.util;

import java.io.File;

public class FileOperate {

     public static boolean exist(String src){
    	 File f= new File(src);
    	 if(f.isFile()) return true;
    	 else return false;
     } 
    public static boolean delete(String src){
    	File f=new File(src);
    	if(f.isFile()){
    		if(f.delete()) return true;
    		else return false;
    	}else return false;
    }
}
