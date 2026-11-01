package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
public class ServoBlocker extends LinearOpMode {
    private Servo armServo, armServoSnd;

    @Override
    public void runOpMode() {

        initHardware();

        telemetry.addData("Status", "Initialized — press Play");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleArm();
            telemetry.update();
        }
    }


    private void initHardware() {
        // Servo for the arm mechanism
        armServo = hardwareMap.get(Servo.class, "armServo");
    }

    private void handleArm() {
        if (gamepad1.a) {
            armServo.setPosition(0.45);
            armServoSnd.setPosition(0.45);// Arm down
        }
        else if (gamepad1.b) {
            armServo.setPosition(0.0);
            armServoSnd.setPosition(0.0);// Arm up
        }

        telemetry.addData("Arm", "position=%.2f", armServo.getPosition());
        telemetry.addData("Arm#2", "position=%.2f", armServoSnd.getPosition());
    }

}
