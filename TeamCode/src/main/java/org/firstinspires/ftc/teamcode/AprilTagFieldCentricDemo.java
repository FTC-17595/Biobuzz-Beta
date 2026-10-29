package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Field-centric version of {@link AprilTagDemo}.
 *
 * Manual driving is field-centric: pushing the left stick forward always moves the robot
 * away from the driver, regardless of which way the robot is facing.
 * See https://gm0.org/en/latest/docs/software/tutorials/mecanum-drive.html
 *
 * The AprilTag corrections are measured by the camera, so they are ROBOT-relative and are
 * applied WITHOUT the field-centric rotation:
 *  - Assist Mode 1 (right bumper): driver keeps field-centric translation, turn is auto-aimed.
 *  - Assist Mode 2 (A button): drive/strafe/turn all come from the tag (robot-centric).
 *
 * Press OPTIONS to re-zero the heading (point the robot away from you first).
 */
@TeleOp(name = "BioBuzz FC TeleOp Hive AutoAlign & Shoot", group = "BioBuzz")
public class AprilTagFieldCentricDemo extends LinearOpMode {

    // Set this to "RED" or "BLUE" to match your Alliance
    private static final String ALLIANCE_COLOR = "RED";

    // Target standoff distance (in inches) from camera lens to the Hive Cell opening
    private static final double DESIRED_DISTANCE = 50.0; //36.0;

    // Launcher RPM tuning constants (tune for your specific flywheel & motor!)
    private static final double TICKS_PER_REV    = 28.0;   // 28.0 for bare REV HD Hex / goBILDA 5202/5203 motor shaft
    private static final double BASE_SHOOTER_RPM = 2200.0; // Ideal RPM when shooting from DESIRED_DISTANCE (36 in)
    private static final double RPM_PER_INCH     = 35.0;   // Extra RPM added for each inch further than 36 in

    // Proportional drivetrain gains (start small and tune on your robot!)
    private static final double TURN_GAIN   = 0.15;  // Rotates robot so Bearing -> 0
    private static final double STRAFE_GAIN = 0.2; // Strafes robot so Yaw -> 0
    private static final double DRIVE_GAIN  = 0.1;  // Drives forward/back so Range -> DESIRED_DISTANCE

    // Maximum speeds during auto-alignment
    private static final double MAX_AUTO_SPEED  = 0.5;
    private static final double MAX_AUTO_STRAFE = 0.5;
    private static final double MAX_AUTO_TURN   = 0.4;

    public static final double MASTER_DRIVE_MULTIPLIER = 0.3; // reduce max motors speed after normalization

    // Counteracts imperfect mecanum strafing (gm0 recommends ~1.1, tune on your robot)
    private static final double STRAFE_CORRECTION = 1.1;

    // Control Hub orientation on the robot (must match how the hub is mounted!)
    private static final RevHubOrientationOnRobot.LogoFacingDirection LOGO_DIRECTION =
            RevHubOrientationOnRobot.LogoFacingDirection.RIGHT;
    private static final RevHubOrientationOnRobot.UsbFacingDirection USB_DIRECTION =
            RevHubOrientationOnRobot.UsbFacingDirection.UP;

    private DcMotor leftFront, rightFront, leftBack, rightBack;
//    private DcMotorEx shooterMotor;
    private IMU imu;
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {
        // 1. Initialize Mecanum Drivetrain Motors
        leftFront  = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        rightFront = hardwareMap.get(DcMotor.class, "frontRightMotor");
        leftBack   = hardwareMap.get(DcMotor.class, "backLeftMotor");
        rightBack  = hardwareMap.get(DcMotor.class, "backRightMotor");

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        rightBack.setDirection(DcMotorSimple.Direction.FORWARD);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // 2. Initialize IMU for field-centric heading
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(
                new RevHubOrientationOnRobot(LOGO_DIRECTION, USB_DIRECTION)));
        imu.resetYaw();

        // 3. Initialize Launcher Motor (DcMotorEx enables closed-loop velocity control)
//        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooterMotorPollen");
//        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // 4. Initialize VisionPortal & AprilTagProcessor for BIOBUZZ
        initAprilTag();

        telemetry.addData("Status", "Initialized. Face robot AWAY from you, then press START.");
        telemetry.addLine("OPTIONS = reset heading, RB = auto-aim, A = full auto-square");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Re-zero heading (point the robot away from the driver first)
            if (gamepad1.options) {
                imu.resetYaw();
            }

            double botHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

            // Manual joystick inputs (gamepad Y axis is negative when pushed forward)
            double y  = -gamepad1.left_stick_y; // field forward
            double x  =  gamepad1.left_stick_x; // field right
            double rx =  gamepad1.right_stick_x;

            // Rotate the field-relative joystick vector by -heading into the robot frame
            double drive  = x * Math.sin(-botHeading) + y * Math.cos(-botHeading);
            double strafe = (x * Math.cos(-botHeading) - y * Math.sin(-botHeading)) * STRAFE_CORRECTION;
            double turn   = rx;

            double targetShooterRpm = 0.0;

            // Search for our alliance's upward-facing (scorable) Hive Cell
            AprilTagClusterDetection activeCell = findActiveHiveCell();

