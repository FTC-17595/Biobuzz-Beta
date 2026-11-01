package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    private DcMotor shooterMotorPollen;
    private Servo pollenFlicker;

    public void initShooterMotorAndFlickerServo(HardwareMap HwMap) {
        shooterMotorPollen = HwMap.get(DcMotor.class, "shooterMotorPollen");
        pollenFlicker = HwMap.get(Servo.class,"pollenFlicker");

        shooterMotorPollen.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooterMotorPollen.setDirection(DcMotor.Direction.REVERSE);
    }

    public void shoot(double shooterMotorPollenSpeed) {
        shooterMotorPollen.setPower(shooterMotorPollenSpeed);
    }
    public void flick(double flickPos){
        pollenFlicker.setPosition(flickPos);
    }

}