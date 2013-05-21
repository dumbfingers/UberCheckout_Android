package com.yeyaxi.android.ubercheckout.utilities;

public class ISO639Converter {
	public String convert(String language) {
		String result = "en";
		if (language.equals("Chinese"))
			result = "zh";
		else if (language.equals("English"))
			result = "en";
		else if (language.equals("German"))
			result = "de";
		else if (language.equals("French"))
			result = "fr";
		return result;
	}
}
