package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

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
 
        	InputStreamReader isr = new InputStreamReader(in);
        	BufferedReader br = new BufferedReader(isr);
        	
        	String headerInfo = getHttpInfo(br);
        	
        	String headerFirstInfo = getHeaderFirstLine(headerInfo);
        	
        	String httpMethod = getHeaderMethod(headerFirstInfo);
        	
        	String mappingUrl = getMappingUrl(headerFirstInfo);
            
        	Map<String, String> headerInfoMap = getHeaderInfoMap(headerInfo);
        	
        	String bodyInfo = "";
        	
        	
        	DataOutputStream dos = new DataOutputStream(out);
        	
        	log.debug(mappingUrl);
        	
        	if("POST".equals(httpMethod)) {
        		bodyInfo = getBodyInfo(br, headerInfoMap.get("Content-Length"));
        	}
        	
        	if(mappingUrl.contains("/create")) {
        		
        		Map<String, String> params = HttpRequestUtils.parseQueryString(bodyInfo);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
            	
        		DataBase.addUser(user);
        		
                response302Header(dos,homeURL, null);
        	}else if(mappingUrl.endsWith("/user/login")){
        		
        		Map<String, String> params = HttpRequestUtils.parseQueryString(bodyInfo);
        		
        		String loginUserId =params.get("userId");
                String loginPassword =params.get("password");
                
        		User user = DataBase.findUserById(loginUserId);
        		
        		if(user != null && user.getPassword().equals(loginPassword)) {
                    response302Header(dos, homeURL, "logined=true");
        		}else {
            		response302Header(dos, "/user/login_failed.html", "logined=false");
        		}
        	}else if(mappingUrl.contains("/user/list")) {
        		
        		Map<String, String> cookies = HttpRequestUtils.parseCookies(headerInfoMap.get("Cookie"));
        		
        		if(cookies.get("logined") != null && Boolean.parseBoolean(cookies.get("logined"))==true) {
            		byte[] body = getUserListHttpBody();
                    response200Header(dos, body.length, headerInfoMap.get("Accept").split(",")[0]);
                    responseBody(dos, body);
        		}else {
        			response302Header(dos,"/user/login.html",null);
        		}
        	}else {
        		File file = new File("./webapp" + mappingUrl);
                
        		byte[] body = getMappingBody(file);
            	
                response200Header(dos, body.length, headerInfoMap.get("Accept").split(",")[0]);
                responseBody(dos, body);
        	}
        	
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: "+contentType+";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String locationUrl, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            if(cookie != null) {
            	dos.writeBytes("Set-Cookie: "+cookie+"\r\n");
            }
            dos.writeBytes("Location: "+locationUrl);
            
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getHttpInfo(BufferedReader br) {
    	

    	StringBuffer headerInfo= new StringBuffer();
		try {
			String line = br.readLine();
			
			while (!"".equals(line)) {
				headerInfo.append(line+"\n");
				line = br.readLine();
				
				if(line == null) {
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return headerInfo.toString();
    }
    
    private String getHeaderFirstLine(String headerInfo) {
    	String firstLine = headerInfo.split("\n")[0];
    	
    	return firstLine;
    }
    
    private String getHeaderMethod(String headerFirstLine) {
    	return headerFirstLine.split(" ")[0];
    }
    
    private String getMappingUrl(String headerFirstLine) {
    	return headerFirstLine.split(" ")[1];
    }
    
    private byte[] getMappingBody(File file) {
    	byte[] body = null;
    	
    	try {
    		body = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return body;
    }
    
//    private File getMappgingFile(String mappingUrl) {
//    	File file = new File("./webapp" + mappingUrl);
//    	
//        if (file.exists()) {
//            if (!file.isDirectory()) {
//            	return file;
//            }
//        }
//        
//        log.debug("No Mapping Url[URL : "+mappingUrl+"]");
//        return new File("./webapp"+homeURL);  
//    }
//    
    
    private Map<String,String> getHeaderInfoMap(String headerInfo){
    	Map<String, String> headerInfoMap = new HashMap<String, String>();
    	
    	String infoLineArray[] = headerInfo.split("\n");
    	
    	//httpmethod정보와 url정보가 있는 첫번째 줄은 무
    	for(int i = 1 ; i<infoLineArray.length ; i++) {
			String oneHeaderInfo[] = infoLineArray[i].split(": ");
			
			if(oneHeaderInfo.length==2) {
				headerInfoMap.put(oneHeaderInfo[0],oneHeaderInfo[1]);
			}
    	}
    	
    	return headerInfoMap;
    }
    private String getBodyInfo(BufferedReader br, String contentLength) {
    	
    	String requestBody="";
    	try {
			requestBody=IOUtils.readData(br,Integer.parseInt(contentLength));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return requestBody;
    	
    	
    }
    
    private byte[] getUserListHttpBody() {
    	Collection<User> userList = DataBase.findAll();
		
    	StringBuilder httpBody = new StringBuilder();
    	httpBody.append("<!DOCTYPE html>\n"
    			+ "<html lang=\"kr\">\n"
    			+ "<head>\n"
    			+ "    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n"
    			+ "    <meta charset=\"utf-8\">\n"
    			+ "    <title>SLiPP Java Web Programming</title>\n"
    			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">\n"
    			+ "    <link href=\"../css/bootstrap.min.css\" rel=\"stylesheet\">\n"
    			+ "    <!--[if lt IE 9]>\n"
    			+ "    <script src=\"//html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n"
    			+ "    <![endif]-->\n"
    			+ "    <link href=\"../css/styles.css\" rel=\"stylesheet\">\n"
    			+ "</head>\n"
    			+ "<body>\n"
    			+ "<nav class=\"navbar navbar-fixed-top header\">\n"
    			+ "    <div class=\"col-md-12\">\n"
    			+ "        <div class=\"navbar-header\">\n"
    			+ "\n"
    			+ "            <a href=\"../index.html\" class=\"navbar-brand\">SLiPP</a>\n"
    			+ "            <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#navbar-collapse1\">\n"
    			+ "                <i class=\"glyphicon glyphicon-search\"></i>\n"
    			+ "            </button>\n"
    			+ "\n"
    			+ "        </div>\n"
    			+ "        <div class=\"collapse navbar-collapse\" id=\"navbar-collapse1\">\n"
    			+ "            <form class=\"navbar-form pull-left\">\n"
    			+ "                <div class=\"input-group\" style=\"max-width:470px;\">\n"
    			+ "                    <input type=\"text\" class=\"form-control\" placeholder=\"Search\" name=\"srch-term\" id=\"srch-term\">\n"
    			+ "                    <div class=\"input-group-btn\">\n"
    			+ "                        <button class=\"btn btn-default btn-primary\" type=\"submit\"><i class=\"glyphicon glyphicon-search\"></i></button>\n"
    			+ "                    </div>\n"
    			+ "                </div>\n"
    			+ "            </form>\n"
    			+ "            <ul class=\"nav navbar-nav navbar-right\">\n"
    			+ "                <li>\n"
    			+ "                    <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"glyphicon glyphicon-bell\"></i></a>\n"
    			+ "                    <ul class=\"dropdown-menu\">\n"
    			+ "                        <li><a href=\"https://slipp.net\" target=\"_blank\">SLiPP</a></li>\n"
    			+ "                        <li><a href=\"https://facebook.com\" target=\"_blank\">Facebook</a></li>\n"
    			+ "                    </ul>\n"
    			+ "                </li>\n"
    			+ "                <li><a href=\"../user/list.html\"><i class=\"glyphicon glyphicon-user\"></i></a></li>\n"
    			+ "            </ul>\n"
    			+ "        </div>\n"
    			+ "    </div>\n"
    			+ "</nav>\n"
    			+ "<div class=\"navbar navbar-default\" id=\"subnav\">\n"
    			+ "    <div class=\"col-md-12\">\n"
    			+ "        <div class=\"navbar-header\">\n"
    			+ "            <a href=\"#\" style=\"margin-left:15px;\" class=\"navbar-btn btn btn-default btn-plus dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"glyphicon glyphicon-home\" style=\"color:#dd1111;\"></i> Home <small><i class=\"glyphicon glyphicon-chevron-down\"></i></small></a>\n"
    			+ "            <ul class=\"nav dropdown-menu\">\n"
    			+ "                <li><a href=\"../user/profile.html\"><i class=\"glyphicon glyphicon-user\" style=\"color:#1111dd;\"></i> Profile</a></li>\n"
    			+ "                <li class=\"nav-divider\"></li>\n"
    			+ "                <li><a href=\"#\"><i class=\"glyphicon glyphicon-cog\" style=\"color:#dd1111;\"></i> Settings</a></li>\n"
    			+ "            </ul>\n"
    			+ "            \n"
    			+ "            <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#navbar-collapse2\">\n"
    			+ "            	<span class=\"sr-only\">Toggle navigation</span>\n"
    			+ "            	<span class=\"icon-bar\"></span>\n"
    			+ "            	<span class=\"icon-bar\"></span>\n"
    			+ "            	<span class=\"icon-bar\"></span>\n"
    			+ "            </button>            \n"
    			+ "        </div>\n"
    			+ "        <div class=\"collapse navbar-collapse\" id=\"navbar-collapse2\">\n"
    			+ "            <ul class=\"nav navbar-nav navbar-right\">\n"
    			+ "                <li class=\"active\"><a href=\"../index.html\">Posts</a></li>\n"
    			+ "                <li><a href=\"../user/login.html\" role=\"button\">로그인</a></li>\n"
    			+ "                <li><a href=\"../user/form.html\" role=\"button\">회원가입</a></li>\n"
    			+ "                <li><a href=\"#\" role=\"button\">로그아웃</a></li>\n"
    			+ "                <li><a href=\"#\" role=\"button\">개인정보수정</a></li>\n"
    			+ "            </ul>\n"
    			+ "        </div>\n"
    			+ "    </div>\n"
    			+ "</div>\n"
    			+ "\n"
    			+ "<div class=\"container\" id=\"main\">\n"
    			+ "   <div class=\"col-md-10 col-md-offset-1\">\n"
    			+ "      <div class=\"panel panel-default\">\n"
    			+ "          <table class=\"table table-hover\">\n"
    			+ "              <thead>\n"
    			+ "                <tr>\n"
    			+ "                    <th>#</th> <th>사용자 아이디</th> <th>이름</th> <th>이메일</th><th></th>\n"
    			+ "                </tr>\n"
    			+ "              </thead>\n"
    			+ "              <tbody>");
    	
    	int n = 1;
    	for(User user : userList) {
    		
    		httpBody.append("<tr>");
    		
    		httpBody.append(" <th scope=\"row\">"+ n++ +"</th> <td>");
    		httpBody.append(user.getUserId());
    		httpBody.append("</td> <td>");
    		httpBody.append(user.getName());
    		httpBody.append("</td> <td>");
    		httpBody.append(user.getEmail());
    		httpBody.append("</td><td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
    		
    		httpBody.append("</tr>");
    	}
    	
    	httpBody.append("</tbody>\n"
    			+ "          </table>\n"
    			+ "        </div>\n"
    			+ "    </div>\n"
    			+ "</div>\n"
    			+ "\n"
    			+ "<!-- script references -->\n"
    			+ "<script src=\"../js/jquery-2.2.0.min.js\"></script>\n"
    			+ "<script src=\"../js/bootstrap.min.js\"></script>\n"
    			+ "<script src=\"../js/scripts.js\"></script>\n"
    			+ "	</body>\n"
    			+ "</html>");
		
    	return httpBody.toString().getBytes();
    }
    
}
