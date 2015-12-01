package pyj_hadoop;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class data_handler {
	public static void main(String args[]) throws IndexOutOfBoundsException, NullPointerException {
		xml_parser();

	}

	public static void printf(String s) {

		System.out.printf(s);
		
	}
	
	
	
	public static void xml_parser()
	{
		try {
			int journal_no = 0;
			File[] dataFiles = new File(Path.JOURNAL_DIR).listFiles();
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path.XML_PARSING_TEXT_FILE), "UTF8"));
			for (int i = 0; i < dataFiles.length; i++) // 개수 zip개수인 6개가될것이다.
			{
				printf(dataFiles.length +"/" + i+1 +" 처리중..\n" );
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
						String[] sb_line = sb.toString().split("\n"); // 라인별로나눠서

						for (int j = 0; j < sb_line.length; j++) {

							String sb_lined = sb_line[j];
							if (sb_lined.contains("<p>")) //p태그 찾아내서 년도와  
							{
								sb_lined = sb_lined.replaceAll("<[^>]*>", " "); // 태그 없애기
								fw.write("year:" + year + " journal_no:" + journal_no +  " " + sb_lined + "\n"); 
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
			System.err.println(e); // ?????? ???? ????? ???
			System.exit(1);

		}
	}
}
