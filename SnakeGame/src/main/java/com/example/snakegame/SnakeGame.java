package com.example.snakegame;


import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

public class SnakeGame extends Application {
    private Label scoreLabel;
    private static final int WIDTH = 30, HEIGHT = 18, CELL_SIZE = 30;

    private char[][] grid = new char[HEIGHT][WIDTH];
    private SnakeLinkedList snake;
    private int foodX, foodY;
    private Direction direction = Direction.RIGHT;
    private boolean gameOver = false, spacePressed = false;
    private GraphicsContext gc;
    private GameView gameView;
    private MediaPlayer splashSound;
    private MediaPlayer eatSound;
    private MediaPlayer gameOverSound;
    private String playerName = "";
    private String playerCode = "";
    private int score = 0;
    private String difficulty = "Easy";
    private long speed = 200_000_000;

    private int bonusFoodX, bonusFoodY;
    private boolean hasBonusFood = false;
    private long lastBonusSpawnAttemptTime = 0;
    private long bonusFoodActiveStartTime = 0;
    private final long BONUS_SPAWN_COOLDOWN = 8_000_000_000L;
    private final long BONUS_LIFETIME = 4_000_000_000L;
    private final int BONUS_SCORE_THRESHOLD = 10;
    private int segmentsToAdd = 0;
    private Random random = new Random();


    private AnimationTimer gameLoopTimer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        loadAudio();
        stage.setScene(splashScene(stage));
        stage.setTitle("Desert Slither");
        stage.show();
    }

    private void loadAudio() {
        try {
            URL splashSoundUrl = getClass().getResource("/5091770_snake-charmer_by_mystic8_preview.mp3");
            if (splashSoundUrl != null) {
                Media splashMedia = new Media(splashSoundUrl.toString());
                splashSound = new MediaPlayer(splashMedia);
                splashSound.setCycleCount(1);
                splashSound.play();
            }

            // Load eat sound
            URL eatSoundUrl = getClass().getResource("/Chomp.wav");
            if (eatSoundUrl != null) {
                Media eatMedia = new Media(eatSoundUrl.toString());
                eatSound = new MediaPlayer(eatMedia);
            }

            // Load game over sound
            URL gameOverSoundUrl = getClass().getResource("/game-over-arcade-6435.mp3");
            if (gameOverSoundUrl != null) {
                Media gameOverMedia = new Media(gameOverSoundUrl.toString());
                gameOverSound = new MediaPlayer(gameOverMedia);
            }
        } catch (Exception e) {
            System.err.println("Error loading audio files: " + e.getMessage());
        }
    }

    private Scene splashScene(Stage stage) {
        StackPane splashRoot = new StackPane();
        splashRoot.setStyle("-fx-background-color: linear-gradient(to right, #141e30, #243b55);");
        splashRoot.setPrefSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);

        ImageView logoView = new ImageView();
        Label fallback = null;

        try {
            URL logoUrl = getClass().getResource("logo.png");
            if (logoUrl == null) throw new FileNotFoundException("logo.png not found");
            Image logoImage = new Image(logoUrl.toExternalForm(), 200, 0, true, true);
            logoView.setImage(logoImage);
        } catch (Exception ex) {
            System.err.println("Splash image load error: " + ex);
            fallback = new Label("Desert Slither");
            fallback.setTextFill(Color.WHITE);
            fallback.setFont(Font.font("Verdana", FontWeight.BOLD, 48));
        }

        if (fallback != null) {
            splashRoot.getChildren().add(fallback);
            Platform.runLater(() -> stage.setScene(loginScene(stage)));
        } else {
            logoView.setOpacity(0);
            splashRoot.getChildren().add(logoView);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), logoView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeIn.setOnFinished(e -> {
                if (splashSound != null) {
                    splashSound.stop();
                    splashSound.play();
                }
            });

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), logoView);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            final boolean[] switched = {false};

            fadeOut.setOnFinished(e -> {
                if (!switched[0]) {
                    switched[0] = true;
                    Platform.runLater(() -> {
                        stage.setTitle("Snake Game – Login");
                        stage.setScene(loginScene(stage));
                    });
                }
            });

            new SequentialTransition(fadeIn, pause, fadeOut).play();
        }
        if (splashSound != null) splashSound.stop();
        return new Scene(splashRoot);
    }

    private Scene loginScene(Stage stage) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
        box.setStyle("-fx-background-color: linear-gradient(to bottom right, #141e30, #243b55);");

        Label title = new Label("Snake Game");
        title.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 36));
        title.setTextFill(Color.web("#ecf0f1"));
        title.setEffect(new DropShadow(20, Color.BLACK));

        TextField nameInput = styledTextField("Player Name");
        TextField codeInput = styledTextField("Game Code");

        Label message = new Label();
        message.setTextFill(Color.RED);

        Button signUp = styledButton("Sign Up");
        signUp.setOnAction(e -> {
            if (nameInput.getText().isEmpty() || codeInput.getText().isEmpty()) {
                message.setText("Fill both fields");
            } else {
                savePlayerData(codeInput.getText(), nameInput.getText());
                message.setTextFill(Color.LIGHTGREEN);
                message.setText("Signed up!");
            }
            if (splashSound != null) splashSound.stop();
        });

        Button login = styledButton("Login");
        login.setOnAction(e -> {
            if (verifyPlayerData(codeInput.getText(), nameInput.getText())) {
                playerName = nameInput.getText();
                playerCode = codeInput.getText();
                stage.setScene(difficultyScene(stage));
            } else {
                message.setTextFill(Color.RED);
                message.setText("Invalid login");
            }
        });

        box.getChildren().addAll(title, nameInput, codeInput, signUp, login, message);
        return new Scene(box);
    }

