package com.ajna.riskman.md;


import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MarketDataGW {

 
	private HashMap<String, Vector<MarketDataClientInterface>> m_subscriptions;
	private HashMap<String, MarketData> m_mdMap;
	private Timer m_scheduler;

	public MarketDataGW() {
		
		m_subscriptions = new HashMap<String, Vector<MarketDataClientInterface>>();

		m_mdMap = new HashMap<String, MarketData>();
		
		//mainApp startes the timer now
		//m_scheduler = new Timer();
    	//m_scheduler.scheduleAtFixedRate(new refresherTimerTask(), 60*1000,60*1000);

	}

	/**
	 * TODO: not thread safe at the moment
	 */
	public void subscribe(String sym, MarketDataClientInterface bk) {
		
		Vector<MarketDataClientInterface> bkvect;
		System.out.println("MarketDataGW: Subscribe for " + sym);
		if (m_subscriptions.containsKey(sym)) {
			bkvect = m_subscriptions.get(sym);
		} else {
			bkvect = new Vector<MarketDataClientInterface>();
			m_subscriptions.put(sym, bkvect);
		}
		bkvect.add(bk);
		
	}


	/**
	 * TODO: not thread safe at the moment
	 */
	public void unSubscribe(String sym, MarketDataClientInterface bk) {

		Vector<MarketDataClientInterface> mdci = m_subscriptions.get(sym);

		if (mdci != null) {
			mdci.remove(bk);
		} else {

		}
		if (mdci.isEmpty()) {
			m_subscriptions.remove(sym);
		}
	}

	 
	public void unSubscribeAll() {
		//just creating a new hashmap is more effiecient?
		m_subscriptions = new HashMap<String, Vector<MarketDataClientInterface>>();
	}
	/**
	 * 
	 * @param Refresh thread to call this method
	 */
	public void refreshMarketData() {
		m_subscriptions.keySet();
		
		MarketData md;
		for (String symb : m_subscriptions.keySet()) {
			 md = m_mdMap.get(symb);
			 if (md == null) {
				 md = new MarketData(symb);
				 m_mdMap.put(symb, md);
			 }
			 refreshMarketData(md);
			 //publish updates
			 for (MarketDataClientInterface client : m_subscriptions.get(symb) ) {
				 //client.onLastTradePriceUpdate(md.getOptionLtp());
				 //client.onSpotPriceUpdate(md.getUnderlyingSpotPrice());
				 client.onMDUpdate(symb,md.getOptionLtp(),md.getUnderlyingSpotPrice());
			 }
		}
	}

	/**
	 * 
	 * @param sym
	 */
	public void refreshMarketData(MarketData md) {

		Document doc;
		
		try {

			
			String opturl = "https://www.nseindia.com/live_market/dynaContent/live_watch/option_chain/optionKeys.jsp?symbol="
					+ md.getUnderlyingSymbol() + "&date=" + md.getExpiryDate();
			
			//System.out.println("URL is :[" + opturl + "]");
			doc = Jsoup.connect(opturl).get();
			Elements prcStr = doc.select("table").eq(0).select("td").eq(1).select("span").eq(0);

			
			String priceStr = prcStr.select("b").tagName("b").html();
			String a[] = priceStr.split(" ");

			String tradeTime = doc.select("table").eq(0).select("td").eq(1).select("span").eq(1).text();

			//String a[] = priceStr.split(" ");
			//System.out.println("TradeTime : " + tradeTime);
			//System.out.println("Price of " + a[0] + " is: " + a[1]);
			md.setUnderlyingSpotPrice(Double.parseDouble(a[1]));
			
			
			int i = 0;
			Elements rows = doc.select("table").eq(2).select("tbody").select("tr");
			//System.out.println("total rows: " + rows.size());
			double strikelevel = 0.0;
			double ltp = 0.0;
			
			for (Element row : rows) {
				if ((i + 1) >= rows.size())
					continue;
				String cltp = row.select("td").eq(5).text();
				String strk = row.select("td").eq(11).text();
				String pltp = row.select("td").eq(17).text();
				
				if (!strk.equals("-") && !strk.isEmpty())
					strikelevel = Double.parseDouble(strk.replaceAll(",", ""));
				
				if(strikelevel < md.getStrike()) 
					continue;
				else if (strikelevel == md.getStrike()){
					if (md.getOptionType().equalsIgnoreCase("Call")){
						if (!cltp.equals("-") && !cltp.isEmpty())
							ltp = Double.parseDouble(cltp.replaceAll(",", ""));
					} else { //for Put
						if (!pltp.equals("-") && !pltp.isEmpty())
							ltp = Double.parseDouble(pltp.replaceAll(",", ""));
					}
					md.setOptionLtp(ltp);
				}
				i++;
			}
 		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * 
	 * 
	 *
	 */
	private class refresherTimerTask extends TimerTask {
		private final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    public void run() {
	    	Date date = new Date(); 
	    	System.out.println(sdf.format(date) + " Refresher Timer : " + Thread.currentThread().getName() );
	    	refreshMarketData();
	    }
	}
	
 
}



 

