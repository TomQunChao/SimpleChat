package per.tom.chat.repo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import per.tom.chat.controller.Utility;
import per.tom.chat.model.HistoryMessage;

public class HistoryMessageRepo {
	public List<HistoryMessage> findBySendUser(String sendUser){
		List<HistoryMessage> res = new ArrayList<HistoryMessage>();
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT * FROM history_message where send_user=\""+sendUser+"\"";
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()) {
				HistoryMessage hm = new HistoryMessage();
				hm.setId(rs.getInt("id"));
				
				hm.setMessage(rs.getString("message"));
				hm.setMessage(Utility.readFile(hm.getMessage()));
				
				hm.setReadDate(rs.getDate("read_date"));
				hm.setSendDate(rs.getDate("send_date"));
				hm.setSendUser(rs.getString("send_user"));
				hm.setReceiveUser(rs.getString("receive_user"));
				res.add(hm);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	public List<HistoryMessage> findByReceiveUser(String receiveUser){
		List<HistoryMessage> res = new ArrayList<HistoryMessage>();
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT * FROM history_message where receive_user=\""+receiveUser+"\"";
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()) {
				HistoryMessage hm = new HistoryMessage();
				hm.setId(rs.getInt("id"));
				
				hm.setMessage(rs.getString("message"));
				hm.setMessage(Utility.readFile(hm.getMessage()));
				
				hm.setReadDate(rs.getDate("read_date"));
				hm.setSendDate(rs.getDate("send_date"));
				hm.setSendUser(rs.getString("send_user"));
				hm.setReceiveUser(rs.getString("receive_user"));
				res.add(hm);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	public void save(HistoryMessage hm) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Utility.SQL_DATE_FORMAT);
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			
			hm.setMessage(Utility.saveAsFile(hm.getMessage()));
			
			String sql = String.format("INSERT INTO history_message (id,message,read_date,"
					+ "receive_user,send_date,send_user)VALUES("
					+ "%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\")", 
					hm.getId(),
					hm.getMessage(),
					sdf.format(hm.getReadDate()),
					hm.getReceiveUser(),
					sdf.format(hm.getSendDate()),
					hm.getSendUser());
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
			String sql = "SELECT MAX(id) FROM history_message";
			ResultSet rs = s.executeQuery(sql);
			if(rs.next())
				return rs.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
