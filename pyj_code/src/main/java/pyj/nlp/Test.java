package pyj.nlp;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class Test {
   public static void main(String args[]) throws IndexOutOfBoundsException, IOException,java.lang.IllegalArgumentException
   {

      
	   String a = "I love you \n am a boy";
	   a = a.replace("\n","\\n");
	   System.out.println(a);
//	   for(int i = 0 ; i < b.length ; i++)
//	   {
//		   String temp = b[i];
//		   int ngram = 2;
//		   String ngram_set ="";
//
//			for(int j = i - ngram ; j < i ; j ++)
//			{
//				   if(i - ngram < 0)
//				   {continue;}
//				   ngram_set += b[j] + " ";
//					if (ngram_set.contains("http://") || ngram_set.contains("\n")) 
//					{
//						ngram_set ="";
//						continue;
//					}
//				
//				System.out.println(ngram_set);
//			} // n그램 계산 부분.
//		   
//		   int c = ngram_set.split(" ").length;
//			
//	   }
   }
}