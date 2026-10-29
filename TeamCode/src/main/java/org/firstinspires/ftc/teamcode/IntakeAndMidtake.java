package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeAndMidtake {
    private DcMotor intakeMotor, midtakeMotor, midtakeSmallMotor;
    public void initIntakeMidtakeMotor(HardwareMap HwMap){
        intakeMotor = HwMap.get(DcMotor.class,"intakeMotor");
        midtakeMotor = HwMap.get(DcMotor.class,"midtakeMotor");
        midtakeSmallMotor = HwMap.get(DcMotor.class,"midtakeMotor2");
    }

    public void runIntakeMidtake(double intakePower, double midtakePower, double secondMidtakeMotorPower){
        intakeMotor.setPower(intakePower);
        midtakeMotor.setPower(midtakePower);
        midtakeSmallMotor.setPower(secondMidtakeMotorPower);
    }

}
