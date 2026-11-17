# Madoc - Hockey League Management System

Madoc is a comprehensive web application for managing hockey league operations, including teams, players, schedules, standings, statistics, and more. Built with Spring Boot for the backend and Angular for the frontend.

## Table of Contents
1. [Features](#features)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
5. [Configuration](#configuration)
6. [Database Setup](#database-setup)
7. [API Endpoints](#api-endpoints)
8. [Frontend](#frontend)
9. [Development](#development)
10. [Deployment](#deployment)

## Features

- **Team Management**: Create and manage teams with rosters
- **Player Management**: Track player information, stats, and assignments
- **Schedule Management**: Upload and view game schedules
- **Game Management**: Record games, attendance, and statistics
- **Standings**: Real-time league standings calculation
- **Statistics**: Comprehensive player and team statistics tracking
- **Constitution**: League rules management
- **News System**: News articles and announcements
- **User Authentication**: JWT-based authentication system
- **Gamesheet Management**: Upload and process gamesheets

## Technology Stack

### Backend
- **Spring Boot 3.5.3** - Application framework
- **Java 21** - Programming language
- **PostgreSQL** - Database
- **Thymeleaf** - Server-side templating
- **Spring Security** - Authentication and authorization
- **JWT** - Token-based authentication
- **Flyway** - Database migrations
- **Apache Commons CSV** - CSV parsing
- **ODF Toolkit** - OpenDocument format manipulation

### Frontend
- **Angular 17** - Web framework
- **TypeScript** - Programming language
- **HTML/CSS** - Markup and styling

## Project Structure

```
Madoc/
├── src/main/java/dev/arcsoftware/madoc/              # Main application code
│   ├── controller/                                  # REST controllers
│   ├── service/                                     # Business logic services
│   ├── repository/                                  # Data access layer
│   ├── model/                                       # Data models and DTOs
│   ├── entity/                                      # JPA entities
│   ├── payload/                                     # API request/response payloads
│   ├── request/                                     # Request objects
│   ├── enums/                                       # Enum definitions
│   ├── auth/                                        # Authentication related classes
│   ├── config/                                      # Configuration classes
│   ├── exception/                                   # Custom exceptions
│   ├── util/                                        # Utility classes
│   └── view/                                        # View controllers for Thymeleaf templates
├── src/main/resources/                              # Resources
│   ├── templates/                                   # Thymeleaf templates
│   ├── data/                                        # Data files (CSV, etc.)
│   ├── static/                                      # Static assets
│   ├── application.yaml                             # Main configuration
│   └── application-local.yaml                     # Local development configuration
├── madoc-web/                                       # Angular frontend
│   ├── src/app/                                     # Application source
│   ├── src/assets/                                  # Static assets
│   └── src/styles.css                               # Global CSS styles
└── pom.xml                                          # Maven build configuration
```

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL 13+
- Node.js and npm (for frontend development)
- Maven

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/arcsoftware-dev/Madoc.git
cd Madoc
```

2. **Set up database:**
```sql
CREATE DATABASE madoc;
CREATE USER madoc_user WITH PASSWORD 'madoc_password';
GRANT ALL PRIVILEGES ON DATABASE madoc TO madoc_user;
```

3. **Configure environment variables:**
```bash
export JDBC_URL=jdbc:postgresql://localhost:5432/madoc
export DB_USER=madoc_user
export DB_PASSWORD=madoc_password
export DB_SCHEMA=public
export JWT_SIGNING_SECRET=your-super-secret-jwt-key-here
export ADMIN_TOKEN=your-admin-token-here
```

4. **Build and run:**
```bash
mvn clean install
java -jar target/Madoc-exec.jar
```

## Configuration

The application uses `application.yaml` for configuration:

- **Server**: Port 8100 by default
- **Database**: PostgreSQL connection details via environment variables
- **JWT**: Secret key and expiry settings
- **Security**: Authentication and authorization settings
- **File Uploads**: Size limits (128KB max file size)

## Database Setup

The application uses Flyway for database migrations. The schema is automatically created/updated when the application starts with 27+ migration scripts.

### Database Schema Overview

The Madoc database follows a normalized relational structure with the following key tables:

**Core Tables:**
- `teams` - Stores team information (team_name, year)
- `players` - Player details (first_name, last_name, email, phone_number)
- `roster_assignments` - Links players to teams with season details (draft_position, position, jersey_number)
- `games` - Game schedule and details (home_team, away_team, venue, game_time)
- `goals` - Goal records with player and assist information
- `assists` - Assist records for goals
- `penalties` - Penalty records for players
- `attendance` - Attendance tracking for games
- `users` - User authentication and permissions
- `news` - News articles and announcements
- `rules` - League constitution rules
- `gamesheets` - Gamesheet data

**Relationships:**
- Teams and Players are linked through roster_assignments
- Games reference teams (home_team, away_team)
- Goals reference games and players with assist information
- All tables use appropriate foreign key constraints for data integrity

The schema supports comprehensive hockey league management with proper normalization to prevent data redundancy while maintaining efficient querying capabilities.

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/logout` - User logout
- `POST /auth/register` - Register new user

### Teams & Players
- `GET /teams` - Get all teams
- `GET /teams/{id}` - Get team details
- `GET /players` - Get all players
- `GET /rosters` - Get rosters

### Schedule & Games
- `GET /schedule` - Get schedule
- `POST /games/upload` - Upload gamesheet
- `POST /games/attendance` - Add attendance

### Statistics & Standings
- `GET /standings` - Get standings
- `GET /stats` - Get statistics
- `GET /news` - Get news articles

## Frontend

The frontend is built with Angular and provides:
- Responsive web interface
- Interactive dashboards
- Data visualization
- User-friendly forms for data entry
- Authentication UI

### Development Setup
```bash
cd madoc-web
npm install
ng serve
```

## Development

### Backend Development
1. Use Spring Boot's auto-reload features during development
2. Run tests with `mvn test`
3. Follow Spring Boot best practices for controllers and services

### Frontend Development
1. Use Angular CLI for scaffolding components
2. Implement responsive design with CSS
3. Test with Angular testing utilities

## Deployment

### Production Deployment
1. Build the application: `mvn clean package`
2. Set environment variables
3. Run the JAR file: `java -jar target/Madoc-exec.jar`

### Docker Deployment (Optional)
The project can be containerized using Docker for easier deployment.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please open an issue on the GitHub repository.