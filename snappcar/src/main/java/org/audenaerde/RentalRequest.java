package org.audenaerde;

public class RentalRequest
{
	public enum CarType
	{
		STATION("stationwagen");

		private String s;
		CarType(String s)
		{
			this.s =s;
		}
		public String getUrlString()
		{
			return this.s;
		}
	};

	double mylat = 52.0914852;
	double mylong = 5.12342760000001;
	String startDate = "02-10-2015";
	String startTime = "09:00".replace(":", "%3A");
	String endDate = "04-10-2015";
	String endTime = "22:00".replace(":", "%3A");
	int kms = 400;
	int pages = 2;
	CarType type = CarType.STATION;

	public String getSearchUrl()
	{
		String url = "https://www.snappcar.nl/search.aspx?lat=" + mylat + " &lng=" + mylong;
		url += "&rl=&ps=" + startDate + "&ps=" + startTime + "&pe=" + endDate + "&pe=" + endTime
				+ "&mip=&map=&miy=&may=&cm=&cb=&pis=undefined&o=Distance&view=l&rr=10&cci=&fkd=Geen%20selectie...&fo=0";

		if (type != null)
		{
			url += "&bt=" + type.getUrlString();
		}
		return url;
	}
}
