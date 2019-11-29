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

		System.out.println("����");
		
		long s = System.currentTimeMillis();

		//�����Լ�
		start();
		
		long e = System.currentTimeMillis();
		System.out.println( "�� ���� �ð� : " + ( e - s )/1000.0 );
		System.out.println("����� �޸� : " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

	}

	//���۸޼ҵ�
	private static void start() {
		try {
			
			long s = System.currentTimeMillis();

			makeFile1();//����1����
			
			long e = System.currentTimeMillis();
			double startTime1=( e - s )/1000.0;
			
			s = System.currentTimeMillis();
			
			makeFile2();//����2����
			
			e = System.currentTimeMillis();
			double startTime2=( e - s )/1000.0;
			
			System.out.println("file1 �ð� : "+startTime1+"\nfile2 �ð� : "+startTime2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void makeFile1() throws IOException{
		 
		HashMap<String, String> dataMap = new HashMap<String,String>(); //key:������, value:�������� �� �����͵� 
		HashMap <String, Integer> startMap = new HashMap <String,Integer>();//key:������, value:start����
		TreeMap<Integer, String> allDataMap = new TreeMap<Integer,String>();//key:�ش� �������� startMap�� value, value:�������� �� �����͵� 

		int startcount = 0;//start ����.
		try {
			String oneLine=null;
			BufferedReader in = new BufferedReader(new FileReader("C:\\03����\\99.����÷������\\2.�α����Ϻм�\\galileo.log"));
			
			//������ ���پ� �����鼭 �ش� Ű���尡 ���ԵǸ� ó��.
			/*
			 * 1. ##galileo_bean start�� ���Եɶ�
			 * 		- Ű:������, ��:�ð� �� ������ dataMap�� �־���.
			 * 		- Ű:������, ��:start�� ���� �� ������ startMap�� �־���.
			 * 		- startcount++;
			 * 
			 * 2. �� ������ Ű���� �϶�
			 * 		- if(�����尪�� ������ dataMap�� key�� ��. �ش� �����尡 �����Ѵٸ�?)
			 * 			- dataMap�� �ð�, �������͵� �߰�.
			 * 
			 * 3. ##galileo_bean end�� ���Եɶ�
			 * 		- dataMap�̶� startMap�� ���� allDataMap�� �־���. ���� dataMap�̶� startMap�� �ش� �������� �����͵��� ����.(���� ������ �˻��� �����˻��� ����)
			 * 
			 * 4. ���ϸ���� - Map�� ���� StringBuffer�� �� �������� file���鶧 toString ����.
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
//        	System.out.println( String.format("Ű : %s, �� : %s", key, allDataMap.get(key)) );
        	dataBuf.append(allDataMap.get(key)+"\n");
        }

		//���ϸ����
		File file = new File("C:\\03����\\99.����÷������\\2.�α����Ϻм�\\file1.txt");
        FileWriter writer = null;
        
        try {
            // ���� ������ ���뿡 �̾ ������ true��, ���� ������ ���ְ� ���� ������ false�� �����Ѵ�.
            writer = new FileWriter(file, false);
            writer.write(dataBuf.toString());
            writer.flush();
            
           System.out.println("file1 ����.");
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
	
	//����2 �����
	private static void makeFile2(){
		
		String copyTime="";//������ �ð�
		int lineNum=0;//���μ�(ó���Ǽ�)
		long addTime=0;//��� �ҿ�ð� = addTime/lineNum
		long minTime=0;//�ּҽð�
		long maxTime=0;//�ִ�ð�
		long addSize=0;//��ջ����� = ��� ������ = addSize/lineNum
		long minSize=0;//�ּһ�����
		long maxSize=0;//�ִ������
		Date endTime;//�����ð�
		Date startTime;//���۽ð�
		long takeTime;//�ɸ��ð�
		SimpleDateFormat dateFormat = new SimpleDateFormat ( "yy.MM.dd HH:mm" );
		
		//������ ��� ������ ������ ����.
		ArrayList<String> allDataList = new ArrayList<>();
		
		/*
		 * 1. ù�� - �� ������ ���� ù���� �ռ� ���� �־���.
		 * 
		 * 2. ������ 
		 * 			- �ð� ������
		 *				�� ������ �־���.
		 * 
		 * 			- �ð� �ٸ���
		 * 				�����͸� allDataList�� add����.
		 * 				�� �����͸� �ʱ�ȭ �� ���� �־���.
		 * 
		 * 3. ���� ����.
		 */
		
		try {
			String oneLine=null;
			BufferedReader in = new BufferedReader(new FileReader("C:\\03����\\99.����÷������\\2.�α����Ϻм�\\file1.txt"));
			
			//*********ù�� ó��**********//
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

			//�����ٺ��� ó��
			while ((oneLine = in.readLine()) != null) {
				String time = oneLine.split(",")[0].substring(0,14);//���۽ð�
				//�ð� ������
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
				//�ð� �ٸ���
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
		
		//���ϸ����
		File file = new File("C:\\03����\\99.����÷������\\2.�α����Ϻм�\\file2.txt");
        FileWriter writer = null;
        
        try {
            // ���� ������ ���뿡 �̾ ������ true��, ���� ������ ���ְ� ���� ������ false�� �����Ѵ�.
            writer = new FileWriter(file, false);
            writer.write(data);
            writer.flush();
            
           System.out.println("file2���Ϸ� ����.");
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


































