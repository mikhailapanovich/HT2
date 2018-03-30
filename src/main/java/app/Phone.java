package app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phone {
	// Валидация телефонного номера.
	public static boolean validatePhoneNumber(String phoneNumber) {
    	Matcher matcher = Pattern.compile("[\\d+#-]{2,50}").matcher(phoneNumber);
    	return matcher.matches();  
	}
}
