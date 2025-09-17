# Stage 1: Build Angular app
FROM node:20-bullseye

# Update system packages to patch vulnerabilities
RUN apt-get update && apt-get upgrade -y && apt-get clean

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy the rest of the project
COPY . .

# Build Angular app
# RUN npx ng serve

EXPOSE 4200

CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--port", "4200"]