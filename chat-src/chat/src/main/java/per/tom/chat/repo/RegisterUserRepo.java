package per.tom.chat.repo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import per.tom.chat.controller.Utility;
import per.tom.chat.model.RegisterUser;

public class RegisterUserRepo {
	public List<RegisterUser> findByUserName(String userName){
		List<RegisterUser> res = new ArrayList<>();
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT * FROM register_user WHERE user_name=\""+userName+"\"";
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()) {
				RegisterUser ru = new RegisterUser();
				ru.setId(rs.getInt("id"));
				ru.setPasswdHash(rs.getString("passwd_hash"));
				ru.setRegisterTime(rs.getDate("register_time"));
				ru.setUserName(rs.getString("user_name"));
				res.add(ru);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	public long selectMaxId() {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT MAX(id) FROM register_user";
			ResultSet rs = s.executeQuery(sql);
			if(rs.next())
				return rs.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public void deleteByUserName(String userName) {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			// TODO
			String sql = String.format("DELETE FROM register_user WHERE user_name=%s",userName);
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void save(RegisterUser ru) {
		try {
			Connection con = Utility.getSqlConnection();
			SimpleDateFormat sdf = new SimpleDateFormat(Utility.SQL_DATE_FORMAT);
			Statement s = con.createStatement();
			String sql = String.format("INSERT INTO register_user (id,user_name,passwd_hash,register_time)"
					+ "VALUES(\"%d\",\"%s\",\"%s\",\"%s\")",
					selectMaxId()+1,
					ru.getUserName(),
					ru.getPasswdHash(),
					sdf.format(ru.getRegisterTime()));
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
