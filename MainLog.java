package logfileanalysis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class MainLog {

	public static void main(String[] args) {

		System.out.println("시작");
		
		long s = System.currentTimeMillis();

		//시작함수
		start();
		
		long e = System.currentTimeMillis();
		System.out.println( "총 실행 시간 : " + ( e - s )/1000.0 );
		System.out.println("사용한 메모리 : " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

	}

	//시작메소드
	private static void start() {
		try {
			
			long s = System.currentTimeMillis();

			makeFile1();//파일1만듬
			
			long e = System.currentTimeMillis();
			double startTime1=( e - s )/1000.0;
			
			s = System.currentTimeMillis();
			
			makeFile2();//파일2만듬
			
			e = System.currentTimeMillis();
			double startTime2=( e - s )/1000.0;
			
			System.out.println("file1 시간 : "+startTime1+"\nfile2 시간 : "+startTime2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void makeFile1() throws IOException{
		 
		HashMap<String, String> dataMap = new HashMap<String,String>(); //key:스레드, value:스레드의 각 데이터들 
		HashMap <String, Integer> startMap = new HashMap <String,Integer>();//key:스레드, value:start순서
		TreeMap<Integer, String> allDataMap = new TreeMap<Integer,String>();//key:해당 스레드의 startMap의 value, value:스레드의 각 데이터들 

		int startcount = 0;//start 순서.
		try {
			String oneLine=null;
			BufferedReader in = new BufferedReader(new FileReader("C:\\03과제\\99.과제첨부파일\\2.로그파일분석\\galileo.log"));
			
			//파일을 한줄씩 읽으면서 해당 키워드가 포함되면 처리.
			/*
			 * 1. ##galileo_bean start가 포함될때
			 * 		- 키:스레드, 값:시간 을 가져와 dataMap에 넣어줌.
			 * 		- 키:스레드, 값:start의 순서 를 가져와 startMap에 넣어줌.
			 * 		- startcount++;
			 * 
			 * 2. 각 나머지 키워드 일때
			 * 		- if(스레드값을 가져와 dataMap의 key와 비교. 해당 스레드가 존재한다면?)
			 * 			- dataMap에 시간, 각데이터들 추가.
			 * 
			 * 3. ##galileo_bean end가 포함될때
			 * 		- dataMap이랑 startMap의 값을 allDataMap에 넣어줌. 그후 dataMap이랑 startMap의 해당 스레드인 데이터들을 삭제.(다음 스레드 검색시 빠른검색을 위해)
			 * 
			 * 4. 파일만들기 - Map의 값을 StringBuffer에 다 넣은다음 file만들때 toString 해줌.
			 */
			while ((oneLine = in.readLine()) != null) {
				
				if (oneLine.contains("##galileo_bean start")) {
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					String time= oneLine.substring(1,18);
					dataMap.put(thread, time);
					startMap.put(thread,startcount);
					startcount++;
				}
				else if(oneLine.contains("ESB_TRAN_ID : ")){
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					if(dataMap.containsKey(thread)){
						dataMap.put(thread, dataMap.get(thread)+",	"+oneLine.substring(oneLine.indexOf("ESB_TRAN_ID : ")).split("ESB_TRAN_ID : ",2)[1]);
					}
				}
				else if(oneLine.contains("Content-Length:")){
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					if(dataMap.containsKey(thread)){
						dataMap.put(thread, dataMap.get(thread)+",	"+oneLine.substring(oneLine.indexOf("Content-Length:")).split("Content-Length:",2)[1]);
					}
				}
				else if(oneLine.contains("#galileo call time:")){
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					if(dataMap.containsKey(thread)){
						String callTime = (oneLine.substring(oneLine.indexOf("#galileo call time:"))).split("#galileo call time:",2)[1];
						dataMap.put(thread, dataMap.get(thread)+",	"+callTime.substring(0,callTime.indexOf(" ")));
					}
				}
				else if(oneLine.contains("StopWatch 'Time Watch")){
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					
					if(dataMap.containsKey(thread)){
						
						String line;
						
						in.readLine();
						in.readLine();
						in.readLine();
						
						if(!(line=in.readLine()).contains("Before Marshalling")){
							dataMap.put(thread, "");
						}else{
							dataMap.put(thread, dataMap.get(thread)+",	"+line.substring(0, 5));
						}
						
						if(!(line=in.readLine()).contains("Marshalling")){
							dataMap.put(thread, "");
						}else{
							dataMap.put(thread, dataMap.get(thread)+",	"+line.substring(0, 5));
						}
						
						if(!(line=in.readLine()).contains("Invoking galileo")){
							dataMap.put(thread, "");
						}else{
							dataMap.put(thread, dataMap.get(thread)+",	"+line.substring(0, 5));
						}
						
						if(!(line=in.readLine()).contains("Unmarshalling and Send to CmmMod Server")){
							dataMap.put(thread, "");
						}else{
							dataMap.put(thread, dataMap.get(thread)+",	"+line.substring(0, 5));
						}
					}
					
				}
				else if(oneLine.contains("##galileo_bean end")){
					String thread=oneLine.substring(oneLine.indexOf("eclipse.galileo-bean-thread-"),66).split("eclipse.galileo-bean-thread-",2)[1];
					String time= oneLine.substring(1,18);

					if(dataMap.containsKey(thread)){
						
						String[] temp = dataMap.remove(thread).split(",",2);
						if(temp.length==2){
							allDataMap.put(startMap.remove(thread),temp[0]+",	"+time+","+temp[1]);
						}
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		String data="";
		StringBuffer dataBuf = new StringBuffer();
		Iterator<Integer> keys = allDataMap.keySet().iterator();
		while( keys.hasNext() ){
            Integer key = keys.next();
//        	System.out.println( String.format("키 : %s, 값 : %s", key, allDataMap.get(key)) );
        	dataBuf.append(allDataMap.get(key)+"\n");
        }

		//파일만들기
		File file = new File("C:\\03과제\\99.과제첨부파일\\2.로그파일분석\\file1.txt");
        FileWriter writer = null;
        
        try {
            // 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
            writer = new FileWriter(file, false);
            writer.write(dataBuf.toString());
            writer.flush();
            
           System.out.println("file1 저장.");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	//파일2 만들기
	private static void makeFile2(){
		
		String copyTime="";//복사한 시간
		int lineNum=0;//라인수(처리건수)
		long addTime=0;//평균 소요시간 = addTime/lineNum
		long minTime=0;//최소시간
		long maxTime=0;//최대시간
		long addSize=0;//평균사이즈 = 평균 사이즈 = addSize/lineNum
		long minSize=0;//최소사이즈
		long maxSize=0;//최대사이즈
		Date endTime;//끝난시간
		Date startTime;//시작시간
		long takeTime;//걸린시간
		SimpleDateFormat dateFormat = new SimpleDateFormat ( "yy.MM.dd HH:mm" );
		
		//추출한 모든 데이터 저장할 변수.
		ArrayList<String> allDataList = new ArrayList<>();
		
		/*
		 * 1. 첫줄 - 전 라인이 없는 첫줄은 손수 값을 넣어줌.
		 * 
		 * 2. 다음줄 
		 * 			- 시간 같을때
		 *				각 데이터 넣어줌.
		 * 
		 * 			- 시간 다를때
		 * 				데이터를 allDataList에 add해줌.
		 * 				각 데이터를 초기화 및 값을 넣어줌.
		 * 
		 * 3. 파일 만듬.
		 */
		
		try {
			String oneLine=null;
			BufferedReader in = new BufferedReader(new FileReader("C:\\03과제\\99.과제첨부파일\\2.로그파일분석\\file1.txt"));
			
			//*********첫줄 처리**********//
			oneLine = in.readLine();
			copyTime=oneLine.split(",\t")[0].substring(0,14);
    
			lineNum++;
			 
			endTime = dateFormat.parse(oneLine.split(",\t")[1].substring(0,14));
			startTime = dateFormat.parse(oneLine.split(",\t")[0].substring(0,14));
			takeTime = endTime.getTime()-startTime.getTime();
			addTime+=takeTime;
			minTime=takeTime;
			maxTime=takeTime;
			
			addSize+=Long.parseLong(oneLine.split(",\t")[3]);
			minSize=Long.parseLong(oneLine.split(",\t")[3]);
			maxSize=Long.parseLong(oneLine.split(",\t")[3]);

			//다음줄부터 처리
			while ((oneLine = in.readLine()) != null) {
				String time = oneLine.split(",")[0].substring(0,14);//시작시간
				//시간 같을때
				if(copyTime.equals(time)){

					lineNum++;
					
					endTime = dateFormat.parse(oneLine.split(",\t")[1].substring(0,14));
					startTime = dateFormat.parse(time);
					takeTime = endTime.getTime()-startTime.getTime();
					addTime+=takeTime;
					
					minTime=(minTime<takeTime) ? minTime : takeTime;
					maxTime=(maxTime>takeTime) ? maxTime : takeTime;
					
					long size = Long.parseLong(oneLine.split(",\t")[3]);
					
					addSize+=size;
					
					minSize=(minSize<size) ? minSize : size;
					maxSize=(maxSize>size) ? maxSize : size;
				}
				//시간 다를때
				else {
					
					//add
					allDataList.add(copyTime+",	"+lineNum+",	"+addTime/lineNum+",	"+minTime+",	"+maxTime+",	"+addSize/lineNum+",	"+minSize+",	"+maxSize);
					
					copyTime=time;
					lineNum=1;
					endTime = dateFormat.parse(oneLine.split(",\t")[1].substring(0,14));
					startTime = dateFormat.parse(time);
					takeTime = endTime.getTime()-startTime.getTime();
					
					addTime=takeTime;
					minTime=takeTime;
					maxTime=takeTime;
					
					long size = Long.parseLong(oneLine.split(",\t")[3]);
					addSize=size;
					minSize=size;
					maxSize=size;
				}
			}
			allDataList.add(copyTime+",	"+lineNum+",	"+addTime/lineNum+",	"+minTime+",	"+maxTime+",	"+addSize/lineNum+",	"+minSize+",	"+maxSize);
			in.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		String data="";
		for(int i=0; i<allDataList.size(); i++){
			data += allDataList.get(i)+"\n";
		}
		
		//파일만들기
		File file = new File("C:\\03과제\\99.과제첨부파일\\2.로그파일분석\\file2.txt");
        FileWriter writer = null;
        
        try {
            // 기존 파일의 내용에 이어서 쓰려면 true를, 기존 내용을 없애고 새로 쓰려면 false를 지정한다.
            writer = new FileWriter(file, false);
            writer.write(data);
            writer.flush();
            
           System.out.println("file2파일로 저장.");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) 
                	writer.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
	}
}


































