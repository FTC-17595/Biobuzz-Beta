package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class BlockerServo {
    public CRServo blockerServo;

    public void initBlockerServo(HardwareMap HwMap){
        blockerServo = HwMap.get(CRServo.class,"blockerServo");
    }
    public void blockedByJames(double servoPower){
        blockerServo.setPower(servoPower);
    }
}
