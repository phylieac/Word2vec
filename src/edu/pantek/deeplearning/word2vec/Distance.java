/**
 * @author Hongyan Pan----Phy
 * Date:2014.9.27
 * Main Calculation Class: Get N Words that Most Closest to the Given Word.
 * Date:2014.10.5  Added Object File Save as cache.
 * Date:2014.10.6  Added k-means method to cluster all vectors to n classes.
 * Date:2015.3  Present the POS-CBOW Model and POS-Skip-gram Model
 */
package edu.pantek.deeplearning.word2vec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import edu.pantek.deeplearning.math.MathMethods;
import edu.pantek.util.nlp.NLPIR;

public class Distance {

	/**
	 * 参数设置
	 * */
	 private  long  N=40;                   //最优词数     number of closest words
	 private String vec_src;
	/**
	 * 内存参数声明
	 * */
	 private  long  vocabSize;
	 private  long  layerSize;
	 private  String [] best_word;
	 private  Float[] bestd;
	 Map<String,float[]> vecMap=new LinkedHashMap<String,float[]>();
	 int[] posTable=new int[26];
	 
	 //Map<String,List<Float[]>> vecMap=new LinkedHashMap<String,List<Float[]>>();
	 //List<String> wordList=new ArrayList<String>();
	// List<Object> vectorList=new ArrayList<Object>();
	 /**
	  * 参数get和set方法
	  * */
	 private void set_N(long n){
		 this.N=n;
	 }
	 private void set_vec_file(String file){
		 this.vec_src=file;
	 }
     private void set_vocabSize(long size){
    	 this.vocabSize=size;
     }
     private long get_layerSize(){
    	 return this.layerSize;
     }
	 private void set_layerSize(long size){
		 this.layerSize=size;
	 }
	/**
	 * 读取二进制词向量文件
	 * Object File
	 * */
	@SuppressWarnings("unchecked")
	private void readBinaryVec(String Cache_file_path){
		 FileInputStream fi=null;
		 ObjectInputStream oi=null;		 
		 try {
			fi=new FileInputStream(Cache_file_path);
			oi=new ObjectInputStream(fi);			
			vecMap=  (Map<String, float[]>) oi.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				oi.close();
				fi.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 vocabSize=vecMap.size();
		 layerSize=vecMap.get("</s>").length;
	}
	/**
	 * 
	 * @param vector_file_path
	 */
	 private void readVector(String vector_file_path){
		//设置文本型词向量文件
		 set_vec_file(vector_file_path);
		 File file =new File(vec_src);
		 if(file.exists()&&file.canRead()){
		 try{
		          FileReader reader=new FileReader(file);
		          BufferedReader buffer=new BufferedReader(reader);
		          String line=null;
		         //读取第一行，{词数，层数}
		          String[] firstLine= buffer.readLine().split("\\s");
		          set_vocabSize(Integer.valueOf(firstLine[0]).intValue());
		          set_layerSize(Integer.valueOf(firstLine[1]).intValue());
		         //读取所有词到词典，所有层值
		          String[] wordVec=new String[(int)get_layerSize()+1];
		         
		          while((line=buffer.readLine())!=null){ 
		        	           float[] vector = new  float[(int)get_layerSize()];
		                       wordVec=line.split("\\s");			    
		                      //wordvec[0]为词，wordvec[1]-wordvec[200]为layer每层的值
			                   for(int i=1;i<wordVec.length;i++){				 
				                      vector[i-1]=Float.valueOf(wordVec[i]).floatValue();
			                   }		
			                   //求各项平方和
			                  double len=0;
			                  for(int j=0;j<vector.length;j++){
				                        len+=vector[j]*vector[j];
			                   }
			                  //开len二次方
			                  len=Math.sqrt(len);
			                 //各项除以len
			                 for(int h=0;h<vector.length;h++){
				                        vector[h]=vector[h]/(float)len;
			                  }
			                    //加入vecMap,vector
			                 vecMap.put(wordVec[0], vector);			                
		 }		          
		 buffer.close();
		 reader.close();
		 }catch(IOException e){
			 System.out.println(e);
		 }
		 }else{
			 System.out.println("文件不可读或不存在！");
		 }
	}
   /**
	* 初始化词性表
	*/
	public void initPOSTable(){
		 int a=97;
		 for(int i=0;i<26;i++) posTable[i]=a+i;
	}		
	/**
	 *使用默认参数加载词向量文件 
	 * */
	public Distance(String filePath,boolean loadcache){
		this.initPOSTable();
		this.best_word=new String[(int)N];
		this.bestd=new Float[(int)N];
		if(!filePath.trim().isEmpty()){
			if(loadcache)
				     this.readBinaryVec(filePath);
			else
				     this.readVector(filePath);
		}
		else 
			System.out.println("词向量文件不存在或路径不正确！");
	}
	/**
	 * 设置相近词向量个数
	 * */
	public Distance(int cn,String filePath,boolean loadcache){
		this.set_N(cn);
		this.best_word=new String[(int)N];
		this.bestd=new Float[(int)N];
		this.initPOSTable();
		if(!filePath.trim().isEmpty()){
			if(loadcache)
				this.readBinaryVec(filePath);
		     else
			     this.readVector(filePath);
		}
        else 
	          System.out.println("词向量文件不存在或路径不正确！");
	}
   public int getPOS(String pos){
	   for(int x:posTable){
		   if(pos.indexOf(x)!=-1) return x;
	   }
	   return -1;
   }
	/**
	 * 计算词向量
	 * */
	public float calculateWords(String word1,String word2){
		float dist=0;
		if(!word1.trim().equals("")&&!word2.trim().equals("")){
		            float[] word1vec=vecMap.get(word1);
		            float[] word2vec=vecMap.get(word2);
		            if(word1vec!=null&&word2vec!=null){
		            	//计算词一向量
			                float len=0;
			                 for(int i=0;i<word1vec.length;i++){
			                	 len+=word1vec[i]*word1vec[i];
			                 }
			                 len=(float)Math.sqrt(len);
		            	     for(int i=0;i<word1vec.length;i++){
		            	    	 word1vec[i]/=len;
		            	     }
		            	    //计算word1和word2的Distance
		            	     if(word1vec.length==word2vec.length){
		            	         for(int i=0;i<word1vec.length;i++){
		            	        	       dist=dist+word1vec[i]*word2vec[i];
		            	         }
		            	     }else
		            	    	 System.out.println("检查\""+word1+"\"与\""+word2+"\" 的向量值！");
		            }else if(word1vec==null) 
                              System.out.println(word1+": Out of vocab");
		            else{
			                 System.out.println(word2+": Out of vocab");
		             }
		}else
			       System.out.println("参数不能为空字符！");
        return dist;
	}
	/**
	 * 计算word相近词Distance集合
	 * */
	public void calculateDistance(String word){
		       word=word.trim();
	           if(word.equals("")){
	        	   System.out.println("计算词不能为空！");
	        	   return;
	           }
	  	         //重新初始化bestd数组与best_word数组
	  	         for(int l=0;l<N;l++)   { 
	  	        	 bestd[l]=(float) -1;
	  	        	 best_word[l]=null;
	  	        }
	           //取出词word的词向量
	           float[] wordvec=vecMap.get(word);
	           if(wordvec!=null){
	        	   float len=0;
	        	   for(int i=0;i<wordvec.length;i++){
	        		   len+=wordvec[i]*wordvec[i];
	        	   }
	        	   len=(float)Math.sqrt(len);
	        	   for(int j=0;j<wordvec.length;j++){
	        		   wordvec[j]=wordvec[j]/len;
	        	   }
		        	 //迭代整个词典计算Distance  
		           Iterator<Entry<String, float[]>> iter=vecMap.entrySet().iterator();
		           Map.Entry<String, float[]> entry;
		           float[] f;	     
		           String vectorWord;
		           while(iter.hasNext()){
		        	         //词距离
		        	         float dist=0;
		        	         //该向量词	        	         
		                     entry=iter.next();
		                     //System.out.println(entry.getKey());
		                     vectorWord=entry.getKey();
		                     if (vectorWord.equals(word)) continue;
		        	         f=entry.getValue();	        	         	        	         
		        	         if(wordvec.length==f.length){	        	          	 
		        	        	 for(int k=0;k<wordvec.length;k++){
		        	        		 dist+=wordvec[k]*f[k];
		        	        	 }	        	        
		        	         }else
		        	        	 System.out.println("词 "+word+" ，"+entry.getKey()+"向量读取错误");
		        	         //加入最优词与Distance集合
		        	         for(int m=0;m<N;m++){
		        	        	 if(dist>bestd[m]){
		        	        		 for(int d=(int)N-1;d>m;d--){
		        	        			 bestd[d]=bestd[d-1];
		        	        			 best_word[d]=best_word[d-1];
		        	        		 }
		        	        		 bestd[m]=dist;
		        	        		 best_word[m]=vectorWord;
		        	        		 break;
		        	        	 }
		        	         }	        	         
		           }
	           }else{
	        	   System.out.println("Word: "+word+" , out of vocab");
	           }	           
	}
	
	 public void calculateDistanceWithPOS(String wordPos){
		if(!NLPIR.InitState) NLPIR.Init();
		wordPos=wordPos.trim();
       if(wordPos.equals("")){
    	   System.out.println("计算词不能为空！");
    	   return;
       }
       String posInfo=NLPIR.getWordPos(wordPos);
       wordPos=wordPos+"/"+posInfo;
       int posNum=this.getPOS(posInfo);
      // System.out.println("pos : "+(char)posNum);
	    //重新初始化bestd数组与best_word数组
	    for(int l=0;l<N;l++){
	        bestd[l]=(float) -1;
	        best_word[l]=null;
	    }
       //取出词word的词向量
       float[] wordvec=vecMap.get(wordPos);
       if(wordvec!=null){
    	   float len=0;
    	   for(int i=0;i<wordvec.length;i++){
    		   len+=wordvec[i]*wordvec[i];
    	   }
    	   len=(float)Math.sqrt(len);
    	   for(int j=0;j<wordvec.length;j++){
    		   wordvec[j]=wordvec[j]/len;
    	   }
	        	 //迭代整个词典计算Distance  
	           Iterator<Entry<String, float[]>> iter=vecMap.entrySet().iterator();
	           Map.Entry<String, float[]> entry;
	           float[] f;	     
	           String vectorWord;
	           while(iter.hasNext()){
	        	         //词距离
	        	         float dist=0;
	        	         //该向量词	        	         
	                     entry=iter.next();
	                     //System.out.println(entry.getKey());
	                     vectorWord=entry.getKey();
	                     if (vectorWord.equals(wordPos)) continue;
	                     if(!vectorWord.equals("</s>")){
	                    	 //System.out.println(vectorWord.split("/").length);
	                    	 if(vectorWord.split("/").length<=1) continue;
		                     if(posNum!=this.getPOS(vectorWord.split("/")[1])) continue; //&&this.getPOS(vectorWord.split("/")[1])!=-1
		                     if(vectorWord.split("/")[1].equals("un")) continue;
	                     }	                     
	        	         f=entry.getValue();
	        	         if(wordvec.length==f.length){
	        	        	 for(int k=0;k<wordvec.length;k++){
	        	        		 dist+=wordvec[k]*f[k];
	        	        	 }
	        	         }else
	        	        	 System.out.println("词 "+wordPos+" ，"+entry.getKey()+"向量读取错误");
	        	         //加入最优词与Distance集合
	        	         for(int m=0;m<N;m++){
	        	        	 if(dist>bestd[m]){
	        	        		 for(int d=(int)N-1;d>m;d--){
	        	        			 bestd[d]=bestd[d-1];
	        	        			 best_word[d]=best_word[d-1];
	        	        		 }
	        	        		 bestd[m]=dist;
	        	        		 best_word[m]=vectorWord;
	        	        		 break;
	        	        	 }
	        	         }
	           }
       }else{
    	   System.out.println("Word: "+wordPos+" , out of vocab");
       }
}
	
	public void calculateDistanceByPOS(String wordPos){
		if(!NLPIR.InitState) NLPIR.Init();
		wordPos=wordPos.trim();
        if(wordPos.equals("")){
     	   System.out.println("计算词不能为空！");
     	   return;
        }
        String posInfo=NLPIR.getWordPos(wordPos);
        wordPos=wordPos+"/"+posInfo;
        int posNum=this.getPOS(posInfo);
        System.out.println("pos : "+(char)posNum);
	    //重新初始化bestd数组与best_word数组
	    for(int l=0;l<N;l++){
	        	 bestd[l]=(float) -1;
	        	 best_word[l]=null;
	    }
        //取出词word的词向量
        float[] wordvec=vecMap.get(wordPos);
        if(wordvec!=null){
     	   float len=0;
     	   for(int i=0;i<wordvec.length;i++){
     		   len+=wordvec[i]*wordvec[i];
     	   }
     	   len=(float)Math.sqrt(len);
     	   for(int j=0;j<wordvec.length;j++){
     		   wordvec[j]=wordvec[j]/len;
     	   }
	        	 //迭代整个词典计算Distance  
	           Iterator<Entry<String, float[]>> iter=vecMap.entrySet().iterator();
	           Map.Entry<String, float[]> entry;
	           float[] f;	     
	           String vectorWord;
	           while(iter.hasNext()){
	        	         //词距离
	        	         float dist=0;
	        	         //该向量词	        	         
	                     entry=iter.next();
	                     //System.out.println(entry.getKey());
	                     vectorWord=entry.getKey();
	                     if (vectorWord.equals(wordPos)) continue;
	                     if(!vectorWord.equals("</s>")){
		                     if(posNum!=this.getPOS(vectorWord.split("/")[1])&&this.getPOS(vectorWord.split("/")[1])!=-1) continue;
	                     }
	        	         f=entry.getValue();	        	         	        	         
	        	         if(wordvec.length==f.length){	        	          	 
	        	        	 for(int k=0;k<wordvec.length;k++){
	        	        		 dist+=wordvec[k]*f[k];
	        	        	 }	        	        
	        	         }else
	        	        	 System.out.println("词 "+wordPos+" ，"+entry.getKey()+"向量读取错误");
	        	         //加入最优词与Distance集合
	        	         for(int m=0;m<N;m++){
	        	        	 if(dist>bestd[m]){
	        	        		 for(int d=(int)N-1;d>m;d--){
	        	        			 bestd[d]=bestd[d-1];
	        	        			 best_word[d]=best_word[d-1];
	        	        		 }
	        	        		 bestd[m]=dist;
	        	        		 best_word[m]=vectorWord;
	        	        		 break;
	        	        	 }
	        	         }
	           }
        }else{
     	   System.out.println("Word: "+wordPos+" , out of vocab");
        }
}
	/**
	 * 计算大于distValue的word相近词Distance集合
	 * */
	public Map<String,Float> calculateDistance(float distValue,String word){
		       word=word.trim();
	           if(word.equals("")){
	        	   System.out.println("计算词不能为空！");	        	   
	        	   return null;
	           }	           
	  	         //重新初始化bestd数组与best_word数组
	  	         for(int l=0;l<N;l++)   { 
	  	        	 bestd[l]=(float) -1;
	  	        	 best_word[l]=null;
	  	        }
	  	       Map<String,Float>  wordVector=new LinkedHashMap<String,Float>();
	           //取出词word的词向量
	           float[] wordvec=vecMap.get(word);	          
	           if(wordvec!=null){
	        	   float len=0;
	        	   for(int i=0;i<wordvec.length;i++){
	        		   len+=wordvec[i]*wordvec[i];
	        	   }
	        	   len=(float)Math.sqrt(len);
	        	   for(int j=0;j<wordvec.length;j++){
	        		   wordvec[j]=wordvec[j]/len;
	        	   }
		        	 //迭代整个词典计算Distance  
		           Iterator<Entry<String, float[]>> iter=vecMap.entrySet().iterator();
		           Map.Entry<String, float[]> entry;
		           float[] f;	     
		           String vectorWord;
		           //声明ArrayList暂时存放结果
		           List<String> wordList=new ArrayList<String>();
		           List<Float>  vectorList=new ArrayList<Float>();
		           while(iter.hasNext()){
		        	         //词距离
		        	         float dist=0;
		        	         //该向量词	        	         
		                     entry=iter.next();
		                     //System.out.println(entry.getKey());
		                     vectorWord=entry.getKey();
		                     if (vectorWord.equals(word)) continue;
		                     
		        	         f=entry.getValue();	        	         	        	         
		        	         if(wordvec.length==f.length){	        	          	 
		        	        	 for(int k=0;k<wordvec.length;k++){
		        	        		 dist+=wordvec[k]*f[k];
		        	        	 }	        	        
		        	         }else{
		        	        	 System.out.println("词 "+word+" ，"+entry.getKey()+"向量读取错误");
		        	         }
		        	       //大于给定值，加入list
		        	         if(dist>distValue){
	                                 wordList.add(vectorWord);
		        	        	     vectorList.add(dist);	        	        	 
		        	        }	        	          
		           }
		           //排序
		           int aSize=0;
		           if(wordList.size()==vectorList.size()) {
		                  aSize=wordList.size();
		           }else  
		        	   System.out.println("词向量分配出错！");
		           String[] wordsort=new String[aSize];
		           float[]  vectorsort=new float[aSize];
		           //重新初始化vectorsort排序数组
		           for(int i=0;i<aSize;i++)     vectorsort[i]=(float)-1;
		         //重新排序结果集合
		           float dist=0;
		           String sword;
		           for(int i=0;i<aSize;i++){
		        	   dist= vectorList.get(i);
		        	   sword=wordList.get(i);
		        	   for(int h=0;h<aSize;h++){
			                if(dist>vectorsort[h]){
	      	        		         for(int d=(int)aSize-1;d>h;d--){
	      	        			             vectorsort[d]=vectorsort[d-1];
	      	        			             wordsort[d]=wordsort[d-1];
	      	        		          }
	      	        		          vectorsort[h]=dist;
	      	        		          wordsort[h]=sword;
	      	        		         break;     	        	 
			        	    }
			            }		            
		           }
		           for(int i=0;i<aSize;i++){
		        	   wordVector.put(wordsort[i], vectorsort[i]);
		           }
		           wordList.clear();
		           vectorList.clear();
		           return wordVector;
	           }else{
	        	   System.out.println("Word: "+word+" , out of vocab");
	        	   return null;
	           }
	}
	/**
	 * 向量计算  word1-word2+word3=word4;  word1与word2的关系,推测word3与word4的关系.
	 * */
	public void distanceAnalogy(String word1,String word2,String word3){
		word1=word1.trim();
		word2=word2.trim();
		word3=word3.trim();
		if(word1.equals("")){			 
			     System.out.println("输入词不能为空！");
			     return;
		}
		if(word2.equals("")){
	    	     System.out.println("输入词不能为空！");
                 return;
	     }
   	    if(word3.equals("")){
		         System.out.println("输入词不能为空！");
		         return;
	     }
		float[] f1=vecMap.get(word1);
		float[] f2=vecMap.get(word2);
		float[] f3=vecMap.get(word3);
		if(f1==null){
			System.out.println("Word: "+word1+" , out of vocab");
			return;
		}if(f2==null){
			System.out.println("Word: "+word2+" , out of vocab");
			return;
		}if(f3==null){
			System.out.println("Word: "+word3+" , out of vocab");
			return;
		}
		//重新初始化bestd数组与best_word数组
	     for(int l=0;l<N;l++)   { 
	        	 bestd[l]=(float) -1;
	        	 best_word[l]=null;
	      }

		 if(f1.length!=f2.length&&f1.length!=f3.length){
			 System.out.println("词向量读取出错！");
		     return;
		}
		float[] wordvec=new float[(int)layerSize];
		for(int i=0;i<(int)layerSize;i++)    wordvec[i]=f2[i]-f1[i]+f3[i];
       //计算向量运算
      	float len=0;
      	for(int i=0;i<wordvec.length;i++){
      		   len+=wordvec[i]*wordvec[i];
      	 }
      	len=(float)Math.sqrt(len);
      	for(int j=0;j<wordvec.length;j++){
      		   wordvec[j]=wordvec[j]/len;
      	}
      	 //迭代整个词典计算Distance  
         Iterator<Entry<String, float[]>> iter=vecMap.entrySet().iterator();
         Map.Entry<String, float[]> entry;
         float[] f;	     
         String vectorWord;
         while(iter.hasNext()){
      	         //词距离
      	         float dist=0;
      	         //该向量词	        	         
                 entry=iter.next();
                 //System.out.println(entry.getKey());
                 vectorWord=entry.getKey();
                 if (vectorWord.equals(word1)) continue;
                 if (vectorWord.equals(word2)) continue;
                 if (vectorWord.equals(word3)) continue;
      	         f=entry.getValue();	        	         	        	         
      	         if(wordvec.length==f.length){	        	          	 
      	        	 for(int k=0;k<wordvec.length;k++){
      	        		 dist+=wordvec[k]*f[k];
      	        	 }	        	        
      	         }else
      	        	 System.out.println("词 "+word1+" ，"+word2+" , "+word3+"  "+"向量计算错误！");
      	         //加入最优词与Distance集合
      	         for(int m=0;m<N;m++){
      	        	       if(dist>bestd[m]){
      	        		           for(int d=(int)N-1;d>m;d--){
      	        			               bestd[d]=bestd[d-1];
      	        			               best_word[d]=best_word[d-1];
      	        		           }
      	        		           bestd[m]=dist;
      	        		           best_word[m]=vectorWord;
      	        		           break;
      	        	       }
      	        	 }
      	         }	        	         
	}
    /**
     * 获取计算结果集合
     * @return
     */
	public Map<String,Float> getResultMap(){
		Map<String,Float> result=new LinkedHashMap<String,Float>();
		if(best_word[0]==null) return null;
		if(bestd.length==best_word.length){
			for(int i=0;i<N;i++){
				if(best_word[i]==null) break;
				result.put(best_word[i], bestd[i]);
			}
		}else{
			System.out.println("计算结果集出错！");
			return null;
		}
		return result;
	}
	public void Convert2Binary(String cache_file){
		FileOutputStream fw = null;
		ObjectOutputStream ow=null;
		try {
			fw=new FileOutputStream(cache_file);
			ow=new ObjectOutputStream(fw);
			ow.writeObject(vecMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ow.close();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * 词性标注
	 */
	public void POSTaging(){
		if(!NLPIR.Init()) return;
		Map<String,float[]> newMap=new LinkedHashMap<String,float[]>();
		for(Entry<String,float[]> entry:vecMap.entrySet()){			
			String word=entry.getKey();
			if(word.equals("</s>")){
				newMap.put(word, entry.getValue());
				continue;
			}
			word+="/"+NLPIR.getWordPos(entry.getKey());
			newMap.put(word, entry.getValue());
		}
		vecMap.clear();
		vecMap=newMap;
	}
	
	/**
	 * 序列化Object
	 * @param cache_file
	 * @param o
	 */
	private void SaveObject(String cache_file,Object o){
		FileOutputStream fw = null;
		ObjectOutputStream ow=null;
		try {
			fw=new FileOutputStream(cache_file);
			ow=new ObjectOutputStream(fw);
			ow.writeObject(o);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ow.close();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
    public void SaveClasses(String Out_File,Map<String,Integer> result){
  	  FileWriter wr = null;
  	  BufferedWriter buff=null;
  	  String str;
  	  try {
			 wr=new FileWriter(Out_File,true);
			 buff=new BufferedWriter(wr);			
			 buff.flush();
			 Iterator<Entry<String, Integer>> it=result.entrySet().iterator();
			 Map.Entry<String, Integer> entry;
			 int i=0;
			 while(it.hasNext()){
				 entry=it.next();
				 str=entry.getKey()+" "+entry.getValue();
				 str+="\n";
				 buff.write(str);
				 i++;
				 if(i%1000==0) buff.flush();
			 }
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			    e.printStackTrace();
		   }finally{
			   try {
				   buff.close();
				   wr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
   }
    /**
     * Map Sort利用Collections的sort函数排序 降序
     * @param map
     * @return
     */
    private Map<String,Integer> SortMapAsValue(Map<String,Integer> map){
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
    	return result;
    }
	/**
	 * 词向量K-Means聚类
	 */
	public void KMeans(String file,int classes,boolean save_text){
		int iter=10,closeid;
		int[] centcn=new int[classes];
		List<Integer> cl=new ArrayList<Integer>();
		float closev,x;
		List<Float[]> cent=new ArrayList<Float[]>();
		List<float[]> vec=new ArrayList<float[]>();
		for(int i=0;i<vocabSize;i++) cl.add(i%classes);
		//开始迭代计算
		for(int i=0;i<iter;i++){
			 System.out.println("第 "+(i+1)+" 次聚类迭代");
			//初始化cent
			for(int j=0;j<classes;j++) {
				Float[] v=new Float[(int)layerSize];
				for(int h=0;h<layerSize;h++) v[h]=(float)0;
				cent.add(v);
			}
			//初始化centcn
			for(int j=0;j<classes;j++) centcn[j]=1;
			//遍历vecMap,转存到list
			Iterator<Entry<String,float[]>> it=vecMap.entrySet().iterator();
			 Map.Entry<String, float[]> entry;
			 while(it.hasNext()){
				 entry=it.next();
				 vec.add(entry.getValue());
			 }
             for(int k=0;k<vocabSize;k++){           	
            	for(int a=0;a<layerSize;a++){
            		cent.get(cl.get(k))[a]+=vec.get(k)[a];
            	}
            	centcn[cl.get(k)]++;
            }
             for(int k=0;k<classes;k++){
            	 closev=0;
            	 for(int a=0;a<layerSize;a++){
            		 cent.get(k)[a]/=centcn[k];
            		 closev+=cent.get(k)[a]*cent.get(k)[a];
            	 }
            	 closev=(float) Math.sqrt(closev);
            	 for(int a=0;a<layerSize;a++) cent.get(k)[a]/=closev;
             }
             //cluster
             for(int k=0;k<vocabSize;k++){
            	 closev=-10;
            	 closeid=0;
            	 for(int a=0;a<classes;a++){
            		 x=0;
            		 for(int b=0;b<layerSize;b++) x+=cent.get(a)[b]*vec.get(k)[b];
            		 if(x>closev){
            			 closev=x;
            			 closeid=a;
            		 }
            	 }
            	cl.set(k, closeid);           	
             }
             System.out.println("已完成: "+((float)i/iter)*100+"%");
		}
		//保存结果
		Map<String,Integer> ClassesResult=new LinkedHashMap<String,Integer>();
		Iterator<Entry<String,float[]>> itor=vecMap.entrySet().iterator();
	    Map.Entry<String, float[]> entry1;
	    for(int i=0;i<vocabSize;i++){
	    	entry1=itor.next();
	    	ClassesResult.put(entry1.getKey(), cl.get(i));
	    }
	    //结果排序
	    ClassesResult= SortMapAsValue(ClassesResult);
	    if(save_text) SaveClasses(file,ClassesResult);
	    else SaveObject(file,ClassesResult);
	    System.out.println("Save Result :  "+System.getProperty("user.dir")+file.substring(1).replaceAll("/", "\\\\"));
	}
	
	/**
	 * 计算平均值
	 * */
	public  double averageResult(){
		double aver=0;
		if(bestd.length ==0){
			return (Double) 0.0;
		}else
			aver= MathMethods.average(bestd);
		return aver;		
	}
	/**
	 * 计算标准差
	 * */
	public  double standardDeviationResult(){
		double aver=0;
		if(bestd.length ==0){
			return (Double) 0.0;
		}else
			aver= MathMethods.standardDeviation(bestd);
		return aver;		
	}
	/**
	 * 计算偏度
	 * */
	public  double skewness(){
		double aver=0;
		if(bestd.length ==0){
			return aver;
		}else
			aver= MathMethods.skewness(bestd);
		return aver;		
	}
		
	public static void main(String[] args) throws IOException{
		//java.text.DecimalFormat df = new java.text.DecimalFormat("0.000000");
		//String vecPath="E:\\Untitled Folder\\结果集\\weibo-cbow-new.bin";
		String vecPath="./weibo2.bin";
		//String cPath="D:\\Word2vec\\w.bin";
		//Distance dis=new Distance(cachePath,true);
		
 		Distance dis=new Distance(20,vecPath,false);

// 		dis.POSTaging();
// 		dis.Convert2Binary("./skipgram.dat");
 		
		dis.distanceAnalogy("中国", "北京", "美国");
		for(Entry<String,Float> entry:dis.getResultMap().entrySet()){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
		//Map<String,float[]> wordMap=dis.vecMap;
		//System.out.println(wordMap.get("中国"));
		/*
		//NLPIR.Init();
		FileWriter wr = null;
	  	BufferedWriter buff=null;
	  	String str;
	  	try {
				 wr=new FileWriter("./wordSementicNet.txt",true);
				 buff=new BufferedWriter(wr);
				 buff.flush();
				 Iterator<Entry<String, float[]>> it=wordMap.entrySet().iterator();
				 Map.Entry<String, float[]> entry;
				 float[] vector=new float[200];
				 int i=0;
				 int num=0;
				 while(it.hasNext()){
					 System.out.println(++num);
					 entry=it.next();
					 str=entry.getKey();
					 //if(!(NLPIR.getWordPos(str).contains("n")&&!NLPIR.getWordPos(str).contains("un")&&!NLPIR.getWordPos(str).contains("v"))||str.contains(".cn")) continue;
					 //System.out.println(str);
					 buff.write(str+"\n");
					 i++;
					 if(i%1000==0) {
						 buff.flush();
						 //break;
					 }
				 }
		} catch (IOException e) {
				// TODO Auto-generated catch block
				    e.printStackTrace();
		}finally{
				   try {
					   buff.close();
					   wr.close();
		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
   
		//String classes="./classes.txt";
		//dis.KMeans(classes, 500, true);
		//dis.Convert2Binary("E:\\Untitled Folder\\结果集\\CBOW(stopw).dat");
		/*
		System.out.println( dis.get_vocabSize());
		System.out.println(dis.get_layerSize());
	    System.out.println(dis.vecMap.size());
	    */
		/*
		String word="✌";
	    dis.calculateDistance(word);
	    Map<String,Float> result=dis.getResultMap();
	    if(result==null)  return;
		  System.out.println("输入词："+word+"      Distance：");
		  Iterator<Entry<String,Float>> iter=result.entrySet().iterator();
		  Entry<String,Float> entry;
		  while(iter.hasNext()){
			  entry=iter.next();
			  if(entry.getKey()!=null)
			      System.out.println(entry.getKey()+"   "+ entry.getValue());
		  } 
     //测试	  
	/*
	  float wordDist=  dis.calculateWords("中国", "美国");
	  System.out.println("中国-美国：Distance："+wordDist);
	  dis.calculateDistance("collectively");
	  System.out.println("输入词：中国      Distance：");
	  for(int i=0;i<dis.best_word.length;i++){
		  System.out.println(dis.best_word[i]+"   "+ dis.bestd[i]);
	  } 
	  System.out.println("输入词：中国     平均值:  "+dis.averageResult());
	  System.out.println("输入词：中国     标准差:  "+dis.standardDeviationResult());
	  System.out.println("输入词：中国     偏度:   "+dis.skewness());
	  
	  dis.distanceAnalogy("中国", "北京", "美国");
	  System.out.println("输入词：中国-北京+美国=      Distance：");
	  for(int i=0;i<dis.best_word.length;i++){
		  System.out.println(dis.best_word[i]+"   "+ dis.bestd[i]);
	  }
	   Map<String,Float> map=   dis.calculateDistance((float)0.7,"双鱼座");
	   System.out.println("输入词：双鱼座      Distance>0.7：");
	    Iterator iter=map.entrySet().iterator();
	    Map.Entry<String, Float> entry;
	    while(iter.hasNext()){
			 entry=(Entry<String, Float>) iter.next();
			  System.out.println(entry.getKey()+"   "+ entry.getValue());
		 }
		 Map<String,Float> map1=   dis.calculateDistance((float)0.4,"音乐");
		  System.out.println("输入词：音乐      Distance > 0.4");
		  Iterator iter1=map1.entrySet().iterator();
		  Map.Entry<String, Float> entry1;
		 while(iter1.hasNext()){
			 entry1=(Entry<String, Float>) iter1.next();
			  System.out.println(entry1.getKey()+"   "+ entry1.getValue());
		  }
		 */
	    
//		//初始化分词
//		  NLPIR.Init();
//		 Map<String,Float> resultPos=new LinkedHashMap<String,Float>();
//		 List<Float> list=new ArrayList<Float>();
//		 Float averge;
//		 double ske;
//		 //System.out.println(dis.calculateWords("王子", "矮人"));
//		 //System.out.println(dis.calculateWords("中国", "国家"));
//	     System.out.print("请输入词:");
//		 String wordin;
//		 String wordPos;
//		 BufferedReader buffer;
//		 while(true){	
//				 buffer=new BufferedReader( new InputStreamReader(System.in));
//				 wordin=new String(buffer.readLine().getBytes(),"utf8");
//				 if(wordin!=null&&wordin!="\n"){
//				       //System.out.println(wordin);
//					    dis.calculateDistance(wordin);
//					    Map<String,Float> result=dis.getResultMap();
//					    wordPos=NLPIR.getWordPos(wordin, 1).trim();
//					    //wordPos=NLPAnalyzer.getWordPos(wordin).trim();
//					    //Map<String,Float> result=dis.calculateDistance(0,wordin);
//					    System.out.println("获取词向量集合完毕。" );
//					    if(result==null)  {
//					    	System.out.println(wordin+" : out of vocab!");
//					    	System.out.print("请输入词:  ");
//					    	continue;
//					    }
//						  System.out.println("输入词："+wordin+"/"+wordPos+"      Distance：");
//						  Iterator<Entry<String,Float>> iter=result.entrySet().iterator();
//						  Entry<String,Float> entry;
//						  while(iter.hasNext()){
//							  entry=iter.next();
//							  //if(entry.getKey()!=null&&NLPIR.getWordPos(entry.getKey(),1).trim().contains("n")){//&&NLPIR.getWordPos(entry.getKey(), 1).trim().contains(wordPos)
//							      System.out.println(entry.getKey()+"/"+NLPIR.getWordPos(entry.getKey(), 1)+"   "+ entry.getValue());//+"/"+NLPIR.getWordPos(entry.getKey(), 1)
//								  //resultPos.put(entry.getKey(),entry.getValue());
//								 // list.add(entry.getValue());
//							  //}
//						  }
//						  /*
//						  Float[] l=new Float[list.size()];
//						  for(int ij=0;ij<list.size();ij++){
//							  l[ij]=list.get(ij);
//						  }	  
//						  ske=MathMethods.skewness(l);
//						  if(ske>0) {
//							  averge=list.get((int)list.size()/2);
//						  }else
//							  averge=(float) MathMethods.average(l);
//						  System.out.println("偏度值：" +ske +"  averge: "+averge+"  结果集大小: "+resultPos.size());
//						  Iterator<Entry<String,Float>> it=resultPos.entrySet().iterator();
//						  while(it.hasNext()){
//							  entry=it.next();
//							  if(entry.getValue()>averge&&entry.getValue()>0.1)
//							  System.out.println(entry.getKey()+"/"+NLPIR.getWordPos(entry.getKey(),1)+"   "+ entry.getValue());
//						  }
//						  */
//						  list.clear();
//						  resultPos.clear();
//						  
//					  System.out.print("请输入词: ");
//			 }
//		 }
		
	}	
} 