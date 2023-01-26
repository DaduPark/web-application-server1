package webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

public class HttpResponseTest {
	private String testDirectory = "./src/test/resources/";
	
	@Test
	public void reponseForward() throws Exception{
		//Http_Forward.txt 결과는 응답 body에 index.html이 포함되어 있어야된다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
		response.forward("/index.html");
	}
	
	@Test
	public void reponseRedirect() throws Exception{
		//Http_Redirect.txt 결과는 응답 header에
		//Location 정보가 /index.html이 포함되어있어야된다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		response.sendRedirect("/index.html");
	}
	
	@Test
	public void reponseCookies() throws Exception{
		//Http_Cookie.txt 결과는 응답 header에 Set-Cookie값으
		//logined=true 값이 포함되어어야된다.
		HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie.txt"));
		response.addHeader("Set-Cookie", "logined=true");
		response.sendRedirect("/index.html");
	}
	
	
	
	private OutputStream createOutputStream(String filename) throws FileNotFoundException{	
		return new FileOutputStream(new File(testDirectory + filename));
	}
}
