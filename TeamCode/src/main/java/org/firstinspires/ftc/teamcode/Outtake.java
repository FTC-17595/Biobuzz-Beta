package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;
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

    public void shoot(double shooterMotorNectarSpeed, double shooterMotorPollenSpeed) {
        shooterMotorNectar.setPower(shooterMotorNectarSpeed);
        shooterMotorPollen.setPower(shooterMotorPollenSpeed);
    }

    public void flickerLoop(double servoPos) {
        pollenFlicker.setPosition(servoPos);
        nectarFlicker.setPosition(servoPos);
        pollenFlicker.setPosition(0);
        nectarFlicker.setPosition(0);
    }
}
