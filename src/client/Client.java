package client;

import interfaces.Executable;
import interfaces.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

public class Client extends JFrame {
    private JTextField ipField, portField, numberField;
    private JTextArea resultArea;
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client() {
        super("TCP Client");

        setLayout(new BorderLayout());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 3));

        ipField = new JTextField("localhost");
        portField = new JTextField("12345");
        numberField = new JTextField("27");

        topPanel.add(new JLabel("IP Address:"));
        topPanel.add(ipField);
        topPanel.add(new JLabel(""));
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("N:"));
        topPanel.add(numberField);

        add(topPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton calculateButton = new JButton("Calculate");
        calculateButton.setBackground(Color.PINK);
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendTask();
            }
        });

        JButton clearButton = new JButton("Clear Result");
        clearButton.setBackground(Color.PINK);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("");
            }
        });

        JButton exitButton = new JButton("Exit Program");
        exitButton.setBackground(Color.PINK);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeConnection();
                System.exit(0);
            }
        });

        buttonPanel.add(calculateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void sendTask() {
        try {
            if (client == null || client.isClosed()) {
                client = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
                resultArea.append("Connected to server\n");
            }

            int number = Integer.parseInt(numberField.getText());
            JobOne job = new JobOne(number);
            out.writeObject(job);

            resultArea.append("Submitted a job for execution\n");

            Result result = (Result) in.readObject();
            resultArea.append("result = " + result.output() + ", time taken = " + result.scoreTime() + "ns\n");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            resultArea.append("Error: " + e.getMessage() + "\n");
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (client != null && !client.isClosed()) {
                client.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}

class JobOne implements Executable {
    private static final long serialVersionUID = 1L;
    private int number;

    public JobOne(int number) {
        this.number = number;
    }

    @Override
    public Object execute() {
        return factorial(number);
    }

    private BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}