private Scene difficultyScene(Stage stage) {
    VBox box = new VBox(20);
    box.setAlignment(Pos.CENTER);
    box.setPrefSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
    box.setStyle("-fx-background-color: linear-gradient(to bottom right, #141e30, #243b55);");

    Label label = new Label("Select Difficulty");
    label.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
    label.setTextFill(Color.WHITE);

    Button easy = styledButton("Easy");
    Button med = styledButton("Medium");
    Button hard = styledButton("Hard");

    easy.setOnAction(e -> setDifficulty("Easy", 200_000_000, stage));
    med.setOnAction(e -> setDifficulty("Medium", 120_000_000, stage));
    hard.setOnAction(e -> setDifficulty("Hard", 80_000_000, stage));

    // ---------- Bottom Bar (Back Button) ----------
    Button backButton = new Button("Back");
    backButton.setFont(Font.font(14));
    backButton.setTextFill(Color.WHITE);
    backButton.setStyle("-fx-background-color:Orange;");
    backButton.setOnMouseEntered(e ->
            backButton.setStyle("-fx-background-color:Red;"));
    backButton.setOnMouseExited(e ->
            backButton.setStyle("-fx-background-color: Orange;"));

    backButton.setOnAction(e -> {
        stage.setScene(loginScene(stage));
    });

    HBox bottomBar = new HBox(backButton);
    bottomBar.setAlignment(Pos.CENTER);
    bottomBar.setPadding(new Insets(10));
    bottomBar.setStyle("-fx-background-color: linear-gradient(to bottom right, #141e30, #243b55);");

    VBox content = new VBox(20, label, easy, med, hard);
    content.setAlignment(Pos.CENTER);

    box.getChildren().addAll(content, bottomBar);
    VBox.setVgrow(content, Priority.ALWAYS);

    return new Scene(box);
}

    private TextField styledTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setMaxWidth(200);
        tf.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;"
                + "-fx-background-radius: 8px; -fx-padding: 8;");
        return tf;
    }

    private Button styledButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setFont(Font.font(16));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px;");
        DropShadow ds = new DropShadow(10, Color.DODGERBLUE);
        btn.setEffect(ds);

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2980b9; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3498db; -fx-background-radius: 8px;"));
        return btn;
    }

