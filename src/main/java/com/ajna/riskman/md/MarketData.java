package com.ajna.riskman.md;

public class MarketData {

	String optionSymbol; //this should be INFY_25MAY2017_Sell_Put_1234
	String underlyingSymbol; //INFY
	String expiryDate;  //25MAY2017
	String side; //Sell
	String optionType; //Put
	double strike; //Strike price
	double underlyingSpotPrice; // spot price of the underlying 
	double optionLtp; //ltp of the option
	
	
	public MarketData(String underlyingSymbol, String expiryDate, String side, String optionType, double strike) {
		//
		this.underlyingSymbol = underlyingSymbol.toUpperCase();
		this.expiryDate = expiryDate;
		this.side = side;
		this.optionType = optionType;
		this.strike = strike;
		this.optionSymbol = underlyingSymbol + "_" + expiryDate + "_" + side + "_" + optionType + "_" + strike;
		this.underlyingSpotPrice = 0.0;
		this.optionLtp = 0.0;
	}
	
	public MarketData(String optSymb){
		//this one used when constructing using options symbol
		String[] myList = optSymb.split("_");
		this.optionSymbol = optSymb;
		this.underlyingSymbol  = myList[0];
		this.expiryDate = myList[1];
		this.side = myList[2];		
		this.optionType = myList[3];
		this.strike = Double.parseDouble(myList[4]);
		this.underlyingSpotPrice = 0.0;
		this.optionLtp = 0.0;
	}
	
	public String getOptionSymbol() {
		return optionSymbol;
	}
	public void setOptionSymbol(String optionSymbol) {
		this.optionSymbol = optionSymbol;
	}
	public String getUnderlyingSymbol() {
		return underlyingSymbol;
	}
	public void setUnderlyingSymbol(String underlyingSymbol) {
		this.underlyingSymbol = underlyingSymbol;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public String getOptionType() {
		return optionType;
	}
	public void setOptionType(String optionType) {
		this.optionType = optionType;
	}
	public double getUnderlyingSpotPrice() {
		return underlyingSpotPrice;
	}
	public void setUnderlyingSpotPrice(double underlyingSpotPrice) {
		this.underlyingSpotPrice = underlyingSpotPrice;
	}
	
	public double getStrike() {
		return strike;
	}

	public void setStrike(double strike) {
		this.strike = strike;
	}

	public double getOptionLtp() {
		return optionLtp;
	}
	public void setOptionLtp(double optionLtp) {
		this.optionLtp = optionLtp;
	}
}
