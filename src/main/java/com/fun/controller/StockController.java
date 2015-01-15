package com.fun.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/stock")
public class StockController {

	private Logger logger = LoggerFactory.getLogger(StockController.class);

	private final static String yahooHistoricalData = "http://real-chart.finance.yahoo.com/table.csv?s={TICKER}&a=01&b=01&c=2014&d=12&e=28&f=2014&g=d&ignore=.csv";
	private Map<String, String> tickers = new ConcurrentHashMap<String, String>();

	@RequestMapping(value = "/parse", method = RequestMethod.GET)
	public void parse(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("calling parse");

		if (!tickers.isEmpty()) {
			return;
		}

		ClassLoader loader = this.getClass().getClassLoader();
		File companyListFile = new File(loader.getResource("companylist1.csv")
				.getPath());

		logger.info("reading file");

		loadTickers(response, companyListFile);

		companyListFile = new File(loader.getResource("companylist2.csv")
				.getPath());

		loadTickers(response, companyListFile);

		companyListFile = new File(loader.getResource("companylist3.csv")
				.getPath());

		loadTickers(response, companyListFile);
	}

	private void loadTickers(HttpServletResponse response, File companyListFile)
			throws Exception {
		OutputStream out = null;
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(companyListFile));
			byte[] content = new byte[input.available()];
			input.readFully(content);

			String[] tmp = (new String(content)).split("\n");

			for (String t : tmp) {
				out = response.getOutputStream();
				String[] fields = t.split(",");

				tickers.put(fields[0].replaceAll("\"", ""),
						fields[5].replaceAll("\"", ""));

				out.write((fields[0].replaceAll("\"", "") + " "
						+ fields[5].replaceAll("\"", "") + "\n").getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	private Map<String, Double> growingTicker = new ConcurrentHashMap<String, Double>();
	private Map<String, Integer> growing5DaysTicker = new ConcurrentHashMap<String, Integer>();
	private Map<String, Integer> growing1DayTicker = new ConcurrentHashMap<String, Integer>();
	private Map<String, Integer> growingSameDayTicker = new ConcurrentHashMap<String, Integer>();

	/**
	 * Looking for continuously growing
	 * 
	 * Looking for continuous 5 day growth
	 * 
	 * Looking for continuous 1 day growth over 20 dollars
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/history", method = RequestMethod.GET)
	public void history(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		logger.info("Calling History");

		for (Map.Entry<String, String> entry : tickers.entrySet()) {
			try {
				String url = yahooHistoricalData.replace("{TICKER}", entry.getKey());

				HttpClient client = HttpClientBuilder.create().build();
				HttpGet httpRequest = new HttpGet(url);

				// add request header
				HttpResponse httpResponse = client.execute(httpRequest);

				BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

				String line = "";

				Deque<Stock> results = new ArrayDeque<Stock>();

				// dump first line
				line = rd.readLine();

				// assuming this is read latest to oldest
				while ((line = rd.readLine()) != null) {
					String[] data = line.split(",");
					Stock stock = new Stock();
					stock.setDate(new Integer(data[0].replaceAll("-", "")));
					stock.setLow(new Double(data[1]));
					stock.setClose(new Double(data[4]));
					results.add(stock);
				}

				boolean continuousAll = true;
				int growth = 0;
				int drop = 0;

				int index = 0;

				Stock startStock = results.removeLast();
				Stock cursor = new Stock();
				cursor.setDate(startStock.getDate());
				cursor.setLow(startStock.getLow());
				cursor.setClose(startStock.getClose());

				while (!results.isEmpty()) {
					index++;
					Stock stock = results.removeLast();

					if ((stock.getClose() - cursor.getLow()) > 3.0) {
						if (index > 5) {
							logger.info("5 Day: " + entry.getKey());
							Integer count = growing5DaysTicker.get(entry.getKey());
							if(count == null){
								growing5DaysTicker.put(entry.getKey(), 0);
							}else{
								growing5DaysTicker.put(entry.getKey(), count + 1);
							}
						}
					}
					
					if ((stock.getClose() - cursor.getLow()) < 0.0) {
						drop ++;
						continuousAll = false;
					}else{
						growth ++;
					}

					if ((stock.getClose() - cursor.getLow()) > 30.0) {
						logger.info("1 Day: " + cursor.getDate() + "-" + stock.getDate() + " " + entry.getKey());
						Integer count = growing1DayTicker.get(entry.getKey());
						if(count == null){
							growing1DayTicker.put(entry.getKey(), 0);
						}else{
							growing1DayTicker.put(entry.getKey(), count + 1);
						}
					}
					
					// same day stock movement
					if((stock.getClose() - stock.getLow()) > 30.00){
						logger.info("Same Day: " + stock.getDate() + " " + entry.getKey());
						Integer count = growingSameDayTicker.get(entry.getKey());
						if(count == null){
							growingSameDayTicker.put(entry.getKey(), 0);
						}else{
							growingSameDayTicker.put(entry.getKey(), count + 1);
						}
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

				logger.info("Growth: " + growth);
				logger.info("Drop: " + drop);
				if (continuousAll) {
					logger.info("Continuous Growth: " + entry.getKey());
					growingTicker.put(entry.getKey(), new Double((growth / (growth + drop)) * 100));
				}
			} catch (Exception e) {
				logger.error("Unable to query " + entry.getKey());
			}
		}
	}

	@RequestMapping(value = "/print", method = RequestMethod.GET)
	public void print(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		logger.info("Calling Print");

		ClassLoader loader = this.getClass().getClassLoader();
		File companyListFile = new File(loader.getResource("companylist1.csv")
				.getPath());
		File resultsFile = new File(companyListFile.getParent().concat(
				"/results." + System.currentTimeMillis()));
		resultsFile.createNewFile();

		print(resultsFile);
	}

	private void print(File resultsFile) throws IOException {
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(resultsFile));
			out.write("Growing Tickers\n".getBytes());
			for (Map.Entry<String, Double> entry : growingTicker.entrySet()) {
				if(entry.getValue() > 75){
				out.write((entry.getKey() + " " + entry.getValue() + "\n").getBytes());
				}
			}
			
			out.write("Growing Continuous 5 Days Tickers\n".getBytes());
			for (Map.Entry<String, Integer> entry : growing5DaysTicker.entrySet()) {
				out.write((entry.getKey() + "\t" + entry.getValue() + "\n").getBytes());
			}
			
			out.write("Growing 1 Day Over 50.00 Tickers\n".getBytes());
			for (Map.Entry<String, Integer> entry : growing1DayTicker.entrySet()) {
				out.write((entry.getKey() + "\t" + entry.getValue() + "\n").getBytes());
			}
			
			out.write("Same Day Growth Over 20.00 Tickers\n".getBytes());
			for (Map.Entry<String, Integer> entry : growingSameDayTicker.entrySet()) {
				out.write((entry.getKey() + "\t" + entry.getValue() + "\n").getBytes());
			}
		} finally {
			out.close();
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
