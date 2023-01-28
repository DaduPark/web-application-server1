package controller;

import java.io.IOException;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController extends AbstractController{

	public static String homeURL = "/index.html";
	
	public void doPost(HttpRequest request, HttpResponse response) throws IOException {
		User user = DataBase.findUserById(request.getParameter("userId"));
		
		if(user != null && user.getPassword().equals(request.getParameter("password"))) {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect(homeURL);
		}else {
			 response.forward("/user/login_failed.html");
		}
	};
}
