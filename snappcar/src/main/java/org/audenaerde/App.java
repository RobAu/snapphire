package org.audenaerde;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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
	static class Car
	{
		public Car(String carurl, String price)
		{
			this.url = carurl;
			try
			{
				this.basePrice = DecimalFormat.getInstance(Locale.GERMAN).parse(price.replace("€ ","")).doubleValue();
			} catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public String toString()
		{
			return "Car [url=" + url + ", name=" + name + ", basePrice=" + basePrice + ", pricePerKm=" + pricePerKm
					+ ", kmFree=" + kmFree + "]";
		}


		String url;
		String name;
		String id;
		String owner;
		double basePrice;
		double pricePerKm;
		int kmFree;
		
		public double getPricePerKm()
		{
			return pricePerKm;
		}

		public void setPricePerKm(double pricePerKm)
		{
			this.pricePerKm = pricePerKm;
		}

		public int getKmFree()
		{
			return kmFree;
		}

		public void setKmFree(int kmFree)
		{
			this.kmFree = kmFree;
		}
	}

	public static void main(String[] args) throws IOException
	{
		String startDate = "02-10-2015";
		String startTime = "09:00".replace(":", "%3A"  );
		String endDate = "04-10-2015";
		String endTime = "22:00".replace(":", "%3A"  );
		int kms = 400;
		
		String url = "https://www.snappcar.nl/search.aspx?lat=52.0914852&lng=5.12342760000001";
		url+="&rl=&ps="+startDate+"&ps="+startTime+"&pe="+endDate+"&pe="+endTime+"&mip=&map=&miy=&may=&cm=&cb=&pis=undefined&o=Distance&view=l&rr=10&cci=&fkd=Geen%20selectie...&fo=0";
		url+="&bt=stationwagen";
		List<Car> cars = new ArrayList<Car>();
		for (int i = 0; i < 1; i++)
		{
			System.out.println("Getting cars page:" + i);
			cars.addAll(getCars(url, i));
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		cars.stream().forEach(car -> getDetails(car));
		
		List<Hire> optionalHires = cars.stream().map( car -> new Hire(car, kms)).collect(Collectors.toList());
		Collections.sort(optionalHires);
		for (Hire h : optionalHires)
		{
			System.out.println(h.totalPrice + " -- " + h.c.url);
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
			cars.add(new Car(carurl, price));
		}
		return cars;
	}

	private static void getDetails(Car car)
	{
		try
		{
			String url = "https://www.snappcar.nl" + car.url;
			Document doc = Jsoup.connect(url).get();

			Elements kmdetail = doc.select("li:contains(kilometers)");

			String kmdetailstr = kmdetail.text();
			car.setKmFree(getBaseFree(kmdetailstr));
			car.setPricePerKm(getPricePerKM(kmdetailstr));
			System.out.println(car);
		} catch (Exception e)
		{
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
}
