package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.ArrayDeque;
import java.util.Map;

public class TicketConstants {
    public static final Map<Type,Integer> TICKET_PRICES = Map.of(
            Type.INFANT,0,
            Type.CHILD,15,
            Type.ADULT,25
    );

    public static final Map<Type, Boolean> SEAT_REQUIRED = Map.of(
            Type.INFANT,false,
            Type.CHILD,true,
            Type.ADULT,true
    );

    public static final int MAXIMUM_TICKETS_PURCHASED = 25;
}
