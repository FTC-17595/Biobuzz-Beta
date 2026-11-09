package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


@Autonomous
public class MainAuto extends OpMode {
    MecanumDT drive = new MecanumDT();

    public void init(){
        drive.initMoveMotor(hardwareMap);
    }
    public void loop() {
            drive.driveFieldRelative(0.2, 0, 0);
    }
}
