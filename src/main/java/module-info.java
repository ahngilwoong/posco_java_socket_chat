module com.example.posco_java_socket_chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.posco_java_socket_chat to javafx.fxml;
    exports com.example.posco_java_socket_chat;
}