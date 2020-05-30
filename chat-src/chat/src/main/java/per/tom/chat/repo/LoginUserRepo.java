package per.tom.chat.repo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import per.tom.chat.controller.Utility;
import per.tom.chat.model.LoginUser;

public class LoginUserRepo {
	public List<LoginUser> findByUserName(String userName){
		List<LoginUser> res = new ArrayList<LoginUser>();
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT * FROM login_user WHERE user_name=\""+userName+"\"";
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()) {
				LoginUser lu = new LoginUser();
				lu.setId(rs.getInt("id"));
				lu.setRand(rs.getInt("rand"));
				lu.setUserName(rs.getString("user_name"));
				res.add(lu);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	public void deleteByUserName(String userName) {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "DELETE FROM login_user WHERE user_name=\""+userName+"\"";
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public long selectMaxId() {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT MAX(id) FROM login_user";
			ResultSet rs = s.executeQuery(sql);
			if(rs.next())
				return rs.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public void save(LoginUser lu) {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = String.format("INSERT INTO login_user (id,user_name,rand) VALUES (\"%d\",\"%s\",\"%d\")",
					selectMaxId()+1,
					lu.getUserName(),
					lu.getRand());
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	public void delete(LoginUser lu) {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = String.format("DELETE FROM login_user WHERE id=\"%d\" AND "
					+ "user_name=\"%s\" AND "
					+ "rand=\"%d\"",
					lu.getId(),
					lu.getUserName(),
					lu.getRand());
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
}
