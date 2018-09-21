package gui;

import java.io.File;
import java.util.List;

import cipher.CryptrCipher;
import compressor.CryptrCompressor;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * CryptrGUI is a JavaFX application which allows users to easily encrypt and
 * decrypt files with a graphical interface.
 */
public class CryptrGUI extends Application {

    // true iff the user has selected to encrypt files
    private boolean encryptMode = false;

    @Override
    public void start(Stage stage) {
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(300, 400);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(15);
        gridPane.setAlignment(Pos.CENTER);

        // initializes all JFX nodes
        Text title = new Text("Cryptr");

        Button encryptButton = new Button("Encrypt");
        Button decryptButton = new Button("Decrypt");

        Text selectEncryptText = new Text("select files to encrypt: ");
        Text selectDecryptText = new Text("select files to decrypt: ");
        Button selectButton = new Button("Select");
        FileChooser fileChooser = new FileChooser();

        CheckBox zipOption = new CheckBox("Compress files to .zip?");
        TextField zipLocation = new TextField();
        Button browseButton = new Button("Browse");
        zipOption.setAllowIndeterminate(false);
        zipLocation.setPromptText("Enter location to save .zip");

        ToggleGroup group = new ToggleGroup();
        RadioButton keyOption = new RadioButton("Use a generated key");
        RadioButton passwordOption = new RadioButton("Use a password");
        TextField keyField = new TextField();
        PasswordField passwordField = new PasswordField();
        keyOption.setToggleGroup(group);
        keyOption.setSelected(true);
        passwordOption.setToggleGroup(group);
        keyField.setPromptText("Enter location of key file");
        passwordField.setPromptText("Enter password");

        Button startOperationButton = new Button();
        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);

        // menuHandler switches the gui from selecting a mode to selecting files
        EventHandler<MouseEvent> menuHandler = (MouseEvent e) -> {
            gridPane.getChildren().remove(encryptButton);
            gridPane.getChildren().remove(decryptButton);

            this.encryptMode = e.getSource() == encryptButton;
            Text description = this.encryptMode ? selectEncryptText : selectDecryptText;
            gridPane.add(description, 0, 1);
            gridPane.add(selectButton, 1, 1);
        };

        encryptButton.setOnMouseClicked(menuHandler);
        decryptButton.setOnMouseClicked(menuHandler);

        // sets the select button to begin encrypting/decrypting the chosen files when clicked
        selectButton.setOnMouseClicked((MouseEvent e) -> {
            List<File> files = fileChooser.showOpenMultipleDialog(stage);

            if (files != null) {
                Text description = this.encryptMode ? selectEncryptText : selectDecryptText;
                gridPane.getChildren().remove(description);
                gridPane.getChildren().remove(selectButton);

                if (this.encryptMode) {
                    startOperationButton.setOnMouseClicked((MouseEvent f) -> {
                        gridPane.add(pb, 0, 5, 2, 1);
                        boolean compress = zipOption.isSelected();
                        String zipFile = zipLocation.getCharacters().toString();
                        boolean useKey = group.getSelectedToggle() == keyOption;
                        String password = passwordField.getCharacters().toString();
                        System.out.println(handleEncryption(files, compress, zipFile, useKey, password, pb));
                    });

                    startOperationButton.setText("Encrypt");
                    gridPane.add(zipOption, 0, 1);
                }
                else {
                    startOperationButton.setOnMouseClicked((MouseEvent f) -> {
                        gridPane.add(pb, 0, 5, 2, 1);
                        boolean useKey = group.getSelectedToggle() == keyOption;
                        String keyFile = keyField.getCharacters().toString();
                        String password = passwordField.getCharacters().toString();
                        System.out.println(handleDecryption(files, useKey, keyFile, password, pb));
                    });

                    startOperationButton.setText("Decrypt");
                    gridPane.add(keyField, 0, 3);
                    gridPane.add(browseButton, 1, 3);
                }

                gridPane.add(keyOption, 0, 2);
                gridPane.add(passwordOption, 1, 2);
                gridPane.add(startOperationButton, 2, 5);
            }
        });

        // sets the zipOption checkbox to toggle between getting a path for a zip file or not
        zipOption.setOnMouseClicked((MouseEvent e) -> {
            if (gridPane.getChildren().contains(zipLocation)) {
                gridPane.getChildren().remove(zipLocation);
                gridPane.getChildren().remove(browseButton);
            }
            else {
                gridPane.add(zipLocation, 1, 1);
                gridPane.add(browseButton, 2, 1);
            }
        });

