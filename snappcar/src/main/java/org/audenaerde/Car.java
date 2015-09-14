package org.audenaerde;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

public class Car
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
	String imageUrl;





	double basePrice;
	double pricePerKm;

	int kmFree;
	public double latitude;
	public double longitude;
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
	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	public String getImageUrl()
	{
		return imageUrl;
	}

	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}

}