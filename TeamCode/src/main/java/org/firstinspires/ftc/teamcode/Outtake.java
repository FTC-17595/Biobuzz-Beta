package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;
import static java.lang.Thread.sleep;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    public DcMotorEx shooterMotorPollen;
    private Servo pollenFlicker;

    public void initShooterMotorAndFlickerServo(HardwareMap HwMap) {
        shooterMotorPollen = HwMap.get(DcMotorEx.class, "shooterMotorPollen");
        pollenFlicker = HwMap.get(Servo.class,"pollenFlicker");

        shooterMotorPollen.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooterMotorPollen.setDirection(DcMotor.Direction.REVERSE);
        shooterMotorPollen.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void shoot(double shooterMotorPollenSpeed) {
        shooterMotorPollen.setVelocity(shooterMotorPollenSpeed);
    }
    public void flick(double flickPos){
        pollenFlicker.setPosition(flickPos);
    }
    private void outputMotorPower(DcMotor motor, String motorName) {
        telemetry.addData(shooterMotorPollen + "Power", motor.getPower());
        telemetry.addData(shooterMotorPollen + "Ticks", motor.getCurrentPosition());
    }
    public void motorVelocity(){
        double velocity = shooterMotorPollen.getVelocity();

        telemetry.addData("Shooter Velocity", shooterMotorPollen.getVelocity());
        if (velocity <= 1325 || velocity >= 1100){
            telemetry.addLine("True");
        }
        else {
            telemetry.addLine("False");
        }
        telemetry.update();
    }
}