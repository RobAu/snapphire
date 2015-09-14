package org.audenaerde;

import java.io.IOException;
import java.lang.reflect.Type;
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
public class SnappCarSmartRental
{
	public static void main(String[] args) throws IOException
	{
		int pages = 2;
		RentalRequest request = new RentalRequest();

		List<RentalOption> rentalOptions = getRentalOptions(pages, request);
		for (RentalOption h : rentalOptions)
		{
			System.out.println(h.asTabbedString());
		}

	}

	private static List<RentalOption> getRentalOptions(int pages, RentalRequest request) throws IOException
	{
		List<Car> cars = new ArrayList<Car>();
		String url = request.getSearchUrl();

		for (int i = 0; i < pages; i++)
		{
			System.out.println("Getting cars page:" + i);
			cars.addAll(getCars(url, i));
			sleep(1000);

		}
		cars.stream().forEach(car -> getDetails(car));
		List<RentalOption> rentalOptions = cars.stream().map(car -> new RentalOption(request, car))
				.collect(Collectors.toList());
		Collections.sort(rentalOptions);

		return rentalOptions;
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

			Pattern carDetailsVars = Pattern.compile("snappCar.detailMapsVars = \\{.*carData..(.*?)\\};");
			Matcher m = carDetailsVars.matcher(doc.toString().replace("\n", ""));
			if (m.find())
			{
				String array = m.group(1);
				Type collectionType = new TypeToken<List>()
				{
				}.getType();
				List items = new Gson().fromJson(array, collectionType);
				car.latitude = (double) items.get(1);
				car.longitude = (double) items.get(2);
			}

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
		return Integer.parseInt(s.split(" ")[0]); // 100 vrije kilometers per
													// dag (€ 0,20 per extra km)
	}

	public static double getPricePerKM(String s)
	{
		String t = s.substring(s.indexOf("(€ ") + 3);
		try
		{
			return DecimalFormat.getInstance(Locale.GERMAN).parse(t.split(" ")[0]).doubleValue();
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 100 vrije kilometers per dag (€ 0,20 per extra km)
		return 0;
	}

	public static String getCarData()
	{
		String s = "   snappCar.detailMapsVars = {\n"
				+ "   carData: ['Seat Exeo Stationwagon', 52.0946, 5.12969033333333, 1, '323b1b82-2090-4c4c-9ccb-e9b08ef2bc85', '47,50', null, 0, 0, 0, '/image.aspx?ii=68429418-cbfe-4f14-bdcd-339c6a36240c&w=38']\n"
				+ " };";

		return s;
	}
}
