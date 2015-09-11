package org.audenaerde;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Hello world!
 *
 */
public class App
{
	static class Hire implements Comparable<Hire>
	{
		Hire(Car c, int kms)
		{
			this.c=c;
			
			this.totalPrice = c.basePrice + (kms-c.kmFree) * c.pricePerKm;
		}
		Car c;
		double totalPrice;
		@Override
		public int compareTo(Hire o)
		{
			return Double.compare(totalPrice, o.totalPrice);
		}
	}
	

	
	public static void main(String[] args) throws IOException
	{
		
		double mylat  = 52.0914852;
		double mylong =  5.12342760000001;


		System.out.println(Haversine.haversine(52.0914852, 5.12342760000001, 52.08846, 5.12143666666667));
		
		String startDate = "02-10-2015";
		String startTime = "09:00".replace(":", "%3A"  );
		String endDate = "04-10-2015";
		String endTime = "22:00".replace(":", "%3A"  );
		int kms = 400;
		
		String url = "https://www.snappcar.nl/search.aspx?lat="+ mylat+ " &lng=" + mylong;
		url+="&rl=&ps="+startDate+"&ps="+startTime+"&pe="+endDate+"&pe="+endTime+"&mip=&map=&miy=&may=&cm=&cb=&pis=undefined&o=Distance&view=l&rr=10&cci=&fkd=Geen%20selectie...&fo=0";
		url+="&bt=stationwagen";
		List<Car> cars = new ArrayList<Car>();
		for (int i = 0; i < 1; i++)
		{
			System.out.println("Getting cars page:" + i);
			cars.addAll(getCars(url, i));
			sleep(1000);

		}
		cars.stream().forEach(car -> getDetails(car));
		
		List<Hire> optionalHires = cars.stream().map( car -> new Hire(car, kms)).collect(Collectors.toList());
		Collections.sort(optionalHires);
		for (Hire h : optionalHires)
		{
			System.out.println(h.c.owner + " == " + h.totalPrice + " -- " + Haversine.haversine(mylat, mylong, h.c.latitude, h.c.longitude)  + "km -- " + h.c.url);
		}
		
		
	}

	private static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Car> getCars(String url, int i) throws IOException
	{
		String newUrl = url + ((i > 0) ? ("&pi=" + (i + 1)) : "");
		Document doc = Jsoup.connect(newUrl).get();
		Elements links = doc.select("li[data-href^=/auto-huren/auto]");

		List<Car> cars = new ArrayList<Car>();
		for (Element elem : links)
		{
			String carurl = elem.attr("data-href");
			String price = elem.select("div[class=price h1]").text();
			String owner = elem.select("img[class=owner-image]").attr("title");
			Car c = new Car(carurl, price);
			c.setOwner(owner);
			cars.add(c);
			
		}
		sleep(500);
		return cars;
	}
	
	public static double parseDouble(String s) throws ParseException
	{
		return DecimalFormat.getInstance(Locale.GERMAN).parse(s).doubleValue();
	}
	

	private static void getDetails(Car car)
	{
		System.out.println("Getting details of:" + car.url);
		
		try
		{
			String url = "https://www.snappcar.nl" + car.url;
			Document doc = Jsoup.connect(url).get();

		
			Pattern carDetailsVars  = Pattern.compile("snappCar.detailMapsVars = \\{.*carData..(.*?)\\};");
			Matcher m =carDetailsVars.matcher(doc.toString().replace("\n", "")); 
			if (m.find())
			{
				String array = m.group(1);
				Type collectionType = new TypeToken<List>(){}.getType();
				List items = new Gson().fromJson(array, collectionType);
				car.latitude = (double) items.get(1);
				car.longitude = (double) items.get(2);
			}

//			Elements dis = doc.select("a[href=#map]").select("li");
//			double distance = parseDouble(dis.text());
//			
//			car.setDistance(distance);
			
			Elements kmdetail = doc.select("li:contains(kilometers)");

			String kmdetailstr = kmdetail.text();
			car.setKmFree(getBaseFree(kmdetailstr));
			car.setPricePerKm(getPricePerKM(kmdetailstr));
			System.out.println(car);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getBaseFree(String s)
	{
		return Integer.parseInt(s.split(" ")[0]); //100 vrije kilometers per dag (€ 0,20 per extra km)
	}
	public static double getPricePerKM(String s)
	{
		String t = s.substring(s.indexOf("(€ ")+3);
		try
		{
			return DecimalFormat.getInstance(Locale.GERMAN).parse(t.split(" ")[0]).doubleValue();
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //100 vrije kilometers per dag (€ 0,20 per extra km)
		return 0;
	}
	
	public static String getCarData()
	{
	     String s = "   snappCar.detailMapsVars = {\n"+
	           "   carData: ['Seat Exeo Stationwagon', 52.0946, 5.12969033333333, 1, '323b1b82-2090-4c4c-9ccb-e9b08ef2bc85', '47,50', null, 0, 0, 0, '/image.aspx?ii=68429418-cbfe-4f14-bdcd-339c6a36240c&w=38']\n"+
	              " };";
	     
	     return s;
	}
}
