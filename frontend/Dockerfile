FROM node:24.13.0

WORKDIR /app

# Copy package files first
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy app source
COPY . .

# Use the default 'node' user provided by the image for security
USER node

EXPOSE 3000
CMD ["node", "server.js"]