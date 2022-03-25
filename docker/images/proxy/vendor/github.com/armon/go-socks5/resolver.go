package socks5

import (
	"net"
	"os"
	"time"
	"golang.org/x/net/context"
)

// NameResolver is used to implement custom name resolution
type NameResolver interface {
	Resolve(ctx context.Context, name string) (context.Context, net.IP, error)
}

// DNSResolver uses the system DNS to resolve host names
type DNSResolver struct{}

func (d DNSResolver) Resolve(ctx context.Context, name string) (context.Context, net.IP, error) {
	dns := os.Getenv("PROXY_DNS")
	
	if dns != "" {
		r := &net.Resolver{
			PreferGo: true,
			Dial: func(ctx context.Context, network, address string) (net.Conn, error) {
				d := net.Dialer{
					Timeout: time.Millisecond * time.Duration(10000),
				}
				return d.DialContext(ctx, "udp", dns + ":53")
			},
		}
		ip, err := r.LookupHost(context.Background(), name)
		if err != nil {
			return ctx, nil, err
		}
		realIp := net.ParseIP(ip[0])
		
		return ctx, realIp, err
	} else {
		addr, err := net.ResolveIPAddr("ip", name)
		if err != nil {
			return ctx, nil, err
		}
		return ctx, addr.IP, err
	}
}