            // ASSIST MODE 1: Hold Right Bumper to Auto-Aim (Turn) + Auto-Rev Launcher while driving field-centric
            if (gamepad1.right_bumper && activeCell != null) {
                double bearingError = activeCell.ftcPose.bearing;
                turn = Range.clip(-bearingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);

                // Calculate dynamic RPM based on current distance
                targetShooterRpm = BASE_SHOOTER_RPM + ((activeCell.ftcPose.range - DESIRED_DISTANCE) * RPM_PER_INCH);
                telemetry.addData("Assist Mode", "AUTO-AIM + DYNAMIC RPM (field-centric drive)");
            }
            // ASSIST MODE 2: Hold 'A' Button for Full Auto-Square (Range + Strafe + Turn) + Auto-Rev
            // Camera errors are robot-relative, so these powers are NOT rotated by the heading.
            else if (gamepad1.a && activeCell != null) {
                double rangeError   = activeCell.ftcPose.range - DESIRED_DISTANCE;
                double bearingError = activeCell.ftcPose.bearing; // Use Bearing to TURN
                double yawError     = activeCell.ftcPose.yaw;     // Use Yaw to STRAFE

                drive  = Range.clip(rangeError * DRIVE_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
                turn   = Range.clip(-bearingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);
                strafe = Range.clip(yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);

                targetShooterRpm = BASE_SHOOTER_RPM + ((activeCell.ftcPose.range - DESIRED_DISTANCE) * RPM_PER_INCH);
                telemetry.addData("Assist Mode", "FULL AUTO-SQUARE + DYNAMIC RPM (robot-centric)");
            } else {
                telemetry.addData("Assist Mode", "MANUAL FIELD-CENTRIC CONTROL");
            }

            // Normalization denominator
            double denominator = Math.max(Math.abs(drive) + Math.abs(strafe) + Math.abs(turn), 1);

            // Apply Mecanum drivetrain powers (drive/strafe are already in the robot frame here)
            leftFront.setPower((drive + strafe + turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            rightFront.setPower((drive - strafe - turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            leftBack.setPower((drive - strafe + turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            rightBack.setPower((drive + strafe - turn) / denominator * MASTER_DRIVE_MULTIPLIER);

//            // Convert Target RPM to Encoder Ticks Per Second and command the launcher motor
//            double targetTicksPerSec = (targetShooterRpm / 60.0) * TICKS_PER_REV;
//            shooterMotor.setVelocity(targetTicksPerSec);
//
//            // Calculate actual RPM from encoder feedback so drivers know when the wheel is up to speed
//            double currentRpm = (shooterMotor.getVelocity() / TICKS_PER_REV) * 60.0;

            // Display telemetry on the Driver Station
            telemetry.addData("Heading (deg)", "%.1f", Math.toDegrees(botHeading));
            if (activeCell != null) {
                telemetry.addData("Target Cell", activeCell.metadata.name);
                telemetry.addData("Range (in)", "%.1f (Target: %.1f)", activeCell.ftcPose.range, DESIRED_DISTANCE);
                telemetry.addData("Bearing (deg)", "%.1f", activeCell.ftcPose.bearing);
                telemetry.addData("Yaw (deg)", "%.1f", activeCell.ftcPose.yaw);
//                telemetry.addData("Shooter RPM", "%.0f / %.0f", currentRpm, targetShooterRpm);
            } else {
                telemetry.addLine("Target Cell: None visible (Check camera view!)");
            }

            telemetry.update();
        }

        visionPortal.close();
    }

    /**
     * Configures VisionPortal with the BIOBUZZ AprilTag Cluster Library
     */
    private void initAprilTag() {
        aprilTag = new AprilTagProcessor.Builder()
                .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
                .build();

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam1"))
                .addProcessor(aprilTag)
                .build();

        // exposure control tweaks to disable motion blur (disconnects tags recognition)

        // Wait for the webcam stream to finish opening
        while (!isStopRequested() && visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            sleep(20);
        }

        // Now configure exposure and gain safely
        if (!isStopRequested()) {
            ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            GainControl gainControl = visionPortal.getCameraControl(GainControl.class);

            // Switch to manual mode and set exposure
            if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
                exposureControl.setMode(ExposureControl.Mode.Manual);
                sleep(50);
            }

            // 8 to 15 milliseconds prevents motion blur on standard webcams
            exposureControl.setExposure(10, TimeUnit.MILLISECONDS);
            gainControl.setGain(200); // Boost gain to compensate for darker frames
        }
    }

    /**
     * Finds the upward-facing (scorable) Hive Cell for our alliance
     */
    private AprilTagClusterDetection findActiveHiveCell() {
        List<AprilTagDetection> detections = aprilTag.getDetections();

        for (AprilTagDetection detection : detections) {
            if (detection instanceof AprilTagClusterDetection) {
                AprilTagClusterDetection cluster = (AprilTagClusterDetection) detection;

                // 1. Check that the cluster belongs to our alliance ("RED..." or "BLUE...")
                boolean isOurAlliance = cluster.metadata.name.startsWith(ALLIANCE_COLOR);

                // 2. Check Roll: |roll| < 90° means this Cell is right-side up (tilted UP and scorable)
                boolean isTiltedUp = Math.abs(cluster.ftcPose.roll) < 90.0;

                // 3. Require at least 2 of the 4 tags (50%) to be visible for an accurate 3D pose
                boolean hasGoodView = cluster.percentClusterFound >= 50;

                if (isOurAlliance && isTiltedUp && hasGoodView) {
                    return cluster;
                }
            }
        }
        return null;
    }
}
