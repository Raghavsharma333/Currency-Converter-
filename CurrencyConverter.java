package io.raghavsharma333.currency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * This class functions as a <tt>Currency Converter</tt> for various types of currencies.
 * All supported currencies are determined by the <tt>Currencies</tt> enum constant.<br><br>
 * 
 * All definitions for the conversion rates come from <tt>Yahoo! Finance</tt>, and are
 * updated no more frequently than every 24 hours to avoid wasting processing resources.
 * 
 * @author Craig
 * @version 2.0
 */
public final class CurrencyConverter implements Serializable{

	/**
	 * The SerialVersionUID for this class.<br>
	 * Increment if you need to break backwards compatibility.
	 */
	private static final long serialVersionUID = 7983890980404533949L;
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger("currency.CurrencyConverter");

	/**
	 * The last Date/Time that the conversion definitions
	 * were updated.
	 * 
	 *  TODO find a way to properly save this value to be re-loaded in the future
	 */
	private static DateTime lastAccessed = null;
	
	/**
	 * Basic lock object for synchronizing access to usdToList.
	 */
	private static Object usdToListLock = new Object();
	
	/**
	 * The list of conversion definitions. They are in the format
	 * of "usdTo". That means you multiply to convert US Dollars
	 * to another currency, and you divide to convert another
	 * currency into US Dollars.
	 */
	private static List<BigDecimal> usdToList;
	
	/**
	 * Runs the update script. Connects to the <tt>Yahoo! Finance</tt>
	 * servers and updates the definitions for the currency conversion
	 * formulas, saving them in a <tt>List</tt> object.
	 * 
	 * @throws IOException if unable to download the data.
	 */
	private static void runUpdate() throws IOException{
		BufferedReader in = null;
		usdToList = new ArrayList<>();
		try{
			String start = "http://download.finance.yahoo.com/d/quotes.csv?e=.csv&f=l1&s=";
			String mid = "=X,";
			
			StringBuilder urlString = new StringBuilder(start);
			
			for(CurrencyType c : CurrencyType.values()){
				String code = c.getCode();
				urlString.append("USD" + code + mid);
			}
			
			URL url = new URL(urlString.toString());
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			synchronized(usdToListLock){
				for(@SuppressWarnings("unused") CurrencyType c : CurrencyType.values()){
					BigDecimal bd = new BigDecimal(in.readLine());
					usdToList.add(bd);
				}
			}
			
			
		} catch(MalformedURLException ex){
			LOGGER.logp(Level.SEVERE, CurrencyConverter.class.getName(), 
					"runUpdate", "Exception", ex);
			throw new IOException(ex);
		} catch(SocketException ex){
			LOGGER.logp(Level.SEVERE, CurrencyConverter.class.getName(), 
					"runUpdate", "Exception", ex);
			throw new IOException(ex);
		} finally{
			if(in != null){
				try{
					in.close();
				}catch(IOException ex){
					LOGGER.logp(Level.SEVERE, CurrencyConverter.class.getName(), 
							"runUpdate", "Exception", ex);
				}
			}
		}
	}
}
