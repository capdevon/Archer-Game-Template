# Archer-Game-Template
A Third Person Shooter demo made with jMonkeyEngine v3.3.2-stable. Build with gradle.

The demo contains:

* Physics with Minie (https://stephengold.github.io/Minie/minie/overview.html)
* Physics raycast to detect hit collision
* Animations (with gltf2 animations, file .blend included): 
    * "Idle", "Running", "Running_2", "Aim_Idle", "Aim_Overdraw", "Aim_Recoil", "Draw_Arrow", "Water_Idle", "Water_Moving", "Swimming"
* Third Person Camera with collision detection
* Bow with two types of ammo and effect
* Dynamic update of camera FOV when aiming
* Keyboard/Mouse and Joystick support
* Sounds
* Particles
* Postprocessing
* ... many many useful functions! Stay tuned for the next addon.

# Keyboard Commands:
(see file [GInputAppState](https://github.com/capdevon/Archer-Game-Template/blob/main/src/main/java/com/capdevon/input/GInputAppState.java) for all configurations, joystick included)
- MOVE_FORWARD		(W)
- MOVE_BACKWARD	(S)
- MOVE_LEFT			(A)
- MOVE_RIGHT		(D)
- AIMING			   (E)
- SWITCH_WEAPON	(R)
- FIRE				(MBL - Mouse Button Left)
- RUNNING			(Hold down the space key while moving)
- CAMERA			   (Use the mouse to orient the camera)

# New add-on: Swimming System
* add Trigger System based on Unity Collider

Resource Used:

- Code
    - [jMonkeyEngine](https://jmonkeyengine.org/)
    - [Minie](https://stephengold.github.io/Minie/minie/overview.html)
    
- Assets
    - [Mixamo](https://www.mixamo.com/)
    - [Blender](https://www.blender.org/download/)

# Swimmming System
![Screenshot](media/swim/image-1.png)
------
![Screenshot](media/swim/image-2.png)
------
![Screenshot](media/swim/image-3.png)
------
![Screenshot](media/swim/image-4.png)

# Archer
![Screenshot](media/image2.jpg)
------
![Screenshot](media/image3.jpg)
------
![Screenshot](media/image4.jpg)
