package backend.academy.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.MutableLong;

@Slf4j
public record LogReport(List<String> fileNames, long counter, List<Map.Entry<String, Long>> mostFrequentResources,
                        List<Map.Entry<Integer, Long>> mostFrequentStatus,
                        List<Map.Entry<String, Long>> mostFrequentMethods,
                        double averageResponseSize, long percentile,
                        long totalTransferredBytes) {
    public static LogReport of(List<LogAnalyzer> reports) {
        final List<String> fileNames = new ArrayList<>();
        final MutableLong counter = new MutableLong();
        final Map<String, Long> mostFrequentResources = new HashMap<>();
        final Map<Integer, Long> mostFrequentStatuses = new HashMap<>();
        final Map<String, Long> mostFrequentMethods = new HashMap<>();
        final List<Long> notZeroBytes = new ArrayList<>();
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("No reports found");
        }

        log.info("Creating log report...");

        reports.forEach(
            report -> {
                fileNames.add(report.fileName());
                counter.addAndGet(report.counter());
                report.mostFrequentResources().forEach((k, v) -> mostFrequentResources.merge(k, v, Long::sum));
                report.mostFrequentStatuses().forEach((k, v) -> mostFrequentStatuses.merge(k, v, Long::sum));
                report.mostFrequentMethods().forEach((k, v) -> mostFrequentMethods.merge(k, v, Long::sum));
                notZeroBytes.addAll(report.notZeroBytes());

            }
        );
        return new LogReport(
            fileNames,
            counter.getValue(),
            sortMapToListByValueDescending(mostFrequentResources, Config.MOST_FREQUENT_RESOURCES_LIMIT),
            sortMapToListByValueDescending(mostFrequentStatuses, Config.MOST_FREQUENT_STATUSES_LIMIT),
            sortMapToListByValueDescending(mostFrequentMethods, Config.MOST_FREQUENT_METHODS_LIMIT),
            calculateAverageSize(notZeroBytes),
            calculatePercentile(notZeroBytes),
            notZeroBytes.stream().mapToLong(Long::longValue).sum());
    }

    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> sortMapToListByValueDescending(
        Map<K, V> map,
        int limit
    ) {
        return map.entrySet()
            .stream()
            .sorted(Map.Entry.<K, V>comparingByValue().reversed())
            .limit(limit)
            .toList();
    }

    private static long calculatePercentile(List<Long> notZeroBytes) {
        if (notZeroBytes == null || notZeroBytes.isEmpty()) {
            return 0;
        }

        notZeroBytes.sort(Long::compareTo);

        int index = (int) Math.ceil(Config.PERCENTILE_RANK * notZeroBytes.size()) - 1;

        return notZeroBytes.get(index);
    }

    private static double calculateAverageSize(List<Long> bytes) {
        OptionalDouble average = bytes.stream().mapToDouble(el -> el).average();
        return average.isPresent() ? average.getAsDouble() : 0;
    }
}
