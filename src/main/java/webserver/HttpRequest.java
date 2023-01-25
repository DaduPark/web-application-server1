package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	 private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
	private String method;
	
	private String path;
	
	private Map<String, String> header;
	
	private Map<String, String> parameter;

	public HttpRequest(InputStream in) {
		
		InputStreamReader isr = new InputStreamReader(in);
    	BufferedReader br = new BufferedReader(isr);
    	
    	String httpInfo = setHttpInfo(br);
    	
    	String headerFirstInfo = setHeaderFirstLine(httpInfo);
    	
    	method = setHeaderMethod(headerFirstInfo);
    	
        String mappingUrl[] = setMappingUrl(headerFirstInfo).split("\\?");
       
        path = mappingUrl[0];
        
    	header = setHeaderInfoMap(httpInfo);
    	
    	if("POST".equals(method)) {
    		parameter = setPostParams(br, header.get("Content-Length"));
    	}else if("GET".equals(method)) {
    		if(mappingUrl.length>1) {
    			parameter = setGetParams(mappingUrl[1]);
    		}
    	}
	}
	
    public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
	
	public String getHeader(String key) {
		return header.get(key);
	}
	
	public String getParameter(String key) {
		return parameter.get(key);
	}

	private String setHttpInfo(BufferedReader br) {
    	

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
    
    private String setHeaderFirstLine(String httpInfo) {
    	String firstLine = httpInfo.split("\n")[0];
    	
    	return firstLine;
    }
    
    private String setHeaderMethod(String headerFirstLine) {
    	return headerFirstLine.split(" ")[0];
    }
    
    private String setMappingUrl(String headerFirstLine) {
    	String returnStr = "";
    	if(headerFirstLine.split(" ").length>1) {
    		returnStr = headerFirstLine.split(" ")[1];
    	}
    	return returnStr;
    }
    
    
    private Map<String,String> setHeaderInfoMap(String httpInfo){
    	Map<String, String> headerInfoMap = new HashMap<String, String>();
    	
    	String infoLineArray[] = httpInfo.split("\n");
    	
    	//httpmethod정보와 url정보가 있는 첫번째 줄은 무
    	for(int i = 1 ; i<infoLineArray.length ; i++) {
			String oneHeaderInfo[] = infoLineArray[i].split(": ");
			
			if(oneHeaderInfo.length==2) {
				headerInfoMap.put(oneHeaderInfo[0],oneHeaderInfo[1]);
			}
    	}
    	
    	return headerInfoMap;
    }
    private Map<String, String>  setPostParams(BufferedReader br, String contentLength) {
    	
    	Map<String, String> params =null;
    	String requestBody="";
    	try {
			requestBody=IOUtils.readData(br,Integer.parseInt(contentLength));
			params = HttpRequestUtils.parseQueryString(requestBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return params;
    	
    	
    }
    
    private Map<String, String> setGetParams(String paramString) {
    	Map<String, String> params =null;
    	params = HttpRequestUtils.parseQueryString(paramString);
    	return params;
    }
}
