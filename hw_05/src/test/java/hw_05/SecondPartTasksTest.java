package hw_05;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        assertEquals(
        		Arrays.asList("askdlcewline kabra"), 
        		SecondPartTasks.findQuotes(
        				Arrays.asList("existingFile.txt", "nonExistingFile.txt"),
        				"line"));
        assertEquals(
        		Arrays.asList("abracdadabra dmvdhg",
        				"tksdl",
        				"", 
        				"askdlcewline kabra",
        				""), 
        		SecondPartTasks.findQuotes(
        				Arrays.asList("existingFile.txt", "nonExistingFile.txt"),
        				""));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI/4, SecondPartTasks.piDividedBy4(), 1e-2);
    }

    @Test
    public void testFindPrinter() {
    	Map<String, List<String>> testMap = new HashMap<>();
    	testMap.put("Auth1", Arrays.asList(
    			"ab",
    			"bc",
    			"cd",
    			"ef"));
    	
    	testMap.put("Auth2", Arrays.asList(
    			"abсd",
    			"bc",
    			"cdk"));
    	
    	testMap.put("Auth3", Arrays.asList());
    	
        assertEquals("Auth2",
        		SecondPartTasks.findPrinter(testMap));
        
        testMap = new HashMap<>();
        
        assertNull(SecondPartTasks.findPrinter(testMap));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("Картошка", 150);
        m1.put("Лук", 200);
        m1.put("Дрова", 10);
        
        Map<String, Integer> m2 = new HashMap<>();
        
        m2.put("Бензин", 90);
        m2.put("Картошка", 300);
        m2.put("Лук", 5);
        
        Map<String, Integer> etalon = new HashMap<>();
        
        etalon.put("Картошка", 450);
        etalon.put("Дрова", 10);
        etalon.put("Бензин", 90);
        etalon.put("Лук", 205);
        
        assertEquals(etalon, SecondPartTasks.calculateGlobalOrder(Arrays.asList(m1, m2)));
    }
}