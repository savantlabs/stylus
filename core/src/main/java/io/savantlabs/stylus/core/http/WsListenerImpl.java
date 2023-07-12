package io.savantlabs.stylus.core.http;

import okio.ByteString;

import java.util.concurrent.CountDownLatch;

public class WsListenerImpl implements WsListener{
    private String response;
    private CountDownLatch latch;
    @Override
    public void onText(String text) {
        System.out.println("Received text: " + text);
        onResponse(text);
    }

    @Override
    public void onBinary(ByteString bytes) {
        System.out.println("Received binary: " + bytes);
    }

    @Override
    public void onResponse(String response) {
        this.response = response;
        if (latch != null) {
            latch.countDown(); // Count down the latch when response is received
        }
    }

    public String getResponse() {
        return response;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
}
