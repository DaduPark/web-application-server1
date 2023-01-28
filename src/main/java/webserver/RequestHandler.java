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

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;
import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
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
        	controllerMap.put("/user/list.html", new ListUserController());
        	controllerMap.put("/user/login", new LoginController());
        	
        	if(controllerMap.containsKey(request.getPath())) {
        		Controller controller = controllerMap.get(request.getPath());
        		controller.service(request, response);
        	}else {
        		response.forward(request.getPath());
        	}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
