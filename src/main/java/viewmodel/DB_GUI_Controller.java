package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import model.Major;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();

    @FXML
    ProgressBar progressBar;

    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<Major> majorComboBox;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button addButton;
    @FXML
    private MenuItem editMenuItem;
    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private TextField statusField;

    @FXML
    private MenuItem newItem;
    @FXML
    private MenuItem ChangePic;
    @FXML
    private MenuItem logOut;
    @FXML
    private MenuItem ClearItem;
    @FXML
    private MenuItem CopyItem;

    //Regex patterns
    private final Pattern namePattern = Pattern.compile("^[A-Za-z]{2,50}$");
    private final Pattern departmentPattern = Pattern.compile("^[A-Za-z ]{2,100}$");
    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final Pattern imageURLPattern = Pattern.compile(".*"); //"^https?://.*\\.(png|jpg|jpeg)$"

    //Guest Mode Property
    private final BooleanProperty guestMode = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        editMenuItem.setDisable(true);
        deleteMenuItem.setDisable(true);

        //Data validation bindings from the regex
        BooleanBinding firstNameValid = Bindings.createBooleanBinding(() ->
                        namePattern.matcher(first_name.getText()).matches(),
                first_name.textProperty());

        BooleanBinding lastNameValid = Bindings.createBooleanBinding(() ->
                        namePattern.matcher(last_name.getText()).matches(),
                last_name.textProperty());

        BooleanBinding departmentValid = Bindings.createBooleanBinding(() ->
                        departmentPattern.matcher(department.getText()).matches(),
                department.textProperty());

        BooleanBinding emailValid = Bindings.createBooleanBinding(() ->
                        emailPattern.matcher(email.getText()).matches(),
                email.textProperty());

        BooleanBinding imageURLValid = Bindings.createBooleanBinding(() ->
                        imageURLPattern.matcher(imageURL.getText()).matches(),
                imageURL.textProperty());

        BooleanBinding majorValid = Bindings.createBooleanBinding(() ->
                        majorComboBox.getValue() != null,
                majorComboBox.valueProperty());

        BooleanBinding formValid = firstNameValid
                .and(lastNameValid)
                .and(departmentValid)
                .and(majorValid)
                .and(emailValid)
                .and(imageURLValid)
                .and(Bindings.not(guestMode));

        addButton.disableProperty().bind(formValid.not());

        editButton.disableProperty().bind(
                guestMode.or(tv.getSelectionModel().selectedItemProperty().isNull())
        );
        deleteButton.disableProperty().bind(
                guestMode.or(tv.getSelectionModel().selectedItemProperty().isNull())
        );
        editMenuItem.disableProperty().bind(
                guestMode.or(tv.getSelectionModel().selectedItemProperty().isNull())
        );
        deleteMenuItem.disableProperty().bind(
                guestMode.or(tv.getSelectionModel().selectedItemProperty().isNull())
        );


        editMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        deleteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        ChangePic.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        logOut.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        ClearItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        CopyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
    }

    @FXML
    protected void addNewRecord() {
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.insertUser(p);
        cnUtil.retrieveId(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
        statusField.setText("Record added successfully"); //Updating the status field
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        email.setText("");
        imageURL.setText("");
        majorComboBox.setValue(null);
        statusField.setText(""); //Clear the status message
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorComboBox.getValue().toString(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        statusField.setText("Record updated successfully");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }

        //Delete confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this record?");
        alert.setContentText("First Name: " + p.getFirstName() + "\nLast Name: " + p.getLastName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int index = data.indexOf(p);
            cnUtil.deleteRecord(p);
            data.remove(index);
            tv.getSelectionModel().clearSelection();
            statusField.setText("Record deleted successfully");
        } else {
            statusField.setText("Delete operation cancelled");
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));

            //Set status message to indicate upload has started
            statusField.setText("Uploading image...");

            Task<Void> uploadTask = createUploadTask(file, progressBar);

            //When the task succeeds, update the message
            uploadTask.setOnSucceeded(e -> {
                statusField.setText("Image uploaded successfully.");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
            });

            //Handle failure
            uploadTask.setOnFailed(e -> {
                statusField.setText("Image upload failed.");
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                uploadTask.getException().printStackTrace();
            });

            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            try {
                majorComboBox.setValue(Major.valueOf(p.getMajor()));
            } catch (IllegalArgumentException e) {
                majorComboBox.setValue(null);
            }

            if (p.getImageURL() != null && !p.getImageURL().isEmpty()) {
                try {
                    img_view.setImage(new Image(p.getImageURL()));
                } catch (Exception e) {
                    img_view.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/profile.png")).toExternalForm()));
                }
            } else {
                //This sets to default image if the URL is blank
                img_view.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/profile.png")).toExternalForm()));
            }

        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options = FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        //int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        //updateProgress(progress, 100);
                        updateProgress(uploadedBytes, fileSize);
                        //System.out.println("Uploaded bytes: " + uploadedBytes + "/" + fileSize); //Debug
                    }
                }

                return null;
            }
        };
    }

    @FXML
    protected void importCSV(ActionEvent event) {
        System.out.println("Import CSV click"); //Debug

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            statusField.setText("Importing CSV...");
            Task<Void> importTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                        String line;
                        int totalLines = (int) Files.lines(selectedFile.toPath()).count();
                        int currentLine = 0;

                        line = br.readLine();
                        if (line != null && line.startsWith("id,")) { //Check for headers, if none skip
                        } else {
                            processLine(line);
                            currentLine++;
                        }

                        while ((line = br.readLine()) != null) {
                            processLine(line);
                            currentLine++;
                        }
                    }
                    return null;
                }

                private void processLine(String line) {
                    if (line == null || line.trim().isEmpty()) return;
                    String[] tokens = line.split(",", -1);
                    if (tokens.length < 7) {
                        Platform.runLater(() -> statusField.setText("Invalid line format: " + line));
                        return;
                    }
                    try {
                        //Parsing the CSV
                        String firstName = tokens[1].trim();
                        String lastName = tokens[2].trim();
                        String department = tokens[3].trim();
                        String major = tokens[4].trim();
                        String email = tokens[5].trim();
                        String imageURL = tokens[6].trim();

                        //Check if email already exists
                        if (cnUtil.emailExists(email)) {
                            Platform.runLater(() -> statusField.setText("Duplicate email found: " + email));
                            System.out.println("Duplicate email found: " + email); //Debug
                            return; //Skip inserting duplicate
                        }

                        //Creating a new Person object without setting the ID
                        Person p = new Person(firstName, lastName, department, major, email, imageURL);

                        //Insert into DB
                        cnUtil.insertUser(p);

                        p.setId(cnUtil.retrieveId(p));

                        //Updating the UI
                        Platform.runLater(() -> {
                            data.add(p);
                            statusField.setText("Imported record: " + firstName + " " + lastName);
                            System.out.println("Imported record: " + firstName + " " + lastName); //Debug
                        });
                    } catch (NumberFormatException e) {
                        Platform.runLater(() -> statusField.setText("Invalid format in line: " + line));
                        System.out.println("Invalid format in line: " + line); //Debug
                    } catch (Exception e) {
                        Platform.runLater(() -> statusField.setText("Error importing line: " + line));
                        e.printStackTrace();
                    }
                }
            };

            //Run import task in a new thread
            new Thread(importTask).start();
        } else {
            statusField.setText("CSV import canceled.");
            System.out.println("CSV import canceled."); //Debug
        }
    }

    @FXML
    protected void exportCSV(ActionEvent event) {
        System.out.println("Export CSV click"); //Debug

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            statusField.setText("Exporting CSV...");
            Task<Void> exportTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile))) {
                        bw.write("id,firstName,lastName,department,major,email,imageURL");
                        bw.newLine();

                        for (Person p : data) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(p.getId()).append(",");
                            sb.append(p.getFirstName()).append(",");
                            sb.append(p.getLastName()).append(",");
                            sb.append(p.getDepartment()).append(",");
                            sb.append(p.getMajor()).append(",");
                            sb.append(p.getEmail()).append(",");
                            sb.append(p.getImageURL());

                            bw.write(sb.toString());
                            bw.newLine();

                            Platform.runLater(() -> {
                                statusField.setText("Exported record: " + p.getFirstName() + " " + p.getLastName());
                                System.out.println("Exported record: " + p.getFirstName() + " " + p.getLastName()); //Debug
                            });
                        }
                    }
                    return null;
                }
            };

            //Run export task in a new thread
            new Thread(exportTask).start();
        } else {
            statusField.setText("CSV export canceled.");
            System.out.println("CSV export canceled."); //Debug
        }
    }

    @FXML
    public void setGuestMode(boolean isGuest) {
        guestMode.set(isGuest);
    }
}
