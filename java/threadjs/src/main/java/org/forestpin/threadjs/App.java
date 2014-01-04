package org.forestpin.threadjs;
import net.sf.json.*;


/**
 * Hello world!
 * 
 */
public class App {
	private static int port = 11010;
	private static String host = "localhost";
	
	
	public static void main(String[] args) {
		ThreadJS node = new ThreadJS(host, port);

		System.out.println("Hello World!");
		JSONObject jsonObject = new JSONObject().element("string", "JSON")
				.element("integer", "1").element("double", "2.0")
				.element("boolean", "true");
	
		System.out.println(jsonObject.toString());
		
		
		
		/*
		assertEquals("JSON", jsonObject.getString("string"));
		assertEquals(1, jsonObject.getInt("integer"));
		assertEquals(2.0d, jsonObject.getDouble("double"), 0d);
		assertTrue(jsonObject.getBoolean("boolean"));
		*/
	}
}
