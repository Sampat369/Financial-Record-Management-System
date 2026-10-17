# Financial Record Management System

This project contains a complete full-stack financial record management system built with Angular, Spring Boot, and MySQL.

## Project structure

- `frontend/` - Angular application
- `backend/financial-records/` - Spring Boot backend
- `database/` - MySQL schema and sample data scripts

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Angular CLI 19+
- MySQL Server 8.0+
- MySQL Workbench (optional but recommended for schema management)

## Local MySQL setup on Windows

1. Install MySQL Server and MySQL Workbench.
2. Start MySQL and create a connection named `Local MySQL` with:
   - Hostname: `127.0.0.1`
   - Port: `3306`
   - Username: `root`
   - Database: `financial_records`
3. Set the database password in your Windows environment:

```powershell
setx DB_PASSWORD "your_mysql_root_password"
```

Then restart your terminal or PowerShell session and confirm:

```powershell
$env:DB_PASSWORD
```

4. Create the schema:

```powershell
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Then execute the script:

```sql
SOURCE C:/path/to/Financial Record Management System/database/schema.sql;
```

If you prefer to run the SQL script non-interactively using the environment variable password, use:

```powershell
$env:DB_PASSWORD="your_mysql_root_password"
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p"$env:DB_PASSWORD" < "C:\path\to\Financial Record Management System\database\schema.sql"
```

## Backend setup

From the project root:

```powershell
cd backend\financial-records
set DB_USERNAME=root
set DB_PASSWORD=your_mysql_root_password
mvn spring-boot:run
```

The backend runs at:

- http://localhost:8080/api/transactions
- http://localhost:8080/api/dashboard

## Frontend setup

From the project root:

```powershell
cd frontend
npm install
npm start
```

Then open:

- http://localhost:4200

## Database scripts

- `database/schema.sql` creates the schema and `transactions` table.
- `database/sample-data.sql` inserts example records.

## Notes

- The backend uses `spring.jpa.hibernate.ddl-auto=validate` to avoid overwriting existing database tables.
- Currency values use `DECIMAL(12,2)` in MySQL.
- The frontend and backend use the same transaction categories and values.
