package com.github.kpavlov.jreactive8583.netty.pipeline;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ParseExceptionHandlerTest {

    private static MessageFactory<?> messageFactory;
    private ParseExceptionHandler handler;
    @Mock
    private ChannelHandlerContext ctx;
    @Captor
    private ArgumentCaptor<IsoMessage> messageCaptor;

    @BeforeAll
    public static void beforeClass() throws Exception {
        messageFactory = ConfigParser.createDefault();
    }

    @BeforeEach
    public void setUp() {
        handler = new ParseExceptionHandler(messageFactory, true);
    }

    @Test
    public void testExceptionCaught() throws Exception {
        String errorMessage = UUID.randomUUID().toString();

        handler.exceptionCaught(ctx, new ParseException(errorMessage, 0));

        verify(ctx).writeAndFlush(messageCaptor.capture());
        final IsoMessage message = messageCaptor.getValue();

        assertThat(message.getType()).isEqualTo(0x1644);

        //field 24
        final IsoValue<Object> field24 = message.getAt(24);
        assertThat(field24).as("field24").isInstanceOf(IsoValue.class);
        assertThat(field24.getType()).as("field24.type").isEqualTo(IsoType.NUMERIC);
        assertThat(field24.getLength()).as("field24.length").isEqualTo(3);
        assertThat(field24.getValue()).as("field24.value").isEqualTo(650);

        final IsoValue<Object> field44 = message.getAt(44);
        assertThat(field44).as("field44").isInstanceOf(IsoValue.class);
        assertThat(field44.getType()).as("field44.type").isEqualTo(IsoType.LLVAR);
        assertThat(field44.getLength()).as("field44.length").isEqualTo(25);
        assertThat(field44.getValue()).as("field44.value")
                .isEqualToComparingFieldByField(errorMessage.substring(0, 22) + "...");

    }
}
