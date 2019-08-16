/**
 * @author Hongyan Pan---Phy
 * Date:2014.9.27-----Optimized Memory Allocation,Boosting Training Task. 
 * Date:2014.10.3    Added Multi Threads.
 * Date:2014.10.8   If user used the class Configuration ,and Threads set <=0;the program change it to the processor number.
 * Data:2015.2        Added stopwords to extra the vector space 
 * Optimize: Sentence change to natrual sentence.
 */
package edu.pantek.deeplearning.word2vec;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import edu.pantek.util.ObjectSort;
import edu.pantek.util.Struct;
import edu.pantek.util.mulithread.MulitThreads;
import edu.pantek.util.mulithread.ThreadObject;

public class Word2vec extends ThreadObject {
           /**
           * 默认参数设置
           * */
          final int Exp_Table_Size=100;
          final int Max_Exp=6;
          final int Max_String=50;
          final int Max_Code_Length=40;
          final int Max_Sentence_Length=1000;
          final int Vocab_Hash_Size=30000000;
          /**
           * 词典
           * */
          List<Struct> Vocab;
          /**
           * 训练参数设置
           * */
          private String Train_File;
		  private String Out_File;
          String Save_Vocab_File,Read_Vocab_File;
          int CBow=0;
          int Window=5;
          /**
           * @Mini_Count 排序阈值测试临时改为 1 ,原为5
           * @Mini_Reduce 词典削减时,词频阈值
           */
          int Mini_Count=5; 
          int Num_Threads=1;
          int Threads_Count=0;
          int Min_Reduce=1;
          
          long train_words=0;
          long[] Vocab_Hash;
          int Vocab_Size=0;
          int Layer1_Size=100;
          long word_count_actual=0;
          
          float alpha=(float) 0.025;
          float starting_alpha,sample;
          
          /**
           * NN网络参数
           */
          List<Float[]> Syn0;
          List<Float[]> Syn1;
          List<Float[]> Syn1neg;
          float[] expTable;
          int Debug_Mode=2;
          boolean hs=true;
          int negative=0;
          final int Table_Size=(int) 1e8;
          int[] table;
          
