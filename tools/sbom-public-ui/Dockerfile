# Stage 1: Build Angular app
FROM node:20-bookworm

# Update package indexes and upgrade system packages
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy application source
COPY . .

# Expose Angular development server port
EXPOSE 4200

# Start Angular application
CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--port", "4200"]