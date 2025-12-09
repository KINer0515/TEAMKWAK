FROM python:3.11-slim

WORKDIR /app

# Install system dependencies if needed (build tools, etc.)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
 && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy all RL source code
COPY . .

# Java HTTP server (Javalin) runs on host:8000
# On Windows/macOS Docker Desktop, host.docker.internal points to the host machine.
ENV JAVA_SERVER_URL=http://host.docker.internal:8000

# Entry point
CMD ["python", "ai_controller.py"]

#build: docker build -t teamkwak-rl-ai ./rl