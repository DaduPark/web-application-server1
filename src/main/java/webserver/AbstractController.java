package webserver;

abstract class AbstractController implements Controller{
	public void service(HttpRequest request, HttpResponse response) {
		if("POST".equals(request.getMethod())){
			doPost(request, response);
		}else {
			doGet(request, response);
		}
	};
	public void doPost(HttpRequest request, HttpResponse response) {};
	public void doGet(HttpRequest request, HttpResponse response) {};
}
