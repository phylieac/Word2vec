package edu.pantek.deeplearning.nlp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ClassificationTree {

	private static String classfile="./data/ClassWord_lib/ClassWordLIB.txt";
	private static String cachepath="./data/cache/";
	private static boolean loadState=false;
	//Classification Tree
	static Map<Map<String,String>,String> root=new LinkedHashMap<Map<String,String>,String>();
	
	public ClassificationTree(){
		
	}
	public ClassificationTree(boolean cachetree){
		if(cachetree)
		        loadTree();
		else System.out.println("please build the classification tree!");
	}
	public static boolean initTree(){
		loadTree();
		loadState=true;
		if(root.size()!=0) return true;
		else 
			return false;
	}
	
	public static  void buildTree(){
		FileReader file=null;
		BufferedReader buff=null;
		try{
			file=new FileReader(classfile);
			buff=new BufferedReader(file);
			String line="";				
			while((line=buff.readLine())!=""&&line!=null){
				String[] result=line.split("\\s");
				Map<String,String> node=new LinkedHashMap<String,String>();
				for(int i=1;i<result.length;i++){
					node.put(result[i], result[0]);
				}
				root.put(node, "root");
			}
		}catch(Exception e){
			System.out.println("can't read the file"+classfile);
		}finally{
			try {
				buff.close();
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void cacheTree(){
		FileOutputStream file=null;
		ObjectOutputStream obj=null; 
		String filepath=cachepath+"classtree.dat";
		try{
			file=new FileOutputStream(filepath);
			obj=new ObjectOutputStream(file);
			obj.writeObject(root);
			obj.flush();
		}catch(Exception e){
			System.out.println("can't save the file："+filepath);
		}finally{
			try{
			     obj.close();
			     file.close();
			}catch(IOException w){
				System.out.println(w);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static void loadTree(){
		FileInputStream file=null;
		ObjectInputStream obj=null;
		String filecache=cachepath+"classtree.dat";
		try{
			file=new FileInputStream(filecache);			
			obj=new ObjectInputStream(file);
			root=(Map<Map<String, String>, String>) obj.readObject();
			obj.close();
			file.close();
		}catch(Exception e){
			System.out.println("Rebuild and cache the Classification Tree!");
			buildTree();
			cacheTree();
			loadTree();
		}
	}
	public static String getClassification(String word){
		if(!loadState) {
			System.out.println("Init Classification Tree First!");
			return null; 
		}
		String classnode="unknow";
		Iterator<Entry<Map<String, String>, String>> iter=root.entrySet().iterator();
		Entry<Map<String,String>,String> entry;
		while(iter.hasNext()){
			entry=iter.next();
			if(entry.getKey().containsKey(word)) return entry.getKey().get(word);
		}
		//if no find return "unknow"
		return classnode;
	}
	public static void main(String[] args){
		//ClassificationTree tree=new ClassificationTree();
		//tree.buildTree();
		//tree.cacheTree();
		//tree.getClassification();
		if(ClassificationTree.initTree())
		   System.out.println(ClassificationTree.getClassification("水煮鱼"));
	}
}
