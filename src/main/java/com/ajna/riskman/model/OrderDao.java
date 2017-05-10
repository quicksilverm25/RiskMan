package com.ajna.riskman.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class OrderDao {

	PreparedStatement psInsert;
	PreparedStatement psDelete;
	PreparedStatement psUpdate;
	
	Connection mConnection;

	
	public OrderDao(Connection conn) throws SQLException{
		 //  Prepare the insert statement to use 
	    
	    mConnection = conn;
	    
	    createTable();
	    
	    psInsert = mConnection.prepareStatement("insert into Orders(EntryDate,Symbol,Quantity,Side,Price,OptType,ExpiryDate,StrikePrc,Status) values (?,?,?,?,?,?,?,?,?)");
	    psDelete = mConnection.prepareStatement("delete from Orders where OrderID = ?");
	    psUpdate = mConnection.prepareStatement("update Orders set ClosePrc = ?, CloseDate = ?, Status = ? where OrderID = ?");

	}
	 
	/**
	 * 
	 * 	
	//0. OrderID	int (inserted automatically)
	//1. EntryDate	Date (inserted automatically)
	//1. Symbol		String
	//2. Quantiy 	int
	//3. Side		String
	//4. Price		doube
	//5. OptionType	String
	//6. Expiry		String
	//7. Strike		String
	 */
	public String createString(){
		String createString = "CREATE TABLE Orders  "
		        +  "(OrderID INT NOT NULL GENERATED ALWAYS AS IDENTITY " 
		        +  "   CONSTRAINT OrderPK PRIMARY KEY, " 
		        +  " EntryDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
		        +  " Symbol VARCHAR(32) NOT NULL, "
		        +  " Quantity INT NOT NULL, "
		        +  " Side VARCHAR(8) NOT NULL, "
		        +  " Price DOUBLE NOT NULL, "
		        +  " OptType VARCHAR(8) NOT NULL, "
		        +  " ExpiryDate TIMESTAMP, "
		        +  " StrikePrc DOUBLE NOT NULL, "
		        +  " ClosePrc DOUBLE, "
		        +  " CloseDate TIMESTAMP, "
		        +  " Status VARCHAR(16) NOT NULL ) " ;
		return createString;
	}
	
	public boolean insertOrder(Date entryDate,String sym, int qty, String side,double prc, String optype, Date expryDate, double strkPrc) throws SQLException{

		psInsert.setDate(1,entryDate);
		psInsert.setString(2,sym);
        psInsert.setInt(3, qty);
        psInsert.setString(4, side);
        psInsert.setDouble(5, prc);
        psInsert.setString(6, optype);
        psInsert.setDate(7, expryDate);
        psInsert.setDouble(8, strkPrc);
        psInsert.setString(9, "Open");
        
        psInsert.executeUpdate(); 
        
        return true;
        
	}
	
	public boolean updateOrder(double clsPrc, Date clsDate, int ordId) throws SQLException{
		
		psUpdate.setDouble(1,  clsPrc);
		psUpdate.setDate(2,  clsDate);
		psUpdate.setString(3, "Closed");
		psUpdate.setInt(4,  ordId);
		
		psUpdate.executeUpdate();
			
		return true;
	}
	
	public boolean deleteOrder(int id) throws SQLException {
		
		psDelete.setInt(1, id);
		
		psDelete.executeUpdate();
		
		return true;
		
	}
	public String selectString() {
		String selectStr = "Select * from Orders where Status='OPen'; ";
		return selectStr;
	}
	
	public boolean createTable(){
		boolean ret = false;
		Statement s;
		try {
			s = mConnection.createStatement();
			 if (! Chk4Table())
		     {  
		          System.out.println (" . . . . creating table");
		          s.execute(this.createString());
		      }
		} catch (SQLException e) {
			 
			e.printStackTrace();
		}
	
	 return ret;
	}
	
	
	public Vector<Order> getOpenOrders() throws SQLException {
		
		Vector<Order> orders = new Vector<Order>();
		
		//   Select all records in the Orders table
		
		Statement s;
		
		 s = mConnection.createStatement();
		 
       ResultSet rset  = s.executeQuery("select OrderID, EntryDate, Symbol,Quantity, Side, Price, OptType, ExpiryDate, StrikePrc from Orders where Status='Open' order by EntryDate ");

     
       while (rset.next())
        {
              
              orders.add(new Order(rset.getInt(1), rset.getDate(2), rset.getString(3), rset.getInt(4), rset.getString(5), rset.getDouble(6),rset.getString(7), rset.getDate(8),rset.getDouble(9)));
         }
         
         rset.close(); 
         
         return orders;
	}
	
public Vector<Order> getCloseOrders() throws SQLException {
		
		Vector<Order> orders = new Vector<Order>();
		
		//   Select all records in the Orders table
		
		Statement s;
		
		 s = mConnection.createStatement();
		 
       ResultSet rset  = s.executeQuery("select OrderID, EntryDate, Symbol,Quantity, Side, Price, OptType, ExpiryDate, StrikePrc, ClosePrc, CloseDate from Orders where Status='Closed' order by EntryDate ");

     
       while (rset.next())
        {
              
              orders.add(new Order(rset.getInt(1), rset.getDate(2), rset.getString(3), rset.getInt(4), rset.getString(5), rset.getDouble(6),rset.getString(7), rset.getDate(8),rset.getDouble(9),rset.getDouble(10), rset.getDate(11)));
         }
         
         rset.close(); 
         
         return orders;
	}
	
	public void showTable() throws SQLException {
		
		 //   Select all records in the Orders table
		
		Statement s;
		
		 s = mConnection.createStatement();
		
        ResultSet rset  = s.executeQuery("select EntryDate, OrderID, Symbol,Quantity, Side, Price, OptType, ExpiryDate, StrikePrc from Orders where Status='Open' order by EntryDate ");

      
        while (rset.next())
         {
               System.out.println("Order:  " + rset.getTimestamp(1) + "  " + rset.getInt(2) + " " +  rset.getString(3));
          }
          
          rset.close();   
	}
	public void closeSession() throws SQLException{
		mConnection.close();
		psInsert.close();
	}
	

	 public  boolean Chk4Table ( ) throws SQLException {
	      try {
	         Statement s = mConnection.createStatement();
	         s.execute("update Orders set EntryDate = CURRENT_TIMESTAMP, Symbol = 'TestSymbol' where 1=3");
	      }  catch (SQLException sqle) {
	         String theError = (sqle).getSQLState();
	         //   System.out.println("  Utils GOT:  " + theError);
	         /** If table exists will get -  WARNING 02000: No row was found **/
	         if (theError.equals("42X05"))   // Table does not exist
	         {  return false;
	          }  else if (theError.equals("42X14") || theError.equals("42821"))  {
	             System.out.println("WwdChk4Table: Incorrect table definition. Drop table WISH_LIST and rerun this program");
	             throw sqle;   
	          } else { 
	             System.out.println("WwdChk4Table: Unhandled SQLException" );
	             throw sqle; 
	          }
	      }
	      //  System.out.println("Just got the warning - table exists OK ");
	      return true;
	 } 
}
