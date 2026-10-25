package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "MainOpMode")
public class MainOpMode extends OpMode {
    MecanumDT drive = new MecanumDT();
    IntakeAndMidtake inAndMid = new IntakeAndMidtake();

    double forward, strafe, rotate;

    public void init(){
        drive.initMoveMotor(hardwareMap);
        inAndMid.initIntakeMidtakeMotor(hardwareMap);
    }

    public void loop(){
        forward = gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.driveFieldRelative(forward, strafe, rotate);
        if (gamepad2.b){
            inAndMid.runIntakeMidtake(1.0,1.0);
        }
        else if (gamepad2.a){
            inAndMid.runIntakeMidtake(-1.0,-1.0);
        }
        else {
            inAndMid.runIntakeMidtake(0,0);
        }
    }
}
