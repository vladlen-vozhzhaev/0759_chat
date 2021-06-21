package server;

import org.w3c.dom.ls.LSOutput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;

public class Server {
    static ArrayList<User> users = new ArrayList<>();
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8188); // Создаёи серверный сокет
            System.out.println("Сервер запущен");
            while (true){ // бесконечный цикл для ожидания подключения клиентов
                System.out.println("Ожидаю подключения клиентов...");
                Socket socket = serverSocket.accept(); // Ожидаем подключения клиента
                User currentUser = new User(socket);
                users.add(currentUser);
                System.out.println("Клиент подключился");
                DataInputStream in = new DataInputStream(socket.getInputStream()); // Поток ввода
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); // Поток вывода
                currentUser.setOos(oos);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String request = null;
                        try {
                            currentUser.getOos().writeObject("Введите имя: ");
                            String userName = in.readUTF();
                            currentUser.setUserName(userName);
                            sendUserList(); //Отправляем обновление списка пользователей
                            System.out.println(currentUser.getUserName()+" добро пожаловать на сервер!");
                            currentUser.getOos().writeObject(currentUser.getUserName()+" добро пожаловать на сервер!");
                            while (true){
                                    request = in.readUTF(); // Принимает сообщение от клиента
                                    System.out.println("Клиент прислал: "+request);
                                    for (User user: users) { // Перебираем клиентов которые подключенны в настоящий момент
                                        if(currentUser != user){
                                            user.getOos().writeObject(currentUser.getUserName()+": "+request); // Рассылает принятое сообщение всем клиентам
                                        }
                                    }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            users.remove(currentUser);
                            for (User user: users) { // Перебираем клиентов которые подключенны в настоящий момент
                                try {
                                    user.getOos().writeObject("Пользователь "+currentUser.getUserName()+" покинул чат"); // Рассылает принятое сообщение всем клиентам
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                             // Удаление сокета, когда клиент отключился
                            sendUserList(); //Отправляем обновление списка пользователей
                        }
                    }
                });
                thread.start();
            }





        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendUserList(){
        String usersName = "**userList**";
        for (User user:users) {
            usersName += "//"+user.getUserName(); // **userList**//user1//user2//user3
        }
        for(User user : users){
            try {
                user.getOos().writeObject(usersName);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}

