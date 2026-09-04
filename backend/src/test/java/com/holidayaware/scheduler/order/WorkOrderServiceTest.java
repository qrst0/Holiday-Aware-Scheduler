package com.holidayaware.scheduler.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.holidayaware.scheduler.common.BadRequestException;
import com.holidayaware.scheduler.common.NotFoundException;
import com.holidayaware.scheduler.holiday.HolidayService;
import com.holidayaware.scheduler.holiday.HolidaySource;
import com.holidayaware.scheduler.holiday.HolidayWindow;
import com.holidayaware.scheduler.order.dto.CreateOrderRequest;
import com.holidayaware.scheduler.order.dto.OrderResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class WorkOrderServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    private WorkOrderRepository repository;
    private HolidayService holidayService;
    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        repository = mock(WorkOrderRepository.class);
        holidayService = mock(HolidayService.class);
        workOrderService = new WorkOrderService(repository, holidayService);
        when(repository.save(any(WorkOrder.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    @DisplayName("flags ON_TRACK when the window has enough working days")
    void enoughWorkingDaysIsOnTrack() {
        givenWindow(21, HolidaySource.CACHE_FRESH);

        OrderResponse response = workOrderService.create(request(20));

        assertThat(response.riskFlag()).isEqualTo(RiskFlag.ON_TRACK);
        assertThat(response.workingDays()).isEqualTo(21);
    }

    @Test
    @DisplayName("flags AT_RISK when the window is short")
    void tooFewWorkingDaysIsAtRisk() {
        givenWindow(21, HolidaySource.CACHE_FRESH);

        assertThat(workOrderService.create(request(25)).riskFlag()).isEqualTo(RiskFlag.AT_RISK);
    }

    @Test
    @DisplayName("treats exactly enough working days as ON_TRACK, not AT_RISK")
    void exactlyEnoughIsOnTrack() {
        givenWindow(21, HolidaySource.CACHE_FRESH);

        assertThat(workOrderService.create(request(21)).riskFlag()).isEqualTo(RiskFlag.ON_TRACK);
    }

    @Test
    @DisplayName("flags UNKNOWN when no holiday data was available")
    void unavailableHolidayDataIsUnknown() {
        givenWindow(22, HolidaySource.UNAVAILABLE);

        assertThat(workOrderService.create(request(20)).riskFlag()).isEqualTo(RiskFlag.UNKNOWN);
    }

    @Test
    @DisplayName("still flags normally on a stale cache")
    void staleCacheStillProducesARealFlag() {
        givenWindow(21, HolidaySource.CACHE_STALE);

        OrderResponse response = workOrderService.create(request(25));

        assertThat(response.riskFlag()).isEqualTo(RiskFlag.AT_RISK);
        assertThat(response.holidaySource()).isEqualTo(HolidaySource.CACHE_STALE);
    }

    @Test
    @DisplayName("normalises the country code and defaults the status")
    void normalisesInput() {
        givenWindow(21, HolidaySource.LIVE);

        OrderResponse response = workOrderService.create(new CreateOrderRequest(
                "WIDGET-A", 10, "id", START, END, 20, null));

        assertThat(response.countryCode()).isEqualTo("ID");
        assertThat(response.status()).isEqualTo(OrderStatus.PLANNED);
    }

    @Test
    @DisplayName("rejects a window whose due date precedes its start date")
    void rejectsInvertedWindow() {
        assertThatThrownBy(() -> workOrderService.create(new CreateOrderRequest(
                "WIDGET-A", 10, "ID", END, START, 20, OrderStatus.PLANNED)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dueDate");
    }

    @Test
    @DisplayName("raises NotFoundException for an id that does not exist")
    void missingOrderIsNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.get(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    private void givenWindow(int workingDays, HolidaySource source) {
        when(holidayService.evaluate(anyString(), any(), any()))
                .thenReturn(new HolidayWindow(workingDays, List.of(), source, LocalDateTime.now()));
    }

    private CreateOrderRequest request(int requiredDays) {
        return new CreateOrderRequest("WIDGET-A", 10, "ID", START, END, requiredDays, OrderStatus.PLANNED);
    }
}
