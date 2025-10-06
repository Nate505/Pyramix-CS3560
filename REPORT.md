# Project Report

## Design Decisions

###  Architecture
I separated the drawing of the pyraminx in PyraminxView class, while calling the drawFace method from the app, where it renders the rest of the JavaFX UI. The Model of the Pyraminx is in model/Pyraminx.java, that is where all the moves get taken in and processed
I did not create any abstractions nor interfaces, I added onto the hands on code that was provided
I chose JavaFX because we were already working with JavaFX from the hands on code

### Data Structure
I represent the faces as 2D arrays, where faces[0] is yellow, faces[1] is red, faces[2] is green, and faces[3] is blue. The 2D part represents the different face pieces, where the corresponding number represents the piece
        0
    1   6   2
3   7   4   8   5

### Algorithms
rCW(), lCW(), uCW(), bCW(), was added onto to make sure faces are rotating and colors are where they are supposed to be

### Challenges Faced
1. **Challenge 1** Drawing the Pyraminx
    - **Solution** I'm going to be honest, I didn't really understand how to use JavaFX at first, so I asked ChatGPT for a little help with Canvas drawing in JavaFX

2. **Challenge 2** "Rotating" The Pyraminx for the 2D Net Visualization
    - **Solution** I manually cycle the pieces over, this could be seen in rCW(), lCW(), uCW(), bCW(), where i manually set faces[#][#] = faces[#][#]

### What we learned
- JavaFX UI and Drawing and wiring
- OOP Concepts reinforced

## If I had more Time
- 3D Visualization on top of the 2D one