          /**
           * Logger日志类
           */
          private Logger log = Logger.getLogger(Word2vec.class);
          Date begtime;
          Date end;
          /**
           * 构造函数
           * */
          public Word2vec(String train_file,String out_file){
        	  begtime=new Date();
        	  //初始化输入输出
        	  Train_File=train_file;
        	  Out_File=out_file;
        	  //默认参数
        	  if(this.Layer1_Size==100){
        		       // log.info("Loading default Variable......");
        	            this.Layer1_Size=200;
        	  }
        	  this.sample=(float) 1e3;
        	  starting_alpha=alpha;
        	  
        	  Vocab=new ArrayList<Struct>();
        	  Vocab_Hash=new long[Vocab_Hash_Size];
        	  //声明未分配内存
        	  Syn0=new ArrayList<Float[]>();
        	  Syn1=new ArrayList<Float[]>();
        	  Syn1neg=new ArrayList<Float[]>();
          }
          /**
           * 构造函数
           * */
          public Word2vec(String train_file,String out_file,Configuration conf){
        	  begtime=new Date();
        	  //初始化输入输出
        	  Train_File=train_file;
        	  Out_File=out_file;
        	  //加载自定义参数
        	  conf(conf);
        	  Vocab=new ArrayList<Struct>();
        	  Vocab_Hash=new long[Vocab_Hash_Size];
        	  //声明未分配内存
        	  Syn0=new ArrayList<Float[]>();
        	  Syn1=new ArrayList<Float[]>();
        	  Syn1neg=new ArrayList<Float[]>();
          }
          /**
           * 初始化Unigram表
           */
          private void InitUnigramTable(){
        	  //初始化参数调整(优化内存占用问题)
        	  table=new int[Table_Size];
        	  //原程序开始.
        	  long train_words_pow=0;
        	  float d1,power=(float) 0.75;
        	  for(int i=0;i<Vocab_Size;i++) train_words_pow+=Math.pow(Vocab.get(i).cn, power);
        	  int j=0;
        	  d1=(float)Math.pow(Vocab.get(j).cn, power)/(float)train_words_pow;
        	  for(int x=0;x<Table_Size;x++){
        		  table[x]=j;
        		  if(x/(float)Table_Size>d1){
        			  j++;
        			  d1+=Math.pow(Vocab.get(j).cn, power)/(float)train_words_pow;
        		  }
        		  if(j>=Vocab_Size) j=Vocab_Size-1;
        	  }   	  
          }
          /**
           * if hs 
           */
          private void InitExpTable(){
        	  expTable=new float[Exp_Table_Size];
        	  //初始化ExpTable
        	  for(int i=0;i<Exp_Table_Size;i++){
        		  expTable[i]=(float) Math.exp((i/(float)Exp_Table_Size*2-1)*Max_Exp);
        		  expTable[i]=expTable[i]/(expTable[i]+1);
        	  }
          }
          /**
           * 计算词的Hash值
           * */
          private Long GetHash(String word){
        	 Long hash= new Long(0);
        	 Double h=new Double(0);
        	  for(int i=0;i<word.length();i++){
        		  h=h*257+(int)word.charAt(i);
        	  }
        	  hash=(long) (h%Vocab_Hash_Size);
        	  return hash;
          }
          /**
           * 向词典中添加词
           * */
          private int AddToVocab(String word){
        	  long hash=0;
        	  Struct stru=new Struct();
        	  stru.word=word;
        	  stru.cn=0;
        	  stru.code=new long[Max_Code_Length];
        	  stru.code=new long[Max_Code_Length];
        	  Vocab.add(stru);
        	  Vocab_Size++;
        	  hash=GetHash(word);
        	  //System.out.println(Vocab_Hash[(int)hash]);
        	  while(Vocab_Hash[(int)hash]!=-1){
        		  hash=(hash+1)%Vocab_Hash_Size;
        	  }
        	  Vocab_Hash[(int)hash]=Vocab_Size-1;  	  
        	  return Vocab_Size-1;
          }
          /**
           * 查找词典，如果word存在，返回其在词典中的位置，否则返回-1
           * */
          private int SearchVocab(String word){
        	  long hash=GetHash(word);
        	 // System.out.println(word);
        	  for(;hash<Vocab_Hash_Size;){
        		  if(Vocab_Hash[(int)hash]==-1){        			  
        			  return -1;
        		}
        		  if(Vocab.get((int)Vocab_Hash[(int)hash]).word.equals(word)){
        			  return (int)Vocab_Hash[(int)hash];
        		  }
        		  hash=(hash+1)%Vocab_Hash_Size;
        	  }
        	  return -1;
          }
          /**
           * 读取word在vocab中的序号
           */
          private int ReadWordIndex(String word){      	  
        	  return SearchVocab(word);
          }
          /**
           * Reduce词典中不频繁出现的词
           * */
          private void ReduceVocab(){
        	  int b=0;
        	  long hash=0;
        	  List<Struct> vocabNew=new ArrayList<Struct>();
        	  for(int a=0;a<Vocab_Size;a++){
        		  if(Vocab.get(a).cn>Min_Reduce){
        			  Struct wordStr=new Struct();
        			  wordStr.cn=Vocab.get(a).cn;
        			  wordStr.word=Vocab.get(a).word;
        			  vocabNew.add(wordStr);
        			  b++;
        		  }
        	  }
        	  //重新植入数据
        	  Vocab.clear();
        	  Vocab=vocabNew;
        	  //不能清空,list赋值操作,传递了地址,而为进行实质性赋值.
        	  //vocabNew.clear();
        	  Vocab_Size=b;
        	 //重新计算Hash值
        	  for(int i=0;i<Vocab_Size;i++)   Vocab_Hash[i]=-1;
        	  for(int j=0;j<Vocab_Size;j++){
        		  hash=GetHash(Vocab.get(j).word);
        		  while(Vocab_Hash[(int)hash]!=-1){ 
        			  hash=(hash+1)%Vocab_Hash_Size;
        			  }
        		  //hash为word的Hash值,Vocab_Hash存的是该词对应的索引号------词典中的位置号
        		  Vocab_Hash[(int)hash]=j;
        	  }
        	  Min_Reduce++;
          }
          /**
           * 对Vocab进行排序,按词频降序,词频小于count时,该词将被移除
           * */
          private void SortVocab(){
        	  log.info("Sorting the Vocab......");
        	  //int size=0;
        	  long hash=0;
        	  //先除去小于Mini_Count值的词后再排序-----------调整原程序方法
        	  //重置hash值
        	  for(int i=0;i<Vocab_Hash_Size;i++)  Vocab_Hash[i]=-1;
        	  //移除小于min_count值的词,   注意:保持</s>  cn=0  仍在第一位
        	  for(int i=Vocab.size()-1;i>0;i--){
        		  if(Vocab.get(i).cn<Mini_Count){
        			  Vocab.remove(i);
        		  }
        	  }
        	  Vocab_Size=Vocab.size();
        	  train_words=0;
        	  //保持"</s>"在第一位置,降序排列vocab----------------------快速排序
        	  //ObjectSort.quicksort(Vocab, 1, Vocab.size()-1);
        	  //改为冒泡-------待提升效率
        	  //ObjectSort.sortList(Vocab, 1);
        	  //优化改为用Collections中的Sort对vocab排序
        	  Struct stru=Vocab.get(0);
        	  Vocab.remove(0);
        	  ObjectSort.CSort(Vocab);
        	  Vocab.add(0, stru);
        	  //重新计算hash值
        	  for(int i=0;i<Vocab.size();i++){
        		  hash=GetHash(Vocab.get(i).word);
        		  while(Vocab_Hash[(int)hash]!=-1)  hash=(hash+1)%Vocab_Hash_Size;
        		  Vocab_Hash[(int)hash]=i;
        		  train_words+=Vocab.get(i).cn;
        	  }
        	 // System.out.println(Vocab.size());
        	 // log.info("Training Words :"+train_words);
        	  //System.out.println(Vocab.get(1).word+"  "+Vocab.get(1).cn);
          }
          /**
           * 从训练文件中加载词
           * */
          private void LearnVocab(){
        	  log.info("Loading Vocab Resource......");
        	  long pos ,a;
        	  //初始化Vocab_Hash数组
        	  for(int i=0;i<Vocab_Hash_Size;i++)   Vocab_Hash[i]=-1;
        	  //按空格读入词，Scanner扫描
        	  FileInputStream input = null;
        	  Scanner scan = null;
        	  try {
			    input=new FileInputStream(Train_File);
			    scan=new Scanner(input);
				String word;
				Vocab_Size=0;				
				AddToVocab("</s>");
				while(scan.hasNext()){
					word=scan.next();
					//加载词典时,显示进度用
					//train_words++;					
					pos=SearchVocab(word);
					//System.out.println(pos);
					if(pos==-1){
						a=AddToVocab(word);
						Vocab.get((int)a).cn=1;
						System.out.println(a);
					}else {
						Vocab.get((int)pos).cn++;
						//输出词 和词频
						//System.out.println(Vocab.get((int)pos).word+"  "+Vocab.get((int)pos).cn);
					}
					if(Vocab_Size>Vocab_Hash_Size*0.7){
						log.info("Reduce the Vocab!");
						ReduceVocab();
					}				
				}
				SortVocab();
				log.info("TrainFile Words Size: "+train_words+"    Vector Size: "+Vocab.size());
				//System.out.println(Vocab.get(1).word+"  "+Vocab.get(1).cn);
			} catch (FileNotFoundException e) {
				log.info("FileNotFoundException:  not find file--"+Train_File);
				return;
			}finally{
				try {
					scan.close();
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
          }
          /**
           * 创建Binary Huffman tree , 根据Vocab.cn;
           */
          private void BinaryTree(){
        	  log.info("Building Binary Huffman Tree.....");
        	  int min1,min2;
        	  int pos1,pos2;
        	  long[] point=new long[Max_Code_Length];
        	  long[] code=new  long[Max_Code_Length];
        	  long[] count=new long[Vocab_Size*2+1];
        	  long[] binary=new long[Vocab_Size*2+1];
        	  long[] parent_node=new long[Vocab_Size*2+1];
        	  //初始化count数组
        	 // System.out.println(Vocab_Size);
        	  for(int w=0;w<Vocab_Size;w++)  count[w]=Vocab.get(w).cn;
        	  for(int j=Vocab_Size;j<Vocab_Size*2;j++)  count[j]=(long)1e15;
        	  //设置pos
        	  pos1=Vocab_Size-1;
        	  pos2=Vocab_Size;
        	  //开始上构造Huffman Tree
        	  for(int i=0;i<Vocab_Size-1;i++){
        		  //找出两个最小的node
        		  if(pos1>=0){
        			  if(count[pos1]<count[pos2]){
        				  min1=pos1;
        				  pos1--;
        			  }else{
        				  min1=pos2;
        				  pos2++;
        			  }
        		  }else{
        			  min1=pos2;
        			  pos2++;
        		  }
        		  if(pos1>=0){
        			  if(count[pos1]<count[pos2]){
        				  min2=pos1;
        				  pos1--;
        			  }else{
        				  min2=pos2;
        				  pos2++;
        			  }
        		  }else{
        			  min2=pos2;
        			  pos2++;
        		  }
        		 count[Vocab_Size+i]=count[min1]+count[min2];
        		 parent_node[min1]=Vocab_Size+i;
        		 parent_node[min2]=Vocab_Size+i;
        		 binary[min2]=1;
        		  
        	  }
        	  //重新指定词典中词的binary code 
        	  for(int j=0;j<Vocab_Size;j++){
        		  int a=j;
        		  int i=0;
        		  while(true){
        			  code[i]=binary[a];
        			  point[i]=a;
        			  i++;
        			  a=(int)parent_node[a];
        			  if(a==Vocab_Size*2-2) break;
        		  }
        		  Vocab.get(j).codelen=i;
        		  Vocab.get(j).point=new long[Max_Code_Length];
        		  Vocab.get(j).code=new long[Max_Code_Length];
        		  Vocab.get(j).point[0]=Vocab_Size-2;
        		  for(int b=0;b<i;b++){
        			  Vocab.get(j).code[i-b-1]=code[b];
        			  Vocab.get(j).point[i-b]=(point[b]-Vocab_Size);
        		  }
        	  }
          }
          /**
           * 初始化网络参数
           * */
          public void InitNN(){
        	  log.info("Init NN.......");
        	  if(hs){
        		  for(int i=0;i<Vocab_Size;i++){
        			  Float[]  vector=new Float[Layer1_Size];
        			  for(int j=0;j<Layer1_Size;j++)  vector[j]=(float)0;
        			  Syn1.add(vector);
        		  }
        	  }
        	  if(negative>0){
        		  for(int i=0;i<Vocab_Size;i++){
        			  Float[]  vecNeg=new Float[Layer1_Size];
        			  for(int j=0;j<Layer1_Size;j++)  vecNeg[j]=(float)0;
        			  Syn1neg.add(vecNeg);
        		  }
        	  }
        	  for(int i=0;i<Vocab_Size;i++){
    			  Float[]  vecSys0=new Float[Layer1_Size];
    			  for(int j=0;j<Layer1_Size;j++)  vecSys0[j]=(float)Math.random()/Layer1_Size;
    			  Syn0.add(vecSys0);
        	  }
        	  BinaryTree();
          }
          /**
           *读取一个词
           * @param ran
           * @param l
           * @return
           */
 	     public String ReadOne(RandomAccessFile ran,Long l)  {    	   
	    	    String word="";
	    	    int i=0;
	    	    byte temp=' ';
	    	    byte[] tmp=new byte[Max_String];
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
	    	        if(i>=Max_String) break;
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
          /**
           * 训练线程
           */
           protected void ThreadsModel(int id){
        	  long a,b,c,d;
        	  long word=0,last_word,sentence_length=0,sentence_position=0;
        	  long word_count=0,last_word_count=0;
        	  long[] sen=new long[Max_Sentence_Length+1];
        	  @SuppressWarnings("unused")
			  long l1,l2,target,label;
        	  float f,g;
        	  float[] neu1=new float[Layer1_Size];
        	  float[] neu1e=new float[Layer1_Size];
        	  String str="";
        	  //资源分配号------暂时不使用,默认线程1
        	  Long next_random=new Long(id);
        	  Long var=new Long("25214903917");
        	  FileInputStream  file=null;
        	  Scanner scan=null;
        	  RandomAccessFile  ranReader=null;
        	  Long filesize=null;
        	  try{
            	  if(this.Num_Threads==1){     	           
       	                    file=new FileInputStream(Train_File);
       	                    scan=new Scanner(file);
       	           } 
            	  if(this.Num_Threads>1){
            		  ranReader=new RandomAccessFile(Train_File,"r");
            		  filesize=new Long(ranReader.length()/Num_Threads*(id+1)-1);
            		  ranReader.seek(ranReader.length()/Num_Threads*(id));
            	  }
        		  while(true){
        			  if(word_count-last_word_count>10000){
        				  word_count_actual+=word_count-last_word_count;
        				  last_word_count=word_count;
        				  if(Debug_Mode>1){
        					  //输出参数 Eclipse 控制台输出问题，导致不能单行输出
        					  //System.out.println(word_count_actual+"  "+word_count);
        					  System.out.printf("%cAlpha: %f   Progress: %.2f%%",13,alpha,(word_count_actual/((float)train_words+1)*100));
        				  }
        				  //10000词调整一次Alpha值
        				  alpha=starting_alpha*(1-word_count_actual/(float)(train_words+1));
        				  if(alpha<starting_alpha*0.0001) alpha=(float) (starting_alpha*0.0001);
        			  }
        			  if(sentence_length==0){
        				  while(true){        					 
        					  if(Num_Threads==1){
        						   if(!scan.hasNext()) break;
        					       str=scan.next().trim();
        					  }
        					  if(Num_Threads>1){     	            		 
        						   if(ranReader.getFilePointer()>=filesize) break;
        						   str=ReadOne(ranReader,filesize).trim();
        					  }
        					  
        					  if(str.equals(null)&&str.equals("")) continue;
        					  word=ReadWordIndex(str);
        					  //System.out.println(word);
        					  if(word==-1) continue;
        					 // System.out.println(str+"  "+word_count+"    "+word);
        					  word_count++;       					 
        					  if(word==0) break;
        					  //The subsampling randomly discard frequent words while words while keeping the ranking same 
        					  if(sample>0){
        						  float ran=(float) ((Math.sqrt(Vocab.get((int)word).cn/(sample*train_words))+1)*(sample*train_words)/Vocab.get((int)word).cn);
        						  next_random=next_random*var+11;
        						  if(ran<(next_random&0xFFFF)/(float)65535) continue;
        					  }
        					  sen[(int)sentence_length]=word;
        					  sentence_length++;
        					  //Max_Sentence_Length=1000;
        					  if(sentence_length>=Max_Sentence_Length) break;
        				  }  
        				// System.out.println(word_count);
        				 sentence_position=0;
        			  }
        			  if(Num_Threads==1){
						   if(!scan.hasNext()) break;
        			  }
        			  if(Num_Threads>1){     	            		 
						   if(ranReader.getFilePointer()>=filesize) break;
        			  }
        			  //应该为>=否则出现死循环
        			 // if(word_count>=train_words/Num_Threads) break;
        			  //此处形成了死循环
        			  //System.out.println("here");
        			  word=sen[(int) sentence_position];
        			  if(word==-1) continue;
        			  for(c=0;c<Layer1_Size;c++) neu1[(int)c]=0;
        			  for(c=0;c<Layer1_Size;c++) neu1e[(int)c]=0;
        			  next_random=next_random*var+11;
        			  b=next_random%Window;
        			  if(CBow==1){
        				  //CBOW语言模型
        				  //输入层－－＞隐含层
        				  for(a=b;a<Window*2+1-b;a++){
        					  if(a!=Window){
        						  c=sentence_position-Window+a;
        						  if(c<0) continue;
        						  if(c>sentence_length) continue;
        						  last_word=sen[(int) c];
        						  if(last_word==-1) continue;
        						  for(c=0;c<Layer1_Size;c++)   neu1[(int)c]+=Syn0.get((int) c)[(int)c];
        					  }
        				  }
        				  if(hs){
        					  for(d=0;d<Vocab.get((int) word).codelen;d++){
        						  f=0;
        						  l2=Vocab.get((int)word).point[(int)d]*Layer1_Size;
        						  //隐含层到输出层
        						  for(c=0;c<Layer1_Size;c++)  f+=neu1[(int)c]*Syn1.get((int) Vocab.get((int)word).point[(int)d])[(int)c];
        						  if(f<=-Max_Exp) continue;
        						  else if(f>=Max_Exp) continue;
        						  else f=expTable[(int)((f+Max_Exp)*(Exp_Table_Size/Max_Exp/2))];
        						  //梯度下降　学习速率
        						  g=(1-Vocab.get((int)word).code[(int)d]-f)*alpha;
        					  }
        				  }
        				  //Negative Sample
        				  if(negative>0){
        					  for(d=0;d<negative+1;d++){
        						  if(d==0){
        							  target=word;
        							  label=1;
        						  }else{
        							  next_random=next_random*var+11;
        						 	  target=table[(int) ((next_random>>16)%Table_Size)];
        						 	  if(target==0) target=next_random%(Vocab_Size-1)+1;
        						 	  if(target==word) continue;
        						 	  label=0;
        						  }
        						  l2=target*Layer1_Size;
        						  f=0;
        						  for(c=0;c<Layer1_Size;c++) f+=neu1[(int)c]*Syn1neg.get((int)target)[(int)c];
        						  if(f>Max_Exp) g=(label-1)*alpha;
        						  else if(f<-Max_Exp) g=(label-1)*alpha;
        						  else g=(label-expTable[(int)((f+Max_Exp)*(Exp_Table_Size/Max_Exp/2))])*alpha;
        						  for(c=0;c<Layer1_Size;c++) neu1e[(int)c]+=g*Syn1neg.get((int)target)[(int)c];
        						  for(c=0;c<Layer1_Size;c++) Syn1neg.get((int)target)[(int)c]+=g*neu1[(int)c];
        					  }
        				  }
        				  //hiden->in
        				  for(a=b;a<Window*2+1-b;a++){
        					  if(a!=Window){
        						  c=sentence_position-Window+a;
        						  if(c<0) continue;
        						  if(c>=sentence_length) continue;
        						  last_word=sen[(int)c];
        						  if(last_word==-1) continue;
        						  for(c=0;c<Layer1_Size;c++)    Syn0.get((int)last_word)[(int)c]+=neu1e[(int)c];
        					  }
        				  }
        			  }else{
        				  //skip-gram
        				  for(a=b;a<Window*2+1-b;a++){
        					  if(a!=Window){
        						  c=sentence_position-Window+a;
        						  if(c<0) continue;
        						  if(c>=sentence_length) continue;
        						  last_word=sen[(int)c];
        						  if(last_word==-1) continue;
        						  l1=last_word*Layer1_Size;
        						  for(c=0;c<Layer1_Size;c++) neu1e[(int)c]=0;
        						  //Hierarchical Softmax回归函数
        						  if(hs){
        							  for(d=0;d<Vocab.get((int)word).codelen;d++){
        								  f=0;
        								  l2=Vocab.get((int)word).point[(int)d]*Layer1_Size;
        								  //hidden---->output
        								  for(c=0;c<Layer1_Size;c++)  f+=Syn0.get((int)last_word)[(int)c]*Syn1.get((int)Vocab.get((int)word).point[(int)d])[(int)c];
        								  if(f<=-Max_Exp) continue;
        								  else if(f>=Max_Exp) continue;
        								  else  f=expTable[(int)((f+Max_Exp)*(Exp_Table_Size/Max_Exp/2))];
        								  //g
        								  g=(1-Vocab.get((int)word).code[(int)d]-f)*alpha;
        								  //errors output--->hidden
        								  for(c=0;c<Layer1_Size;c++) neu1e[(int)c]+=g*Syn1.get((int)Vocab.get((int)word).point[(int)d])[(int)c];
        								  //学习权重hidden--->output
        								  for(c=0;c<Layer1_Size;c++) Syn1.get((int)Vocab.get((int)word).point[(int)d])[(int)c]+=g*Syn0.get((int)last_word)[(int)c];     								  
        							  }
        						  }
        						  //Negative Sample
        						  if(negative>0){
        							  for(d=0;d<negative+1;d++){
        								  if(d==0){
        									  target=word;
        									  label=1;
        								  }else{
        									  next_random=next_random*var+11;
        									  target=table[(int) ((next_random>>16)%Table_Size)];
        									  if(target==0) target=next_random%(Vocab_Size-1)+1;
        									  if(target==word) continue;
        									  label=0;
        								  }
        								  l2=target*Layer1_Size;
        								  f=0;
        								  for(c=0;c<Layer1_Size;c++) f+=Syn0.get((int)last_word)[(int)c]*Syn1neg.get((int)target)[(int)c];
        								  if(f>Max_Exp) g=(label-1)*alpha;
        								  else if(f<-Max_Exp) g=(label-0)*alpha;
        								  else g=(label-expTable[(int)((f+Max_Exp)*(Exp_Table_Size/Max_Exp/2))])*alpha;
        								  for(c=0;c<Layer1_Size;c++)  neu1e[(int)c]+=g*Syn1neg.get((int)target)[(int)c];
        								  for(c=0;c<Layer1_Size;c++) Syn1neg.get((int)target)[(int)c]+=g*Syn0.get((int)last_word)[(int)c];
         							  }
        						  }
        						  //学习权重hidden--->output
        						  for(c=0;c<Layer1_Size;c++) Syn0.get((int)last_word)[(int)c]+=neu1e[(int)c];
        					  }
        				  }
        			  }
        			  sentence_position++;
        			  if(sentence_position>=sentence_length){
        				  sentence_length=0;
        				  continue;
        			  }
        		  } 
        	  }catch(IOException e){
        		  this.Threads_Count=0;
        		  System.out.print("读取文件:"+Train_File+" 失败");        		  
        	  }finally{
        		  try {//注意线程同步
        			  if(this.Num_Threads==1) {
        				  scan.close();
        				  file.close();
        				  end=new Date();
        			  }
				      if(this.Num_Threads>1){        			        			  
        			    ranReader.close();
        			    end=new Date();
				      }    			  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	  }//finally结束   
    		  this.Threads_Count-=1;
        	  if(this.Threads_Count==0)   SaveVector();   
          }//thread结束
          /**
           * 以文本形式保存
           */
          private void SaveVector(){
        	  log.info("Saving Vector Result File......");        	  
        	  FileWriter wr = null;
        	  BufferedWriter buff=null;
        	  String str;
        	  try {
				 wr=new FileWriter(Out_File,true);
				 buff=new BufferedWriter(wr);
				 buff.write(Vocab_Size+" "+Layer1_Size+"\n");
				 buff.flush();
				 for(int i=0;i<Vocab_Size;i++){
					 str=Vocab.get(i).word;
					 for(int j=0;j<Layer1_Size;j++){
						 str+=" "+Syn0.get(i)[j];
					 }
					 str+="\n";
					 buff.write(str);
					 if(i%1000==0) buff.flush();
				 }
				 log.info("Saved Vector Result File!"); 
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
        	  log.info("Stoped training ,Using file: "+Train_File+" Result Vec file:"+Out_File+ " Successfully!");
        	  log.info("Vector count: "+Vocab_Size+" NN Layer Size: "+Layer1_Size+" Timecost: "+(end.getTime()-begtime.getTime())/1000+" s");
          }
          /**
           * 缓存数据,以便增量训练
           * Vocab   Vocab_hash  Syn0 Syn1 Syn1neg
           */
          @SuppressWarnings("unused")
		private void SaveObject(){
             FileOutputStream  fw=null;
             ObjectOutputStream obj=null;
			 try {
				fw=new FileOutputStream("./data");
				obj = new ObjectOutputStream(fw);
				obj.writeObject(Word2vec.class);
			 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}             
         }
          /**
           * 多线程
           */
          private void ThreadsExcute(int num_t,Word2vec vec){
        	  log.info("Entering TrainModelThread,Starting Training by "+num_t+" Threads........");
        	  MulitThreads.MulitExcute(num_t,vec);	
          }
          /**
           * 加载参数类
           * @param con
           */
          private void conf(Configuration con){
        	  log.info("Loading Variables......");
        	  this.CBow=con.CBow;
        	  if(this.CBow==0)  log.info("CBow Used Default Varable!");
        	  if(this.CBow>=2||this.CBow<0){
        		  log.info("CBow Num can't be the number, except 0 and 1, System auto reset it to 0");
        		  this.CBow=0;
        	  }
        	  this.Window=con.Window;
        	  if(this.Window==5)  log.info("Window Used Default Varable!");
        	  if( this.Window<=0){
        		  log.info("Window Num can't be the number, System auto reset it to 5");
        		  this.Window=5;
        	  }
        	  this.Mini_Count=con.Mini_Count;
        	  if(this.Mini_Count==5)  log.info("Mini_Count Used Default Varable!");
        	  if(  this.Mini_Count<=0){
        		  log.info("Mini_Count Num can't be the number, System auto reset it to 5");
        		  this.Mini_Count=5;
        	  }
        	  this.Min_Reduce=con.Min_Reduce;
        	  if(this.Min_Reduce==1)  log.info("MiniReduce Used Default Varable!");
        	  if( this.Min_Reduce<=0){
        		  log.info("Min_Reduce Num can't be the number, System auto reset it to 1");
        		  this.Min_Reduce=1;
        	  }
        	  this.Num_Threads=con.Num_Threads;
        	  this.Threads_Count=con.Num_Threads;
        	  if(this.Num_Threads==1)  log.info("Threads num Used Default Varable!");
        	  if(this.Num_Threads<=0){
        		  log.info("Threads Num can't be o or - ,System auto reset NumThreads to System's Processors number");
        		  this.Num_Threads=Runtime.getRuntime().availableProcessors();
        		  this.Threads_Count=this.Num_Threads;
        	  }
        	  this.Layer1_Size=con.Layer1_Size;
        	  if(this.Layer1_Size<=0){
        		  log.info("Layer_Size Num can't be o or - ,System auto reset to 200");
        		  this.Layer1_Size=200;
        	  }
        	  this.hs=con.hs;
        	  this.negative=con.negative;
        	  //默认参数
        	  if(this.Layer1_Size==100){
        		        log.info("Loading default Variable......");
        	            this.Layer1_Size=200;
        	  }
        	  this.sample=(float) 1e3;
        	  starting_alpha=alpha;
          }
          /**
           * 训练任务
           */
          public void train(){      	  
        	  log.info("Start to train  file: "+Train_File);
        	  LearnVocab();
        	  InitNN();
        	  if(negative>0) InitUnigramTable();
        	  //调整内存分配
        	  InitExpTable();
        	  //ThreadsModel(0);
        	  ThreadsExcute(Num_Threads, this);
        	 // SaveVector();
        	  
          }
          public static void main(String[] args){
        	  Configuration conf=new Configuration();
        	  conf.setCBOW(0);
        	  conf.setThreads(2);
        	  conf.setMiniCount(0);
        	  //Word2vec vectest=new Word2vec(args[0],args[1],conf);
        	  Word2vec vectest=new Word2vec("text8","./skipgram.bin",conf);
        	  vectest.train();
          }
}
