package org.forestpin.threadjs;


/**
 * Hello world!
 * 
 */
public class App {
    private static int port = 11010;
    private static String host = "localhost";


    public static void test() {
        System.out.println("parent");
    }


    public static void main(String[] args) {

        ThreadJS node = new ThreadJS();



        /*
        SendThread t = new SendThread();

        t.start();
        t.send();
        System.out.println("called 1");
        t.send();
        System.out.println("called 2");
         */

        /*
		assertEquals("JSON", jsonObject.getString("string"));
		assertEquals(1, jsonObject.getInt("integer"));
		assertEquals(2.0d, jsonObject.getDouble("double"), 0d);
		assertTrue(jsonObject.getBoolean("boolean"));
         */
    }
}
