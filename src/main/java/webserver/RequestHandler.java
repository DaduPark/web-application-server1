package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    
    public static String homeURL = "/index.html";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
 
        	HttpRequest httpRequest = new HttpRequest(in);
        	
        	HttpResponse response = new HttpResponse(out);
        	
        	if(httpRequest.getPath().contains("/create")) {
        		
        		User user = new User(httpRequest.getParameter("userId"), 
        							httpRequest.getParameter("password"), 
        							URLDecoder.decode(httpRequest.getParameter("name"), "UTF-8"), 
        							URLDecoder.decode(httpRequest.getParameter("email"), "UTF-8"));
            	
        		DataBase.addUser(user);
        		
                response.sendRedirect(homeURL);
        	}else if(httpRequest.getPath().endsWith("/user/login")){
                
        		User user = DataBase.findUserById(httpRequest.getParameter("userId"));
        		
        		if(user != null && user.getPassword().equals(httpRequest.getParameter("password"))) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect(homeURL);
        		}else {
        			 response.forward("/user/login_failed.html");
        		}
        	}else if(httpRequest.getPath().contains("/user/list")) {
        		
        		Map<String, String> cookies = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
        		
        		if(cookies.get("logined") != null && Boolean.parseBoolean(cookies.get("logined"))==true) {
        			response.forwardUserList();
        		}else {
        			response.forward("/user/login.html");
        		}
        	}else {
        		response.forward(httpRequest.getPath());
        	}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    

    
    
}
