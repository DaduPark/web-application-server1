package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;

abstract class AbstractController implements Controller{
	public void service(HttpRequest request, HttpResponse response)  throws IOException {
		if(request.getMethod().isPost()){
			doPost(request, response);
		}else {
			doGet(request, response);
		}
	};
	public void doPost(HttpRequest request, HttpResponse response) throws IOException {};
	public void doGet(HttpRequest request, HttpResponse response)  throws IOException {};
}
