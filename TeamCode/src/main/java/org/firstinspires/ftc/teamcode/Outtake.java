package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Outtake {
    private DcMotor shooterMotorPollen;

    public void initShooterMotorAndFlickerServo(HardwareMap HwMap) {
        shooterMotorPollen = HwMap.get(DcMotor.class, "shooterMotorPollen");
    }

    public void shoot( double shooterMotorPollenSpeed) {
        shooterMotorPollen.setPower(shooterMotorPollenSpeed);
    }


}