        // opens a gui for selecting a file and sends the data to the appropriate TextField
        browseButton.setOnMouseClicked((MouseEvent e) -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                if (this.encryptMode) {
                    zipLocation.setText(file.getAbsolutePath());
                }
                else {
                    keyField.setText(file.getAbsolutePath());
                }
            }
        });

        keyOption.setOnMouseClicked((MouseEvent f) -> {
            if (gridPane.getChildren().contains(passwordField)) {
                gridPane.getChildren().remove(passwordField);
            }
            if (!this.encryptMode && !gridPane.getChildren().contains(keyField)) {
                gridPane.add(keyField, 0, 3);
                gridPane.add(browseButton, 1, 3);
            }
        });

        passwordOption.setOnMouseClicked((MouseEvent f) -> {
            if (gridPane.getChildren().contains(keyField)) {
                gridPane.getChildren().remove(keyField);
                gridPane.getChildren().remove(browseButton);
            }
            if (!gridPane.getChildren().contains(passwordField)) {
                gridPane.add(passwordField, 0, 3);
            }
        });

        String textStyle = "-fx-font: normal bold 21px 'SF Movie Poster', sansserif;";
        String buttonStyle = "-fx-background-color: crimson; -fx-text-fill: white; -fx-font-size: 16px";
        String smallButtonStyle = "-fx-background-color: crimson; -fx-text-fill: white; -fx-font-size: 14px";

        title.setStyle("-fx-font: normal bold 40px 'SF Movie Poster', sansserif;");
        selectEncryptText.setStyle(textStyle);
        selectDecryptText.setStyle(textStyle);
        selectButton.setStyle(buttonStyle);
        encryptButton.setStyle(buttonStyle);
        decryptButton.setStyle(buttonStyle);
        browseButton.setStyle(smallButtonStyle);
        startOperationButton.setStyle(smallButtonStyle);
        pb.setStyle("-fx-accent: crimson;");
        gridPane.setStyle("-fx-background-color: slategray;");

        gridPane.add(title, 0, 0);
        gridPane.add(encryptButton, 0, 1);
        gridPane.add(decryptButton, 1, 1);

        stage.setTitle("Cryptr");
        stage.centerOnScreen();
        stage.setMinWidth(500);
        stage.setMinHeight(500);
        stage.setScene(new Scene(gridPane));
        stage.getIcons().add(new Image(CryptrGUI.class.getResourceAsStream("cryptr_logo.png")));
        stage.show();
    }

    /**
     * Encrypts a list of files with a given configuration.
     *
     * @param files - list of files to encrypt
     * @param compress - whether or not to compress the files to a zip
     * @param zipFile - the name of the zip file to create if compress is true
     * @param useKey - whether or not to use a key for encryption
     * @param password - the password to use for encryption if useKey is false
     * @param pb - progress bar to set accordingly to progress on encrypting files
     * @return -2 on other failure
     *         -1 if JRE does not support essential operations
     *         0 on success
     *         else returns the index of the file which failed to encrypt
     */
    private static int handleEncryption(List<File> files, boolean compress, String zipFile,
            boolean useKey, String password, ProgressBar pb) {
        String[] filePaths = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            filePaths[i] = files.get(i).getAbsolutePath();
        }

        if (compress) {
            int response = CryptrCompressor.compress(filePaths, zipFile);
            pb.setProgress(0.5);
            if (response != -1) {
                return response;
            }

            if (useKey) {
                response = CryptrCipher.encryptWithKey(zipFile, zipFile, zipFile + ".KEY");
            }
            else {
                response = CryptrCipher.cipherWithPassword(zipFile, zipFile, password,
                        CryptrCipher.Mode.ENCRYPT);
            }

            pb.setProgress(1);
            if (response > 0) {
                return -2;
            }
            return response;
        }

        for (int i = 0; i < filePaths.length; i++, pb.setProgress(i * 1.0 / filePaths.length)) {
            int response;
            if (useKey) {
                response = CryptrCipher.encryptWithKey(filePaths[i], filePaths[i], filePaths[i] + ".KEY");
            }
            else {
                response = CryptrCipher.cipherWithPassword(filePaths[i], filePaths[i], password,
                        CryptrCipher.Mode.ENCRYPT);
            }
            if (response > 0) {
                return i;
            }
            if (response < 0) {
                return response;
            }
        }

        return 0;
    }

    /**
     * Decrypts a list of files with a given configuration.
     *
     * @param files - list of files to decrypt
     * @param useKey - whether or not to use a key for decryption
     * @param keyFile - path to key file to use for decryption if useKey is true
     * @param password - the password to use for decryption if useKey is false
     * @param pb - progress bar to set accordingly to progress on decrypting files
     * @return -2 on other failure
     *         -1 if JRE does not support essential operations
     *         0 on success
     *         else returns the index of the file which failed to decrypt
     */
    private static int handleDecryption(List<File> files, boolean useKey, String keyFile,
            String password, ProgressBar pb) {
        String[] filePaths = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            filePaths[i] = files.get(i).getAbsolutePath();
        }

        for (int i = 0; i < filePaths.length; i++, pb.setProgress(i * 1.0 / filePaths.length)) {
            int response;
            if (useKey) {
                response = CryptrCipher.decryptWithKey(filePaths[i], filePaths[i], keyFile);
            }
            else {
                response = CryptrCipher.cipherWithPassword(filePaths[i], filePaths[i], password,
                        CryptrCipher.Mode.DECRYPT);
            }
            if (response > 0) {
                return i;
            }
            if (response < 0) {
                return response;
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
