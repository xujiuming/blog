FROM nginx:1.15.2-alpine
MAINTAINER "ming"
# copy conf
COPY ming.conf /etc/nginx/conf.d/default.conf
# copy dist to nginx workspace
COPY public /usr/share/nginx/html
