package per.tom.chat.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import per.tom.chat.model.LoginUser;
import per.tom.chat.model.RegisterUser;
import per.tom.chat.repo.FriendsRepo;
import per.tom.chat.repo.HistoryMessageRepo;
import per.tom.chat.repo.LoginUserRepo;
import per.tom.chat.repo.MessageToReceiveRepo;
import per.tom.chat.repo.RegisterUserRepo;

public class Utility {

	public static final String SQL_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	private static LoginUserRepo mLoginUserRepo = new LoginUserRepo();
	private static RegisterUserRepo mRegisterUserRepo = new RegisterUserRepo();
	private static MessageToReceiveRepo mMessageToReceiveRepo = new MessageToReceiveRepo();
	private static HistoryMessageRepo mHistoryMessageRepo = new HistoryMessageRepo();
	private static FriendsRepo mFriendsRepo = new FriendsRepo();

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static LoginUserRepo getmLoginUserRepo() {
		return mLoginUserRepo;
	}
	public static RegisterUserRepo getmRegisterUserRepo() {
		return mRegisterUserRepo;
	}
	public static MessageToReceiveRepo getmMessageToReceiveRepo() {
		return mMessageToReceiveRepo;
	}
	public static HistoryMessageRepo getmHistoryMessageRepo() {
		return mHistoryMessageRepo;
	}
	public static FriendsRepo getmFriendsRepo() {
		return mFriendsRepo;
	}
	
	public static boolean vertifyUser(String user,String rand) {
		List<LoginUser> lus = mLoginUserRepo.findByUserName(user);
		return lus.size()!=0&&lus.get(0).getRand()==Integer.valueOf(rand);
	}
	public static boolean isUserExist(String user) {
		List<RegisterUser> rus = mRegisterUserRepo.findByUserName(user);
		return rus.size()!=0;
	}
	public boolean isInit=false;
	public boolean isInited() {
		return isInit;
	}
	

	private static final String DB_USER="chat";
	private static final String DB_PASSWD="admin123";
	private static final String DB_URL="jdbc:mysql://localhost:3306/chat";

	public static Connection getSqlConnection() throws SQLException {
		Connection con;
		con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWD);
		return con;
	}
	
	public static boolean saveFile(String filename,String text) {
		File f = new File(filename);
		if(f.exists())return false;
		try {
			PrintWriter pw = new PrintWriter(f);
			pw.println(text);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}
	public static String genUnicString() {
		String s = String.format("%x",new Date().getTime());
		s+=(long)(Math.random()*65535);
		return s;
	}
	public static String readFile(String filename) {
		File f = new File(filename);
		try {
			Scanner in = new Scanner(f);
			String res = in.nextLine();
			in.close();
			return res;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * @param text
	 * @return 保存的文件名
	 */
	public static String saveAsFile(String text) {
		File folder = new File("record/");
		if(!folder.exists()||!folder.isDirectory()) {
			folder.mkdir();
		}
		String s;
		do {
			s = folder+"/"+genUnicString();
		}while(!saveFile(s, text));
		return s;
	}
	
}
