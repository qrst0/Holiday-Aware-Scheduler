package com.holidayaware.scheduler.holiday;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


@Component
class NagerDateClient {

    private static final Logger log = LoggerFactory.getLogger(NagerDateClient.class);

    private final RestClient restClient;
    private final HolidayApiToggle toggle;

    NagerDateClient(
            HolidayApiToggle toggle,
            @Value("${holiday.nager.base-url}") String baseUrl,
            @Value("${holiday.nager.connect-timeout}") Duration connectTimeout,
            @Value("${holiday.nager.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.toggle = toggle;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    Optional<List<PublicHoliday>> fetchHolidays(String countryCode, int year) {
        if (!toggle.isEnabled()) {
            log.warn("nager lookup skipped for {} {}: api switched off", countryCode, year);
            return Optional.empty();
        }
        try {
            NagerHoliday[] response = restClient.get()
                    .uri("/api/v3/PublicHolidays/{year}/{countryCode}", year, countryCode)
                    .retrieve()
                    .body(NagerHoliday[].class);

            if (response == null) {
                return Optional.of(List.of());
            }
            return Optional.of(List.of(response).stream()
                    .filter(NagerHoliday::isNationwidePublicHoliday)
                    .map(holiday -> new PublicHoliday(holiday.date(), holiday.name()))
                    .toList());

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.of(List.of());
        } catch (RestClientException e) {
            log.warn("nager lookup failed for {} {}: {}", countryCode, year, e.getMessage());
            return Optional.empty();
        }
    }

    private record NagerHoliday(LocalDate date, String name, Boolean global, List<String> types) {
        boolean isNationwidePublicHoliday() {
            return Boolean.TRUE.equals(global) && types != null && types.contains("Public");
        }
    }
}
