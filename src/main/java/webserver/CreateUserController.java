package webserver;

import java.io.IOException;
import java.net.URLDecoder;

import db.DataBase;
import model.User;

public class CreateUserController extends AbstractController{

	public static String homeURL = "/index.html";
	
	public void doPost(HttpRequest request, HttpResponse response) throws IOException {
		User user = new User(request.getParameter("userId"), 
				request.getParameter("password"), 
				URLDecoder.decode(request.getParameter("name"), "UTF-8"), 
				URLDecoder.decode(request.getParameter("email"), "UTF-8"));

		DataBase.addUser(user);
		
		response.sendRedirect(homeURL);
	};
}
