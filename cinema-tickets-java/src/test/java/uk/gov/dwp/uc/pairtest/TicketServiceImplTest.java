package uk.gov.dwp.uk.pairtest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Spy;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;

public class TicketServiceImplTest {
    @Spy
    TicketServiceImpl ticketService = new TicketServiceImpl();


    @ParameterizedTest
    @CsvSource({
            ""
    })

}
