package com.ajna.riskman.pricing;

 
public class BlackScholes {

	// The Abramowitz & Stegun (1964) numerical approximation
	// below uses six constant values in its formula.
	private static final double P = 0.2316419;
	private static final double B1 = 0.319381530;
	private static final double B2 = -0.356563782;
	private static final double B3 = 1.781477937;
	private static final double B4 = -1.821255978;
	private static final double B5 = 1.330274429;

	/**
	 * 
	 * @param callOption
	 *            boolean true/false
	 * @param s
	 *            = Spot price of underlying stock/asset
	 * @param k
	 *            = Strike price
	 * @param r
	 *            = Risk free annual interest rate continuously compounded
	 * @param t
	 *            = Time in years until option expiration (maturity)
	 * @param v
	 *            = Implied volatility of returns of underlying stock/asset
	 * @return
	 */
	public static double calculate(boolean callOption, double s, double k,
			double r, double t, double v) {
	
		System.out.println("    ----- ");
		System.out.println(" in BlackScholesFormula:calculate(" + callOption + "," + s + "," + k + "," + r + "," + t + "," + v);
		
		double blackScholesOptionPrice = 0.0;
		
		if (callOption) {
			double cd1 = cumulativeDistribution(d1(s, k, r, t, v));
			double cd2 = cumulativeDistribution(d2(s, k, r, t, v));
			
			blackScholesOptionPrice = s * cd1 - k * Math.exp(-r * t) * cd2;
		} else {
			double cd1 = cumulativeDistribution(-d1(s, k, r, t, v));
			double cd2 = cumulativeDistribution(-d2(s, k, r, t, v));
			
			blackScholesOptionPrice = k * Math.exp(-r * t) * cd2 - s * cd1;
		}
		
		System.out.print("in BlackScholesFormula.callOption() result-[" + blackScholesOptionPrice + "] ");
		System.out.println("  callOption-" + callOption + ", spot-" + s + ", strike-" + k + ", r-" + r + ", T-" + t + ", v-" + v);
		
		return blackScholesOptionPrice;
	}
	

	/**
	 * 
	 * @param s
	 *            = Spot price of underlying stock/asset
	 * @param k
	 *            = Strike price
	 * @param r
	 *            = Risk free annual interest rate continuously compounded
	 * @param t
	 *            = Time in years until option expiration (maturity)
	 * @param v
	 *            = Implied volatility of returns of underlying stock/asset
	 * @return
	 */
	private static double d1(double s, double k, double r, double t, double v) {
		
		double top = Math.log(s / k) + (r + Math.pow(v, 2) / 2) * t;
		double bottom = v * Math.sqrt(t);
		
		return top / bottom;
	}

	/**
	 * 
	 * @param s
	 *            = Spot price of underlying stock/asset
	 * @param k
	 *            = Strike price
	 * @param r
	 *            = Risk free annual interest rate continuously compounded
	 * @param t
	 *            = Time in years until option expiration (maturity)
	 * @param v
	 *            = Implied volatility of returns of underlying stock/asset
	 * @return
	 */
	private static double d2(double s, double k, double r, double t, double v) {
		return d1(s, k, r, t, v) - v * Math.sqrt(t);
	}

	public static double cumulativeDistribution(double x) {
		
		//System.out.println(" in BlackScholesFormula:cumulativeDitibution(" + x + ")");
		
		double t = 1 / (1 + P * Math.abs(x));
		double t1 = B1 * Math.pow(t, 1);
		double t2 = B2 * Math.pow(t, 2);
		double t3 = B3 * Math.pow(t, 3);
		double t4 = B4 * Math.pow(t, 4);
		double t5 = B5 * Math.pow(t, 5);
		double b = t1 + t2 + t3 + t4 + t5;
		
		double snd = standardNormalDistribution(x); //for testing
		double cd = 1 - (snd * b);
		
		double resp = 0.0;
		if( x < 0 ) {
			resp = 1 - cd;
		} else {
			resp = cd;
		}
		
		//return x < 0 ? 1 - cd : cd;
		return resp;
	}

	/**
	 * The Abramowitz & Stegun numerical approximation above uses six constant
	 * values in its formula. However it also relies on another function in turn
	 * – the standard normal probability density function (PDF)
	 * 
	 * @param x
	 * @return
	 */
	public static double standardNormalDistribution(double x) {
		
		//System.out.println(" in BlackScholesFormula:standardNormalDistribution(" + x + ")");
		double top = Math.exp(-0.5 * Math.pow(x, 2));
		double bottom = Math.sqrt(2 * Math.PI);
		double resp = top / bottom;		
		
		return resp;
	}
	public static void main(String[] args) {
 
		boolean c = false;
		double s = 326;
		double k = 320;
		double r = 10;
		double t = 20; t /= 365;
		double v = 33.83;
		
		double prc =  calculate(c,   s,   k,   r,   t,   v);
		
		System.out.println( "Option : " + ( c ? "Call" : "Put"));
		System.out.println( "Stock Price : " + s);
		System.out.println( "Strike Price: " + k);
		System.out.println( "Rate: " + r);
		System.out.println( "Time: " + t);
		System.out.println( "IV: " + v);
		System.out.println( "Option Price: " + prc);
	}

}