package com.holidayaware.scheduler.holiday;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class HolidayServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);
    private static final PublicHoliday NEW_YEAR =
            new PublicHoliday(LocalDate.of(2026, 1, 1), "New Year's Day");

    private static final int TTL_DAYS = 30;
    private static final Duration BACKOFF = Duration.ofMinutes(15);

    private NagerDateClient client;
    private HolidayCacheStore store;
    private HolidayService holidayService;

    @BeforeEach
    void setUp() {
        client = mock(NagerDateClient.class);
        store = mock(HolidayCacheStore.class);
        holidayService = new HolidayService(client, store, TTL_DAYS, BACKOFF);
    }

    @Test
    @DisplayName("serves a fresh cache without calling the holiday API at all")
    void freshCacheSkipsTheApi() {
        givenSync(sync -> sync.recordAnswer(LocalDateTime.now().minusDays(1), SyncOutcome.SUCCESS));
        when(store.findHolidays("ID", 2026)).thenReturn(List.of(NEW_YEAR));

        HolidayWindow window = holidayService.evaluate("ID", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.CACHE_FRESH);
        assertThat(window.workingDays()).isEqualTo(21);
        assertThat(window.holidays()).containsExactly(NEW_YEAR);
        verify(client, never()).fetchHolidays(anyString(), anyInt());
    }

    @Test
    @DisplayName("refetches once the cache is past its TTL")
    void staleCacheIsRefetched() {
        givenSync(sync -> sync.recordAnswer(LocalDateTime.now().minusDays(TTL_DAYS + 1), SyncOutcome.SUCCESS));
        when(client.fetchHolidays("ID", 2026)).thenReturn(Optional.of(List.of(NEW_YEAR)));
        when(store.replaceHolidays(eq("ID"), eq(2026), any(), eq(SyncOutcome.SUCCESS)))
                .thenReturn(LocalDateTime.now());

        HolidayWindow window = holidayService.evaluate("ID", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.LIVE);
        assertThat(window.workingDays()).isEqualTo(21);
        verify(store).replaceHolidays(eq("ID"), eq(2026), any(), eq(SyncOutcome.SUCCESS));
    }

    @Test
    @DisplayName("falls back to stale rows when the API is unreachable, keeping the real count")
    void apiDownWithCacheServesStaleData() {
        givenSync(sync -> sync.recordAnswer(LocalDateTime.now().minusDays(TTL_DAYS + 1), SyncOutcome.SUCCESS));
        when(client.fetchHolidays("ID", 2026)).thenReturn(Optional.empty());
        when(store.findHolidays("ID", 2026)).thenReturn(List.of(NEW_YEAR));

        HolidayWindow window = holidayService.evaluate("ID", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.CACHE_STALE);
        assertThat(window.workingDays()).isEqualTo(21);
        assertThat(window.holidays()).containsExactly(NEW_YEAR);
        verify(store).recordFailure("ID", 2026);
    }

    @Test
    @DisplayName("reports UNAVAILABLE and counts weekends only when nothing is cached")
    void apiDownWithoutCacheIsUnavailable() {
        when(store.findSync("JP", 2026)).thenReturn(Optional.empty());
        when(client.fetchHolidays("JP", 2026)).thenReturn(Optional.empty());
        when(store.findHolidays("JP", 2026)).thenReturn(List.of());

        HolidayWindow window = holidayService.evaluate("JP", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.UNAVAILABLE);
        assertThat(window.isUnavailable()).isTrue();
        assertThat(window.workingDays()).isEqualTo(22);
        assertThat(window.holidays()).isEmpty();
    }

    @Test
    @DisplayName("records a definitively empty calendar as EMPTY rather than FAILED")
    void emptyCalendarIsCachedAsAnAnswer() {
        when(store.findSync("ZZ", 2026)).thenReturn(Optional.empty());
        when(client.fetchHolidays("ZZ", 2026)).thenReturn(Optional.of(List.of()));
        when(store.replaceHolidays(eq("ZZ"), eq(2026), any(), eq(SyncOutcome.EMPTY)))
                .thenReturn(LocalDateTime.now());

        HolidayWindow window = holidayService.evaluate("ZZ", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.LIVE);
        assertThat(window.workingDays()).isEqualTo(22);
        verify(store).replaceHolidays(eq("ZZ"), eq(2026), any(), eq(SyncOutcome.EMPTY));
        verify(store, never()).recordFailure(anyString(), anyInt());
    }

    @Test
    @DisplayName("does not call the API again while a recent failure is inside the backoff window")
    void backoffSuppressesRepeatCalls() {
        givenSync("KR", sync -> sync.recordFailure(LocalDateTime.now().minusMinutes(1)));
        when(store.findHolidays("KR", 2026)).thenReturn(List.of());

        HolidayWindow window = holidayService.evaluate("KR", START, END);

        assertThat(window.source()).isEqualTo(HolidaySource.UNAVAILABLE);
        verify(client, never()).fetchHolidays(anyString(), anyInt());
        verify(store, never()).recordFailure(anyString(), anyInt());
    }

    @Test
    @DisplayName("retries once the backoff window has elapsed")
    void expiredBackoffAllowsAnotherCall() {
        givenSync("KR", sync -> sync.recordFailure(LocalDateTime.now().minus(BACKOFF).minusMinutes(1)));
        when(client.fetchHolidays("KR", 2026)).thenReturn(Optional.of(List.of()));
        when(store.replaceHolidays(eq("KR"), eq(2026), any(), any())).thenReturn(LocalDateTime.now());

        holidayService.evaluate("KR", START, END);

        verify(client).fetchHolidays("KR", 2026);
    }

    @Test
    @DisplayName("asks for every year a window spans and merges the answers")
    void crossYearWindowLoadsBothYears() {
        when(store.findSync(anyString(), anyInt())).thenReturn(Optional.empty());
        when(client.fetchHolidays("ID", 2026)).thenReturn(Optional.of(List.of(
                new PublicHoliday(LocalDate.of(2026, 12, 25), "Christmas Day"))));
        when(client.fetchHolidays("ID", 2027)).thenReturn(Optional.of(List.of(
                new PublicHoliday(LocalDate.of(2027, 1, 1), "New Year's Day"))));
        when(store.replaceHolidays(anyString(), anyInt(), any(), any())).thenReturn(LocalDateTime.now());

        HolidayWindow window = holidayService.evaluate(
                "ID", LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 1));

        verify(client).fetchHolidays("ID", 2026);
        verify(client).fetchHolidays("ID", 2027);
        assertThat(window.workingDays()).isEqualTo(4);
        assertThat(window.holidays()).hasSize(1);
    }

    @Test
    @DisplayName("takes the most degraded source when one year of a window is worse than the other")
    void mixedSourcesReportTheWorstOne() {
        givenSync("ID", 2026, sync -> sync.recordAnswer(LocalDateTime.now().minusDays(1), SyncOutcome.SUCCESS));
        when(store.findHolidays("ID", 2026)).thenReturn(List.of());
        when(store.findSync("ID", 2027)).thenReturn(Optional.empty());
        when(client.fetchHolidays("ID", 2027)).thenReturn(Optional.empty());
        when(store.findHolidays("ID", 2027)).thenReturn(List.of());

        HolidayWindow window = holidayService.evaluate(
                "ID", LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 1));

        assertThat(window.source()).isEqualTo(HolidaySource.UNAVAILABLE);
    }

    @Test
    @DisplayName("refuses to guess on an inverted window")
    void invertedWindowIsUnavailable() {
        HolidayWindow window = holidayService.evaluate("ID", END, START);

        assertThat(window.source()).isEqualTo(HolidaySource.UNAVAILABLE);
        assertThat(window.workingDays()).isZero();
        verify(client, never()).fetchHolidays(anyString(), anyInt());
    }

    private void givenSync(java.util.function.Consumer<HolidaySync> setUp) {
        givenSync("ID", setUp);
    }

    private void givenSync(String countryCode, java.util.function.Consumer<HolidaySync> setUp) {
        givenSync(countryCode, 2026, setUp);
    }

    private void givenSync(String countryCode, int year, java.util.function.Consumer<HolidaySync> setUp) {
        HolidaySync sync = new HolidaySync(countryCode, year);
        setUp.accept(sync);
        when(store.findSync(countryCode, year)).thenReturn(Optional.of(sync));
    }
}
