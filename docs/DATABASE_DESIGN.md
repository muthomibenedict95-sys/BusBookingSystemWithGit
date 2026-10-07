# Database Design Plan

## Entities and Relationships

1. **Routes**
   - Represents travel paths between locations A through K.
   - Fields: ID, Source, Destination, Distance, Base Price.

2. **Buses**
   - Represents the physical vehicles.
   - Fields: ID, Bus Number, Capacity, Type.

3. **Schedules**
   - Links a Bus to a Route at a specific Date and Time.
   - Fields: ID, Bus ID (FK), Route ID (FK), Departure Date, Departure Time.

4. **Bookings**
   - Represents a passenger reservation for a specific schedule.
   - Fields: ID, Schedule ID (FK), Passenger Name, Seat Number, Booking Date.

## Seat Tracking
- Individual seat tracking will be implemented to manage availability precisely.
- Each booking will be mapped to a specific seat identifier.
