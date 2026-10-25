package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

@TeleOp(name = "BioBuzz TeleOp Hive AutoAlign & Shoot", group = "BioBuzz")
public class AprilTagDemo extends LinearOpMode {

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

    private DcMotor leftFront, rightFront, leftBack, rightBack;
    private DcMotorEx shooterMotor;
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

        // 2. Initialize Launcher Motor (DcMotorEx enables closed-loop velocity control)
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooterMotorPollen");
        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // 3. Initialize VisionPortal & AprilTagProcessor for BIOBUZZ
        initAprilTag();

        telemetry.addData("Status", "Initialized. Ready for Match Start!");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Manual joystick inputs (gamepad Y axis is negative when pushed forward)
            double drive  = -gamepad1.left_stick_y;
            double strafe =  gamepad1.left_stick_x;
            double turn   =  gamepad1.right_stick_x;

            double targetShooterRpm = 0.0;

            // Search for our alliance's upward-facing (scorable) Hive Cell
            AprilTagClusterDetection activeCell = findActiveHiveCell();

            // ASSIST MODE 1: Hold Right Bumper to Auto-Aim (Turn) + Auto-Rev Launcher while driving manually
            if (gamepad1.right_bumper && activeCell != null) {
                double bearingError = activeCell.ftcPose.bearing;
                turn = Range.clip(-bearingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);

                // Calculate dynamic RPM based on current distance
                targetShooterRpm = BASE_SHOOTER_RPM + ((activeCell.ftcPose.range - DESIRED_DISTANCE) * RPM_PER_INCH);
                telemetry.addData("Assist Mode", "AUTO-AIM + DYNAMIC RPM");
            }
            // ASSIST MODE 2: Hold 'A' Button for Full Auto-Square (Range + Strafe + Turn) + Auto-Rev
            else if (gamepad1.a && activeCell != null) {
                double rangeError   = activeCell.ftcPose.range - DESIRED_DISTANCE;
                double bearingError = activeCell.ftcPose.bearing; // Use Bearing to TURN
                double yawError     = activeCell.ftcPose.yaw;     // Use Yaw to STRAFE

                drive  = Range.clip(rangeError * DRIVE_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
                turn   = Range.clip(-bearingError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);
                strafe = Range.clip(yawError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);

                targetShooterRpm = BASE_SHOOTER_RPM + ((activeCell.ftcPose.range - DESIRED_DISTANCE) * RPM_PER_INCH);
                telemetry.addData("Assist Mode", "FULL AUTO-SQUARE + DYNAMIC RPM");
            } else {
                telemetry.addData("Assist Mode", "MANUAL DRIVER CONTROL");
            }

            // Normalization denominator
            double denominator = Math.max(Math.abs(drive) + Math.abs(strafe) + Math.abs(turn), 1);

            // Apply Mecanum drivetrain powers
            leftFront.setPower((drive + strafe + turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            rightFront.setPower((drive - strafe - turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            leftBack.setPower((drive - strafe + turn) / denominator * MASTER_DRIVE_MULTIPLIER);
            rightBack.setPower((drive + strafe - turn) / denominator * MASTER_DRIVE_MULTIPLIER);

            // Convert Target RPM to Encoder Ticks Per Second and command the launcher motor
            double targetTicksPerSec = (targetShooterRpm / 60.0) * TICKS_PER_REV;
            shooterMotor.setVelocity(targetTicksPerSec);

            // Calculate actual RPM from encoder feedback so drivers know when the wheel is up to speed
            double currentRpm = (shooterMotor.getVelocity() / TICKS_PER_REV) * 60.0;

            // Display telemetry on the Driver Station
            if (activeCell != null) {
                telemetry.addData("Target Cell", activeCell.metadata.name);
                telemetry.addData("Range (in)", "%.1f (Target: %.1f)", activeCell.ftcPose.range, DESIRED_DISTANCE);
                telemetry.addData("Bearing (deg)", "%.1f", activeCell.ftcPose.bearing);
                telemetry.addData("Yaw (deg)", "%.1f", activeCell.ftcPose.yaw);
                telemetry.addData("Shooter RPM", "%.0f / %.0f", currentRpm, targetShooterRpm);
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
//                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
//                .setDrawTagID(true)
//                .setDrawTagOutline(true)

                .build();

          // 2 is standard; use 1 if detecting from far away and robot is stationary
//        aprilTag.setDecimation(2);

        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam1"))
//                .setCameraResolution(new Size(640, 480))
                .addProcessor(aprilTag)
                .build();

        // exposure control tweaks to disable motion blur (disconnects tags recognition)

        // 2. Wait for the webcam stream to finish opening
        while (!isStopRequested() && visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            sleep(20);
        }

        // 3. Now configure exposure and gain safely
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