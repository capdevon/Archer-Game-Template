/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.capdevon.engine.Animation3;
import com.jme3.animation.LoopMode;

public interface IAnimation {
    
    final String MODEL              = "Models/gltf2/Archer/archer.j3o";
    final String RIGHT_HAND         = "Armature_mixamorig:RightHandMiddle1";
    final Animation3 Idle           = new Animation3("Idle", LoopMode.Loop, .2f);
    final Animation3 Running        = new Animation3("Running", LoopMode.Loop);
    final Animation3 Running_2      = new Animation3("Running_2", LoopMode.Loop);
    final Animation3 Aim_Idle       = new Animation3("Aim_Idle", LoopMode.DontLoop);
    final Animation3 Aim_Overdraw   = new Animation3("Aim_Overdraw", LoopMode.DontLoop);
    final Animation3 Aim_Recoil     = new Animation3("Aim_Recoil", LoopMode.DontLoop);
    final Animation3 Draw_Arrow     = new Animation3("Draw_Arrow", LoopMode.DontLoop);
}
