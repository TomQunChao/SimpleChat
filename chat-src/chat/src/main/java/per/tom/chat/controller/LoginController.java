package per.tom.chat.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import per.tom.chat.model.LoginUser;
import per.tom.chat.model.RegisterUser;

@WebServlet("/login_control")
public class LoginController extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = new PrintWriter(resp.getOutputStream());
		JSONObject obj = new JSONObject();
		JSONArray ja = new JSONArray();
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		List<LoginUser> lus = Utility.getmLoginUserRepo().findByUserName(username);
		if(lus.size()>0) {
			Utility.getmLoginUserRepo().deleteByUserName(username);
		}
		RegisterUser ru;
		List<RegisterUser> rus = Utility.getmRegisterUserRepo().findByUserName(username);
		if(rus.size()==0||!rus.get(0).getPasswdHash().equals(password)) {
			obj.put("type","error");
			ja.add("username or password wrong");
		}else {
			

			int rand = (int)(Math.random()*Integer.MAX_VALUE);
			ru = rus.get(0);
			LoginUser lu = new LoginUser();
			lu.setId(ru.getId());
			lu.setUserName(username);
			lu.setRand(rand);
			Utility.getmLoginUserRepo().save(lu);
			log.info("login from "+ru.getUserName());
			obj.put("type","success");
			ja.add(""+rand);
		}
		obj.put("data",ja);
		pw.println(obj.toString());
		
		pw.close();
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		pw.println("This page can not be accessed via GET");
		pw.close();
	}
	
}