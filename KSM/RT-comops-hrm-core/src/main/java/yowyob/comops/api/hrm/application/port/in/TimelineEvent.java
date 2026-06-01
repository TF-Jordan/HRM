package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;

/**
 * A single entry in an employee's career timeline, aggregated from real HRM
 * records (hire, contracts, performance reviews).
 */
public record TimelineEvent(String type, LocalDate date, String title, String detail) {
}
