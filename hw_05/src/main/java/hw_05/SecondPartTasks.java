package hw_05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths.stream()
        		.flatMap(p -> {
					try {
						return Files.lines(Paths.get(p));
					} catch (IOException e) {
						return null;
					}
				})
        		.filter(s -> s.contains(sequence.toString()))
        		.collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        //Random.doubles(10000).collect(() -> new)
    	Random r = new Random(12345);
    	final int numberOfExperiments = 10000;
    	
    	double experimentCount =
    			Stream.generate((Supplier<double[]>) () ->
    				new double[] {r.nextDouble(), r.nextDouble()})
    			.limit(numberOfExperiments)
    			.filter(p -> Math.pow(p[0] - 0.5, 2) + Math.pow(p[1] - 0.5, 2) <= 0.25)
    			.count();
    	
    	return experimentCount / numberOfExperiments;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
        .entrySet()
        .stream()
        .max(Comparator.comparingInt(entr -> entr.getValue()
        		.stream()
        		.collect(Collectors.summingInt(String::length))))
        .map(Entry::getKey)
        .orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.groupingBy(ent -> ent.getKey(),
        		Collectors.summingInt(ent -> ent.getValue())));
    }
}
