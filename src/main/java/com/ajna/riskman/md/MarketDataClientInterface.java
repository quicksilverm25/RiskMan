package com.ajna.riskman.md;

public interface MarketDataClientInterface {

	//public void onLastTradePriceUpdate(double ltp);
	//public void onSpotPriceUpdate(double sprc);
	 public void onMDUpdate(String symb,double ltp,double sprc);
}
