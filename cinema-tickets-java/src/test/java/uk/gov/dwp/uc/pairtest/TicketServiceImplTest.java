package uk.gov.dwp.uc.pairtest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import java.util.EnumMap;
import java.util.Map;
import static org.mockito.Mockito.verify;



@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {
    @Mock
    TicketPaymentServiceImpl ticketPaymentService;

    @Mock
    SeatReservationServiceImpl seatReservationService;

    @Spy
    @InjectMocks
    TicketServiceImpl ticketService;

    @Test
    void testPurchaseTickets() {
        TicketTypeRequest ticketTypeRequestAdult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,1);
        TicketTypeRequest ticketTypeRequestChild = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,2);
        TicketTypeRequest ticketTypeRequestInfant = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,3);
        ticketService.purchaseTickets(1L, ticketTypeRequestAdult,ticketTypeRequestChild,ticketTypeRequestInfant);

        verify(ticketService).validateAccountId(1L);
        verify(ticketService).ticketTypeRequestsToMap(ticketTypeRequestAdult,ticketTypeRequestChild,ticketTypeRequestInfant);

        Map<TicketTypeRequest.Type,Integer> ticketsRequested = ticketService.ticketTypeRequestsToMap(ticketTypeRequestAdult,ticketTypeRequestChild,ticketTypeRequestInfant);
        verify(ticketService).validateRequiredAdultsPresent(ticketsRequested);
        verify(ticketService).validateTotalTickets(ticketsRequested);
        verify(ticketService).makePayments(1L,ticketsRequested);
        verify(ticketService).reserveSeats(1L,ticketsRequested);
    }


    @ParameterizedTest
    @CsvSource(value = {
            "2,0,3,0,4,0",
            "0,1,0,5,0,7",
            "1,3,6,3,8,1",
            "0,0,0,0,0,0"
    })
    void testTicketTypeRequestsToMap (int noTicketsAdult1, int noTicketsAdult2,int noTicketsChild1, int noTicketsChild2,int noTicketsInfant1, int noTicketsInfant2) {
        TicketTypeRequest ticketTypeRequestAdult1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,noTicketsAdult1);
        TicketTypeRequest ticketTypeRequestAdult2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,noTicketsAdult2);
        int totalAdultTickets = noTicketsAdult1 + noTicketsAdult2;

        TicketTypeRequest ticketTypeRequestChild1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,noTicketsChild1);
        TicketTypeRequest ticketTypeRequestChild2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,noTicketsChild2);
        int totalChildTickets = noTicketsChild1 + noTicketsChild2;

        TicketTypeRequest ticketTypeRequestInfant1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,noTicketsInfant1);
        TicketTypeRequest ticketTypeRequestInfant2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,noTicketsInfant2);
        int totalInfantTickets = noTicketsInfant1 + noTicketsInfant2;



        Map<TicketTypeRequest.Type,Integer> ticketsRequested = ticketService.ticketTypeRequestsToMap(ticketTypeRequestAdult1,ticketTypeRequestAdult2,ticketTypeRequestChild1,ticketTypeRequestChild2,ticketTypeRequestInfant1,ticketTypeRequestInfant2);


        Assertions.assertEquals(totalAdultTickets, ticketsRequested.get(TicketTypeRequest.Type.ADULT));
        Assertions.assertEquals(totalChildTickets, ticketsRequested.get(TicketTypeRequest.Type.CHILD));
        Assertions.assertEquals(totalInfantTickets, ticketsRequested.get(TicketTypeRequest.Type.INFANT));

    }

    @ParameterizedTest
    @CsvSource(value = {
        "1,0,0",
        "0,2,0",
        "0,0,3",
        "3,4,5",
        "0,0,0"})
    void testMakePayments(int noOfAdults, int noOfChildren, int noOfInfants) {
        Map<TicketTypeRequest.Type, Integer> ticketsRequested = new EnumMap<>(TicketTypeRequest.Type.class);

        ticketsRequested.put(TicketTypeRequest.Type.ADULT, noOfAdults);
        ticketsRequested.put(TicketTypeRequest.Type.CHILD, noOfChildren);
        ticketsRequested.put(TicketTypeRequest.Type.INFANT, noOfInfants);

        int totalCost = (noOfAdults * TicketConstants.TICKET_PRICES.get(TicketTypeRequest.Type.ADULT)) + (noOfChildren * TicketConstants.TICKET_PRICES.get(TicketTypeRequest.Type.CHILD)) + (noOfInfants * TicketConstants.TICKET_PRICES.get(TicketTypeRequest.Type.INFANT));

        ticketService.makePayments(1L, ticketsRequested);

        verify(ticketPaymentService).makePayment(1L, totalCost);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1,0,0",
            "0,2,0",
            "0,0,3",
            "3,4,5",
            "0,0,0"})
    void testReserveSeats(int noOfAdults, int noOfChildren, int noOfInfants) {
        Map<TicketTypeRequest.Type, Integer> ticketsRequested = new EnumMap<>(TicketTypeRequest.Type.class);

        ticketsRequested.put(TicketTypeRequest.Type.ADULT, noOfAdults);
        ticketsRequested.put(TicketTypeRequest.Type.CHILD, noOfChildren);
        ticketsRequested.put(TicketTypeRequest.Type.INFANT, noOfInfants);

        int totalSeats = noOfAdults + noOfChildren;

        ticketService.reserveSeats(1L, ticketsRequested);

        verify(seatReservationService).reserveSeat(1L, totalSeats);
    }



    @ParameterizedTest
    @CsvSource(value = {
            "1,false",
            "2,false",
            "24536345435,false",
            "0,true",
            "-1,true",
            "NULL,true"
    }, nullValues = "NULL")
    void testValidateAccountId(Long accountId, boolean shouldThrow) {
        if (shouldThrow) {
            Assertions.assertThrows(Exception.class, () -> ticketService.validateAccountId(accountId));
        }
        else {
            Assertions.assertDoesNotThrow(() -> ticketService.validateAccountId(accountId));
        }
    }


    @ParameterizedTest
    @CsvSource(value = {
            "1,false",
            "2,false",
            "24536335,false",
            "0,false",
            "-1,true"
    })
    void testValidateTicketTypeRequest(int noOfTickets, boolean shouldThrow) {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, noOfTickets);

        if (shouldThrow) {
            Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.validateTicketTypeRequest(ticketTypeRequest));
        }
        else {
            Assertions.assertDoesNotThrow(() -> ticketService.validateTicketTypeRequest(ticketTypeRequest));
        }

        TicketTypeRequest ticketTypeRequestNull = new TicketTypeRequest(null, 1);

        Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.validateTicketTypeRequest(ticketTypeRequestNull));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1,0,0,false",
            "0,0,0,false",
            "1,1,0,false",
            "1,0,1,false",
            "1,1,1,false",
            "0,1,0,true",
            "0,0,1,true",
            "0,1,1,true",
            "2,1345,12,false",
            "0,3423,775,true"
    })
    void testValidateRequiredAdultsPresent(int noOfAdults, int noOfChildren, int noOfInfants, boolean shouldThrow) {
        Map<TicketTypeRequest.Type, Integer> ticketsRequested = new EnumMap<>(TicketTypeRequest.Type.class);

        ticketsRequested.put(TicketTypeRequest.Type.ADULT, noOfAdults);
        ticketsRequested.put(TicketTypeRequest.Type.CHILD, noOfChildren);
        ticketsRequested.put(TicketTypeRequest.Type.INFANT, noOfInfants);

        if (shouldThrow) {
            Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.validateRequiredAdultsPresent(ticketsRequested));
        } else {
            Assertions.assertDoesNotThrow(() -> ticketService.validateRequiredAdultsPresent(ticketsRequested));
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1,0,0,false",
            "0,0,0,true",
            "1,1,0,false",
            "1,0,1,false",
            "1,1,1,false",
            "0,1,0,false",
            "0,0,1,false",
            "0,1,1,false",
            "2,1345,12,true",
            "0,3423,775,true",
            "12,12,1,false",
            "12,12,2,true"
    })
    void testValidateTotalTickets(int noOfAdults, int noOfChildren, int noOfInfants, boolean shouldThrow) {
        Map<TicketTypeRequest.Type, Integer> ticketsRequested = new EnumMap<>(TicketTypeRequest.Type.class);

        ticketsRequested.put(TicketTypeRequest.Type.ADULT, noOfAdults);
        ticketsRequested.put(TicketTypeRequest.Type.CHILD, noOfChildren);
        ticketsRequested.put(TicketTypeRequest.Type.INFANT, noOfInfants);

        if (shouldThrow) {
            Assertions.assertThrows(InvalidPurchaseException.class, () -> ticketService.validateTotalTickets(ticketsRequested));
        } else {
            Assertions.assertDoesNotThrow(() -> ticketService.validateTotalTickets(ticketsRequested));
        }
    }
}
