package edu.pantek.deeplearning.nlp;

public class SimilarityCalculate {
      
	/**
	 * Levenshtein Distance or Edit Distance
	 * @param word1
	 * @param word2
	 * @return word1 and word2's distance
	 */
	public static float LSDistance(String word1,String word2){
		float distance;
		int w1L=word1.length();
		int w2L=word2.length();
		int[] w1=new int[w1L+1];
		int[] w2=new int[w2L+1];

		int maxLength=Math.max(word1.length(), word2.length());
		int[][] disMatrix=new int[w2L+1][w1L+1];

		w1[0]=0;
		w2[0]=0;

		//用内码初始化w1
		for(int w=1;w<w1L+1;w++){
			w1[w]=(int)word1.charAt(w-1);
		}
		//用内码初始化w2
		for(int w=1;w<w2L+1;w++){
			w2[w]=(int)word2.charAt(w-1);
		}

		//初始化第一行
		for(int i=0;i<w1L+1;i++){
			disMatrix[0][i]=i;
		}
		//初始化第一列
		for(int j=0;j<w2L+1;j++){
			disMatrix[j][0]=j;
		}
		//相似矩阵计算
		int dis;
		for(int i=1;i<w1L+1;i++){
			for(int j=1;j<w2L+1;j++){
				if(w1[i]==w2[j]) dis=disMatrix[j-1][i-1]+0;
				else dis=disMatrix[j-1][i-1]+1;
				disMatrix[j][i]=Math.min(Math.min(disMatrix[j-1][i]+1, disMatrix[j][i-1]+1), dis);
				//System.out.println(disMatrix[j][i]);
			}
		}
		//System.out.println(disMatrix[w2L][w1L]);
		for(int i=1;i<w1L+1;i++){
			for(int j=1;j<w2L+1;j++){
				System.out.print(disMatrix[j][i]);
			}
			System.out.println();
		}
		distance=1-(float)disMatrix[w2L][w1L]/maxLength;
		return distance;
	}
	/**
	 * 
	 * @return
	 */
	public static float cosinDistance(String word1,String word2){
		float distance=0;
		int w1L=word1.length();
		int w2L=word2.length();
		int[] vec1=new int[w1L];
		int[] vec2=new int[w2L];
		//初始化向量
		for(int i=0;i<w1L;i++){
			vec1[i]=(int)word1.charAt(i);
		}
		for(int j=0;j<w2L;j++){
			vec2[j]=(int)word2.charAt(j);
		}
		int a=0;
		for(int i=0;i<w1L;i++){
			a+=vec1[i]*vec1[i];
		}
		int b=0;
		for(int j=0;j<w2L;j++){
			b+=vec2[j]*vec2[j];
		}
		int sum=0;
		for(int i=0;i<w1L;i++){
			sum+=vec1[i]*vec2[i];
		}
		
		distance=(float) ((float)sum/(Math.sqrt(a)*Math.sqrt(b)));
		return distance;
	}
	public static void main(String[] args){
		String w="我是中国人";
		String y="我是科大人";
		System.out.println(LSDistance(w,y));
	}
}
