FROM ham.client

RUN echo ham.apache 4.1.2

# Setup apache and php
RUN apk --no-cache add apache2  apache2-ssl curl \
    && mkdir /htdocs
    
COPY config/startapache.sh /etc/startapache.sh
COPY config/setupapache.sh /etc/setupapache.sh

RUN chmod +x /etc/*.sh \
	&& /etc/setupapache.sh \
    && /etc/startservice.sh --app=apache --run=/etc/startapache.sh