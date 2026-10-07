# System Architecture Plan

## Layered Architecture (N-Tier)

The application will follow a strict layered approach to ensure separation of concerns:

1. **Presentation Layer (GUI)**
   - Java Swing components.
   - Handles user interaction and display logic.
   - Communicates ONLY with the Service Layer.

2. **Service Layer**
   - Business logic and validation.
   - Coordinates operations between the UI and the Data Access Layer.
   - Communicates ONLY with the DAO Layer.

3. **Data Access Layer (DAO)**
   - Handles all SQL queries and interactions with MariaDB.
   - Converts database rows into Java Objects (Models).
   - Communicates ONLY with the Database.

4. **Database Layer**
   - MariaDB storage for persistent data.

## Connection Management
- Future implementation may utilize a Singleton pattern for the database connection utility to ensure efficient resource usage.
