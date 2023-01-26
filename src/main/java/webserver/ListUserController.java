package webserver;

import java.io.IOException;
import java.util.Map;

import util.HttpRequestUtils;

public class ListUserController extends AbstractController{

	public static String homeURL = "/index.html";
	
	public void doGet(HttpRequest request, HttpResponse response) throws IOException {
		Map<String, String> cookies = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
		
		if(isLogin(cookies.get("logined"))) {
			response.forwardUserList();
		}else {
			response.forward("/user/login.html");
		}
	};
	
	private boolean isLogin(String logined) {
		
		if(logined != null && Boolean.parseBoolean(logined)==true) {
			return true;
		}
		
		return false;
	}
}
