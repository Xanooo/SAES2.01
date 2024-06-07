package fr.univamu.iut.chess.controllers;

import fr.univamu.iut.chess.ChessApplication;
import fr.univamu.iut.chess.Piece.Couleur;
import fr.univamu.iut.chess.Piece.Piece;
import fr.univamu.iut.chess.Piece.Plateau;
import fr.univamu.iut.chess.Piece.Position;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class ChessBotGameController implements Initializable {
    @FXML
    private Label timeLabelWhite;

    @FXML
    private Label timeLabelBlack;

    private Timeline timerWhite;
    private Timeline timerBlack;
    private int timeWhite = 600; // 10 minutes in seconds
    private int timeBlack = 600; // 10 minutes in seconds
    private boolean isWhiteTurn = true;

    @FXML
    private GridPane gridPaneJeu;
    @FXML
    private Label tourMessage;

    private Plateau plateau;
    private Piece selectedPiece;
    private Position selectedPosition;
    private Couleur currentTurn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTimers();
        timeLabelWhite.setOnMouseClicked(event -> handleMove());
        this.plateau = new Plateau();
        this.currentTurn = Couleur.WHITE;
        afficherPlateau();
        afficherTourMessage();
        startGame();
    }

    public void afficherPlateau() {
        gridPaneJeu.getChildren().clear();

        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Rectangle rectangle = new Rectangle(40, 40);
                if ((ligne + colonne) % 2 == 0) {
                    rectangle.setFill(Color.BEIGE);
                } else {
                    rectangle.setFill(Color.GREEN);
                }

                StackPane stackPane = new StackPane();
                stackPane.getChildren().add(rectangle);

                Piece piece = plateau.getPieces(ligne, colonne);
                if (piece != null) {
                    Image image = new Image(getClass().getResourceAsStream(piece.getImagePath()));
                    ImageView imageView = new ImageView(image);
                    stackPane.getChildren().add(imageView);

                    int finalLigne = ligne;
                    int finalColonne = colonne;
                    imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> handlePieceClick(piece, new Position(finalLigne, finalColonne)));
                } else {
                    int finalLigne1 = ligne;
                    int finalColonne1 = colonne;
                    stackPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> handleEmptySquareClick(new Position(finalLigne1, finalColonne1)));
                }

                gridPaneJeu.add(stackPane, colonne, ligne);
            }
        }
    }

    private void handlePieceClick(Piece piece, Position position) {
        if (selectedPiece == null) {
            if (piece.getColor().equals(currentTurn)) {
                selectedPiece = piece;
                selectedPosition = position;
                System.out.println("Piece selected: " + piece.getClass().getSimpleName() + " at position " + position.getRow() + ", " + position.getCol());
            }
        } else {
            movePiece(position);
        }
    }

    private void handleEmptySquareClick(Position position) {
        if (selectedPiece != null) {
            movePiece(position);
        }
    }

    private void movePiece(Position newPosition) {
        if (selectedPiece != null && selectedPiece.estDeplacementValide(
                selectedPosition.getRow(), selectedPosition.getCol(),
                newPosition.getRow(), newPosition.getCol(), plateau.getPieces())) {
            System.out.println("Moving piece to " + newPosition.getRow() + ", " + newPosition.getCol());
            plateau.deplacerPiece(
                    selectedPosition.getRow(), selectedPosition.getCol(),
                    newPosition.getRow(), newPosition.getCol(), plateau.getPieces());

            selectedPiece = null;
            selectedPosition = null;
            switchTurn();
            afficherPlateau();
        }
        else {
            selectedPiece = null;
            selectedPosition = null;
        }
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Couleur.WHITE) ? Couleur.BLACK : Couleur.WHITE;
        afficherTourMessage();
        if (currentTurn == Couleur.BLACK) {
            jouerTourBot();
        }
    }

    private void afficherTourMessage() {
        tourMessage.setText((currentTurn == Couleur.WHITE ? "Les blancs" : "Les noirs") + " jouent !");
    }
    private void jouerTourBot() {
        List<Move> validMoves = obtenirDeplacementsValides(Couleur.BLACK);
        if (!validMoves.isEmpty()) {
            Random rand = new Random();
            Move move = validMoves.get(rand.nextInt(validMoves.size()));
            selectedPiece = plateau.getPieces(move.from.getRow(), move.from.getCol());
            selectedPosition = new Position(move.from.getRow(),move.from.getCol());
            handleEmptySquareClick(new Position(move.to.getRow(),move.to.getCol()));
            movePiece(move.to);
        }
    }

    private List<Move> obtenirDeplacementsValides(Couleur couleur) {
        List<Move> validMoves = new ArrayList<>();
        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = plateau.getPieces(ligne, colonne);
                if (piece != null && piece.getColor().equals(couleur)) {
                    for (int newLigne = 0; newLigne < 8; newLigne++) {
                        for (int newColonne = 0; newColonne < 8; newColonne++) {
                            if (piece.estDeplacementValide(ligne, colonne, newLigne, newColonne, plateau.getPieces())) {
                                validMoves.add(new Move(new Position(ligne, colonne), new Position(newLigne, newColonne)));
                            }
                        }
                    }
                }
            }
        }
        return validMoves;
    }
    private class Move {
        Position from;
        Position to;

        Move(Position from, Position to) {
            this.from = from;
            this.to = to;
        }
    }
    private void setupTimers() {
        timerWhite = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeWhite--;
            updateTimeLabel(timeLabelWhite, timeWhite);
            if (timeWhite <= 0) {
                endGame(Couleur.BLACK);
            }
        }));
        timerWhite.setCycleCount(Timeline.INDEFINITE);

        timerBlack = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeBlack--;
            updateTimeLabel(timeLabelBlack, timeBlack);
            if (timeBlack <= 0) {
                endGame(Couleur.WHITE);
            }
        }));
        timerBlack.setCycleCount(Timeline.INDEFINITE);
        updateTimeLabel(timeLabelWhite, timeWhite);
        updateTimeLabel(timeLabelBlack, timeBlack);
    }

    private void updateTimeLabel(Label label, int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void handleMove() {
        if (isWhiteTurn) {
            timerWhite.stop();
            timerBlack.play();
        } else {
            timerBlack.stop();
            timerWhite.play();
        }
        isWhiteTurn = !isWhiteTurn;
    }
    private void startGame() {
        timerWhite.play();
    }
    private void endGame(Couleur winnerColor) {
        timerWhite.stop();
        timerBlack.stop();

        String winner = (winnerColor == Couleur.WHITE) ? "Les blancs" : "Les noirs";
        System.out.println(winner+" on gagnés");
        Platform.exit(); // fermer l'application
    }
}