private Scene startGameScene(Stage stage) {
    VBox box = new VBox();
    box.setAlignment(Pos.CENTER);
    box.setPrefSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
    box.setStyle("-fx-background-color: linear-gradient(to bottom right, #141e30, #243b55);");

    Label label = new Label("Ready to Start?");
    label.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
    label.setTextFill(Color.WHITE);

    Button startButton = styledButton("Start Game");
    startButton.setOnAction(e -> stage.setScene(gameScene(stage)));

    // ---------- Bottom Bar (Back Button) ----------
    Button backButton = new Button("Back");
    backButton.setFont(Font.font(14));
    backButton.setTextFill(Color.WHITE);
    backButton.setStyle("-fx-background-color:Orange;");
    backButton.setOnMouseEntered(e ->
            backButton.setStyle("-fx-background-color:Red;"));
    backButton.setOnMouseExited(e ->
            backButton.setStyle("-fx-background-color: Orange;"));

    backButton.setOnAction(e -> {
        stage.setScene(difficultyScene(stage));
    });

    HBox bottomBar = new HBox(backButton);
    bottomBar.setAlignment(Pos.CENTER);
    bottomBar.setPadding(new Insets(10));
    bottomBar.setStyle("-fx-background-color: linear-gradient(to bottom right, #141e30, #243b55);");

    VBox content = new VBox(30, label, startButton);
    content.setAlignment(Pos.CENTER);

    box.getChildren().addAll(content, bottomBar);
    VBox.setVgrow(content, Priority.ALWAYS);

    return new Scene(box);
}


    private void setDifficulty(String diff, long sp, Stage stage) {
        difficulty = diff;
        speed = sp;
        stage.setScene(startGameScene(stage)); // Go to the new scene
    }

    private Scene gameScene(Stage stage) {
        // ---------- Top Bar ----------
        Label nameLabel = new Label("Player: " + playerName);
        nameLabel.setTextFill(Color.ORANGE);
        nameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Label titleLabel = new Label("Desert Slither");
        titleLabel.setTextFill(Color.ORANGE);
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 22));

        scoreLabel = new Label("Score: 0");
        scoreLabel.setTextFill(Color.ORANGE);
        scoreLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);");

        // Left: Player name
        topBar.setLeft(nameLabel);
        BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);

        // Center: Title
        topBar.setCenter(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        // Right: Score
        topBar.setRight(scoreLabel);
        BorderPane.setAlignment(scoreLabel, Pos.CENTER_RIGHT);

        // ---------- Game Canvas ----------
        Canvas canvas = new Canvas(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();
        gameView = new GameView(gc, CELL_SIZE);
        StackPane canvasPane = new StackPane(canvas);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        // ---------- Bottom Bar (Back Button) ----------
        Button backToDifficultyBtn = new Button("Back");
        backToDifficultyBtn.setFont(Font.font(14));
        backToDifficultyBtn.setTextFill(Color.WHITE);
        backToDifficultyBtn.setStyle("-fx-background-color:Orange;");
        backToDifficultyBtn.setOnMouseEntered(e ->
                backToDifficultyBtn.setStyle("-fx-background-color:Red;"));
        backToDifficultyBtn.setOnMouseExited(e ->
                backToDifficultyBtn.setStyle("-fx-background-color: Orange;"));

        backToDifficultyBtn.setOnAction(e -> {
            if (gameLoopTimer != null) {
                gameLoopTimer.stop();
            }
            gameOver = true;
            score = 0;
            scoreLabel.setText("Score: 0");
            stage.setTitle("Snake Game – Difficulty");
            stage.setScene(startGameScene(stage));
        });

        HBox bottomBar = new HBox(backToDifficultyBtn);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setStyle("-fx-background-color: #2c3e50;");
        bottomBar.setStyle("-fx-background-color: linear-gradient(to right, #0f2027, #203a43, #2c5364);");

        // ---------- Main Layout ----------
        VBox root = new VBox(topBar, canvasPane, bottomBar);

        Scene scene = new Scene(root);

        // ---------- Input Handling ----------
        scene.setOnKeyPressed(evt -> {
            KeyCode c = evt.getCode();
            if (!gameOver) {
                switch (c) {
                    case W, UP -> {
                        if (direction != Direction.DOWN) direction = Direction.UP;
                    }
                    case S, DOWN -> {
                        if (direction != Direction.UP) direction = Direction.DOWN;
                    }
                    case A, LEFT -> {
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                    }
                    case D, RIGHT -> {
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                    }
                }
            } else if (c == KeyCode.SPACE) {
                spacePressed = true;
            }
        });

        // ---------- Start Game ----------
        stage.setTitle("Playing as " + playerName);
        initializeGame();
        startGameLoop();
        return scene;
    }

    private void initializeGame() {
        snake = new SnakeLinkedList(WIDTH / 2, HEIGHT / 2);
        direction = Direction.RIGHT;
        score = 0;
        gameOver = false;
        spacePressed = false; // Reset spacePressed
        generateFood();

        hasBonusFood = false;
        bonusFoodX = -1;
        bonusFoodY = -1;
        segmentsToAdd = 0;
        lastBonusSpawnAttemptTime = System.nanoTime();
        bonusFoodActiveStartTime = 0;
    }

    private void generateFood() {
        do {
            foodX = random.nextInt(WIDTH);
            foodY = random.nextInt(HEIGHT);
        } while (snake.checkCollision(foodX, foodY) || (hasBonusFood && foodX == bonusFoodX && foodY == bonusFoodY));
    }

    private void generateBonusFood() {
        int newBonusX, newBonusY;
        int attempts = 0;
        do {
            newBonusX = random.nextInt(WIDTH);
            newBonusY = random.nextInt(HEIGHT);
            attempts++;
            if (attempts > WIDTH * HEIGHT * 2) {
                return;
            }
        } while (snake.checkCollision(newBonusX, newBonusY) ||
                (newBonusX == foodX && newBonusY == foodY) ||
                (hasBonusFood && newBonusX == bonusFoodX && newBonusY == bonusFoodY));

        bonusFoodX = newBonusX;
        bonusFoodY = newBonusY;
        hasBonusFood = true;
        bonusFoodActiveStartTime = System.nanoTime();
    }

    private void updateGame() {
        // Handle game over state and restart
        if (gameOver) {
            if (spacePressed) {
                initializeGame();
                spacePressed = false;
            }
            return;
        }

        // All the game logic below this line will only execute if gameOver is false
        if (score >= BONUS_SCORE_THRESHOLD && !hasBonusFood) {
            if (System.nanoTime() - lastBonusSpawnAttemptTime >= BONUS_SPAWN_COOLDOWN) {
                if (random.nextDouble() < 0.4) {
                    generateBonusFood();
                }
                lastBonusSpawnAttemptTime = System.nanoTime();
            }
        }

        if (hasBonusFood && System.nanoTime() - bonusFoodActiveStartTime >= BONUS_LIFETIME) {
            hasBonusFood = false;
            bonusFoodX = -1;
            bonusFoodY = -1;
        }

        SnakeNode head = snake.getHead();
        int nx = head.x + (direction == Direction.LEFT ? -1 : direction == Direction.RIGHT ? 1 : 0);
        int ny = head.y + (direction == Direction.UP ? -1 : direction == Direction.DOWN ? 1 : 0);

        if ("Hard".equals(difficulty) && (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT)) {
            gameOver();
            return;
        }

        nx = (nx + WIDTH) % WIDTH;
        ny = (ny + HEIGHT) % HEIGHT;

        if (snake.checkCollision(nx, ny)) {
            gameOver();
            return;
        }

        snake.addFirst(nx, ny);

        if (nx == foodX && ny == foodY) {
            score++;
            generateFood();
            if (eatSound != null) {
                eatSound.stop();
                eatSound.play();
            }
        } else if (hasBonusFood && nx == bonusFoodX && ny == bonusFoodY) {
            score += 5;
            segmentsToAdd = random.nextInt(2) + 2;
            hasBonusFood = false;
            bonusFoodX = -1;
            bonusFoodY = -1;

            if (eatSound != null) {
                eatSound.stop();
                eatSound.play();
            }
        } else if (segmentsToAdd > 0) {
            segmentsToAdd--;
        } else {
            snake.removeLast();
        }
    }

    private void gameOver() {
        gameOver = true;
        saveScore();

        if (gameOverSound != null) {
            gameOverSound.stop();
            gameOverSound.play();
        }
    }

    private void renderGame() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = '.';
            }
        }

        snake.markOnGrid(grid);
        gameView.render(grid, foodX, foodY, difficulty, bonusFoodX, bonusFoodY, hasBonusFood);

        if (gameOver) {
            gc.setFont(Font.font("Consolas", FontWeight.EXTRA_BOLD, 36));
            gc.setFill(Color.RED);
            gc.fillText("Game Over!", WIDTH * CELL_SIZE / 2.0 - 120, HEIGHT * CELL_SIZE / 2.0);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            gc.setFill(Color.WHITE);
            gc.fillText("Press SPACE to Restart", WIDTH * CELL_SIZE / 2.0 - 140, HEIGHT * CELL_SIZE / 2.0 + 40);
        }

        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score);
        }
    }

    private void startGameLoop() {
        long[] last = {0};
        gameLoopTimer = new AnimationTimer() { // Assign to the instance variable
            @Override
            public void handle(long now) {
                if (now - last[0] >= speed) {
                    updateGame();
                    renderGame();
                    last[0] = now;
                }
            }
        };
        gameLoopTimer.start();
    }

    private void savePlayerData(String code, String name) {
        safeWrite("players.txt", code + "," + name + "\n");
    }

    private void saveScore() {
        safeWrite("scores.txt", playerName + " (" + playerCode + "): " + score + "\n");
    }

    private void safeWrite(String file, String text) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(text);
        } catch (IOException ignored) {
        }
    }

    private boolean verifyPlayerData(String code, String name) {
        try (Scanner sc = new Scanner(new File("players.txt"))) {
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length == 2 && parts[0].equals(code) && parts[1].equalsIgnoreCase(name)) return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }
}