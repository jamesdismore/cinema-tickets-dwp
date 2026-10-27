package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.EnumMap;
import java.util.Map;


public class TicketServiceImpl implements TicketService {
    private TicketPaymentServiceImpl ticketPaymentService;
    private SeatReservationServiceImpl seatReservationService;

    public TicketServiceImpl(TicketPaymentServiceImpl ticketPaymentService, SeatReservationServiceImpl seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     * James: Methods made protected to allow for testing, can be made private without issue otherwise.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);

        Map<Type,Integer> ticketsRequested = ticketTypeRequestsToMap(ticketTypeRequests);

        validateRequiredAdultsPresent(ticketsRequested);

        validateTotalTickets(ticketsRequested);

        makePayments(accountId, ticketsRequested);

        reserveSeats(accountId, ticketsRequested);
    }

    protected Map<Type,Integer> ticketTypeRequestsToMap(TicketTypeRequest... ticketTypeRequests) {
        Map<Type,Integer> ticketsRequested = new EnumMap<>(Type.class);

        for (Type type : Type.values()) {
            ticketsRequested.put(type,0);
        }


        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            validateTicketTypeRequest(ticketTypeRequest);
            ticketsRequested.merge(ticketTypeRequest.getTicketType(), ticketTypeRequest.getNoOfTickets(), Integer::sum);
        }

        return ticketsRequested;
    }

    protected void makePayments(Long accountId, Map<Type,Integer> ticketsRequested) {
        int amountToPay = 0;

        for (Map.Entry<Type, Integer> entry : ticketsRequested.entrySet()) {
            int cost = TicketConstants.TICKET_PRICES.get(entry.getKey());
            amountToPay += cost * entry.getValue();
        }

        ticketPaymentService.makePayment(accountId, amountToPay);
    }


    protected void reserveSeats(Long accountId, Map<Type,Integer> ticketsRequested) {
        int seatsTotal = 0;

        for (Map.Entry<Type, Integer> entry : ticketsRequested.entrySet()) {
            boolean seatRequired = TicketConstants.SEAT_REQUIRED.get(entry.getKey());

            if (seatRequired) {
                seatsTotal += entry.getValue();
            }
        }

        seatReservationService.reserveSeat(accountId, seatsTotal);
    }

    protected void validateAccountId(Long accountId) {
        if (accountId <= 0) {
            throw new InvalidPurchaseException("Invalid Account Id");
        }
    }

    protected void validateTicketTypeRequest(TicketTypeRequest ticketTypeRequest) {
        if (ticketTypeRequest.getNoOfTickets() < 0) {
            throw new InvalidPurchaseException("Number of tickets cannot be negative");
        }

        if (ticketTypeRequest.getTicketType() == null) {
            throw new InvalidPurchaseException("Ticket type cannot be null");
        }
    }

    protected void validateRequiredAdultsPresent(Map<Type,Integer> ticketsRequested) {
        if (ticketsRequested.get(Type.ADULT) == 0) {
            if (ticketsRequested.get(Type.CHILD) > 0 || ticketsRequested.get(Type.INFANT) > 0)
                throw new InvalidPurchaseException("An Adult ticket must be purchased");
        }
    }

    protected void validateTotalTickets(Map<Type,Integer> ticketsRequested) {
        int totalTickets = ticketsRequested.values().stream().mapToInt(Integer::intValue).sum();

        if (totalTickets <=0) {
            throw new InvalidPurchaseException("At least one ticket cannot be purchased");
        }

        if (totalTickets > TicketConstants.MAXIMUM_TICKETS_PURCHASED) {
            throw new InvalidPurchaseException(String.format("More than %d tickets cannot be purchased at once",TicketConstants.MAXIMUM_TICKETS_PURCHASED));
        }
    }
}