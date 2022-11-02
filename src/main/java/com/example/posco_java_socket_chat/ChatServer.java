package com.example.posco_java_socket_chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final ConcurrentHashMap<String, DataOutputStream> clientOutMap = new ConcurrentHashMap();

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.start();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println(getTime() + " Start server " + serverSocket.getLocalSocketAddress());
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientSession client = new ClientSession(socket);
                    client.start();
                } catch (IOException e) {
                }
            }
        }
    }

    private String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
    }

    private void joinChat(ClientSession session) {
        clientOutMap.put(session.id, session.out);
        sendToAll("[System] " + session.id + "님이 입장했습니다.");
        System.out.println(getTime() + " " + session.id + " is joined: " + session.socket.getInetAddress());
        loggingCurrentClientCount();
    }


    private boolean isIdExistChecked(String id){
        if(!Objects.isNull(clientOutMap.get(id))){
            return true;
        }
        return false;
    }

    private void leaveChat(ClientSession session) {
        clientOutMap.remove(session.id);

        sendToAll("[System] " + session.id + "님이 나갔습니다.");
        System.out.println(getTime() + " " + session.id + " is leaved: " + session.socket.getInetAddress());
        loggingCurrentClientCount();
    }

    private void loggingCurrentClientCount() {
        System.out.println(getTime() + " Currently " + clientOutMap.size() + " clients are connected.");
    }

    private void sendToAll(String message) {
        for (DataOutputStream out : clientOutMap.values()) {
            try {
                out.writeUTF(message);
            } catch (IOException e) {
            }
        }
    }

    class ClientSession extends Thread {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private String id;

        ClientSession(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            initialize();
            connect();
        }

        private void initialize() {
            try {
                this.id = in.readUTF();
                while (isIdExistChecked(id)){
                    out.writeUTF("아이디가 이미 존재합니다. 다른 아이디를 입력해주세요.");
                    this.id = in.readUTF();
                }
                joinChat(this);
            } catch (IOException cause) {
            }
        }

        private void connect() {
            try {
                while (isConnect()) {
                    sendToAll("["+this.id+"] "+in.readUTF());
                }
            } catch (IOException cause) {
            } finally {
                disconnect();
            }
        }

        private boolean isConnect() {
            return this.in != null;
        }

        private void disconnect() {
            leaveChat(this);
        }
    }
}
