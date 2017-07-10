FROM node:argon

# Create app directory
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Install app dependencies
COPY package.json /usr/src/app/
RUN npm install

# Bundle app source
COPY dist /usr/src/app/dist
COPY config /usr/src/app/config

EXPOSE 3333
CMD [ "npm", "start" ]
