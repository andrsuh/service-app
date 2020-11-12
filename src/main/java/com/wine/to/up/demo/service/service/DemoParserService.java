package com.wine.to.up.demo.service.service;

import com.wine.to.up.commonlib.annotations.InjectEventLogger;
import com.wine.to.up.commonlib.logging.EventLogger;
import com.wine.to.up.demo.service.components.DemoServiceMetricsCollector;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wine.to.up.demo.service.components.DemoServiceMetricsCollector.*;
import static com.wine.to.up.demo.service.logging.DemoServiceNotableEvents.*;

@Component
public class DemoParserService {
    private static final String PAGE_URL = "http://wine.ru?page=";
    private static final String PARSER_NAME = "demo-parser";
    private static final int PAGE_NUM = 15;

    private static final String PARSING_IN_PROGRESS_GAUGE = "parsing_in_progress";

    @InjectEventLogger
    private EventLogger eventLogger;

    @Autowired
    private DemoServiceMetricsCollector metrics;

    private static final Random random = new Random();
    private static final Sleeper sleeper50 = new Sleeper(50);
    private static final Sleeper sleeper500 = new Sleeper(500);

    private final Parser parser;
    private final HttpClient httpClient;

    private final AtomicInteger parsingInProgress = new AtomicInteger();

    public DemoParserService() {
        this(new Parser(), new HttpClient());
    }

    public DemoParserService(Parser parser, HttpClient httpClient) {
        this.parser = parser;
        this.httpClient = httpClient;
        Metrics.gauge(PARSING_IN_PROGRESS_GAUGE, List.of(Tag.of(PARSER_NAME_TAG, PARSER_NAME)), parsingInProgress);
    }

    @Scheduled(fixedDelay = 1_000)
    @Timed
    public List<Wine> performParsing() {
        List<Wine> resultList = new ArrayList<>();

        parsingInProgress.getAndIncrement();
        metrics.incParsingStarted(PARSER_NAME);

        for (int i = 0; i < PAGE_NUM; i++) {
            String url = PAGE_URL + i;
            try {
                String htmlPage = httpClient.fetchHttpPage(url);
                resultList.addAll(parser.parse(htmlPage));
            } catch (Parser.ParserException parserException) {
                eventLogger.warn(W_PAGE_PARSING_FAILED, url);
            } catch (HttpClient.HttpClientException httpClientException) {
                eventLogger.error(E_HTML_PAGE_FETCHING_FAILED, url);
                metrics.incParsingFailed(PARSER_NAME);
            } catch (Throwable t) {
                metrics.incParsingFailed(PARSER_NAME);
                eventLogger.error(DEV_UNEXPECTED_ERROR, t);
            }
        }

        metrics.incParsingComplete(PARSER_NAME);
        return resultList;
    }

    static class Parser {
        List<Wine> parse(String htmlPage) {
            failWithProbability(7, ParserException::new);
            return Stream.iterate(0, v -> v + 1)
                    .limit(random.nextInt(20))
                    .map(i -> new Wine())
                    .peek(w -> sleeper50.threadSleepRand())
                    .collect(Collectors.toList());
        }

        static class ParserException extends RuntimeException {
        }
    }

    static class HttpClient {
        String fetchHttpPage(String url) {
            failWithProbability(2, HttpClientException::new);
            sleeper500.threadSleepRand();
            return "Fetched :" + url;
        }

        static class HttpClientException extends RuntimeException {
        }
    }

    static class Wine {
        private static final String BASE_NAME = "wine_";
        private static final LongAdder adder = new LongAdder();

        private final String name;

        public Wine() {
            adder.add(1);
            this.name = BASE_NAME + adder.longValue();
        }
    }

    @SneakyThrows
    private static void failWithProbability(int probabilityPercent, Supplier<Exception> failer) {
        if (random.nextInt(101) > (100 - probabilityPercent)) {
            throw failer.get();
        }
    }

    @AllArgsConstructor
    static class Sleeper {
        private int upperBound;

        @SneakyThrows
        private static void threadSleep(long millis) {
            Thread.sleep(millis);
        }

        @SneakyThrows
        private void threadSleepRand() {
            threadSleep(random.nextInt(upperBound));
        }
    }
}
