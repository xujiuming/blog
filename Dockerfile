FROM docker.io/nginx:1.15.2-alpine
MAINTAINER "ming"
# copy dist to nginx workspace
COPY public /usr/share/nginx/html
