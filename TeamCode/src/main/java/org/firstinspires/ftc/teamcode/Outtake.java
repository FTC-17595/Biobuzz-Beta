package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    private DcMotor shooterMotorNectar, shooterMotorPollen;
    private Servo pollenFlicker, nectarFlicker;
    public void initShooterMotorAndFlickerServo(HardwareMap HwMap){
        shooterMotorNectar = HwMap.get(DcMotor.class,"shooterMotorNectar");
        shooterMotorPollen = HwMap.get(DcMotor.class,"shooterMotorPollen");
        pollenFlicker = HwMap.get(Servo.class,"pollenFlicker");
        nectarFlicker = HwMap.get(Servo.class,"nectarFlicker");
    }

    public void shoot(double shooterMotorNectarSpeed, double shooterMotorPollenSpeed, double servoPos, int waitTime) throws InterruptedException {
        shooterMotorNectar.setPower(shooterMotorNectarSpeed);
        shooterMotorPollen.setPower(shooterMotorPollenSpeed);
        pollenFlicker.setPosition(servoPos);
        nectarFlicker.setPosition(servoPos);
        Thread.sleep(waitTime);
        pollenFlicker.setPosition(0);
        nectarFlicker.setPosition(0);

    }
}
