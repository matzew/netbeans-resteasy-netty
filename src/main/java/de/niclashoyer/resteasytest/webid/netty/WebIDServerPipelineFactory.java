
package de.niclashoyer.resteasytest.webid.netty;

import javax.net.ssl.SSLContext;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.resteasy.plugins.server.netty.HttpsServerPipelineFactory;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;

public class WebIDServerPipelineFactory extends HttpsServerPipelineFactory {
    
    public WebIDServerPipelineFactory(RequestDispatcher dispatcher, String root, int executorThreadCount, int maxRequestSize, SSLContext context) {
        super(dispatcher, root, executorThreadCount, maxRequestSize, context);
    }
    
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline cp = super.getPipeline();
        SslHandler ssl = cp.get(SslHandler.class);
        if (ssl == null) {
            throw new IllegalStateException("WebIDServerPipelineFactory needs an SslHandler in the pipeline");
        }
        ssl.getEngine().setWantClientAuth(true);
        ChannelHandler handler = new WebIDHandler();
        cp.addAfter("resteasyDecoder", "webidHandler", handler);
        return cp;
    }
    
}
