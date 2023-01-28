package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;
abstract class AbstractController implements Controller{
	
	@Override
	public void service(HttpRequest request, HttpResponse response)  throws IOException {
		if(request.getMethod().isPost()){
			doPost(request, response);
		}else {
			doGet(request, response);
		}
	};
	protected void doPost(HttpRequest request, HttpResponse response) throws IOException {};
	protected void doGet(HttpRequest request, HttpResponse response)  throws IOException {};
}
