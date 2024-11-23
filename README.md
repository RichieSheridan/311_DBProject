# Inventory Management System

## Project Overview

The **Inventory Management System** is a desktop application developed using JavaFX. It allows users to efficiently manage inventory records by providing functionalities to add, edit, delete, import, and export inventory items. The system also supports image management for each inventory item, ensuring a user-friendly and visually consistent interface.

## Features

- **Add New Records:** Easily add new inventory items with details such as first name, last name, department, major, email, and profile picture.
- **Edit Records:** Modify existing inventory items to keep your data up to date.
- **Delete Records:** Remove unwanted or obsolete inventory items.
- **Import CSV:** Import inventory data from CSV files, ensuring quick data entry.
- **Export CSV:** Export your inventory data to CSV files for reporting or backup purposes.
- **Image Management:** Upload and display profile pictures for each inventory item.  
- **Themes:** Switch between light and dark themes to suit your preference or working environment.
- **Guest Mode:** Restrict certain functionalities for guest users to maintain data integrity.
- **Data Validation:** Ensure data accuracy with regex validation.

## Technologies Used

- **JavaFX:** For building the graphical user interface.
- **FXML:** For defining the UI layout.
- **CSS:** For styling the application.
- **Azure** For storage.

## Usage

1. **Launching the Application:**
    - Upon launching, the application displays a table of profiles with details and profile pictures.

2. **Adding a New Record:**
    - Fill in the text fields for first name, last name, department, major, and email.
    - Upload a profile picture by clicking the image or adding a path to the textbox.
    - Click the Add button to save the new record.

3. **Editing a Record:**
    - Select an existing record from the table.
    - Modify the desired fields in the form.
    - Click the Edit button to update the record.

4. **Deleting a Record:**
    - Select a record from the table.
    - Click the Delete button.
    - Confirm the deletion.

5. **Importing CSV:**
    - Navigate to `File > Import CSV`.
    - Select a CSV file with the appropriate format.
    - The application will process and add valid records to the table.

6. **Exporting CSV:**
    - Navigate to `File > Export CSV`.
    - Choose a destination and filename.
    - The application will export all current records to the specified CSV file.

7. **Changing Themes:**
    - Switch between light and dark themes via the `View` menu.

8. **Keyboard Shortcuts:**
    - Utilize keyboard shortcuts for faster navigation and actions.

## Keyboard Shortcuts

- **New Record:** `Ctrl + N`
- **Edit Record:** `Ctrl + E`
- **Delete Record:** `Ctrl + D`
- **Change Picture:** `Ctrl + P`
- **Log Out:** `Ctrl + L`

## Themes

The application supports two themes:

1. **Light Theme:**
    - Accessible via `View > Light Theme`.
    - Offers a bright and clear interface.

2. **Dark Theme:**
    - Accessible via `View > Dark Theme`.
    - Provides a subdued interface ideal for lowlight settings.

## Import/Export CSV

- **Importing:**
    - Ensure your CSV file follows the format:
      ```
      id,firstName,lastName,department,major,email,imageURL
      ```
    - The application will skip duplicate emails and invalid lines, providing status updates accordingly.

- **Exporting:**
    - The exported CSV will include all current records in the same format, facilitating easy data sharing and backup.

## Guest Mode

- **Activation:**
    - Guest Mode can be enabled on the login screen.

- **Restrictions:**
    - In Guest Mode, buttons for editing and deleting records are disabled to prevent unauthorized modifications.

