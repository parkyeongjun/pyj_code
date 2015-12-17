package pyj.hadoop;


/////PLoS Biology_20150903.zip,PLoS Computational Biology_20150903.zip등이 들어있는 폴더에서 바디부분을 불러와서
/////parsed_xml이라는 ㅎ나의 파잉료 바꿔준다
/////parsed_xml의 형식은 아래와 같다.

/*
year:2003 journal_no:0             This project is part of an ongoing multicentre collaboration on elephant conservation. All authors on this manuscript contributed substantively to the work described herein. 
year:2003 journal_no:0           The authors have declared that no conflicts of interest exist. 
year:2003 journal_no:1          The origin of Borneo's elephants is controversial. Two competing hypotheses argue that they are either indigenous, tracing back to the Pleistocene, or were introduced, descending from elephants imported in the 16thâ18th centuries. Taxonomically, they have either been classified as a unique subspecies or placed under the Indian or Sumatran subspecies. If shown to be a unique indigenous population, this would extend the natural species range of the Asian elephant by 1300 km, and therefore Borneo elephants would have much greater conservation importance than if they were a feral population. We compared DNA of Borneo elephants to that of elephants from across the range of the Asian elephant, using a fragment of mitochondrial DNA, including part of the hypervariable d-loop, and five autosomal microsatellite loci. We find that Borneo's elephants are genetically distinct, with molecular divergence indicative of a Pleistocene colonisation of Borneo and subsequent isolation. We reject the hypothesis that Borneo's elephants were introduced. The genetic divergence of Borneo elephants warrants their recognition as a separate evolutionary significant unit. Thus, interbreeding Borneo elephants with those from other populations would be contraindicated in ex situ conservation, and their genetic distinctiveness makes them one of the highest priority populations for Asian elephant conservation. 
year:2004 journal_no:2          Comparison between DNA sequences of Borneo elephants with those of other Asian elephants settles a longstanding dispute about the origins of these endangered animals. 
*///년도와 몇번째 논문인지 정보를 담고있다.

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DataHandler {
	public static void main(String args[]) throws IndexOutOfBoundsException, NullPointerException {
		
		parseXmls();

	}

	public static void printf(String s) {

		System.out.printf(s);
		
	}
	
	
	
	public static void parseXmls()
	{
		try {
			int journal_no = 0;
			File[] dataFiles = new File(Path.JOURNAL_DIR).listFiles();
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path.XML_PARSING_TEXT_FILE), "UTF8"));
			for (int i = 0; i < dataFiles.length; i++) // 개수 zip개수인 6개가될것이다.
			{
				printf(dataFiles.length-1 +"/" + i +" 처리중..\n" );
				File dataFile = dataFiles[i]; // 0은 biology.zip 1은
												// computatinoal.zip 임.

				if (dataFile.isFile() && dataFile.getName().endsWith(".zip"))// 집파일만
																				// 로드
				{

				} else {
					continue;
				}
				ZipInputStream is = new ZipInputStream(new FileInputStream(dataFile));// zip다루기
				ZipEntry entry = null;
				while ((entry = is.getNextEntry()) != null) {
					
					if (!entry.isDirectory()) // 폴더가 아닐경우.
					{
						String fileName = entry.getName();

						if (!fileName.contains("journal")) // 파일일므이 저널만 된것으로 불러오기.
						{
							continue;
						}
						String year = fileName.split("/")[0];

						year = year.split("_")[0]; // 년도추출
						int c = 0;
						StringBuffer sb = new StringBuffer();
						while ((c = is.read()) != -1) {
							
							sb.append((char) c); //파일일므이 journal로 된것이면 불러온다.
						}
						String[] sb_lines = sb.toString().split("\n"); // 스트링버퍼를 라인별로나눠서

						for (int j = 0; j < sb_lines.length; j++) {

							String sb_line = sb_lines[j];
							if (sb_line.contains("<p>")) //p태그 찾아내서 년도와  
							{
								sb_line = sb_line.replaceAll("<[^>]*>", " "); // 태그 없애기
								fw.write("year:" + year + " journal_no:" + journal_no +  " " + sb_line + "\n"); 
								// year:2003 journal_no:59        Finally, how is it that elongated spermatids~~.

							}
						}
					}
					journal_no++;
					
				}
				printf("처리완료");
				is.close();
			}
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);

		}
	}
}
