package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "MainTeleOp")
public class MainTeleOp extends OpMode {
    MecanumDT drive = new MecanumDT();
    IntakeAndMidtake inAndMid = new IntakeAndMidtake();
    Outtake outtake = new Outtake();

    double forward, strafe, rotate;

    //private void shootLoop(){
        //if (gamepad2.right_trigger_pressed){
            //outtake.shoot(1.0,1.0);
            //outtake.flickerLoop(0.75);
        //}
        //else {
            //outtake.shoot(0.0,0.0);
            //outtake.flickerLoop(0);
       //}
    //}

    public void init(){
        drive.initMoveMotor(hardwareMap);
        inAndMid.initIntakeMidtakeMotor(hardwareMap);
        outtake.initShooterMotorAndFlickerServo(hardwareMap);
    }

    public void loop(){
        forward = -gamepad1.left_stick_y;
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
        if (gamepad2.right_trigger_pressed){
            outtake.shoot(1.0);
            }
        else {
            outtake.shoot(0.0);
            }
    }

    }
