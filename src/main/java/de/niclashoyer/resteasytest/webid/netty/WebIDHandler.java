package de.niclashoyer.resteasytest.webid.netty;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.resteasy.spi.HttpRequest;

public class WebIDHandler implements ChannelUpstreamHandler {

    protected X509Certificate getPeerCertificate(SslHandler handler) {
        SSLEngine engine = handler.getEngine();
        SSLSession session = engine.getSession();
        Certificate[] certs;
        try {
            certs = session.getPeerCertificates();
            if (certs.length > 0) {
                Certificate cert = certs[0];
                if (cert instanceof X509Certificate) {
                    return (X509Certificate) cert;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (SSLPeerUnverifiedException ex) {
            return null;
        }
    }

    protected Collection<String> getClaimedURIs(X509Certificate cert) throws CertificateParsingException {
        if (cert == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<String> uris;
        int type;
        String uri;
        uris = new HashSet<>();
        for (List l : cert.getSubjectAlternativeNames()) {
            type = (int) l.get(0);
            // type 6 is URI, see http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames%28%29
            if (type == 6) {
                uri = (String) l.get(1);
                uris.add(uri);
            }
        }
        return uris;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendUpstream(evt);
            return;
        }
        MessageEvent e = (MessageEvent) evt;
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            SslHandler ssl = ctx.getPipeline().get(SslHandler.class);
            if (ssl == null) {
                throw new IllegalStateException("WebIDHandler needs an SslHandler attached");
            }
            X509Certificate cert = getPeerCertificate(ssl);
            if (cert != null) {
                Collection<String> uris = getClaimedURIs(cert);
                req.setAttribute("webidclaims", uris);
                req.setAttribute("webidcertificate", cert);
            }
        }
        ctx.sendUpstream(evt);
    }
}
