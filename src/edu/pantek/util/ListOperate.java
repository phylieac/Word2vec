package edu.pantek.util;

import java.util.ArrayList;
import java.util.List;



public class ListOperate {

	/*返回第二个List与第一个不同的数*/
	//字符串比较时使用equal函数，比较内容，==比较的是对象
	public static List<String> ListCompare(List<String> list1,List<String> list2){
		List<String> list = new ArrayList<String>();
		for(int i=0;i<list2.size();i++){
			String x=list2.get(i).replaceAll("\\s", "");
			for(int j=0;j<list1.size();j++){
				String y=list1.get(j).replaceAll("\\s","");				
				if(!x.equals(y)){
					list.add(x);
				}
			}
		}
		return list;
	}
	public static void main(String [] args){
		List<String> list1=new ArrayList<String>();
		List<String> list2=new ArrayList<String>();
		list1.add("zhongguo");
		list2.add("shijie");
		List<String> test=ListCompare(list1,list2);
		if(test!=null){
			for(String i:test){
				System.out.println(i);
			}
		}else
			System.out.println("List is null ");
			
		String str="中国    时间    ";
		System.out.println(str.replaceAll("\\s",""));
	}
		
}
