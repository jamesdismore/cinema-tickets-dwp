# Ticket Service

## Validations assumptions made:

`accountId` must be > 0

`TicketTypeRequest.noOfTickets` >= 0 
- the front end might supply a 0 for some reason
- a null value is invalid

Total `noOfTickets` <= 25
- this was stored as a constant in `TicketConstants`, as it could be changed

Adult tickets > 0 to before you can have any infant or child tickets

infant does not take up a seat

## Design Decisions

Core logic in `TicketServiceImpl` was built to allow for ticket prices to change, ticket seat requirements to change, and for additional ticket types to be added
- Additional ticket types should have testing modified to validate any new rules involving them

Methods in TicketServiceImpl were set to `protected` instead of `private` to allow for testing, but this could be changed without affecting any logic should testing be removed.
A version of the repo this change made can be provided easily if required.

Testing was added to cover `TicketServiceImpl`, as well as verify that price and seat requirements are included in `TicketConstants` for any new ticket types added.