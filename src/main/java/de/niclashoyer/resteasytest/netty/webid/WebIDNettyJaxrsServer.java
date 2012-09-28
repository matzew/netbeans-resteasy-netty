package de.niclashoyer.resteasytest.netty.webid;

import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.embedded.EmbeddedJaxrsServer;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

/**
 * An HTTP server that requests a client certificate and parses WebID claims.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author Norman Maurer
 * @author Niclas Hoyer
 */
public class WebIDNettyJaxrsServer implements EmbeddedJaxrsServer {

    protected ServerBootstrap bootstrap;
    protected Channel channel;
    protected int port = 8080;
    protected ResteasyDeployment deployment = new ResteasyDeployment();
    protected String root = "";
    protected SecurityDomain domain;
    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
    private int executorThreadCount = 16;
    private SSLContext sslContext;
    private int maxRequestSize = 1024 * 1024 * 10;
    private KeyManager[] keyManagers;

    /**
     * Specify the worker count to use. For more informations about this please
     * see the javadocs of {@link NioServerSocketChannelFactory}
     *
     * @param ioWorkerCount
     */
    public void setIoWorkerCount(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    /**
     * Set the number of threads to use for the Executor. For more informations
     * please see the javadocs of {@link OrderedMemoryAwareThreadPoolExecutor}.
     * If you want to disable the use of the {@link ExecutionHandler} specify a
     * value <= 0. This should only be done if you are 100% sure that you don't
     * have any blocking code in there.
     *
     *
     *
     *
     *
     *

     *
     * @param executorThreadCount
     */
    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    /**
     * Set the max. request size in bytes. If this size is exceed we will send a
     * "413 Request Entity Too Large" to the client.
     *
     * @param maxRequestSize the max request size. This is 10mb by default.
     */
    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setDeployment(ResteasyDeployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public void setRootResourcePath(String rootResourcePath) {
        root = rootResourcePath;
        if (root != null && root.equals("/")) {
            root = "";
        }
    }

    @Override
    public ResteasyDeployment getDeployment() {
        return deployment;
    }

    @Override
    public void setSecurityDomain(SecurityDomain sc) {
        this.domain = sc;
    }

    @Override
    public void start() {
        deployment.start();
        RequestDispatcher dispatcher = new RequestDispatcher((SynchronousDispatcher) deployment.getDispatcher(), deployment.getProviderFactory(), domain);

        // Configure the server.
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(),
                ioWorkerCount));

        ChannelPipelineFactory factory;
        try {
            initSSLContext();
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw new IllegalStateException(ex);
        }
        factory = new WebIDServerPipelineFactory(dispatcher, root, executorThreadCount, maxRequestSize, sslContext);

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(factory);

        // Bind and start to accept incoming connections.
        channel = bootstrap.bind(new InetSocketAddress(port));
    }

    @Override
    public void stop() {
        channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
        deployment.stop();
    }
    
    public void setKeyManagers(KeyManager[] km) {
        this.keyManagers = km;
    }

    protected void initSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("TLS");
        X509TrustManager[] trustManagers = {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
        sslContext.init(keyManagers, trustManagers, null);
    }
}