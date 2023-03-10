package com.example.chatclient;

import android.app.Activity;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ChatClient {
    Socket socket;
    private BufferedReader bufferedReader;
    PrintWriter writer;
    private final String ip;
    private final int port;
    private final List<Message> messages;
    Message message = new Message();
    public ChatClient(String ip, int port, List<Message> messages) {
        this.ip = ip;
        this.port = port;
        this.messages = messages;
    }
    public void connect(RecyclerView.Adapter adapter,Activity activity) {
        try {
            socket = new Socket(ip, port);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(streamReader);
            writer = new PrintWriter(socket.getOutputStream());
            System.out.println("Connected");

            Thread readerThread = new Thread(new IncomingReader(bufferedReader,messages,adapter,activity, new CountDownLatch(1)));
            readerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            writer.println(message);
            writer.flush();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void connect(RecyclerView.Adapter adapter,Activity activity, CountDownLatch latch) {
        try {
            socket = new Socket(ip, port);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(streamReader);
            writer = new PrintWriter(socket.getOutputStream());
            System.out.println("Connected");

            Thread readerThread = new Thread(new IncomingReader(bufferedReader,messages,adapter,activity, latch));
            readerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class IncomingReader implements Runnable {
        private final BufferedReader bufferedReader;
        private final List<Message> messages;
        private final RecyclerView.Adapter adapter;
        private final Activity activity;
        private final CountDownLatch latch;

        public IncomingReader(BufferedReader bufferedReader, List<Message> messages,
                              RecyclerView.Adapter adapter, Activity activity, CountDownLatch latch) {
            this.bufferedReader = bufferedReader;
            this.messages = messages;
            this.adapter = adapter;
            this.activity = activity;
            this.latch = latch;
        }

        @Override
        public void run() {
            String msg;
            try {
                while ((msg = bufferedReader.readLine()) != null) {
                    String finalMsg = msg;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add(new Message(finalMsg, NameActivity.message.getName()));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }
    }
}
