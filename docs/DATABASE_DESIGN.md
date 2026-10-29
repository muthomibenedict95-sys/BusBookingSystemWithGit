# Database Design

## Core entities

1. **Routes**
   - Stores source, destination, distance, and base price.
2. **Buses**
   - Stores each physical bus, its unique bus number, fixed passenger capacity, and type.
3. **Schedules**
   - Connects a bus and route to a departure date and time.
4. **Users**
   - Stores customer name, unique email, phone number, and password hash.
   - Passwords must never be stored in plaintext.
5. **Bookings**
   - Records a customer's reservation, schedule, passenger-name snapshot, seat number, and status.
6. **Seat reservations**
   - Enforces one allocation per seat on a schedule.
   - `HELD` means temporarily reserved while payment is pending.
   - `CONFIRMED` means payment succeeded and the seat is permanently allocated for that journey.
7. **Payments**
   - Records each payment attempt, including the transaction phone number, amount, status, and timestamps.
   - A retry creates a new payment record, preserving previous attempts.

## Booking and payment workflow

1. A registered customer selects a future schedule and seat.
2. The application creates a `PENDING_PAYMENT` booking and a `HELD` seat reservation in one transaction.
3. The seat hold expires after 10 minutes.
4. A failed payment attempt is recorded, but the customer may retry while the hold remains valid.
5. A successful payment confirms the booking and seat reservation in one database transaction.
6. An expired hold is released and the booking becomes `EXPIRED`. Payment history is retained.
7. Ticket generation is permitted only for a `CONFIRMED` booking.

## Integrity rules

- MariaDB's unique constraint on `(schedule_id, seat_number)` in `seat_reservations` prevents two allocations for the same seat.
- A booking can have multiple payment attempts.
- The transaction phone number is stored with the payment attempt. Changing it does not change the customer's account phone number.
- Booking, seat reservation, and payment changes must be coordinated transactionally.
- The application must validate that the schedule is still in the future using the `Africa/Nairobi` business time zone.
- Hold expiration must be checked and enforced by backend logic. A timestamp alone does not automatically release a seat.
- The schedule's bus capacity must be checked before allocating a seat.

## Implementation note

`resources/sql/schema.sql` defines the intended schema for a fresh database. `CREATE TABLE IF NOT EXISTS` does not migrate existing tables. Existing databases must be migrated deliberately after the Java DAO and service code is updated to use this design.
