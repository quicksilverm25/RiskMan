package com.ajna.riskman.app;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.ajna.riskman.dao.DaoController;
import com.ajna.riskman.model.OrderDao;

public class TestApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			DaoController daoc = new DaoController();
			
			OrderDao odao = new OrderDao(daoc.getDBConn());
			
			 
			
			
			String startDate="25MAY2017";
			SimpleDateFormat sdf1 = new SimpleDateFormat("DDMMMYYYY");
			java.util.Date date;
			try {
				date = sdf1.parse(startDate);
				java.sql.Date sqlDate  = new java.sql.Date(Calendar.getInstance().getTime().getTime());
				java.sql.Date sqlExpiryDate = new java.sql.Date(date.getTime()); 
				odao.insertOrder(sqlDate, "SBIN", 5000, "Sell", 5, "Put", sqlExpiryDate, 185);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			odao.showTable();
			System.out.println("-------------------------");
			odao.deleteOrder(401);
			
			odao.showTable();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
