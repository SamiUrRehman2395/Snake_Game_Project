# Snake_Game_Project
A JavaFX-based Snake Game built using custom data structures like linked lists, featuring difficulty modes, bonus items, and a dynamic UI.
Introduction & Features of the Project

	Introduction
•	Project Title: Desert Slither
•	Technology Used: Java (JavaFX), Data Structures, Object-Oriented Programming.
•	Theme: A modern version of the classic snake game with enhanced visuals and gameplay elements.
•	Project Type: Desktop Game Application
•	Desert Slither is a Data Structures and Algorithms (DSA) based implementation of the classic Snake game using JavaFX. While the game offers an engaging visual experience and smooth gameplay, its core strength lies in the underlying DSA concepts. This project serves as a practical demonstration of applying linked lists, game loops, event handling, and algorithmic state transitions in a real-time environment.
•	At the heart of the game is a custom-built singly linked list that represents the snake’s body. Each node in the list corresponds to a segment of the snake (head, body, tail), dynamically growing as the snake consumes food. This structure allows efficient manipulation of the snake's size and movement, making the game an excellent example of how core data structures are applied beyond theory.
•	The project also integrates additional algorithms to manage movement direction, collision detection, random food generation, and scene transitions, showcasing how logical thinking and structured algorithm design are essential in game development.


	Key Features

1.	Splash Screen with Logo Animation
A professionally animated splash screen with the game's logo that fades in and out before transitioning to the login screen.

2.	User Login & Sign-Up System
Users are required to either log in or sign up before accessing the game. This creates a personalized experience and is a potential foundation for storing individual scores.
3.	Multiple Difficulty Modes
o	Easy: Slower snake speed, game ends only if the snake bites itself.
o	Medium: Faster speed, self-collision ends the game.
o	Hard: Fast speed, game ends on both self-collision and border collision, making it highly challenging.

4.	Snake Representation using Singly Linked List
The snake is modeled as a SnakeLinkedList, where each node holds coordinates of the segment. Efficient node insertion (when eating) and deletion (when moving) replicate snake growth/shrink behavior.

5.	Game Loop Algorithm  
The game runs on a timed loop using Timeline to simulate real-time motion. Each cycle updates the snake’s position, checks for collisions, and renders graphics accordingly.

6.	Collision Detection Algorithms 
Self-collision detection is performed by traversing the linked list and comparing head coordinates with body segments. 
In Hard Mode, additional boundary collision logic is used.

7.	Randomized Food Generation and Bonus Items
o	Randomized coordinates are generated for placing food and bonus items. Ensures that food does not spawn inside the snake's body (collision avoidance).
o	Regular food increases score by 1.
o	Special Diamond Bonus Food increases score by 5, adding an exciting gameplay twist.

8.	Score Tracking & File Handling
The score is calculated algorithmically based on food type. A file-based score saving system is in place, with plans to add high-score leaderboard using file I/O.

