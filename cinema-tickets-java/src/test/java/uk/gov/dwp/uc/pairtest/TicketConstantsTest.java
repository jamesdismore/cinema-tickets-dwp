package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.TicketConstants;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;


public class TicketConstantsTest {
    @Test
    void allTicketsHavePriceAndSeats() {
        for (TicketTypeRequest.Type ticket : TicketTypeRequest.Type.values()) {
            Assertions.assertNotNull(TicketConstants.TICKET_PRICES.get(ticket));
            Assertions.assertNotNull(TicketConstants.SEAT_REQUIRED.get(ticket));
        }
    }
}
