package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
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
 
        	HttpRequest request = new HttpRequest(in);
        	
        	HttpResponse response = new HttpResponse(out);
        	
        	Map<String, Controller> controllerMap = new HashMap<String, Controller>	();
        	
        	controllerMap.put("/user/create", new CreateUserController());
        	
        	if(request.getPath().contains("/create")) {
        		
        		Controller controller = controllerMap.get(request.getPath());
        		
        		controller.service(request, response);
        		
        		
        	}else if(request.getPath().endsWith("/user/login")){
                
        		User user = DataBase.findUserById(request.getParameter("userId"));
        		
        		if(user != null && user.getPassword().equals(request.getParameter("password"))) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect(homeURL);
        		}else {
        			 response.forward("/user/login_failed.html");
        		}
        	}else if(request.getPath().contains("/user/list")) {
        		
        		Map<String, String> cookies = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
        		
        		if(cookies.get("logined") != null && Boolean.parseBoolean(cookies.get("logined"))==true) {
        			response.forwardUserList();
        		}else {
        			response.forward("/user/login.html");
        		}
        	}else {
        		response.forward(request.getPath());
        	}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    

    
    
}
