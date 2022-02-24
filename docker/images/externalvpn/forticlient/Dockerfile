#docker run --privileged --dns 8.8.8.8  --dns 10.136.49.21 --dns 10.110.49.21 --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
#docker run --privileged --cap-add SYS_ADMIN --cap-add DAC_READ_SEARCH --name myvpn myvpn
FROM ubuntu:latest
RUN apt-get update
RUN apt-get upgrade -y

# Block timezone request prompt
ENV DEBIAN_FRONTEND="noninteractive" TZ="Europe/Rome"

RUN apt-get install -y bash curl vim iptables wget unzip forticlient iptables-persistent screen

# Prepare runit
RUN apk add --no-cache runit \
    && mkdir -p /etc/service \
    && mkdir -p /etc/apps

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

RUN mkdir -p /etc/service/sshd \
    && mkdir -p /etc/app/sshd/log \
    && mkdir -p /etc/service/sshd/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/sshd/log\n" > /etc/service/sshd/log/run \
    && chmod +x /etc/service/sshd/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /usr/sbin/sshd\n" > /etc/service/sshd/run \
    && chmod +x /etc/service/sshd/run


# Setup the route
RUN mkdir -p /etc/service/setuproute \
    && mkdir -p /etc/app/setuproute/log \
    && mkdir -p /etc/service/setuproute/log \
    && echo -e "#!/bin/bash\nexec svlogd -tt /etc/app/setuproute/log\n" > /etc/service/setuproute/log/run \
    && chmod +x /etc/service/setuproute/log/run \
    && echo -e "#!/bin/bash\nexec 2>&1\nexec /etc/app/setuproute/setuproute.sh\n" > /etc/service/setuproute/run \
    && chmod +x /etc/service/setuproute/run  
    
    
# Force root password
RUN mkdir -p /etc/service/rootforce \
    && echo -e "#!/bin/bash\nexec 2>&1\necho \"root:\${ROOT_PWD}\"|chpasswd\nsleep infinity\n" > /etc/service/rootforce/run \
    && chmod +x /etc/service/rootforce/run 

# Start everythin
CMD ["runsvdir", "/etc/service"]

