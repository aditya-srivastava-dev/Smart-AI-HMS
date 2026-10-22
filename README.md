# Smart Healthcare Management Platform (Smart AI HMS)

Hospital management (patients, doctors, appointments, medical records, admin) plus a simple
disease prediction feature (Naive Bayes written in plain Java).

## What you need
- Java 23 (see `backend/pom.xml`; change `java.version` if you use another JDK)
- MongoDB running on `localhost:27017`
- Node.js 20 or newer

## Run the backend
```
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
The server starts on http://localhost:8080. The database `smart_ai_hms` is created automatically.

First admin login (created once, change the password after login):
- Email: `admin@hms.local`
- Password: `Admin@12345`

Settings can be changed with environment variables: `MONGODB_URI`, `JWT_SECRET` (32+ characters),
`CORS_ORIGIN`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

## Run the frontend
```
cd frontend
npm install
npm run dev
```
Open http://localhost:5173.

## How to use
1. Admin logs in and creates doctor accounts.
2. A patient registers, books an appointment and tries the Symptom Checker.
3. The doctor sees the appointment and writes a medical record.

## Prediction module
- Dataset: `backend/src/main/resources/data/disease_symptoms.csv` (41 diseases, 131 symptoms, 304 unique records).
- Code: `backend/src/main/java/com/smartaihms/backend/prediction/`.
- Accuracy: log in and open `GET /api/prediction/metrics` (average of 5 random 80/20 splits).
- The result is only a computer suggestion, not a medical diagnosis.
