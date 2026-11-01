package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "MainTeleOp")
public class MainTeleOp extends OpMode {
    MecanumDT drive = new MecanumDT();
    IntakeAndMidtake inAndMid = new IntakeAndMidtake();
    Outtake outtake = new Outtake();
    ElapsedTime timer = new ElapsedTime();
    double forward, strafe, rotate;

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
            inAndMid.runIntakeMidtake(1.0,1.0,0.33);
        }
        else if (gamepad2.a){
            inAndMid.runIntakeMidtake(-1.0,-1.0,-0.33);
        }
        else {
            inAndMid.runIntakeMidtake(0,0,0);
        }
        if (gamepad2.right_trigger_pressed){
            outtake.shoot(1);
            }
        else {
            outtake.shoot(0);
            }
        if (gamepad2.left_trigger_pressed) {
            outtake.flick(0.75);

        }
        else {
            outtake.flick(0.0);
        }

    }
    }
