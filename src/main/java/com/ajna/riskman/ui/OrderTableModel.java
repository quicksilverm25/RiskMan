package com.ajna.riskman.ui;

import javafx.beans.property.SimpleStringProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.ajna.riskman.app.RiskManGUI;
import com.ajna.riskman.md.MarketDataClientInterface;
import com.ajna.riskman.md.MarketDataGW;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
 
public class OrderTableModel  {
	//1. Symbol		String
	//2. Quantiy 	int
	//3. Side		String
	//4. Price		doube
	//5. OptionType	String
	//6. Expiry		String
	//7. Strike		String
	DecimalFormat formatter = new DecimalFormat("#,###.00");
	String mdKey;
	SimpleIntegerProperty oid = new SimpleIntegerProperty();
	SimpleStringProperty entry = new SimpleStringProperty();
	SimpleStringProperty symbol = new SimpleStringProperty();
	SimpleIntegerProperty quantity = new SimpleIntegerProperty();
	SimpleStringProperty side = new SimpleStringProperty();
	SimpleDoubleProperty price  = new SimpleDoubleProperty();
	SimpleDoubleProperty ltprice  = new SimpleDoubleProperty();
	SimpleStringProperty optionType  = new SimpleStringProperty();
	SimpleStringProperty expiry  = new SimpleStringProperty();
	SimpleDoubleProperty strike   = new SimpleDoubleProperty();
	SimpleDoubleProperty spot   = new SimpleDoubleProperty();
	
	SimpleDoubleProperty dayPL   = new SimpleDoubleProperty();
	SimpleDoubleProperty posPL   = new SimpleDoubleProperty();
	
	SimpleStringProperty dayPLStr   = new SimpleStringProperty();
	SimpleStringProperty posPLStr  = new SimpleStringProperty();
	
	public OrderTableModel( Integer mOid, String mEntry, String mSymbol, Integer mQuantity, String mSide,
			Double mPrice, String mOptionType, String mExpiry, Double mStrike) {
		//super();
		 
		this.oid.set(mOid);
		this.entry.set(mEntry);
		this.symbol.set(mSymbol.toUpperCase());
		this.quantity.set(mQuantity);
		this.side.set(mSide);
		this.price.set(mPrice);
		this.optionType.set(mOptionType);
		this.expiry.set(mExpiry.toUpperCase());
		this.strike.set(mStrike);
		
		this.ltprice.set(0);
		this.spot.set(0);
		
		this.dayPL.set(0);
		this.posPL.set(0);
		
		this.dayPLStr.set("0.00");
		this.posPLStr.set("0.00");
		
		mdKey = mSymbol.toUpperCase() + "_" + mExpiry.toUpperCase() + "_" + mSide + "_" + mOptionType + "_"  + mStrike ;
 
	}
 




	public String getMdKey() {
		return mdKey;
	}



	public void setMdKey(String mdKey) {
		this.mdKey = mdKey;
	}



	public Integer getOid() {
		return oid.get();
	}

	public void setOid(Integer oid) {
		this.oid.set(oid);
	}

	
	public String getEntry() {
		return entry.get();
	}

	public void setEntry(String entry) {
		this.entry.set(entry);
	}

	public String getSymbol() {
		return symbol.get();
	}

	public void setSymbol(String symbol) {
		this.symbol.set(symbol);
	}

	public Integer getQuantity() {
		return quantity.get();
	}

	public void setQuantity(Integer quantity) {
		this.quantity.set(quantity);
	}

	public String getSide() {
		return side.get();
	}

	public void setSide(String side) {
		this.side.set(side);
	}

	public Double getPrice() {
		return price.get();
	}

	public void setPrice(Double price) {
		this.price.set(price);
	}

	public String getOptionType() {
		return optionType.get();
	}

	public void setOptionType(String optionType) {
		this.optionType.set(optionType);
	}

	public String getExpiry() {
		return expiry.get();
	}

	public void setExpiry(String expiry) {
		this.expiry.set(expiry);
	}

	public Double getStrike() {
		return strike.get();
	}

	public void setStrike(Double strike) {
		this.strike.set(strike);
	}

	public Double getLtprice() {
		return ltprice.get();
	}

	public void setLtprice(Double ltprice) {
		this.ltprice.set(ltprice);
	}

	public Double getSpot() {
		return spot.get();
	}

	public void setSpot(Double spot) {
		this.spot.set(spot);
	}


	public Double getDayPL() {
		return dayPL.get();
	}



	public void setDayPL(Double dayPL) {
		this.dayPL.set(dayPL);
		this.setDayPLStr(formatter.format(dayPL));
	}



	public Double getPosPL() {
		return posPL.get();
	}



	public void setPosPL(Double posPL) {
		this.posPL.set(posPL);
		this.setPosPLStr(formatter.format(posPL));
	}
 


	public String getDayPLStr() {
		return dayPLStr.get();
	}



	public void setDayPLStr(String dayPLStr) {
		this.dayPLStr.set(dayPLStr);
	}



	public String getPosPLStr() {
		return posPLStr.get();
	}



	public void setPosPLStr(String posPLStr) {
		this.posPLStr.set(posPLStr);
	}

	
	public void computePL() {
		 if(side.get().equalsIgnoreCase("Buy")){
			 
			 this.setDayPL(quantity.get() * ( ltprice.get() - price.get()));
			 
			 if(optionType.get().equalsIgnoreCase("Call")){
				 
				 this.setPosPL(quantity.get() * (spot.get() - strike.get() - price.get() ));
				 
			 } else { //Put
				  
				 this.setPosPL(quantity.get() * (strike.get() - spot.get() - price.get() ));
			 }
		 } else { //Sell
			 
			 this.setDayPL(quantity.get() * (price.get() - ltprice.get() ));
			 
			 double prem = quantity.get() * price.get();
			 double factor, redu;
			 
			 if(optionType.get().equalsIgnoreCase("Call")){
				 factor = (strike.get() - spot.get() ); 
			 } else { //Put				
				 factor = (spot.get() - strike.get());  
			 }
			 if(factor < 0){
					redu = quantity.get() * factor;
					prem += redu;
				 }
			 this.setPosPL(prem);
		 }
		
		
	}

}
