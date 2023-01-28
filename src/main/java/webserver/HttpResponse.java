package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
	
	private DataOutputStream dos;
	
	private Map<String, String> header;
	
	public HttpResponse(OutputStream out) {
		
		dos = new DataOutputStream(out);
		header = new HashMap<>();
	}
	
	public void forward(String url)  throws IOException {
		
		byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
		if(url.endsWith(".css")){
			addHeader("Content-Type","text/css");
		}else if(url.endsWith(".js")){
			addHeader("Content-Type","application/javascript");
		}else {
			addHeader("Content-Type","text/html;charset=utf=8");
		}
		addHeader("Content-Length",body.length +"");
		response200Header(body.length);
		responseBody(body);
		
	}
	
	public void forwardBody(byte[] body)  throws IOException {
		addHeader("Content-Type","text/html;charset=utf=8");
		addHeader("Content-Length",body.length +"");
		response200Header(body.length);
        responseBody(body);
	}
	
	public void sendRedirect(String url) throws IOException  {
		 dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
         dos.writeBytes("Location: "+url+" \r\n");
         processHeader();
         dos.writeBytes("\r\n");
	}
	
	public void addHeader(String headerKey, String headerValue) throws IOException {
		header.put(headerKey, headerValue);
	}
	
    
    private void response200Header(int lengthOfBodyContent) {
    	try {
    		dos.writeBytes("HTTP/1.1 200 OK \r\n");
    		processHeader();
    		dos.writeBytes("\r\n");
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }
    
    private void responseBody(byte[] body) {
    	try {
    		dos.write(body, 0, body.length);
    		dos.writeBytes("\r\n");
    		dos.flush();
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }
    
    private void processHeader()  throws IOException  {
    	for ( String headerKey : header.keySet() ) {
 			dos.writeBytes(headerKey+": "+header.get(headerKey)+"\r\n");
 		}
    }
   
}
