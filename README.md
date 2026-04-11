## Quick Start

**1. Prepare configuration files**
Copy the example files to create your local environment and properties files:
```bash
cp .env.example .env
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Open the newly created .env file and application.properties file. Replace placeholders with your actual credentials before running the app.

**2. Build and start the containers**
```bash

docker compose up --build

```

#### Application will be avaliadble at:

http://localhost:8080
