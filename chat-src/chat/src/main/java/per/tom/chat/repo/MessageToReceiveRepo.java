package per.tom.chat.repo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import per.tom.chat.controller.Utility;
import per.tom.chat.model.MessageToReceive;

public class MessageToReceiveRepo {
	public List<MessageToReceive> findByReceiveUser(String receiveUser){
		List<MessageToReceive> res = new ArrayList<>();
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "SELECT * FROM message_to_receive WHERE receive_user=\""+receiveUser+"\"";
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()) {
				MessageToReceive mtr = new MessageToReceive();
				mtr.setDate(rs.getDate("date"));
				mtr.setId(rs.getLong("id"));
				mtr.setMessage(rs.getString("message"));
				
				mtr.setMessage(Utility.readFile(mtr.getMessage()));
				
				mtr.setReceiveUser(rs.getString("receive_user"));
				mtr.setSendUser(rs.getString("send_user"));
				res.add(mtr);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	public List<MessageToReceive> findAll(){
		
		return null;
	}
	public void deleteById(long id) {
		try {
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			String sql = "DELETE FROM message_to_receive WHERE id=\""+id+"\"";
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
			String sql = "SELECT MAX(id) FROM message_to_receive";
			ResultSet rs = s.executeQuery(sql);
			if(rs.next())
				return rs.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	public void save(MessageToReceive mtr) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Utility.SQL_DATE_FORMAT);
			Connection con = Utility.getSqlConnection();
			Statement s = con.createStatement();
			
			mtr.setMessage(Utility.saveAsFile(mtr.getMessage()));
			
			String sql = String.format("INSERT INTO message_to_receive "
					+ "(id,message,send_user,receive_user,date)"
					+ "VALUES(%d,\"%s\",\"%s\",\"%s\",\"%s\")",
					mtr.getId(),
					mtr.getMessage(),
					mtr.getSendUser(),
					mtr.getReceiveUser(),
					sdf.format(mtr.getDate()));
			s.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
