ham.sampleapp.multi.proxy:
    container_name: ham.sampleapp.multi.proxy
    privileged: true
    cap_add:
      - NET_ADMIN
      - DAC_READ_SEARCH
    dns:
      - 127.0.0.1
    ports:
      - "1080:1080"
    networks:
      - multisampleappnet
    environment:
      - DNS_HIJACK_SERVER=ham.sampleapp.multi.master
      - ROOT_PWD=root
      - PROXY_DNS=127.0.0.1
    image: ham.proxy
    depends_on:
      - ham.sampleapp.multi.master