FROM ham.socks5 as builder

FROM ham.client:latest

RUN apk add --no-cache git make musl-dev go

# Configure Go
ENV GOROOT /usr/lib/go
ENV GOPATH /go
ENV PATH /go/bin:$PATH

RUN mkdir -p ${GOPATH}/src ${GOPATH}/bin

COPY --from=builder /usr/share/zoneinfo /usr/share/zoneinfo
COPY --from=builder /etc/ssl/certs /etc/ssl/certs
COPY --from=builder /socks5 /

# Start socks5
RUN /etc/startservice.sh --app=socks5 --run=/socks5

# Start everythin
CMD ["runsvdir", "/etc/service"]