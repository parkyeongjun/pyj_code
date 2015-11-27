package pyj_nlp;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class xmlparser { 
	public static void main(String args[]) throws IndexOutOfBoundsException
	{
		    try {
		      BufferedReader am = new BufferedReader(new FileReader("pall.txt"));
		      String a = "";
		      //FileWriter fw = new FileWriter("pall.txt");
		      int i =0;
		      String year = "";
		      //fw.write("\n<yjyear>2015\n");
		      while((a = am.readLine()) != null) 
		      {     
		    	  
//		    	  if(a.contains("<p>"))
//		    	  {
//		    		  a = a.replaceAll("<[^>]*>", " ");
//		    		  if(a.equals("")==false)
//		    			  fw.write("ㅁyearck"+year+"ㅁartnck"+i+"ㅁ"+a+"\n");
//		    	  }
//		    	  else if(a.contains("<front>"))
//		    	  {
//		    		  i++;
//		    	  }
//		    	  else if(a.contains("<yjyear>"))
//		    	  {
//		    		  year = a.substring(8, 12);
//		    	  }
//		    	  fw.write(a+"\n");
		    	  System.out.println(a);
		      }
		      
		      am.close();
		      
		    } 
		      catch (IOException e) {
		        System.err.println(e); // ?????? ???? ????? ???
		        System.exit(1);

		    }
	  }
	
}
