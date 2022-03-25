package socks5

import (
	"net"
	"os"
	"strings"
	"golang.org/x/net/context"
	"io/ioutil"
	"errors"
	"net/http"
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
		apiUri := "http://"+dns+"/api/dns/lookup/"+name
		resp, err := http.Get(apiUri)
		if err != nil {
			return ctx, nil, err
		}
		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {
			return ctx, nil, err
		}
		bodyString := string(body)
		splitted := strings.Split(strings.ReplaceAll(bodyString, "\r\n", "\n"), "\n")
		if len(splitted) == 0 {
			return ctx, nil, errors.New("Nothing founded")
		}
		realIp := net.ParseIP(splitted[0])
		
		return ctx, realIp, nil
	} else {
		addr, err := net.ResolveIPAddr("ip", name)
		if err != nil {
			return ctx, nil, err
		}
		return ctx, addr.IP, err
	}
}
