package com.ajna.riskman.model;

import java.sql.Date;

public class Order {

	//1. Symbol		String
	//2. Quantiy 	int
	//3. Side		String
	//4. Price		doube
	//5. OptionType	String
	//6. Expiry		String
	//7. Strike		String
	
	
	int orderID;
	Date createdDate;
	String symbol;
	int quantity;
	String side;
	double price;
	String optType;
	Date	expiryDate;
	double	strike;
	double  clsPrice;
	Date	clsDate;
	
	
	
	
	
	public Order(int orderID, Date createdDate, String symbol, int quantity, String side, double price, String optType,
			Date expiryDate, double strike) {
		super();
		this.orderID = orderID;
		this.createdDate = createdDate;
		this.symbol = symbol;
		this.quantity = quantity;
		this.side = side;
		this.price = price;
		this.optType = optType;
		this.expiryDate = expiryDate;
		this.strike = strike;
	}
	
	public Order(int orderID, Date createdDate, String symbol, int quantity, String side, double price, String optType,
			Date expiryDate, double strike, double clsPrice, Date clsDate) {
		super();
		 
		this.orderID = orderID;
		this.createdDate = createdDate;
		this.symbol = symbol;
		this.quantity = quantity;
		this.side = side;
		this.price = price;
		this.optType = optType;
		this.expiryDate = expiryDate;
		this.strike = strike;
		
		this.clsPrice = clsPrice;
		this.clsDate = clsDate;
		
	}
	
	public int getOrderID() {
		return orderID;
	}
	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getOptType() {
		return optType;
	}
	public void setOptType(String optType) {
		this.optType = optType;
	}
	public Date getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	public double getStrike() {
		return strike;
	}
	public void setStrike(double strike) {
		this.strike = strike;
	}

	public double getClsPrice() {
		return clsPrice;
	}

	public void setClsPrice(double clsPrice) {
		this.clsPrice = clsPrice;
	}

	public Date getClsDate() {
		return clsDate;
	}

	public void setClsDate(Date clsDate) {
		this.clsDate = clsDate;
	}

	@Override
	public String toString() {
		return "Order [orderID=" + orderID + ", createdDate=" + createdDate + ", symbol=" + symbol + ", quantity="
				+ quantity + ", side=" + side + ", price=" + price + ", optType=" + optType + ", expiryDate="
				+ expiryDate + ", strike=" + strike + "]";
	}
	
	
}
