package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "WTeleOp")
public class outaketest extends LinearOpMode {

    private DcMotor outTakeMotor; // motor define
    private Servo pollenFlicker;
    @Override
    public void runOpMode() throws InterruptedException {

        outTakeMotor = hardwareMap.dcMotor.get("rightFrontMotor");

        waitForStart();
        if(isStopRequested()) return;

        while (opModeIsActive()) {
            if (gamepad1.a || gamepad2.a) {
                outTakeMotor.setPower(-1);
                pollenFlicker.setPosition(-0.75);
                wait(500);
                pollenFlicker.setPosition(0);
            }
            else if (gamepad1.b || gamepad2.b) {
                outTakeMotor.setPower(1);
                pollenFlicker.setPosition(0.75);
                wait(500);
                pollenFlicker.setPosition(0);
            }
            else{
                outTakeMotor.setPower(0);
            }

        }
    }
}
