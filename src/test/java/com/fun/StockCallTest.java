package com.fun;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fun.controller.StockController.Stock;

public class StockCallTest extends TestCase {

	private List<String> growingTicker = new CopyOnWriteArrayList<String>();
	private Map<String, String> growing5DaysTicker = new ConcurrentHashMap<String, String>();
	private Map<String, String> growing1DayTicker = new ConcurrentHashMap<String, String>();
	private Map<String, String> growingSameDayTicker = new ConcurrentHashMap<String, String>();

	private final static String yahooHistoricalData = "http://real-chart.finance.yahoo.com/table.csv?s={TICKER}&a=11&b=12&c={STARTYEAR}&d=11&e=28&f=2014&g=d&ignore=.csv";
	private Map<String, String> tickers = new ConcurrentHashMap<String, String>();

	public void testCall() throws Exception {

		ClassLoader loader = this.getClass().getClassLoader();
		File companyListFile = new File(loader.getResource("companylist1.csv")
				.getPath());

		File resultsFile = new File(companyListFile.getParent().concat(
				"/results.txt"));

		resultsFile.createNewFile();

		String url = yahooHistoricalData.replace("{TICKER}", "AAPL").replace(
				"{STARTYEAR}", "1999");

		System.out.println(url);

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet(url);

		// add request header
		HttpResponse httpResponse = client.execute(httpRequest);

		System.out.println("Response Code : "
				+ httpResponse.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				httpResponse.getEntity().getContent()));

		String line = "";

		Deque<Stock> results = new ArrayDeque<Stock>();

		// read out the first line
		line = rd.readLine();

		// assuming this is read latest to oldest
		while ((line = rd.readLine()) != null) {
			String[] data = line.split(",");
			Stock stock = new Stock();
			stock.setDate(new Integer(data[0].replaceAll("-", "")));
			stock.setLow(new Double(data[3]));
			stock.setClose(new Double(data[4]));
			System.out.println(line + "\n");
			results.add(stock);
		}

		boolean continuousAll = true;

		int index = 0;

		Stock startStock = results.removeLast();
		Stock cursor = new Stock();
		cursor.setDate(startStock.getDate());
		cursor.setLow(startStock.getLow());
		cursor.setClose(startStock.getClose());

		while (!results.isEmpty()) {
			index++;
			Stock stock = results.removeLast();

			if ((stock.getClose() - cursor.getLow()) > 5.0) {
				if (index > 5) {
					growing5DaysTicker.put(
							startStock.getDate() + "-" + stock.getDate(),
							"AAPL");
				}
			} else {
				continuousAll = false;
			}

			if ((stock.getClose() - cursor.getLow()) > 20.0) {
				growing1DayTicker.put(cursor.getDate() + "-" + stock.getDate(),
						"AAPL");
			}
			
			// same day stock movement
			if((stock.getClose() - stock.getLow()) > 5.00){
				growingSameDayTicker.put(""+stock.getDate(), "AAPL");
			}

			cursor = new Stock();
			cursor.setDate(stock.getDate());
			cursor.setLow(stock.getLow());
			cursor.setClose(stock.getClose());

			if (index == 4) {
				startStock.setDate(cursor.getDate());
				startStock.setLow(cursor.getLow());
				startStock.setClose(cursor.getClose());
				index = 0;
			}
		}

		if (continuousAll) {
			growingTicker.add("AAPL");
		}

		print(resultsFile);
	}

	private void print(File resultsFile) throws IOException {
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(resultsFile));
			out.write("Growing Tickers\n".getBytes());
			for (String ticker : growingTicker) {
				out.write((ticker + ",").getBytes());
			}
			out.write("Growing Continuous 5 Days Tickers\n".getBytes());
			for (Map.Entry<String, String> entry : growing5DaysTicker
					.entrySet()) {
				out.write(("AAPL" + "\t" + entry.getKey() + "\n").getBytes());
			}
			out.write("Growing 1 Day Over 20.00 Tickers\n".getBytes());
			for (Map.Entry<String, String> entry : growing1DayTicker.entrySet()) {
				out.write(("AAPL" + "\t" + entry.getKey() + "\n").getBytes());
			}
			out.write("Same Day Day Over 5.00 Tickers\n".getBytes());
			for (Map.Entry<String, String> entry : growingSameDayTicker.entrySet()) {
				out.write(("AAPL" + "\t" + entry.getKey() + "\n").getBytes());
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public class Stock {

		private Integer date;
		private Double low;
		private Double close;

		public Integer getDate() {
			return date;
		}

		public void setDate(Integer date) {
			this.date = date;
		}

		public Double getLow() {
			return low;
		}

		public void setLow(Double low) {
			this.low = low;
		}

		public Double getClose() {
			return close;
		}

		public void setClose(Double close) {
			this.close = close;
		}

	}
}
