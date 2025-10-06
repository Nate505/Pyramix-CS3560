# Pyramix-CS3560

## Team Members
- Nathaniel Arifin (016861663)

## How To Run
1. Clone Repository: 'git clone https://github.com/Nate505/Pyramix-CS3560'
2. Open in IntelliJ/Eclipse
3. Configure VM options: '--module-path ["JavaFX Lib folder path"] --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics'
4. Navigate to Pyramix/src/main/java
6. Run Main.java 

## Features Implemented
- Feature 1: 2D Net Visualization Just like "https://rubiks-cube-solver.com/pyraminx/"
- Feature 2: Undo/Redo Buttons
- Feature 3: Save/load from a Json File
- Feature 4: Tip Only move

## Controls
- Buttons:
    - Reset (Solved): Moved everything back together
    - Scramble x20: Apply 20 random moves
    - Undo: Undo Moves
    - Redo: Redo Moves
    - Save: Save to a json file
    - Load: Load from the json file
    - R R': Move right side of pyraminx
    - L L': Move Left side of Pyraminx
    - U U': Move Upper side of Pyraminx
    - B B': Move the back side of Pyraminx
    - Move Mode:
        - Normal Mode: Moves tip, center piece, and 2 of the edges
        - Tip Only Mode: Only Moves Tip
    - Algorithm: Enter in a String of moves, then press Run Sequence to run it

## Video Demo Link
https://drive.google.com/file/d/1Kl-nqVJ1JnqSzXsm4dXzX6gdvfvjIKqL/view?usp=sharing