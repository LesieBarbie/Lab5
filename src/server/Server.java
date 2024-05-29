package server;

import interfaces.Executable;
import interfaces.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    private JTextField portField;
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private boolean running = false;

    public Server() {
        super("TCP Server");

        setLayout(new BorderLayout());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 2));

        portField = new JTextField("12345");

        topPanel.add(new JLabel("Working Port:"));
        topPanel.add(portField);

        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton startButton = new JButton("Start Server");
        startButton.setBackground(Color.PINK);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!running) {
                    startServer();
                }
            }
        });

        JButton stopButton = new JButton("Stop Server");
        stopButton.setBackground(Color.PINK);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (running) {
                    stopServer();
                }
            }
        });

        JButton exitButton = new JButton("Exit Server");
        exitButton.setBackground(Color.PINK);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(portField.getText()));
            logArea.append("The server is waiting for connections...\n");
            running = true;

            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            logArea.append("Connection " + clientSocket.getInetAddress() + " starting execution...\n");
            Executable task = (Executable) in.readObject();

            long startTime = System.nanoTime();
            Object result = task.execute();
            long endTime = System.nanoTime();

            double duration = (endTime - startTime) / 1_000_000.0;

            ResultImpl resultObj = new ResultImpl(result, duration);
            out.writeObject(resultObj);

            logArea.append("Connection " + clientSocket.getInetAddress() + " [WORK DONE]\n");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void stopServer() {
        try {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
                logArea.append("The server stops working...\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
