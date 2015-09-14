package org.audenaerde;

public class RentalOption implements Comparable<RentalOption>
{
	public RentalOption(RentalRequest request, Car c)
	{
		this.c = c;
		this.totalPrice = c.basePrice + (request.kms - c.kmFree) * c.pricePerKm;
		this.distance = Haversine.haversine(request.mylat, request.mylong, c.latitude, c.longitude);
	}

	Car c;
	double totalPrice;
	double distance;

	@Override
	public int compareTo(RentalOption o)
	{
		return Double.compare(totalPrice, o.totalPrice);
	}
	
}