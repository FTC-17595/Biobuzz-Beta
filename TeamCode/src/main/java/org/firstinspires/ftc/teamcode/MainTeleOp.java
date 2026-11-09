package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "MainTeleOp")
public class MainTeleOp extends OpMode {
    MecanumDT drive = new MecanumDT();
    IntakeAndMidtake inAndMid = new IntakeAndMidtake();
    Outtake outtake = new Outtake();
    BlockerServo blocker = new BlockerServo();

    double forward, strafe, rotate;

    public void init(){
        drive.initMoveMotor(hardwareMap);
        inAndMid.initIntakeMidtakeMotor(hardwareMap);
        outtake.initShooterMotorAndFlickerServo(hardwareMap);
        blocker.initBlockerServo(hardwareMap);

    }

    public void loop() {
        forward = -gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        rotate = gamepad1.right_stick_x;

        drive.driveFieldRelative(forward, strafe, rotate);

        if (gamepad2.b) {
            inAndMid.runIntakeMidtake(1.0, 0.33);
            inAndMid.secondMid(1.0);
        } else if (gamepad2.a) {
            inAndMid.runIntakeMidtake(-1.0, -0.33);
            inAndMid.secondMid(-1);
        } else {
            inAndMid.runIntakeMidtake(0, 0);
            inAndMid.secondMid(0);
        }
        if (gamepad2.right_trigger_pressed) {
            outtake.shoot(1200);
        } else {
            outtake.shoot(0);
        }
        if (gamepad2.left_trigger_pressed) {
            outtake.flick(0.7);
        }
        else {
            outtake.flick(0);
        }
        if (gamepad2.y){
            inAndMid.secondMid(1);
        }
        if (gamepad2.x){
            inAndMid.secondMid(-1);
        }


        if (gamepad1.right_trigger_pressed) {
            blocker.blockedByJames(-0.95);
        }
        else {
            blocker.blockedByJames(0);
        }

        double velocity = outtake.shooterMotorPollen.getVelocity();

        telemetry.addData("Shooter Velocity", outtake.shooterMotorPollen.getVelocity());
        if (velocity <= 1325 || velocity >= 1100){
            telemetry.addLine("True");
        }
        else {
            telemetry.addLine("False");
        }
        telemetry.update();

        if(gamepad1.left_bumper){
            drive.imu.resetYaw();
        }
        outtake.motorVelocity();
    }
}