#docker run --privileged --dns 8.8.8.8  --dns 10.136.49.21 --dns 10.110.49.21 --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
#docker run --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
FROM ubuntu:latest
RUN apt-get update
RUN apt-get upgrade -y

# Block timezone request prompt
ENV DEBIAN_FRONTEND="noninteractive" TZ="Europe/Rome"

RUN apt-get install -y bash curl vim iptables  wget unzip openconnect iptables-persistent screen dnsutils iputils-ping

# Prepare runit
RUN apt-get install -y runit \
    && mkdir -p /etc/service \
    && mkdir -p /etc/app

# Update iptables
RUN echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf
RUN update-alternatives --set iptables /usr/sbin/iptables-legacy

# Install sshd
RUN apt-get install -y \ 
        openssh-server \
    && ssh-keygen -A \
    && mkdir /root/.ssh \
    && chmod 0700 /root/.ssh
    
COPY data/sshd_config /etc/ssh/
RUN mkdir /etc/app/setuproute
COPY data/setuproute.sh /etc/app/setuproute/
RUN chmod +x /etc/app/setuproute/*

COPY data/sshd_config /etc/ssh/
COPY data/startservice.sh /etc/startservice.sh
RUN chmod +x /etc/startservice.sh

# Setup the sshd
RUN /etc/startservice.sh --app=sshd --capturelogs --run=/usr/sbin/sshd


# Setup the route
#RUN /etc/startservice.sh --app=setuproute --config --run=/etc/app/setuproute/setuproute.sh
      
# Setup the basic dns servers
#RUN /etc/startservice.sh --app=dns --config --run=/etc/app/dns/dns.sh
#COPY data/dns.sh /etc/app/dns/dns.sh
#RUN chmod +x /etc/app/dns/dns.sh

# Force root password
RUN mkdir -p /etc/service/rootforce \
    && echo "#!/bin/bash" > /etc/service/rootforce/run \
    && echo "exec 2>&1" >> /etc/service/rootforce/run \
    && echo 'echo "root:\${ROOT_PWD}"|chpasswd' >> /etc/service/rootforce/run \
    && echo "/etc/DoSleep.sh" >> /etc/service/rootforce/run \
    && chmod +x /etc/service/rootforce/run 

# Start everythin
CMD ["runsvdir", "/etc/service"]

