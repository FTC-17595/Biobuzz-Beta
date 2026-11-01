package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
public class Servopos extends OpMode {

    private Servo Flicker;

    public void init(){
        Flicker = hardwareMap.get(Servo.class,"pollenFlicker");
    }
    public void loop(){
        if (gamepad1.a){
        telemetry.addData("Arm#2", "position=%.2f", Flicker.getPosition());
    }}
}
