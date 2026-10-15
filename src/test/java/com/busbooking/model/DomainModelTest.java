package com.busbooking.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class DomainModelTest {

    @Test
    public void testBusCapacityOwnership() {
        Bus bus = new Bus(1, "BUS-001", 40, "Luxury");
        assertEquals(40, bus.getCapacity(), "Bus should own its capacity");
    }

    @Test
    public void testScheduleReferencesBusCapacity() {
        Bus bus = new Bus(1, "BUS-001", 40, "Luxury");
        Route route = new Route(1, "City A", "City B", 100.0, new BigDecimal("50.00"));
        Schedule schedule = new Schedule(1, bus, route, LocalDate.now(), LocalTime.NOON);

        assertEquals(40, schedule.getBusCapacity(), "Schedule should retrieve capacity from referenced Bus");

        // Verify that changing bus capacity reflects in schedule (proving reference)
        bus.setCapacity(45);
        assertEquals(45, schedule.getBusCapacity(), "Schedule should reflect changes in Bus capacity");
    }

    @Test
    public void testScheduleWithoutBus() {
        Schedule schedule = new Schedule();
        assertEquals(0, schedule.getBusCapacity(), "Schedule without a bus should return 0 capacity");
    }
}
