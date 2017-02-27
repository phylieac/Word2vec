package edu.pantek.deeplearning.nlp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WordPos {

	private static String wordpos="./data/WordPosLib/swresult.txt";
	private static String cachePath="./data/cache/";
	private static Map<String,String> posMap=new HashMap<String,String>();
	@SuppressWarnings("unused")
	private static boolean loadstate=false;
	
	public static void loadWord(){
		FileInputStream file=null;
		Scanner scan=null;
		
		try {
			file = new FileInputStream(wordpos);
			scan=new Scanner(file);
			while(scan.hasNext()){			
				String[] words=scan.next().split("/");
				if(words.length==1) continue;
				//System.out.println(words[0]);			
				posMap.put(words[0], words[1]);
			}
			scan.close();
			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void cachePos(){
		String cachefile=cachePath+"wordPos.dat";
		FileOutputStream out=null;
		ObjectOutputStream obj=null;
		try{
			out=new FileOutputStream(cachefile);
			obj=new ObjectOutputStream(out);
			obj.writeObject(posMap);
			obj.close();
			out.close();
		}catch(Exception e){
			System.out.println("Load the file fault! ："+cachefile);
		}	
	}
	@SuppressWarnings("unchecked")
	public static void loadcache(){
		   String cachefile=cachePath+"wordPos.dat";
		   FileInputStream in=null;
		   ObjectInputStream obj=null;
		   try{
			   in=new FileInputStream(cachefile);
			   obj=new ObjectInputStream(in);
			   posMap= (Map<String, String>) obj.readObject();
			   obj.close();
			   in.close();
		   }catch(Exception e){
			   System.out.println("Rebuild and cache wordspos!");
			   loadWord();
			   cachePos();
		   }
	}
	public static String getPos(String word){
		String pos="un";
		if(posMap.containsKey(word)) return posMap.get(word);
		else
			return pos;
	}
	public static boolean InitWordPos(){
        loadcache();
        loadstate=true;
        if(posMap.size()!=0) return true;
        else    return false;
	}
	public static void main(String[] args){
		if(WordPos.InitWordPos())
			System.out.println(WordPos.getPos("中华人民共和国"));
		//WordPos.loadWord();
	    System.out.println(WordPos.posMap.size());
		//WordPos.cachePos();
	}
}
