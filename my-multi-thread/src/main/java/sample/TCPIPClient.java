package sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class TCPIPClient extends JFrame {
    private JTextField idField;
    private JTextField betField;
    private String userId;
    private PrintWriter writer;
    private BufferedReader serverReader;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel coinLabel;
    private JLabel resultCoinLabel1;
    private JLabel resultCoinLabel2;
    private JLabel resultCoinLabel3;
    private JLabel resultCoinLabel4;

    public TCPIPClient() {
        setTitle("TCP/IP Client");
        setSize(800, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        connectToServer();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initSelectPanel();
        initLoginPanel();
        initMatchPanel();
        initWaitingPanel();
        initGamePanel();
        initMultiPanel();
        initMultiWaitingPanel("src/main/java/sample/images/wait0.png", "wait0");
        initMultiWaitingPanel("src/main/java/sample/images/wait2.png", "wait2");
        initMultiWaitingPanel("src/main/java/sample/images/wait5.png", "wait5");        
        resultCoinLabel1 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/cho_lose.png", "resultCL", resultCoinLabel1);
        resultCoinLabel2 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/cho_win.png", "resultCW", resultCoinLabel2);
        resultCoinLabel3 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/han_lose.png", "resultHL", resultCoinLabel3);
        resultCoinLabel4 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/han_win.png", "resultHW", resultCoinLabel4);
        resultCoinLabel5 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/multi_win.png", "resultW", resultCoinLabel5);
        resultCoinLabel6 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/multi_draw.png", "resultD", resultCoinLabel6);
        resultCoinLabel7 = new JLabel("Your Coin");
        initResultPanel("src/main/java/sample/images/multi_lose.png", "resultL", resultCoinLabel7);

        getContentPane().add(mainPanel);
    }

    private void initSelectPanel() {
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        idField.setBounds(340, 280, 200, 50);
        JButton gameButton = createTransparentButton("", 0, 0, 400, 450);
        JButton matchButton = createTransparentButton("", 400, 0, 400, 450);

        gameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "loginPanel");
            }
        });

        matchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "matchPanel");
            }
        });

        BackgroundPanel selectPanel = new BackgroundPanel("src/main/java/sample/images/game_select.png");
        selectPanel.setLayout(null);
        selectPanel.add(gameButton);
        selectPanel.add(matchButton);

        mainPanel.add(selectPanel, "selectPanel");
    }

    private void initLoginPanel() {
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        idField.setBounds(340, 280, 200, 50);
        JButton loginButton = createTransparentButton("", 320, 340, 160, 30);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    if (writer != null) {
                        writer.println("LOGIN " + userId);
                        cardLayout.show(mainPanel, "gamePanel");
                    } else {
                        // outputArea.append("Error: Not connected to server.\n");
                    }
                }
            }
        });

        BackgroundPanel loginPanel = new BackgroundPanel("src/main/java/sample/images/login.png");
        loginPanel.setLayout(null);
        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(loginButton);

        mainPanel.add(loginPanel, "loginPanel");
    }

    private void initMatchPanel() {
        JLabel idLabel = new JLabel("Your ID");
        idField = new JTextField(30);
        idField.setBounds(340, 280, 200, 50);
        JButton matchButton = createTransparentButton("", 320, 340, 160, 30);

        matchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    if (writer != null) {
                        writer.println("MULTIGAME_MATCH " + userId);
                        cardLayout.show(mainPanel, "waiting");
                    } else {
                        // outputArea.append("Error: Not connected to server.\n");
                    }
                }
            }
        });

        BackgroundPanel matchPanel = new BackgroundPanel("src/main/java/sample/images/multi_match.png");
        matchPanel.setLayout(null);
        matchPanel.add(idLabel);
        matchPanel.add(idField);
        matchPanel.add(matchButton);

        mainPanel.add(matchPanel, "matchPanel");
    }
    

    private void initGamePanel() {
        BackgroundPanel gamePanel = new BackgroundPanel("src/main/java/sample/images/game.png");
        gamePanel.setLayout(null); // Absolute layout for positioning buttons

        betField = new JTextField(10);
        betField.setBounds(330, 330, 200, 40);
        gamePanel.add(betField);

        coinLabel = new JLabel("Your Coin: x"); // coinLabel の初期化
        coinLabel.setBounds(220, 15, 200, 40);
        gamePanel.add(coinLabel);

        JButton choButton = createTransparentButton("", 0, 100, 200, 250);
        choButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(0);
            }
        });

        JButton hanButton = createTransparentButton("", 600, 100, 200, 250);
        hanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendGameMessage(1);
            }
        });

        gamePanel.add(choButton);
        gamePanel.add(hanButton);

        mainPanel.add(gamePanel, "gamePanel");
    }

    private void initResultPanel(String imagePath, String panelName, JLabel resultCoinLabel) {
        resultCoinLabel.setBounds(350, 265, 200, 50);
        JButton retryButton = createTransparentButton("", 280, 325, 100, 30);
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userId = idField.getText();
                if (userId != null && !userId.isEmpty()) {
                    cardLayout.show(mainPanel, "gamePanel");
                }
            }
        });

        JButton exitButton = createTransparentButton("", 420, 325, 100, 30);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        BackgroundPanel resultPanel = new BackgroundPanel(imagePath);
        resultPanel.setLayout(null);
        resultPanel.add(resultCoinLabel);
        resultPanel.add(retryButton);
        resultPanel.add(exitButton);

        mainPanel.add(resultPanel, panelName);
    }

    private JButton createTransparentButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        return button;
    }

    private void connectToServer() {
        String hostname = "127.0.0.1";
        int port = 12345;

        try {
            Socket socket = new Socket(hostname, port);
            // outputArea.append("Connected to the server\n");

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            serverReader = new BufferedReader(new InputStreamReader(input));

            // Thread to listen for server messages
            new Thread(() -> {
                try {
                    String response;
                    while ((response = serverReader.readLine()) != null) {
                        String[] parts = response.split(" ");
                        if (parts[0].equals("LOGIN")) {
                            if (parts[1].equals("FAIL")) {
                                SwingUtilities.invokeLater(() -> {
                                    cardLayout.show(mainPanel, "loginPanel");
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    coinLabel.setText("Your Coin: " + parts[2]);
                                });
                            }
                        }

                        if (parts[0].equals("GAME")) {
                            SwingUtilities.invokeLater(() -> {
                                handleGameResponse(parts);
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendGameMessage(int choice) {
        String coin = betField.getText();
        if (coin != null && !coin.isEmpty()) {
            String message = "GAME " + userId + " " + coin + " " + choice;
            writer.println(message);
        }
    }

    private void handleGameResponse(String[] parts) {
        if (parts.length < 5) {
            System.err.println("Invalid response from server: " + String.join(" ", parts));
            return;
        }

        coinLabel.setText("Your Coin: " + parts[4]);

        if (parts[1].equals("WIN") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 0) {
            resultCoinLabel2.setText("Your Coin: " + parts[4]);
            cardLayout.show(mainPanel, "resultCW");
        } else if (parts[1].equals("WIN") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 1) {
            resultCoinLabel4.setText("Your Coin: " + parts[4]);
            cardLayout.show(mainPanel, "resultHW");
        } else if (parts[1].equals("LOSE") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 0) {
            resultCoinLabel1.setText("Your Coin: " + parts[4]);
            cardLayout.show(mainPanel, "resultCL");
        } else if (parts[1].equals("LOSE") && (Integer.parseInt(parts[2]) + Integer.parseInt(parts[3])) % 2 == 1) {
            resultCoinLabel3.setText("Your Coin: " + parts[4]);
            cardLayout.show(mainPanel, "resultHL");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TCPIPClient().setVisible(true);
        });
    }
}

class BackgroundPanel extends JPanel {
    private BufferedImage image;

    public BackgroundPanel(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}