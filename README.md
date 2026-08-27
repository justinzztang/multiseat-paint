# Multiseat Paint
A simple multiplayer drawing program made with React and Spring Boot. Key features include real-time canvas synchronization and independent undo/redo functionality.

## Usage
### Build & Run
Build the jar with gradle:
```bash
./gradlew dependencies
./gradlew bootJar
```
And then run it with Java 25 to start the server:
```bash
java -jar build/libs/multiplayer-paint-1.0.0.jar
```
To run the frontend, change to the frontend directory, install dependencies, and build the site:
```bash
npm ci
npm run build
```
### Containerization
Basic Dockerfiles are provided, alongside an NGINX config for reverse proxying.
