package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

// Put the robot on a box so the wheels are off the ground.
// Hold a button: that ONE wheel spins at 30% "forward".
// X = frontLeft, Y = frontRight, A = backLeft, B = backRight
// Every wheel should spin so it would push the robot FORWARD.
@TeleOp(name = "driveTest")
public class DriveTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        DcMotor fl = hardwareMap.dcMotor.get("frontLeftMotor");
        DcMotor bl = hardwareMap.dcMotor.get("backLeftMotor");
        DcMotor fr = hardwareMap.dcMotor.get("frontRightMotor");
        DcMotor br = hardwareMap.dcMotor.get("backRightMotor");

        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);

        IMU imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.UP)));
        imu.resetYaw();

        waitForStart();

        while (opModeIsActive()) {
            fl.setPower(gamepad1.x ? 0.3 : 0);
            fr.setPower(gamepad1.y ? 0.3 : 0);
            bl.setPower(gamepad1.a ? 0.3 : 0);
            br.setPower(gamepad1.b ? 0.3 : 0);

            if (gamepad1.options) imu.resetYaw();

            // Rotate robot by hand: CCW should make Yaw go UP.
            telemetry.addData("Yaw", "%.1f", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.addData("Pitch", "%.1f", imu.getRobotYawPitchRollAngles().getPitch(AngleUnit.DEGREES));
            telemetry.addData("Roll", "%.1f", imu.getRobotYawPitchRollAngles().getRoll(AngleUnit.DEGREES));
            telemetry.update();
        }
    }
}