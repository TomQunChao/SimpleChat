package per.tom.chat.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import per.tom.chat.model.Friends;

//TODO 朋友系统

public class FriendsRepo {
	private static final String DB_USER="chat";
	private static final String DB_PASSWD="admin123";
	private static final String DB_URL="jdbc:mysql://localhost:3306/chat";
	
	public List<Friends> findByHost(String host){
		Connection con;
		Statement state;
		List<Friends> res= new ArrayList<Friends>();
		try {
			con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWD);
			state = con.createStatement();
			String sql = "SELECT * FROM friends WHERE host=\""+host+"\";";
			ResultSet rs = state.executeQuery(sql);
			while(rs.next()) {
				Friends f = new Friends();
				f.setHost(host);
				f.setFriend(rs.getString("friend"));
				res.add(f);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	public List<Friends> findByHostAndFriend(String host,String friend){
		Connection con;
		Statement state;
		List<Friends> res= new ArrayList<Friends>();
		try {
			con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWD);
			state = con.createStatement();
			String sql = "SELECT * FROM friends WHERE host=\""+host+"\" AND friend=\""+friend+"\";";
			ResultSet rs = state.executeQuery(sql);
			while(rs.next()) {
				Friends f = new Friends();
				f.setHost(host);
				f.setFriend(rs.getString("friend"));
				res.add(f);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	public void save(Friends f) {
		Connection con;
		Statement state;
		try {
			con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWD);
			state = con.createStatement();
			List<Friends> fs = findByHostAndFriend(f.getHost(),f.getFriend());
			String sql;
			if(fs.size()==0) {
				sql = "INSERT INTO friends VALUES(\""+f.getHost()+"\",\""+f.getFriend()+"\");";
				state.executeUpdate(sql);
			}
			fs = findByHostAndFriend(f.getFriend(),f.getHost());
			if(fs.size()==0) {
				sql = "INSERT INTO friends VALUES(\""+f.getFriend()+"\",\""+f.getHost()+"\");";
				state.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<Friends> findByFriend(String friend){
		Connection con;
		Statement state;
		List<Friends> res= new ArrayList<Friends>();
		try {
			con = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWD);
			state = con.createStatement();
			String sql = "SELECT * FROM friends WHERE host=\""+friend+"\";";
			ResultSet rs = state.executeQuery(sql);
			while(rs.next()) {
				Friends f = new Friends();
				f.setHost(rs.getString("host"));
				f.setFriend(rs.getString("friend"));
				res.add(f);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
}
