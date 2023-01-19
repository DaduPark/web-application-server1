package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
 
        	InputStreamReader isr = new InputStreamReader(in);
        	BufferedReader br = new BufferedReader(isr);
        	
        	String headerInfo = getHttpInfo(br);
        	
        	String headerFirstInfo = getHeaderFirstLine(headerInfo);
        	
        	String httpMethod = getHeaderMethod(headerFirstInfo);
        	
        	String mappingUrl = getMappingUrl(headerFirstInfo);
            
        	Map<String, String> headerInfoMap = getHeaderInfoMap(headerInfo);
        	
        	String bodyInfo = "";
        	
        	if("POST".equals(httpMethod)) {
        		bodyInfo = getBodyInfo(br, headerInfoMap.get("Content-Length"));
        	}
        	
        	if(mappingUrl.contains("/create")) {
        		String userInfoArray[] = bodyInfo.split("&");
        		
        		String userId ="";
                String password ="";
                String name ="";
                String email ="";
                
        		for(String userInfo : userInfoArray) {
        			String userInfoMap[] = userInfo.split("=");
        			
        			userInfoMap[1] = URLDecoder.decode(userInfoMap[1], "UTF-8");
        			if("userId".contains(userInfoMap[0])) {
        				userId = userInfoMap[1];
        			}
        			
        			if("password".contains(userInfoMap[0])) {
        				password = userInfoMap[1];
        			}
        			
        			if("name".contains(userInfoMap[0])) {
        				name = userInfoMap[1];
        			}
        			
        			if("email".contains(userInfoMap[0])) {
        				email = userInfoMap[1];
        			}
        		}
        		User user = new User(userId, password, name, email);
        		
        		System.out.println(user.toString());
        	}else {
        		byte[] body = getMappingBody(getMappgingFile(mappingUrl));
            	
            	// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        		DataOutputStream dos = new DataOutputStream(out);
                response200Header(dos, body.length);
                responseBody(dos, body);
        	}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getHttpInfo(BufferedReader br) {
    	

    	StringBuffer headerInfo= new StringBuffer();
		try {
			String line = br.readLine();
			
			while (!"".equals(line)) {
				headerInfo.append(line+"\n");
				line = br.readLine();
				
				if(line == null) {
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return headerInfo.toString();
    }
    
    private String getHeaderFirstLine(String headerInfo) {
    	String firstLine = headerInfo.split("\n")[0];
    	
    	return firstLine;
    }
    
    private String getHeaderMethod(String headerFirstLine) {
    	return headerFirstLine.split(" ")[0];
    }
    
    private String getMappingUrl(String headerFirstLine) {
    	return headerFirstLine.split(" ")[1];
    }
    
    private byte[] getMappingBody(File file) {
    	byte[] body = null;
    	
    	try {
    		body = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return body;
    }
    
    private File getMappgingFile(String mappingUrl) {
    	File file = new File("./webapp" + mappingUrl);
    	
        if (file.exists()) {
            if (!file.isDirectory()) {
            	return file;
            }
        }
        
        log.debug("No Mapping Url[URL : "+mappingUrl+"]");
        return new File("./webapp/index.html");  
    }
    
    
    private Map<String,String> getHeaderInfoMap(String headerInfo){
    	Map<String, String> headerInfoMap = new HashMap<String, String>();
    	
    	String infoLineArray[] = headerInfo.split("\n");
    	
    	//httpmethod정보와 url정보가 있는 첫번째 줄은 무
    	for(int i = 1 ; i<infoLineArray.length ; i++) {
			String oneHeaderInfo[] = infoLineArray[i].split(": ");
			
			if(oneHeaderInfo.length==2) {
				headerInfoMap.put(oneHeaderInfo[0],oneHeaderInfo[1]);
			}
    	}
    	
    	return headerInfoMap;
    }
    private String getBodyInfo(BufferedReader br, String contentLength) {
    	
    	String requestBody="";
    	try {
			requestBody=IOUtils.readData(br,Integer.parseInt(contentLength));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return requestBody;
    	
    	
    }
}
