package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

@TeleOp(name = "FieldCentricMecanumDT", group = "Main")
public class FieldCentricMecanumDT extends LinearOpMode {

    // ================= TUNING / SETUP CONSTANTS =================

    // Drive motor directions. If the robot drives backwards, swap these two.
    private static final DcMotorSimple.Direction LEFT_DIRECTION  = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction RIGHT_DIRECTION = DcMotorSimple.Direction.FORWARD;

    // Control Hub orientation on the robot (logo facing right, USB facing up).
    private static final RevHubOrientationOnRobot.LogoFacingDirection LOGO_DIRECTION =
            RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
    private static final RevHubOrientationOnRobot.UsbFacingDirection USB_DIRECTION =
            RevHubOrientationOnRobot.UsbFacingDirection.UP;

    private static final double STICK_DEADZONE    = 0.05;  // ignore stick drift
    private static final double STRAFE_CORRECTION = 1.1;   // counteracts imperfect strafing
    private static final double SLOW_MODE_SCALE   = 0.35;  // hold left bumper for precision
    private static final double TURN_SCALE        = 0.85;  // slightly tames rotation

    // Mechanism directions (flip sign if something spins the wrong way)
    private static final double INTAKE_FORWARD_POWER  =  1.0;
    private static final double INTAKE_REVERSE_POWER  = -1.0;
    private static final double MIDTAKE_FORWARD_POWER =  1.0;
    private static final double MIDTAKE_REVERSE_POWER = -1.0;

    // ============================================================

    private DcMotor frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor;
    private DcMotor intakeMotor, midtakeMotor;
    private IMU imu;

    @Override
    public void runOpMode() throws InterruptedException {

        // ---------- Hardware map ----------
        frontLeftMotor  = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        backLeftMotor   = hardwareMap.get(DcMotor.class, "backLeftMotor");
        frontRightMotor = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backRightMotor  = hardwareMap.get(DcMotor.class, "backRightMotor");
        intakeMotor     = hardwareMap.get(DcMotor.class, "intakeMotor");
        midtakeMotor    = hardwareMap.get(DcMotor.class, "midtakeMotor");

        // ---------- Drive motor setup (NO encoders needed) ----------
        frontLeftMotor.setDirection(LEFT_DIRECTION);
        backLeftMotor.setDirection(LEFT_DIRECTION);
        frontRightMotor.setDirection(RIGHT_DIRECTION);
        backRightMotor.setDirection(RIGHT_DIRECTION);

        DcMotor[] driveMotors = {frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor};
        for (DcMotor m : driveMotors) {
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        // ---------- Mechanism motor setup (also no encoders) ----------
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        midtakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        midtakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // ---------- IMU ----------
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(LOGO_DIRECTION, USB_DIRECTION)));
        imu.resetYaw();

        telemetry.addLine("Ready. Face the robot AWAY from you, then press START.");
        telemetry.addLine("Driver (gamepad1): sticks drive, OPTIONS resets heading, L-bumper = slow mode");
        telemetry.addLine("Operator (gamepad2): A intake, B intake reverse, X midtake, Y midtake reverse");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) return;

        while (opModeIsActive()) {

            // =====================================================
            // DRIVER - gamepad1 : field-centric mecanum
            // =====================================================
            if (gamepad1.options) {
                imu.resetYaw(); // re-zero heading (point robot away from you first)
            }

            double y  = shape(-gamepad1.left_stick_y);  // forward is positive
            double x  = shape(gamepad1.left_stick_x);
            double rx = shape(gamepad1.right_stick_x) * TURN_SCALE;

            double scale = gamepad1.left_bumper ? SLOW_MODE_SCALE : 1.0;
            y *= scale;
            x *= scale;
            rx *= scale;

            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            // Rotate the joystick vector by the negative of the robot heading
            double rotX = x * Math.cos(-botHeading) - y * Math.sin(-botHeading);
            double rotY = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);

            rotX *= STRAFE_CORRECTION;

            // Keep power ratios intact if any value would exceed 1.0
            double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rx), 1.0);
            double frontLeftPower  = (rotY + rotX + rx) / denominator;
            double backLeftPower   = (rotY - rotX + rx) / denominator;
            double frontRightPower = (rotY - rotX - rx) / denominator;
            double backRightPower  = (rotY + rotX - rx) / denominator;

            frontLeftMotor.setPower(frontLeftPower);
            backLeftMotor.setPower(backLeftPower);
            frontRightMotor.setPower(frontRightPower);
            backRightMotor.setPower(backRightPower);

            // =====================================================
            // OPERATOR - gamepad2 : intake / midtake
            // =====================================================

            // Intake: hold A = full speed, hold B = reverse
            if (gamepad2.a) {
                intakeMotor.setPower(INTAKE_FORWARD_POWER);
            } else if (gamepad2.b) {
                intakeMotor.setPower(INTAKE_REVERSE_POWER);
            } else {
                intakeMotor.setPower(0);
            }

            // Midtake: hold X = normal, hold Y = reverse
            if (gamepad2.x) {
                midtakeMotor.setPower(MIDTAKE_FORWARD_POWER);
            } else if (gamepad2.y) {
                midtakeMotor.setPower(MIDTAKE_REVERSE_POWER);
            } else {
                midtakeMotor.setPower(0);
            }

            // ---------- Telemetry ----------
            telemetry.addData("Heading (deg)", "%.1f", Math.toDegrees(botHeading));
            telemetry.addData("Slow mode", gamepad1.left_bumper);
            telemetry.addData("Drive FL/BL/FR/BR", "%.2f %.2f %.2f %.2f",
                    frontLeftPower, backLeftPower, frontRightPower, backRightPower);
            telemetry.addData("Intake", intakeMotor.getPower());
            telemetry.addData("Midtake", midtakeMotor.getPower());
            telemetry.update();
        }

        // Safety stop
        for (DcMotor m : driveMotors) m.setPower(0);
        intakeMotor.setPower(0);
        midtakeMotor.setPower(0);
    }

    /**
     * Deadzone + smooth response curve. Gives fine control near the center of the
     * stick while still reaching full power at the edge.
     */
    private double shape(double input) {
        if (Math.abs(input) < STICK_DEADZONE) return 0;
        double scaled = (Math.abs(input) - STICK_DEADZONE) / (1.0 - STICK_DEADZONE);
        double curved = 0.4 * scaled + 0.6 * scaled * scaled * scaled;
        return Math.signum(input) * curved;
    }
}
