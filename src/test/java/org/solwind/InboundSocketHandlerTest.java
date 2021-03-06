package org.solwind;

import com.sun.org.apache.xpath.internal.operations.String;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.mockito.verification.VerificationMode;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by theso on 6/21/2017.
 */
@RunWith(JUnit4.class)
public class InboundSocketHandlerTest {

    @Mock
    private Map<Class, Object> services;

    private InboundSocketHandler inboundSocketHandler;

    @Mock
    private ChannelHandlerContext channelHandlerContext;

    @Mock
    private Channel channel;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        inboundSocketHandler = new InboundSocketHandler(services);
    }

    @Test
    public void channelRead() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "org.solwind.TestService", null)).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test
    public void channelReadWithArguments() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(true);
        Mockito.when(services.get(TestService.class)).thenReturn(new TestService());
        Object[] args = new Object[1];
        args[0] = "test";
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("args", "org.solwind.TestService", args)).get()));
        Mockito.verify(channel).writeAndFlush(Mockito.any());
    }

    @Test
    public void channelReadWithWrongClassName() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "org.solwind.TestService1", new Object[0])).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
        Mockito.verify(channel).close();
    }

    @Test
    public void channelReadWithWrongServiceClass() throws Exception {
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        Mockito.when(services.containsKey(TestService.class)).thenReturn(false);
        inboundSocketHandler.channelRead(channelHandlerContext,
                Functions.byteConverter.apply(Functions.serialize.apply(new CallRequest("echoText", "org.solwind.TestService", new Object[0])).get()));
        Mockito.verify(channel, new Times(0)).writeAndFlush(Mockito.any());
        Mockito.verify(channel).close();
    }

    @Test
    public void channelReadComplete() throws Exception {
        inboundSocketHandler.channelReadComplete(channelHandlerContext);
        Mockito.verify(channelHandlerContext).flush();
    }

    @Test
    public void exceptionCaught() throws Exception {
        inboundSocketHandler.exceptionCaught(channelHandlerContext, new Exception("Test exception"));
        Mockito.verify(channelHandlerContext).close();
    }

}