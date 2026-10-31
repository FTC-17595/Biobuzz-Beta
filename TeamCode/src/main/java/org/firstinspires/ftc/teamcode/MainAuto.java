package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous
public class MainAuto extends OpMode {
    MecanumDT drive = new MecanumDT();
    ElapsedTime timer = new ElapsedTime();
    public void init(){
        drive.initMoveMotor(hardwareMap);
    }
    public void loop(){
        timer.reset();
        if (timer.seconds() < 20){
            drive.driveFieldRelative(1,0,0);
        }
        else {
            drive.driveFieldRelative(0,0,0);
        }
    }
}
