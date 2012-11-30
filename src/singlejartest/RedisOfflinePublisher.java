package singlejartest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.charts.data.datacache.TickData;

public class RedisOfflinePublisher implements IStrategy {
	private IEngine engine = null;
	private int tagCounter = 0;
	private IConsole console;
	private List<ITick> tickList;
	private Jedis jedis;

	public void onStart(IContext context) throws JFException {
		BufferedReader csvFile = null;
		jedis = new Jedis("localhost");

		try {
			csvFile = new BufferedReader(new FileReader("data//EURUSD_Ticks_15.11.2012-15.11.2012.csv"));
			String line;
			tickList = new ArrayList<ITick>();

			csvFile.readLine(); //skip header line
			while ((line = csvFile.readLine()) != null) {
				String[] fields = line.split(",");
				String sTimestamp = fields[0];
				String sAsk = fields[1];
				String sBid = fields[2];
				String sAskVol = fields[3];
				String sBidVol = fields[4];

				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
					ITick tick = new TickData(sdf.parse(sTimestamp).getTime(),
							Double.parseDouble(sAsk),
							Double.parseDouble(sBid),
							Double.parseDouble(sAskVol),
							Double.parseDouble(sBidVol));

					tickList.add(tick);
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			csvFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onStop() throws JFException {
		for (IOrder order : engine.getOrders()) {
			order.close();
		}
		console.getOut().println("Stopped");
	}

	public void onTick(Instrument instrument, ITick tick) throws JFException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.getSerializationConfig().addMixInAnnotations(ITick.class, ITickMixin.class);
			String json = mapper.writeValueAsString(tick);

			Transaction t = jedis.multi();			
			//t.zadd(instrument.name(), tick.getTime(), json);						
			t.publish(instrument.name(), json);
			t.exec();
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) {
	}

	//count open positions
	protected int positionsTotal(Instrument instrument) throws JFException {
		int counter = 0;
		for (IOrder order : engine.getOrders(instrument)) {
			if (order.getState() == IOrder.State.FILLED) {
				counter++;
			}
		}
		return counter;
	}

	protected String getLabel(Instrument instrument) {
		String label = instrument.name();
		label = label.substring(0, 2) + label.substring(3, 5);
		label = label + (tagCounter++);
		label = label.toLowerCase();
		return label;
	}

	public void onMessage(IMessage message) throws JFException {
	}

	public void onAccount(IAccount account) throws JFException {
	}

	public List<ITick> getTickList() {
		return tickList;
	}

	public void setTickList(List<ITick> tickList) {
		this.tickList = tickList;
	}
}