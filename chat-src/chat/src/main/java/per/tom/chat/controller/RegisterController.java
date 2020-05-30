package per.tom.chat.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import per.tom.chat.model.RegisterUser;

public class RegisterController extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// super.doPost(req, resp);
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		PrintWriter pw = new PrintWriter(resp.getOutputStream());
		JSONObject obj = new JSONObject();
		JSONArray ja = new JSONArray();
		List<RegisterUser> userNameUsers = Utility.getmRegisterUserRepo().findByUserName(username);
		if(userNameUsers.size()!=0) {
			obj.put("type","error");
			ja.add("Failed, \""+username+"\"existed!!!");
			obj.put("data", ja);
		}else {
			
			RegisterUser ru = new RegisterUser();
			ru.setUserName(username);
			ru.setPasswdHash(password);
			Date rd = new Date();
			ru.setRegisterTime(rd);
			Utility.getmRegisterUserRepo().save(ru);

			log.info(username+password+" Saved at "+rd.toString());
			obj.put("type","success");
			ja.add("Register Successful");
			obj.put("data",ja);
		}
		pw.println(obj.toString());
		pw.close();
				
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// super.doGet(req, resp);
		PrintWriter pw = new PrintWriter(resp.getWriter());
		pw.println("This page only can be accessed via get");
		pw.close();
	}
	
	
}
