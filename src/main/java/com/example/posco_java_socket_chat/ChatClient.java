package com.example.posco_java_socket_chat;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient extends Application {
    private String id = "Visitor";
    private Sender sender;
    private Receiver receiver;
    private ChatClient client;
    public void setId(String id) {
        this.id = id;
    }
    private Socket socket;
    @Override
    public void start(Stage stage) throws IOException {

        VBox root = new VBox();
        root.setPrefSize(1000, 400);
        root.setSpacing(10);

        TextField nameSpace = new TextField("이름을 입력하세요");

        Button btn = new Button("이름 입력");
        TextField textField = new TextField();
        Button btn1 = new Button("채팅 입력");
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        root.getChildren().addAll(nameSpace,btn,textArea);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                id = nameSpace.getText();
                client = new ChatClient();

                client.setId(id);
                client.connect("127.0.0.1", 8888,textArea, id);
            }
        });

        btn1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    client.sender.sendMessage(textField.getText());
                    textField.setText("");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        root.getChildren().addAll(textField, btn1);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }

    private static boolean hasNotArgs(String[] args) {
        return args.length == 0;
    }

    private void connect(String severHost, int port, TextArea textArea, String identify) {
        try {
            socket = new Socket(severHost, port);
            System.out.println("Connected to Server " + severHost + ":" + port);
            sender = new Sender(socket, identify);
            receiver = new Receiver(socket, textArea);

            sender.start();
            receiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Sender extends Thread {
        private String id;
        private DataOutputStream out;
        private String message;

        public void setMessage(String message) {
            this.message = message;
        }

        private Sender(Socket socket, String id) throws IOException {
            this.id = id;
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        private void setId(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                initialize();
            } catch (IOException e) {
                // TODO
            }
        }

        private void initialize() throws IOException {
            if (isSendAble()) {
                this.out.writeUTF(id);

            }
        }

        private boolean isSendAble() {
            return this.out != null;
        }

        private void sendMessage(String message) throws IOException {
            this.out.writeUTF(message);
        }
    }

    private static class Receiver extends Thread {
        private final DataInputStream in;
        private TextArea textArea;

        private Receiver(Socket socket, TextArea textArea) throws IOException {
            this.in = new DataInputStream(socket.getInputStream());
            this.textArea = textArea;
        }

        @Override
        public void run() {
            while (isReceivable()) {
                receiveMessage();
            }
        }

        private boolean isReceivable() {
            return this.in != null;
        }

        private void receiveMessage() {
            try {
                //System.out.println(in.readUTF());
                StringBuilder text = new StringBuilder();
                text.append(textArea.getText()).append("\n").append(in.readUTF());
                textArea.setText(text.toString());
            } catch (IOException e) {
                // TODO
            }

        }
    }

}