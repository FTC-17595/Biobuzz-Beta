package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TeleopOpMode extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MecanumDriveSubsystem drive = new MecanumDriveSubsystem(hardwareMap);
        IntakeSubsystem intake = new IntakeSubsystem(hardwareMap);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            if (gamepad1.options) {
                drive.resetYaw();
            }

            if (gamepad1.a || gamepad2.a) {
                intake.setPower(-1);
            } else if (gamepad1.b || gamepad2.b) {
                intake.setPower(1);
            } else {
                intake.setPower(0);
            }

            drive.driveFieldCentric(y, x, rx);
        }
    }
}
