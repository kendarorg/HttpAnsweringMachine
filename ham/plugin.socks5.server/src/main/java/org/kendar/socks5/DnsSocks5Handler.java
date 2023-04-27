package org.kendar.socks5;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sockslib.client.SocksProxy;
import sockslib.client.SocksSocket;
import sockslib.common.ProtocolErrorException;
import sockslib.common.SocksCommand;
import sockslib.common.SocksException;
import sockslib.common.methods.SocksMethod;
import sockslib.server.*;
import sockslib.server.io.Pipe;
import sockslib.server.io.SocketPipe;
import sockslib.server.msg.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DnsSocks5Handler implements SocksHandler {
    protected static final Logger logger = LoggerFactory.getLogger(Socks5Handler.class);
    private static final int VERSION = 5;
    public static DnsMultiResolver multiResolver;
    private Session session;
    private MethodSelector methodSelector;
    private int bufferSize;
    private int idleTime = 2000;
    private SocksProxy proxy;
    private SocksProxyServer socksProxyServer;
    private SessionManager sessionManager;

    public DnsSocks5Handler() {
    }

    public void handle(Session session) throws Exception {
        this.sessionManager = this.getSocksProxyServer().getSessionManager();
        this.sessionManager.sessionOnCreate(session);
        MethodSelectionMessage msg = new MethodSelectionMessage();
        session.read(msg);
        if (msg.getVersion() != 5) {
            throw new ProtocolErrorException();
        } else {
            SocksMethod selectedMethod = this.methodSelector.select(msg);
            logger.debug("SESSION[{}] Response client:{}", session.getId(), selectedMethod.getMethodName());
            session.write(new MethodSelectionResponseMessage(5, selectedMethod));
            selectedMethod.doMethod(session);
            CommandMessage commandMessage = new CommandMessage();
            session.read(commandMessage);
            if (commandMessage.hasSocksException() && commandMessage.getSocksException().getMessage()
                    .equalsIgnoreCase("Host unreachable")) {
                var realHost = multiResolver.resolve(commandMessage.getHost());
                if (realHost != null && !realHost.isEmpty()) {
                    commandMessage.setSocksException(null);
                }
            }
            if (commandMessage.hasSocksException()) {
                ServerReply serverReply = commandMessage.getSocksException().getServerReply();
                session.write(new CommandResponseMessage(serverReply));
                logger.info("SESSION[{}] will close, because {}", session.getId(), serverReply);
            } else {
                this.sessionManager.sessionOnCommand(session, commandMessage);
                if (commandMessage.getCommand() == SocksCommand.BIND) {
                    this.doBind(session, commandMessage);

                } else if (commandMessage.getCommand() == SocksCommand.CONNECT) {
                    this.doConnect(session, commandMessage);

                } else if (commandMessage.getCommand() == SocksCommand.UDP_ASSOCIATE) {
                    this.doUDPAssociate(session, commandMessage);
                }

            }
        }
    }

    public void doConnect(Session session, CommandMessage commandMessage) throws IOException {
        ServerReply reply = null;
        Socket socket = null;
        InetAddress bindAddress = null;
        int bindPort = 0;
        InetAddress remoteServerAddress;
        List<String> realHost = new ArrayList<>();
        if (commandMessage.getHost() != null && !commandMessage.getHost().isEmpty()) {
            realHost = multiResolver.resolve(commandMessage.getHost());
        }

        if (realHost != null && !realHost.isEmpty()) {
            remoteServerAddress = InetAddress.getByName(realHost.get(0));
        } else {
            remoteServerAddress = commandMessage.getInetAddress();
        }
        int remoteServerPort = commandMessage.getPort();
        byte[] defaultAddress = new byte[]{0, 0, 0, 0};
        bindAddress = InetAddress.getByAddress(defaultAddress);

        try {
            if (this.proxy == null) {
                socket = new Socket(remoteServerAddress, remoteServerPort);
            } else {
                socket = new SocksSocket(this.proxy, remoteServerAddress, remoteServerPort);
            }

            bindAddress = ((Socket) socket).getLocalAddress();
            bindPort = ((Socket) socket).getLocalPort();
            reply = ServerReply.SUCCEEDED;
        } catch (IOException var14) {
            if (var14.getMessage().equals("Connection refused")) {
                reply = ServerReply.CONNECTION_REFUSED;
            } else if (var14.getMessage().equals("Operation timed out")) {
                reply = ServerReply.TTL_EXPIRED;
            } else if (var14.getMessage().equals("Network is unreachable")) {
                reply = ServerReply.NETWORK_UNREACHABLE;
            } else if (var14.getMessage().equals("Connection timed out")) {
                reply = ServerReply.TTL_EXPIRED;
            } else {
                reply = ServerReply.GENERAL_SOCKS_SERVER_FAILURE;
            }

            logger.info("SESSION[{}] connect {} [{}] exception:{}", new Object[]{session.getId(), new InetSocketAddress(remoteServerAddress, remoteServerPort), reply, var14.getMessage()});
        }

        CommandResponseMessage responseMessage = new CommandResponseMessage(5, reply, bindAddress, bindPort);
        session.write(responseMessage);
        if (reply != ServerReply.SUCCEEDED) {
            session.close();
        } else {
            Pipe pipe = new SocketPipe(session.getSocket(), (Socket) socket);
            ((Pipe) pipe).setName("SESSION[" + session.getId() + "]");
            ((Pipe) pipe).setBufferSize(this.bufferSize);
            if (this.getSocksProxyServer().getPipeInitializer() != null) {
                pipe = this.getSocksProxyServer().getPipeInitializer().initialize((Pipe) pipe);
            }

            ((Pipe) pipe).start();

            while (((Pipe) pipe).isRunning()) {
                    Sleeper.sleep((long) this.idleTime);
            }

        }
    }

    public void doBind(Session session, CommandMessage commandMessage) throws IOException {
        ServerSocket serverSocket = new ServerSocket(commandMessage.getPort());
        int bindPort = serverSocket.getLocalPort();
        Socket socket = null;
        logger.info("Create TCP server bind at {} for session[{}]", serverSocket.getLocalSocketAddress(), session.getId());
        session.write(new CommandResponseMessage(5, ServerReply.SUCCEEDED, serverSocket.getInetAddress(), bindPort));
        socket = serverSocket.accept();
        session.write(new CommandResponseMessage(5, ServerReply.SUCCEEDED, socket.getLocalAddress(), socket.getLocalPort()));
        Pipe pipe = new SocketPipe(session.getSocket(), socket);
        pipe.setBufferSize(this.bufferSize);
        pipe.start();

        while (pipe.isRunning()) {
                Sleeper.sleep((long) this.idleTime);
        }

        serverSocket.close();
    }

    public void doUDPAssociate(Session session, CommandMessage commandMessage) throws IOException {
        UDPRelayServer udpRelayServer = new UDPRelayServer(((InetSocketAddress) session.getClientAddress()).getAddress(), commandMessage.getPort());
        InetSocketAddress socketAddress = (InetSocketAddress) udpRelayServer.start();
        logger.info("Create UDP relay server at[{}] for {}", socketAddress, commandMessage.getSocketAddress());
        session.write(new CommandResponseMessage(5, ServerReply.SUCCEEDED, InetAddress.getLocalHost(), socketAddress.getPort()));

        while (udpRelayServer.isRunning()) {
                Sleeper.sleep((long) this.idleTime);

            if (session.isClose()) {
                udpRelayServer.stop();
                logger.debug("UDP relay server for session[{}] is closed", session.getId());
            }
        }

    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void run() {
        try {
            this.handle(this.session);
        } catch (Exception var5) {
            this.sessionManager.sessionOnException(this.session, var5);
        } finally {
            this.session.close();
            this.sessionManager.sessionOnClose(this.session);
        }

    }

    public MethodSelector getMethodSelector() {
        return this.methodSelector;
    }

    public void setMethodSelector(MethodSelector methodSelector) {
        this.methodSelector = methodSelector;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getIdleTime() {
        return this.idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public SocksProxy getProxy() {
        return this.proxy;
    }

    public void setProxy(SocksProxy proxy) {
        this.proxy = proxy;
    }

    public SocksProxyServer getSocksProxyServer() {
        return this.socksProxyServer;
    }

    public void setSocksProxyServer(SocksProxyServer socksProxyServer) {
        this.socksProxyServer = socksProxyServer;
    }
}
